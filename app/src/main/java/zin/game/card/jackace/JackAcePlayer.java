package zin.game.card.jackace;

import java.util.ArrayList;
import java.util.List;

import zin.game.card.Card;
import zin.game.card.CardPlayer;
import zin.z.network.ServerClientEventCallback;

import static zin.game.card.jackace.JackAceGameEvent.*;

public class JackAcePlayer extends CardPlayer {

    private ServerClientEventCallback callBack;
    
    private List<Integer> cardValues = new ArrayList<>();
    
    private int totalCount = 0;
    
    private String type;
    
    public JackAcePlayer(ServerClientEventCallback callback, String type) {
        super();
        this.callBack = callback;
        this.type = type;
    }
    
    public JackAcePlayer(List<Card> cards, ServerClientEventCallback callback, String type) {
        super(cards);
        this.type = type;
        this.callBack = callback;
        for(Card c : cards) {
            cardValues.add(c.getNumber()); totalCount+= c.getNumber();
        }
        //cards.forEach(e -> { cardValues.add(e.getNumber()); totalCount+= e.getNumber(); });
    }
    
    public void receiveOneCard(Card c) {
        this.giveMoreCard(c);
        JackAceCard jackAceCard = (JackAceCard) c;
        int num = jackAceCard.getNumber(totalCount);
        cardValues.add(num);
        if(totalCount + num > 25) {
            // decrease the first face card's value
            for(int i=0 ; i<cardValues.size() ; i++) {
                if(cardValues.get(i) > 10) {
                    cardValues.set(i, cardValues.get(i) - 10);
                    break;
                }
            }
        }
        totalCount = getTotal();
        callBack.publishEvent( (type.equals("SERVER") ? SERVER_RECEIVED_THE_CARD
                :CLIENT_RECEIVED_THE_CARD) +" "+c.getNumber()+" "+c.getSuit());
        if(totalCount > 25)
            callBack.publishEvent(I_LOST + " " + totalCount);
    }
    
    public int getTotal() {
        totalCount = 0;
        for(Integer c : cardValues)
            totalCount += c;
        return totalCount;
    }
    
}
