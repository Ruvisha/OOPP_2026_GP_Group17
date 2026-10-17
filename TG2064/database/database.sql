-- ============================================================================
-- FACULTY OF TECHNOLOGY ACADEMIC MANAGEMENT SYSTEM
-- ICT2132 - Object Oriented Programming Practicum
-- University of Ruhuna - Group 17
-- Database Initialization & Sample Data Script
-- ============================================================================

DROP DATABASE IF EXISTS faculty_academic_management;
CREATE DATABASE faculty_academic_management
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE faculty_academic_management;

-- ----------------------------------------------------------------------------
-- Table: roles
-- ----------------------------------------------------------------------------
CREATE TABLE roles (
    role_id INT AUTO_INCREMENT PRIMARY KEY,
    role_name VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255)
) ENGINE=InnoDB;

-- ----------------------------------------------------------------------------
-- Table: departments
-- ----------------------------------------------------------------------------
CREATE TABLE departments (
    department_id INT AUTO_INCREMENT PRIMARY KEY,
    department_code VARCHAR(10) NOT NULL UNIQUE,
    department_name VARCHAR(150) NOT NULL,
    description TEXT
) ENGINE=InnoDB;

-- ----------------------------------------------------------------------------
-- Table: users
-- ----------------------------------------------------------------------------
CREATE TABLE users (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role_id INT NOT NULL,
    full_name VARCHAR(150) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    phone VARCHAR(20),
    address VARCHAR(255),
    profile_pic VARCHAR(255) DEFAULT 'default_avatar.png',
    department_id INT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    status ENUM('ACTIVE', 'INACTIVE') DEFAULT 'ACTIVE',
    
    -- Role specific attributes stored cleanly
    student_reg_no VARCHAR(30) UNIQUE,
    academic_year VARCHAR(20),
    current_semester INT DEFAULT 1,
    student_type ENUM('REGULAR', 'REPEAT', 'BATCH_MISSED') DEFAULT 'REGULAR',
    designation VARCHAR(100),
    office_room VARCHAR(50),
    assigned_lab VARCHAR(100),
    
    CONSTRAINT fk_users_role FOREIGN KEY (role_id) 
        REFERENCES roles(role_id) ON DELETE RESTRICT,
    CONSTRAINT fk_users_department FOREIGN KEY (department_id) 
        REFERENCES departments(department_id) ON DELETE SET NULL,
    INDEX idx_user_role (role_id),
    INDEX idx_user_reg_no (student_reg_no)
) ENGINE=InnoDB;

-- ----------------------------------------------------------------------------
-- Table: courses
-- ----------------------------------------------------------------------------
CREATE TABLE courses (
    course_id INT AUTO_INCREMENT PRIMARY KEY,
    course_code VARCHAR(20) NOT NULL UNIQUE,
    course_name VARCHAR(150) NOT NULL,
    credits INT NOT NULL DEFAULT 2,
    semester INT NOT NULL DEFAULT 1,
    department_id INT NOT NULL,
    has_theory BOOLEAN NOT NULL DEFAULT TRUE,
    has_practical BOOLEAN NOT NULL DEFAULT FALSE,
    description TEXT,
    CONSTRAINT fk_courses_department FOREIGN KEY (department_id) 
        REFERENCES departments(department_id) ON DELETE RESTRICT,
    INDEX idx_course_dept (department_id)
) ENGINE=InnoDB;

-- ----------------------------------------------------------------------------
-- Table: course_materials
-- ----------------------------------------------------------------------------
CREATE TABLE course_materials (
    material_id INT AUTO_INCREMENT PRIMARY KEY,
    course_id INT NOT NULL,
    lecturer_id INT NOT NULL,
    title VARCHAR(150) NOT NULL,
    description TEXT,
    file_path VARCHAR(255) NOT NULL,
    uploaded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_materials_course FOREIGN KEY (course_id) 
        REFERENCES courses(course_id) ON DELETE CASCADE,
    CONSTRAINT fk_materials_lecturer FOREIGN KEY (lecturer_id) 
        REFERENCES users(user_id) ON DELETE RESTRICT,
    INDEX idx_materials_course (course_id)
) ENGINE=InnoDB;

-- ----------------------------------------------------------------------------
-- Table: course_enrollment
-- ----------------------------------------------------------------------------
CREATE TABLE course_enrollment (
    enrollment_id INT AUTO_INCREMENT PRIMARY KEY,
    student_id INT NOT NULL,
    course_id INT NOT NULL,
    academic_year VARCHAR(20) NOT NULL,
    semester INT NOT NULL DEFAULT 1,
    is_repeat BOOLEAN NOT NULL DEFAULT FALSE,
    enrolled_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_student_course_year UNIQUE (student_id, course_id, academic_year),
    CONSTRAINT fk_enroll_student FOREIGN KEY (student_id) 
        REFERENCES users(user_id) ON DELETE CASCADE,
    CONSTRAINT fk_enroll_course FOREIGN KEY (course_id) 
        REFERENCES courses(course_id) ON DELETE CASCADE,
    INDEX idx_enroll_student (student_id),
    INDEX idx_enroll_course (course_id)
) ENGINE=InnoDB;

-- ----------------------------------------------------------------------------
-- Table: attendance
-- ----------------------------------------------------------------------------
CREATE TABLE attendance (
    attendance_id INT AUTO_INCREMENT PRIMARY KEY,
    student_id INT NOT NULL,
    course_id INT NOT NULL,
    session_date DATE NOT NULL,
    session_type ENUM('THEORY', 'PRACTICAL') NOT NULL,
    session_number INT NOT NULL,
    hours INT NOT NULL DEFAULT 2,
    status ENUM('PRESENT', 'ABSENT') NOT NULL,
    recorded_by INT,
    recorded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_student_course_session UNIQUE (student_id, course_id, session_type, session_number),
    CONSTRAINT fk_att_student FOREIGN KEY (student_id) 
        REFERENCES users(user_id) ON DELETE CASCADE,
    CONSTRAINT fk_att_course FOREIGN KEY (course_id) 
        REFERENCES courses(course_id) ON DELETE CASCADE,
    CONSTRAINT fk_att_recorder FOREIGN KEY (recorded_by) 
        REFERENCES users(user_id) ON DELETE SET NULL,
    INDEX idx_att_student (student_id),
    INDEX idx_att_course (course_id)
) ENGINE=InnoDB;

