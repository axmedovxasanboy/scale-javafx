package uz.tenzorsoft.scaleapplication.service.sendData;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import uz.tenzorsoft.scaleapplication.domain.response.AllDataResponse;
import uz.tenzorsoft.scaleapplication.domain.response.LocalAndServerIds;
import uz.tenzorsoft.scaleapplication.domain.response.StatusResponse;
import uz.tenzorsoft.scaleapplication.domain.response.sendData.ActionResponse;
import uz.tenzorsoft.scaleapplication.domain.response.sendData.AttachmentResponse;
import uz.tenzorsoft.scaleapplication.domain.response.sendData.UserSendResponse;
import uz.tenzorsoft.scaleapplication.domain.response.sendData.WeighingResponse;
import uz.tenzorsoft.scaleapplication.domain.response.sendData.mycoal.MyCoalData;
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
        List<MyCoalData> request  = truckService.getMyCoalData();


        HttpStatusCode statusCode = restTemplate.postForEntity(
                "", request, Void.class
        ).getStatusCode();

        if (statusCode.isError()) {
            System.err.println("Error with status code: " + statusCode);
        }

    }
}
