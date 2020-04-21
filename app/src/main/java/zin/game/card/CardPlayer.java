package zin.game.card;

import java.util.ArrayList;
import java.util.List;

public class CardPlayer {

    private List<Card> cards;

    public CardPlayer(List<Card> cards) {
        this.cards = cards;
    }

    public CardPlayer() {
        this.cards = new ArrayList<Card>();
    }

    public List<Card> getCards() {
        return cards;
    }

    public void setCards(List<Card> cards) {
        this.cards = cards;
    }

    public void giveMoreCard(Card card) {
        cards.add(card);
    }

    public void giveMoreCards(List<Card> list) {
        cards.addAll(list);
    }
    
}
