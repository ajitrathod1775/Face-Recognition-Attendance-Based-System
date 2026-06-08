package com.attendance;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.stage.FileChooser;

import java.time.LocalDate;
import java.io.File;
import java.io.FileOutputStream;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

// Apache POI Libraries for Excel
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 * Teacher Dashboard - Master Version
 * ✅ Excel Export Fix (Ambiguous Cell)
 * ✅ Correctly Linked with DatabaseManager Models
 * ✅ Enhanced UI for Reports
 */
public class TeacherDashboard extends Application {
    private DatabaseManager dbManager;
    private TableView<DatabaseManager.AttendanceRecord> table;
    private TableView<DatabaseManager.HolidayRecord> holidayTable;
    private TableView<DatabaseManager.AttendanceRecord> monthlyReportTable;
    private ObservableList<DatabaseManager.AttendanceRecord> masterData = FXCollections.observableArrayList();
    private PieChart chart;
    private Label totalLbl, presentLbl, absentLbl;

    @Override
    public void start(Stage primaryStage) {
        dbManager = new DatabaseManager();

        TabPane tabPane = new TabPane();

        Tab analyticsTab = new Tab("📊 ANALYTICS", createAnalyticsView());
        Tab recordsTab = new Tab("📋 DAILY RECORDS", createTableView(primaryStage));
        Tab monthlyTab = new Tab("📅 ATTENDANCE REPORTS", createMonthlyReportView(primaryStage));
        Tab scheduleTab = new Tab("🛠️ SCHEDULE", createScheduleView());

        tabPane.getTabs().addAll(analyticsTab, recordsTab, monthlyTab, scheduleTab);
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        VBox layout = new VBox(tabPane);
        VBox.setVgrow(tabPane, Priority.ALWAYS);

        primaryStage.setTitle("Smart Attendance Pro - Teacher Panel");
        primaryStage.setScene(new Scene(layout, 1280, 800));

        refreshAllData();
        primaryStage.show();
    }

    private VBox createMonthlyReportView(Stage stage) {
        Label title = new Label("Student Attendance Summary (Periodic)");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        ComboBox<Integer> monthBox = new ComboBox<>();
        monthBox.getItems().addAll(IntStream.rangeClosed(1, 12).boxed().collect(Collectors.toList()));
        monthBox.setValue(LocalDate.now().getMonthValue());

        ComboBox<Integer> yearBox = new ComboBox<>();
        yearBox.getItems().addAll(2025, 2026, 2027);
        yearBox.setValue(LocalDate.now().getYear());

        Button generateBtn = new Button("Monthly Report");
        generateBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-weight: bold;");

        Button btn3Month = new Button("3-Month Report");
        btn3Month.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white; -fx-font-weight: bold;");

        Button btn6Month = new Button("6-Month Report");
        btn6Month.setStyle("-fx-background-color: #8e44ad; -fx-text-fill: white; -fx-font-weight: bold;");

        monthlyReportTable = new TableView<>();
        monthlyReportTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<DatabaseManager.AttendanceRecord, String> mNameCol = new TableColumn<>("Student Name");
        mNameCol.setCellValueFactory(new PropertyValueFactory<>("name"));

        TableColumn<DatabaseManager.AttendanceRecord, String> mIdCol = new TableColumn<>("Student ID");
        mIdCol.setCellValueFactory(new PropertyValueFactory<>("id"));

        TableColumn<DatabaseManager.AttendanceRecord, String> mPctCol = new TableColumn<>("Attendance %");
        mPctCol.setCellValueFactory(new PropertyValueFactory<>("status"));

        monthlyReportTable.getColumns().addAll(mNameCol, mIdCol, mPctCol);

        generateBtn.setOnAction(e -> loadReportData("MONTHLY", monthBox.getValue(), yearBox.getValue(), 0));
        btn3Month.setOnAction(e -> loadReportData("RANGE", 0, 0, 3));
        btn6Month.setOnAction(e -> loadReportData("RANGE", 0, 0, 6));

        HBox controls = new HBox(15, new Label("Month:"), monthBox, new Label("Year:"), yearBox, generateBtn, new Separator(), btn3Month, btn6Month);
        controls.setAlignment(Pos.CENTER_LEFT);
        controls.setPadding(new Insets(10));

        VBox layout = new VBox(20, title, controls, monthlyReportTable);
        layout.setPadding(new Insets(30));
        VBox.setVgrow(monthlyReportTable, Priority.ALWAYS);
        return layout;
    }

    private void loadReportData(String mode, int m, int y, int range) {
        ObservableList<DatabaseManager.AttendanceRecord> reportData = FXCollections.observableArrayList();
        List<DatabaseManager.StudentModel> students = dbManager.getAllStudents();

        for (DatabaseManager.StudentModel s : students) {
            String pct = mode.equals("MONTHLY") ? dbManager.getMonthlyPercentage(s.getId(), m, y) : dbManager.getCustomRangePercentage(s.getId(), range);
            reportData.add(new DatabaseManager.AttendanceRecord(s.getName(), s.getId(), "Report", pct, "All", LocalDate.now()));
        }
        monthlyReportTable.setItems(reportData);
    }

