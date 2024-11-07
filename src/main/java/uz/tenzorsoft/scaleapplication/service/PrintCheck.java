package uz.tenzorsoft.scaleapplication.service;

import org.springframework.stereotype.Service;
import uz.tenzorsoft.scaleapplication.domain.response.TruckResponse;
import com.fazecast.jSerialComm.SerialPort;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.io.OutputStream;


@Service
public class PrintCheck {

    private static final byte[] INIT_PRINTER = {0x1B, 0x40};
    private static final byte[] CUT_PAPER = {0x1D, 'V', 1};

    public void printReceipt(TruckResponse response) {
        try {
            SerialPort serialPort = findThermalPrinterPort();
            if (serialPort == null) {
                System.out.println("Printer not found.");
                return;
            }

            if (!serialPort.openPort()) {
                System.out.println("Failed to open the port.");
                return;
            }

            serialPort.setBaudRate(9600);
            serialPort.setNumDataBits(8);
            serialPort.setNumStopBits(1);
            serialPort.setParity(SerialPort.NO_PARITY);
            serialPort.setFlowControl(SerialPort.FLOW_CONTROL_DISABLED);

            String receipt = buildReceiptContent(response);

            try (OutputStream outputStream = serialPort.getOutputStream()) {
                outputStream.write(INIT_PRINTER);
                outputStream.write(receipt.getBytes(StandardCharsets.UTF_8));
                outputStream.write(CUT_PAPER);
                outputStream.flush();
                System.out.println("Receipt printed successfully.");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private SerialPort findThermalPrinterPort() {
        SerialPort[] ports = SerialPort.getCommPorts();
        for (SerialPort port : ports) {
            if (port.getSystemPortName().toLowerCase().contains("ttyusb") || port.getSystemPortName().toLowerCase().contains("com")) {
                return port;
            }
        }
        return null;
    }

    private String buildReceiptContent(TruckResponse response) {
        String truckNumber = response.getTruckNumber();
        LocalDateTime enteredAt = response.getEnteredAt();
        LocalDateTime exitedAt = response.getExitedAt();
        Double enteredWeight = response.getEnteredWeight() != null ? response.getEnteredWeight() : 0.0;
        Double exitedWeight = response.getExitedWeight() != null ? response.getExitedWeight() : 0.0;
        String operatorNumber = response.getExitConfirmedBy();

        double netto = Math.abs(enteredWeight - exitedWeight);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
        String formattedEnteredAt = enteredAt != null ? enteredAt.format(formatter) : "N/A";
        String formattedExitedAt = exitedAt != null ? exitedAt.format(formatter) : "N/A";

        return String.format(
                new String(INIT_PRINTER) +
                        "       Sirdayo       \n" +
                        "№ авто: %s\n" +
                        "Прибыл: %s\n" +
                        "Убыл: %s\n" +
                        "Вес (кг): %.0f\n" +
                        "Тара (кг): %.0f\n" +
                        "БРУТТО (кг): %.0f\n" +
                        "НЕТТО (кг): %.0f\n" +
                        "Оператор: %s\n" +
                        new String(CUT_PAPER),
                truckNumber, formattedEnteredAt, formattedExitedAt,
                enteredWeight, exitedWeight, enteredWeight, netto, operatorNumber
        );
    }
}
