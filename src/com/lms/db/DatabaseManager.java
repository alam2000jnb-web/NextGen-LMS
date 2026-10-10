package com.lms.db;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Singleton Database Manager handling SQLite JDBC Connection Lifecycle,
 * schema initialization, and realistic seed data injection.
 */
public class DatabaseManager {
    private static DatabaseManager instance;
    private static final String DB_FILE = "lms.db";
    private static final String JDBC_URL = "jdbc:sqlite:" + DB_FILE;

    private DatabaseManager() {
        try {
            Class.forName("org.sqlite.JDBC");
            initDatabase();
        } catch (ClassNotFoundException | SQLException e) {
            System.err.println("Fatal: Failed to initialize SQLite database: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    public Connection getConnection() throws SQLException {
        Connection connection = DriverManager.getConnection(JDBC_URL);
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("PRAGMA foreign_keys = ON;");
            stmt.execute("PRAGMA busy_timeout = 5000;");
            return connection;
        } catch (SQLException e) {
            try {
                connection.close();
            } catch (SQLException closeError) {
                e.addSuppressed(closeError);
            }
            throw e;
        }
    }

    private void initDatabase() throws SQLException {
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            // 1. Users Table
            stmt.execute("CREATE TABLE IF NOT EXISTS users (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "name TEXT NOT NULL, " +
                    "email TEXT UNIQUE NOT NULL, " +
                    "password_hash TEXT NOT NULL, " +
                    "role TEXT NOT NULL, " +
                    "status TEXT DEFAULT 'ACTIVE', " +
                    "dept_or_major TEXT, " +
                    "created_at TEXT DEFAULT CURRENT_TIMESTAMP" +
                    ");");

            // 2. Courses Table
            stmt.execute("CREATE TABLE IF NOT EXISTS courses (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "code TEXT UNIQUE NOT NULL, " +
                    "title TEXT NOT NULL, " +
                    "description TEXT, " +
                    "category TEXT DEFAULT 'General', " +
                    "instructor_id INTEGER NOT NULL, " +
                    "max_seats INTEGER DEFAULT 50, " +
                    "enrolled_count INTEGER DEFAULT 0, " +
                    "syllabus TEXT, " +
                    "status TEXT DEFAULT 'ACTIVE', " +
                    "created_at TEXT DEFAULT CURRENT_TIMESTAMP, " +
                    "FOREIGN KEY (instructor_id) REFERENCES users(id) ON DELETE CASCADE" +
                    ");");

            // 3. Materials Table
            stmt.execute("CREATE TABLE IF NOT EXISTS materials (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "course_id INTEGER NOT NULL, " +
                    "title TEXT NOT NULL, " +
                    "content_type TEXT DEFAULT 'DOCUMENT', " +
                    "content_data TEXT, " +
                    "file_url TEXT, " +
                    "uploaded_at TEXT DEFAULT CURRENT_TIMESTAMP, " +
                    "FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE CASCADE" +
                    ");");

            // 4. Assignments Table
            stmt.execute("CREATE TABLE IF NOT EXISTS assignments (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "course_id INTEGER NOT NULL, " +
                    "title TEXT NOT NULL, " +
                    "description TEXT, " +
                    "max_marks REAL DEFAULT 100.0, " +
                    "due_date TEXT, " +
                    "created_at TEXT DEFAULT CURRENT_TIMESTAMP, " +
                    "FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE CASCADE" +
                    ");");

            // 5. Submissions Table
            stmt.execute("CREATE TABLE IF NOT EXISTS submissions (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "assignment_id INTEGER NOT NULL, " +
                    "student_id INTEGER NOT NULL, " +
                    "submission_text TEXT, " +
                    "file_attachment TEXT, " +
                    "marks_obtained REAL, " +
                    "feedback TEXT, " +
                    "status TEXT DEFAULT 'PENDING', " +
                    "submitted_at TEXT DEFAULT CURRENT_TIMESTAMP, " +
                    "graded_at TEXT, " +
                    "FOREIGN KEY (assignment_id) REFERENCES assignments(id) ON DELETE CASCADE, " +
                    "FOREIGN KEY (student_id) REFERENCES users(id) ON DELETE CASCADE" +
                    ");");

            // 6. Enrollments Table
            stmt.execute("CREATE TABLE IF NOT EXISTS enrollments (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "course_id INTEGER NOT NULL, " +
                    "student_id INTEGER NOT NULL, " +
                    "progress_percent INTEGER DEFAULT 0, " +
                    "status TEXT DEFAULT 'ACTIVE', " +
                    "final_grade REAL, " +
                    "enrolled_at TEXT DEFAULT CURRENT_TIMESTAMP, " +
                    "UNIQUE(course_id, student_id), " +
                    "FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE CASCADE, " +
                    "FOREIGN KEY (student_id) REFERENCES users(id) ON DELETE CASCADE" +
                    ");");

            // 7. Audit Logs Table
            stmt.execute("CREATE TABLE IF NOT EXISTS audit_logs (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "timestamp TEXT DEFAULT CURRENT_TIMESTAMP, " +
                    "user_id INTEGER, " +
                    "user_name TEXT, " +
                    "action TEXT NOT NULL, " +
                    "details TEXT, " +
                    "ip_address TEXT DEFAULT '127.0.0.1'" +
                    ");");

            // 8. System Settings Table
            stmt.execute("CREATE TABLE IF NOT EXISTS system_settings (" +
                    "setting_key TEXT PRIMARY KEY, " +
                    "setting_value TEXT NOT NULL, " +
                    "description TEXT, " +
                    "category TEXT DEFAULT 'GENERAL'" +
                    ");");

            // 9. Quiz Questions Table
            stmt.execute("CREATE TABLE IF NOT EXISTS quiz_questions (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "course_id INTEGER NOT NULL, " +
                    "question_text TEXT NOT NULL, " +
                    "option_a TEXT NOT NULL, " +
                    "option_b TEXT NOT NULL, " +
                    "option_c TEXT NOT NULL, " +
                    "option_d TEXT NOT NULL, " +
                    "correct_option TEXT NOT NULL, " +
                    "explanation TEXT, " +
                    "FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE CASCADE" +
                    ");");

            seedInitialData(conn);
        }
    }

