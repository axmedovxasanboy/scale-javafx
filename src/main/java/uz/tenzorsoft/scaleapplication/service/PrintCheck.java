package uz.tenzorsoft.scaleapplication.service;

import uz.tenzorsoft.scaleapplication.domain.response.TruckResponse;

import javax.print.*;
import java.awt.*;
import java.awt.print.Book;
import java.awt.print.PageFormat;
import java.awt.print.Printable;

public class PrintCheck {

    public void printReceipt(TruckResponse response) {
        // Locate printer by name
        PrintService selectedPrinter = findPrinter("Xprinter XP-T80Q");
        if (selectedPrinter == null) {
            System.out.println("Printer not found!");
            return;
        }

        // Create a DocPrintJob from the selected printer
        DocPrintJob job = selectedPrinter.createPrintJob();

        // Define a Printable to render custom TruckResponse fields
        Printable printable = (graphics, pageFormat, pageIndex) -> {
            if (pageIndex > 0) {
                return Printable.NO_SUCH_PAGE;
            }

            Graphics2D g2d = (Graphics2D) graphics;
            g2d.translate(pageFormat.getImageableX(), pageFormat.getImageableY());

            // Set custom font and layout
            g2d.setFont(new Font("Monospaced", Font.PLAIN, 10));

            // Render TruckResponse fields on receipt
            g2d.drawString("       Truck Receipt       ", 10, 10);
            g2d.drawString("Truck Number: " + response.getTruckNumber(), 10, 30);
            g2d.drawString("Entered Weight: " + response.getEnteredWeight() + " kg", 10, 50);
            g2d.drawString("Exited Weight: " + response.getExitedWeight() + " kg", 10, 70);

            double netto = Math.abs(response.getEnteredWeight() - response.getExitedWeight());
            g2d.drawString("Net Weight: " + netto + " kg", 10, 90);
            g2d.drawString("Operator: " + response.getExitConfirmedBy(), 10, 110);

            return Printable.PAGE_EXISTS;
        };

        // Add Printable to a Book for printing
        Book book = new Book();
        book.append(printable, new PageFormat());

        // Prepare a Doc with PRINTABLE format for the printer
        Doc doc = new SimpleDoc(book, DocFlavor.SERVICE_FORMATTED.PRINTABLE, null);

        // Print the document
        try {
            job.print(doc, null);
            System.out.println("Receipt printed successfully.");
        } catch (PrintException e) {
            e.printStackTrace();
        }
    }

    private PrintService findPrinter(String printerName) {
        PrintService[] printServices = PrintServiceLookup.lookupPrintServices(null, null);
        for (PrintService printer : printServices) {
            if (printer.getName().equalsIgnoreCase(printerName)) {
                return printer;
            }
        }
        return null;
    }
}
