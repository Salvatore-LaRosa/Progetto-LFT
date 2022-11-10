public class NumberTok extends Token {
    int numero = 0;
    public NumberTok(int x) { super(Tag.NUM); numero = x; }
    public String toString() { return "<" + Tag.NUM + ", " + numero + ">"; }	       
}