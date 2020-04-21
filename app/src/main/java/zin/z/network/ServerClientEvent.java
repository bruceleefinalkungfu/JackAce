package zin.z.network;


public enum ServerClientEvent {

    /**
     * The most important event. The real message is followed after that
     */
    MESSAGE_RECEIVED_SUCCESSFULLY,
    
    
    MESSAGE_SENT_SUCCESSFULLY,
    FAILED_TO_SEND_THE_MESSAGE,

    NOTIFYING_SERVER_IP_INFORM_THE_CLIENT_OF_IT,
    
    CLIENT_FAILED_TO_CONNECT_TO_SERVER,
    SERVER_FAILED_TO_CONNECT_A_CLIENT,
    
    EXCEPTION_TO_BE_IGNORED_DURING_DISCONNECT,
    EXCEPTION_TO_BE_IGNORED_WHEN_FAILED_TO_RECEIVE_MESSAGE,
    ;    
}
