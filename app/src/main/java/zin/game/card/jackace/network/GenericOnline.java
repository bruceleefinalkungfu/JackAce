package zin.game.card.jackace.network;

import java.util.HashSet;
import java.util.Set;

public class GenericOnline {
    Set<String> messageIdsRead = new HashSet<>();
    public void addMessageIdAsRead(String msgId) {
        messageIdsRead.add(msgId);
    }
    public boolean isMsgAlreadyRead(String msgId) {
        return messageIdsRead.contains(msgId);
    }
}
