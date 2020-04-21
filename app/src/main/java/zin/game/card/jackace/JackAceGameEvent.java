package zin.game.card.jackace;


public enum JackAceGameEvent {

    /**
     * Another player is gonna send these messages
     */
    SERVER_RECEIVED_THE_CARD,
    CLIENT_RECEIVED_THE_CARD,
    MY_TURN_IS_OVER,
    
    I_WON,
    I_LOST,
    I_NEED_CARD,
    I_DONT_NEED_CARD_ANYMORE,
    HERES_A_CARD,
    ;
}
