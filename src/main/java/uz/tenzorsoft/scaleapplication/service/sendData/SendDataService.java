package uz.tenzorsoft.scaleapplication.service.sendData;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatusCode;
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
import uz.tenzorsoft.scaleapplication.service.AttachService;
import uz.tenzorsoft.scaleapplication.service.CargoService;
import uz.tenzorsoft.scaleapplication.service.TruckService;
import uz.tenzorsoft.scaleapplication.service.UserService;

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

    public void sendNotSentData() {

        List<ActionResponse> notSentTruckData = truckService.getNotSentData();
        List<UserSendResponse> notSentUserData = userService.getNotSentData();
        List<WeighingResponse> notSentWeighingData = cargoService.getNotSentData();
        List<AttachmentResponse> notSentAttachmentData = attachService.getNotSentData();

        if (notSentTruckData.isEmpty() && notSentUserData.isEmpty() && notSentWeighingData.isEmpty() && notSentAttachmentData.isEmpty()) {
            return;
        }

        AllDataResponse allDataResponse = new AllDataResponse(notSentTruckData, notSentUserData, notSentWeighingData, notSentAttachmentData);
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

        List<TruckEntity> trucks = truckService.findAll();

        if (trucks.isEmpty()) {return;}

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
            myCoalData.setTarozi_id(1L);
            myCoalData.setRfid("");
            myCoalData.setAvto_number(truckEntity.getTruckNumber());
            myCoalData.setFul_name("");
            myCoalData.setTex_pass_number("");
            myCoalData.setOrg_name_buyer("");
            myCoalData.setOrg_name_seller(cargo == null ? "" : cargo.getScaleName());
            myCoalData.setProduct(new ProductResponse());
            myCoalData.setCheck(new CheckResponse(
                    enteredWeigh.getCreatedAt() != null ? enteredWeigh.getCreatedAt().toString() : "",
                    exitedWeigh.getCreatedAt() != null ? exitedWeigh.getCreatedAt().toString() : ""
            ));
            myCoalData.setAccord(new AccordResponse());
            myCoalData.setDoverennost(new Doverennost());
            myCoalData.setHeft(new Heft(
                    Math.max(exitedWeigh.getWeight(), enteredWeigh.getWeight()),
                    Math.min(exitedWeigh.getWeight(), enteredWeigh.getWeight()),
                    Math.max(exitedWeigh.getWeight(), enteredWeigh.getWeight()),
                    cargo != null ? cargo.getNetWeight() : null
            ));

            request.add(myCoalData);
        }


//        HttpStatusCode statusCode = restTemplate.postForEntity(
//                "https://api.mycoal.uz/be/api/v1/scales/save-list", request, Void.class
//        ).getStatusCode();

        HttpEntity<?> entity = new HttpEntity<>(request);

        HttpStatusCode statusCode = restTemplate.exchange(
                "https://api.mycoal.uz/be/api/v1/scales/save-list",
                HttpMethod.POST,
                entity, Void.class
        ).getStatusCode();


        if (statusCode.isError()) {
            System.err.println("Error with status code: " + statusCode);
        }
        if (statusCode.is2xxSuccessful()) {
            System.out.println("Successfully sent data to MyCoal\nstatus code " + statusCode);
        }

    }
}
