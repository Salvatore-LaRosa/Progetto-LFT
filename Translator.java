import java.io.*;

public class Translator {
    private Lexer lex;
    private BufferedReader pbr;
    private Token look;
    
    SymbolTable st = new SymbolTable();
    CodeGenerator code = new CodeGenerator();
    int count=0;

    public Translator(Lexer l, BufferedReader br) {
        lex = l;
        pbr = br;
        move();
    }

    void move() { 
        look = lex.lexical_scan(pbr);
        System.err.println("token = " + look);
    }

    void error(String s) { 
        throw new Error("near line " + lex.line + ": " + s);
    }

    void match(int t) {
        if (look.tag == t) {
            if (look.tag != Tag.EOF){ 
                move();
            }
        } else {
            error("errore di sintassi");
        }
    }

    public void prog() {   
        if (look.tag == '=' || look.tag == Tag.PRINT ||  look.tag == Tag.READ || look.tag == Tag.COND || look.tag == Tag.WHILE || look.tag == '{') {
            int lnext_prog = code.newLabel();

            statlist(lnext_prog);

            code.emitLabel(lnext_prog);
            
            match(Tag.EOF);
            try {
        	    code.toJasmin();
            }
            catch(java.io.IOException e) {
        	    System.out.println("IO error\n");
            };
        } else {
            System.out.println("Error in grammar (prog) after read( with " + look);
        }
    }

    private void statlist(int lnext) {
        if (look.tag == '=' || look.tag == Tag.PRINT ||  look.tag == Tag.READ || look.tag == Tag.COND || look.tag == Tag.WHILE || look.tag == '{') {
            stat(lnext);
            statlistp(lnext);
        } else {
            System.out.println("Error in grammar (statlist) after read( with " + look);
        }
    }

    private void statlistp(int lnext) {
	    if (look.tag == ';') {
            match(';');

            stat(lnext);
            statlistp(lnext);
        } // else {eps}
    }

    public void stat( int lnext ) {
        switch(look.tag) {
            case '=':
                match('=');
                if (look.tag==Tag.ID) {
                    int id_addr = st.lookupAddress(((Word)look).lexeme);
                    if (id_addr==-1) {
                        id_addr = count;
                        st.insert(((Word)look).lexeme,count++);
                    }                    
                    match(Tag.ID);

                    expr();

                    code.emit(OpCode.istore,id_addr);
                } 
                break;

            case Tag.PRINT:
                match(Tag.PRINT);
                match('(');

                exprlist();

                code.emit(OpCode.invokestatic,1);

                match(')');
                break;
            case Tag.READ:
                match(Tag.READ);
                match('(');
                if (look.tag==Tag.ID) {
                    int id_addr = st.lookupAddress(((Word)look).lexeme);
                    if (id_addr==-1) {
                        id_addr = count;
                        st.insert(((Word)look).lexeme,count++);
                    }                    
                    match(Tag.ID);
                    match(')');

                    code.emit(OpCode.invokestatic,0);
                    code.emit(OpCode.istore,id_addr);
                }
                else
                    error("Error in grammar (stat) after read( with " + look);
                break;
            case Tag.COND:
                match(Tag.COND);

                int ltrue = code.newLabel();
                int lfalse = code.newLabel();
                int lbl = code.newLabel();

                whenlist(ltrue, lfalse);

                lnext = code.newLabel();
                code.emit(OpCode.GOto, lbl);

                match(Tag.ELSE);

                code.emitLabel(lfalse);

                stat(lbl);

                code.emitLabel(lbl);
                break;
            case Tag.WHILE:
                match(Tag.WHILE);
                match('(');

                int ltrue1 = code.newLabel();
                int lfalse1 = code.newLabel();
                int lprev = code.newLabel();

                code.emitLabel(lprev);

                bexpr(ltrue1);
                match(')');

                code.emit(OpCode.GOto, lfalse1);    //Se bexpr e' falsa esce da while saltando stat(lnext)
                code.emitLabel(ltrue1);             //Se bexpr e' vera andra' qua

                stat(lnext);

                code.emit(OpCode.GOto, lprev);      //Ritorno a lprev

                code.emitLabel(lfalse1);            //Se bexpr e' falsa andra' qua
                break;

            case '{':
                match('{');

                statlist(lnext);

                match('}');
                break;
        }
     }

