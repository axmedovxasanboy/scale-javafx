package uz.tenzorsoft.scaleapplication.service.sendData;

import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import uz.tenzorsoft.scaleapplication.domain.Instances;
import uz.tenzorsoft.scaleapplication.domain.entity.CargoEntity;
import uz.tenzorsoft.scaleapplication.domain.entity.LogEntity;
import uz.tenzorsoft.scaleapplication.domain.entity.TruckActionEntity;
import uz.tenzorsoft.scaleapplication.domain.entity.TruckEntity;
import uz.tenzorsoft.scaleapplication.domain.enumerators.TruckAction;
import uz.tenzorsoft.scaleapplication.domain.response.AllDataResponse;
import uz.tenzorsoft.scaleapplication.domain.response.LocalAndServerIds;
import uz.tenzorsoft.scaleapplication.domain.response.StatusResponse;
import uz.tenzorsoft.scaleapplication.domain.response.sendData.*;
import uz.tenzorsoft.scaleapplication.domain.response.sendData.mycoal.*;
import uz.tenzorsoft.scaleapplication.service.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static uz.tenzorsoft.scaleapplication.domain.Instances.*;

@Service
@RequiredArgsConstructor
public class SendDataService {

    private final TruckService truckService;
    private final UserService userService;
    private final RestTemplate restTemplate;
    private final CargoService cargoService;
    private final AttachService attachService;
    private final TruckActionService truckActionService;
    private final LogService logService;

    public void sendNotSentData() {

        List<ActionResponse> notSentTruckData = truckService.getNotSentData();
        List<UserSendResponse> notSentUserData = userService.getNotSentData();
        List<WeighingResponse> notSentWeighingData = cargoService.getNotSentData();
        List<AttachmentResponse> notSentAttachmentData = attachService.getNotSentData();

        if (notSentTruckData.isEmpty() && notSentUserData.isEmpty() && notSentWeighingData.isEmpty() && notSentAttachmentData.isEmpty()) {
            return;
        }

        AllDataResponse allDataResponse = new AllDataResponse(notSentTruckData, notSentUserData, notSentWeighingData, null);
        LocalAndServerIds body = restTemplate.postForObject(
                "https://api-scale.mycoal.uz/remote/localAndServerIds",
                allDataResponse, LocalAndServerIds.class
        );
        if (body == null) {
            return;
        }
        truckService.dataSent(notSentTruckData, body.getAction());
        userService.dataSent(notSentUserData, body.getUser());
        cargoService.dataSent(notSentWeighingData, body.getWeighing());

        notSentAttachmentData = attachService.getNotSentData();
        allDataResponse = new AllDataResponse(null, null, null, notSentAttachmentData);
        body = restTemplate.postForObject(
                "https://api-scale.mycoal.uz/remote/localAndServerIds",
                allDataResponse, LocalAndServerIds.class
        );

        if (body == null) {
            return;
        }

        attachService.dataSent(notSentAttachmentData, body.getAttach());

        System.out.println(body);

    }

    public void sendStatuses() {
        try {
            StatusResponse statusResponse = new StatusResponse(
                    isConnected, gate1Connection, gate2Connection, camera1Connection, camera2Connection,
                    camera3Connection, sensor1Connection, sensor2Connection, sensor3Connection, currentUser.getScaleId()
            );

            RestTemplate restTemplate = new RestTemplate();
            HttpStatusCode statusCode = restTemplate.postForEntity(
                    "https://api-scale.mycoal.uz/remote/getAllDatchikStatus",
                    statusResponse, Void.class
            ).getStatusCode();
            boolean error = statusCode.isError();
            if (error) {
                System.err.println("Error occurred with status code: " + statusCode);
            }

        } catch (Exception e) {
            logService.save(new LogEntity(5L, truckNumber, "00016: (" + getClass().getName() + ") " + e.getMessage()));
            e.printStackTrace();
            System.err.println("Unable to send data");
        }
    }

