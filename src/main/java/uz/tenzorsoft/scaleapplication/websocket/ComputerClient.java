package uz.tenzorsoft.scaleapplication.websocket;

import com.ghgande.j2mod.modbus.ModbusException;
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
import java.util.Map;
import java.util.concurrent.ExecutionException;

@Service
public class ComputerClient {

    private static final String WEBSOCKET_URL = "ws://scale.mycoal.uz/ws";
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
                session.subscribe("/topic/door-control", this);
                logger.info("WebSocket ulanish muvaffaqiyatli amalga oshirildi!");
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                if (payload instanceof Map<?, ?>) {
                    Map<String, Boolean> doorStatus = (Map<String, Boolean>) payload;

                    try {
                        if (doorStatus.getOrDefault("openGate1", false)) {
                            controllerService.openGate1();
                            logger.info("Kirish darvozasi 1 ochildi");
                        } else if (doorStatus.getOrDefault("closeGate1", false)) {
                            controllerService.closeGate1();
                            logger.info("Kirish darvozasi 1 yopildi");
                        } else if (doorStatus.getOrDefault("openGate2", false)) {
                            controllerService.openGate2();
                            logger.info("Chiqish darvozasi 2 ochildi");
                        } else if (doorStatus.getOrDefault("closeGate2", false)) {
                            controllerService.closeGate2();
                            logger.info("Chiqish darvozasi 2 yopildi");
                        } else {
                            logger.warn("Noma'lum buyruq qabul qilindi!");
                        }
                    } catch (ModbusException e) {
                        logger.error("Modbus xatolik yuz berdi: {}", e.getMessage());
                    }
                } else {
                    logger.error("Noma'lum payload turi qabul qilindi: {}", payload.getClass().getName());
                }
            }
        };

        try {
            stompClient.connect(WEBSOCKET_URL, sessionHandler).get();
        } catch (InterruptedException | ExecutionException e) {
            logger.error("WebSocket ulanishi amalga oshirilishda xatolik yuz berdi: {}", e.getMessage());
        }
    }
}
