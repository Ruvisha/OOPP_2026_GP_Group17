package com.fot.ams.dao;

import com.fot.ams.database.DatabaseConnection;

import java.sql.*;
import java.util.*;

/**
 * Data Access Object providing real database queries for
 * Technical Officer, Lecturer, and Undergraduate Student dashboard analytics.
 */
public class RoleAnalyticsDAO {

    public static class TechnicalSummary {
        public final int totalSessions;
        public final int medicalLogged;
        public final int medicalPending;
        public final int eligibleCount;
        public final int totalStudentCourses;

        public TechnicalSummary(int totalSessions, int medicalLogged, int medicalPending, int eligibleCount, int totalStudentCourses) {
            this.totalSessions = totalSessions;
            this.medicalLogged = medicalLogged;
            this.medicalPending = medicalPending;
            this.eligibleCount = eligibleCount;
            this.totalStudentCourses = totalStudentCourses;
        }
    }

    public static class LecturerSummary {
        public final int assignedCourses;
        public final int enrolledStudents;
        public final int evaluationsCount;
        public final int pendingAssessments;

        public LecturerSummary(int assignedCourses, int enrolledStudents, int evaluationsCount, int pendingAssessments) {
            this.assignedCourses = assignedCourses;
            this.enrolledStudents = enrolledStudents;
            this.evaluationsCount = evaluationsCount;
            this.pendingAssessments = pendingAssessments;
        }
    }

    public static class StudentSummary {
        public final int enrolledCredits;
        public final double overallAttendancePct;
        public final int completedCourses;
        public final boolean isBarredFromExams;

        public StudentSummary(int enrolledCredits, double overallAttendancePct, int completedCourses, boolean isBarredFromExams) {
            this.enrolledCredits = enrolledCredits;
            this.overallAttendancePct = overallAttendancePct;
            this.completedCourses = completedCourses;
            this.isBarredFromExams = isBarredFromExams;
        }
    }

    // =========================================================================
    // 1. TECHNICAL OFFICER ANALYTICS
    // =========================================================================

    public TechnicalSummary getTechnicalSummary() {
        int totalSessions = 0;
        int medicalLogged = 0;
        int medicalPending = 0;
        int eligibleCount = 0;
        int totalPairs = 0;

        try (Connection conn = DatabaseConnection.getConnection()) {
            // Sessions count
            try (Statement s = conn.createStatement();
                 ResultSet rs = s.executeQuery("SELECT COUNT(*) FROM attendance")) {
                if (rs.next()) totalSessions = rs.getInt(1);
            }

            // Medical logged and pending
            try (Statement s = conn.createStatement();
                 ResultSet rs = s.executeQuery("SELECT COUNT(*), SUM(CASE WHEN status = 'PENDING' THEN 1 ELSE 0 END) FROM medical")) {
                if (rs.next()) {
                    medicalLogged = rs.getInt(1);
                    medicalPending = rs.getInt(2);
                }
            }

            // Attendance eligibility: calculate per student-course
            String eligSql = "SELECT student_id, course_id, " +
                             "SUM(CASE WHEN status = 'PRESENT' THEN 1 ELSE 0 END) * 100.0 / COUNT(*) AS pct " +
                             "FROM attendance GROUP BY student_id, course_id";
            try (Statement s = conn.createStatement();
                 ResultSet rs = s.executeQuery(eligSql)) {
                while (rs.next()) {
                    totalPairs++;
                    if (rs.getDouble("pct") >= 80.0) {
                        eligibleCount++;
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("[RoleAnalyticsDAO] Tech summary error: " + e.getMessage());
        }

        return new TechnicalSummary(totalSessions, medicalLogged, medicalPending, eligibleCount, totalPairs);
    }

    public Map<String, Integer> getAttendanceTheoryVsPractical() {
        Map<String, Integer> map = new LinkedHashMap<>();
        String sql = "SELECT session_type, COUNT(*) AS cnt FROM attendance GROUP BY session_type";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                String type = rs.getString("session_type");
                map.put(type != null ? type : "THEORY", rs.getInt("cnt"));
            }
        } catch (Exception e) {
            System.err.println("[RoleAnalyticsDAO] Theory vs practical error: " + e.getMessage());
        }
        return map;
    }

    public Map<String, Integer> getAttendanceEligibilityDistribution() {
        Map<String, Integer> map = new LinkedHashMap<>();
        int above80 = 0;
        int exact80 = 0;
        int below80 = 0;

        String sql = "SELECT student_id, course_id, " +
                     "ROUND(SUM(CASE WHEN status = 'PRESENT' THEN 1 ELSE 0 END) * 100.0 / COUNT(*), 1) AS pct " +
                     "FROM attendance GROUP BY student_id, course_id";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                double pct = rs.getDouble("pct");
                if (pct > 80.0) above80++;
                else if (Math.abs(pct - 80.0) < 0.1) exact80++;
                else below80++;
            }
        } catch (Exception e) {
            System.err.println("[RoleAnalyticsDAO] Eligibility dist error: " + e.getMessage());
        }

