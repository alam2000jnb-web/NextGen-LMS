package com.lms.ui.instructor;

import com.lms.concurrent.SubmissionProcessor;
import com.lms.db.AuditLogDAO;
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
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;

public class AssignmentGradingPanel extends JPanel {

    private final Instructor currentInstructor;
    private final CourseDAO courseDAO = new CourseDAO();
    private final SubmissionDAO submissionDAO = new SubmissionDAO();
    private final AuditLogDAO auditLogDAO = new AuditLogDAO();

    private final ModernTable tableSubmissions;
    private final DefaultTableModel tableModel;
    private final JComboBox<String> cmbStatusFilter;

    private List<Submission> currentSubmissions = new ArrayList<>();

    public AssignmentGradingPanel(Instructor instructor) {
        this.currentInstructor = instructor;

        setLayout(new BorderLayout(16, 16));
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JLabel lblTitle = new JLabel("Assignment Evaluation & Feedback Portal");
        lblTitle.setFont(UITheme.FONT_TITLE);
        lblTitle.setForeground(UITheme.TEXT_PRIMARY);

        JPanel actionBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actionBtns.setOpaque(false);

        JButton btnGrade = UITheme.createPrimaryButton("✎ Grade Selected Submission");
        btnGrade.addActionListener(e -> gradeSelectedSubmission());

        JButton btnRefresh = UITheme.createSecondaryButton("↻ Refresh Submissions");
        btnRefresh.addActionListener(e -> loadSubmissions());

        actionBtns.add(btnGrade);
        actionBtns.add(btnRefresh);

        headerPanel.add(lblTitle, BorderLayout.WEST);
        headerPanel.add(actionBtns, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        // Filter Bar
        JPanel filterCard = UITheme.createCardPanel();
        filterCard.setLayout(new FlowLayout(FlowLayout.LEFT, 15, 6));

        filterCard.add(new JLabel("Status Filter:"));
        cmbStatusFilter = new JComboBox<>(new String[]{"All Submissions", "PENDING", "GRADED"});
        cmbStatusFilter.addActionListener(e -> filterTable());
        filterCard.add(cmbStatusFilter);

        // Table
        String[] columns = {"ID", "Assignment", "Student Name", "Status", "Score", "Feedback", "Submitted At"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        tableSubmissions = new ModernTable(tableModel);

        JScrollPane scrollPane = new JScrollPane(tableSubmissions);
        scrollPane.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_COLOR, 1, true));

        JPanel center = new JPanel(new BorderLayout(0, 12));
        center.setOpaque(false);
        center.add(filterCard, BorderLayout.NORTH);
        center.add(scrollPane, BorderLayout.CENTER);

        add(center, BorderLayout.CENTER);

        loadSubmissions();
    }

    public void loadSubmissions() {
        try {
            List<Course> myCourses = courseDAO.findByInstructorId(currentInstructor.getId());
            currentSubmissions.clear();
            for (Course c : myCourses) {
                currentSubmissions.addAll(submissionDAO.findByCourseId(c.getId()));
            }
            filterTable();
        } catch (Exception e) {
            DialogHelper.showError(this, "Failed to load submissions: " + e.getMessage());
        }
    }

    private void filterTable() {
        String filter = (String) cmbStatusFilter.getSelectedItem();
        tableModel.setRowCount(0);

        for (Submission s : currentSubmissions) {
            if (filter == null || filter.equals("All Submissions") || s.getStatus().equalsIgnoreCase(filter)) {
                String scoreStr = s.getMarksObtained() != null ? String.format("%.1f pts", s.getMarksObtained()) : "Not Graded";
                String fb = s.getFeedback() != null ? s.getFeedback() : "None";
                if (fb.length() > 30) fb = fb.substring(0, 28) + "...";

                tableModel.addRow(new Object[]{
                        s.getId(),
                        s.getAssignmentTitle(),
                        s.getStudentName(),
                        s.getStatus(),
                        scoreStr,
                        fb,
                        s.getFormattedSubmittedAt()
                });
            }
        }
    }

