package uz.tenzorsoft.scaleapplication.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import uz.tenzorsoft.scaleapplication.domain.data.TableViewData;
import uz.tenzorsoft.scaleapplication.domain.response.*;

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

        List<TableViewData> notSentTruckData = truckService.getNotSentData();
        List<UserResponse> notSentUserData = userService.getNotSentData();
        List<CargoResponse> notSentWeighingData = cargoService.getNotSentData();
        List<AttachResponse> notSentAttachmentData = attachService.getNotSentData();

        if (notSentTruckData.isEmpty() && notSentUserData.isEmpty() && notSentWeighingData.isEmpty() && notSentAttachmentData.isEmpty()) {
            return;
        }

        AllDataResponse allDataResponse = new AllDataResponse(notSentTruckData, notSentUserData, notSentWeighingData, notSentAttachmentData);
        RestTemplate restTemplate = new RestTemplate();
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

}