-- ----------------------------------------------------------------------------
-- Table: medical
-- ----------------------------------------------------------------------------
CREATE TABLE medical (
    medical_id INT AUTO_INCREMENT PRIMARY KEY,
    student_id INT NOT NULL,
    course_id INT NOT NULL,
    session_type ENUM('THEORY', 'PRACTICAL', 'BOTH') NOT NULL DEFAULT 'THEORY',
    session_date DATE NOT NULL,
    submission_date DATE NOT NULL,
    reason TEXT NOT NULL,
    medical_doc_path VARCHAR(255),
    status ENUM('APPROVED', 'NOT_APPROVED') NOT NULL DEFAULT 'NOT_APPROVED',
    reviewed_by INT,
    reviewed_at TIMESTAMP NULL,
    remarks VARCHAR(255),
    CONSTRAINT fk_med_student FOREIGN KEY (student_id) 
        REFERENCES users(user_id) ON DELETE CASCADE,
    CONSTRAINT fk_med_course FOREIGN KEY (course_id) 
        REFERENCES courses(course_id) ON DELETE CASCADE,
    CONSTRAINT fk_med_reviewer FOREIGN KEY (reviewed_by) 
        REFERENCES users(user_id) ON DELETE SET NULL,
    INDEX idx_med_student (student_id),
    INDEX idx_med_course (course_id)
) ENGINE=InnoDB;

-- ----------------------------------------------------------------------------
-- Table: evaluations
-- ----------------------------------------------------------------------------
CREATE TABLE evaluations (
    evaluation_id INT AUTO_INCREMENT PRIMARY KEY,
    course_id INT NOT NULL,
    evaluation_name VARCHAR(100) NOT NULL,
    evaluation_type ENUM('QUIZ', 'ASSIGNMENT', 'MID_EXAM', 'FINAL_THEORY', 'FINAL_PRACTICAL', 'CA_AGGREGATE') NOT NULL,
    max_marks DOUBLE NOT NULL DEFAULT 100.0,
    weightage_percent DOUBLE NOT NULL,
    semester INT NOT NULL DEFAULT 1,
    academic_year VARCHAR(20) NOT NULL,
    CONSTRAINT fk_eval_course FOREIGN KEY (course_id) 
        REFERENCES courses(course_id) ON DELETE CASCADE,
    INDEX idx_eval_course (course_id)
) ENGINE=InnoDB;

-- ----------------------------------------------------------------------------
-- Table: marks
-- ----------------------------------------------------------------------------
CREATE TABLE marks (
    mark_id INT AUTO_INCREMENT PRIMARY KEY,
    evaluation_id INT NOT NULL,
    student_id INT NOT NULL,
    marks_obtained DOUBLE NOT NULL,
    entered_by INT,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT uq_student_eval UNIQUE (evaluation_id, student_id),
    CONSTRAINT fk_marks_eval FOREIGN KEY (evaluation_id) 
        REFERENCES evaluations(evaluation_id) ON DELETE CASCADE,
    CONSTRAINT fk_marks_student FOREIGN KEY (student_id) 
        REFERENCES users(user_id) ON DELETE CASCADE,
    CONSTRAINT fk_marks_enterer FOREIGN KEY (entered_by) 
        REFERENCES users(user_id) ON DELETE SET NULL,
    INDEX idx_marks_student (student_id)
) ENGINE=InnoDB;

-- ----------------------------------------------------------------------------
-- Table: grades
-- ----------------------------------------------------------------------------
CREATE TABLE grades (
    grade_id INT AUTO_INCREMENT PRIMARY KEY,
    student_id INT NOT NULL,
    course_id INT NOT NULL,
    academic_year VARCHAR(20) NOT NULL,
    ca_mark DOUBLE NOT NULL DEFAULT 0.0,
    final_exam_mark DOUBLE NOT NULL DEFAULT 0.0,
    total_mark DOUBLE NOT NULL DEFAULT 0.0,
    grade_letter VARCHAR(5) NOT NULL,
    grade_point DOUBLE NOT NULL,
    is_repeat BOOLEAN NOT NULL DEFAULT FALSE,
    calculated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT uq_student_course_grade UNIQUE (student_id, course_id, academic_year),
    CONSTRAINT fk_grades_student FOREIGN KEY (student_id) 
        REFERENCES users(user_id) ON DELETE CASCADE,
    CONSTRAINT fk_grades_course FOREIGN KEY (course_id) 
        REFERENCES courses(course_id) ON DELETE CASCADE,
    INDEX idx_grades_student (student_id)
) ENGINE=InnoDB;

-- ----------------------------------------------------------------------------
-- Table: notices
-- ----------------------------------------------------------------------------
CREATE TABLE notices (
    notice_id INT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    content TEXT NOT NULL,
    published_date DATE NOT NULL,
    created_by INT,
    target_role VARCHAR(50) DEFAULT 'ALL',
    status ENUM('ACTIVE', 'ARCHIVED') DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_notices_creator FOREIGN KEY (created_by) 
        REFERENCES users(user_id) ON DELETE SET NULL
) ENGINE=InnoDB;

-- ----------------------------------------------------------------------------
-- Table: timetables
-- ----------------------------------------------------------------------------
CREATE TABLE timetables (
    timetable_id INT AUTO_INCREMENT PRIMARY KEY,
    department_id INT NOT NULL,
    course_id INT NOT NULL,
    day_of_week ENUM('MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY', 'SUNDAY') NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    room VARCHAR(50) NOT NULL,
    lecturer_id INT,
    session_type ENUM('THEORY', 'PRACTICAL') NOT NULL DEFAULT 'THEORY',
    semester INT NOT NULL DEFAULT 1,
    academic_year VARCHAR(20) NOT NULL DEFAULT '2025/2026',
    CONSTRAINT fk_tt_department FOREIGN KEY (department_id) 
        REFERENCES departments(department_id) ON DELETE CASCADE,
    CONSTRAINT fk_tt_course FOREIGN KEY (course_id) 
        REFERENCES courses(course_id) ON DELETE CASCADE,
    CONSTRAINT fk_tt_lecturer FOREIGN KEY (lecturer_id) 
        REFERENCES users(user_id) ON DELETE SET NULL,
    INDEX idx_tt_dept (department_id),
    INDEX idx_tt_course (course_id)
) ENGINE=InnoDB;

-- ============================================================================
-- SEED DATA INSERTION
-- ============================================================================

-- 1. Roles
INSERT INTO roles (role_id, role_name, description) VALUES
(1, 'Admin', 'System Administrator with full access to manage users, courses, notices, and timetables'),
(2, 'Lecturer', 'Academic staff who upload marks, manage course materials, and view student progress'),
(3, 'Technical Officer', 'Technical staff maintaining attendance, medical records, and lab timetables'),
(4, 'Undergraduate', 'Student who views personal attendance, marks, grades, GPA, notices, and timetables');

-- 2. Departments
INSERT INTO departments (department_id, department_code, department_name, description) VALUES
(1, 'ICT', 'Department of Information and Communication Technology', 'Focuses on software engineering, computer networks, and data systems'),
(2, 'BST', 'Department of Bio Systems Technology', 'Focuses on agricultural systems, bio-processing, and environmental tech'),
(3, 'ET', 'Department of Engineering Technology', 'Focuses on mechanical, electrical, and automation technologies'),
(4, 'MDT', 'Department of Multidisciplinary Studies', 'Provides mathematics, management, and language courses');

