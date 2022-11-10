import java.io.*; 
import java.util.*;

public class Lexer {

    public static int line = 1;
    private char peek = ' ';
    
    private void readch(BufferedReader br) {
        try {
            peek = (char) br.read();
        } catch (IOException exc) {
            peek = (char) -1; // ERROR
        }
    }

    public Token lexical_scan(BufferedReader br) {

        while (peek == ' ' || peek == '\t' || peek == '\n'  || peek == '\r') {
            if (peek == '\n') line++;
            readch(br);
        }
        //Inizio modifica commento

            //commenti delimitati con /* e */
            if (peek == '/') {
                boolean commento = true;
                readch(br);
                if (peek == '*') {
                    readch(br);
                    while (commento) {

                        if (peek == '*') {
                            readch(br);
                            if (peek == '/') {
                                commento = false;
                                peek = ' ';
                            } else if (peek == (char) -1) {
                                commento = false;
                                new Token(Tag.EOF);
                            }

                        } else {
                            readch(br);
                            if (peek == (char) -1) {
                                commento = false;
                            }
                        }
                    }

                    while (peek == ' ' || peek == '\t' || peek == '\n' || peek == '\r') {
                        if (peek == '\n') {
                            line++;
                        }
                        readch(br);
                    }

                // commenti che iniziano con // e che terminano con un a capo oppure con EOF
                } else if (peek == '/') {
                    readch(br);
                    while (commento) {
                        if (peek == '\n') {
                            commento = false;
                            peek = ' ';
                        } else if (peek == (char) -1) {
                            commento = false;
                            new Token(Tag.EOF);
                        }
                        readch(br);
                    }
                } else {

                    // non e' nessun commento ma una semplice operazione di divisione
                    return Token.div;
                }

            }
            //Fine modifica commento


        
        switch (peek) {
            case '!':
                peek = ' ';
                return Token.not;

	// ... gestire i casi di (, ), {, }, +, -, *, /, ; ... //

            case '&':
                readch(br);
                if (peek == '&') {
                    peek = ' ';
                    return Word.and;
                } else {
                    System.err.println("Erroneous character"
                            + " after & : "  + peek );
                    return null;
                }
            
            case '(':
                peek = ' ';
                return Token.lpt;

            case ')':
                peek = ' ';
                return Token.rpt;

            case '{':
                peek = ' ';
                return Token.lpg;

            case '}':
                peek = ' ';
                return Token.rpg;

            case '+':
                peek = ' ';
                return Token.plus;

            case '-':
                peek = ' ';
                return Token.minus;

            case '*':
                peek = ' ';
                return Token.mult;

            /*case '/':
                readch(br);

                // commenti che iniziano con // e che terminano con un a capo oppure con EOF
                if (peek == '/') {
                    do {
                        readch(br);
                    } while (peek != '\n');

                //commenti delimitati con /* e *//*
                } else if (peek == '*') {
                    boolean commento = true;
                    do {
                        readch(br);
                        if (peek == '*') {
                            readch(br);
                            if (peek == '/') {
                                commento = false;
                            }
                        }
                    } while (commento);

                    
                } else {            // non e' nessun commento ma una semplice operazione di divisione
                    return Token.div;
                }*/


            case ';':
                peek = ' ';
                return Token.semicolon;

    // ... gestire i casi di ||, <, >, <=, >=, ==, <>, = ... //
    
            case '|':
                readch(br);
                if (peek == '|') {
                    peek = ' ';
                    return Word.or;
                } else {
                    System.err.println("Erroneous character"
                            + " after & : "  + peek );
                    return null;
                }

            case '<':
                readch(br);
                if (peek == '=') {
                    peek = ' ';
                    return Word.le;
                } else if (peek == '>') {
                    peek = ' ';
                    return Word.ne;
                }else {
                    return Word.lt;
                }

            case '>':
                readch(br);
                if (peek == '=') {
                    peek = ' ';
                    return Word.ge;
                } else {
                    return Word.gt;
                }

            case '=':
                readch(br);
                if (peek == '=') {
                    peek = ' ';
                    return Word.eq;
                } else {
                    return Token.assign;
                }

            
          
            case (char)-1:
                return new Token(Tag.EOF);

            default:

            if (peek == '/') {
                readch(br);

                // commenti che iniziano con // e che terminano con un a capo oppure con EOF
                if (peek == '/') {
                    do {
                        readch(br);
                    } while (peek != '\n');

                //commenti delimitati con /* e */
                } else if (peek == '*') {
                    boolean commento = true;
                    do {
                        readch(br);
                        if (peek == '*') {
                            readch(br);
                            if (peek == '/') {
                                commento = false;
                            }
                        }
                    } while (commento);
                    
                } else {            // non e' nessun commento ma una semplice operazione di divisione
                    peek = ' ';
                    return Token.div;
                }
            }
                
            // ... gestire il caso degli identificatori e delle parole chiave //
            if (Character.isLetter(peek) || peek == '_') {
                String parola = "";
                while (Character.isLetter(peek) || peek == '_' || Character.isDigit(peek)) {
                    if (Character.isLetter(peek) || Character.isDigit(peek)) {
                        while (Character.isLetter(peek) || peek == '_' || Character.isDigit(peek)) {
                            parola = String.valueOf(parola) + peek;

                            switch (parola) {

                                case "cond":
                                    peek = ' ';
                                    return Word.cond;

                                case "when":
                                    peek = ' ';
                                    return Word.when;

                                case "then":
                                    peek = ' ';
                                    return Word.then;

                                case "else":
                                    peek = ' ';
                                    return Word.elsetok;

                                case "while":
                                    peek = ' ';
                                    return Word.whiletok;

                                case "do":
                                    peek = ' ';
                                    return Word.dotok;

                                case "seq":
                                    peek = ' ';
                                    return Word.seq;

                                case "print":
                                    peek = ' ';
                                    return Word.print;

                                case "read":
                                    peek = ' ';
                                    return Word.read;

                            }

                            readch(br);
                        }
                    } else if (peek == '_') {
                        parola = String.valueOf(parola) + peek;
                        readch(br);
                        //caso in cui l'identificatore e' composto d soli '_', deve dare errore
                        if(peek == ' ') {

                            System.err.println("Erroneous character: " + peek );
                            return null;
                        }
                    }
                }

                String x = parola;
                    parola = "";
                    return new Word(Tag.ID, x);

                
            }else if (Character.isDigit(peek)) {    // ... gestire il caso dei numeri ... //

                String result = "";
                while (Character.isDigit(peek)) {
                    result = result + peek;
                    readch(br);
                }

                //peek = ' ';
                String x = result;
                result = "";
                return new NumberTok(Integer.parseInt(x));

                    

            } else {
                System.err.println("Erroneous character: " + peek );
                return null;
            }
         }
    }
		
    public static void main(String[] args) {
        Lexer lex = new Lexer();
        String path = "Input.txt"; // il percorso del file da leggere
        try {
            BufferedReader br = new BufferedReader(new FileReader(path));
            Token tok;
            do {
                tok = lex.lexical_scan(br);
                System.out.println("Scan: " + tok);
            } while (tok.tag != Tag.EOF);
            br.close();
        } catch (IOException e) {e.printStackTrace();}    
    }

}
