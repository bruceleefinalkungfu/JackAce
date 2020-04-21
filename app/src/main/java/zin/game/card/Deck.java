package zin.game.card;

import java.util.ArrayList;
import java.util.List;

import zin.game.card.Card.Suit;

public class Deck {

    private int pointer = 0;
    
    private Card[] cards;

    public void shuffle() {
        pointer = 0;
        cards = new Card[52];
        Suit[] SUITS = { Suit.CLUB, Suit.DIAMOND, Suit.HEART, Suit.SPADE }; 
        
        int[] RANKS = { 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14 };
        int n = SUITS.length * RANKS.length; // a deck consists of 4*13 cards
        for (int i = 0; i < RANKS.length; i++) {
            for (int j = 0; j < SUITS.length; j++) {
                cards[i* SUITS.length + j] = new Card(RANKS[i], SUITS[j]);
            }
        }
        // shuffle
        for (int i = 0; i < n; i++) {
            int r = i + (int)(Math.random() * (n - i));
            Card temp = cards[r];
            cards[r] = cards[i];
            cards[i] = temp;
            
        }

    }
    
    public List<Card> getNCards(int n) {
        List<Card> list = new ArrayList<Card>();
        int oldPointer = pointer;
        for (int j = pointer ; j<cards.length && j< oldPointer+n ; j++) {
            list.add(cards[j]);
            pointer ++;
        }
        return list;
    }

}