    private VBox createAnalyticsView() {
        totalLbl = new Label("0"); presentLbl = new Label("0"); absentLbl = new Label("0");
        HBox statsBox = new HBox(25,
                createStatCard("Total Students", totalLbl, "#3498db"),
                createStatCard("Present Today", presentLbl, "#2ecc71"),
                createStatCard("Absent Today", absentLbl, "#e74c3c")
        );
        statsBox.setAlignment(Pos.CENTER);
        chart = new PieChart();
        chart.setTitle("Attendance Overview (Today)");
        VBox layout = new VBox(40, statsBox, chart);
        layout.setPadding(new Insets(40)); layout.setAlignment(Pos.CENTER);
        return layout;
    }

    private VBox createScheduleView() {
        Label head = new Label("Manage Academic Calendar");
        head.setStyle("-fx-font-size: 22px; -fx-font-weight: bold;");
        DatePicker picker = new DatePicker(LocalDate.now());
        TextField reasonField = new TextField();
        reasonField.setPromptText("Enter Reason (e.g. Diwali)");
        Button addBtn = new Button("Mark as Holiday");
        addBtn.setStyle("-fx-background-color: #e67e22; -fx-text-fill: white; -fx-font-weight: bold;");

        holidayTable = new TableView<>();
        TableColumn<DatabaseManager.HolidayRecord, LocalDate> hDateCol = new TableColumn<>("Date");
        hDateCol.setCellValueFactory(new PropertyValueFactory<>("date"));
        TableColumn<DatabaseManager.HolidayRecord, String> hReasonCol = new TableColumn<>("Reason");
        hReasonCol.setCellValueFactory(new PropertyValueFactory<>("reason"));
        holidayTable.getColumns().addAll(hDateCol, hReasonCol);
        holidayTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        addBtn.setOnAction(e -> {
            if(dbManager.addHoliday(picker.getValue(), reasonField.getText())) {
                holidayTable.setItems(FXCollections.observableArrayList(dbManager.getAllHolidays()));
                refreshAllData();
            }
        });

        holidayTable.setItems(FXCollections.observableArrayList(dbManager.getAllHolidays()));
        VBox layout = new VBox(25, head, new HBox(15, picker, reasonField, addBtn), holidayTable);
        layout.setPadding(new Insets(30));
        return layout;
    }

    private void refreshAllData() {
        new Thread(() -> {
            int total = dbManager.getTotalStudentCount();
            int present = dbManager.getPresentCount();
            List<DatabaseManager.AttendanceRecord> allRecords = dbManager.getAllRecords();
            Platform.runLater(() -> {
                totalLbl.setText(String.valueOf(total));
                presentLbl.setText(String.valueOf(present));
                absentLbl.setText(String.valueOf(Math.max(0, total - present)));
                chart.getData().setAll(
                        new PieChart.Data("Present", present),
                        new PieChart.Data("Absent", Math.max(0, total - present))
                );
                masterData.setAll(allRecords);
            });
        }).start();
    }

    private VBox createTableView(Stage stage) {
        table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        TableColumn<DatabaseManager.AttendanceRecord, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        TableColumn<DatabaseManager.AttendanceRecord, String> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        TableColumn<DatabaseManager.AttendanceRecord, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        TableColumn<DatabaseManager.AttendanceRecord, LocalDate> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("date"));

        table.getColumns().addAll(nameCol, idCol, statusCol, dateCol);
        table.setItems(masterData);

        Button exportBtn = new Button("📥 Download Excel Report");
        exportBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-weight: bold;");
        exportBtn.setOnAction(e -> handleExcelExport(stage));

        VBox layout = new VBox(20, table, exportBtn);
        layout.setPadding(new Insets(30));
        VBox.setVgrow(table, Priority.ALWAYS);
        return layout;
    }

    private void handleExcelExport(Stage stage) {
        if (masterData.isEmpty()) return;
        FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialFileName("Attendance_Report_" + LocalDate.now() + ".xlsx");
        File selectedFile = fileChooser.showSaveDialog(stage);
        if (selectedFile == null) return;

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Attendance");
            Row headerRow = sheet.createRow(0);
            String[] columns = {"Name", "ID", "Subject", "Date", "Status"};

            for (int i = 0; i < columns.length; i++) {
                // ✅ FIXED: Explicitly calling org.apache.poi Cell to avoid JavaFX Cell conflict
                org.apache.poi.ss.usermodel.Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
            }

            int rowIdx = 1;
            for (DatabaseManager.AttendanceRecord record : masterData) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(record.getName());
                row.createCell(1).setCellValue(record.getId());
                row.createCell(2).setCellValue(record.getSubject());
                row.createCell(3).setCellValue(record.getDate().toString());
                row.createCell(4).setCellValue(record.getStatus());
            }

            try (FileOutputStream fileOut = new FileOutputStream(selectedFile)) {
                workbook.write(fileOut);
                showSimpleAlert(Alert.AlertType.INFORMATION, "Success", "Excel Report Saved Successfully!");
            }
        } catch (Exception e) {
            e.printStackTrace();
            showSimpleAlert(Alert.AlertType.ERROR, "Error", "Failed to save Excel file.");
        }
    }

    private VBox createStatCard(String title, Label val, String color) {
        VBox card = new VBox(10, new Label(title), val);
        card.setStyle("-fx-background-color: " + color + "; -fx-padding: 20; -fx-background-radius: 15; -fx-text-fill: white;");
        val.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: white;");
        card.setMinWidth(220); card.setAlignment(Pos.CENTER);
        return card;
    }

    private void showSimpleAlert(Alert.AlertType type, String title, String msg) {
        Alert alert = new Alert(type); alert.setTitle(title); alert.setContentText(msg); alert.show();
    }

    public static void main(String[] args) { launch(args); }
}