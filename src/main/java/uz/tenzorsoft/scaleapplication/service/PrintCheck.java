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

        String receipt = buildReceiptContent(response); // Get the formatted receipt content

        try (InputStream inputStream = new ByteArrayInputStream(receipt.getBytes(StandardCharsets.ISO_8859_1))) {
            // Use ISO-8859-1 encoding for compatibility with many thermal printers
            DocPrintJob printJob = printer.createPrintJob(); // Create a print job
            Doc doc = new SimpleDoc(inputStream, DocFlavor.INPUT_STREAM.AUTOSENSE, null); // Create document

            // Print the receipt
            printJob.print(doc, null); // Print the document
            System.out.println("Receipt printed successfully.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private PrintService findPrinter(String printerName) {
        PrintService[] printServices = PrintServiceLookup.lookupPrintServices(null, null);
        for (PrintService printService : printServices) {
            if (printService.getName().equalsIgnoreCase(printerName)) {
                return printService; // Return the matching printer
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

        receiptContent.append("       Sirdayo       \n")
                .append("--------------------------\n");

        // Truck Data
        receiptContent.append("Moshina raqami: ").append(truckNumber).append("\n")
                .append("Kirgan vaqti: ").append(formattedEnteredAt).append("\n")
                .append("Vazni (kg): ").append(String.format("%.0f", enteredWeight)).append("\n")
                .append("Chiqgan vaqti: ").append(formattedExitedAt).append("\n")
                .append("Vazni (kg): ").append(String.format("%.0f", exitedWeight)).append("\n")
                .append("Tare (kg): ").append(String.format("%.0f", tara)).append("\n")
                .append("GROSS (kg): ").append(String.format("%.0f", brutto)).append("\n")
                .append("NETTO (kg): ").append(String.format("%.0f", netto)).append("\n")
                .append("Operator: ").append(operatorNumber).append("\n");

        // Ending the receipt with a "Thank you" note
        receiptContent.append("--------------------------\n")
                .append("   Thank you for visiting!   \n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n");

        return receiptContent.toString(); // Return the formatted receipt content
    }

    public void listAvailablePrinters() {
        PrintService[] printServices = PrintServiceLookup.lookupPrintServices(null, null);

        if (printServices.length == 0) {
            System.out.println("No printers found.");
        } else {
            System.out.println("Available Printers:");
            for (PrintService printer : printServices) {
                System.out.println(printer.getName()); // List all available printers
            }
        }
    }
}
