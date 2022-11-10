import java.io.*;

public class Parser {
    private Lexer lex;
    private BufferedReader pbr;
    private Token look;

    public Parser(Lexer l, BufferedReader br) {
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
        statlist();
        match(Tag.EOF);
    }

    private void statlist() {
        stat();
        statlistp();
    }

    private void statlistp() {
	    if (look.tag == ';') {
            move();
            stat();
            statlistp();
        }
    }

    private void stat() {
	    switch (look.tag) {
            case '=':
                move();
                match(Tag.ID);
                expr();
                break;

            case Tag.PRINT:
                move();
                match(Token.lpt.tag);
                exprlist();
                match(Token.rpt.tag);
                break;

            case Tag.READ:
                move();
                match(Token.lpt.tag);
                match(Tag.ID);
                match(Token.rpt.tag);
                break;

            case Tag.COND:
                move();
                whenlist();
                if (look.tag == Tag.ELSE) {
                    move();
                    stat();
                }
                break;

            case Tag.WHILE:
                move();
                match('(');
                bexpr();
                match(')');
                stat();
                statlist();
                break;

            case '{':
                move();
                statlist();
                match('}');
                break;

        }
    }

    private void whenlist() {
        if (look.tag == Tag.WHEN) {
            whenitem();
            whenlistp();
        }
    }

    private void whenlistp() {
        if (look.tag == Tag.WHEN) {
            whenitem();
            whenlistp();
        }
    }

    private void whenitem() {
        move();
        match('(');
        bexpr();
        match(')');
        match(Tag.DO);
        stat();
    }

    private void bexpr() {
        if(look.tag == Tag.RELOP) {
            move();
            expr();
            expr();
        }
    }

    private void expr() {
        switch(look.tag){
            case '+':
                move();
                match('(');
                exprlist();
                match(')');
                break;

            case '-':
                move();
                expr();
                expr();
                break;  
            
            case '*':
                move();
                match('(');
                exprlist();
                match(')');
                break;
            
            case '/':
                move();
                expr();
                expr();
                break;
            
            case Tag.NUM:
                move();
                break;

            case Tag.ID:
                move();
                break;

        }
    }

    private void exprlist() {
        expr();
        exprlistp();
    }

    private void exprlistp() {
        if (look.tag == Tag.NUM || look.tag == Tag.ID || look.tag == '+' || look.tag == '-' || look.tag == '*' || look.tag == '/') {
            expr();
            exprlistp();
        }
    }
		
    public static void main(String[] args) {
        Lexer lex = new Lexer();
        String path = "Input.txt"; // il percorso del file da leggere
        try {
            BufferedReader br = new BufferedReader(new FileReader(path));
            Parser parser = new Parser(lex, br);
            parser.prog();
            System.out.println("Input OK");
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