-- 3. Users
-- Passwords:
-- For seamless experience, we support SHA-256 hash or plain password comparison in PasswordUtil.
-- Default password for all seed accounts:
-- admin -> admin123 (SHA-256: 240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9)
-- lecturer -> lec123 (SHA-256: 7d6c6bc5905d43ec0492cb28d116315264b971a804369a3b8364e6b12f694297)
-- technical officer -> to123 (SHA-256: eca207865cbb46b28489cf3066a50be7520b22a0ec5f97305dcfd229f37c49bb)
-- undergraduate -> student123 (SHA-256: cd73502828457d15655bbd7a63fb0bc8f2bef4f3343646419e474d69a500073e)

-- 3.1 Admin (1 Admin)
INSERT INTO users (user_id, username, password_hash, role_id, full_name, email, phone, address, department_id, designation, status) VALUES
(1, 'admin', '240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9', 1, 'System Administrator', 'admin@fot.ruh.ac.lk', '0412223344', 'Faculty of Technology, University of Ruhuna, Karagoda Uyangoda', 1, 'Senior Assistant Registrar', 'ACTIVE');

-- 3.2 Lecturers (At least 5 Lecturers)
INSERT INTO users (user_id, username, password_hash, role_id, full_name, email, phone, address, department_id, designation, office_room, status) VALUES
(2, 'lec_kamal', '7d6c6bc5905d43ec0492cb28d116315264b971a804369a3b8364e6b12f694297', 2, 'Dr. Kamal Perera', 'kamal@ict.ruh.ac.lk', '0771234501', 'No. 12, Galle Road, Matara', 1, 'Senior Lecturer Gr. I', 'FOT-ICT-301', 'ACTIVE'),
(3, 'lec_nimal', '7d6c6bc5905d43ec0492cb28d116315264b971a804369a3b8364e6b12f694297', 2, 'Prof. Nimal Silva', 'nimal@ict.ruh.ac.lk', '0771234502', 'No. 45, Beach Road, Mirissa', 1, 'Professor in Computing', 'FOT-ICT-302', 'ACTIVE'),
(4, 'lec_sunil', '7d6c6bc5905d43ec0492cb28d116315264b971a804369a3b8364e6b12f694297', 2, 'Dr. Sunil Fernando', 'sunil@bst.ruh.ac.lk', '0771234503', 'No. 88, Kandy Road, Kamburupitiya', 2, 'Senior Lecturer Gr. II', 'FOT-BST-201', 'ACTIVE'),
(5, 'lec_priyantha', '7d6c6bc5905d43ec0492cb28d116315264b971a804369a3b8364e6b12f694297', 2, 'Dr. Priyantha Jayasuriya', 'priyantha@et.ruh.ac.lk', '0771234504', 'No. 15, Station Road, Matara', 3, 'Head of Department', 'FOT-ET-101', 'ACTIVE'),
(6, 'lec_anoma', '7d6c6bc5905d43ec0492cb28d116315264b971a804369a3b8364e6b12f694297', 2, 'Ms. Anoma Wickramasinghe', 'anoma@mdt.ruh.ac.lk', '0771234505', 'No. 05, Temple Road, Matara', 4, 'Lecturer (Unconfirmed)', 'FOT-MDT-105', 'ACTIVE');

-- 3.3 Technical Officers (At least 4 Technical Officers)
INSERT INTO users (user_id, username, password_hash, role_id, full_name, email, phone, address, department_id, designation, assigned_lab, status) VALUES
(7, 'to_saman', 'eca207865cbb46b28489cf3066a50be7520b22a0ec5f97305dcfd229f37c49bb', 3, 'Mr. Saman Kumara', 'saman.to@fot.ruh.ac.lk', '0714567801', 'No. 22, Hakmana Road, Matara', 1, 'Technical Officer Gr. I', 'Computer Lab 01', 'ACTIVE'),
(8, 'to_ruwan', 'eca207865cbb46b28489cf3066a50be7520b22a0ec5f97305dcfd229f37c49bb', 3, 'Mr. Ruwan Bandara', 'ruwan.to@fot.ruh.ac.lk', '0714567802', 'No. 34, Akuressa Road, Kamburupitiya', 2, 'Technical Officer Gr. II', 'Biotechnology Lab', 'ACTIVE'),
(9, 'to_chaminda', 'eca207865cbb46b28489cf3066a50be7520b22a0ec5f97305dcfd229f37c49bb', 3, 'Mr. Chaminda Dissanayake', 'chaminda.to@fot.ruh.ac.lk', '0714567803', 'No. 56, Rahula Road, Matara', 3, 'Technical Officer Gr. I', 'Engineering Workshop', 'ACTIVE'),
(10, 'to_kasun', 'eca207865cbb46b28489cf3066a50be7520b22a0ec5f97305dcfd229f37c49bb', 3, 'Mr. Kasun Senanayake', 'kasun.to@fot.ruh.ac.lk', '0714567804', 'No. 78, Tangalle Road, Matara', 1, 'Technical Officer Gr. II', 'Network Systems Lab', 'ACTIVE');

