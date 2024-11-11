package uz.tenzorsoft.scaleapplication.service.sendData;

import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import uz.tenzorsoft.scaleapplication.domain.entity.CargoEntity;
import uz.tenzorsoft.scaleapplication.domain.entity.TruckActionEntity;
import uz.tenzorsoft.scaleapplication.domain.entity.TruckEntity;
import uz.tenzorsoft.scaleapplication.domain.enumerators.TruckAction;
import uz.tenzorsoft.scaleapplication.domain.response.AllDataResponse;
import uz.tenzorsoft.scaleapplication.domain.response.LocalAndServerIds;
import uz.tenzorsoft.scaleapplication.domain.response.StatusResponse;
import uz.tenzorsoft.scaleapplication.domain.response.sendData.ActionResponse;
import uz.tenzorsoft.scaleapplication.domain.response.sendData.AttachmentResponse;
import uz.tenzorsoft.scaleapplication.domain.response.sendData.UserSendResponse;
import uz.tenzorsoft.scaleapplication.domain.response.sendData.WeighingResponse;
import uz.tenzorsoft.scaleapplication.domain.response.sendData.mycoal.*;
import uz.tenzorsoft.scaleapplication.repository.CargoRepository;
import uz.tenzorsoft.scaleapplication.service.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static uz.tenzorsoft.scaleapplication.domain.Instances.*;

@Service
@RequiredArgsConstructor
public class SendDataService {

    private final TruckService truckService;
    private final UserService userService;
    private final RestTemplate restTemplate;
    private final CargoService cargoService;
    private final AttachService attachService;
    private final CargoRepository cargoRepository;
    private final TruckActionService truckActionService;

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
                    camera3Connection, sensor1Connection, sensor2Connection, sensor3Connection
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
            CargoEntity cargo = cargoService.findByTruckId(truckEntity.getId());
            MyCoalData myCoalData = new MyCoalData();
            TruckActionEntity exitedWeigh = new TruckActionEntity();
            for (TruckActionEntity action : truckEntity.getTruckActions()) {
                if (action.getAction().equals(TruckAction.EXIT) || action.getAction().equals(TruckAction.MANUAL_EXIT)) {
                    exitedWeigh = action;
                    break;
                }
            }
            TruckActionEntity enteredWeigh = new TruckActionEntity();
            for (TruckActionEntity action : truckEntity.getTruckActions()) {
                if (action.getAction().equals(TruckAction.ENTRANCE) || action.getAction().equals(TruckAction.MANUAL_ENTRANCE)) {
                    enteredWeigh = action;
                    break;
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
            myCoalData.setHeft(new Heft(
                    Math.max(exitedWeigh.getWeight(), enteredWeigh.getWeight()),
                    Math.min(exitedWeigh.getWeight(), enteredWeigh.getWeight()),
                    Math.max(exitedWeigh.getWeight(), enteredWeigh.getWeight()),
                    cargo != null ? cargo.getNetWeight() : null
            ));

            request.add(myCoalData);
        }


        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<List<MyCoalData>> entity = new HttpEntity<>(request, headers);

        Object body = restTemplate.exchange("https://api.mycoal.uz/be/api/v1/scales/save-list", HttpMethod.POST, entity, Object.class).getBody();


        if(body == null){
            return;
        }
        if(body.toString().length() < 5) return;

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

    private static String getLocalDateTime(TruckActionEntity enteredWeigh) {
        LocalDateTime createdAt = enteredWeigh.getCreatedAt();
        if (createdAt == null) createdAt = LocalDateTime.now();
        return createdAt.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss"));
    }
}
