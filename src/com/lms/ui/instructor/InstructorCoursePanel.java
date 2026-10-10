package com.lms.ui.instructor;

import com.lms.db.AssignmentDAO;
import com.lms.db.AuditLogDAO;
import com.lms.db.CourseDAO;
import com.lms.db.MaterialDAO;
import com.lms.db.QuizDAO;
import com.lms.model.Assignment;
import com.lms.model.Course;
import com.lms.model.CourseMaterial;
import com.lms.model.Instructor;
import com.lms.model.QuizQuestion;
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

public class InstructorCoursePanel extends JPanel {

    private final Instructor currentInstructor;
    private final CourseDAO courseDAO = new CourseDAO();
    private final MaterialDAO materialDAO = new MaterialDAO();
    private final AssignmentDAO assignmentDAO = new AssignmentDAO();
    private final QuizDAO quizDAO = new QuizDAO();
    private final AuditLogDAO auditLogDAO = new AuditLogDAO();

    private final ModernTable tableCourses;
    private final DefaultTableModel tableModel;

    private List<Course> myCourses = new ArrayList<>();

    public InstructorCoursePanel(Instructor instructor) {
        this.currentInstructor = instructor;

        setLayout(new BorderLayout(16, 16));
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JLabel lblTitle = new JLabel("Course & Content Management Suite");
        lblTitle.setFont(UITheme.FONT_TITLE);
        lblTitle.setForeground(UITheme.TEXT_PRIMARY);

        JPanel actionBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actionBtns.setOpaque(false);

        JButton btnAddCourse = UITheme.createPrimaryButton("+ Create New Course");
        btnAddCourse.addActionListener(e -> showCreateCourseDialog());

        JButton btnAddMaterial = UITheme.createSecondaryButton("📁 Add Materials");
        btnAddMaterial.addActionListener(e -> showAddMaterialDialog());

        JButton btnAddAssignment = UITheme.createSecondaryButton("📝 Add Assignment");
        btnAddAssignment.addActionListener(e -> showAddAssignmentDialog());

        JButton btnAddQuiz = UITheme.createSecondaryButton("❓ Add Quiz Question");
        btnAddQuiz.addActionListener(e -> showAddQuizDialog());

        actionBtns.add(btnAddCourse);
        actionBtns.add(btnAddMaterial);
        actionBtns.add(btnAddAssignment);
        actionBtns.add(btnAddQuiz);

        headerPanel.add(lblTitle, BorderLayout.WEST);
        headerPanel.add(actionBtns, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        // Center Table
        String[] columns = {"Course ID", "Code", "Title", "Category", "Enrolled / Capacity", "Status"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        tableCourses = new ModernTable(tableModel);

        JScrollPane scrollPane = new JScrollPane(tableCourses);
        scrollPane.setBorder(BorderFactory.createTitledBorder("My Authored Courses:"));
        add(scrollPane, BorderLayout.CENTER);

        loadMyCourses();
    }

    public void loadMyCourses() {
        try {
            myCourses = courseDAO.findByInstructorId(currentInstructor.getId());
            tableModel.setRowCount(0);
            for (Course c : myCourses) {
                tableModel.addRow(new Object[]{
                        c.getId(),
                        c.getCode(),
                        c.getTitle(),
                        c.getCategory(),
                        c.getEnrolledCount() + " / " + c.getMaxSeats(),
                        c.getStatus()
                });
            }
        } catch (Exception e) {
            DialogHelper.showError(this, "Failed to load courses: " + e.getMessage());
        }
    }

    private Course getSelectedCourse() {
        int row = tableCourses.getSelectedRow();
        if (row < 0) {
            DialogHelper.showWarning(this, "Please select a course from the table first.");
            return null;
        }
        int courseId = (int) tableModel.getValueAt(row, 0);
        return myCourses.stream().filter(c -> c.getId() == courseId).findFirst().orElse(null);
    }

    private void showCreateCourseDialog() {
        JDialog dlg = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Create New Course", true);
        dlg.setSize(520, 520);
        dlg.setLocationRelativeTo(this);
        dlg.setLayout(new BorderLayout());

        JPanel form = new JPanel(new GridLayout(5, 2, 10, 12));
        form.setBorder(BorderFactory.createEmptyBorder(20, 24, 10, 24));

        JTextField txtCode = new JTextField();
        JTextField txtTitle = new JTextField();
        JTextField txtCategory = new JTextField("Computer Science");
        JSpinner spnSeats = new JSpinner(new SpinnerNumberModel(40, 5, 200, 5));
        JComboBox<String> cmbStatus = new JComboBox<>(new String[]{"ACTIVE", "DRAFT"});

        form.add(new JLabel("Course Code:"));
        form.add(txtCode);
        form.add(new JLabel("Course Title:"));
        form.add(txtTitle);
        form.add(new JLabel("Category:"));
        form.add(txtCategory);
        form.add(new JLabel("Max Seat Capacity:"));
        form.add(spnSeats);
        form.add(new JLabel("Status:"));
        form.add(cmbStatus);

        JPanel textPanels = new JPanel(new GridLayout(2, 1, 0, 8));
        textPanels.setBorder(BorderFactory.createEmptyBorder(0, 24, 10, 24));

        JTextArea txtDesc = new JTextArea(3, 20);
        txtDesc.setLineWrap(true);
        txtDesc.setWrapStyleWord(true);
        JScrollPane descScroll = new JScrollPane(txtDesc);
        descScroll.setBorder(BorderFactory.createTitledBorder("Course Description:"));

        JTextArea txtSyllabus = new JTextArea(4, 20);
        txtSyllabus.setLineWrap(true);
        txtSyllabus.setWrapStyleWord(true);
        JScrollPane sylScroll = new JScrollPane(txtSyllabus);
        sylScroll.setBorder(BorderFactory.createTitledBorder("Syllabus / Modules Outline:"));

        textPanels.add(descScroll);
        textPanels.add(sylScroll);

        JPanel center = new JPanel(new BorderLayout());
        center.add(form, BorderLayout.NORTH);
        center.add(textPanels, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 12));
        JButton btnCancel = UITheme.createSecondaryButton("Cancel");
        btnCancel.addActionListener(e -> dlg.dispose());

        JButton btnSave = UITheme.createPrimaryButton("Publish Course");
        btnSave.addActionListener(e -> {
            String code = txtCode.getText().trim();
            String title = txtTitle.getText().trim();
            String cat = txtCategory.getText().trim();
            int seats = (Integer) spnSeats.getValue();
            String status = (String) cmbStatus.getSelectedItem();

            if (code.isEmpty() || title.isEmpty()) {
                DialogHelper.showWarning(dlg, "Course Code and Title are required!");
                return;
            }

            try {
                Course c = new Course();
                c.setCode(code);
                c.setTitle(title);
                c.setCategory(cat);
                c.setInstructorId(currentInstructor.getId());
                c.setInstructorName(currentInstructor.getName());
                c.setMaxSeats(seats);
                c.setStatus(status);
                c.setDescription(txtDesc.getText().trim());
                c.setSyllabus(txtSyllabus.getText().trim());

                courseDAO.save(c);
                auditLogDAO.log(currentInstructor.getId(), currentInstructor.getName(), "COURSE_CREATED",
                        "Created and published course: " + code + " - " + title);

                // Output confirmation message
                DialogHelper.showSuccess(this, "Confirmation: Course '" + title + "' created successfully!");
                dlg.dispose();
                loadMyCourses();
            } catch (Exception ex) {
                DialogHelper.showError(dlg, "Failed to create course: " + ex.getMessage());
            }
        });

        btnPanel.add(btnCancel);
        btnPanel.add(btnSave);

        dlg.add(center, BorderLayout.CENTER);
        dlg.add(btnPanel, BorderLayout.SOUTH);
        dlg.setVisible(true);
    }

    private void showAddMaterialDialog() {
        Course c = getSelectedCourse();
        if (c == null) return;

        JDialog dlg = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Upload Material for " + c.getTitle(), true);
        dlg.setSize(480, 420);
        dlg.setLocationRelativeTo(this);
        dlg.setLayout(new BorderLayout());

        JPanel form = new JPanel(new GridLayout(3, 2, 10, 12));
        form.setBorder(BorderFactory.createEmptyBorder(20, 24, 10, 24));

        JTextField txtTitle = new JTextField();
        JComboBox<String> cmbType = new JComboBox<>(new String[]{"DOCUMENT", "VIDEO_NOTES", "SLIDES", "READING", "CODE_LAB"});
        JTextField txtUrl = new JTextField("https://lms.edu/materials/lecture.pdf");

        form.add(new JLabel("Material Title:"));
        form.add(txtTitle);
        form.add(new JLabel("Content Type:"));
        form.add(cmbType);
        form.add(new JLabel("Resource URL / Link:"));
        form.add(txtUrl);

        JTextArea txtContent = new JTextArea(5, 20);
        txtContent.setLineWrap(true);
        txtContent.setWrapStyleWord(true);
        JScrollPane scroll = new JScrollPane(txtContent);
        scroll.setBorder(BorderFactory.createTitledBorder("Material Overview / Key Notes:"));

        JPanel center = new JPanel(new BorderLayout(0, 10));
        center.setBorder(BorderFactory.createEmptyBorder(0, 24, 10, 24));
        center.add(form, BorderLayout.NORTH);
        center.add(scroll, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 12));
        JButton btnCancel = UITheme.createSecondaryButton("Cancel");
        btnCancel.addActionListener(e -> dlg.dispose());

        JButton btnSave = UITheme.createPrimaryButton("Add Material");
        btnSave.addActionListener(e -> {
            String mTitle = txtTitle.getText().trim();
            if (mTitle.isEmpty()) {
                DialogHelper.showWarning(dlg, "Material title cannot be empty!");
                return;
            }

            try {
                CourseMaterial m = new CourseMaterial();
                m.setCourseId(c.getId());
                m.setTitle(mTitle);
                m.setContentType((String) cmbType.getSelectedItem());
                m.setFileUrl(txtUrl.getText().trim());
                m.setContentData(txtContent.getText().trim());

                materialDAO.save(m);
                auditLogDAO.log(currentInstructor.getId(), currentInstructor.getName(), "MATERIAL_ADDED",
                        "Added material '" + mTitle + "' to course #" + c.getId());

                DialogHelper.showSuccess(this, "Confirmation: Course material '" + mTitle + "' added successfully!");
                dlg.dispose();
            } catch (Exception ex) {
                DialogHelper.showError(dlg, "Failed to add material: " + ex.getMessage());
            }
        });

        btnPanel.add(btnCancel);
        btnPanel.add(btnSave);

        dlg.add(center, BorderLayout.CENTER);
        dlg.add(btnPanel, BorderLayout.SOUTH);
        dlg.setVisible(true);
    }

