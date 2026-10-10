# Online Learning Management System (LMS)

# NextGen-LMS
**NextGen Enterprise Educational Platform**  
*Java GUI Desktop Application (Swing + FlatLaf UI, SQLite JDBC, Multithreading & Concurrency, Generic DAOs, Robust OOP)*

---

## 🎯 Executive Summary & Objective

This project is a full-featured, enterprise-grade **Online Learning Management System (LMS)** designed to facilitate end-to-end academic operations with dedicated role dashboards for:
1. **Administrators**: Complete system oversight, user management, course governance, performance analytics, configuration settings, and live system audit monitoring.
2. **Instructors**: Course authoring, module & learning material publishing, assignment and quiz creation, student submission evaluation with feedback, and enrollment metrics.
3. **Students**: Course catalog discovery and 1-click concurrent enrollment, viewing and downloading lecture notes/materials, assignment submissions, academic progress tracking, and interactive quizzes.

---

## 📋 Evaluation Marking Rubric Compliance & Architectural Mapping

The implementation directly addresses and satisfies every category of the **Java GUI Based Projects Marking Rubric**:

| Rubric Criteria | Marks | Implementation Details & Source References |
|---|:---:|---|
| **OOP Implementation**<br>*(Polymorphism, Inheritance, Exception Handling, Interfaces)* | **10** | • **Inheritance**: Abstract base class [`User`](src/com/lms/model/User.java) extended by [`Admin`](src/com/lms/model/Admin.java), [`Instructor`](src/com/lms/model/Instructor.java), and [`Student`](src/com/lms/model/Student.java). Base [`GenericDAO`](src/com/lms/db/GenericDAO.java) extended by all entity DAOs.<br>• **Polymorphism**: Dynamic method dispatch on abstract methods `getDashboardTitle()`, `getPermissions()`, and polymorphic user instantiation in `UserDAO.mapResultSetToUser()`.<br>• **Interfaces**: [`Authenticatable`](src/com/lms/service/interfaces/Authenticatable.java), [`CourseManageable`](src/com/lms/service/interfaces/CourseManageable.java), [`Gradable`](src/com/lms/service/interfaces/Gradable.java), [`Reportable`](src/com/lms/service/interfaces/Reportable.java), [`SystemAuditable`](src/com/lms/service/interfaces/SystemAuditable.java), [`Repository<T, ID>`](src/com/lms/service/interfaces/Repository.java).<br>• **Exception Handling**: Custom exception hierarchy rooted at [`LMSException`](src/com/lms/exception/LMSException.java) with specialized subclasses (`AuthenticationException`, `UserNotFoundException`, `EnrollmentException`, `DatabaseOperationException`, `ValidationException`). |
| **Collections & Generics** | **6** | • **Generics**: Generic interface `Repository<T, ID>`, abstract generic class `GenericDAO<T, ID>`, generic higher-order algorithms in [`GenericCollectionUtil`](src/com/lms/util/GenericCollectionUtil.java) (`filter`, `map`, `paginate`, `groupBy`, `frequencyMap`), and generic report exporter [`ReportExporter`](src/com/lms/util/ReportExporter.java).<br>• **Collections**: Extensive usage of `List<Course>`, `Map<String, Double>`, `LinkedHashMap`, `ConcurrentHashMap`, `BlockingQueue`, `TreeSet`, and frequency maps for analytics. |
| **Multithreading & Synchronization** | **4** | • **Lock Synchronization**: [`ConcurrentEnrollmentManager`](src/com/lms/concurrent/ConcurrentEnrollmentManager.java) uses fine-grained `ReentrantLock` per course to prevent race conditions during simultaneous student enrollments with seat capacity checks.<br>• **Background Monitoring Daemon**: [`SystemActivityMonitor`](src/com/lms/concurrent/SystemActivityMonitor.java) runs a background daemon thread (`ScheduledExecutorService`) that continuously polls and dispatches audit events to the UI via `SwingUtilities.invokeLater()`.<br>• **Producer-Consumer Worker**: [`SubmissionProcessor`](src/com/lms/concurrent/SubmissionProcessor.java) uses a thread-safe `BlockingQueue` and worker thread pool for asynchronous background processing of student submissions and notifications.<br>• **Non-Blocking UI Worker**: [`AsyncDataWorker`](src/com/lms/concurrent/AsyncDataWorker.java) executes heavy queries without freezing the Event Dispatch Thread (EDT). |
| **Classes for Database Operations** | **7** | • Layered DAO architecture with dedicated classes:<br>  - [`UserDAO`](src/com/lms/db/UserDAO.java): User CRUD, authentication, role filtering.<br>  - [`CourseDAO`](src/com/lms/db/CourseDAO.java): Course CRUD, keyword search, capacity updates.<br>  - [`MaterialDAO`](src/com/lms/db/MaterialDAO.java): Course materials and attachments.<br>  - [`AssignmentDAO`](src/com/lms/db/AssignmentDAO.java): Assignment management.<br>  - [`SubmissionDAO`](src/com/lms/db/SubmissionDAO.java): Student submissions and grading updates.<br>  - [`EnrollmentDAO`](src/com/lms/db/EnrollmentDAO.java): Student enrollments and progress metrics.<br>  - [`AuditLogDAO`](src/com/lms/db/AuditLogDAO.java): System activity logging.<br>  - [`SystemSettingDAO`](src/com/lms/db/SystemSettingDAO.java): Configuration management.<br>  - [`QuizDAO`](src/com/lms/db/QuizDAO.java): Course assessments. |
| **Database Connectivity (JDBC)** | **3** | • Direct JDBC connectivity via `DriverManager.getConnection("jdbc:sqlite:lms.db")`.<br>• SQLite embedded database (`sqlite-jdbc`): zero-configuration, zero external server requirements, portable across any computer. |
| **Implement JDBC for Connectivity** | **3** | • Parameterized `PreparedStatement` preventing SQL injection across all SQL queries.<br>• `ResultSet` metadata processing, `Statement.RETURN_GENERATED_KEYS`.<br>• Transaction management with `conn.setAutoCommit(false)`, `conn.commit()`, and `conn.rollback()` in `GenericDAO.executeTransaction()`. |