    public void sendDataToMyCoal() {
        List<MyCoalData> request = new ArrayList<>();

        List<TruckEntity> trucks = truckService.findNotSentDataToMyCoal();

        if (trucks.isEmpty()) {
            return;
        }

        for (TruckEntity truckEntity : trucks) {
            if (!isSendAvailable(truckEntity)) continue;
            CargoEntity cargo = cargoService.findByTruckId(truckEntity.getId());
            MyCoalData myCoalData = new MyCoalData();
            TruckActionEntity exitedWeigh = new TruckActionEntity();
            TruckActionEntity noneAction = new TruckActionEntity();
            TruckActionEntity enteredWeigh = new TruckActionEntity();

            for (TruckActionEntity action : truckEntity.getTruckActions()) {

                if (action.getAction() == null) {
                    noneAction = action;
                    continue;
                }

                if (action.getAction().equals(TruckAction.EXIT) || action.getAction().equals(TruckAction.MANUAL_EXIT)) {
                    exitedWeigh = action;
                    continue;
                }

                if (action.getAction().equals(TruckAction.ENTRANCE) || action.getAction().equals(TruckAction.MANUAL_ENTRANCE)) {
                    enteredWeigh = action;
                    continue;
                }
            }


            myCoalData.setId(truckEntity.getId());
            myCoalData.setNp(0L);
            myCoalData.setTarozi_id(5L);
//            myCoalData.setRfid("");
            myCoalData.setAvto_number(truckEntity.getTruckNumber());
//            myCoalData.setFul_name("");
//            myCoalData.setTex_pass_number("");
//            myCoalData.setOrg_name_buyer("");
            myCoalData.setOrg_name_seller(cargo == null ? "" : cargo.getScaleName());
            myCoalData.setProduct(new ProductResponse());
            myCoalData.setCheck(new CheckResponse(
                    getLocalDateTime(enteredWeigh), getLocalDateTime(exitedWeigh)
            ));
            myCoalData.setAccord(new AccordResponse(null, LocalDate.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))));
            myCoalData.setDoverennost(new Doverennost(null, LocalDate.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))));
            if (noneAction.getId() != null) {
                if (enteredWeigh.getAction() == null) {
                    enteredWeigh = noneAction;
                    enteredWeigh.setAction(TruckAction.ENTRANCE);
                }

                if (exitedWeigh.getAction() == null) {
                    exitedWeigh = noneAction;
                    exitedWeigh.setAction(TruckAction.EXIT);
                }
            }

            double brutto = Math.max(exitedWeigh.getWeight(), enteredWeigh.getWeight());
            double tara = Math.min(exitedWeigh.getWeight(), enteredWeigh.getWeight());
            myCoalData.setHeft(new Heft(
                    brutto, tara, brutto,
                    cargo != null ? cargo.getNetWeight() : null
            ));

            request.add(myCoalData);
        }


        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<List<MyCoalData>> entity = new HttpEntity<>(request, headers);

        Object body = restTemplate.exchange("https://api.mycoal.uz/be/api/v1/scales/save-list", HttpMethod.POST, entity, Object.class).getBody();


        if (body == null) {
            return;
        }
        if (body.toString().length() < 5) return;

        for (TruckEntity truck : trucks) {
            for (TruckActionEntity actionEntity : truck.getTruckActions()) {
                actionEntity.setIsSentToMyCoal(true);
                truckActionService.save(actionEntity);
            }
            truck.setIsSentToMyCoal(true);
            truckService.save(truck);

        }

        System.out.println("My Coal Data body = " + body);

    }

    private boolean isSendAvailable(TruckEntity truckEntity) {
        boolean isEntered = false;
        boolean isExited = false;
        for (TruckActionEntity action : truckEntity.getTruckActions()) {
            switch (action.getAction()) {
                case MANUAL_ENTRANCE, ENTRANCE -> isEntered = true;
                case MANUAL_EXIT, EXIT -> isExited = true;
            }
        }
        return isEntered && isExited;
    }

    public void sendLogsToServer() {
        LogResponse request = new LogResponse();
        List<LogEntity> notSentLogs = logService.getNotSentLogs();
        if (notSentLogs.isEmpty()) return;
        request.setDtoList(notSentLogs);
        ResponseEntity<LocalAndServerIds> response = restTemplate.postForEntity(
                "https://api-scale.mycoal.uz/logs/create",
                request, LocalAndServerIds.class
        );
        System.out.println("Log response: " + response);

        Map<Long, Long> data = Objects.requireNonNull(response.getBody()).getData();
        logService.dataSent(notSentLogs, data);

    }


    private static String getLocalDateTime(TruckActionEntity enteredWeigh) {
        LocalDateTime createdAt = enteredWeigh.getCreatedAt();
        if (createdAt == null) createdAt = LocalDateTime.now();
        return createdAt.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss"));
    }


}