    private void showAddAssignmentDialog() {
        Course c = getSelectedCourse();
        if (c == null) return;

        JDialog dlg = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Create Assignment for " + c.getTitle(), true);
        dlg.setSize(480, 420);
        dlg.setLocationRelativeTo(this);
        dlg.setLayout(new BorderLayout());

        JPanel form = new JPanel(new GridLayout(3, 2, 10, 12));
        form.setBorder(BorderFactory.createEmptyBorder(20, 24, 10, 24));

        JTextField txtTitle = new JTextField();
        JSpinner spnMarks = new JSpinner(new SpinnerNumberModel(100.0, 10.0, 500.0, 10.0));
        JTextField txtDueDate = new JTextField("2026-11-15");

        form.add(new JLabel("Assignment Title:"));
        form.add(txtTitle);
        form.add(new JLabel("Maximum Marks:"));
        form.add(spnMarks);
        form.add(new JLabel("Due Date (YYYY-MM-DD):"));
        form.add(txtDueDate);

        JTextArea txtDesc = new JTextArea(5, 20);
        txtDesc.setLineWrap(true);
        txtDesc.setWrapStyleWord(true);
        JScrollPane scroll = new JScrollPane(txtDesc);
        scroll.setBorder(BorderFactory.createTitledBorder("Assignment Instructions & Rubric:"));

        JPanel center = new JPanel(new BorderLayout(0, 10));
        center.setBorder(BorderFactory.createEmptyBorder(0, 24, 10, 24));
        center.add(form, BorderLayout.NORTH);
        center.add(scroll, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 12));
        JButton btnCancel = UITheme.createSecondaryButton("Cancel");
        btnCancel.addActionListener(e -> dlg.dispose());