        map.put("Above 80%", above80);
        map.put("Exactly 80%", exact80);
        map.put("Below 80%", below80);
        return map;
    }

    public Map<String, Integer> getMedicalStatusDistribution() {
        Map<String, Integer> map = new LinkedHashMap<>();
        String sql = "SELECT status, COUNT(*) AS cnt FROM medical GROUP BY status";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                String st = rs.getString("status");
                map.put(st != null ? st : "PENDING", rs.getInt("cnt"));
            }
        } catch (Exception e) {
            System.err.println("[RoleAnalyticsDAO] Medical status error: " + e.getMessage());
        }
        return map;
    }

    public Map<String, Integer> getMonthlyAttendanceTrend() {
        Map<String, Integer> map = new LinkedHashMap<>();
        String sql = "SELECT DATE_FORMAT(session_date, '%b %Y') AS month_label, " +
                     "DATE_FORMAT(session_date, '%Y-%m') AS ym, COUNT(*) AS cnt " +
                     "FROM attendance GROUP BY ym, month_label ORDER BY ym ASC LIMIT 6";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                map.put(rs.getString("month_label"), rs.getInt("cnt"));
            }
        } catch (Exception e) {
            System.err.println("[RoleAnalyticsDAO] Monthly attendance error: " + e.getMessage());
        }
        return map;
    }

    // =========================================================================
    // 2. LECTURER ANALYTICS
    // =========================================================================

    public LecturerSummary getLecturerSummary(int lecturerId) {
        int assigned = 0;
        int enrolled = 0;
        int evaluations = 0;
        int pending = 0;

        try (Connection conn = DatabaseConnection.getConnection()) {
            // Assigned courses
            String assignSql = "SELECT COUNT(DISTINCT course_id) FROM course_lecturers WHERE lecturer_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(assignSql)) {
                stmt.setInt(1, lecturerId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) assigned = rs.getInt(1);
                }
            }

            // Total enrolled students across assigned courses
            String enrollSql = "SELECT COUNT(DISTINCT ce.student_id) FROM course_enrollment ce " +
                              "JOIN course_lecturers cl ON ce.course_id = cl.course_id " +
                              "WHERE cl.lecturer_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(enrollSql)) {
                stmt.setInt(1, lecturerId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) enrolled = rs.getInt(1);
                }
            }

            // Total evaluations for assigned courses
            String evalSql = "SELECT COUNT(*) FROM evaluations e " +
                             "JOIN course_lecturers cl ON e.course_id = cl.course_id " +
                             "WHERE cl.lecturer_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(evalSql)) {
                stmt.setInt(1, lecturerId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) evaluations = rs.getInt(1);
                }
            }

            // Pending assignments to grade
            String pendingSql = "SELECT COUNT(*) FROM assignment_submissions s " +
                                "JOIN assignments a ON s.assignment_id = a.id " +
                                "WHERE a.lecturer_id = ? AND s.status = 'SUBMITTED'";
            try (PreparedStatement stmt = conn.prepareStatement(pendingSql)) {
                stmt.setInt(1, lecturerId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) pending = rs.getInt(1);
                }
            }
        } catch (Exception e) {
            System.err.println("[RoleAnalyticsDAO] Lecturer summary error: " + e.getMessage());
        }

        return new LecturerSummary(assigned, enrolled, evaluations, pending);
    }

    public Map<String, Integer> getLecturerStudentPerformanceByCourse(int lecturerId) {
        Map<String, Integer> map = new LinkedHashMap<>();
        String sql = "SELECT c.course_code, ROUND(AVG(g.marks)) AS avg_marks " +
                     "FROM grades g " +
                     "JOIN courses c ON g.course_id = c.course_id " +
                     "JOIN course_lecturers cl ON c.course_id = cl.course_id " +
                     "WHERE cl.lecturer_id = ? " +
                     "GROUP BY c.course_id, c.course_code";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, lecturerId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    map.put(rs.getString("course_code"), rs.getInt("avg_marks"));
                }
            }
        } catch (Exception e) {
            System.err.println("[RoleAnalyticsDAO] Lecturer performance error: " + e.getMessage());
        }
        return map;
    }

    public Map<String, Integer> getLecturerAttendanceByCourse(int lecturerId) {
        Map<String, Integer> map = new LinkedHashMap<>();
        String sql = "SELECT c.course_code, " +
                     "ROUND(SUM(CASE WHEN a.status = 'PRESENT' THEN 1 ELSE 0 END) * 100.0 / COUNT(*)) AS att_pct " +
                     "FROM attendance a " +
                     "JOIN courses c ON a.course_id = c.course_id " +
                     "JOIN course_lecturers cl ON c.course_id = cl.course_id " +
                     "WHERE cl.lecturer_id = ? " +
                     "GROUP BY c.course_id, c.course_code";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, lecturerId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    map.put(rs.getString("course_code"), rs.getInt("att_pct"));
                }
            }
        } catch (Exception e) {
            System.err.println("[RoleAnalyticsDAO] Lecturer attendance error: " + e.getMessage());
        }
        return map;
    }

    public Map<String, Integer> getLecturerGradeDistribution(int lecturerId) {
        Map<String, Integer> map = new LinkedHashMap<>();
        map.put("A", 0);
        map.put("B", 0);
        map.put("C", 0);
        map.put("D", 0);
        map.put("F", 0);

        String sql = "SELECT " +
                     "CASE " +
                     "  WHEN g.grade LIKE 'A%' THEN 'A' " +
                     "  WHEN g.grade LIKE 'B%' THEN 'B' " +
                     "  WHEN g.grade LIKE 'C%' THEN 'C' " +
                     "  WHEN g.grade LIKE 'D%' THEN 'D' " +
                     "  ELSE 'F' " +
                     "END AS grade_cat, COUNT(*) AS cnt " +
                     "FROM grades g " +
                     "JOIN course_lecturers cl ON g.course_id = cl.course_id " +
                     "WHERE cl.lecturer_id = ? " +
                     "GROUP BY grade_cat";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, lecturerId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    map.put(rs.getString("grade_cat"), rs.getInt("cnt"));
                }
            }
        } catch (Exception e) {
            System.err.println("[RoleAnalyticsDAO] Lecturer grades error: " + e.getMessage());
        }
        return map;
    }

    public Map<String, Integer> getLecturerPerformanceTrend(int lecturerId) {
        Map<String, Integer> map = new LinkedHashMap<>();
        String sql = "SELECT e.evaluation_name, ROUND(AVG(m.marks_obtained)) AS avg_marks " +
                     "FROM marks m " +
                     "JOIN evaluations e ON m.evaluation_id = e.evaluation_id " +
                     "JOIN course_lecturers cl ON e.course_id = cl.course_id " +
                     "WHERE cl.lecturer_id = ? " +
                     "GROUP BY e.evaluation_id, e.evaluation_name " +
                     "ORDER BY e.evaluation_id ASC LIMIT 6";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, lecturerId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String name = rs.getString("evaluation_name");
                    if (name.length() > 10) name = name.substring(0, 10);
                    map.put(name, rs.getInt("avg_marks"));
                }
            }
        } catch (Exception e) {
            System.err.println("[RoleAnalyticsDAO] Lecturer eval trend error: " + e.getMessage());
        }
        return map;
    }

    // =========================================================================
    // 3. UNDERGRADUATE STUDENT ANALYTICS
    // =========================================================================

    public StudentSummary getStudentSummary(int studentId) {
        int credits = 0;
        double overallAtt = 0.0;
        int completed = 0;

        try (Connection conn = DatabaseConnection.getConnection()) {
            // Total credits enrolled
            String crSql = "SELECT COALESCE(SUM(c.credits), 0) FROM course_enrollment ce " +
                           "JOIN courses c ON ce.course_id = c.course_id WHERE ce.student_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(crSql)) {
                stmt.setInt(1, studentId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) credits = rs.getInt(1);
                }
            }

            // Completed courses
            String compSql = "SELECT COUNT(*) FROM grades WHERE student_id = ? AND grade NOT IN ('F', 'E')";
            try (PreparedStatement stmt = conn.prepareStatement(compSql)) {
                stmt.setInt(1, studentId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) completed = rs.getInt(1);
                }
            }

            // Overall attendance %
            String attSql = "SELECT SUM(CASE WHEN status = 'PRESENT' THEN 1 ELSE 0 END) * 100.0 / COUNT(*) " +
                            "FROM attendance WHERE student_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(attSql)) {
                stmt.setInt(1, studentId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) overallAtt = rs.getDouble(1);
                }
            }
        } catch (Exception e) {
            System.err.println("[RoleAnalyticsDAO] Student summary error: " + e.getMessage());
        }

        return new StudentSummary(credits, overallAtt, completed, overallAtt > 0 && overallAtt < 80.0);
    }

    public Map<String, Integer> getStudentMarksByCourse(int studentId) {
        Map<String, Integer> map = new LinkedHashMap<>();
        String sql = "SELECT c.course_code, ROUND(g.marks) AS marks " +
                     "FROM grades g " +
                     "JOIN courses c ON g.course_id = c.course_id " +
                     "WHERE g.student_id = ? " +
                     "ORDER BY c.course_code ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, studentId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    map.put(rs.getString("course_code"), rs.getInt("marks"));
                }
            }
        } catch (Exception e) {
            System.err.println("[RoleAnalyticsDAO] Student marks error: " + e.getMessage());
        }
        return map;
    }

    public Map<String, Integer> getStudentAttendanceByCourse(int studentId) {
        Map<String, Integer> map = new LinkedHashMap<>();
        String sql = "SELECT c.course_code, " +
                     "ROUND(SUM(CASE WHEN a.status = 'PRESENT' THEN 1 ELSE 0 END) * 100.0 / COUNT(*)) AS pct " +
                     "FROM attendance a " +
                     "JOIN courses c ON a.course_id = c.course_id " +
                     "WHERE a.student_id = ? " +
                     "GROUP BY c.course_id, c.course_code";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, studentId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    map.put(rs.getString("course_code"), rs.getInt("pct"));
                }
            }
        } catch (Exception e) {
            System.err.println("[RoleAnalyticsDAO] Student attendance error: " + e.getMessage());
        }
        return map;
    }

    public Map<String, Integer> getStudentGradeDistribution(int studentId) {
        Map<String, Integer> map = new LinkedHashMap<>();
        map.put("A", 0);
        map.put("B", 0);
        map.put("C", 0);
        map.put("D", 0);
        map.put("F", 0);

        String sql = "SELECT " +
                     "CASE " +
                     "  WHEN grade LIKE 'A%' THEN 'A' " +
                     "  WHEN grade LIKE 'B%' THEN 'B' " +
                     "  WHEN grade LIKE 'C%' THEN 'C' " +
                     "  WHEN grade LIKE 'D%' THEN 'D' " +
                     "  ELSE 'F' " +
                     "END AS grade_cat, COUNT(*) AS cnt " +
                     "FROM grades WHERE student_id = ? " +
                     "GROUP BY grade_cat";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, studentId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    map.put(rs.getString("grade_cat"), rs.getInt("cnt"));
                }
            }
        } catch (Exception e) {
            System.err.println("[RoleAnalyticsDAO] Student grades error: " + e.getMessage());
        }
        return map;
    }
}