    private void seedInitialData(Connection conn) throws SQLException {
        // Check if users already seeded
        try (Statement checkStmt = conn.createStatement();
             ResultSet rs = checkStmt.executeQuery("SELECT COUNT(*) FROM users;")) {
            if (rs.next() && rs.getInt(1) > 0) {
                return; // Already seeded
            }
        }

        // Insert Default Users (Admin, Instructors, Students)
        // Passwords are stored in plain/simple hash for academic demo simplicity: "admin123", "instructor123", "student123"
        String insertUserSql = "INSERT INTO users (name, email, password_hash, role, status, dept_or_major) VALUES (?, ?, ?, ?, ?, ?);";
        try (PreparedStatement pstmt = conn.prepareStatement(insertUserSql)) {
            // Admin
            pstmt.setString(1, "Dr. Sarah Admin");
            pstmt.setString(2, "admin@lms.edu");
            pstmt.setString(3, "admin123");
            pstmt.setString(4, "ADMIN");
            pstmt.setString(5, "ACTIVE");
            pstmt.setString(6, "System Administration");
            pstmt.executeUpdate();

            // Instructors
            pstmt.setString(1, "Prof. Alan Turing");
            pstmt.setString(2, "alan@lms.edu");
            pstmt.setString(3, "instructor123");
            pstmt.setString(4, "INSTRUCTOR");
            pstmt.setString(5, "ACTIVE");
            pstmt.setString(6, "Computer Science");
            pstmt.executeUpdate();

            pstmt.setString(1, "Prof. Ada Lovelace");
            pstmt.setString(2, "ada@lms.edu");
            pstmt.setString(3, "instructor123");
            pstmt.setString(4, "INSTRUCTOR");
            pstmt.setString(5, "ACTIVE");
            pstmt.setString(6, "Software Engineering");
            pstmt.executeUpdate();

            pstmt.setString(1, "Dr. Andrew Ng");
            pstmt.setString(2, "andrew@lms.edu");
            pstmt.setString(3, "instructor123");
            pstmt.setString(4, "INSTRUCTOR");
            pstmt.setString(5, "ACTIVE");
            pstmt.setString(6, "Artificial Intelligence");
            pstmt.executeUpdate();

            // Students
            pstmt.setString(1, "Shivam Kumar");
            pstmt.setString(2, "shivam@lms.edu");
            pstmt.setString(3, "student123");
            pstmt.setString(4, "STUDENT");
            pstmt.setString(5, "ACTIVE");
            pstmt.setString(6, "Computer Science");
            pstmt.executeUpdate();

            pstmt.setString(1, "Priya Sharma");
            pstmt.setString(2, "priya@lms.edu");
            pstmt.setString(3, "student123");
            pstmt.setString(4, "STUDENT");
            pstmt.setString(5, "ACTIVE");
            pstmt.setString(6, "Information Technology");
            pstmt.executeUpdate();

            pstmt.setString(1, "Rahul Verma");
            pstmt.setString(2, "rahul@lms.edu");
            pstmt.setString(3, "student123");
            pstmt.setString(4, "STUDENT");
            pstmt.setString(5, "ACTIVE");
            pstmt.setString(6, "Data Science");
            pstmt.executeUpdate();

            pstmt.setString(1, "Ananya Patel");
            pstmt.setString(2, "ananya@lms.edu");
            pstmt.setString(3, "student123");
            pstmt.setString(4, "STUDENT");
            pstmt.setString(5, "ACTIVE");
            pstmt.setString(6, "Cyber Security");
            pstmt.executeUpdate();
        }

        // Insert Default Courses
        String insertCourseSql = "INSERT INTO courses (code, title, description, category, instructor_id, max_seats, enrolled_count, syllabus, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?);";
        try (PreparedStatement pstmt = conn.prepareStatement(insertCourseSql)) {
            // Course 1 (Instructor id 2: Alan)
            pstmt.setString(1, "CS101");
            pstmt.setString(2, "Data Structures & Algorithms");
            pstmt.setString(3, "Comprehensive study of arrays, linked lists, trees, graphs, sorting, and algorithmic complexity.");
            pstmt.setString(4, "Core CS");
            pstmt.setInt(5, 2);
            pstmt.setInt(6, 40);
            pstmt.setInt(7, 3);
            pstmt.setString(8, "Week 1: Big-O & Arrays\nWeek 2: Linked Lists & Stacks\nWeek 3: Binary Search Trees & AVL\nWeek 4: Graph Traversals (BFS/DFS)\nWeek 5: Dynamic Programming");
            pstmt.setString(9, "ACTIVE");
            pstmt.executeUpdate();

            // Course 2 (Instructor id 3: Ada)
            pstmt.setString(1, "CS201");
            pstmt.setString(2, "Advanced Java & OOP Design Patterns");
            pstmt.setString(3, "Master modern Java, Swing GUI, Multithreading, Generics, and Gang of Four Design Patterns.");
            pstmt.setString(4, "Software Eng");
            pstmt.setInt(5, 3);
            pstmt.setInt(6, 35);
            pstmt.setInt(7, 4);
            pstmt.setString(8, "Week 1: OOP Principles & Polymorphism\nWeek 2: Generics & Collections Framework\nWeek 3: Multithreading & Synchronization\nWeek 4: JDBC & Persistence Architecture\nWeek 5: Clean Code & Architecture");
            pstmt.setString(9, "ACTIVE");
            pstmt.executeUpdate();

            // Course 3 (Instructor id 4: Andrew)
            pstmt.setString(1, "AI301");
            pstmt.setString(2, "Machine Learning & Neural Networks");
            pstmt.setString(3, "Introduction to supervised and unsupervised learning, regression, classification, and deep neural nets.");
            pstmt.setString(4, "Data Science");
            pstmt.setInt(5, 4);
            pstmt.setInt(6, 50);
            pstmt.setInt(7, 2);
            pstmt.setString(8, "Week 1: Linear & Logistic Regression\nWeek 2: Decision Trees & Random Forests\nWeek 3: Neural Networks Architecture\nWeek 4: Convolutional Neural Nets\nWeek 5: Model Deployment & Evaluation");
            pstmt.setString(9, "ACTIVE");
            pstmt.executeUpdate();

            // Course 4 (Instructor id 2: Alan)
            pstmt.setString(1, "CS401");
            pstmt.setString(2, "Database Systems & SQL Optimization");
            pstmt.setString(3, "Relational database design, normal forms, indexing, ACID transactions, and query optimization.");
            pstmt.setString(4, "Databases");
            pstmt.setInt(5, 2);
            pstmt.setInt(6, 30);
            pstmt.setInt(7, 1);
            pstmt.setString(8, "Week 1: Relational Algebra & SQL\nWeek 2: Normalization (1NF to BCNF)\nWeek 3: Storage, B-Trees & Indexing\nWeek 4: Query Execution & Optimization\nWeek 5: Concurrency Control & Recovery");
            pstmt.setString(9, "ACTIVE");
            pstmt.executeUpdate();
        }

        // Insert Course Materials
        String insertMatSql = "INSERT INTO materials (course_id, title, content_type, content_data, file_url) VALUES (?, ?, ?, ?, ?);";
        try (PreparedStatement pstmt = conn.prepareStatement(insertMatSql)) {
            // Materials for CS201 (Java)
            pstmt.setInt(1, 2);
            pstmt.setString(2, "Module 1: Java OOP Mastery Guide");
            pstmt.setString(3, "DOCUMENT");
            pstmt.setString(4, "Deep dive into Abstraction, Encapsulation, Polymorphism, and Inheritance. Includes clean interface designs and SOLID architectural principles.");
            pstmt.setString(5, "https://lms.edu/materials/cs201_m1_oop.pdf");
            pstmt.executeUpdate();

            pstmt.setInt(1, 2);
            pstmt.setString(2, "Module 2: Java Collections & Generics Guide");
            pstmt.setString(3, "READING");
            pstmt.setString(4, "Detailed breakdown of ArrayList, LinkedList, HashMap, ConcurrentHashMap, TreeSet, and bounded wildcards <? extends T>.");
            pstmt.setString(5, "https://lms.edu/materials/cs201_m2_collections.pdf");
            pstmt.executeUpdate();

            pstmt.setInt(1, 2);
            pstmt.setString(2, "Module 3: Concurrency, Locks & Multithreading Video Notes");
            pstmt.setString(3, "VIDEO_NOTES");
            pstmt.setString(4, "Lecture summary covering thread lifecycles, synchronized blocks, ReentrantLock, ExecutorService, and SwingWorker GUI integration.");
            pstmt.setString(5, "https://lms.edu/materials/cs201_m3_multithreading.mp4");
            pstmt.executeUpdate();

            // Materials for CS101 (DSA)
            pstmt.setInt(1, 1);
            pstmt.setString(2, "DSA Module 1: Complexity Analysis & Arrays");
            pstmt.setString(3, "DOCUMENT");
            pstmt.setString(4, "Mathematical foundation of Big-O, Omega, Theta notation, amortized analysis, dynamic array resizing algorithms.");
            pstmt.setString(5, "https://lms.edu/materials/cs101_m1_arrays.pdf");
            pstmt.executeUpdate();
        }

        // Insert Assignments
        String insertAssignSql = "INSERT INTO assignments (course_id, title, description, max_marks, due_date) VALUES (?, ?, ?, ?, ?);";
        try (PreparedStatement pstmt = conn.prepareStatement(insertAssignSql)) {
            // CS201 Assignment 1
            pstmt.setInt(1, 2);
            pstmt.setString(2, "Mini-Project: Thread-Safe Banking Application");
            pstmt.setString(3, "Implement an account transaction system using Java Multithreading and synchronized methods with lock safety.");
            pstmt.setDouble(4, 100.0);
            pstmt.setString(5, "2026-10-25");
            pstmt.executeUpdate();

            // CS201 Assignment 2
            pstmt.setInt(1, 2);
            pstmt.setString(2, "Generic Collection Pipeline Implementation");
            pstmt.setString(3, "Build custom generic filtering, mapping, and reducing methods adhering to Java 8+ functional interfaces.");
            pstmt.setDouble(4, 50.0);
            pstmt.setString(5, "2026-11-05");
            pstmt.executeUpdate();

            // CS101 Assignment 1
            pstmt.setInt(1, 1);
            pstmt.setString(2, "Binary Search Tree & AVL Balancing");
            pstmt.setString(3, "Implement a self-balancing AVL Tree in Java with left and right rotations.");
            pstmt.setDouble(4, 100.0);
            pstmt.setString(5, "2026-10-30");
            pstmt.executeUpdate();
        }

        // Insert Submissions
        String insertSubSql = "INSERT INTO submissions (assignment_id, student_id, submission_text, file_attachment, marks_obtained, feedback, status, graded_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?);";
        try (PreparedStatement pstmt = conn.prepareStatement(insertSubSql)) {
            // Shivam submitted CS201 Assignment 1 -> Graded
            pstmt.setInt(1, 1);
            pstmt.setInt(2, 5); // Shivam
            pstmt.setString(3, "Implemented BankingApp with ReentrantLock, dead-lock prevention with ordered resource acquisition, and multi-threaded test cases.");
            pstmt.setString(4, "shivam_banking_solution.zip");
            pstmt.setDouble(5, 96.0);
            pstmt.setString(6, "Excellent implementation! Superb lock ordering and thread safety tests. Full marks on concurrency.");
            pstmt.setString(7, "GRADED");
            pstmt.setString(8, "2026-10-12 14:30:00");
            pstmt.executeUpdate();

            // Priya submitted CS201 Assignment 1 -> Graded
            pstmt.setInt(1, 1);
            pstmt.setInt(2, 6); // Priya
            pstmt.setString(3, "Completed thread pool execution and synchronized account withdraw and deposit methods.");
            pstmt.setString(4, "priya_bank_v1.zip");
            pstmt.setDouble(5, 88.0);
            pstmt.setString(6, "Good effort! Consider using AtomicDouble or explicit locks rather than coarse synchronized methods.");
            pstmt.setString(7, "GRADED");
            pstmt.setString(8, "2026-10-13 11:15:00");
            pstmt.executeUpdate();

            // Rahul submitted CS201 Assignment 1 -> Pending
            pstmt.setInt(1, 1);
            pstmt.setInt(2, 7); // Rahul
            pstmt.setString(3, "Attached banking concurrency code with producer-consumer transaction log queue.");
            pstmt.setString(4, "rahul_banking.java");
            pstmt.setNull(5, java.sql.Types.DOUBLE);
            pstmt.setString(6, "Pending instructor evaluation");
            pstmt.setString(7, "PENDING");
            pstmt.setNull(8, java.sql.Types.VARCHAR);
            pstmt.executeUpdate();
        }

        // Insert Enrollments
        String insertEnrollSql = "INSERT INTO enrollments (course_id, student_id, progress_percent, status, final_grade) VALUES (?, ?, ?, ?, ?);";
        try (PreparedStatement pstmt = conn.prepareStatement(insertEnrollSql)) {
            // Shivam enrolled in CS101, CS201, AI301
            pstmt.setInt(1, 2); pstmt.setInt(2, 5); pstmt.setInt(3, 85); pstmt.setString(4, "ACTIVE"); pstmt.setDouble(5, 96.0); pstmt.executeUpdate();
            pstmt.setInt(1, 1); pstmt.setInt(2, 5); pstmt.setInt(3, 70); pstmt.setString(4, "ACTIVE"); pstmt.setDouble(5, 90.0); pstmt.executeUpdate();
            pstmt.setInt(1, 3); pstmt.setInt(2, 5); pstmt.setInt(3, 40); pstmt.setString(4, "ACTIVE"); pstmt.setNull(5, java.sql.Types.DOUBLE); pstmt.executeUpdate();

            // Priya enrolled in CS201, CS101
            pstmt.setInt(1, 2); pstmt.setInt(2, 6); pstmt.setInt(3, 60); pstmt.setString(4, "ACTIVE"); pstmt.setDouble(5, 88.0); pstmt.executeUpdate();
            pstmt.setInt(1, 1); pstmt.setInt(2, 6); pstmt.setInt(3, 50); pstmt.setString(4, "ACTIVE"); pstmt.setNull(5, java.sql.Types.DOUBLE); pstmt.executeUpdate();

            // Rahul enrolled in CS201, CS401
            pstmt.setInt(1, 2); pstmt.setInt(2, 7); pstmt.setInt(3, 45); pstmt.setString(4, "ACTIVE"); pstmt.setNull(5, java.sql.Types.DOUBLE); pstmt.executeUpdate();
            pstmt.setInt(1, 4); pstmt.setInt(2, 7); pstmt.setInt(3, 20); pstmt.setString(4, "ACTIVE"); pstmt.setNull(5, java.sql.Types.DOUBLE); pstmt.executeUpdate();

            // Ananya enrolled in CS201, AI301
            pstmt.setInt(1, 2); pstmt.setInt(2, 8); pstmt.setInt(3, 90); pstmt.setString(4, "ACTIVE"); pstmt.setDouble(5, 94.0); pstmt.executeUpdate();
            pstmt.setInt(1, 3); pstmt.setInt(2, 8); pstmt.setInt(3, 35); pstmt.setString(4, "ACTIVE"); pstmt.setNull(5, java.sql.Types.DOUBLE); pstmt.executeUpdate();
        }

        // Insert System Settings
        String insertSettingSql = "INSERT INTO system_settings (setting_key, setting_value, description, category) VALUES (?, ?, ?, ?);";
        try (PreparedStatement pstmt = conn.prepareStatement(insertSettingSql)) {
            pstmt.setString(1, "system_name");
            pstmt.setString(2, "NextGen Enterprise LMS");
            pstmt.setString(3, "Display name for the Learning Management System");
            pstmt.setString(4, "GENERAL");
            pstmt.executeUpdate();

            pstmt.setString(1, "max_course_capacity_default");
            pstmt.setString(2, "50");
            pstmt.setString(3, "Default maximum seat capacity when creating courses");
            pstmt.setString(4, "ENROLLMENT");
            pstmt.executeUpdate();

            pstmt.setString(1, "allow_self_registration");
            pstmt.setString(2, "true");
            pstmt.setString(3, "Allow new students to register directly from login screen");
            pstmt.setString(4, "SECURITY");
            pstmt.executeUpdate();

            pstmt.setString(1, "maintenance_mode");
            pstmt.setString(2, "false");
            pstmt.setString(3, "Put system in read-only maintenance mode");
            pstmt.setString(4, "GENERAL");
            pstmt.executeUpdate();

            pstmt.setString(1, "notification_banner");
            pstmt.setString(2, "Mid-term examination schedule released. Check course boards!");
            pstmt.setString(3, "System-wide announcement shown on top of all dashboards");
            pstmt.setString(4, "NOTIFICATION");
            pstmt.executeUpdate();
        }

        // Insert Sample Audit Logs
        String insertLogSql = "INSERT INTO audit_logs (user_id, user_name, action, details) VALUES (?, ?, ?, ?);";
        try (PreparedStatement pstmt = conn.prepareStatement(insertLogSql)) {
            pstmt.setInt(1, 1); pstmt.setString(2, "Dr. Sarah Admin"); pstmt.setString(3, "SYSTEM_INIT"); pstmt.setString(4, "LMS database initialized and configured with baseline security."); pstmt.executeUpdate();
            pstmt.setInt(1, 3); pstmt.setString(2, "Prof. Ada Lovelace"); pstmt.setString(3, "COURSE_CREATED"); pstmt.setString(4, "Published new course CS201 Advanced Java & OOP."); pstmt.executeUpdate();
            pstmt.setInt(1, 5); pstmt.setString(2, "Shivam Kumar"); pstmt.setString(3, "ENROLLMENT"); pstmt.setString(4, "Enrolled in course CS201: Advanced Java."); pstmt.executeUpdate();
            pstmt.setInt(1, 5); pstmt.setString(2, "Shivam Kumar"); pstmt.setString(3, "ASSIGNMENT_SUBMISSION"); pstmt.setString(4, "Submitted solution for Mini-Project: Thread-Safe Banking Application."); pstmt.executeUpdate();
            pstmt.setInt(1, 3); pstmt.setString(2, "Prof. Ada Lovelace"); pstmt.setString(3, "ASSIGNMENT_GRADED"); pstmt.setString(4, "Graded submission for Shivam Kumar (Score: 96/100)."); pstmt.executeUpdate();
        }

        // Insert Quiz Questions
        String insertQuizSql = "INSERT INTO quiz_questions (course_id, question_text, option_a, option_b, option_c, option_d, correct_option, explanation) VALUES (?, ?, ?, ?, ?, ?, ?, ?);";
        try (PreparedStatement pstmt = conn.prepareStatement(insertQuizSql)) {
            pstmt.setInt(1, 2);
            pstmt.setString(2, "Which Java collection is synchronized and thread-safe for concurrent read-write access?");
            pstmt.setString(3, "ArrayList");
            pstmt.setString(4, "ConcurrentHashMap");
            pstmt.setString(5, "HashMap");
            pstmt.setString(6, "TreeMap");
            pstmt.setString(7, "B");
            pstmt.setString(8, "ConcurrentHashMap utilizes segmented lock stripping allowing high concurrency without blocking reads.");
            pstmt.executeUpdate();

            pstmt.setInt(1, 2);
            pstmt.setString(2, "Which OOP concept is demonstrated when a subclass provides a specific implementation of a parent method?");
            pstmt.setString(3, "Encapsulation");
            pstmt.setString(4, "Method Overloading");
            pstmt.setString(5, "Polymorphism (Method Overriding)");
            pstmt.setString(6, "Data Hiding");
            pstmt.setString(7, "C");
            pstmt.setString(8, "Dynamic method dispatch enables runtime polymorphism through method overriding.");
            pstmt.executeUpdate();
        }
    }
}
