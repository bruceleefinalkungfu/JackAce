package zin.game.card.jackace;

import java.util.ArrayList;
import java.util.List;

import zin.game.card.Card;
import zin.game.card.Card.Suit;
import zin.game.card.Deck;

public class JackAceDeck {

    Deck deck = new Deck();
    public JackAceDeck() {
        deck.shuffle();
    }

    public List<JackAceCard> getNCards(int n) {
        List<Card> cards = deck.getNCards(n);
        List<JackAceCard> list = new ArrayList<>();
        for(Card c : cards) {
            JackAceCard jackAceCard = new JackAceCard(c.getNumber(), c.getSuit());
            list.add(jackAceCard);
        }
        return list;
    }

}
