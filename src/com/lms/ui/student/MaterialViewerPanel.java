package com.lms.ui.student;

import com.lms.db.EnrollmentDAO;
import com.lms.db.MaterialDAO;
import com.lms.model.CourseMaterial;
import com.lms.model.Enrollment;
import com.lms.model.Student;
import com.lms.ui.common.DialogHelper;
import com.lms.ui.common.ModernTable;
import com.lms.util.UITheme;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class MaterialViewerPanel extends JPanel {

    private final Student currentStudent;
    private final EnrollmentDAO enrollmentDAO = new EnrollmentDAO();
    private final MaterialDAO materialDAO = new MaterialDAO();

    private final JComboBox<EnrollmentItem> cmbCourses;
    private final ModernTable tableMaterials;
    private final DefaultTableModel tableModel;
    private final JTextArea txtContentPreview;

    private List<CourseMaterial> currentMaterials = new ArrayList<>();

    public static class EnrollmentItem {
        public final int courseId;
        public final String title;

        public EnrollmentItem(int courseId, String title) {
            this.courseId = courseId;
            this.title = title;
        }

        @Override
        public String toString() {
            return title;
        }
    }

    public MaterialViewerPanel(Student student) {
        this.currentStudent = student;

        setLayout(new BorderLayout(16, 16));
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JLabel lblTitle = new JLabel("Course Learning Materials & Digital Resources");
        lblTitle.setFont(UITheme.FONT_TITLE);
        lblTitle.setForeground(UITheme.TEXT_PRIMARY);

        JPanel actionBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actionBtns.setOpaque(false);

        JButton btnDownload = UITheme.createPrimaryButton("⤓ Download / Save Material");
        btnDownload.addActionListener(e -> downloadSelectedMaterial());

        JButton btnRefresh = UITheme.createSecondaryButton("↻ Refresh");
        btnRefresh.addActionListener(e -> loadEnrolledCourses());

        actionBtns.add(btnDownload);
        actionBtns.add(btnRefresh);

        headerPanel.add(lblTitle, BorderLayout.WEST);
        headerPanel.add(actionBtns, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        // Filter Bar
        JPanel filterCard = UITheme.createCardPanel();
        filterCard.setLayout(new FlowLayout(FlowLayout.LEFT, 15, 6));
        filterCard.add(new JLabel("Select Enrolled Course:"));
        cmbCourses = new JComboBox<>();
        cmbCourses.setPreferredSize(new Dimension(320, 32));
        cmbCourses.addActionListener(e -> loadMaterialsForSelectedCourse());
        filterCard.add(cmbCourses);

        // Center Content Split View
        JPanel centerPanel = new JPanel(new BorderLayout(0, 12));
        centerPanel.setOpaque(false);

        // Table
        String[] columns = {"ID", "Material Title", "Content Type", "Resource URL / File", "Uploaded Date"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        tableMaterials = new ModernTable(tableModel);

        // Content Viewer
        txtContentPreview = new JTextArea(7, 20);
        txtContentPreview.setEditable(false);
        txtContentPreview.setLineWrap(true);
        txtContentPreview.setWrapStyleWord(true);
        txtContentPreview.setFont(UITheme.FONT_BODY);
        txtContentPreview.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
        JScrollPane viewerScroll = new JScrollPane(txtContentPreview);
        viewerScroll.setBorder(BorderFactory.createTitledBorder("Material Full Content & Study Guide:"));
        viewerScroll.setPreferredSize(new Dimension(800, 160));

        tableMaterials.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int row = tableMaterials.getSelectedRow();
                if (row >= 0 && row < currentMaterials.size()) {
                    CourseMaterial m = currentMaterials.get(row);
                    txtContentPreview.setText(String.format("TITLE: %s\nTYPE: %s\nURL: %s\n\nCONTENT / LECTURE NOTES:\n%s",
                            m.getTitle(), m.getContentType(), m.getFileUrl(),
                            m.getContentData() != null ? m.getContentData() : "No extra content data."));
                }
            }
        });

        JScrollPane tableScroll = new JScrollPane(tableMaterials);
        tableScroll.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_COLOR, 1, true));

        centerPanel.add(filterCard, BorderLayout.NORTH);
        centerPanel.add(tableScroll, BorderLayout.CENTER);
        centerPanel.add(viewerScroll, BorderLayout.SOUTH);

        add(centerPanel, BorderLayout.CENTER);

        loadEnrolledCourses();
    }

    public void loadEnrolledCourses() {
        try {
            List<Enrollment> list = enrollmentDAO.findByStudentId(currentStudent.getId());
            cmbCourses.removeAllItems();
            for (Enrollment e : list) {
                cmbCourses.addItem(new EnrollmentItem(e.getCourseId(), e.getCourseTitle()));
            }
            if (cmbCourses.getItemCount() > 0) {
                cmbCourses.setSelectedIndex(0);
                loadMaterialsForSelectedCourse();
            } else {
                tableModel.setRowCount(0);
                txtContentPreview.setText("You are not currently enrolled in any courses. Browse the Course Catalog to enroll!");
            }
        } catch (Exception e) {
            DialogHelper.showError(this, "Failed to load enrolled courses: " + e.getMessage());
        }
    }

    private void loadMaterialsForSelectedCourse() {
        EnrollmentItem item = (EnrollmentItem) cmbCourses.getSelectedItem();
        if (item == null) return;

        try {
            currentMaterials = materialDAO.findByCourseId(item.courseId);
            tableModel.setRowCount(0);
            for (CourseMaterial m : currentMaterials) {
                tableModel.addRow(new Object[]{
                        m.getId(),
                        m.getTitle(),
                        m.getContentType(),
                        m.getFileUrl(),
                        m.getFormattedDate()
                });
            }
            if (currentMaterials.isEmpty()) {
                txtContentPreview.setText("No learning materials have been posted for this course yet.");
            }
        } catch (Exception e) {
            DialogHelper.showError(this, "Failed to load materials: " + e.getMessage());
        }
    }

    private void downloadSelectedMaterial() {
        int row = tableMaterials.getSelectedRow();
        if (row < 0 || row >= currentMaterials.size()) {
            DialogHelper.showWarning(this, "Please select a course material to download.");
            return;
        }

        CourseMaterial m = currentMaterials.get(row);
        JFileChooser chooser = new JFileChooser();
        String safeName = m.getTitle().replaceAll("[^a-zA-Z0-9.-]", "_") + ".txt";
        chooser.setSelectedFile(new File(safeName));

        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File target = chooser.getSelectedFile();
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(target, StandardCharsets.UTF_8))) {
                writer.write("================================================================================"); writer.newLine();
                writer.write(" " + m.getTitle().toUpperCase()); writer.newLine();
                writer.write(" Content Type: " + m.getContentType()); writer.newLine();
                writer.write(" Resource URL: " + m.getFileUrl()); writer.newLine();
                writer.write("================================================================================"); writer.newLine();
                writer.newLine();
                writer.write(m.getContentData() != null ? m.getContentData() : "No text notes provided."); writer.newLine();

                // Confirmation message
                DialogHelper.showSuccess(this, "Confirmation: Material successfully downloaded and saved to:\n" + target.getAbsolutePath());
            } catch (Exception ex) {
                DialogHelper.showError(this, "Failed to save file: " + ex.getMessage());
            }
        }
    }
}
