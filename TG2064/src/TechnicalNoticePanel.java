// ==========================================
// 17. UI TECHNICAL: TechnicalNoticePanel.java
// ==========================================
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Date;

public class TechnicalNoticePanel extends JPanel {
    private final NoticeService noticeService = new NoticeService();
    private DefaultTableModel model;

    public TechnicalNoticePanel(int currentUserId) {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(12, 14, 12, 14));

        String[] columns = { "ID", "Title", "Content", "Date", "Audience", "Posted By", "Status" };
        model = new DefaultTableModel(columns, 0) { public boolean isCellEditable(int r, int c) { return false; } };
        add(new JScrollPane(new JTable(model)), BorderLayout.CENTER);

        JPanel form = new JPanel(new GridLayout(2, 4, 6, 6));
        form.setBorder(BorderFactory.createTitledBorder("Post Notice"));

        JTextField titleField = new JTextField();
        JTextField descField = new JTextField();
        JComboBox<String> targetRoleBox = new JComboBox<>(new String[] { "ALL", "Undergraduate", "Lecturer", "Technical Officer" });
        JButton addBtn = new JButton("Post Notice");

        form.add(new JLabel("Title:")); form.add(titleField);
        form.add(new JLabel("Content:")); form.add(descField);
        form.add(new JLabel("Target Audience:")); form.add(targetRoleBox);
        form.add(addBtn);

        addBtn.addActionListener(e -> {
            try {
                Notice n = new Notice();
                n.setTitle(titleField.getText().trim());
                n.setContent(descField.getText().trim());
                n.setPublishedDate(new Date(System.currentTimeMillis()));
                n.setTargetRole((String) targetRoleBox.getSelectedItem());

                noticeService.createNotice(n, currentUserId);
                loadData();
                JOptionPane.showMessageDialog(this, "Notice published!");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
            }
        });

        add(form, BorderLayout.NORTH);
        loadData();
    }

    public void loadData() {
        model.setRowCount(0);
        try {
            for (Notice n : noticeService.getNoticesForUserRole("Technical Officer")) {
                model.addRow(new Object[]{ n.getNoticeId(), n.getTitle(), n.getContent(), n.getPublishedDate(), n.getTargetRole(), n.getPostedBy(), n.getStatus() });
            }
        } catch (Exception ignored) {}
    }
}
