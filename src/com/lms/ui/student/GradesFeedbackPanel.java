package com.lms.ui.student;

import com.lms.db.EnrollmentDAO;
import com.lms.db.QuizDAO;
import com.lms.db.SubmissionDAO;
import com.lms.model.Enrollment;
import com.lms.model.QuizQuestion;
import com.lms.model.Student;
import com.lms.model.Submission;
import com.lms.ui.common.DialogHelper;
import com.lms.ui.common.ModernTable;
import com.lms.util.UITheme;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;

public class GradesFeedbackPanel extends JPanel {

    private final Student currentStudent;
    private final SubmissionDAO submissionDAO = new SubmissionDAO();
    private final EnrollmentDAO enrollmentDAO = new EnrollmentDAO();
    private final QuizDAO quizDAO = new QuizDAO();

    private final ModernTable tableGrades;
    private final DefaultTableModel tableModel;
    private final JTextArea txtFeedbackView;

    private List<Submission> studentSubmissions = new ArrayList<>();

    public GradesFeedbackPanel(Student student) {
        this.currentStudent = student;

        setLayout(new BorderLayout(16, 16));
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JLabel lblTitle = new JLabel("Evaluation Grades & Instructor Feedback Center");
        lblTitle.setFont(UITheme.FONT_TITLE);
        lblTitle.setForeground(UITheme.TEXT_PRIMARY);

        JPanel actionBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actionBtns.setOpaque(false);

        JButton btnQuiz = UITheme.createPrimaryButton("★ Take Course Quiz Assessment");
        btnQuiz.addActionListener(e -> takeCourseQuiz());

        JButton btnRefresh = UITheme.createSecondaryButton("↻ Refresh");
        btnRefresh.addActionListener(e -> loadGrades());

        actionBtns.add(btnQuiz);
        actionBtns.add(btnRefresh);

        headerPanel.add(lblTitle, BorderLayout.WEST);
        headerPanel.add(actionBtns, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        // Center Split View
        JPanel centerPanel = new JPanel(new BorderLayout(0, 12));
        centerPanel.setOpaque(false);

        String[] columns = {"ID", "Assignment Title", "Evaluation Status", "Score Awarded", "Feedback Remarks", "Graded Date"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        tableGrades = new ModernTable(tableModel);

        // Feedback view
        txtFeedbackView = new JTextArea(6, 20);
        txtFeedbackView.setEditable(false);
        txtFeedbackView.setLineWrap(true);
        txtFeedbackView.setWrapStyleWord(true);
        txtFeedbackView.setFont(UITheme.FONT_BODY);
        txtFeedbackView.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
        JScrollPane fbScroll = new JScrollPane(txtFeedbackView);
        fbScroll.setBorder(BorderFactory.createTitledBorder("Selected Grade & Instructor Feedback Card:"));
        fbScroll.setPreferredSize(new Dimension(800, 160));

        tableGrades.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int row = tableGrades.getSelectedRow();
                if (row >= 0 && row < studentSubmissions.size()) {
                    Submission s = studentSubmissions.get(row);
                    txtFeedbackView.setText(String.format("Assignment: %s\nStatus: %s\nScore: %s\nGraded: %s\n\nInstructor Feedback:\n%s",
                            s.getAssignmentTitle(), s.getStatus(),
                            s.getMarksObtained() != null ? String.format("%.1f pts", s.getMarksObtained()) : "Pending",
                            s.getFormattedGradedAt(),
                            s.getFeedback() != null ? s.getFeedback() : "No instructor remarks yet."));
                }
            }
        });