-- 3.4 Undergraduates (22 Undergraduates: Regular, Repeat, Batch-Missed)
INSERT INTO users (user_id, username, password_hash, role_id, full_name, email, phone, address, department_id, student_reg_no, academic_year, current_semester, student_type, status) VALUES
-- Regular Students (TG/2024/2001 - TG/2024/2018)
(11, 'tg20242001', 'cd73502828457d15655bbd7a63fb0bc8f2bef4f3343646419e474d69a500073e', 4, 'M.D.R.L. Nonis', 'nonis@fot.ruh.ac.lk', '0761112001', 'Faculty Hostel, Karagoda Uyangoda', 1, 'TG/2024/2001', '2024/2025', 1, 'REGULAR', 'ACTIVE'),
(12, 'tg20242002', 'cd73502828457d15655bbd7a63fb0bc8f2bef4f3343646419e474d69a500073e', 4, 'Anuda Pasandul', 'anuda@fot.ruh.ac.lk', '0761112002', 'No. 18, Walasmulla Road, Beliatta', 1, 'TG/2024/2002', '2024/2025', 1, 'REGULAR', 'ACTIVE'),
(13, 'tg20242003', 'cd73502828457d15655bbd7a63fb0bc8f2bef4f3343646419e474d69a500073e', 4, 'Sahan Rashmika', 'sahan@fot.ruh.ac.lk', '0761112003', 'No. 29, Broadway, Matara', 1, 'TG/2024/2003', '2024/2025', 1, 'REGULAR', 'ACTIVE'),
(14, 'tg20242004', 'cd73502828457d15655bbd7a63fb0bc8f2bef4f3343646419e474d69a500073e', 4, 'H.R.H. Nethsara', 'nethsara@fot.ruh.ac.lk', '0761112004', 'No. 40, High Level Road, Colombo', 1, 'TG/2024/2004', '2024/2025', 1, 'REGULAR', 'ACTIVE'),
(15, 'tg20242005', 'cd73502828457d15655bbd7a63fb0bc8f2bef4f3343646419e474d69a500073e', 4, 'Kaveesha Gimhan', 'kaveesha@fot.ruh.ac.lk', '0761112005', 'No. 12, Lake Round, Kandy', 1, 'TG/2024/2005', '2024/2025', 1, 'REGULAR', 'ACTIVE'),
(16, 'tg20242006', 'cd73502828457d15655bbd7a63fb0bc8f2bef4f3343646419e474d69a500073e', 4, 'Dilshan Madushanka', 'dilshan@fot.ruh.ac.lk', '0761112006', 'No. 55, Beach Road, Tangalle', 1, 'TG/2024/2006', '2024/2025', 1, 'REGULAR', 'ACTIVE'),
(17, 'tg20242007', 'cd73502828457d15655bbd7a63fb0bc8f2bef4f3343646419e474d69a500073e', 4, 'Tharindu Malshan', 'tharindu@fot.ruh.ac.lk', '0761112007', 'No. 89, Main Street, Galle', 1, 'TG/2024/2007', '2024/2025', 1, 'REGULAR', 'ACTIVE'),
(18, 'tg20242008', 'cd73502828457d15655bbd7a63fb0bc8f2bef4f3343646419e474d69a500073e', 4, 'Sanduni Tharaka', 'sanduni@fot.ruh.ac.lk', '0761112008', 'Girls Hostel 02, Karagoda Uyangoda', 1, 'TG/2024/2008', '2024/2025', 1, 'REGULAR', 'ACTIVE'),
(19, 'tg20242009', 'cd73502828457d15655bbd7a63fb0bc8f2bef4f3343646419e474d69a500073e', 4, 'Ishara Jayamini', 'ishara@fot.ruh.ac.lk', '0761112009', 'No. 31, Cross Street, Matara', 1, 'TG/2024/2009', '2024/2025', 1, 'REGULAR', 'ACTIVE'),
(20, 'tg20242010', 'cd73502828457d15655bbd7a63fb0bc8f2bef4f3343646419e474d69a500073e', 4, 'Bhanuka Rajapaksha', 'bhanuka@fot.ruh.ac.lk', '0761112010', 'No. 77, Fort, Galle', 1, 'TG/2024/2010', '2024/2025', 1, 'REGULAR', 'ACTIVE'),
(21, 'tg20242011', 'cd73502828457d15655bbd7a63fb0bc8f2bef4f3343646419e474d69a500073e', 4, 'Oshada Kavinda', 'oshada@fot.ruh.ac.lk', '0761112011', 'No. 64, Samagi Mawatha, Matara', 1, 'TG/2024/2011', '2024/2025', 1, 'REGULAR', 'ACTIVE'),
(22, 'tg20242012', 'cd73502828457d15655bbd7a63fb0bc8f2bef4f3343646419e474d69a500073e', 4, 'Chathura Prabath', 'chathura@fot.ruh.ac.lk', '0761112012', 'No. 15, Hospital Road, Kamburupitiya', 1, 'TG/2024/2012', '2024/2025', 1, 'REGULAR', 'ACTIVE'),
(23, 'tg20242013', 'cd73502828457d15655bbd7a63fb0bc8f2bef4f3343646419e474d69a500073e', 4, 'Nimesha Sewwandi', 'nimesha@fot.ruh.ac.lk', '0761112013', 'Girls Hostel 01, Karagoda Uyangoda', 1, 'TG/2024/2013', '2024/2025', 1, 'REGULAR', 'ACTIVE'),
(24, 'tg20242014', 'cd73502828457d15655bbd7a63fb0bc8f2bef4f3343646419e474d69a500073e', 4, 'Janith Pramod', 'janith@fot.ruh.ac.lk', '0761112014', 'No. 09, Station Road, Weligama', 1, 'TG/2024/2014', '2024/2025', 1, 'REGULAR', 'ACTIVE'),
(25, 'tg20242015', 'cd73502828457d15655bbd7a63fb0bc8f2bef4f3343646419e474d69a500073e', 4, 'Sachini Kaushalya', 'sachini@fot.ruh.ac.lk', '0761112015', 'No. 44, Hill Street, Matara', 1, 'TG/2024/2015', '2024/2025', 1, 'REGULAR', 'ACTIVE'),
(26, 'tg20242016', 'cd73502828457d15655bbd7a63fb0bc8f2bef4f3343646419e474d69a500073e', 4, 'Amila Nuwan', 'amila@fot.ruh.ac.lk', '0761112016', 'No. 102, Colombo Road, Bentota', 1, 'TG/2024/2016', '2024/2025', 1, 'REGULAR', 'ACTIVE'),
(27, 'tg20242017', 'cd73502828457d15655bbd7a63fb0bc8f2bef4f3343646419e474d69a500073e', 4, 'Dinuka Lakshan', 'dinuka.l@fot.ruh.ac.lk', '0761112017', 'No. 38, Galle Road, Ambalangoda', 1, 'TG/2024/2017', '2024/2025', 1, 'REGULAR', 'ACTIVE'),
(28, 'tg20242018', 'cd73502828457d15655bbd7a63fb0bc8f2bef4f3343646419e474d69a500073e', 4, 'Pawani Hansika', 'pawani@fot.ruh.ac.lk', '0761112018', 'Girls Hostel 02, Karagoda Uyangoda', 1, 'TG/2024/2018', '2024/2025', 1, 'REGULAR', 'ACTIVE'),

-- Repeat Students (TG/2023/1850, TG/2023/1855)
(29, 'tg20231850', 'cd73502828457d15655bbd7a63fb0bc8f2bef4f3343646419e474d69a500073e', 4, 'P.L. Kasun Wijesinghe', 'kasun.w@fot.ruh.ac.lk', '0761111850', 'No. 51, Hakmana Road, Matara', 1, 'TG/2023/1850', '2024/2025', 1, 'REPEAT', 'ACTIVE'),
(30, 'tg20231855', 'cd73502828457d15655bbd7a63fb0bc8f2bef4f3343646419e474d69a500073e', 4, 'K.M. Sachith Alwis', 'sachith.a@fot.ruh.ac.lk', '0761111855', 'No. 19, Beach Road, Dickwella', 1, 'TG/2023/1855', '2024/2025', 1, 'REPEAT', 'ACTIVE'),

