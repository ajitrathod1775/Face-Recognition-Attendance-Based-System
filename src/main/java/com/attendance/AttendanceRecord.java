package com.attendance;

import java.time.LocalDate;

/**
 * AttendanceRecord - Professional Enterprise Version
 * Updated to support Daily Records and Monthly Summary Reports.
 */
public class AttendanceRecord {
    private String studentName;
    private String studentId;
    private String type; // Teacher or Student
    private String subject;
    private String status; // Present or Absent
    private LocalDate date;
    private String semester;

    // Advanced Fields for Dashboard Insights
    private String attendancePercentage; // e.g. "92.5%"
    private String lateEntry;            // e.g. "Yes" or "No"

    /**
     * Updated Constructor
     */
    public AttendanceRecord(String studentName, String studentId, String type, String subject,
                            String status, LocalDate date, String semester,
                            String attendancePercentage, String lateEntry) {
        this.studentName = studentName;
        this.studentId = studentId;
        this.type = type;
        this.subject = subject;
        this.status = status;
        this.date = date;
        this.semester = semester;
        this.attendancePercentage = (attendancePercentage != null) ? attendancePercentage : "0%";
        this.lateEntry = (lateEntry != null) ? lateEntry : "No";
    }

    // --- Getters (For JavaFX PropertyValueFactory) ---
    public String getStudentName() { return studentName; }
    public String getStudentId() { return studentId; }
    public String getType() { return type; }
    public String getSubject() { return subject; }
    public String getStatus() { return status; }
    public LocalDate getDate() { return date; }
    public String getSemester() { return semester; }
    public String getAttendancePercentage() { return attendancePercentage; }
    public String getLateEntry() { return lateEntry; }

    // --- Setters ---
    public void setStudentName(String studentName) { this.studentName = studentName; }
    public void setStudentId(String studentId) { this.studentId = studentId; }
    public void setStatus(String status) { this.status = status; }
    public void setAttendancePercentage(String attendancePercentage) { this.attendancePercentage = attendancePercentage; }
    public void setLateEntry(String lateEntry) { this.lateEntry = lateEntry; }
    public void setDate(LocalDate date) { this.date = date; }
}