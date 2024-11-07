package uz.tenzorsoft.scaleapplication.service;

import org.springframework.stereotype.Service;
import uz.tenzorsoft.scaleapplication.domain.response.TruckResponse;

import javax.print.*;
import javax.print.attribute.*;
import javax.print.attribute.standard.MediaSizeName;
import javax.print.attribute.standard.OrientationRequested;
import java.awt.*;
import java.awt.print.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class PrintCheck {

    public void printReceipt(TruckResponse response) {
        PrinterJob printerJob = PrinterJob.getPrinterJob();
        printerJob.setPrintable(createPrintable(response));

        PrintService selectedPrinter = findPrintService("Xprinter XP-T80Q");
        if (selectedPrinter == null) {
            System.out.println("Printer not found.");
            return;
        }

        DocPrintJob printJob = selectedPrinter.createPrintJob();
        DocAttributeSet docAttributes = new HashDocAttributeSet();
        docAttributes.add(OrientationRequested.PORTRAIT);
        docAttributes.add(MediaSizeName.ISO_A8);

        try {
            Doc doc = new SimpleDoc(printerJob, DocFlavor.SERVICE_FORMATTED.PRINTABLE, null);
            printJob.print(doc, null);
            System.out.println("Receipt printed successfully.");
        } catch (PrintException e) {
            e.printStackTrace();
        }
    }

    private Printable createPrintable(TruckResponse response) {
        return (graphics, pageFormat, pageIndex) -> {
            if (pageIndex > 0) {
                return Printable.NO_SUCH_PAGE;
            }

            Graphics2D g2d = (Graphics2D) graphics;
            g2d.setFont(new Font("Monospaced", Font.PLAIN, 10));
            g2d.translate(pageFormat.getImageableX(), pageFormat.getImageableY());

            // Example content
            g2d.drawString("       Sirdayo       ", 10, 10);
            g2d.drawString("Truck Number: " + response.getTruckNumber(), 10, 30);
            g2d.drawString("Arrival: " + formatDateTime(response.getEnteredAt()), 10, 50);
            g2d.drawString("Departure: " + formatDateTime(response.getExitedAt()), 10, 70);
            g2d.drawString("Gross Weight: " + response.getEnteredWeight(), 10, 90);
            g2d.drawString("Tare Weight: " + response.getExitedWeight(), 10, 110);

            double netto = Math.abs(response.getEnteredWeight() - response.getExitedWeight());
            g2d.drawString("Net Weight: " + netto, 10, 130);
            g2d.drawString("Operator: " + response.getExitConfirmedBy(), 10, 150);

            return Printable.PAGE_EXISTS;
        };
    }

    private String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "N/A";
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
        return dateTime.format(formatter);
    }

    private PrintService findPrintService(String printerName) {
        PrintService[] printServices = PrintServiceLookup.lookupPrintServices(null, null);
        for (PrintService printService : printServices) {
            if (printService.getName().equalsIgnoreCase(printerName)) {
                return printService;
            }
        }
        return null;
    }
}