-- Batch-Missed Students (TG/2022/1510, TG/2022/1520)
(31, 'tg20221510', 'cd73502828457d15655bbd7a63fb0bc8f2bef4f3343646419e474d69a500073e', 4, 'R.M. Dinuka Bandara', 'dinuka.b@fot.ruh.ac.lk', '0761111510', 'No. 73, Kandy Road, Kurunegala', 1, 'TG/2022/1510', '2024/2025', 1, 'BATCH_MISSED', 'ACTIVE'),
(32, 'tg20221520', 'cd73502828457d15655bbd7a63fb0bc8f2bef4f3343646419e474d69a500073e', 4, 'W.T. Nuwan Jayalath', 'nuwan.j@fot.ruh.ac.lk', '0761111520', 'No. 82, Badulla Road, Bandarawela', 1, 'TG/2022/1520', '2024/2025', 1, 'BATCH_MISSED', 'ACTIVE');

-- 4. Courses
INSERT INTO courses (course_id, course_code, course_name, credits, semester, department_id, has_theory, has_practical, description) VALUES
(1, 'ICT2113', 'Database Systems', 3, 1, 1, TRUE, TRUE, 'Relational model, SQL, normalization, transactions and indexing'),
(2, 'ICT2122', 'Software Engineering', 2, 1, 1, TRUE, FALSE, 'Software lifecycle, agile methodologies, requirements engineering and UML'),
(3, 'ICT2132', 'Object Oriented Programming Practicum', 2, 1, 1, TRUE, TRUE, 'Advanced OOP, Java Swing, JDBC, design patterns, and desktop application development'),
(4, 'ICT2143', 'Computer Networks', 3, 1, 1, TRUE, TRUE, 'OSI model, TCP/IP, IP addressing, subnetting and routing protocols'),
(5, 'ENG2112', 'Professional English II', 2, 1, 4, TRUE, FALSE, 'Academic writing, technical presentations and technical reports'),
(6, 'ET2101', 'Applied Electricity', 2, 1, 3, TRUE, TRUE, 'Circuit analysis, AC circuits, transformers and electric machines'),
(7, 'BST2102', 'Bio-Statistics', 2, 1, 2, TRUE, FALSE, 'Probability distributions, statistical hypothesis testing and regression analysis');

-- 5. Course Materials
INSERT INTO course_materials (material_id, course_id, lecturer_id, title, description, file_path, uploaded_at) VALUES
(1, 3, 2, 'Lecture 01 - Introduction to OOP Concepts', 'Fundamental principles of OOP: Abstraction, Encapsulation, Inheritance, Polymorphism in Java', 'materials/ict2132_lec01_oop_basics.pdf', '2026-09-01 09:00:00'),
(2, 3, 2, 'Lecture 02 - Java Swing GUI Development', 'Building user interfaces using JFrame, JPanel, LayoutManagers and Listeners', 'materials/ict2132_lec02_swing_gui.pdf', '2026-09-08 10:30:00'),
(3, 3, 2, 'Lecture 03 - JDBC Architecture & DAO Pattern', 'Connecting Java applications to MySQL database with PreparedStatement and DAO layer', 'materials/ict2132_lec03_jdbc_dao.pdf', '2026-09-15 11:00:00'),
(4, 3, 2, 'Lab Sheet 01 - Swing Forms & Event Handling', 'Hands-on practice exercises for building responsive desktop forms', 'materials/ict2132_lab01_forms.pdf', '2026-09-16 14:00:00'),
(5, 1, 3, 'Lecture 01 - Relational Database Concepts & SQL', 'Overview of DBMS, relational algebra, DDL and DML queries', 'materials/ict2113_lec01_relational_sql.pdf', '2026-09-02 08:30:00'),
(6, 1, 3, 'Lecture 02 - Normalization (1NF, 2NF, 3NF, BCNF)', 'Database design rules to prevent insertion, update and deletion anomalies', 'materials/ict2113_lec02_normalization.pdf', '2026-09-09 09:30:00'),
(7, 2, 2, 'Lecture 01 - Software Requirements Specification', 'Writing SRS documents, functional & non-functional requirements IEEE 830', 'materials/ict2122_lec01_srs_standards.pdf', '2026-09-03 13:00:00');

-- 6. Course Enrollment
-- Enroll all regular students in semester 1 ICT courses (ICT2113, ICT2122, ICT2132, ICT2143, ENG2112)
INSERT INTO course_enrollment (student_id, course_id, academic_year, semester, is_repeat)
SELECT u.user_id, c.course_id, '2024/2025', 1, FALSE
FROM users u
CROSS JOIN courses c
WHERE u.role_id = 4 AND u.student_type = 'REGULAR' AND c.course_id IN (1, 2, 3, 4, 5);

-- Repeat Students enrollments:
-- TG/2023/1850 (user_id 29) repeats ICT2132
INSERT INTO course_enrollment (student_id, course_id, academic_year, semester, is_repeat) VALUES
(29, 3, '2024/2025', 1, TRUE),
(29, 1, '2024/2025', 1, FALSE);

-- TG/2023/1855 (user_id 30) repeats ICT2113
INSERT INTO course_enrollment (student_id, course_id, academic_year, semester, is_repeat) VALUES
(30, 1, '2024/2025', 1, TRUE),
(30, 3, '2024/2025', 1, FALSE);

-- Batch-Missed Students enrollments:
INSERT INTO course_enrollment (student_id, course_id, academic_year, semester, is_repeat) VALUES
(31, 1, '2024/2025', 1, FALSE),
(31, 2, '2024/2025', 1, FALSE),
(31, 3, '2024/2025', 1, FALSE),
(32, 1, '2024/2025', 1, FALSE),
(32, 3, '2024/2025', 1, FALSE),
(32, 4, '2024/2025', 1, FALSE);

-- 7. Evaluations for Courses
-- Evaluation for ICT2132:
-- Quiz 01 (10%), Quiz 02 (10%), Assignment 01 (20%), Mid Exam (20%) -> CA components
-- Final Theory (40%), Final Practical (60%) -> Exam components
INSERT INTO evaluations (evaluation_id, course_id, evaluation_name, evaluation_type, max_marks, weightage_percent, semester, academic_year) VALUES
(1, 3, 'Quiz 01 - Java OOP Fundamentals', 'QUIZ', 100.0, 10.0, 1, '2024/2025'),
(2, 3, 'Quiz 02 - GUI & Event Handling', 'QUIZ', 100.0, 10.0, 1, '2024/2025'),
(3, 3, 'Assignment 01 - Desktop Application Design', 'ASSIGNMENT', 100.0, 20.0, 1, '2024/2025'),
(4, 3, 'Mid Semester Exam - Practical OOP', 'MID_EXAM', 100.0, 20.0, 1, '2024/2025'),
(5, 3, 'Final Theory Examination', 'FINAL_THEORY', 100.0, 50.0, 1, '2024/2025'),
(6, 3, 'Final Practical Examination', 'FINAL_PRACTICAL', 100.0, 50.0, 1, '2024/2025'),

