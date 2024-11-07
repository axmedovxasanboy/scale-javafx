package uz.tenzorsoft.scaleapplication.service;

import org.springframework.stereotype.Service;
import uz.tenzorsoft.scaleapplication.domain.response.TruckResponse;

import javax.print.*;

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

        // Convert the receipt to bytes (to be sent to the printer)
        byte[] receiptBytes = receipt.toString().getBytes();

        // Set the printer to use (USB port 'usb001')
        PrintService[] printServices = PrintServiceLookup.lookupPrintServices(null, null);
        PrintService selectedPrinter = null;

        for (PrintService printer : printServices) {
            if (printer.getName().contains("XP-T80Q")) { // Match the printer name
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
            } catch (PrintException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("Printer not found!");
        }
    }


}
