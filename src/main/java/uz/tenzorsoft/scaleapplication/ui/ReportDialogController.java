package uz.tenzorsoft.scaleapplication.ui;

import javafx.fxml.FXML;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uz.tenzorsoft.scaleapplication.service.ReportService;

import java.time.LocalDate;

@Component
public class ReportDialogController {

    @FXML
    private DatePicker fromDatePicker;

    @FXML
    private DatePicker toDatePicker;

    @FXML
    private TextArea reportTextArea;

    @Autowired
    private ReportService reportService;

    @FXML
    private void generateReport() {
        LocalDate fromDate = fromDatePicker.getValue();
        LocalDate toDate = toDatePicker.getValue();

        try {
            String report = reportService.generateReport(fromDate, toDate);
            reportTextArea.setText(report);
        } catch (IllegalArgumentException e) {
            reportTextArea.setText("Xatolik: " + e.getMessage());
        }
    }

    @FXML
    private void clearStartDate() {
        fromDatePicker.setValue(null);
        toDatePicker.setValue(null);
        reportTextArea.setText("Hisobotni ko'rish uchun kunni tanlang.");
    }

    @FXML
    private void clearEndDate() {
        toDatePicker.setValue(null);
        LocalDate fromDate = fromDatePicker.getValue();

        if (fromDate != null) {
            try {
                String report = reportService.generateReport(fromDate, null);
                reportTextArea.setText(report);
            } catch (IllegalArgumentException e) {
                reportTextArea.setText("Xatolik: " + e.getMessage());
            }
        } else {
            reportTextArea.setText("Hisobotni ko'rish uchun kunni tanlang.");
        }
    }

    public void closePopup() {
        Stage stage = (Stage) reportTextArea.getScene().getWindow();
        stage.close();
    }
}
