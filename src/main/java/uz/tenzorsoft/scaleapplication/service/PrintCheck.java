package uz.tenzorsoft.scaleapplication.service;

import org.springframework.stereotype.Service;
import uz.tenzorsoft.scaleapplication.domain.entity.TruckActionEntity;
import uz.tenzorsoft.scaleapplication.domain.entity.TruckEntity;
import uz.tenzorsoft.scaleapplication.domain.enumerators.TruckAction;
import uz.tenzorsoft.scaleapplication.domain.response.TruckResponse;

import javax.print.*;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class PrintCheck {


    public void printReceipt(TruckEntity response) {
        PrintService printer = findPrinter("XP-80C (copy 2)"); // Specify your printer name here
        if (printer == null) {
            System.out.println("Printer not found.");
            return;
        }

        String receipt = buildReceiptContent(response);

        try (InputStream inputStream = new ByteArrayInputStream(receipt.getBytes(StandardCharsets.ISO_8859_1))) {
            DocPrintJob printJob = printer.createPrintJob();
            Doc doc = new SimpleDoc(inputStream, DocFlavor.INPUT_STREAM.AUTOSENSE, null);

            // Print the receipt
            printJob.print(doc, null);

            // Send the cut command after printing
            sendCutCommand(printer);
            System.out.println("Receipt printed and paper cut successfully.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private PrintService findPrinter(String printerName) {
        PrintService[] printServices = PrintServiceLookup.lookupPrintServices(null, null);
        for (PrintService printService : printServices) {
            if (printService.getName().equalsIgnoreCase(printerName)) {
                return printService;
            }
        }
        return null;
    }

    private String buildReceiptContent(TruckEntity response) {
        String truckNumber = response.getTruckNumber();
        String operatorNumber = "N/A";
        LocalDateTime enteredAt = null;
        LocalDateTime exitedAt = null;
        Double exitedWeight = 0.0;
        Double enteredWeight = 0.0;

        for (TruckActionEntity truckAction : response.getTruckActions()) {
            if (truckAction.getAction() == TruckAction.ENTRANCE || truckAction.getAction() == TruckAction.MANUAL_ENTRANCE)
            {
                enteredAt = truckAction.getCreatedAt();
                enteredWeight = truckAction.getWeight();
            }
            if(truckAction.getAction() == TruckAction.EXIT || truckAction.getAction() == TruckAction.MANUAL_EXIT){
                exitedAt = truckAction.getCreatedAt();
                exitedWeight = truckAction.getWeight();
                operatorNumber = truckAction.getOnDuty().getPhoneNumber();
            }
        }
        double tara = Math.min(enteredWeight, exitedWeight);
        double brutto = Math.abs(enteredWeight - exitedWeight);
        double netto = Math.abs(enteredWeight - exitedWeight);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
        String formattedEnteredAt = enteredAt != null ? enteredAt.format(formatter) : "N/A";
        String formattedExitedAt = exitedAt != null ? exitedAt.format(formatter) : "N/A";

        StringBuilder receiptContent = new StringBuilder();
        receiptContent.append("       Sirdayo  Baxt      \n")
                .append("--------------------------\n")
                .append("Moshina raqami: ").append(truckNumber).append("\n")
                .append("Kirish vaqti: ").append(formattedEnteredAt).append("\n")
                .append("Kirish vazni (kg): ").append(String.format("%.0f", enteredWeight)).append("\n")
                .append("Chiqish vaqti: ").append(formattedExitedAt).append("\n")
                .append("Chiqish vazni (kg): ").append(String.format("%.0f", exitedWeight)).append("\n")
                .append("Tara (kg): ").append(String.format("%.0f", tara)).append("\n")
                .append("Sof og'irlik (kg): ").append(String.format("%.0f", brutto)).append("\n")
                .append("Operator: ").append(operatorNumber).append("\n")
                .append("--------------------------\n")
                .append("   Tashrifingiz uchun raxmat!   \n\n\n\n\n\n\n\n\n\n\n");

        return receiptContent.toString();
    }


    private void sendCutCommand(PrintService printer) {
        try {
            // ESC/POS command to cut paper
            byte[] cutCommand = new byte[]{0x1D, 0x56, 0x42, 0x00};
            DocPrintJob printJob = printer.createPrintJob();
            Doc doc = new SimpleDoc(cutCommand, DocFlavor.BYTE_ARRAY.AUTOSENSE, null);
            printJob.print(doc, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void listAvailablePrinters() {
        PrintService[] printServices = PrintServiceLookup.lookupPrintServices(null, null);
        if (printServices.length == 0) {
            System.out.println("No printers found.");
        } else {
            System.out.println("Available Printers:");
            for (PrintService printer : printServices) {
                System.out.println(printer.getName());
            }
        }
    }
}