        JScrollPane tableScroll = new JScrollPane(tableGrades);
        tableScroll.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_COLOR, 1, true));

        centerPanel.add(tableScroll, BorderLayout.CENTER);
        centerPanel.add(fbScroll, BorderLayout.SOUTH);

        add(centerPanel, BorderLayout.CENTER);

        loadGrades();
    }

    public void loadGrades() {
        try {
            studentSubmissions = submissionDAO.findByStudentId(currentStudent.getId());
            tableModel.setRowCount(0);

            for (Submission s : studentSubmissions) {
                String scoreStr = s.getMarksObtained() != null ? String.format("%.1f pts", s.getMarksObtained()) : "Pending Evaluation";
                String fb = s.getFeedback() != null ? s.getFeedback() : "Waiting for evaluation";
                if (fb.length() > 36) fb = fb.substring(0, 34) + "...";

                tableModel.addRow(new Object[]{
                        s.getId(),
                        s.getAssignmentTitle(),
                        s.getStatus(),
                        scoreStr,
                        fb,
                        s.getFormattedGradedAt()
                });
            }

            if (studentSubmissions.isEmpty()) {
                txtFeedbackView.setText("No submissions found yet. Submit assignments from the Assignment tab to view grades!");
            }
        } catch (Exception e) {
            DialogHelper.showError(this, "Failed to load grades: " + e.getMessage());
        }
    }

    private void takeCourseQuiz() {
        try {
            List<Enrollment> enrolled = enrollmentDAO.findByStudentId(currentStudent.getId());
            if (enrolled.isEmpty()) {
                DialogHelper.showWarning(this, "You need to be enrolled in at least one course to take a quiz!");
                return;
            }

            // Find quiz questions for enrolled courses
            List<QuizQuestion> questions = new ArrayList<>();
            for (Enrollment e : enrolled) {
                questions.addAll(quizDAO.findByCourseId(e.getCourseId()));
            }

            if (questions.isEmpty()) {
                DialogHelper.showWarning(this, "No quiz questions currently available for your enrolled courses.");
                return;
            }

            QuizQuestion q = questions.get(0);

            JDialog dlg = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Interactive Assessment Quiz", true);
            dlg.setSize(520, 420);
            dlg.setLocationRelativeTo(this);
            dlg.setLayout(new BorderLayout());

            JPanel qPanel = new JPanel(new BorderLayout(8, 12));
            qPanel.setBorder(BorderFactory.createEmptyBorder(20, 24, 10, 24));

            JLabel lblPrompt = new JLabel("<html><b>Question:</b> " + q.getQuestionText() + "</html>");
            lblPrompt.setFont(UITheme.FONT_HEADER);
            qPanel.add(lblPrompt, BorderLayout.NORTH);

            JPanel optionsPanel = new JPanel(new GridLayout(4, 1, 6, 8));
            JRadioButton optA = new JRadioButton("A) " + q.getOptionA());
            JRadioButton optB = new JRadioButton("B) " + q.getOptionB());
            JRadioButton optC = new JRadioButton("C) " + q.getOptionC());
            JRadioButton optD = new JRadioButton("D) " + q.getOptionD());

            ButtonGroup bg = new ButtonGroup();
            bg.add(optA); bg.add(optB); bg.add(optC); bg.add(optD);
            optA.setSelected(true);

            optionsPanel.add(optA); optionsPanel.add(optB);
            optionsPanel.add(optC); optionsPanel.add(optD);
            qPanel.add(optionsPanel, BorderLayout.CENTER);

            JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 12));
            JButton btnCancel = UITheme.createSecondaryButton("Cancel");
            btnCancel.addActionListener(e -> dlg.dispose());

            JButton btnSubmit = UITheme.createPrimaryButton("Submit Answer");
            btnSubmit.addActionListener(e -> {
                String chosen = optA.isSelected() ? "A" : (optB.isSelected() ? "B" : (optC.isSelected() ? "C" : "D"));
                boolean isCorrect = chosen.equalsIgnoreCase(q.getCorrectOption());

                if (isCorrect) {
                    DialogHelper.showSuccess(dlg, "CORRECT ANSWER! (+100% Score)\n\nExplanation:\n" + q.getExplanation());
                } else {
                    DialogHelper.showWarning(dlg, "Incorrect option chosen.\nThe correct answer is Option " + q.getCorrectOption() + ".\n\nExplanation:\n" + q.getExplanation());
                }
                dlg.dispose();
            });

            btnPanel.add(btnCancel);
            btnPanel.add(btnSubmit);

            dlg.add(qPanel, BorderLayout.CENTER);
            dlg.add(btnPanel, BorderLayout.SOUTH);
            dlg.setVisible(true);

        } catch (Exception ex) {
            DialogHelper.showError(this, "Failed to launch quiz: " + ex.getMessage());
        }
    }
}
