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
        SerialPort serialPort = findThermalPrinterPort();
        if (serialPort == null) {
            System.out.println("Printer not found.");
            return;
        }

        try {
            if (!serialPort.openPort()) {
                System.out.println("Failed to open the port.");
                return;
            }

            configureSerialPort(serialPort);

            String receiptContent = buildReceiptContent(response);
            sendToPrinter(serialPort, receiptContent);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (serialPort.isOpen()) {
                serialPort.closePort();
            }
        }
    }

    private void configureSerialPort(SerialPort serialPort) {
        serialPort.setBaudRate(9600);
        serialPort.setNumDataBits(8);
        serialPort.setNumStopBits(SerialPort.ONE_STOP_BIT);
        serialPort.setParity(SerialPort.NO_PARITY);
        serialPort.setFlowControl(SerialPort.FLOW_CONTROL_DISABLED);
    }

    private SerialPort findThermalPrinterPort() {
        SerialPort[] ports = SerialPort.getCommPorts();
        for (SerialPort port : ports) {
            if (port.getSystemPortName().toLowerCase().contains("usb") || port.getSystemPortName().toLowerCase().contains("com")) {
                return port;
            }
        }
        return null;
    }

    private void sendToPrinter(SerialPort serialPort, String receiptContent) throws Exception {
        try (OutputStream outputStream = serialPort.getOutputStream()) {
            outputStream.write(INIT_PRINTER);
            outputStream.write(receiptContent.getBytes(StandardCharsets.UTF_8));
            outputStream.write(CUT_PAPER);
            outputStream.flush();
            System.out.println("Receipt printed successfully.");
        }
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
                "       Sirdayo       \n" +
                        "№ авто: %s\n" +
                        "Прибыл: %s\n" +
                        "Убыл: %s\n" +
                        "Вес (кг): %.0f\n" +
                        "Тара (кг): %.0f\n" +
                        "БРУТТО (кг): %.0f\n" +
                        "НЕТТО (кг): %.0f\n" +
                        "Оператор: %s\n",
                truckNumber, formattedEnteredAt, formattedExitedAt,
                enteredWeight, exitedWeight, enteredWeight, netto, operatorNumber
        );
    }
}
