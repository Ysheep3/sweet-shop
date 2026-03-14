package com.sweet.order.websocket;

import com.sweet.common.exception.BaseException;
import org.springframework.stereotype.Component;

import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@ServerEndpoint("/ws/rider/{riderId}")
public class RiderWebSocketServer {

    // ⭐ 线程安全
    private static final Map<Long, Session> riderSessionMap = new ConcurrentHashMap<>();

    /**
     * 建立连接
     */
    @OnOpen
    public void onOpen(Session session, @PathParam("riderId") Long riderId) {
        riderSessionMap.put(riderId, session);
        System.out.println("骑手上线: " + riderId);
    }

    /**
     * 接收消息（一般用不到）
     */
    @OnMessage
    public void onMessage(String message, @PathParam("riderId") Long riderId) {
        System.out.println("收到骑手消息: " + riderId + " -> " + message);
    }

    /**
     * 断开连接
     */
    @OnClose
    public void onClose(@PathParam("riderId") Long riderId) {
        riderSessionMap.remove(riderId);
        System.out.println("骑手下线: " + riderId);
    }

    /**
     * 服务器主动推送给骑手
     */
    public void sendToRider(Long riderId, String message) {

        Session session = riderSessionMap.get(riderId);

        if (session != null && session.isOpen()) {
            try {
                session.getAsyncRemote().sendText(message);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            throw new BaseException("骑手不在线: " + riderId);
        }
    }
}
