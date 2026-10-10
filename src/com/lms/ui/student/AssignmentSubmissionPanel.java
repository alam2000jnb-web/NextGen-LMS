package com.lms.ui.student;

import com.lms.concurrent.SubmissionProcessor;
import com.lms.db.AssignmentDAO;
import com.lms.db.AuditLogDAO;
import com.lms.db.EnrollmentDAO;
import com.lms.db.SubmissionDAO;
import com.lms.model.Assignment;
import com.lms.model.Enrollment;
import com.lms.model.Student;
import com.lms.model.Submission;
import com.lms.ui.common.DialogHelper;
import com.lms.ui.common.ModernTable;
import com.lms.util.UITheme;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;

public class AssignmentSubmissionPanel extends JPanel {

    private final Student currentStudent;
    private final EnrollmentDAO enrollmentDAO = new EnrollmentDAO();
    private final AssignmentDAO assignmentDAO = new AssignmentDAO();
    private final SubmissionDAO submissionDAO = new SubmissionDAO();
    private final AuditLogDAO auditLogDAO = new AuditLogDAO();

    private final ModernTable tableAssignments;
    private final DefaultTableModel tableModel;

    private List<Assignment> availableAssignments = new ArrayList<>();
    private List<Submission> studentSubmissions = new ArrayList<>();

