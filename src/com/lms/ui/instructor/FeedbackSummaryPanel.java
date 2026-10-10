package com.lms.ui.instructor;

import com.lms.db.CourseDAO;
import com.lms.db.SubmissionDAO;
import com.lms.model.Course;
import com.lms.model.Instructor;
import com.lms.model.Submission;
import com.lms.ui.common.DialogHelper;
import com.lms.ui.common.ModernTable;
import com.lms.util.UITheme;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.List;

public class FeedbackSummaryPanel extends JPanel {

    private final Instructor currentInstructor;
    private final CourseDAO courseDAO = new CourseDAO();
    private final SubmissionDAO submissionDAO = new SubmissionDAO();

    private final ModernTable tableFeedback;
    private final DefaultTableModel tableModel;
    private final JTextArea txtDetailFeedback;
    private final JTextField txtSearch;

    private List<Submission> gradedSubmissions = new ArrayList<>();

    public FeedbackSummaryPanel(Instructor instructor) {
        this.currentInstructor = instructor;

        setLayout(new BorderLayout(16, 16));
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JLabel lblTitle = new JLabel("Student Assignment Feedback Summary & History");
        lblTitle.setFont(UITheme.FONT_TITLE);
        lblTitle.setForeground(UITheme.TEXT_PRIMARY);

        JButton btnRefresh = UITheme.createPrimaryButton("↻ Refresh Feedbacks");
        btnRefresh.addActionListener(e -> loadFeedbacks());

        headerPanel.add(lblTitle, BorderLayout.WEST);
        headerPanel.add(btnRefresh, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        // Center Split View
        JPanel centerPanel = new JPanel(new BorderLayout(0, 12));
        centerPanel.setOpaque(false);

        // Filter Bar
        JPanel searchCard = UITheme.createCardPanel();
        searchCard.setLayout(new FlowLayout(FlowLayout.LEFT, 15, 6));
        searchCard.add(new JLabel("Search Feedback or Student:"));
        txtSearch = new JTextField(20);
        txtSearch.putClientProperty("JTextField.placeholderText", "Search student, assignment, or remarks...");
        txtSearch.addCaretListener(e -> filterTable());
        searchCard.add(txtSearch);
        centerPanel.add(searchCard, BorderLayout.NORTH);

        // Table
        String[] columns = {"ID", "Assignment Title", "Student Name", "Score", "Feedback Remarks"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        tableFeedback = new ModernTable(tableModel);

        // Detail feedback panel
        txtDetailFeedback = new JTextArea(6, 20);
        txtDetailFeedback.setEditable(false);
        txtDetailFeedback.setLineWrap(true);
        txtDetailFeedback.setWrapStyleWord(true);
        txtDetailFeedback.setFont(UITheme.FONT_BODY);
        txtDetailFeedback.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
        JScrollPane detailScroll = new JScrollPane(txtDetailFeedback);
        detailScroll.setBorder(BorderFactory.createTitledBorder("Selected Feedback Full View:"));
        detailScroll.setPreferredSize(new Dimension(800, 140));

        tableFeedback.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int row = tableFeedback.getSelectedRow();
                if (row >= 0 && row < gradedSubmissions.size()) {
                    Submission s = gradedSubmissions.get(row);
                    txtDetailFeedback.setText(String.format("Assignment: %s\nStudent: %s\nScore: %.1f points\n\nDetailed Evaluation Feedback:\n%s",
                            s.getAssignmentTitle(), s.getStudentName(), s.getMarksObtained() != null ? s.getMarksObtained() : 0.0,
                            s.getFeedback() != null ? s.getFeedback() : "No remarks"));
                }
            }
        });

        JScrollPane tableScroll = new JScrollPane(tableFeedback);
        tableScroll.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_COLOR, 1, true));

        centerPanel.add(tableScroll, BorderLayout.CENTER);
        centerPanel.add(detailScroll, BorderLayout.SOUTH);

        add(centerPanel, BorderLayout.CENTER);

        loadFeedbacks();
    }

    public void loadFeedbacks() {
        try {
            List<Course> courses = courseDAO.findByInstructorId(currentInstructor.getId());
            gradedSubmissions.clear();
            for (Course c : courses) {
                List<Submission> list = submissionDAO.findByCourseId(c.getId());
                for (Submission s : list) {
                    if (s.isGraded()) {
                        gradedSubmissions.add(s);
                    }
                }
            }
            filterTable();
        } catch (Exception e) {
            DialogHelper.showError(this, "Failed to load feedbacks: " + e.getMessage());
        }
    }

    private void filterTable() {
        String query = txtSearch.getText().trim().toLowerCase();
        tableModel.setRowCount(0);

        for (Submission s : gradedSubmissions) {
            if (query.isEmpty() ||
                    s.getStudentName().toLowerCase().contains(query) ||
                    s.getAssignmentTitle().toLowerCase().contains(query) ||
                    (s.getFeedback() != null && s.getFeedback().toLowerCase().contains(query))) {

                String scoreStr = s.getMarksObtained() != null ? String.format("%.1f pts", s.getMarksObtained()) : "N/A";
                tableModel.addRow(new Object[]{
                        s.getId(),
                        s.getAssignmentTitle(),
                        s.getStudentName(),
                        scoreStr,
                        s.getFeedback() != null ? s.getFeedback() : "None"
                });
            }
        }
    }
}
