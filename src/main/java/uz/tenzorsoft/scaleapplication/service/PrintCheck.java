package uz.tenzorsoft.scaleapplication.service;

import org.springframework.stereotype.Service;
import uz.tenzorsoft.scaleapplication.domain.response.TruckResponse;

import javax.print.*;
import java.nio.charset.StandardCharsets;

@Service
public class PrintCheck {

    public static void printReceipt(TruckResponse response) {
        // Define your receipt content
        StringBuilder receipt = new StringBuilder();
        String escInit = "\u001B\u0040"; // Initialize ESC/POS command

        receipt.append(escInit);
        receipt.append("       Truck Receipt       \n\n");
        receipt.append("Truck Number: ").append(response.getTruckNumber()).append("\n");
        receipt.append("Entered Weight: ").append(response.getEnteredWeight()).append(" kg\n");
        receipt.append("Exited Weight: ").append(response.getExitedWeight()).append(" kg\n");
        double netWeight = Math.abs(response.getEnteredWeight() - response.getExitedWeight());
        receipt.append("Net Weight: ").append(netWeight).append(" kg\n");
        receipt.append("Operator: ").append(response.getExitConfirmedBy()).append("\n\n");

        // Convert the receipt to bytes (to be sent to the printer) with UTF-8 encoding
        byte[] receiptBytes = receipt.toString().getBytes(StandardCharsets.UTF_8);

        // Check for available printers and find the target printer
        PrintService[] printServices = PrintServiceLookup.lookupPrintServices(null, null);
        PrintService selectedPrinter = null;

        for (PrintService printer : printServices) {
            System.out.println();
            System.out.println();
            System.out.println();
            System.out.println();
            System.out.println("Available printer: " + printer.getName()); // List available printers
            if (printer.getName().equalsIgnoreCase("XP-T80Q")) { // Match the printer name exactly
                selectedPrinter = printer;
                break;
            }

        }

        if (selectedPrinter != null) {
            try {
                // Create a print job
                DocPrintJob printJob = selectedPrinter.createPrintJob();

                // Create a Doc object with the receipt bytes
                Doc doc = new SimpleDoc(receiptBytes, DocFlavor.BYTE_ARRAY.AUTOSENSE, null);

                // Print the receipt
                printJob.print(doc, null);
                System.out.println("Print job sent to printer: " + selectedPrinter.getName());
            } catch (PrintException e) {
                System.err.println("Failed to print: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            System.out.println("Printer 'XP-T80Q' not found! Make sure the printer name matches exactly.");
        }
    }
}
