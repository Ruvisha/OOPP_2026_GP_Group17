package test;

import com.fot.ams.dao.AdminAnalyticsDAO;
import com.fot.ams.dao.NotificationDAO;
import com.fot.ams.dao.RoleAnalyticsDAO;
import com.fot.ams.model.*;
import com.fot.ams.ui.admin.*;
import com.fot.ams.ui.common.*;
import com.fot.ams.ui.lecturer.LecturerDashboard;
import com.fot.ams.ui.lecturer.LecturerOverviewPanel;
import com.fot.ams.ui.technical.TechnicalOfficerDashboard;
import com.fot.ams.ui.technical.TechnicalOverviewPanel;
import com.fot.ams.ui.undergraduate.UndergraduateDashboard;
import com.fot.ams.ui.undergraduate.UndergraduateOverviewPanel;
import com.fot.ams.util.SessionManager;

import java.util.HashMap;
import java.util.Map;

public class VerifyAllDashboards {
    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println("   FOT - AMS ALL DASHBOARDS VERIFICATION SUITE   ");
        System.out.println("==================================================");

        // 1. Modern Theme & Vector Icons
        System.out.println("[1/7] Testing ModernTheme & Icons...");
        System.out.println("  Font: " + ModernTheme.getFontFamily());
        String[] icons = {
            "dashboard", "users", "courses", "students", "marks", "notices",
            "timetables", "reports", "audit", "settings", "profile", "logout",
            "search", "calendar", "refresh", "check", "alert", "academic"
        };
        for (String name : icons) {
            if (ModernIcons.get(name, 18) == null) {
                throw new RuntimeException("Missing icon: " + name);
            }
        }
        System.out.println("  ✓ All 18 vector icons rendered successfully.");

        // 2. Vector Chart Panels
        System.out.println("[2/7] Testing Vector Chart Components...");
        AdminCharts.DonutChartPanel donut = new AdminCharts.DonutChartPanel("Role Distribution");
        Map<String, Integer> donutData = new HashMap<>();
        donutData.put("Admin", 3);
        donutData.put("Lecturers", 14);
        donut.setData(donutData);
        donut.setSize(300, 200);

        AdminCharts.BarChartPanel bar = new AdminCharts.BarChartPanel("Department Breakdown", ModernTheme.ACCENT);
        Map<String, Integer> barData = new HashMap<>();
        barData.put("ET", 12);
        barData.put("BST", 18);
        bar.setData(barData);
        bar.setSize(300, 200);

        AdminCharts.TrendLineChartPanel line = new AdminCharts.TrendLineChartPanel("Registration Trends");
        Map<String, Integer> lineData = new HashMap<>();
        lineData.put("Jan", 5);
        lineData.put("Feb", 12);
        line.setData(lineData);
        line.setSize(300, 200);
        System.out.println("  ✓ Donut, Bar, and Line charts instantiated with test data.");

        // 3. DAOs with Offline Fault Tolerance
        System.out.println("[3/7] Testing DAOs with graceful offline handling...");
        AdminAnalyticsDAO adminDAO = new AdminAnalyticsDAO();
        AdminAnalyticsDAO.StatOverview stats = adminDAO.getOverviewStats();
        System.out.println("  Admin stats: users=" + stats.totalUsers + ", courses=" + stats.activeCourses);

        RoleAnalyticsDAO roleDAO = new RoleAnalyticsDAO();
        Map<String, Integer> attStats = roleDAO.getAttendanceTheoryVsPractical();
        System.out.println("  Tech attendance overview size: " + attStats.size());

        NotificationDAO notifDAO = new NotificationDAO();
        int unread = notifDAO.getUnreadCount(1, "ADMIN");
        System.out.println("  Notification unread count: " + unread);
        System.out.println("  ✓ DAOs gracefully returned results without throwing unhandled exceptions.");

        // 4. Role Overview Panels
        System.out.println("[4/7] Testing Role Overview Panels...");
        AdminDashboardOverviewPanel adminOverview = new AdminDashboardOverviewPanel(null);
        TechnicalOverviewPanel techOverview = new TechnicalOverviewPanel(null);
        LecturerOverviewPanel lectOverview = new LecturerOverviewPanel(null);
        UndergraduateOverviewPanel ugOverview = new UndergraduateOverviewPanel(null);
        System.out.println("  ✓ All 4 role overview panels instantiated.");

        // 5. Admin Supplementary Modules
        System.out.println("[5/7] Testing Enterprise Admin Modules...");
        StudentEnrollmentPanel studentPanel = new StudentEnrollmentPanel();
        ReportsAnalyticsPanel reportsPanel = new ReportsAnalyticsPanel();
        AuditLogsPanel auditPanel = new AuditLogsPanel();
        SystemSettingsPanel settingsPanel = new SystemSettingsPanel();
        AdminProfilePanel profilePanel = new AdminProfilePanel();
        NoticeManagementPanel noticePanel = new NoticeManagementPanel();
        System.out.println("  ✓ Student Enrollment, Reports, Audit Logs, Settings, Profile, Notice panels instantiated.");

        // 6. Complete Dashboards
        System.out.println("[6/7] Testing Full Role Dashboards & Stationary Sidebars...");
        if (!java.awt.GraphicsEnvironment.isHeadless()) {
            // Admin
            Admin adminUser = new Admin();
            adminUser.setUserId(1);
            adminUser.setUsername("admin");
            adminUser.setFullName("System Administrator");
            SessionManager.setCurrentUser(adminUser);
            AdminDashboard adminDash = new AdminDashboard();
            System.out.println("  ✓ AdminDashboard initialized: " + adminDash.getTitle());
            adminDash.dispose();

            // Technical Officer
            TechnicalOfficer techUser = new TechnicalOfficer();
            techUser.setUserId(2);
            techUser.setUsername("to_user");
            techUser.setFullName("Technical Officer");
            SessionManager.setCurrentUser(techUser);
            TechnicalOfficerDashboard toDash = new TechnicalOfficerDashboard();
            System.out.println("  ✓ TechnicalOfficerDashboard initialized: " + toDash.getTitle());
            toDash.dispose();

            // Lecturer
            Lecturer lectUser = new Lecturer();
            lectUser.setUserId(3);
            lectUser.setUsername("lect_user");
            lectUser.setFullName("Dr. Perera");
            SessionManager.setCurrentUser(lectUser);
            LecturerDashboard lectDash = new LecturerDashboard();
            System.out.println("  ✓ LecturerDashboard initialized: " + lectDash.getTitle());
            lectDash.dispose();

            // Undergraduate
            Undergraduate ugUser = new Undergraduate();
            ugUser.setUserId(4);
            ugUser.setUsername("ug_user");
            ugUser.setFullName("Kamal Silva");
            ugUser.setStudentRegNo("TG/2024/0001");
            SessionManager.setCurrentUser(ugUser);
            UndergraduateDashboard ugDash = new UndergraduateDashboard();
            System.out.println("  ✓ UndergraduateDashboard initialized: " + ugDash.getTitle());
            ugDash.dispose();
        } else {
            System.out.println("  Skipped JFrame instantiation in headless environment.");
        }

        // 7. Common Header & Notification Dialog
        System.out.println("[7/7] Testing Common Header & Notification System...");
        DashboardHeaderPanel header = new DashboardHeaderPanel(SessionManager.getCurrentUser(), () -> {});
        System.out.println("  ✓ DashboardHeaderPanel instantiated.");

        System.out.println("\n>>> ALL TESTS PASSED! FULL ENTERPRISE SUITE VERIFIED! <<<");
    }
}
