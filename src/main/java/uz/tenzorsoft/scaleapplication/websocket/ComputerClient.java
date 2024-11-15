package uz.tenzorsoft.scaleapplication.websocket;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uz.tenzorsoft.scaleapplication.service.ControllerService;

import javax.annotation.PostConstruct;
import java.util.concurrent.ExecutionException;

@Service
public class ComputerClient {

    private static final String WEBSOCKET_URL = "ws://192.168.68.136:9090/ws"; // Serverning IP manzili va porti
    private static final Logger logger = LoggerFactory.getLogger(ComputerClient.class);

    private final ControllerService controllerService;

    @Autowired
    public ComputerClient(ControllerService controllerService) {
        this.controllerService = controllerService;
    }

    @PostConstruct
    public void connectToWebSocket() {
        WebSocketStompClient stompClient = new WebSocketStompClient(new StandardWebSocketClient());
        StompSessionHandlerAdapter sessionHandler = new StompSessionHandlerAdapter() {

            @Override
            public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
                session.subscribe("/topic/commands", this);
                logger.info("WebSocket ulanish muvaffaqiyatli amalga oshirildi!");
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                logger.info("Xabar kelib tushdi: {}", payload);
            }
        };

        try {
            stompClient.connect(WEBSOCKET_URL, sessionHandler).get();
        } catch (InterruptedException | ExecutionException e) {
            logger.error("WebSocket ulanishi amalga oshirilishda xatolik: {}", e.getMessage());
        }
    }
}
