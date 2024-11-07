package uz.tenzorsoft.scaleapplication.service;

import org.springframework.stereotype.Service;

import javax.print.*;
import javax.print.attribute.*;
import javax.print.attribute.standard.Copies;
import java.io.ByteArrayInputStream;
import java.io.InputStream;


public class POSPrinter {
    private String printerName;

    public POSPrinter(String printerName) {
        this.printerName = printerName;
    }

    // Method to find the specified printer
    private PrintService findPrinter() {
        PrintService[] services = PrintServiceLookup.lookupPrintServices(null, null);
        for (PrintService service : services) {
            if (service.getName().equalsIgnoreCase(printerName)) {
                return service;
            }
        }
        System.out.println("Printer not found.");
        return null;
    }

    // Method to print text to the printer
    public void printReceipt(String text) {
        PrintService printService = findPrinter();
        if (printService == null) {
            System.out.println("Error: Printer not available.");
            return;
        }

        try {
            DocPrintJob job = printService.createPrintJob();
            InputStream stream = new ByteArrayInputStream(text.getBytes());
            Doc doc = new SimpleDoc(stream, DocFlavor.INPUT_STREAM.AUTOSENSE, null);

            PrintRequestAttributeSet attributes = new HashPrintRequestAttributeSet();
            attributes.add(new Copies(1)); // Set the number of copies to 1

            job.print(doc, attributes); // Send the print job to the printer
            System.out.println("Receipt printed successfully.");
            stream.close();
        } catch (Exception e) {
            System.out.println("Error printing receipt: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
