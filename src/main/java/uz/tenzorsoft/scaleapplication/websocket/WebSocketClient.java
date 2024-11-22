package uz.tenzorsoft.scaleapplication.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.websocket.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uz.tenzorsoft.scaleapplication.domain.request.CommandsRequest;
import uz.tenzorsoft.scaleapplication.service.CommandsService;

import java.net.URI;

@Component
@ClientEndpoint
public class WebSocketClient {

    @Autowired
    private CommandsService commandsService;

    @Autowired
    private ObjectMapper objectMapper; // JSON-ni pars qilish uchun

    private Session session;

    @OnOpen
    public void onOpen(Session session) {
        System.out.println("Connected to server");
        this.session = session;
    }

    @OnMessage
    public void onMessage(String message) {
        System.out.println("Received message: " + message);
        try {
            if (message.startsWith("CommandsDto")) {
                CommandsRequest commands = parseCommandsRequest(message);
                System.out.println("Parsed CommandsDto: " + commands);
                // Saqlash yoki yangilash uchun yangi metodni chaqiring
                commandsService.saveOrUpdateCommands(commands);
            } else if (message.startsWith("Started") || message.startsWith("Completed")) {
                System.out.println("Log message: " + message);
            } else {
                System.err.println("Unrecognized message format: " + message);
            }
        } catch (Exception e) {
            System.err.println("Error processing message: " + e.getMessage());
            System.err.println("Raw message: " + message);
        }
    }


    // "CommandsDto(scaleId=1, openGate1=false, ...)" ko‘rinishidagi matnni tahlil qilish
    private CommandsRequest parseCommandsRequest(String message) {
        CommandsRequest commands = new CommandsRequest();
        commands.setScaleId(Long.parseLong(extractValue(message, "scaleId")));
        commands.setOpenGate1(Boolean.parseBoolean(extractValue(message, "openGate1")));
        commands.setCloseGate1(Boolean.parseBoolean(extractValue(message, "closeGate1")));
        commands.setWeighing(Boolean.parseBoolean(extractValue(message, "weighing")));
        commands.setOpenGate2(Boolean.parseBoolean(extractValue(message, "openGate2")));
        commands.setCloseGate2(Boolean.parseBoolean(extractValue(message, "closeGate2")));
        return commands;
    }

    private String extractValue(String message, String key) {
        int startIndex = message.indexOf(key + "=") + key.length() + 1;
        int endIndex = message.indexOf(",", startIndex);
        if (endIndex == -1) endIndex = message.indexOf(")", startIndex);
        return message.substring(startIndex, endIndex).trim();
    }


    @OnClose
    public void onClose(Session session, CloseReason reason) {
        System.out.println("Connection closed: " + reason.getReasonPhrase());
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        System.err.println("Error: " + throwable.getMessage());
    }

    public void connect(String uri) throws Exception {
        WebSocketContainer container = ContainerProvider.getWebSocketContainer();
        container.connectToServer(this, new URI(uri));
    }

    public void sendMessage(String message) {
        if (session != null && session.isOpen()) {
            session.getAsyncRemote().sendText(message);
        } else {
            System.err.println("Cannot send message. Session is not open.");
        }
    }
}
