package zin.z.network;

public interface ServerClientEventCallback {
    
    public void handleOtherEvent(String event);
    
    public void onReceiveMessage(String receivedMessage);
    
    default void publishEvent (String event) {
        System.out.println(event);
        if(event.contains(ServerClientEvent.MESSAGE_RECEIVED_SUCCESSFULLY.toString())) {
            onReceiveMessage(event.replaceAll
                 (ServerClientEvent.MESSAGE_RECEIVED_SUCCESSFULLY.toString() + " ", ""));
        } else {
            handleOtherEvent(event);
        }
    }
    
    @Deprecated
    static ServerClientEventCallback instance() {
        return new ServerClientEventCallback() {
            
            @Override
            public void onReceiveMessage(String receivedMessage) {
                
            }
            
            @Override
            public void handleOtherEvent(String event) {
            
            }
        };
    }
    
}
