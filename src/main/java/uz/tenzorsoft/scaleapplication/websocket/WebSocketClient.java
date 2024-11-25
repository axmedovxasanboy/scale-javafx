package uz.tenzorsoft.scaleapplication.websocket;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.websocket.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import uz.tenzorsoft.scaleapplication.domain.Instances;
import uz.tenzorsoft.scaleapplication.domain.entity.LogEntity;
import uz.tenzorsoft.scaleapplication.domain.request.CommandsRequest;
import uz.tenzorsoft.scaleapplication.service.CommandsService;
import uz.tenzorsoft.scaleapplication.service.LogService;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

@Component
@ClientEndpoint
public class WebSocketClient {

    @Autowired
    private CommandsService commandsService;

    @Autowired
    private ObjectMapper objectMapper; // JSON-ni pars qilish uchun

    private Session session;
    @Autowired
    private LogService logService;

    @OnOpen
    public void onOpen(Session session) {
        System.out.println("Connected to server");
        this.session = session;
    }

    @OnMessage
    public void onMessage(String message) {
        try {
            if (message.startsWith("CommandsDto")) {
                System.out.println("Processing commands...");
                CommandsRequest commands = parseCommandsRequest(message);
                commandsService.saveOrUpdateCommands(commands);
            }
//            else if (message.startsWith("Started") || message.startsWith("Completed")) {
//                System.out.println(message);
//            } else {
//                logService.save(new LogEntity(5L, Instances.truckNumber, "Unrecognized message format: " + message));
//                System.err.println("Unrecognized message format: " + message);
//            }
        } catch (Exception e) {
            logService.save(new LogEntity(5L, Instances.truckNumber, e.getMessage()));
            System.err.println("Error processing message: " + e.getMessage());
            System.err.println("Raw message: " + message);
        }
    }


    private CommandsRequest parseCommandsRequest(String message) {
        String jsonValue = convertToJson(message);
        CommandsRequest commands = new CommandsRequest();
        try {
            commands = objectMapper.readValue(jsonValue, CommandsRequest.class);
//            commands.setScaleId(Long.parseLong(extractValue(message, "scaleId")));
//            commands.setOpenGate1(Boolean.parseBoolean(extractValue(message, "openGate1")));
//            commands.setCloseGate1(Boolean.parseBoolean(extractValue(message, "closeGate1")));
//            commands.setWeighing(Boolean.parseBoolean(extractValue(message, "weighing")));
//            commands.setOpenGate2(Boolean.parseBoolean(extractValue(message, "openGate2")));
//            commands.setCloseGate2(Boolean.parseBoolean(extractValue(message, "closeGate2")));
        } catch (JsonProcessingException e) {
            logService.save(new LogEntity(5L, Instances.truckNumber, e.getMessage()));
            System.err.println(e.getMessage());
        }
        return commands;
    }

    private String convertToJson(String dtoString) {
        // Convert "CommandsDto{id=54, scaleId=1, ...}" to JSON: {"id":54, "scaleId":1, ...}
        String json = dtoString.replace("CommandsDto{", "{");

        // Replace equals signs with colons and wrap keys in quotes
        json = json.replaceAll("([a-zA-Z0-9_]+)=", "\"$1\":");

        // Replace boolean values (true/false) to ensure JSON compatibility
        json = json.replace("true", "true").replace("false", "false");

        return json;
    }

    @OnClose
    public void onClose(Session session, CloseReason reason) {
        System.out.println("Connection closed: " + reason.getReasonPhrase());
        try {
            connect("wss://api-scale.mycoal.uz/ws");
        } catch (URISyntaxException | DeploymentException | IOException e) {
            System.err.println(e.getMessage());
        }
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        System.err.println("Error: " + throwable.getMessage());
        try {
            connect("wss://api-scale.mycoal.uz/ws");
        } catch (URISyntaxException | DeploymentException | IOException e) {
            logService.save(new LogEntity(5L, Instances.truckNumber, e.getMessage()));
            System.err.println(e.getMessage());
        }
    }

    public void connect(String uri) throws URISyntaxException, DeploymentException, IOException {
        WebSocketContainer container = ContainerProvider.getWebSocketContainer();
        container.connectToServer(this, new URI(uri));
    }

    public void sendMessage(String message) {
        if (session != null && session.isOpen()) {
            session.getAsyncRemote().sendText(message);
        } else {
            System.err.println("Cannot send message. Session is not open.");
            logService.save(new LogEntity(5L, Instances.truckNumber, "Cannot send message. Session is not open."));
            try {
                connect(Instances.WEBSOCKET_URL);
            } catch (Exception e) {
                logService.save(new LogEntity(5L, Instances.truckNumber, e.getMessage()));
                System.err.println(e.getMessage());
            }
        }
    }

    @Scheduled(fixedRate = 500)
    public void sendPeriodicMessage() {
        sendMessage("/sendCommends" + Instances.currentUser.getInternalScaleId());
    }
}
