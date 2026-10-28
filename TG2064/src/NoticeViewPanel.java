// ==========================================
// 21. UI UNDERGRADUATE: NoticeViewPanel.java
// ==========================================
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class NoticeViewPanel extends JPanel {
    private final NoticeService noticeService = new NoticeService();
    private DefaultTableModel model;

    public NoticeViewPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(12, 14, 12, 14));

        String[] columns = { "Notice Title", "Content / Details", "Published Date", "Posted By" };
        model = new DefaultTableModel(columns, 0) { public boolean isCellEditable(int r, int c) { return false; } };
        add(new JScrollPane(new JTable(model)), BorderLayout.CENTER);

        loadData();
    }

    public void loadData() {
        model.setRowCount(0);
        try {
            for (Notice n : noticeService.getNoticesForUserRole("Undergraduate")) {
                model.addRow(new Object[]{ n.getTitle(), n.getContent(), n.getPublishedDate(), n.getPostedBy() });
            }
        } catch (Exception ignored) {}
    }
}