     private void whenlist(int ltrue, int lfalse) {
         if (look.tag == Tag.WHEN) {
             whenitem(ltrue, lfalse);
             whenlistp(ltrue, lfalse);
         } else {
            System.out.println("Error in grammar (whenlist) after read( with " + look);
         }
     }

     private void whenlistp(int ltrue, int lfalse) {
        if (look.tag == Tag.WHEN) {
            whenitem(ltrue, lfalse);
            whenlistp(ltrue, lfalse);
        } // else {eps}
    }

    private void whenitem(int ltrue, int lfalse) {
        if (look.tag == Tag.WHEN) {
            match(Tag.WHEN);
            match('(');

            bexpr(ltrue);

            match(')');
            match(Tag.DO);

            code.emit(OpCode.GOto, lfalse);     //Se bexpr e' falsa esce dall'if saltando stat(lfalse)
            code.emitLabel(ltrue);              //Se bexpr e' vera andra' qua

            stat(lfalse);
        } else {
            System.out.println("Error in grammar (whenitem) after read( with " + look);
        }

    }

     private void bexpr(int ltrue) {
        if (look == Word.eq) {
            match(Tag.RELOP);

            expr();
            expr();

            code.emit(OpCode.if_icmpeq, ltrue);

        } else if (look == Word.ne) {
            match(Tag.RELOP);

            expr();
            expr();

            code.emit(OpCode.if_icmpne, ltrue);

        } else if (look == Word.ge) {
            match(Tag.RELOP);

            expr();
            expr();

            code.emit(OpCode.if_icmpge, ltrue);

        } else if (look == Word.le) {
            match(Tag.RELOP);

            expr();
            expr();

            code.emit(OpCode.if_icmple, ltrue);

        } else if (look == Word.gt) {
            match(Tag.RELOP);

            expr();
            expr();

            code.emit(OpCode.if_icmpgt, ltrue);

        } else if (look == Word.lt) {
            match(Tag.RELOP);

            expr();
            expr();

            code.emit(OpCode.if_icmplt, ltrue);

        } else {
            System.out.println("Error in grammar (bexpr) after read( with " + look);
        }


     }
     
     private void expr() {
        if (look.tag == '+' || look.tag == '-' || look.tag == '*' || look.tag == '/' || look.tag == Tag.NUM || look.tag == Tag.ID) {
            switch(look.tag) {
                case '+':
                    match('+');
                    match('(');

                    exprlist();

                    match(')');

                    code.emit(OpCode.iadd);
                    break;
                case '-':
                    match('-');

                    expr();
                    expr();

                    code.emit(OpCode.isub);
                    break;
                case '*':
                    match('*');
                    match('(');

                    exprlist();

                    match(')');

                    code.emit(OpCode.imul);
                    break;
                case '/':
                    match('/');

                    expr();
                    expr();

                    code.emit(OpCode.idiv);
                    break;
                case Tag.NUM:
                    NumberTok x = (NumberTok) look;
                    int num = x.numero;

                    code.emit(OpCode.ldc, num);

                    match(Tag.NUM);
                    break;
                case Tag.ID:
                    int read_id_addr = st.lookupAddress(((Word) look).lexeme);

                    match(Tag.ID);

                    code.emit(OpCode.iload, read_id_addr);
                    break;

            }

        } else {
            System.out.println("Error in grammar (expr) after read( with " + look);
        }
    }

    private void exprlist() {
        if (look.tag == '+' || look.tag == '-' || look.tag == '*' || look.tag == '/' || look.tag == Tag.NUM || look.tag == Tag.ID) {
            expr();
            exprlistp();
        } else {
            System.out.println("Error in grammar (exprlist) after read( with " + look);
        }
    }

    private void exprlistp() {
        if (look.tag == '+' || look.tag == '-' || look.tag == '*' || look.tag == '/' || look.tag == Tag.NUM || look.tag == Tag.ID) {
            expr();
            exprlistp();
        } // else {eps}
    }

    public static void main(String[] args) {
        Lexer lex = new Lexer();
        String path = "Input.txt"; // il percorso del file da leggere
        try {
            BufferedReader br = new BufferedReader(new FileReader(path));
            Translator translator = new Translator(lex, br);
            translator.prog();
            System.out.println("Input OK");
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}