---

## 👥 User Roles & Dedicated Dashboards

### 🛡️ 1. Administrator Dashboard
- **User Management**: Table listing accounts with role filters and search. Full Add, Edit, and Delete options with confirmation dialogs.
- **Course Management**: System-wide course governance. Add new courses, assign instructors, configure seat capacities, or delete courses with confirmation messages.
- **Performance Analytics**: Visual charts (Capacity Utilization Bar Chart & Course Share Donut Chart) with CSV & formatted Text export functionality.
- **System Settings**: Real-time configuration panel for system name, seat defaults, self-registration toggle, maintenance mode, and global institutional announcement banners.
- **System Activity Monitoring**: Live audit feed populated by the background daemon thread in real time.

### 👨‍🏫 2. Instructor Dashboard
- **Course Management & Creation**: Create courses with descriptions, syllabi, max capacity, upload learning materials (PDFs, video notes, code labs), assignments, and quiz questions with confirmation alerts.
- **Assignment Grading**: View submitted student assignments. Grade submissions by inputting marks and detailed feedback. Triggers asynchronous background notification tasks.
- **Student Performance**: Monitor student progress percentages and final grades across instructor courses with CSV scorecard export.
- **Course Enrollment Stats**: Visual charts displaying student enrollments and seat fill rates per course.
- **Feedback Summary**: Inspect the history of all grades and feedback provided to students.

### 🎓 3. Student Dashboard
- **Course Catalog & Enrollment**: Browse catalog with keyword search and category filters. 1-click thread-safe enrollment with seat checks and confirmation message.
- **Material Access & Download**: View course materials, read lecture summaries, and download material files directly to local disk.
- **Assignment Submissions**: View assignment requirements and rubrics. Submit solutions and file attachments with instant status tracking (Not Submitted, Pending, Graded).
- **Progress Tracking**: Visual progress bars and completion percentages. Interactive module completion simulator that updates database progress.
- **Feedback, Grades & Quiz**: View scores, percentages, and instructor feedback remarks. Interactive **Take Practice Quiz** assessment with instant grading and explanations.

---

## 🔑 Quick Demo Credentials (1-Click Login Available)

The sign-in screen features a **"Quick Demo Access"** tab that lets you launch any dashboard immediately with 1 click:

Open the web portal at `http://localhost:8080/`. Use these IDs and passwords in **Sign In to Dashboard**:

| Role | Name | Login ID (Email) | Password |
|---|---|---|---|
| **Admin** | Dr. Sarah Admin | `admin@lms.edu` | `admin123` |
| **Instructor** | Prof. Alan Turing | `alan@lms.edu` | `instructor123` |
| **Instructor** | Prof. Ada Lovelace | `ada@lms.edu` | `instructor123` |
| **Student** | Shivam Kumar | `shivam@lms.edu` | `student123` |
| **Student** | Priya Sharma | `priya@lms.edu` | `student123` |

*(New student accounts can also be registered directly using the **"Student Registration"** tab on the login screen).*

---

## 🚀 How to Run the Project

### Option A: Using Batch Files (Recommended on Windows)
1. **Compile the project**:
   Double-click `build.bat` (or run `./build.bat` in command prompt).
2. **Launch the application**:
   Double-click `run.bat` (or run `./run.bat` in command prompt).
3. **Run the Rubric Verification Suite**:
   Double-click `run_tests.bat` to run the automated unit and concurrency tests.

### Option B: Using Command Line
```powershell
# 1. Compile
javac -cp "lib/*" -d bin src\com\lms\*.java src\com\lms\model\*.java src\com\lms\db\*.java src\com\lms\service\interfaces\*.java src\com\lms\exception\*.java src\com\lms\concurrent\*.java src\com\lms\util\*.java src\com\lms\ui\*.java src\com\lms\ui\common\*.java src\com\lms\ui\admin\*.java src\com\lms\ui\instructor\*.java src\com\lms\ui\student\*.java src\com\lms\test\*.java

# 2. Run Main LMS Desktop Application
java -cp "bin;lib/*" com.lms.Main

# 3. Run Automated Rubric Verification Tests
java -cp "bin;lib/*" com.lms.test.LMSVerificationTest
```

---

## 🧪 Verification Test Suite Results

Running `run_tests.bat` executes `LMSVerificationTest`, verifying:
- ✅ **Test 1**: SQLite JDBC connection active and operational.
- ✅ **Test 2**: Database schema initialized and seed records verified (8 users, 4 courses, 9 enrollments).
- ✅ **Test 3**: OOP inheritance & polymorphic dispatch on `User` subclasses validated.
- ✅ **Test 4**: Generic collections pipeline (`filter`, `map`, `frequencyMap`) validated.
- ✅ **Test 5**: Multithreading & ReentrantLock concurrency test (5 simultaneous threads competing for 2 seats: exactly 2 seats granted, 3 rejected without race condition).
- ✅ **Test 6**: Background worker queue (`SubmissionProcessor`) asynchronous execution verified.
- **Result: 6 / 6 Tests Passed (100% Score)**.
#   N e x t G e n - L M S  
 #   N e x t G e n - L M S  
 