-- Evaluations for ICT2113 (Database Systems):
(7, 1, 'Quiz 01 - Relational Algebra & SQL', 'QUIZ', 100.0, 15.0, 1, '2024/2025'),
(8, 1, 'Assignment 01 - Database Schema Normalization', 'ASSIGNMENT', 100.0, 25.0, 1, '2024/2025'),
(9, 1, 'Final Theory Examination', 'FINAL_THEORY', 100.0, 60.0, 1, '2024/2025'),

-- Evaluations for ICT2122 (Software Engineering):
(10, 2, 'Assignment 01 - SRS Documentation', 'ASSIGNMENT', 100.0, 30.0, 1, '2024/2025'),
(11, 2, 'Final Theory Examination', 'FINAL_THEORY', 100.0, 70.0, 1, '2024/2025');

-- 8. Marks
-- Sample marks out of 100 for ICT2132 evaluations:
-- Student 11 (Nonis): High achiever (88, 92, 85, 90, 86, 91)
INSERT INTO marks (evaluation_id, student_id, marks_obtained, entered_by) VALUES
(1, 11, 88.0, 2), (2, 11, 92.0, 2), (3, 11, 85.0, 2), (4, 11, 90.0, 2), (5, 11, 86.0, 2), (6, 11, 91.0, 2),
-- Student 12 (Anuda): Excellent (82, 84, 88, 80, 85, 87)
(1, 12, 82.0, 2), (2, 12, 84.0, 2), (3, 12, 88.0, 2), (4, 12, 80.0, 2), (5, 12, 85.0, 2), (6, 12, 87.0, 2),
-- Student 13 (Sahan): Very Good (75, 78, 80, 76, 78, 82)
(1, 13, 75.0, 2), (2, 13, 78.0, 2), (3, 13, 80.0, 2), (4, 13, 76.0, 2), (5, 13, 78.0, 2), (6, 13, 82.0, 2),
-- Student 14 (Nethsara): Good (72, 70, 75, 74, 71, 75)
(1, 14, 72.0, 2), (2, 14, 70.0, 2), (3, 14, 75.0, 2), (4, 14, 74.0, 2), (5, 14, 71.0, 2), (6, 14, 75.0, 2),
-- Student 15 (Kaveesha): Average Pass (55, 58, 62, 54, 58, 60)
(1, 15, 55.0, 2), (2, 15, 58.0, 2), (3, 15, 62.0, 2), (4, 15, 54.0, 2), (5, 15, 58.0, 2), (6, 15, 60.0, 2),
-- Student 16 (Dilshan): CA Eligible (50, 52, 55, 50, 52, 54)
(1, 16, 50.0, 2), (2, 16, 52.0, 2), (3, 16, 55.0, 2), (4, 16, 50.0, 2), (5, 16, 52.0, 2), (6, 16, 54.0, 2),
-- Student 17 (Tharindu): Low CA - NOT ELIGIBLE FOR FINAL EXAM (CA < 40%) (30, 32, 28, 35, 30, 25)
(1, 17, 30.0, 2), (2, 17, 32.0, 2), (3, 17, 28.0, 2), (4, 17, 35.0, 2), (5, 17, 30.0, 2), (6, 17, 25.0, 2),
-- Student 18 (Sanduni): Good (78, 80, 82, 79, 81, 84)
(1, 18, 78.0, 2), (2, 18, 80.0, 2), (3, 18, 82.0, 2), (4, 18, 79.0, 2), (5, 18, 81.0, 2), (6, 18, 84.0, 2),
-- Student 19 (Ishara): (68, 72, 70, 71, 70, 73)
(1, 19, 68.0, 2), (2, 19, 72.0, 2), (3, 19, 70.0, 2), (4, 19, 71.0, 2), (5, 19, 70.0, 2), (6, 19, 73.0, 2),
-- Student 20 (Bhanuka): (64, 66, 68, 62, 65, 67)
(1, 20, 64.0, 2), (2, 20, 66.0, 2), (3, 20, 68.0, 2), (4, 20, 62.0, 2), (5, 20, 65.0, 2), (6, 20, 67.0, 2),
-- Repeat student 29 (Kasun W.): Repeating ICT2132 (62, 65, 68, 64, 65, 66)
(1, 29, 62.0, 2), (2, 29, 65.0, 2), (3, 29, 68.0, 2), (4, 29, 64.0, 2), (5, 29, 65.0, 2), (6, 29, 66.0, 2);

-- Sample marks for ICT2113:
INSERT INTO marks (evaluation_id, student_id, marks_obtained, entered_by) VALUES
(7, 11, 85.0, 3), (8, 11, 90.0, 3), (9, 11, 88.0, 3),
(7, 12, 80.0, 3), (8, 12, 85.0, 3), (9, 12, 82.0, 3),
(7, 13, 76.0, 3), (8, 13, 80.0, 3), (9, 13, 78.0, 3),
(7, 14, 72.0, 3), (8, 14, 74.0, 3), (9, 14, 73.0, 3),
(7, 30, 65.0, 3), (8, 30, 68.0, 3), (9, 30, 66.0, 3); -- Repeat student 30 in ICT2113

-- 9. Grades (Pre-calculated records based on UGC Circular No. 12-2024)
INSERT INTO grades (student_id, course_id, academic_year, ca_mark, final_exam_mark, total_mark, grade_letter, grade_point, is_repeat) VALUES
-- Student 11 (Nonis)
(11, 3, '2024/2025', 88.7, 88.5, 88.6, 'A+', 4.00, FALSE),
(11, 1, '2024/2025', 87.5, 88.0, 87.8, 'A+', 4.00, FALSE),
-- Student 12 (Anuda)
(12, 3, '2024/2025', 83.5, 86.0, 85.0, 'A+', 4.00, FALSE),
(12, 1, '2024/2025', 82.5, 82.0, 82.2, 'A', 4.00, FALSE),
-- Student 13 (Sahan)
(13, 3, '2024/2025', 77.2, 80.0, 78.9, 'A-', 3.70, FALSE),
(13, 1, '2024/2025', 78.0, 78.0, 78.0, 'A-', 3.70, FALSE),
-- Student 14 (Nethsara)
(14, 3, '2024/2025', 72.7, 73.0, 72.9, 'B+', 3.30, FALSE),
(14, 1, '2024/2025', 73.0, 73.0, 73.0, 'B+', 3.30, FALSE),
-- Student 15 (Kaveesha)
(15, 3, '2024/2025', 57.2, 59.0, 58.3, 'C+', 2.30, FALSE),
-- Student 16 (Dilshan)
(16, 3, '2024/2025', 51.7, 53.0, 52.5, 'C', 2.00, FALSE),
-- Student 17 (Tharindu - CA < 40%)
(17, 3, '2024/2025', 31.2, 27.5, 29.0, 'E', 0.00, FALSE),
-- Student 18 (Sanduni)
(18, 3, '2024/2025', 79.7, 82.5, 81.4, 'A', 4.00, FALSE),
-- Repeat Student 29 (Capped at C/2.00 per standard repeat rules or marked as repeat)
(29, 3, '2024/2025', 64.7, 65.5, 65.2, 'C', 2.00, TRUE),
-- Repeat Student 30 in ICT2113
(30, 1, '2024/2025', 66.5, 66.0, 66.2, 'C', 2.00, TRUE);

