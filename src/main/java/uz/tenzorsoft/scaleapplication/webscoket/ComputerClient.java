package uz.tenzorsoft.scaleapplication.webscoket;

import jakarta.annotation.PostConstruct;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import uz.tenzorsoft.scaleapplication.service.ControllerService;

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
                session.subscribe("/topic/commands", this); // "/topic/commands" bu yerda mavzuga obuna bo'lamiz
                logger.info("WebSocket ulanish muvaffaqiyatli amalga oshirildi!");
            }

            @SneakyThrows
            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                if (payload instanceof CommandsDto) {
                    CommandsDto commandsDto = (CommandsDto) payload; // payload ni CommandsDto turiga kasting qilamiz

                    logger.info("Xabar kelib tushdi: {}", commandsDto);

                    if (commandsDto.getOpenGate1() != null && commandsDto.getOpenGate1()) {
                        controllerService.openGate1(); // Kirish darvozasi 1 ni ochish
                        logger.info("Kirish darvozasi 1 ochildi");
                    } else if (commandsDto.getCloseGate1() != null && commandsDto.getCloseGate1()) {
                        controllerService.closeGate1(); // Kirish darvozasi 1 ni yopish
                        logger.info("Kirish darvozasi 1 yopildi");
                    }
                    // Boshqa buyruqlarni shu tarzda qo'shish mumkin
                } else {
                    logger.error("Xato payload turi keldi: {}", payload.getClass().getName());
                }
            }
        };

        try {
            stompClient.connect(WEBSOCKET_URL, sessionHandler).get();
        } catch (InterruptedException | ExecutionException e) {
            logger.error("WebSocket ulanishi amalga oshirilishda xatolik: {}", e.getMessage());
        }
    }
}