    private void gradeSelectedSubmission() {
        int row = tableSubmissions.getSelectedRow();
        if (row < 0) {
            DialogHelper.showWarning(this, "Please select a student submission from the table.");
            return;
        }

        int subId = (int) tableModel.getValueAt(row, 0);
        Submission sub = currentSubmissions.stream().filter(s -> s.getId() == subId).findFirst().orElse(null);
        if (sub == null) return;

        JDialog dlg = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Grade Submission #" + subId, true);
        dlg.setSize(520, 520);
        dlg.setLocationRelativeTo(this);
        dlg.setLayout(new BorderLayout());

        JPanel detailsPanel = new JPanel(new GridLayout(3, 2, 8, 8));
        detailsPanel.setBorder(BorderFactory.createTitledBorder("Submission Info:"));
        detailsPanel.add(new JLabel("Assignment:")); detailsPanel.add(new JLabel(sub.getAssignmentTitle()));
        detailsPanel.add(new JLabel("Student:")); detailsPanel.add(new JLabel(sub.getStudentName()));
        detailsPanel.add(new JLabel("Attachment File:")); detailsPanel.add(new JLabel(sub.getFileAttachment() != null ? sub.getFileAttachment() : "None"));

        JTextArea txtAnswer = new JTextArea(sub.getSubmissionText(), 4, 20);
        txtAnswer.setEditable(false);
        txtAnswer.setLineWrap(true);
        txtAnswer.setWrapStyleWord(true);
        JScrollPane answerScroll = new JScrollPane(txtAnswer);
        answerScroll.setBorder(BorderFactory.createTitledBorder("Student Submitted Content:"));

        JPanel gradingForm = new JPanel(new BorderLayout(8, 8));
        gradingForm.setBorder(BorderFactory.createTitledBorder("Instructor Evaluation:"));

        JPanel scoreRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
        scoreRow.add(new JLabel("Marks Awarded (Points):"));
        double initialMarks = sub.getMarksObtained() != null ? sub.getMarksObtained() : 85.0;
        JSpinner spnMarks = new JSpinner(new SpinnerNumberModel(initialMarks, 0.0, 500.0, 1.0));
        scoreRow.add(spnMarks);
        gradingForm.add(scoreRow, BorderLayout.NORTH);

        JTextArea txtFeedback = new JTextArea(sub.getFeedback() != null ? sub.getFeedback() : "Great work! Clear explanations and solid code structure.", 4, 20);
        txtFeedback.setLineWrap(true);
        txtFeedback.setWrapStyleWord(true);
        JScrollPane fbScroll = new JScrollPane(txtFeedback);
        fbScroll.setBorder(BorderFactory.createTitledBorder("Constructive Feedback & Comments:"));
        gradingForm.add(fbScroll, BorderLayout.CENTER);

        JPanel center = new JPanel(new BorderLayout(0, 10));
        center.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));
        center.add(detailsPanel, BorderLayout.NORTH);
        center.add(answerScroll, BorderLayout.CENTER);
        center.add(gradingForm, BorderLayout.SOUTH);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 12));
        JButton btnCancel = UITheme.createSecondaryButton("Cancel");
        btnCancel.addActionListener(e -> dlg.dispose());

        JButton btnSubmitGrade = UITheme.createPrimaryButton("Submit Grade & Feedback");
        btnSubmitGrade.addActionListener(e -> {
            double marks = (Double) spnMarks.getValue();
            String feedback = txtFeedback.getText().trim();

            if (feedback.isEmpty()) {
                DialogHelper.showWarning(dlg, "Please provide feedback for the student!");
                return;
            }

            try {
                submissionDAO.gradeSubmission(sub.getId(), marks, feedback);

                // Multithreaded background processing of notification
                SubmissionProcessor.getInstance().submitTask(new SubmissionProcessor.SubmissionTask(
                        sub.getId(),
                        sub.getStudentId(),
                        sub.getStudentName(),
                        sub.getAssignmentTitle(),
                        "GRADE_NOTIFICATION"
                ));

                auditLogDAO.log(currentInstructor.getId(), currentInstructor.getName(), "ASSIGNMENT_GRADED",
                        String.format("Graded %s for %s with %.1f pts", sub.getAssignmentTitle(), sub.getStudentName(), marks));

                // Confirmation message
                DialogHelper.showSuccess(this, String.format(
                        "Confirmation: Submission graded successfully!\nScore: %.1f pts\nFeedback: \"%s\"",
                        marks, feedback));

                dlg.dispose();
                loadSubmissions();
            } catch (Exception ex) {
                DialogHelper.showError(dlg, "Failed to submit grade: " + ex.getMessage());
            }
        });

        btnPanel.add(btnCancel);
        btnPanel.add(btnSubmitGrade);

        dlg.add(center, BorderLayout.CENTER);
        dlg.add(btnPanel, BorderLayout.SOUTH);
        dlg.setVisible(true);
    }
}
