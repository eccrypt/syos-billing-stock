package web.websockets;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.util.*;

@ServerEndpoint("/inventory-updates")
public class InventorySocket {
    private static Set<Session> sessions = Collections.synchronizedSet(new HashSet<>());

    @OnOpen
    public void onOpen(Session session) { sessions.add(session); }

    @OnMessage
    public void onMessage(String message, Session session) throws Exception {
        for (Session s : sessions) {
            if (s.isOpen()) {
                s.getBasicRemote().sendText("Broadcast: " + message);
            }
        }
    }

    @OnClose
    public void onClose(Session session) { sessions.remove(session); }
}