        JButton btnSave = UITheme.createPrimaryButton("Create Assignment");
        btnSave.addActionListener(e -> {
            String aTitle = txtTitle.getText().trim();
            if (aTitle.isEmpty()) {
                DialogHelper.showWarning(dlg, "Assignment title cannot be empty!");
                return;
            }

            try {
                Assignment a = new Assignment();
                a.setCourseId(c.getId());
                a.setTitle(aTitle);
                a.setMaxMarks((Double) spnMarks.getValue());
                a.setDueDate(txtDueDate.getText().trim());
                a.setDescription(txtDesc.getText().trim());

                assignmentDAO.save(a);
                auditLogDAO.log(currentInstructor.getId(), currentInstructor.getName(), "ASSIGNMENT_CREATED",
                        "Created assignment '" + aTitle + "' in course #" + c.getId());

                DialogHelper.showSuccess(this, "Confirmation: Assignment '" + aTitle + "' created successfully!");
                dlg.dispose();
            } catch (Exception ex) {
                DialogHelper.showError(dlg, "Failed to create assignment: " + ex.getMessage());
            }
        });

        btnPanel.add(btnCancel);
        btnPanel.add(btnSave);

        dlg.add(center, BorderLayout.CENTER);
        dlg.add(btnPanel, BorderLayout.SOUTH);
        dlg.setVisible(true);
    }

    private void showAddQuizDialog() {
        Course c = getSelectedCourse();
        if (c == null) return;

        JDialog dlg = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Add Quiz Question for " + c.getTitle(), true);
        dlg.setSize(500, 480);
        dlg.setLocationRelativeTo(this);
        dlg.setLayout(new BorderLayout());

        JPanel form = new JPanel(new GridLayout(6, 2, 10, 10));
        form.setBorder(BorderFactory.createEmptyBorder(20, 24, 10, 24));

        JTextField txtQuestion = new JTextField();
        JTextField txtA = new JTextField();
        JTextField txtB = new JTextField();
        JTextField txtC = new JTextField();
        JTextField txtD = new JTextField();
        JComboBox<String> cmbCorrect = new JComboBox<>(new String[]{"A", "B", "C", "D"});

        form.add(new JLabel("Question Prompt:")); form.add(txtQuestion);
        form.add(new JLabel("Option A:")); form.add(txtA);
        form.add(new JLabel("Option B:")); form.add(txtB);
        form.add(new JLabel("Option C:")); form.add(txtC);
        form.add(new JLabel("Option D:")); form.add(txtD);
        form.add(new JLabel("Correct Option:")); form.add(cmbCorrect);

        JTextField txtExpl = new JTextField();
        JPanel center = new JPanel(new BorderLayout(0, 10));
        center.setBorder(BorderFactory.createEmptyBorder(0, 24, 10, 24));
        center.add(form, BorderLayout.NORTH);

        JPanel explPanel = new JPanel(new BorderLayout(0, 4));
        explPanel.add(new JLabel("Explanation / Answer Key:"), BorderLayout.NORTH);
        explPanel.add(txtExpl, BorderLayout.CENTER);
        center.add(explPanel, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 12));
        JButton btnCancel = UITheme.createSecondaryButton("Cancel");
        btnCancel.addActionListener(e -> dlg.dispose());

        JButton btnSave = UITheme.createPrimaryButton("Save Question");
        btnSave.addActionListener(e -> {
            String qText = txtQuestion.getText().trim();
            if (qText.isEmpty() || txtA.getText().trim().isEmpty()) {
                DialogHelper.showWarning(dlg, "Please provide the question prompt and option text!");
                return;
            }

            try {
                QuizQuestion q = new QuizQuestion();
                q.setCourseId(c.getId());
                q.setQuestionText(qText);
                q.setOptionA(txtA.getText().trim());
                q.setOptionB(txtB.getText().trim());
                q.setOptionC(txtC.getText().trim());
                q.setOptionD(txtD.getText().trim());
                q.setCorrectOption((String) cmbCorrect.getSelectedItem());
                q.setExplanation(txtExpl.getText().trim());

                quizDAO.save(q);
                auditLogDAO.log(currentInstructor.getId(), currentInstructor.getName(), "QUIZ_CREATED",
                        "Added quiz question to course #" + c.getId());

                DialogHelper.showSuccess(this, "Confirmation: Quiz question added successfully!");
                dlg.dispose();
            } catch (Exception ex) {
                DialogHelper.showError(dlg, "Failed to save quiz question: " + ex.getMessage());
            }
        });

        btnPanel.add(btnCancel);
        btnPanel.add(btnSave);

        dlg.add(center, BorderLayout.CENTER);
        dlg.add(btnPanel, BorderLayout.SOUTH);
        dlg.setVisible(true);
    }
}
