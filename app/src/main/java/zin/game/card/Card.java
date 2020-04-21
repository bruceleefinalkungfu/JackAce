package zin.game.card;

public class Card {

    private int  number;
    private Suit suit;
    
    public Card(int number, Suit suit) {
        super();
        this.number = number;
        this.suit = suit;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public Suit getSuit() {
        return suit;
    }

    public void setSuit(Suit suit) {
        this.suit = suit;
    }

    public static enum Suit {
        SPADE, CLUB, HEART, DIAMOND;
    }

    public static String getActualCardValueInsteadOfNumber(int num) {
        if(num <= 10)
            return String.valueOf(num);
        else if(num == 11)
            return "J";
        else if(num == 12)
            return "Q";
        else if(num == 13)
            return "K";
        else if(num == 14 || num == 1)
            return "A";
        else
            return "Joker";
    }

}