-- 10. Medical Records
-- Scenario 4 & 5 medical submissions:
INSERT INTO medical (medical_id, student_id, course_id, session_type, session_date, submission_date, reason, medical_doc_path, status, reviewed_by, reviewed_at, remarks) VALUES
-- Approved medicals for Student 16 (TG/2024/2006) -> 3 approved medicals
(1, 16, 3, 'THEORY', '2026-09-15', '2026-09-17', 'Viral fever - treated at Base Hospital Kamburupitiya', 'medicals/tg2006_med01.pdf', 'APPROVED', 7, '2026-09-18 10:00:00', 'Medical certificate verified by University Medical Officer'),
(2, 16, 3, 'THEORY', '2026-09-22', '2026-09-24', 'Viral fever rest recommended by doctor', 'medicals/tg2006_med02.pdf', 'APPROVED', 7, '2026-09-25 11:30:00', 'Approved by UMO'),
(3, 16, 3, 'PRACTICAL', '2026-09-23', '2026-09-25', 'Doctor recommended recovery rest', 'medicals/tg2006_med03.pdf', 'APPROVED', 7, '2026-09-26 09:15:00', 'Approved by UMO'),

-- Approved medicals for Student 17 (TG/2024/2007) -> 2 approved medicals (Scenario 5)
(4, 17, 3, 'THEORY', '2026-09-08', '2026-09-10', 'Dengue fever admission', 'medicals/tg2007_med01.pdf', 'APPROVED', 7, '2026-09-12 14:00:00', 'Approved by UMO'),
(5, 17, 3, 'PRACTICAL', '2026-09-09', '2026-09-11', 'Hospitalization for Dengue', 'medicals/tg2007_med02.pdf', 'APPROVED', 7, '2026-09-12 14:10:00', 'Approved by UMO'),

-- Rejected / Not Approved medical for Student 15 (TG/2024/2005)
(6, 15, 3, 'THEORY', '2026-09-29', '2026-10-05', 'Headache - submitted after 14-day university deadline', 'medicals/tg2005_med01.pdf', 'NOT_APPROVED', 7, '2026-10-06 09:00:00', 'Rejected: Submitted after deadline of 7 days from illness');

-- 11. Attendance Records (15 Theory + 15 Practical sessions for ICT2132)
-- As required by the SRS and assignment:
-- Scenario 1: > 80% attendance (Students 11, 12, 13: 15/15 present = 100%, or 14/15 = 93.3%)
-- Scenario 2: Exactly 80% attendance (Student 14: exactly 12/15 present = 80.0%, 3 absent, 0 medical)
-- Scenario 3: < 80% attendance without medical (Student 15: 9/15 present = 60.0%, 6 absent, 0 medical)
-- Scenario 4: > 80% attendance WITH medical (Student 16: 10 present + 3 approved medicals = 13/15 = 86.7%)
-- Scenario 5: < 80% attendance WITH medical (Student 17: 8 present + 2 approved medicals = 10/15 = 66.7%)