    public AssignmentSubmissionPanel(Student student) {
        this.currentStudent = student;

        setLayout(new BorderLayout(16, 16));
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JLabel lblTitle = new JLabel("Course Assignment Submission & Status Tracker");
        lblTitle.setFont(UITheme.FONT_TITLE);
        lblTitle.setForeground(UITheme.TEXT_PRIMARY);

        JPanel actionBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actionBtns.setOpaque(false);

        JButton btnSubmit = UITheme.createPrimaryButton("✎ Submit Assignment Work");
        btnSubmit.addActionListener(e -> showSubmitDialog());

        JButton btnRefresh = UITheme.createSecondaryButton("↻ Refresh List");
        btnRefresh.addActionListener(e -> loadAssignments());

        actionBtns.add(btnSubmit);
        actionBtns.add(btnRefresh);

        headerPanel.add(lblTitle, BorderLayout.WEST);
        headerPanel.add(actionBtns, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        // Table
        String[] columns = {"Assignment ID", "Course", "Title", "Max Marks", "Due Date", "My Status", "My Score"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        tableAssignments = new ModernTable(tableModel);

        JScrollPane scrollPane = new JScrollPane(tableAssignments);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Assignments for Enrolled Courses:"));
        add(scrollPane, BorderLayout.CENTER);

        loadAssignments();
    }

    public void loadAssignments() {
        try {
            List<Enrollment> enrolled = enrollmentDAO.findByStudentId(currentStudent.getId());
            availableAssignments.clear();
            for (Enrollment e : enrolled) {
                availableAssignments.addAll(assignmentDAO.findByCourseId(e.getCourseId()));
            }

            studentSubmissions = submissionDAO.findByStudentId(currentStudent.getId());
            tableModel.setRowCount(0);

            for (Assignment a : availableAssignments) {
                // Check if student submitted this assignment
                Submission sub = studentSubmissions.stream()
                        .filter(s -> s.getAssignmentId() == a.getId())
                        .findFirst()
                        .orElse(null);

                String status = sub != null ? sub.getStatus() : "NOT SUBMITTED";
                String score = (sub != null && sub.getMarksObtained() != null) ?
                        String.format("%.1f / %.1f pts", sub.getMarksObtained(), a.getMaxMarks()) : "N/A";

                tableModel.addRow(new Object[]{
                        a.getId(),
                        a.getCourseTitle(),
                        a.getTitle(),
                        String.format("%.0f pts", a.getMaxMarks()),
                        a.getDueDate(),
                        status,
                        score
                });
            }
        } catch (Exception e) {
            DialogHelper.showError(this, "Failed to load assignments: " + e.getMessage());
        }
    }

    private void showSubmitDialog() {
        int row = tableAssignments.getSelectedRow();
        if (row < 0 || row >= availableAssignments.size()) {
            DialogHelper.showWarning(this, "Please select an assignment from the table to submit.");
            return;
        }

        Assignment a = availableAssignments.get(row);

        // Check if already submitted
        Submission existing = studentSubmissions.stream().filter(s -> s.getAssignmentId() == a.getId()).findFirst().orElse(null);
        if (existing != null && "GRADED".equalsIgnoreCase(existing.getStatus())) {
            DialogHelper.showWarning(this, "This assignment has already been evaluated and graded! Resubmissions are closed.");
            return;
        }

        JDialog dlg = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Submit Work: " + a.getTitle(), true);
        dlg.setSize(520, 520);
        dlg.setLocationRelativeTo(this);
        dlg.setLayout(new BorderLayout());

        JPanel info = new JPanel(new GridLayout(3, 2, 8, 6));
        info.setBorder(BorderFactory.createTitledBorder("Assignment Instructions:"));
        info.add(new JLabel("Course:")); info.add(new JLabel(a.getCourseTitle()));
        info.add(new JLabel("Max Score:")); info.add(new JLabel(String.format("%.0f pts", a.getMaxMarks())));
        info.add(new JLabel("Due Date:")); info.add(new JLabel(a.getDueDate()));

        JTextArea txtAnswer = new JTextArea(existing != null ? existing.getSubmissionText() : "", 6, 20);
        txtAnswer.setLineWrap(true);
        txtAnswer.setWrapStyleWord(true);
        JScrollPane answerScroll = new JScrollPane(txtAnswer);
        answerScroll.setBorder(BorderFactory.createTitledBorder("Your Solution / Written Submission:"));

        JPanel attachPanel = new JPanel(new BorderLayout(8, 4));
        attachPanel.add(new JLabel("Attachment File Name (e.g. Solution.zip, Lab1.java):"), BorderLayout.NORTH);
        JTextField txtAttach = new JTextField(existing != null ? existing.getFileAttachment() : "solution_" + currentStudent.getName().toLowerCase().replace(" ", "_") + ".zip");
        attachPanel.add(txtAttach, BorderLayout.CENTER);

        JPanel center = new JPanel(new BorderLayout(0, 10));
        center.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));
        center.add(info, BorderLayout.NORTH);
        center.add(answerScroll, BorderLayout.CENTER);
        center.add(attachPanel, BorderLayout.SOUTH);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 12));
        JButton btnCancel = UITheme.createSecondaryButton("Cancel");
        btnCancel.addActionListener(e -> dlg.dispose());

        JButton btnSave = UITheme.createPrimaryButton("Submit Assignment");
        btnSave.addActionListener(e -> {
            String text = txtAnswer.getText().trim();
            String attach = txtAttach.getText().trim();

            if (text.isEmpty()) {
                DialogHelper.showWarning(dlg, "Submission text cannot be empty!");
                return;
            }

            try {
                if (existing != null) {
                    existing.setSubmissionText(text);
                    existing.setFileAttachment(attach);
                    existing.setStatus("PENDING");
                    submissionDAO.update(existing);
                } else {
                    Submission s = new Submission();
                    s.setAssignmentId(a.getId());
                    s.setStudentId(currentStudent.getId());
                    s.setSubmissionText(text);
                    s.setFileAttachment(attach);
                    s.setStatus("PENDING");
                    Submission saved = submissionDAO.save(s);

                    // Multithreaded background verification task
                    SubmissionProcessor.getInstance().submitTask(new SubmissionProcessor.SubmissionTask(
                            saved.getId(),
                            currentStudent.getId(),
                            currentStudent.getName(),
                            a.getTitle(),
                            "SUBMISSION_VERIFICATION"
                    ));
                }

                auditLogDAO.log(currentStudent.getId(), currentStudent.getName(), "ASSIGNMENT_SUBMISSION",
                        "Submitted solution for: " + a.getTitle());

                // Output confirmation message as required
                DialogHelper.showSuccess(this, "Confirmation: Assignment '" + a.getTitle() + "' submitted successfully!\nYour instructor has been notified for grading.");
                dlg.dispose();
                loadAssignments();
            } catch (Exception ex) {
                DialogHelper.showError(dlg, "Failed to submit assignment: " + ex.getMessage());
            }
        });

        btnPanel.add(btnCancel);
        btnPanel.add(btnSave);

        dlg.add(center, BorderLayout.CENTER);
        dlg.add(btnPanel, BorderLayout.SOUTH);
        dlg.setVisible(true);
    }
}
