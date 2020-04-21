package zin.game.card.jackace;

import zin.game.card.Card;

public class JackAceCard extends Card {

    public JackAceCard(int number, Suit suit) {
        super(number, suit);
    }

    public int getNumber(int givenCurrentTotal) {
        if(givenCurrentTotal + this.getNumber() > 25 && isFaceCard())
            return this.getNumber() - 10;
        return this.getNumber();
    }

    public boolean isFaceCard() {
        return this.getNumber() > 10;
    }
    
}