-- Helper procedure to populate attendance systematically
DELIMITER $$
CREATE PROCEDURE PopulateAttendanceData()
BEGIN
    DECLARE i INT DEFAULT 1;
    DECLARE sessionDate DATE;
    
    -- Populate 15 Theory and 15 Practical sessions for ICT2132 (course_id = 3)
    WHILE i <= 15 DO
        SET sessionDate = DATE_ADD('2026-08-03', INTERVAL (i - 1) * 7 DAY);
        
        -- Student 11 (Nonis): 15 Theory Present, 15 Practical Present (>80%)
        INSERT INTO attendance (student_id, course_id, session_date, session_type, session_number, status, recorded_by)
        VALUES (11, 3, sessionDate, 'THEORY', i, 'PRESENT', 7);
        INSERT INTO attendance (student_id, course_id, session_date, session_type, session_number, status, recorded_by)
        VALUES (11, 3, sessionDate, 'PRACTICAL', i, 'PRESENT', 7);
        
        -- Student 12 (Anuda): 14/15 Theory Present (absent session 5), 15 Practical Present (>80%)
        INSERT INTO attendance (student_id, course_id, session_date, session_type, session_number, status, recorded_by)
        VALUES (12, 3, sessionDate, 'THEORY', i, IF(i = 5, 'ABSENT', 'PRESENT'), 7);
        INSERT INTO attendance (student_id, course_id, session_date, session_type, session_number, status, recorded_by)
        VALUES (12, 3, sessionDate, 'PRACTICAL', i, 'PRESENT', 7);
        
        -- Student 13 (Sahan): 14/15 Theory, 14/15 Practical (>80%)
        INSERT INTO attendance (student_id, course_id, session_date, session_type, session_number, status, recorded_by)
        VALUES (13, 3, sessionDate, 'THEORY', i, IF(i = 8, 'ABSENT', 'PRESENT'), 7);
        INSERT INTO attendance (student_id, course_id, session_date, session_type, session_number, status, recorded_by)
        VALUES (13, 3, sessionDate, 'PRACTICAL', i, IF(i = 8, 'ABSENT', 'PRESENT'), 7);

        -- Student 14 (Nethsara) - SCENARIO 2: EXACTLY 80% ATTENDANCE (12 present out of 15, sessions 13,14,15 absent)
        INSERT INTO attendance (student_id, course_id, session_date, session_type, session_number, status, recorded_by)
        VALUES (14, 3, sessionDate, 'THEORY', i, IF(i IN (13, 14, 15), 'ABSENT', 'PRESENT'), 7);
        INSERT INTO attendance (student_id, course_id, session_date, session_type, session_number, status, recorded_by)
        VALUES (14, 3, sessionDate, 'PRACTICAL', i, IF(i IN (13, 14, 15), 'ABSENT', 'PRESENT'), 7);

        -- Student 15 (Kaveesha) - SCENARIO 3: < 80% WITHOUT MEDICAL (9 present out of 15 = 60%, sessions 10..15 absent)
        INSERT INTO attendance (student_id, course_id, session_date, session_type, session_number, status, recorded_by)
        VALUES (15, 3, sessionDate, 'THEORY', i, IF(i >= 10, 'ABSENT', 'PRESENT'), 7);
        INSERT INTO attendance (student_id, course_id, session_date, session_type, session_number, status, recorded_by)
        VALUES (15, 3, sessionDate, 'PRACTICAL', i, IF(i >= 10, 'ABSENT', 'PRESENT'), 7);

        -- Student 16 (Dilshan) - SCENARIO 4: > 80% WITH MEDICAL (10 present + 3 approved medicals = 13/15 = 86.7%)
        -- Absences at sessions 7, 8, 9 (medical submitted & approved for sessions 7, 8 Theory and 8 Practical)
        INSERT INTO attendance (student_id, course_id, session_date, session_type, session_number, status, recorded_by)
        VALUES (16, 3, sessionDate, 'THEORY', i, IF(i IN (7, 8, 9, 10, 11), 'ABSENT', 'PRESENT'), 7);
        INSERT INTO attendance (student_id, course_id, session_date, session_type, session_number, status, recorded_by)
        VALUES (16, 3, sessionDate, 'PRACTICAL', i, IF(i IN (7, 8, 9, 10, 11), 'ABSENT', 'PRESENT'), 7);

        -- Student 17 (Tharindu) - SCENARIO 5: < 80% WITH MEDICAL (8 present + 2 approved medicals = 10/15 = 66.7%)
        -- Absences at sessions 9..15 (7 absences, 2 approved medicals -> 10/15)
        INSERT INTO attendance (student_id, course_id, session_date, session_type, session_number, status, recorded_by)
        VALUES (17, 3, sessionDate, 'THEORY', i, IF(i >= 9, 'ABSENT', 'PRESENT'), 7);
        INSERT INTO attendance (student_id, course_id, session_date, session_type, session_number, status, recorded_by)
        VALUES (17, 3, sessionDate, 'PRACTICAL', i, IF(i >= 9, 'ABSENT', 'PRESENT'), 7);

        -- Students 18 to 28 (Regular class batch): high attendance (14/15 or 15/15)
        INSERT INTO attendance (student_id, course_id, session_date, session_type, session_number, status, recorded_by)
        VALUES (18, 3, sessionDate, 'THEORY', i, 'PRESENT', 7);
        INSERT INTO attendance (student_id, course_id, session_date, session_type, session_number, status, recorded_by)
        VALUES (18, 3, sessionDate, 'PRACTICAL', i, 'PRESENT', 7);

        -- Repeat student 29 (Kasun W.): 13/15 Present
        INSERT INTO attendance (student_id, course_id, session_date, session_type, session_number, status, recorded_by)
        VALUES (29, 3, sessionDate, 'THEORY', i, IF(i IN (3, 7), 'ABSENT', 'PRESENT'), 7);
        INSERT INTO attendance (student_id, course_id, session_date, session_type, session_number, status, recorded_by)
        VALUES (29, 3, sessionDate, 'PRACTICAL', i, IF(i IN (3, 7), 'ABSENT', 'PRESENT'), 7);

        SET i = i + 1;
    END WHILE;
END$$
DELIMITER ;

CALL PopulateAttendanceData();
DROP PROCEDURE PopulateAttendanceData;

-- 12. Notices
INSERT INTO notices (notice_id, title, content, published_date, created_by, target_role, status) VALUES
(1, 'Commencement of Semester I Academic Activities - 2024/2025', 'Academic activities for Level II Semester I of the Faculty of Technology will commence on Monday, 3rd August 2026. All students must register for their elective modules via the academic portal within the first two weeks.', '2026-08-01', 1, 'ALL', 'ACTIVE'),
(2, 'Submission of Medical Certificates for Missed Lectures/Labs', 'Students who were absent for lectures or laboratory practicals due to illness must submit their medical certificates to the Technical Officer / Department office within 7 days of returning, endorsed by the University Medical Officer (UMO). Late submissions will be rejected without consideration.', '2026-08-10', 1, 'ALL', 'ACTIVE'),
(3, 'Continuous Assessment (CA) Marks Verification Notice', 'Continuous Assessment (CA) marks for ICT2132 and ICT2113 have been published by the respective course coordinators. All undergraduates are requested to verify their marks before 25th October 2026. Queries must be addressed to the course coordinator directly.', '2026-10-01', 1, 'ALL', 'ACTIVE'),
(4, 'Semester I Final Examination Timetable Release', 'The final examination timetable for the 2024/2025 academic year will be released by the Dean\'s Office on 20th October 2026. Please check the timetable tab for room allocations and session timings. Students with less than 80% attendance or CA < 40% are barred from sitting the exam.', '2026-10-05', 1, 'ALL', 'ACTIVE'),
(5, 'Lab Equipment Maintenance & Lab Access Hours', 'Computer Lab 01 and Network Systems Lab will undergo scheduled server maintenance this Saturday from 8:00 AM to 1:00 PM. Lab users are advised to backup all local project files.', '2026-10-06', 1, 'ALL', 'ACTIVE');

-- 13. Timetables
INSERT INTO timetables (timetable_id, department_id, course_id, day_of_week, start_time, end_time, room, lecturer_id, session_type, semester, academic_year) VALUES
(1, 1, 3, 'MONDAY', '08:00:00', '10:00:00', 'Lecture Theatre 01', 2, 'THEORY', 1, '2024/2025'),
(2, 1, 3, 'MONDAY', '10:00:00', '12:00:00', 'Computer Lab 01', 2, 'PRACTICAL', 1, '2024/2025'),
(3, 1, 1, 'TUESDAY', '08:00:00', '10:00:00', 'Lecture Theatre 02', 3, 'THEORY', 1, '2024/2025'),
(4, 1, 1, 'TUESDAY', '10:00:00', '12:00:00', 'Computer Lab 02', 3, 'PRACTICAL', 1, '2024/2025'),
(5, 1, 2, 'WEDNESDAY', '13:00:00', '15:00:00', 'Lecture Theatre 01', 2, 'THEORY', 1, '2024/2025'),
(6, 1, 4, 'THURSDAY', '08:00:00', '10:00:00', 'Lecture Theatre 01', 2, 'THEORY', 1, '2024/2025'),
(7, 1, 4, 'THURSDAY', '10:00:00', '12:00:00', 'Network Systems Lab', 2, 'PRACTICAL', 1, '2024/2025'),
(8, 4, 5, 'FRIDAY', '08:00:00', '10:00:00', 'Lecture Hall MDT', 6, 'THEORY', 1, '2024/2025'),
(9, 3, 6, 'FRIDAY', '10:00:00', '12:00:00', 'Engineering Workshop', 5, 'PRACTICAL', 1, '2024/2025');

-- End of database.sql
