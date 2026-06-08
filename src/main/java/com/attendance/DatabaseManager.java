package com.attendance;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalDate;

/**
 * DatabaseManager - FINAL MASTER VERSION 2026
 * ✅ Fixed: markSmartAttendance now accepts 5 parameters to match UI
 * ✅ Real Logic for Reports (Percentage)
 * ✅ Parent Email Support
 * ✅ All Models Included
 */
public class DatabaseManager {

    private static Connection connection;
    private static final String URL = "jdbc:postgresql://localhost:5432/smart_attendance";
    private static final String USER = "postgres";
    private static final String PASS = "admin";

    public DatabaseManager() {
        initializeConnection();
        ensureTablesExist();
    }

    private synchronized void initializeConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                Class.forName("org.postgresql.Driver");
                connection = DriverManager.getConnection(URL, USER, PASS);
                System.out.println("✅ Database Connected Successfully!");
            }
        } catch (Exception e) {
            System.err.println("❌ Connection Failed: " + e.getMessage());
        }
    }

    private void ensureTablesExist() {
        try (Statement s = getValidConnection().createStatement()) {
            s.execute("CREATE TABLE IF NOT EXISTS students (student_id VARCHAR(50) PRIMARY KEY, student_name VARCHAR(100), parent_email VARCHAR(255))");
            s.execute("CREATE TABLE IF NOT EXISTS users (username VARCHAR(50) PRIMARY KEY, password VARCHAR(50), role VARCHAR(20))");
            s.execute("CREATE TABLE IF NOT EXISTS teachers (teacher_id VARCHAR(50) PRIMARY KEY, teacher_name VARCHAR(100))");
            s.execute("CREATE TABLE IF NOT EXISTS attendance (id SERIAL PRIMARY KEY, student_name VARCHAR(100), student_id VARCHAR(50), subject VARCHAR(100), semester VARCHAR(20), status VARCHAR(20), date DATE, time TIME)");
            s.execute("CREATE TABLE IF NOT EXISTS holidays (holiday_date DATE PRIMARY KEY, reason VARCHAR(255))");
            s.execute("INSERT INTO users (username, password, role) VALUES ('admin', 'admin123', 'Teacher') ON CONFLICT DO NOTHING");
        } catch (SQLException e) { }
    }

    public Connection getValidConnection() {
        try {
            if (connection == null || connection.isClosed()) initializeConnection();
        } catch (SQLException e) { initializeConnection(); }
        return connection;
    }

    // --- १. REGISTRATION & LOGIN ---
    public boolean registerStudent(String id, String name, String email) {
        String sql = "INSERT INTO students (student_id, student_name, parent_email) VALUES (?, ?, ?) ON CONFLICT (student_id) DO UPDATE SET student_name = EXCLUDED.student_name, parent_email = EXCLUDED.parent_email";
        try (PreparedStatement ps = getValidConnection().prepareStatement(sql)) {
            ps.setString(1, id); ps.setString(2, name); ps.setString(3, email);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { return false; }
    }

    public boolean registerTeacher(String id, String name, String password) {
        Connection conn = getValidConnection();
        try {
            conn.setAutoCommit(false);
            String tSql = "INSERT INTO teachers (teacher_id, teacher_name) VALUES (?, ?) ON CONFLICT (teacher_id) DO UPDATE SET teacher_name = EXCLUDED.teacher_name";
            try (PreparedStatement psT = conn.prepareStatement(tSql)) {
                psT.setString(1, id); psT.setString(2, name); psT.executeUpdate();
            }
            String uSql = "INSERT INTO users (username, password, role) VALUES (?, ?, 'Teacher') ON CONFLICT (username) DO UPDATE SET password = EXCLUDED.password";
            try (PreparedStatement psU = conn.prepareStatement(uSql)) {
                psU.setString(1, id); psU.setString(2, password); psU.executeUpdate();
            }
            conn.commit();
            return true;
        } catch (SQLException e) {
            try { if(conn != null) conn.rollback(); } catch (SQLException ex) {}
            return false;
        }
    }

    public boolean validateLogin(String user, String pass) {
        String sql = "SELECT * FROM users WHERE username = ? AND password = ?";
        try (PreparedStatement ps = getValidConnection().prepareStatement(sql)) {
            ps.setString(1, user); ps.setString(2, pass);
            try (ResultSet rs = ps.executeQuery()) { return rs.next(); }
        } catch (SQLException e) { return false; }
    }

    // --- २. ATTENDANCE & EMAIL SUPPORT ---

    // ✅ FIXED: UI मधून ५ पॅरामीटर्स येत असल्याने ही मेथड आता ५ पॅरामीटर्स घेईल
    public String markSmartAttendance(String id, String name, String type, String subject, String semester) {
        String insertSql = "INSERT INTO attendance (student_name, student_id, subject, semester, status, date, time) VALUES (?, ?, ?, ?, 'Present', CURRENT_DATE, CURRENT_TIME)";
        try (PreparedStatement ps = getValidConnection().prepareStatement(insertSql)) {
            ps.setString(1, name); ps.setString(2, id); ps.setString(3, subject); ps.setString(4, semester);
            return (ps.executeUpdate() > 0) ? "SUCCESS" : "ERROR";
        } catch (SQLException e) { return "ERROR"; }
    }

    public String getParentEmail(String studentId) {
        String sql = "SELECT parent_email FROM students WHERE student_id = ?";
        try (PreparedStatement ps = getValidConnection().prepareStatement(sql)) {
            ps.setString(1, studentId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getString("parent_email");
        } catch (SQLException e) { }
        return null;
    }

    // --- ३. REAL REPORTS LOGIC ---
    public String getMonthlyPercentage(String studentId, int month, int year) {
        String totalDaysSql = "SELECT COUNT(DISTINCT date) FROM attendance WHERE EXTRACT(MONTH FROM date) = ? AND EXTRACT(YEAR FROM date) = ?";
        String presentDaysSql = "SELECT COUNT(DISTINCT date) FROM attendance WHERE student_id = ? AND EXTRACT(MONTH FROM date) = ? AND EXTRACT(YEAR FROM date) = ?";
        try {
            int total = 0, present = 0;
            try (PreparedStatement ps = getValidConnection().prepareStatement(totalDaysSql)) {
                ps.setInt(1, month); ps.setInt(2, year);
                ResultSet rs = ps.executeQuery(); if (rs.next()) total = rs.getInt(1);
            }
            try (PreparedStatement ps = getValidConnection().prepareStatement(presentDaysSql)) {
                ps.setString(1, studentId); ps.setInt(2, month); ps.setInt(3, year);
                ResultSet rs = ps.executeQuery(); if (rs.next()) present = rs.getInt(1);
            }
            return (total == 0) ? "0%" : String.format("%.1f%%", ((double) present / total) * 100);
        } catch (SQLException e) { return "0%"; }
    }

    public String getCustomRangePercentage(String studentId, int months) {
        String totalDaysSql = "SELECT COUNT(DISTINCT date) FROM attendance WHERE date > CURRENT_DATE - (INTERVAL '1 month' * ?)";
        String presentDaysSql = "SELECT COUNT(DISTINCT date) FROM attendance WHERE student_id = ? AND date > CURRENT_DATE - (INTERVAL '1 month' * ?)";
        try {
            int total = 0, present = 0;
            try (PreparedStatement ps = getValidConnection().prepareStatement(totalDaysSql)) {
                ps.setInt(1, months);
                ResultSet rs = ps.executeQuery(); if (rs.next()) total = rs.getInt(1);
            }
            try (PreparedStatement ps = getValidConnection().prepareStatement(presentDaysSql)) {
                ps.setString(1, studentId); ps.setInt(2, months);
                ResultSet rs = ps.executeQuery(); if (rs.next()) present = rs.getInt(1);
            }
            return (total == 0) ? "0%" : String.format("%.1f%%", ((double) present / total) * 100);
        } catch (SQLException e) { return "0%"; }
    }

    // --- ४. DATA FETCHING ---
    public List<AttendanceRecord> getAllRecords() {
        List<AttendanceRecord> list = new ArrayList<>();
        try (Statement s = getValidConnection().createStatement(); ResultSet rs = s.executeQuery("SELECT * FROM attendance ORDER BY date DESC")) {
            while (rs.next()) list.add(new AttendanceRecord(rs.getString("student_name"), rs.getString("student_id"), rs.getString("subject"), rs.getString("status"), rs.getString("semester"), rs.getDate("date").toLocalDate()));
        } catch (Exception e) { }
        return list;
    }

    public List<StudentModel> getAllStudents() {
        List<StudentModel> list = new ArrayList<>();
        try (Statement s = getValidConnection().createStatement(); ResultSet rs = s.executeQuery("SELECT student_id, student_name FROM students")) {
            while (rs.next()) list.add(new StudentModel(rs.getString("student_id"), rs.getString("student_name")));
        } catch (Exception e) { }
        return list;
    }

    public boolean addHoliday(LocalDate d, String r) {
        String sql = "INSERT INTO holidays (holiday_date, reason) VALUES (?, ?) ON CONFLICT (holiday_date) DO UPDATE SET reason = EXCLUDED.reason";
        try (PreparedStatement ps = getValidConnection().prepareStatement(sql)) {
            ps.setDate(1, java.sql.Date.valueOf(d)); ps.setString(2, r);
            return ps.executeUpdate() > 0;
        } catch (Exception e) { return false; }
    }

    public List<HolidayRecord> getAllHolidays() {
        List<HolidayRecord> list = new ArrayList<>();
        try (Statement s = getValidConnection().createStatement(); ResultSet rs = s.executeQuery("SELECT * FROM holidays ORDER BY holiday_date DESC")) {
            while (rs.next()) list.add(new HolidayRecord(rs.getDate("holiday_date").toLocalDate(), rs.getString("reason")));
        } catch (Exception e) { }
        return list;
    }

    public int getTotalStudentCount() { try { ResultSet rs = getValidConnection().createStatement().executeQuery("SELECT COUNT(*) FROM students"); return rs.next() ? rs.getInt(1) : 0; } catch (Exception e) { return 0; } }
    public int getPresentCount() { try { ResultSet rs = getValidConnection().createStatement().executeQuery("SELECT COUNT(*) FROM attendance WHERE date = CURRENT_DATE"); return rs.next() ? rs.getInt(1) : 0; } catch (Exception e) { return 0; } }

    // --- ५. MODELS ---
    public static class StudentModel {
        private String id, name;
        public StudentModel(String id, String name) { this.id = id; this.name = name; }
        public String getId() { return id; }
        public String getName() { return name; }
    }

    public static class HolidayRecord {
        private LocalDate date; private String reason;
        public HolidayRecord(LocalDate d, String r) { this.date = d; this.reason = r; }
        public LocalDate getDate() { return date; }
        public String getReason() { return reason; }
    }

    public static class AttendanceRecord {
        private String name, id, subject, status, semester; private LocalDate date;
        public AttendanceRecord(String n, String i, String s, String st, String sem, LocalDate d) {
            this.name = n; this.id = i; this.subject = s; this.status = st; this.semester = sem; this.date = d;
        }
        public String getName() { return name; }
        public String getId() { return id; }
        public String getSubject() { return subject; }
        public String getStatus() { return status; }
        public String getSemester() { return semester; }
        public LocalDate getDate() { return date; }
    }
}