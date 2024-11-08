package uz.tenzorsoft.scaleapplication.service;

import org.springframework.stereotype.Service;
import uz.tenzorsoft.scaleapplication.domain.response.TruckResponse;

import javax.print.*;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class PrintCheck {

    public void printReceipt(TruckResponse response) {
        PrintService printer = findPrinter("XP-80C (copy 1)"); // Specify your printer name here
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

    private String buildReceiptContent(TruckResponse response) {
        String truckNumber = response.getTruckNumber();
        LocalDateTime enteredAt = response.getEnteredAt();
        Double enteredWeight = response.getEnteredWeight() != null ? response.getEnteredWeight() : 0.0;
        LocalDateTime exitedAt = response.getExitedAt();
        Double exitedWeight = response.getExitedWeight() != null ? response.getExitedWeight() : 0.0;
        double tara = Math.min(enteredWeight, exitedWeight);
        double brutto = Math.max(enteredWeight, exitedWeight);
        double netto = Math.abs(enteredWeight - exitedWeight);
        String operatorNumber = response.getExitConfirmedBy();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
        String formattedEnteredAt = enteredAt != null ? enteredAt.format(formatter) : "N/A";
        String formattedExitedAt = exitedAt != null ? exitedAt.format(formatter) : "N/A";

        StringBuilder receiptContent = new StringBuilder();
        receiptContent.append("       Sirdayo  Baxt      \n")
                .append("--------------------------\n")
                .append("Moshina raqami: ").append(truckNumber).append("\n")
                .append("Kirgan vaqti: ").append(formattedEnteredAt).append("\n")
                .append("Vazni (kg): ").append(String.format("%.0f", enteredWeight)).append("\n")
                .append("Chiqgan vaqti: ").append(formattedExitedAt).append("\n")
                .append("Vazni (kg): ").append(String.format("%.0f", exitedWeight)).append("\n")
                .append("Tare (kg): ").append(String.format("%.0f", tara)).append("\n")
                .append("GROSS (kg): ").append(String.format("%.0f", brutto)).append("\n")
                .append("NETTO (kg): ").append(String.format("%.0f", netto)).append("\n")
                .append("Operator: ").append(operatorNumber).append("\n")
                .append("--------------------------\n")
                .append("   Thank you for visiting!   \n\n\n\n\n\n\n\n\n\n\n");

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
