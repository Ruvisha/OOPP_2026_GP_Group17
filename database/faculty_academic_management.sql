-- ============================================================================
-- UNIVERSITY OF RUHUNA - FACULTY OF TECHNOLOGY
-- ACADEMIC MANAGEMENT SYSTEM (AMS)
-- Consolidated Production Database Schema & Complete Realistic Dataset
-- Database: faculty_academic_management
-- Consolidated from: group17_db.sql, role_specific_id_migration.sql, faculty_academic_management_populated.sql
-- Compatibility: MySQL 5.7+ / MySQL 8.0+ / MariaDB 10.4+ (XAMPP / phpMyAdmin)
-- Character Set: utf8mb4 / utf8mb4_unicode_ci
-- Generated: 2026-11-03
-- ============================================================================

SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO';
SET time_zone = '+00:00';

-- ----------------------------------------------------------------------------
-- Database Initialization
-- ----------------------------------------------------------------------------
CREATE DATABASE IF NOT EXISTS `faculty_academic_management` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE `faculty_academic_management`;

-- ============================================================================
-- 1. TABLE STRUCTURES (DDL)
-- ============================================================================

-- --------------------------------------------------------
-- Table structure for `roles`
-- --------------------------------------------------------
DROP TABLE IF EXISTS `roles`;
CREATE TABLE `roles` (
  `role_id` int(11) NOT NULL AUTO_INCREMENT,
  `role_name` varchar(50) NOT NULL,
  `description` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`role_id`),
  UNIQUE KEY `role_name` (`role_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------
-- Table structure for `departments`
-- --------------------------------------------------------
DROP TABLE IF EXISTS `departments`;
CREATE TABLE `departments` (
  `department_id` int(11) NOT NULL AUTO_INCREMENT,
  `department_code` varchar(10) NOT NULL,
  `department_name` varchar(150) NOT NULL,
  `description` text DEFAULT NULL,
  PRIMARY KEY (`department_id`),
  UNIQUE KEY `department_code` (`department_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------
-- Table structure for `academic_years`
-- --------------------------------------------------------
DROP TABLE IF EXISTS `academic_years`;
CREATE TABLE `academic_years` (
  `academic_year` varchar(10) NOT NULL,
  `is_active` tinyint(1) NOT NULL DEFAULT 0,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  PRIMARY KEY (`academic_year`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------
-- Table structure for `role_user_id_sequences`
-- --------------------------------------------------------
DROP TABLE IF EXISTS `role_user_id_sequences`;
CREATE TABLE `role_user_id_sequences` (
  `role_id` int(11) NOT NULL,
  `next_value` int(11) NOT NULL DEFAULT 1,
  PRIMARY KEY (`role_id`),
  CONSTRAINT `fk_role_user_id_sequences_role` FOREIGN KEY (`role_id`) REFERENCES `roles` (`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------
-- Table structure for `users`
-- --------------------------------------------------------
DROP TABLE IF EXISTS `users`;
CREATE TABLE `users` (
  `user_id` int(11) NOT NULL AUTO_INCREMENT,
  `username` varchar(50) NOT NULL,
  `password_hash` varchar(255) NOT NULL,
  `role_id` int(11) NOT NULL,
  `full_name` varchar(150) NOT NULL,
  `email` varchar(100) NOT NULL,
  `phone` varchar(20) DEFAULT NULL,
  `address` varchar(255) DEFAULT NULL,
  `profile_pic` varchar(255) DEFAULT 'default_avatar.png',
  `department_id` int(11) DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  `status` enum('ACTIVE','INACTIVE') DEFAULT 'ACTIVE',
  `student_reg_no` varchar(30) DEFAULT NULL,
  `academic_year` varchar(20) DEFAULT NULL,
  `current_semester` int(11) DEFAULT 1,
  `student_type` enum('REGULAR','REPEAT','BATCH_MISSED') DEFAULT 'REGULAR',
  `designation` varchar(100) DEFAULT NULL,
  `office_room` varchar(50) DEFAULT NULL,
  `assigned_lab` varchar(100) DEFAULT NULL,
  `role_specific_id` char(4) NOT NULL,
  PRIMARY KEY (`user_id`),
  UNIQUE KEY `username` (`username`),
  UNIQUE KEY `email` (`email`),
  UNIQUE KEY `uq_role_specific_id` (`role_id`,`role_specific_id`),
  UNIQUE KEY `student_reg_no` (`student_reg_no`),
  KEY `fk_users_department` (`department_id`),
  KEY `idx_user_role` (`role_id`),
  KEY `idx_user_reg_no` (`student_reg_no`),
  CONSTRAINT `fk_users_department` FOREIGN KEY (`department_id`) REFERENCES `departments` (`department_id`) ON DELETE SET NULL,
  CONSTRAINT `fk_users_role` FOREIGN KEY (`role_id`) REFERENCES `roles` (`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------
-- Table structure for `courses`
-- --------------------------------------------------------
DROP TABLE IF EXISTS `courses`;
CREATE TABLE `courses` (
  `course_id` int(11) NOT NULL AUTO_INCREMENT,
  `course_code` varchar(20) NOT NULL,
  `course_name` varchar(150) NOT NULL,
  `credits` int(11) NOT NULL DEFAULT 2,
  `semester` int(11) NOT NULL DEFAULT 1,
  `study_year` tinyint(4) NOT NULL DEFAULT 1,
  `department_id` int(11) NOT NULL,
  `has_theory` tinyint(1) NOT NULL DEFAULT 1,
  `has_practical` tinyint(1) NOT NULL DEFAULT 0,
  `description` text DEFAULT NULL,
  PRIMARY KEY (`course_id`),
  UNIQUE KEY `course_code` (`course_code`),
  KEY `idx_course_dept` (`department_id`),
  CONSTRAINT `fk_courses_department` FOREIGN KEY (`department_id`) REFERENCES `departments` (`department_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------
-- Table structure for `course_lecturers`
-- --------------------------------------------------------
DROP TABLE IF EXISTS `course_lecturers`;
CREATE TABLE `course_lecturers` (
  `course_id` int(11) NOT NULL,
  `lecturer_id` int(11) NOT NULL,
  `assigned_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  PRIMARY KEY (`course_id`),
  KEY `fk_course_lecturer_user` (`lecturer_id`),
  CONSTRAINT `fk_course_lecturer_course` FOREIGN KEY (`course_id`) REFERENCES `courses` (`course_id`) ON DELETE CASCADE,
  CONSTRAINT `fk_course_lecturer_user` FOREIGN KEY (`lecturer_id`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------
-- Table structure for `course_enrollment`
-- --------------------------------------------------------
DROP TABLE IF EXISTS `course_enrollment`;
CREATE TABLE `course_enrollment` (
  `enrollment_id` int(11) NOT NULL AUTO_INCREMENT,
  `student_id` int(11) NOT NULL,
  `course_id` int(11) NOT NULL,
  `academic_year` varchar(20) NOT NULL,
  `semester` int(11) NOT NULL DEFAULT 1,
  `is_repeat` tinyint(1) NOT NULL DEFAULT 0,
  `enrolled_at` timestamp NOT NULL DEFAULT current_timestamp(),
  PRIMARY KEY (`enrollment_id`),
  UNIQUE KEY `uq_student_course_year_semester` (`student_id`,`course_id`,`academic_year`,`semester`),
  KEY `idx_enroll_student` (`student_id`),
  KEY `idx_enroll_course` (`course_id`),
  CONSTRAINT `fk_enroll_course` FOREIGN KEY (`course_id`) REFERENCES `courses` (`course_id`) ON DELETE CASCADE,
  CONSTRAINT `fk_enroll_student` FOREIGN KEY (`student_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------
-- Table structure for `evaluations`
-- --------------------------------------------------------
DROP TABLE IF EXISTS `evaluations`;
CREATE TABLE `evaluations` (
  `evaluation_id` int(11) NOT NULL AUTO_INCREMENT,
  `course_id` int(11) NOT NULL,
  `evaluation_name` varchar(100) NOT NULL,
  `evaluation_type` enum('QUIZ','ASSIGNMENT','MID_EXAM','FINAL_THEORY','FINAL_PRACTICAL','CA_AGGREGATE') NOT NULL,
  `max_marks` double NOT NULL DEFAULT 100,
  `weightage_percent` double NOT NULL,
  `semester` int(11) NOT NULL DEFAULT 1,
  `academic_year` varchar(20) NOT NULL,
  PRIMARY KEY (`evaluation_id`),
  KEY `idx_eval_course` (`course_id`),
  CONSTRAINT `fk_eval_course` FOREIGN KEY (`course_id`) REFERENCES `courses` (`course_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------
-- Table structure for `marks`
-- --------------------------------------------------------
DROP TABLE IF EXISTS `marks`;
CREATE TABLE `marks` (
  `mark_id` int(11) NOT NULL AUTO_INCREMENT,
  `evaluation_id` int(11) NOT NULL,
  `student_id` int(11) NOT NULL,
  `marks_obtained` double NOT NULL,
  `entered_by` int(11) DEFAULT NULL,
  `updated_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  PRIMARY KEY (`mark_id`),
  UNIQUE KEY `uq_student_eval` (`evaluation_id`,`student_id`),
  KEY `fk_marks_enterer` (`entered_by`),
  KEY `idx_marks_student` (`student_id`),
  CONSTRAINT `fk_marks_enterer` FOREIGN KEY (`entered_by`) REFERENCES `users` (`user_id`) ON DELETE SET NULL,
  CONSTRAINT `fk_marks_eval` FOREIGN KEY (`evaluation_id`) REFERENCES `evaluations` (`evaluation_id`) ON DELETE CASCADE,
  CONSTRAINT `fk_marks_student` FOREIGN KEY (`student_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------
-- Table structure for `grades`
-- --------------------------------------------------------
DROP TABLE IF EXISTS `grades`;
CREATE TABLE `grades` (
  `grade_id` int(11) NOT NULL AUTO_INCREMENT,
  `student_id` int(11) NOT NULL,
  `course_id` int(11) NOT NULL,
  `academic_year` varchar(20) NOT NULL,
  `ca_mark` double NOT NULL DEFAULT 0,
  `final_exam_mark` double NOT NULL DEFAULT 0,
  `total_mark` double NOT NULL DEFAULT 0,
  `grade_letter` varchar(5) NOT NULL,
  `grade_point` double NOT NULL,
  `is_repeat` tinyint(1) NOT NULL DEFAULT 0,
  `calculated_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  PRIMARY KEY (`grade_id`),
  UNIQUE KEY `uq_student_course_grade` (`student_id`,`course_id`,`academic_year`),
  KEY `fk_grades_course` (`course_id`),
  KEY `idx_grades_student` (`student_id`),
  CONSTRAINT `fk_grades_course` FOREIGN KEY (`course_id`) REFERENCES `courses` (`course_id`) ON DELETE CASCADE,
  CONSTRAINT `fk_grades_student` FOREIGN KEY (`student_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------
-- Table structure for `quizzes`
-- --------------------------------------------------------
DROP TABLE IF EXISTS `quizzes`;
CREATE TABLE `quizzes` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `course_id` int(11) NOT NULL,
  `lecturer_id` int(11) NOT NULL,
  `evaluation_id` int(11) DEFAULT NULL,
  `title` varchar(200) NOT NULL,
  `duration_minutes` int(11) NOT NULL DEFAULT 30,
  `start_time` datetime DEFAULT NULL,
  `end_time` datetime DEFAULT NULL,
  `max_attempts` int(11) NOT NULL DEFAULT 1,
  `status` varchar(20) NOT NULL DEFAULT 'DRAFT',
  `marks_released` tinyint(1) NOT NULL DEFAULT 0,
  `file_path` varchar(1000) DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------
-- Table structure for `quiz_questions`
-- --------------------------------------------------------
DROP TABLE IF EXISTS `quiz_questions`;
CREATE TABLE `quiz_questions` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `quiz_id` int(11) NOT NULL,
  `question_text` text NOT NULL,
  `question_type` varchar(30) NOT NULL,
  `option_a` text DEFAULT NULL,
  `option_b` text DEFAULT NULL,
  `option_c` text DEFAULT NULL,
  `option_d` text DEFAULT NULL,
  `correct_answer` text DEFAULT NULL,
  `marks` decimal(8,2) NOT NULL DEFAULT 1.00,
  PRIMARY KEY (`id`),
  KEY `quiz_id` (`quiz_id`),
  CONSTRAINT `quiz_questions_ibfk_1` FOREIGN KEY (`quiz_id`) REFERENCES `quizzes` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------
-- Table structure for `quiz_attempts`
-- --------------------------------------------------------
DROP TABLE IF EXISTS `quiz_attempts`;
CREATE TABLE `quiz_attempts` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `quiz_id` int(11) NOT NULL,
  `student_id` int(11) NOT NULL,
  `started_at` datetime NOT NULL,
  `submitted_at` datetime DEFAULT NULL,
  `score` decimal(10,2) NOT NULL DEFAULT 0.00,
  `total_marks` decimal(10,2) NOT NULL DEFAULT 0.00,
  `status` varchar(20) NOT NULL DEFAULT 'IN_PROGRESS',
  `graded_by` int(11) DEFAULT NULL,
  `graded_at` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_tg2102_one_attempt` (`quiz_id`,`student_id`),
  KEY `idx_tg2102_attempt_student` (`student_id`),
  CONSTRAINT `fk_tg2102_attempt_quiz` FOREIGN KEY (`quiz_id`) REFERENCES `quizzes` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------
-- Table structure for `quiz_answers`
-- --------------------------------------------------------
DROP TABLE IF EXISTS `quiz_answers`;
CREATE TABLE `quiz_answers` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `attempt_id` int(11) NOT NULL,
  `question_id` int(11) NOT NULL,
  `answer_text` text DEFAULT NULL,
  `awarded_marks` decimal(10,2) NOT NULL DEFAULT 0.00,
  `feedback` text DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_tg2102_attempt_question` (`attempt_id`,`question_id`),
  KEY `fk_tg2102_answer_question` (`question_id`),
  CONSTRAINT `fk_tg2102_answer_attempt` FOREIGN KEY (`attempt_id`) REFERENCES `quiz_attempts` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_tg2102_answer_question` FOREIGN KEY (`question_id`) REFERENCES `quiz_questions` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------
-- Table structure for `assignments`
-- --------------------------------------------------------
DROP TABLE IF EXISTS `assignments`;
CREATE TABLE `assignments` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `course_id` int(11) NOT NULL,
  `lecturer_id` int(11) NOT NULL,
  `evaluation_id` int(11) DEFAULT NULL,
  `title` varchar(200) NOT NULL,
  `instructions` text DEFAULT NULL,
  `max_marks` decimal(8,2) NOT NULL DEFAULT 100.00,
  `due_date` date DEFAULT NULL,
  `status` varchar(20) NOT NULL DEFAULT 'DRAFT',
  `marks_released` tinyint(1) NOT NULL DEFAULT 0,
  `file_path` varchar(1000) DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------
-- Table structure for `assignment_submissions`
-- --------------------------------------------------------
DROP TABLE IF EXISTS `assignment_submissions`;
CREATE TABLE `assignment_submissions` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `assignment_id` int(11) NOT NULL,
  `student_id` int(11) NOT NULL,
  `file_path` varchar(1000) NOT NULL,
  `submitted_at` datetime NOT NULL DEFAULT current_timestamp(),
  `status` varchar(30) NOT NULL DEFAULT 'SUBMITTED',
  `marks` decimal(10,2) DEFAULT NULL,
  `feedback` text DEFAULT NULL,
  `graded_by` int(11) DEFAULT NULL,
  `graded_at` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_tg2102_assignment_student` (`assignment_id`,`student_id`),
  KEY `idx_tg2102_submission_student` (`student_id`),
  CONSTRAINT `fk_tg2102_submission_assignment` FOREIGN KEY (`assignment_id`) REFERENCES `assignments` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------
-- Table structure for `attendance`
-- --------------------------------------------------------
DROP TABLE IF EXISTS `attendance`;
CREATE TABLE `attendance` (
  `attendance_id` int(11) NOT NULL AUTO_INCREMENT,
  `student_id` int(11) NOT NULL,
  `course_id` int(11) NOT NULL,
  `session_date` date NOT NULL,
  `session_type` enum('THEORY','PRACTICAL') NOT NULL,
  `session_number` int(11) NOT NULL,
  `hours` int(11) NOT NULL DEFAULT 2,
  `status` enum('PRESENT','ABSENT') NOT NULL,
  `recorded_by` int(11) DEFAULT NULL,
  `recorded_at` timestamp NOT NULL DEFAULT current_timestamp(),
  PRIMARY KEY (`attendance_id`),
  UNIQUE KEY `uq_student_course_session` (`student_id`,`course_id`,`session_type`,`session_number`),
  KEY `fk_att_recorder` (`recorded_by`),
  KEY `idx_att_student` (`student_id`),
  KEY `idx_att_course` (`course_id`),
  CONSTRAINT `fk_att_course` FOREIGN KEY (`course_id`) REFERENCES `courses` (`course_id`) ON DELETE CASCADE,
  CONSTRAINT `fk_att_recorder` FOREIGN KEY (`recorded_by`) REFERENCES `users` (`user_id`) ON DELETE SET NULL,
  CONSTRAINT `fk_att_student` FOREIGN KEY (`student_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------
-- Table structure for `medical`
-- --------------------------------------------------------
DROP TABLE IF EXISTS `medical`;
CREATE TABLE `medical` (
  `medical_id` int(11) NOT NULL AUTO_INCREMENT,
  `student_id` int(11) NOT NULL,
  `course_id` int(11) NOT NULL,
  `session_type` enum('THEORY','PRACTICAL','BOTH') NOT NULL DEFAULT 'THEORY',
  `session_date` date NOT NULL,
  `submission_date` date NOT NULL,
  `reason` text NOT NULL,
  `medical_doc_path` varchar(255) DEFAULT NULL,
  `status` enum('APPROVED','NOT_APPROVED') NOT NULL DEFAULT 'NOT_APPROVED',
  `reviewed_by` int(11) DEFAULT NULL,
  `reviewed_at` timestamp NULL DEFAULT NULL,
  `remarks` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`medical_id`),
  KEY `fk_med_reviewer` (`reviewed_by`),
  KEY `idx_med_student` (`student_id`),
  KEY `idx_med_course` (`course_id`),
  CONSTRAINT `fk_med_course` FOREIGN KEY (`course_id`) REFERENCES `courses` (`course_id`) ON DELETE CASCADE,
  CONSTRAINT `fk_med_reviewer` FOREIGN KEY (`reviewed_by`) REFERENCES `users` (`user_id`) ON DELETE SET NULL,
  CONSTRAINT `fk_med_student` FOREIGN KEY (`student_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------
-- Table structure for `course_materials`
-- --------------------------------------------------------
DROP TABLE IF EXISTS `course_materials`;
CREATE TABLE `course_materials` (
  `material_id` int(11) NOT NULL AUTO_INCREMENT,
  `course_id` int(11) NOT NULL,
  `lecturer_id` int(11) NOT NULL,
  `title` varchar(150) NOT NULL,
  `description` text DEFAULT NULL,
  `file_path` varchar(255) NOT NULL,
  `uploaded_at` timestamp NOT NULL DEFAULT current_timestamp(),
  PRIMARY KEY (`material_id`),
  KEY `fk_materials_lecturer` (`lecturer_id`),
  KEY `idx_materials_course` (`course_id`),
  CONSTRAINT `fk_materials_course` FOREIGN KEY (`course_id`) REFERENCES `courses` (`course_id`) ON DELETE CASCADE,
  CONSTRAINT `fk_materials_lecturer` FOREIGN KEY (`lecturer_id`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------
-- Table structure for `notices`
-- --------------------------------------------------------
DROP TABLE IF EXISTS `notices`;
CREATE TABLE `notices` (
  `notice_id` int(11) NOT NULL AUTO_INCREMENT,
  `title` varchar(200) NOT NULL,
  `content` text NOT NULL,
  `published_date` date NOT NULL,
  `created_by` int(11) DEFAULT NULL,
  `target_role` varchar(50) DEFAULT 'ALL',
  `status` enum('ACTIVE','ARCHIVED') DEFAULT 'ACTIVE',
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  PRIMARY KEY (`notice_id`),
  KEY `fk_notices_creator` (`created_by`),
  CONSTRAINT `fk_notices_creator` FOREIGN KEY (`created_by`) REFERENCES `users` (`user_id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------
-- Table structure for `timetables`
-- --------------------------------------------------------
DROP TABLE IF EXISTS `timetables`;
CREATE TABLE `timetables` (
  `timetable_id` int(11) NOT NULL AUTO_INCREMENT,
  `department_id` int(11) NOT NULL,
  `course_id` int(11) NOT NULL,
  `day_of_week` enum('MONDAY','TUESDAY','WEDNESDAY','THURSDAY','FRIDAY','SATURDAY','SUNDAY') NOT NULL,
  `start_time` time NOT NULL,
  `end_time` time NOT NULL,
  `room` varchar(50) NOT NULL,
  `lecturer_id` int(11) DEFAULT NULL,
  `session_type` enum('THEORY','PRACTICAL') NOT NULL DEFAULT 'THEORY',
  `semester` int(11) NOT NULL DEFAULT 1,
  `academic_year` varchar(20) NOT NULL DEFAULT '2025/2026',
  PRIMARY KEY (`timetable_id`),
  KEY `fk_tt_lecturer` (`lecturer_id`),
  KEY `idx_tt_dept` (`department_id`),
  KEY `idx_tt_course` (`course_id`),
  CONSTRAINT `fk_tt_course` FOREIGN KEY (`course_id`) REFERENCES `courses` (`course_id`) ON DELETE CASCADE,
  CONSTRAINT `fk_tt_department` FOREIGN KEY (`department_id`) REFERENCES `departments` (`department_id`) ON DELETE CASCADE,
  CONSTRAINT `fk_tt_lecturer` FOREIGN KEY (`lecturer_id`) REFERENCES `users` (`user_id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- ============================================================================
-- 2. DATA INSERTIONS (DML)
-- ============================================================================

-- --------------------------------------------------------
-- Data for table `roles`
-- --------------------------------------------------------
INSERT INTO `roles` (`role_id`, `role_name`, `description`) VALUES (1, 'Admin', 'System Administrator with full access to manage users, courses, notices, and timetables');
INSERT INTO `roles` (`role_id`, `role_name`, `description`) VALUES (2, 'Lecturer', 'Academic staff who upload marks, manage course materials, and view student progress');
INSERT INTO `roles` (`role_id`, `role_name`, `description`) VALUES (3, 'Technical Officer', 'Technical staff maintaining attendance, medical records, and lab timetables');
INSERT INTO `roles` (`role_id`, `role_name`, `description`) VALUES (4, 'Undergraduate', 'Student who views personal attendance, marks, grades, GPA, notices, and timetables');
-- (4 records)

-- --------------------------------------------------------
-- Data for table `departments`
-- --------------------------------------------------------
INSERT INTO `departments` (`department_id`, `department_code`, `department_name`, `description`) VALUES (1, 'ICT', 'Department of Information and Communication Technology', 'Focuses on software engineering, computer networks, and data systems');
INSERT INTO `departments` (`department_id`, `department_code`, `department_name`, `description`) VALUES (2, 'BST', 'Department of Bio Systems Technology', 'Focuses on agricultural systems, bio-processing, and environmental tech');
INSERT INTO `departments` (`department_id`, `department_code`, `department_name`, `description`) VALUES (3, 'ET', 'Department of Engineering Technology', 'Focuses on mechanical, electrical, and automation technologies');
INSERT INTO `departments` (`department_id`, `department_code`, `department_name`, `description`) VALUES (4, 'MDT', 'Department of Multidisciplinary Studies', 'Provides mathematics, management, and language courses');
-- (4 records)

-- --------------------------------------------------------
-- Data for table `academic_years`
-- --------------------------------------------------------
INSERT INTO `academic_years` (`academic_year`, `is_active`, `created_at`) VALUES ('2024', 0, '2026-11-03 05:50:18');
INSERT INTO `academic_years` (`academic_year`, `is_active`, `created_at`) VALUES ('2025', 0, '2026-11-03 05:50:18');
INSERT INTO `academic_years` (`academic_year`, `is_active`, `created_at`) VALUES ('2026', 1, '2026-11-03 05:50:18');
INSERT INTO `academic_years` (`academic_year`, `is_active`, `created_at`) VALUES ('2027', 0, '2026-11-03 05:50:18');
INSERT INTO `academic_years` (`academic_year`, `is_active`, `created_at`) VALUES ('2028', 0, '2026-11-03 05:50:18');
-- (5 records)

-- --------------------------------------------------------
-- Data for table `role_user_id_sequences`
-- --------------------------------------------------------
INSERT INTO `role_user_id_sequences` (`role_id`, `next_value`) VALUES (1, 2);
INSERT INTO `role_user_id_sequences` (`role_id`, `next_value`) VALUES (2, 35);
INSERT INTO `role_user_id_sequences` (`role_id`, `next_value`) VALUES (3, 5);
INSERT INTO `role_user_id_sequences` (`role_id`, `next_value`) VALUES (4, 39);
-- (4 records)

-- --------------------------------------------------------
-- Data for table `users`
-- --------------------------------------------------------
INSERT INTO `users` (`user_id`, `username`, `password_hash`, `role_id`, `full_name`, `email`, `phone`, `address`, `profile_pic`, `department_id`, `created_at`, `updated_at`, `status`, `student_reg_no`, `academic_year`, `current_semester`, `student_type`, `designation`, `office_room`, `assigned_lab`, `role_specific_id`) VALUES (1, 'admin', '240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9', 1, 'System Administrator', 'admin@fot.ruh.ac.lk', '0412223344', 'Faculty of Technology, University of Ruhuna, Karagoda Uyangoda', 'default_avatar.png', 1, '2026-11-01 01:01:30', '2026-11-03 03:37:53', 'ACTIVE', NULL, NULL, 1, 'REGULAR', 'Senior Assistant Registrar', NULL, NULL, '0001');
INSERT INTO `users` (`user_id`, `username`, `password_hash`, `role_id`, `full_name`, `email`, `phone`, `address`, `profile_pic`, `department_id`, `created_at`, `updated_at`, `status`, `student_reg_no`, `academic_year`, `current_semester`, `student_type`, `designation`, `office_room`, `assigned_lab`, `role_specific_id`) VALUES (2, 'lec_kamal', '7d6c6bc5905d43ec0492cb28d116315264b971a804369a3b8364e6b12f694297', 2, 'Dr. Kamal Perera', 'kamal@ict.ruh.ac.lk', '0771234501', 'No. 12, Galle Road, Matara', 'default_avatar.png', 1, '2026-11-01 01:01:30', '2026-11-03 03:37:53', 'ACTIVE', NULL, NULL, 1, 'REGULAR', 'Senior Lecturer Gr. I', 'FOT-ICT-301', NULL, '0001');
INSERT INTO `users` (`user_id`, `username`, `password_hash`, `role_id`, `full_name`, `email`, `phone`, `address`, `profile_pic`, `department_id`, `created_at`, `updated_at`, `status`, `student_reg_no`, `academic_year`, `current_semester`, `student_type`, `designation`, `office_room`, `assigned_lab`, `role_specific_id`) VALUES (3, 'lec_nimal', '7d6c6bc5905d43ec0492cb28d116315264b971a804369a3b8364e6b12f694297', 2, 'Prof. Nimal Silva', 'nimal@ict.ruh.ac.lk', '0771234502', 'No. 45, Beach Road, Mirissa', 'default_avatar.png', 1, '2026-11-01 01:01:30', '2026-11-03 03:37:53', 'ACTIVE', NULL, NULL, 1, 'REGULAR', 'Professor in Computing', 'FOT-ICT-302', NULL, '0002');
INSERT INTO `users` (`user_id`, `username`, `password_hash`, `role_id`, `full_name`, `email`, `phone`, `address`, `profile_pic`, `department_id`, `created_at`, `updated_at`, `status`, `student_reg_no`, `academic_year`, `current_semester`, `student_type`, `designation`, `office_room`, `assigned_lab`, `role_specific_id`) VALUES (4, 'lec_sunil', '7d6c6bc5905d43ec0492cb28d116315264b971a804369a3b8364e6b12f694297', 2, 'Dr. Sunil Fernando', 'sunil@bst.ruh.ac.lk', '0771234503', 'No. 88, Kandy Road, Kamburupitiya', 'default_avatar.png', 2, '2026-11-01 01:01:30', '2026-11-03 03:37:53', 'ACTIVE', NULL, NULL, 1, 'REGULAR', 'Senior Lecturer Gr. II', 'FOT-BST-201', NULL, '0003');
INSERT INTO `users` (`user_id`, `username`, `password_hash`, `role_id`, `full_name`, `email`, `phone`, `address`, `profile_pic`, `department_id`, `created_at`, `updated_at`, `status`, `student_reg_no`, `academic_year`, `current_semester`, `student_type`, `designation`, `office_room`, `assigned_lab`, `role_specific_id`) VALUES (5, 'lec_priyantha', '7d6c6bc5905d43ec0492cb28d116315264b971a804369a3b8364e6b12f694297', 2, 'Dr. Priyantha Jayasuriya', 'priyantha@et.ruh.ac.lk', '0771234504', 'No. 15, Station Road, Matara', 'default_avatar.png', 3, '2026-11-01 01:01:30', '2026-11-03 03:37:53', 'ACTIVE', NULL, NULL, 1, 'REGULAR', 'Head of Department', 'FOT-ET-101', NULL, '0004');
INSERT INTO `users` (`user_id`, `username`, `password_hash`, `role_id`, `full_name`, `email`, `phone`, `address`, `profile_pic`, `department_id`, `created_at`, `updated_at`, `status`, `student_reg_no`, `academic_year`, `current_semester`, `student_type`, `designation`, `office_room`, `assigned_lab`, `role_specific_id`) VALUES (6, 'lec_anoma', '7d6c6bc5905d43ec0492cb28d116315264b971a804369a3b8364e6b12f694297', 2, 'Ms. Anoma Wickramasinghe', 'anoma@mdt.ruh.ac.lk', '0771234505', 'No. 05, Temple Road, Matara', 'default_avatar.png', 4, '2026-11-01 01:01:30', '2026-11-03 03:37:53', 'ACTIVE', NULL, NULL, 1, 'REGULAR', 'Lecturer (Unconfirmed)', 'FOT-MDT-105', NULL, '0005');
INSERT INTO `users` (`user_id`, `username`, `password_hash`, `role_id`, `full_name`, `email`, `phone`, `address`, `profile_pic`, `department_id`, `created_at`, `updated_at`, `status`, `student_reg_no`, `academic_year`, `current_semester`, `student_type`, `designation`, `office_room`, `assigned_lab`, `role_specific_id`) VALUES (7, 'to_saman', '87e74427d50a07b454147b3367ae628e1e2c3b96a28d9e4402d093886d176f31', 3, 'Mr. Saman Kumara', 'saman.to@fot.ruh.ac.lk', '0714567801', 'No. 22, Hakmana Road, Matara', 'default_avatar.png', 1, '2026-11-01 01:01:30', '2026-11-03 05:03:25', 'ACTIVE', NULL, NULL, NULL, NULL, 'Technical Officer Gr. I', NULL, 'Computer Lab 01', '0001');
INSERT INTO `users` (`user_id`, `username`, `password_hash`, `role_id`, `full_name`, `email`, `phone`, `address`, `profile_pic`, `department_id`, `created_at`, `updated_at`, `status`, `student_reg_no`, `academic_year`, `current_semester`, `student_type`, `designation`, `office_room`, `assigned_lab`, `role_specific_id`) VALUES (8, 'to_ruwan', 'eca207865cbb46b28489cf3066a50be7520b22a0ec5f97305dcfd229f37c49bb', 3, 'Mr. Ruwan Bandara', 'ruwan.to@fot.ruh.ac.lk', '0714567802', 'No. 34, Akuressa Road, Kamburupitiya', 'default_avatar.png', 2, '2026-11-01 01:01:30', '2026-11-03 03:37:53', 'ACTIVE', NULL, NULL, 1, 'REGULAR', 'Technical Officer Gr. II', NULL, 'Biotechnology Lab', '0002');
INSERT INTO `users` (`user_id`, `username`, `password_hash`, `role_id`, `full_name`, `email`, `phone`, `address`, `profile_pic`, `department_id`, `created_at`, `updated_at`, `status`, `student_reg_no`, `academic_year`, `current_semester`, `student_type`, `designation`, `office_room`, `assigned_lab`, `role_specific_id`) VALUES (9, 'to_chaminda', 'eca207865cbb46b28489cf3066a50be7520b22a0ec5f97305dcfd229f37c49bb', 3, 'Mr. Chaminda Dissanayake', 'chaminda.to@fot.ruh.ac.lk', '0714567803', 'No. 56, Rahula Road, Matara', 'default_avatar.png', 3, '2026-11-01 01:01:30', '2026-11-03 03:37:53', 'ACTIVE', NULL, NULL, 1, 'REGULAR', 'Technical Officer Gr. I', NULL, 'Engineering Workshop', '0003');
INSERT INTO `users` (`user_id`, `username`, `password_hash`, `role_id`, `full_name`, `email`, `phone`, `address`, `profile_pic`, `department_id`, `created_at`, `updated_at`, `status`, `student_reg_no`, `academic_year`, `current_semester`, `student_type`, `designation`, `office_room`, `assigned_lab`, `role_specific_id`) VALUES (10, 'to_kasun', 'eca207865cbb46b28489cf3066a50be7520b22a0ec5f97305dcfd229f37c49bb', 3, 'Mr. Kasun Senanayake', 'kasun.to@fot.ruh.ac.lk', '0714567804', 'No. 78, Tangalle Road, Matara', 'default_avatar.png', 1, '2026-11-01 01:01:30', '2026-11-03 03:37:53', 'ACTIVE', NULL, NULL, 1, 'REGULAR', 'Technical Officer Gr. II', NULL, 'Network Systems Lab', '0004');
INSERT INTO `users` (`user_id`, `username`, `password_hash`, `role_id`, `full_name`, `email`, `phone`, `address`, `profile_pic`, `department_id`, `created_at`, `updated_at`, `status`, `student_reg_no`, `academic_year`, `current_semester`, `student_type`, `designation`, `office_room`, `assigned_lab`, `role_specific_id`) VALUES (11, 'tg20242001', 'cd73502828457d15655bbd7a63fb0bc8f2bef4f3343646419e474d69a500073e', 4, 'M.D.R.L. Nonis', 'nonis@fot.ruh.ac.lk', '0761112001', 'Faculty Hostel, Karagoda Uyangoda', 'default_avatar.png', 1, '2026-11-01 01:01:30', '2026-11-03 03:37:53', 'ACTIVE', 'TG/2024/2001', '2024/2025', 1, 'REGULAR', NULL, NULL, NULL, '0001');
INSERT INTO `users` (`user_id`, `username`, `password_hash`, `role_id`, `full_name`, `email`, `phone`, `address`, `profile_pic`, `department_id`, `created_at`, `updated_at`, `status`, `student_reg_no`, `academic_year`, `current_semester`, `student_type`, `designation`, `office_room`, `assigned_lab`, `role_specific_id`) VALUES (12, 'tg20242002', '3c52d99e4aa985f94d09c0aa0a27fdd3730dfe80366b80c0f73a72588a6396eb', 4, 'Anuda Pasandul', 'anuda@fot.ruh.ac.lk', '0761112002', 'No. 18, Walasmulla Road, Beliatta', 'default_avatar.png', 1, '2026-11-01 01:01:30', '2026-11-03 05:15:40', 'ACTIVE', 'TG/2024/2002', '2024/2025', 1, 'REGULAR', NULL, NULL, NULL, '0002');
INSERT INTO `users` (`user_id`, `username`, `password_hash`, `role_id`, `full_name`, `email`, `phone`, `address`, `profile_pic`, `department_id`, `created_at`, `updated_at`, `status`, `student_reg_no`, `academic_year`, `current_semester`, `student_type`, `designation`, `office_room`, `assigned_lab`, `role_specific_id`) VALUES (13, 'tg20242003', 'cd73502828457d15655bbd7a63fb0bc8f2bef4f3343646419e474d69a500073e', 4, 'Sahan Rashmika', 'sahan@fot.ruh.ac.lk', '0761112003', 'No. 29, Broadway, Matara', 'default_avatar.png', 1, '2026-11-01 01:01:30', '2026-11-03 03:37:53', 'ACTIVE', 'TG/2024/2003', '2024/2025', 1, 'REGULAR', NULL, NULL, NULL, '0003');
INSERT INTO `users` (`user_id`, `username`, `password_hash`, `role_id`, `full_name`, `email`, `phone`, `address`, `profile_pic`, `department_id`, `created_at`, `updated_at`, `status`, `student_reg_no`, `academic_year`, `current_semester`, `student_type`, `designation`, `office_room`, `assigned_lab`, `role_specific_id`) VALUES (14, 'tg20242004', 'cd73502828457d15655bbd7a63fb0bc8f2bef4f3343646419e474d69a500073e', 4, 'H.R.H. Nethsara', 'nethsara@fot.ruh.ac.lk', '0761112004', 'No. 40, High Level Road, Colombo', 'default_avatar.png', 1, '2026-11-01 01:01:30', '2026-11-03 03:37:53', 'ACTIVE', 'TG/2024/2004', '2024/2025', 1, 'REGULAR', NULL, NULL, NULL, '0004');
INSERT INTO `users` (`user_id`, `username`, `password_hash`, `role_id`, `full_name`, `email`, `phone`, `address`, `profile_pic`, `department_id`, `created_at`, `updated_at`, `status`, `student_reg_no`, `academic_year`, `current_semester`, `student_type`, `designation`, `office_room`, `assigned_lab`, `role_specific_id`) VALUES (15, 'tg20242005', 'cd73502828457d15655bbd7a63fb0bc8f2bef4f3343646419e474d69a500073e', 4, 'Kaveesha Gimhan', 'kaveesha@fot.ruh.ac.lk', '0761112005', 'No. 12, Lake Round, Kandy', 'default_avatar.png', 1, '2026-11-01 01:01:30', '2026-11-03 03:37:53', 'ACTIVE', 'TG/2024/2005', '2024/2025', 1, 'REGULAR', NULL, NULL, NULL, '0005');
INSERT INTO `users` (`user_id`, `username`, `password_hash`, `role_id`, `full_name`, `email`, `phone`, `address`, `profile_pic`, `department_id`, `created_at`, `updated_at`, `status`, `student_reg_no`, `academic_year`, `current_semester`, `student_type`, `designation`, `office_room`, `assigned_lab`, `role_specific_id`) VALUES (16, 'tg20242006', 'cd73502828457d15655bbd7a63fb0bc8f2bef4f3343646419e474d69a500073e', 4, 'Dilshan Madushanka', 'dilshan@fot.ruh.ac.lk', '0761112006', 'No. 55, Beach Road, Tangalle', 'default_avatar.png', 1, '2026-11-01 01:01:30', '2026-11-03 03:37:53', 'ACTIVE', 'TG/2024/2006', '2024/2025', 1, 'REGULAR', NULL, NULL, NULL, '0006');
INSERT INTO `users` (`user_id`, `username`, `password_hash`, `role_id`, `full_name`, `email`, `phone`, `address`, `profile_pic`, `department_id`, `created_at`, `updated_at`, `status`, `student_reg_no`, `academic_year`, `current_semester`, `student_type`, `designation`, `office_room`, `assigned_lab`, `role_specific_id`) VALUES (17, 'tg20242007', 'cd73502828457d15655bbd7a63fb0bc8f2bef4f3343646419e474d69a500073e', 4, 'Tharindu Malshan', 'tharindu@fot.ruh.ac.lk', '0761112007', 'No. 89, Main Street, Galle', 'default_avatar.png', 1, '2026-11-01 01:01:30', '2026-11-03 03:37:53', 'ACTIVE', 'TG/2024/2007', '2024/2025', 1, 'REGULAR', NULL, NULL, NULL, '0007');
INSERT INTO `users` (`user_id`, `username`, `password_hash`, `role_id`, `full_name`, `email`, `phone`, `address`, `profile_pic`, `department_id`, `created_at`, `updated_at`, `status`, `student_reg_no`, `academic_year`, `current_semester`, `student_type`, `designation`, `office_room`, `assigned_lab`, `role_specific_id`) VALUES (18, 'tg20242008', 'cd73502828457d15655bbd7a63fb0bc8f2bef4f3343646419e474d69a500073e', 4, 'Sanduni Tharaka', 'sanduni@fot.ruh.ac.lk', '0761112008', 'Girls Hostel 02, Karagoda Uyangoda', 'default_avatar.png', 1, '2026-11-01 01:01:30', '2026-11-03 03:37:53', 'ACTIVE', 'TG/2024/2008', '2024/2025', 1, 'REGULAR', NULL, NULL, NULL, '0008');
INSERT INTO `users` (`user_id`, `username`, `password_hash`, `role_id`, `full_name`, `email`, `phone`, `address`, `profile_pic`, `department_id`, `created_at`, `updated_at`, `status`, `student_reg_no`, `academic_year`, `current_semester`, `student_type`, `designation`, `office_room`, `assigned_lab`, `role_specific_id`) VALUES (19, 'tg20242009', 'cd73502828457d15655bbd7a63fb0bc8f2bef4f3343646419e474d69a500073e', 4, 'Ishara Jayamini', 'ishara@fot.ruh.ac.lk', '0761112009', 'No. 31, Cross Street, Matara', 'default_avatar.png', 1, '2026-11-01 01:01:30', '2026-11-03 03:37:53', 'ACTIVE', 'TG/2024/2009', '2024/2025', 1, 'REGULAR', NULL, NULL, NULL, '0009');
INSERT INTO `users` (`user_id`, `username`, `password_hash`, `role_id`, `full_name`, `email`, `phone`, `address`, `profile_pic`, `department_id`, `created_at`, `updated_at`, `status`, `student_reg_no`, `academic_year`, `current_semester`, `student_type`, `designation`, `office_room`, `assigned_lab`, `role_specific_id`) VALUES (20, 'tg20242010', 'cd73502828457d15655bbd7a63fb0bc8f2bef4f3343646419e474d69a500073e', 4, 'Bhanuka Rajapaksha', 'bhanuka@fot.ruh.ac.lk', '0761112010', 'No. 77, Fort, Galle', 'default_avatar.png', 1, '2026-11-01 01:01:30', '2026-11-03 03:37:53', 'ACTIVE', 'TG/2024/2010', '2024/2025', 1, 'REGULAR', NULL, NULL, NULL, '0010');
INSERT INTO `users` (`user_id`, `username`, `password_hash`, `role_id`, `full_name`, `email`, `phone`, `address`, `profile_pic`, `department_id`, `created_at`, `updated_at`, `status`, `student_reg_no`, `academic_year`, `current_semester`, `student_type`, `designation`, `office_room`, `assigned_lab`, `role_specific_id`) VALUES (21, 'tg20242011', 'cd73502828457d15655bbd7a63fb0bc8f2bef4f3343646419e474d69a500073e', 4, 'Oshada Kavinda', 'oshada@fot.ruh.ac.lk', '0761112011', 'No. 64, Samagi Mawatha, Matara', 'default_avatar.png', 1, '2026-11-01 01:01:30', '2026-11-03 03:37:53', 'ACTIVE', 'TG/2024/2011', '2024/2025', 1, 'REGULAR', NULL, NULL, NULL, '0011');
INSERT INTO `users` (`user_id`, `username`, `password_hash`, `role_id`, `full_name`, `email`, `phone`, `address`, `profile_pic`, `department_id`, `created_at`, `updated_at`, `status`, `student_reg_no`, `academic_year`, `current_semester`, `student_type`, `designation`, `office_room`, `assigned_lab`, `role_specific_id`) VALUES (22, 'tg20242012', 'cd73502828457d15655bbd7a63fb0bc8f2bef4f3343646419e474d69a500073e', 4, 'Chathura Prabath', 'chathura@fot.ruh.ac.lk', '0761112012', 'No. 15, Hospital Road, Kamburupitiya', 'default_avatar.png', 1, '2026-11-01 01:01:30', '2026-11-03 03:37:53', 'ACTIVE', 'TG/2024/2012', '2024/2025', 1, 'REGULAR', NULL, NULL, NULL, '0012');
INSERT INTO `users` (`user_id`, `username`, `password_hash`, `role_id`, `full_name`, `email`, `phone`, `address`, `profile_pic`, `department_id`, `created_at`, `updated_at`, `status`, `student_reg_no`, `academic_year`, `current_semester`, `student_type`, `designation`, `office_room`, `assigned_lab`, `role_specific_id`) VALUES (23, 'tg20242013', 'cd73502828457d15655bbd7a63fb0bc8f2bef4f3343646419e474d69a500073e', 4, 'Nimesha Sewwandi', 'nimesha@fot.ruh.ac.lk', '0761112013', 'Girls Hostel 01, Karagoda Uyangoda', 'default_avatar.png', 1, '2026-11-01 01:01:30', '2026-11-03 03:37:53', 'ACTIVE', 'TG/2024/2013', '2024/2025', 1, 'REGULAR', NULL, NULL, NULL, '0013');
INSERT INTO `users` (`user_id`, `username`, `password_hash`, `role_id`, `full_name`, `email`, `phone`, `address`, `profile_pic`, `department_id`, `created_at`, `updated_at`, `status`, `student_reg_no`, `academic_year`, `current_semester`, `student_type`, `designation`, `office_room`, `assigned_lab`, `role_specific_id`) VALUES (24, 'tg20242014', 'cd73502828457d15655bbd7a63fb0bc8f2bef4f3343646419e474d69a500073e', 4, 'Janith Pramod', 'janith@fot.ruh.ac.lk', '0761112014', 'No. 09, Station Road, Weligama', 'default_avatar.png', 1, '2026-11-01 01:01:30', '2026-11-03 03:37:53', 'ACTIVE', 'TG/2024/2014', '2024/2025', 1, 'REGULAR', NULL, NULL, NULL, '0014');
INSERT INTO `users` (`user_id`, `username`, `password_hash`, `role_id`, `full_name`, `email`, `phone`, `address`, `profile_pic`, `department_id`, `created_at`, `updated_at`, `status`, `student_reg_no`, `academic_year`, `current_semester`, `student_type`, `designation`, `office_room`, `assigned_lab`, `role_specific_id`) VALUES (25, 'tg20242015', 'cd73502828457d15655bbd7a63fb0bc8f2bef4f3343646419e474d69a500073e', 4, 'Sachini Kaushalya', 'sachini@fot.ruh.ac.lk', '0761112015', 'No. 44, Hill Street, Matara', 'default_avatar.png', 1, '2026-11-01 01:01:30', '2026-11-03 03:37:53', 'ACTIVE', 'TG/2024/2015', '2024/2025', 1, 'REGULAR', NULL, NULL, NULL, '0015');
INSERT INTO `users` (`user_id`, `username`, `password_hash`, `role_id`, `full_name`, `email`, `phone`, `address`, `profile_pic`, `department_id`, `created_at`, `updated_at`, `status`, `student_reg_no`, `academic_year`, `current_semester`, `student_type`, `designation`, `office_room`, `assigned_lab`, `role_specific_id`) VALUES (26, 'tg20242016', 'cd73502828457d15655bbd7a63fb0bc8f2bef4f3343646419e474d69a500073e', 4, 'Amila Nuwan', 'amila@fot.ruh.ac.lk', '0761112016', 'No. 102, Colombo Road, Bentota', 'default_avatar.png', 1, '2026-11-01 01:01:30', '2026-11-03 03:37:53', 'ACTIVE', 'TG/2024/2016', '2024/2025', 1, 'REGULAR', NULL, NULL, NULL, '0016');
INSERT INTO `users` (`user_id`, `username`, `password_hash`, `role_id`, `full_name`, `email`, `phone`, `address`, `profile_pic`, `department_id`, `created_at`, `updated_at`, `status`, `student_reg_no`, `academic_year`, `current_semester`, `student_type`, `designation`, `office_room`, `assigned_lab`, `role_specific_id`) VALUES (27, 'tg20242017', 'cd73502828457d15655bbd7a63fb0bc8f2bef4f3343646419e474d69a500073e', 4, 'Dinuka Lakshan', 'dinuka.l@fot.ruh.ac.lk', '0761112017', 'No. 38, Galle Road, Ambalangoda', 'default_avatar.png', 1, '2026-11-01 01:01:30', '2026-11-03 03:37:53', 'ACTIVE', 'TG/2024/2017', '2024/2025', 1, 'REGULAR', NULL, NULL, NULL, '0017');
INSERT INTO `users` (`user_id`, `username`, `password_hash`, `role_id`, `full_name`, `email`, `phone`, `address`, `profile_pic`, `department_id`, `created_at`, `updated_at`, `status`, `student_reg_no`, `academic_year`, `current_semester`, `student_type`, `designation`, `office_room`, `assigned_lab`, `role_specific_id`) VALUES (28, 'tg20242018', 'cd73502828457d15655bbd7a63fb0bc8f2bef4f3343646419e474d69a500073e', 4, 'Pawani Hansika', 'pawani@fot.ruh.ac.lk', '0761112018', 'Girls Hostel 02, Karagoda Uyangoda', 'default_avatar.png', 1, '2026-11-01 01:01:30', '2026-11-03 03:37:53', 'ACTIVE', 'TG/2024/2018', '2024/2025', 1, 'REGULAR', NULL, NULL, NULL, '0018');
INSERT INTO `users` (`user_id`, `username`, `password_hash`, `role_id`, `full_name`, `email`, `phone`, `address`, `profile_pic`, `department_id`, `created_at`, `updated_at`, `status`, `student_reg_no`, `academic_year`, `current_semester`, `student_type`, `designation`, `office_room`, `assigned_lab`, `role_specific_id`) VALUES (29, 'tg20231850', 'cd73502828457d15655bbd7a63fb0bc8f2bef4f3343646419e474d69a500073e', 4, 'P.L. Kasun Wijesinghe', 'kasun.w@fot.ruh.ac.lk', '0761111850', 'No. 51, Hakmana Road, Matara', 'default_avatar.png', 1, '2026-11-01 01:01:30', '2026-11-03 03:37:53', 'ACTIVE', 'TG/2023/1850', '2024/2025', 1, 'REPEAT', NULL, NULL, NULL, '0019');
INSERT INTO `users` (`user_id`, `username`, `password_hash`, `role_id`, `full_name`, `email`, `phone`, `address`, `profile_pic`, `department_id`, `created_at`, `updated_at`, `status`, `student_reg_no`, `academic_year`, `current_semester`, `student_type`, `designation`, `office_room`, `assigned_lab`, `role_specific_id`) VALUES (30, 'tg20231855', 'cd73502828457d15655bbd7a63fb0bc8f2bef4f3343646419e474d69a500073e', 4, 'K.M. Sachith Alwis', 'sachith.a@fot.ruh.ac.lk', '0761111855', 'No. 19, Beach Road, Dickwella', 'default_avatar.png', 1, '2026-11-01 01:01:30', '2026-11-03 03:37:53', 'ACTIVE', 'TG/2023/1855', '2024/2025', 1, 'REPEAT', NULL, NULL, NULL, '0020');
INSERT INTO `users` (`user_id`, `username`, `password_hash`, `role_id`, `full_name`, `email`, `phone`, `address`, `profile_pic`, `department_id`, `created_at`, `updated_at`, `status`, `student_reg_no`, `academic_year`, `current_semester`, `student_type`, `designation`, `office_room`, `assigned_lab`, `role_specific_id`) VALUES (31, 'tg20221510', 'cd73502828457d15655bbd7a63fb0bc8f2bef4f3343646419e474d69a500073e', 4, 'R.M. Dinuka Bandara', 'dinuka.b@fot.ruh.ac.lk', '0761111510', 'No. 73, Kandy Road, Kurunegala', 'default_avatar.png', 1, '2026-11-01 01:01:30', '2026-11-03 03:37:53', 'ACTIVE', 'TG/2022/1510', '2024/2025', 1, 'BATCH_MISSED', NULL, NULL, NULL, '0021');
INSERT INTO `users` (`user_id`, `username`, `password_hash`, `role_id`, `full_name`, `email`, `phone`, `address`, `profile_pic`, `department_id`, `created_at`, `updated_at`, `status`, `student_reg_no`, `academic_year`, `current_semester`, `student_type`, `designation`, `office_room`, `assigned_lab`, `role_specific_id`) VALUES (32, 'tg20221520', 'cd73502828457d15655bbd7a63fb0bc8f2bef4f3343646419e474d69a500073e', 4, 'W.T. Nuwan Jayalath', 'nuwan.j@fot.ruh.ac.lk', '0761111520', 'No. 82, Badulla Road, Bandarawela', 'default_avatar.png', 1, '2026-11-01 01:01:30', '2026-11-03 03:37:53', 'ACTIVE', 'TG/2022/1520', '2024/2025', 1, 'BATCH_MISSED', NULL, NULL, NULL, '0022');
INSERT INTO `users` (`user_id`, `username`, `password_hash`, `role_id`, `full_name`, `email`, `phone`, `address`, `profile_pic`, `department_id`, `created_at`, `updated_at`, `status`, `student_reg_no`, `academic_year`, `current_semester`, `student_type`, `designation`, `office_room`, `assigned_lab`, `role_specific_id`) VALUES (34, 'lec_sahan', 'f302e3f7418eae0ea0ed8c89e3dceb473476ee038276ca8a1f0cf583567b8633', 2, 'sahan rashmika', 'sahan@gmail.com', '0701948371', 'weligama,matara.', 'default_avatar.png', 1, '2026-11-01 01:15:54', '2026-11-03 16:11:18', 'ACTIVE', NULL, NULL, NULL, NULL, '', '', NULL, '0006');
INSERT INTO `users` (`user_id`, `username`, `password_hash`, `role_id`, `full_name`, `email`, `phone`, `address`, `profile_pic`, `department_id`, `created_at`, `updated_at`, `status`, `student_reg_no`, `academic_year`, `current_semester`, `student_type`, `designation`, `office_room`, `assigned_lab`, `role_specific_id`) VALUES (35, 'tg20242108', 'a7d57c7d7e6b5c5609cbce9b598e502575101d2e0ebb3400d5a91bc96013fe79', 4, 'geeth chamikara', 'geeth@gmail.com', '0771597824', 'dikwalla', 'default_avatar.png', 1, '2026-11-02 02:23:08', '2026-11-03 03:37:53', 'ACTIVE', 'tg20242108', '2024/2025', 1, 'REGULAR', NULL, NULL, NULL, '0023');
INSERT INTO `users` (`user_id`, `username`, `password_hash`, `role_id`, `full_name`, `email`, `phone`, `address`, `profile_pic`, `department_id`, `created_at`, `updated_at`, `status`, `student_reg_no`, `academic_year`, `current_semester`, `student_type`, `designation`, `office_room`, `assigned_lab`, `role_specific_id`) VALUES (36, 'lec_pradeep', '88d11e52e9ce89aa80722a18a2a9238ff998ec7159945f3219bfc07e2c6e59f8', 2, 'Dr. Pradeep Kumara', 'pradeep@ict.ruh.ac.lk', '0771234505', NULL, 'default_avatar.png', 1, '2026-11-03 15:34:44', '2026-11-03 15:34:44', 'ACTIVE', NULL, NULL, 1, 'REGULAR', 'Senior Lecturer Gr. II', 'FOT-ICT-303', NULL, '0007');
INSERT INTO `users` (`user_id`, `username`, `password_hash`, `role_id`, `full_name`, `email`, `phone`, `address`, `profile_pic`, `department_id`, `created_at`, `updated_at`, `status`, `student_reg_no`, `academic_year`, `current_semester`, `student_type`, `designation`, `office_room`, `assigned_lab`, `role_specific_id`) VALUES (37, 'lec_chandima', '88d11e52e9ce89aa80722a18a2a9238ff998ec7159945f3219bfc07e2c6e59f8', 2, 'Ms. Chandima Perera', 'chandima@ict.ruh.ac.lk', '0771234506', NULL, 'default_avatar.png', 1, '2026-11-03 15:34:44', '2026-11-03 15:34:44', 'ACTIVE', NULL, NULL, 1, 'REGULAR', 'Assistant Lecturer', 'FOT-ICT-304', NULL, '0008');
INSERT INTO `users` (`user_id`, `username`, `password_hash`, `role_id`, `full_name`, `email`, `phone`, `address`, `profile_pic`, `department_id`, `created_at`, `updated_at`, `status`, `student_reg_no`, `academic_year`, `current_semester`, `student_type`, `designation`, `office_room`, `assigned_lab`, `role_specific_id`) VALUES (38, 'lec_chamara', '88d11e52e9ce89aa80722a18a2a9238ff998ec7159945f3219bfc07e2c6e59f8', 2, 'Dr. Chamara Wickramasinghe', 'chamara@bst.ruh.ac.lk', '0771234507', NULL, 'default_avatar.png', 2, '2026-11-03 15:34:44', '2026-11-03 15:34:44', 'ACTIVE', NULL, NULL, 1, 'REGULAR', 'Senior Lecturer Gr. I', 'FOT-BST-202', NULL, '0009');
INSERT INTO `users` (`user_id`, `username`, `password_hash`, `role_id`, `full_name`, `email`, `phone`, `address`, `profile_pic`, `department_id`, `created_at`, `updated_at`, `status`, `student_reg_no`, `academic_year`, `current_semester`, `student_type`, `designation`, `office_room`, `assigned_lab`, `role_specific_id`) VALUES (39, 'lec_malathi', '88d11e52e9ce89aa80722a18a2a9238ff998ec7159945f3219bfc07e2c6e59f8', 2, 'Ms. Malathi Jayasinghe', 'malathi@bst.ruh.ac.lk', '0771234508', NULL, 'default_avatar.png', 2, '2026-11-03 15:34:44', '2026-11-03 15:34:44', 'ACTIVE', NULL, NULL, 1, 'REGULAR', 'Lecturer', 'FOT-BST-203', NULL, '0010');
INSERT INTO `users` (`user_id`, `username`, `password_hash`, `role_id`, `full_name`, `email`, `phone`, `address`, `profile_pic`, `department_id`, `created_at`, `updated_at`, `status`, `student_reg_no`, `academic_year`, `current_semester`, `student_type`, `designation`, `office_room`, `assigned_lab`, `role_specific_id`) VALUES (40, 'lec_roshan', '88d11e52e9ce89aa80722a18a2a9238ff998ec7159945f3219bfc07e2c6e59f8', 2, 'Mr. Roshan Bandara', 'roshan@bst.ruh.ac.lk', '0771234509', NULL, 'default_avatar.png', 2, '2026-11-03 15:34:44', '2026-11-03 15:34:44', 'ACTIVE', NULL, NULL, 1, 'REGULAR', 'Assistant Lecturer', 'FOT-BST-204', NULL, '0011');
INSERT INTO `users` (`user_id`, `username`, `password_hash`, `role_id`, `full_name`, `email`, `phone`, `address`, `profile_pic`, `department_id`, `created_at`, `updated_at`, `status`, `student_reg_no`, `academic_year`, `current_semester`, `student_type`, `designation`, `office_room`, `assigned_lab`, `role_specific_id`) VALUES (41, 'lec_thilini', '88d11e52e9ce89aa80722a18a2a9238ff998ec7159945f3219bfc07e2c6e59f8', 2, 'Ms. Thilini Jayakody', 'thilini@bst.ruh.ac.lk', '0771234510', NULL, 'default_avatar.png', 2, '2026-11-03 15:34:44', '2026-11-03 15:34:44', 'ACTIVE', NULL, NULL, 1, 'REGULAR', 'Lecturer', 'FOT-BST-205', NULL, '0012');
INSERT INTO `users` (`user_id`, `username`, `password_hash`, `role_id`, `full_name`, `email`, `phone`, `address`, `profile_pic`, `department_id`, `created_at`, `updated_at`, `status`, `student_reg_no`, `academic_year`, `current_semester`, `student_type`, `designation`, `office_room`, `assigned_lab`, `role_specific_id`) VALUES (42, 'lec_asela', '88d11e52e9ce89aa80722a18a2a9238ff998ec7159945f3219bfc07e2c6e59f8', 2, 'Dr. Asela Madugalle', 'asela@et.ruh.ac.lk', '0771234511', NULL, 'default_avatar.png', 3, '2026-11-03 15:34:44', '2026-11-03 15:34:44', 'ACTIVE', NULL, NULL, 1, 'REGULAR', 'Senior Lecturer Gr. II', 'FOT-ET-102', NULL, '0013');
INSERT INTO `users` (`user_id`, `username`, `password_hash`, `role_id`, `full_name`, `email`, `phone`, `address`, `profile_pic`, `department_id`, `created_at`, `updated_at`, `status`, `student_reg_no`, `academic_year`, `current_semester`, `student_type`, `designation`, `office_room`, `assigned_lab`, `role_specific_id`) VALUES (43, 'lec_nalaka', '88d11e52e9ce89aa80722a18a2a9238ff998ec7159945f3219bfc07e2c6e59f8', 2, 'Mr. Nalaka Gunaratne', 'nalaka@et.ruh.ac.lk', '0771234512', NULL, 'default_avatar.png', 3, '2026-11-03 15:34:44', '2026-11-03 15:34:44', 'ACTIVE', NULL, NULL, 1, 'REGULAR', 'Lecturer', 'FOT-ET-103', NULL, '0014');
INSERT INTO `users` (`user_id`, `username`, `password_hash`, `role_id`, `full_name`, `email`, `phone`, `address`, `profile_pic`, `department_id`, `created_at`, `updated_at`, `status`, `student_reg_no`, `academic_year`, `current_semester`, `student_type`, `designation`, `office_room`, `assigned_lab`, `role_specific_id`) VALUES (44, 'lec_dilrukshi', '88d11e52e9ce89aa80722a18a2a9238ff998ec7159945f3219bfc07e2c6e59f8', 2, 'Ms. Dilrukshi Senanayake', 'dilrukshi@et.ruh.ac.lk', '0771234513', NULL, 'default_avatar.png', 3, '2026-11-03 15:34:44', '2026-11-03 15:34:44', 'ACTIVE', NULL, NULL, 1, 'REGULAR', 'Assistant Lecturer', 'FOT-ET-104', NULL, '0015');
INSERT INTO `users` (`user_id`, `username`, `password_hash`, `role_id`, `full_name`, `email`, `phone`, `address`, `profile_pic`, `department_id`, `created_at`, `updated_at`, `status`, `student_reg_no`, `academic_year`, `current_semester`, `student_type`, `designation`, `office_room`, `assigned_lab`, `role_specific_id`) VALUES (45, 'lec_sanjaya', '88d11e52e9ce89aa80722a18a2a9238ff998ec7159945f3219bfc07e2c6e59f8', 2, 'Mr. Sanjaya Ranasinghe', 'sanjaya@et.ruh.ac.lk', '0771234514', NULL, 'default_avatar.png', 3, '2026-11-03 15:34:44', '2026-11-03 15:34:44', 'ACTIVE', NULL, NULL, 1, 'REGULAR', 'Lecturer', 'FOT-ET-105', NULL, '0016');
INSERT INTO `users` (`user_id`, `username`, `password_hash`, `role_id`, `full_name`, `email`, `phone`, `address`, `profile_pic`, `department_id`, `created_at`, `updated_at`, `status`, `student_reg_no`, `academic_year`, `current_semester`, `student_type`, `designation`, `office_room`, `assigned_lab`, `role_specific_id`) VALUES (46, 'lec_sandamali', '88d11e52e9ce89aa80722a18a2a9238ff998ec7159945f3219bfc07e2c6e59f8', 2, 'Ms. Sandamali Wijeratne', 'sandamali@mdt.ruh.ac.lk', '0771234515', NULL, 'default_avatar.png', 4, '2026-11-03 15:34:44', '2026-11-03 15:34:44', 'ACTIVE', NULL, NULL, 1, 'REGULAR', 'Senior Lecturer', 'FOT-MDT-101', NULL, '0017');
INSERT INTO `users` (`user_id`, `username`, `password_hash`, `role_id`, `full_name`, `email`, `phone`, `address`, `profile_pic`, `department_id`, `created_at`, `updated_at`, `status`, `student_reg_no`, `academic_year`, `current_semester`, `student_type`, `designation`, `office_room`, `assigned_lab`, `role_specific_id`) VALUES (47, 'lec_harsha', '88d11e52e9ce89aa80722a18a2a9238ff998ec7159945f3219bfc07e2c6e59f8', 2, 'Mr. Harsha Abeywickrama', 'harsha@mdt.ruh.ac.lk', '0771234516', NULL, 'default_avatar.png', 4, '2026-11-03 15:34:44', '2026-11-03 15:34:44', 'ACTIVE', NULL, NULL, 1, 'REGULAR', 'Lecturer', 'FOT-MDT-102', NULL, '0018');
INSERT INTO `users` (`user_id`, `username`, `password_hash`, `role_id`, `full_name`, `email`, `phone`, `address`, `profile_pic`, `department_id`, `created_at`, `updated_at`, `status`, `student_reg_no`, `academic_year`, `current_semester`, `student_type`, `designation`, `office_room`, `assigned_lab`, `role_specific_id`) VALUES (48, 'lec_vindya', '88d11e52e9ce89aa80722a18a2a9238ff998ec7159945f3219bfc07e2c6e59f8', 2, 'Ms. Vindya Ratnayake', 'vindya@mdt.ruh.ac.lk', '0771234517', NULL, 'default_avatar.png', 4, '2026-11-03 15:34:44', '2026-11-03 15:34:44', 'ACTIVE', NULL, NULL, 1, 'REGULAR', 'Assistant Lecturer', 'FOT-MDT-103', NULL, '0019');
INSERT INTO `users` (`user_id`, `username`, `password_hash`, `role_id`, `full_name`, `email`, `phone`, `address`, `profile_pic`, `department_id`, `created_at`, `updated_at`, `status`, `student_reg_no`, `academic_year`, `current_semester`, `student_type`, `designation`, `office_room`, `assigned_lab`, `role_specific_id`) VALUES (49, 'lec_sampath', '88d11e52e9ce89aa80722a18a2a9238ff998ec7159945f3219bfc07e2c6e59f8', 2, 'Mr. Sampath Dissanayake', 'sampath@mdt.ruh.ac.lk', '0771234518', NULL, 'default_avatar.png', 4, '2026-11-03 15:34:44', '2026-11-03 15:34:44', 'ACTIVE', NULL, NULL, 1, 'REGULAR', 'Lecturer', 'FOT-MDT-104', NULL, '0020');
INSERT INTO `users` (`user_id`, `username`, `password_hash`, `role_id`, `full_name`, `email`, `phone`, `address`, `profile_pic`, `department_id`, `created_at`, `updated_at`, `status`, `student_reg_no`, `academic_year`, `current_semester`, `student_type`, `designation`, `office_room`, `assigned_lab`, `role_specific_id`) VALUES (50, 'bs20242101', 'b2a1f4fd0a460606b34c8913e2981dac8d2e283d778aba586c416ee2629bfa54', 4, 'W.A. Ishara Madushani', 'ishara.m@fot.ruh.ac.lk', '0762221001', 'No. 22, Temple Road, Embilipitiya', 'default_avatar.png', 2, '2026-11-03 15:41:08', '2026-11-03 15:41:08', 'ACTIVE', 'TG/2024/2101', '2024/2025', 1, 'REGULAR', NULL, NULL, NULL, '0024');
INSERT INTO `users` (`user_id`, `username`, `password_hash`, `role_id`, `full_name`, `email`, `phone`, `address`, `profile_pic`, `department_id`, `created_at`, `updated_at`, `status`, `student_reg_no`, `academic_year`, `current_semester`, `student_type`, `designation`, `office_room`, `assigned_lab`, `role_specific_id`) VALUES (51, 'bs20242102', 'b2a1f4fd0a460606b34c8913e2981dac8d2e283d778aba586c416ee2629bfa54', 4, 'D.M. Kasun Pathirana', 'kasun.p@fot.ruh.ac.lk', '0762221002', 'No. 45, Main Street, Embilipitiya', 'default_avatar.png', 2, '2026-11-03 15:41:08', '2026-11-03 15:41:08', 'ACTIVE', 'TG/2024/2102', '2024/2025', 1, 'REGULAR', NULL, NULL, NULL, '0025');
INSERT INTO `users` (`user_id`, `username`, `password_hash`, `role_id`, `full_name`, `email`, `phone`, `address`, `profile_pic`, `department_id`, `created_at`, `updated_at`, `status`, `student_reg_no`, `academic_year`, `current_semester`, `student_type`, `designation`, `office_room`, `assigned_lab`, `role_specific_id`) VALUES (52, 'bs20242103', 'b2a1f4fd0a460606b34c8913e2981dac8d2e283d778aba586c416ee2629bfa54', 4, 'P.K. Sewwandi Darshani', 'sewwandi@fot.ruh.ac.lk', '0762221003', 'Girls Hostel 01, Karagoda Uyangoda', 'default_avatar.png', 2, '2026-11-03 15:41:08', '2026-11-03 15:41:08', 'ACTIVE', 'TG/2024/2103', '2024/2025', 1, 'REGULAR', NULL, NULL, NULL, '0026');
INSERT INTO `users` (`user_id`, `username`, `password_hash`, `role_id`, `full_name`, `email`, `phone`, `address`, `profile_pic`, `department_id`, `created_at`, `updated_at`, `status`, `student_reg_no`, `academic_year`, `current_semester`, `student_type`, `designation`, `office_room`, `assigned_lab`, `role_specific_id`) VALUES (53, 'bs20242104', 'b2a1f4fd0a460606b34c8913e2981dac8d2e283d778aba586c416ee2629bfa54', 4, 'H.M. Ruwan Rathnayaka', 'ruwan.r@fot.ruh.ac.lk', '0762221004', 'No. 76, Baddegama Road, Galle', 'default_avatar.png', 2, '2026-11-03 15:41:08', '2026-11-03 15:41:08', 'ACTIVE', 'TG/2024/2104', '2024/2025', 1, 'REGULAR', NULL, NULL, NULL, '0027');
INSERT INTO `users` (`user_id`, `username`, `password_hash`, `role_id`, `full_name`, `email`, `phone`, `address`, `profile_pic`, `department_id`, `created_at`, `updated_at`, `status`, `student_reg_no`, `academic_year`, `current_semester`, `student_type`, `designation`, `office_room`, `assigned_lab`, `role_specific_id`) VALUES (54, 'bs20242105', 'b2a1f4fd0a460606b34c8913e2981dac8d2e283d778aba586c416ee2629bfa54', 4, 'K.G. Nimesha Kanchana', 'nimesha.k@fot.ruh.ac.lk', '0762221005', 'No. 11, Siripura, Hambantota', 'default_avatar.png', 2, '2026-11-03 15:41:08', '2026-11-03 15:41:08', 'ACTIVE', 'TG/2024/2105', '2024/2025', 1, 'REGULAR', NULL, NULL, NULL, '0028');
INSERT INTO `users` (`user_id`, `username`, `password_hash`, `role_id`, `full_name`, `email`, `phone`, `address`, `profile_pic`, `department_id`, `created_at`, `updated_at`, `status`, `student_reg_no`, `academic_year`, `current_semester`, `student_type`, `designation`, `office_room`, `assigned_lab`, `role_specific_id`) VALUES (55, 'et20243001', 'b2a1f4fd0a460606b34c8913e2981dac8d2e283d778aba586c416ee2629bfa54', 4, 'M.A. Dinesh Prasad', 'dinesh.p@fot.ruh.ac.lk', '0763331001', 'No. 33, Station Road, Matara', 'default_avatar.png', 3, '2026-11-03 15:41:08', '2026-11-03 15:41:08', 'ACTIVE', 'TG/2024/3001', '2024/2025', 1, 'REGULAR', NULL, NULL, NULL, '0029');
INSERT INTO `users` (`user_id`, `username`, `password_hash`, `role_id`, `full_name`, `email`, `phone`, `address`, `profile_pic`, `department_id`, `created_at`, `updated_at`, `status`, `student_reg_no`, `academic_year`, `current_semester`, `student_type`, `designation`, `office_room`, `assigned_lab`, `role_specific_id`) VALUES (56, 'et20243002', 'b2a1f4fd0a460606b34c8913e2981dac8d2e283d778aba586c416ee2629bfa54', 4, 'S.H. Lasith Wickrama', 'lasith.w@fot.ruh.ac.lk', '0763331002', 'Boys Hostel, Karagoda Uyangoda', 'default_avatar.png', 3, '2026-11-03 15:41:08', '2026-11-03 15:41:08', 'ACTIVE', 'TG/2024/3002', '2024/2025', 1, 'REGULAR', NULL, NULL, NULL, '0030');
INSERT INTO `users` (`user_id`, `username`, `password_hash`, `role_id`, `full_name`, `email`, `phone`, `address`, `profile_pic`, `department_id`, `created_at`, `updated_at`, `status`, `student_reg_no`, `academic_year`, `current_semester`, `student_type`, `designation`, `office_room`, `assigned_lab`, `role_specific_id`) VALUES (57, 'et20243003', 'b2a1f4fd0a460606b34c8913e2981dac8d2e283d778aba586c416ee2629bfa54', 4, 'T.N. Chathuri Perera', 'chathuri.p@fot.ruh.ac.lk', '0763331003', 'No. 64, Old Road, Weligama', 'default_avatar.png', 3, '2026-11-03 15:41:08', '2026-11-03 15:41:08', 'ACTIVE', 'TG/2024/3003', '2024/2025', 1, 'REGULAR', NULL, NULL, NULL, '0031');
INSERT INTO `users` (`user_id`, `username`, `password_hash`, `role_id`, `full_name`, `email`, `phone`, `address`, `profile_pic`, `department_id`, `created_at`, `updated_at`, `status`, `student_reg_no`, `academic_year`, `current_semester`, `student_type`, `designation`, `office_room`, `assigned_lab`, `role_specific_id`) VALUES (58, 'et20243004', 'b2a1f4fd0a460606b34c8913e2981dac8d2e283d778aba586c416ee2629bfa54', 4, 'R.K. Samitha Bandara', 'samitha.b@fot.ruh.ac.lk', '0763331004', 'No. 19, Kandy Road, Ambalangoda', 'default_avatar.png', 3, '2026-11-03 15:41:08', '2026-11-03 15:41:08', 'ACTIVE', 'TG/2024/3004', '2024/2025', 1, 'REGULAR', NULL, NULL, NULL, '0032');
INSERT INTO `users` (`user_id`, `username`, `password_hash`, `role_id`, `full_name`, `email`, `phone`, `address`, `profile_pic`, `department_id`, `created_at`, `updated_at`, `status`, `student_reg_no`, `academic_year`, `current_semester`, `student_type`, `designation`, `office_room`, `assigned_lab`, `role_specific_id`) VALUES (59, 'et20243005', 'b2a1f4fd0a460606b34c8913e2981dac8d2e283d778aba586c416ee2629bfa54', 4, 'A.D. Thisara Kumara', 'thisara.k@fot.ruh.ac.lk', '0763331005', 'No. 88, Sea View, Mirissa', 'default_avatar.png', 3, '2026-11-03 15:41:08', '2026-11-03 15:41:08', 'ACTIVE', 'TG/2024/3005', '2024/2025', 1, 'REGULAR', NULL, NULL, NULL, '0033');
INSERT INTO `users` (`user_id`, `username`, `password_hash`, `role_id`, `full_name`, `email`, `phone`, `address`, `profile_pic`, `department_id`, `created_at`, `updated_at`, `status`, `student_reg_no`, `academic_year`, `current_semester`, `student_type`, `designation`, `office_room`, `assigned_lab`, `role_specific_id`) VALUES (60, 'md20244001', 'b2a1f4fd0a460606b34c8913e2981dac8d2e283d778aba586c416ee2629bfa54', 4, 'V.L. Sandali Jayamini', 'sandali.j@fot.ruh.ac.lk', '0764441001', 'No. 5, Dharmaratne Road, Matara', 'default_avatar.png', 4, '2026-11-03 15:41:08', '2026-11-03 15:41:08', 'ACTIVE', 'TG/2024/4001', '2024/2025', 1, 'REGULAR', NULL, NULL, NULL, '0034');
INSERT INTO `users` (`user_id`, `username`, `password_hash`, `role_id`, `full_name`, `email`, `phone`, `address`, `profile_pic`, `department_id`, `created_at`, `updated_at`, `status`, `student_reg_no`, `academic_year`, `current_semester`, `student_type`, `designation`, `office_room`, `assigned_lab`, `role_specific_id`) VALUES (61, 'md20244002', 'b2a1f4fd0a460606b34c8913e2981dac8d2e283d778aba586c416ee2629bfa54', 4, 'G.P. Haritha Jayalath', 'haritha.j@fot.ruh.ac.lk', '0764441002', 'No. 29, Lake Road, Kataragama', 'default_avatar.png', 4, '2026-11-03 15:41:08', '2026-11-03 15:41:08', 'ACTIVE', 'TG/2024/4002', '2024/2025', 1, 'REGULAR', NULL, NULL, NULL, '0035');
INSERT INTO `users` (`user_id`, `username`, `password_hash`, `role_id`, `full_name`, `email`, `phone`, `address`, `profile_pic`, `department_id`, `created_at`, `updated_at`, `status`, `student_reg_no`, `academic_year`, `current_semester`, `student_type`, `designation`, `office_room`, `assigned_lab`, `role_specific_id`) VALUES (62, 'md20244003', 'b2a1f4fd0a460606b34c8913e2981dac8d2e283d778aba586c416ee2629bfa54', 4, 'N.M. Sachini Weerasinghe', 'sachini.w@fot.ruh.ac.lk', '0764441003', 'No. 57, Queens Road, Badulla', 'default_avatar.png', 4, '2026-11-03 15:41:08', '2026-11-03 15:41:08', 'ACTIVE', 'TG/2024/4003', '2024/2025', 1, 'REGULAR', NULL, NULL, NULL, '0036');
INSERT INTO `users` (`user_id`, `username`, `password_hash`, `role_id`, `full_name`, `email`, `phone`, `address`, `profile_pic`, `department_id`, `created_at`, `updated_at`, `status`, `student_reg_no`, `academic_year`, `current_semester`, `student_type`, `designation`, `office_room`, `assigned_lab`, `role_specific_id`) VALUES (63, 'md20244004', 'b2a1f4fd0a460606b34c8913e2981dac8d2e283d778aba586c416ee2629bfa54', 4, 'C.B. Mahesh Wijesinghe', 'mahesh.w@fot.ruh.ac.lk', '0764441004', 'No. 13, Sirimavo Mawatha, Kandy', 'default_avatar.png', 4, '2026-11-03 15:41:08', '2026-11-03 15:41:08', 'ACTIVE', 'TG/2024/4004', '2024/2025', 1, 'REGULAR', NULL, NULL, NULL, '0037');
INSERT INTO `users` (`user_id`, `username`, `password_hash`, `role_id`, `full_name`, `email`, `phone`, `address`, `profile_pic`, `department_id`, `created_at`, `updated_at`, `status`, `student_reg_no`, `academic_year`, `current_semester`, `student_type`, `designation`, `office_room`, `assigned_lab`, `role_specific_id`) VALUES (64, 'md20244005', 'b2a1f4fd0a460606b34c8913e2981dac8d2e283d778aba586c416ee2629bfa54', 4, 'T.M. Dilini Rajapaksha', 'dilini.r@fot.ruh.ac.lk', '0764441005', 'Girls Hostel 02, Karagoda Uyangoda', 'default_avatar.png', 4, '2026-11-03 15:41:08', '2026-11-03 15:41:08', 'ACTIVE', 'TG/2024/4005', '2024/2025', 1, 'REGULAR', NULL, NULL, NULL, '0038');
-- (63 records)

-- --------------------------------------------------------
-- Data for table `courses`
-- --------------------------------------------------------
INSERT INTO `courses` (`course_id`, `course_code`, `course_name`, `credits`, `semester`, `study_year`, `department_id`, `has_theory`, `has_practical`, `description`) VALUES (1, 'ICT2113', 'Database Systems', 3, 1, 1, 1, 1, 1, 'Relational model, SQL, normalization, transactions and indexing');
INSERT INTO `courses` (`course_id`, `course_code`, `course_name`, `credits`, `semester`, `study_year`, `department_id`, `has_theory`, `has_practical`, `description`) VALUES (2, 'ICT2122', 'Software Engineering', 2, 1, 1, 1, 1, 0, 'Software lifecycle, agile methodologies, requirements engineering and UML');
INSERT INTO `courses` (`course_id`, `course_code`, `course_name`, `credits`, `semester`, `study_year`, `department_id`, `has_theory`, `has_practical`, `description`) VALUES (3, 'ICT2132', 'Object Oriented Programming Practicum', 2, 1, 1, 1, 1, 1, 'Advanced OOP, Java Swing, JDBC, design patterns, and desktop application development');
INSERT INTO `courses` (`course_id`, `course_code`, `course_name`, `credits`, `semester`, `study_year`, `department_id`, `has_theory`, `has_practical`, `description`) VALUES (4, 'ICT2143', 'Computer Networks', 3, 1, 1, 1, 1, 1, 'OSI model, TCP/IP, IP addressing, subnetting and routing protocols');
INSERT INTO `courses` (`course_id`, `course_code`, `course_name`, `credits`, `semester`, `study_year`, `department_id`, `has_theory`, `has_practical`, `description`) VALUES (5, 'ENG2112', 'Professional English II', 2, 1, 1, 4, 1, 0, 'Academic writing, technical presentations and technical reports');
INSERT INTO `courses` (`course_id`, `course_code`, `course_name`, `credits`, `semester`, `study_year`, `department_id`, `has_theory`, `has_practical`, `description`) VALUES (6, 'ET2101', 'Applied Electricity', 2, 1, 1, 3, 1, 1, 'Circuit analysis, AC circuits, transformers and electric machines');
INSERT INTO `courses` (`course_id`, `course_code`, `course_name`, `credits`, `semester`, `study_year`, `department_id`, `has_theory`, `has_practical`, `description`) VALUES (7, 'BST2102', 'Bio-Statistics', 2, 1, 1, 2, 1, 0, 'Probability distributions, statistical hypothesis testing and regression analysis');
INSERT INTO `courses` (`course_id`, `course_code`, `course_name`, `credits`, `semester`, `study_year`, `department_id`, `has_theory`, `has_practical`, `description`) VALUES (16, 'ICT2152', 'Web Application Development', 2, 1, 1, 1, 1, 1, 'HTML, CSS, JavaScript, PHP and MySQL for web-based systems');
INSERT INTO `courses` (`course_id`, `course_code`, `course_name`, `credits`, `semester`, `study_year`, `department_id`, `has_theory`, `has_practical`, `description`) VALUES (17, 'BST2101', 'Crop Science and Agronomy', 3, 1, 1, 2, 1, 1, 'Crop production principles, soil science, irrigation and cultivation practices');
INSERT INTO `courses` (`course_id`, `course_code`, `course_name`, `credits`, `semester`, `study_year`, `department_id`, `has_theory`, `has_practical`, `description`) VALUES (18, 'BST2111', 'Environmental Science', 2, 1, 1, 2, 1, 0, 'Ecosystem dynamics, pollution management and environmental regulations');
INSERT INTO `courses` (`course_id`, `course_code`, `course_name`, `credits`, `semester`, `study_year`, `department_id`, `has_theory`, `has_practical`, `description`) VALUES (19, 'BST2121', 'Microbiology and Biotechnology', 2, 1, 1, 2, 1, 1, 'Microbial growth, fermentation, recombinant DNA techniques and bioreactors');
INSERT INTO `courses` (`course_id`, `course_code`, `course_name`, `credits`, `semester`, `study_year`, `department_id`, `has_theory`, `has_practical`, `description`) VALUES (20, 'BST2131', 'Food Technology and Processing', 3, 1, 1, 2, 1, 1, 'Food preservation, quality control, HACCP and processing technologies');
INSERT INTO `courses` (`course_id`, `course_code`, `course_name`, `credits`, `semester`, `study_year`, `department_id`, `has_theory`, `has_practical`, `description`) VALUES (21, 'ET2111', 'Mechanical Engineering Fundamentals', 3, 1, 1, 3, 1, 1, 'Statics, dynamics, strength of materials and machine elements');
INSERT INTO `courses` (`course_id`, `course_code`, `course_name`, `credits`, `semester`, `study_year`, `department_id`, `has_theory`, `has_practical`, `description`) VALUES (22, 'ET2121', 'Industrial Automation', 2, 1, 1, 3, 1, 1, 'PLC programming, SCADA, sensor systems and control loops');
INSERT INTO `courses` (`course_id`, `course_code`, `course_name`, `credits`, `semester`, `study_year`, `department_id`, `has_theory`, `has_practical`, `description`) VALUES (23, 'ET2131', 'Thermodynamics and Heat Transfer', 2, 1, 1, 3, 1, 0, 'Laws of thermodynamics, heat exchangers and energy systems');
INSERT INTO `courses` (`course_id`, `course_code`, `course_name`, `credits`, `semester`, `study_year`, `department_id`, `has_theory`, `has_practical`, `description`) VALUES (24, 'ET2141', 'Engineering Drawing and CAD', 2, 1, 1, 3, 1, 1, 'Technical drawing standards, AutoCAD and SolidWorks design');
INSERT INTO `courses` (`course_id`, `course_code`, `course_name`, `credits`, `semester`, `study_year`, `department_id`, `has_theory`, `has_practical`, `description`) VALUES (25, 'MDT2101', 'Mathematics for Technology', 3, 1, 1, 4, 1, 0, 'Calculus, linear algebra, matrices and numerical methods');
INSERT INTO `courses` (`course_id`, `course_code`, `course_name`, `credits`, `semester`, `study_year`, `department_id`, `has_theory`, `has_practical`, `description`) VALUES (26, 'MDT2111', 'Business Management', 2, 1, 1, 4, 1, 0, 'Organizational behavior, project management and entrepreneurship');
INSERT INTO `courses` (`course_id`, `course_code`, `course_name`, `credits`, `semester`, `study_year`, `department_id`, `has_theory`, `has_practical`, `description`) VALUES (27, 'MDT2121', 'Sinhala Technical Communication', 2, 1, 1, 4, 1, 0, 'Technical Sinhala writing, report preparation and communication skills');
INSERT INTO `courses` (`course_id`, `course_code`, `course_name`, `credits`, `semester`, `study_year`, `department_id`, `has_theory`, `has_practical`, `description`) VALUES (28, 'MDT2131', 'Research Methodology', 2, 1, 1, 4, 1, 0, 'Literature review, data collection methods, analysis and academic writing');
-- (20 records)

-- --------------------------------------------------------
-- Data for table `course_lecturers`
-- --------------------------------------------------------
INSERT INTO `course_lecturers` (`course_id`, `lecturer_id`, `assigned_at`) VALUES (1, 3, '2026-11-03 15:41:08');
INSERT INTO `course_lecturers` (`course_id`, `lecturer_id`, `assigned_at`) VALUES (2, 2, '2026-11-03 15:41:08');
INSERT INTO `course_lecturers` (`course_id`, `lecturer_id`, `assigned_at`) VALUES (3, 34, '2026-11-03 05:05:12');
INSERT INTO `course_lecturers` (`course_id`, `lecturer_id`, `assigned_at`) VALUES (4, 34, '2026-11-03 05:04:54');
INSERT INTO `course_lecturers` (`course_id`, `lecturer_id`, `assigned_at`) VALUES (5, 6, '2026-11-03 15:41:08');
INSERT INTO `course_lecturers` (`course_id`, `lecturer_id`, `assigned_at`) VALUES (6, 5, '2026-11-03 15:41:08');
INSERT INTO `course_lecturers` (`course_id`, `lecturer_id`, `assigned_at`) VALUES (7, 4, '2026-11-03 15:41:08');
INSERT INTO `course_lecturers` (`course_id`, `lecturer_id`, `assigned_at`) VALUES (16, 37, '2026-11-03 15:41:08');
INSERT INTO `course_lecturers` (`course_id`, `lecturer_id`, `assigned_at`) VALUES (17, 4, '2026-11-03 15:41:08');
INSERT INTO `course_lecturers` (`course_id`, `lecturer_id`, `assigned_at`) VALUES (18, 38, '2026-11-03 15:41:08');
INSERT INTO `course_lecturers` (`course_id`, `lecturer_id`, `assigned_at`) VALUES (19, 39, '2026-11-03 15:41:08');
INSERT INTO `course_lecturers` (`course_id`, `lecturer_id`, `assigned_at`) VALUES (20, 40, '2026-11-03 15:41:08');
INSERT INTO `course_lecturers` (`course_id`, `lecturer_id`, `assigned_at`) VALUES (21, 42, '2026-11-03 15:41:08');
INSERT INTO `course_lecturers` (`course_id`, `lecturer_id`, `assigned_at`) VALUES (22, 43, '2026-11-03 15:41:08');
INSERT INTO `course_lecturers` (`course_id`, `lecturer_id`, `assigned_at`) VALUES (23, 44, '2026-11-03 15:41:08');
INSERT INTO `course_lecturers` (`course_id`, `lecturer_id`, `assigned_at`) VALUES (24, 45, '2026-11-03 15:41:08');
INSERT INTO `course_lecturers` (`course_id`, `lecturer_id`, `assigned_at`) VALUES (25, 46, '2026-11-03 15:41:08');
INSERT INTO `course_lecturers` (`course_id`, `lecturer_id`, `assigned_at`) VALUES (26, 47, '2026-11-03 15:41:08');
INSERT INTO `course_lecturers` (`course_id`, `lecturer_id`, `assigned_at`) VALUES (27, 48, '2026-11-03 15:41:08');
INSERT INTO `course_lecturers` (`course_id`, `lecturer_id`, `assigned_at`) VALUES (28, 49, '2026-11-03 15:41:08');
-- (20 records)

-- --------------------------------------------------------
-- Data for table `course_enrollment`
-- --------------------------------------------------------
INSERT INTO `course_enrollment` (`enrollment_id`, `student_id`, `course_id`, `academic_year`, `semester`, `is_repeat`, `enrolled_at`) VALUES (1, 11, 1, '2024/2025', 1, 0, '2026-11-01 01:01:30');
INSERT INTO `course_enrollment` (`enrollment_id`, `student_id`, `course_id`, `academic_year`, `semester`, `is_repeat`, `enrolled_at`) VALUES (2, 11, 2, '2024/2025', 1, 0, '2026-11-01 01:01:30');
INSERT INTO `course_enrollment` (`enrollment_id`, `student_id`, `course_id`, `academic_year`, `semester`, `is_repeat`, `enrolled_at`) VALUES (3, 11, 3, '2024/2025', 1, 0, '2026-11-01 01:01:30');
INSERT INTO `course_enrollment` (`enrollment_id`, `student_id`, `course_id`, `academic_year`, `semester`, `is_repeat`, `enrolled_at`) VALUES (4, 11, 4, '2024/2025', 1, 0, '2026-11-01 01:01:30');
INSERT INTO `course_enrollment` (`enrollment_id`, `student_id`, `course_id`, `academic_year`, `semester`, `is_repeat`, `enrolled_at`) VALUES (5, 11, 5, '2024/2025', 1, 0, '2026-11-01 01:01:30');
INSERT INTO `course_enrollment` (`enrollment_id`, `student_id`, `course_id`, `academic_year`, `semester`, `is_repeat`, `enrolled_at`) VALUES (6, 12, 1, '2024/2025', 1, 0, '2026-11-01 01:01:30');
INSERT INTO `course_enrollment` (`enrollment_id`, `student_id`, `course_id`, `academic_year`, `semester`, `is_repeat`, `enrolled_at`) VALUES (7, 12, 2, '2024/2025', 1, 0, '2026-11-01 01:01:30');
INSERT INTO `course_enrollment` (`enrollment_id`, `student_id`, `course_id`, `academic_year`, `semester`, `is_repeat`, `enrolled_at`) VALUES (8, 12, 3, '2024/2025', 1, 0, '2026-11-01 01:01:30');
INSERT INTO `course_enrollment` (`enrollment_id`, `student_id`, `course_id`, `academic_year`, `semester`, `is_repeat`, `enrolled_at`) VALUES (9, 12, 4, '2024/2025', 1, 0, '2026-11-01 01:01:30');
INSERT INTO `course_enrollment` (`enrollment_id`, `student_id`, `course_id`, `academic_year`, `semester`, `is_repeat`, `enrolled_at`) VALUES (10, 12, 5, '2024/2025', 1, 0, '2026-11-01 01:01:30');
INSERT INTO `course_enrollment` (`enrollment_id`, `student_id`, `course_id`, `academic_year`, `semester`, `is_repeat`, `enrolled_at`) VALUES (11, 13, 1, '2024/2025', 1, 0, '2026-11-01 01:01:30');
INSERT INTO `course_enrollment` (`enrollment_id`, `student_id`, `course_id`, `academic_year`, `semester`, `is_repeat`, `enrolled_at`) VALUES (12, 13, 2, '2024/2025', 1, 0, '2026-11-01 01:01:30');
INSERT INTO `course_enrollment` (`enrollment_id`, `student_id`, `course_id`, `academic_year`, `semester`, `is_repeat`, `enrolled_at`) VALUES (13, 13, 3, '2024/2025', 1, 0, '2026-11-01 01:01:30');
INSERT INTO `course_enrollment` (`enrollment_id`, `student_id`, `course_id`, `academic_year`, `semester`, `is_repeat`, `enrolled_at`) VALUES (14, 13, 4, '2024/2025', 1, 0, '2026-11-01 01:01:30');
INSERT INTO `course_enrollment` (`enrollment_id`, `student_id`, `course_id`, `academic_year`, `semester`, `is_repeat`, `enrolled_at`) VALUES (15, 13, 5, '2024/2025', 1, 0, '2026-11-01 01:01:30');
INSERT INTO `course_enrollment` (`enrollment_id`, `student_id`, `course_id`, `academic_year`, `semester`, `is_repeat`, `enrolled_at`) VALUES (16, 14, 1, '2024/2025', 1, 0, '2026-11-01 01:01:30');
INSERT INTO `course_enrollment` (`enrollment_id`, `student_id`, `course_id`, `academic_year`, `semester`, `is_repeat`, `enrolled_at`) VALUES (17, 14, 2, '2024/2025', 1, 0, '2026-11-01 01:01:30');
INSERT INTO `course_enrollment` (`enrollment_id`, `student_id`, `course_id`, `academic_year`, `semester`, `is_repeat`, `enrolled_at`) VALUES (18, 14, 3, '2024/2025', 1, 0, '2026-11-01 01:01:30');
INSERT INTO `course_enrollment` (`enrollment_id`, `student_id`, `course_id`, `academic_year`, `semester`, `is_repeat`, `enrolled_at`) VALUES (19, 14, 4, '2024/2025', 1, 0, '2026-11-01 01:01:30');
INSERT INTO `course_enrollment` (`enrollment_id`, `student_id`, `course_id`, `academic_year`, `semester`, `is_repeat`, `enrolled_at`) VALUES (20, 14, 5, '2024/2025', 1, 0, '2026-11-01 01:01:30');
INSERT INTO `course_enrollment` (`enrollment_id`, `student_id`, `course_id`, `academic_year`, `semester`, `is_repeat`, `enrolled_at`) VALUES (21, 15, 1, '2024/2025', 1, 0, '2026-11-01 01:01:30');
INSERT INTO `course_enrollment` (`enrollment_id`, `student_id`, `course_id`, `academic_year`, `semester`, `is_repeat`, `enrolled_at`) VALUES (22, 15, 2, '2024/2025', 1, 0, '2026-11-01 01:01:30');
INSERT INTO `course_enrollment` (`enrollment_id`, `student_id`, `course_id`, `academic_year`, `semester`, `is_repeat`, `enrolled_at`) VALUES (23, 15, 3, '2024/2025', 1, 0, '2026-11-01 01:01:30');
INSERT INTO `course_enrollment` (`enrollment_id`, `student_id`, `course_id`, `academic_year`, `semester`, `is_repeat`, `enrolled_at`) VALUES (24, 15, 4, '2024/2025', 1, 0, '2026-11-01 01:01:30');
INSERT INTO `course_enrollment` (`enrollment_id`, `student_id`, `course_id`, `academic_year`, `semester`, `is_repeat`, `enrolled_at`) VALUES (25, 15, 5, '2024/2025', 1, 0, '2026-11-01 01:01:30');
INSERT INTO `course_enrollment` (`enrollment_id`, `student_id`, `course_id`, `academic_year`, `semester`, `is_repeat`, `enrolled_at`) VALUES (26, 16, 1, '2024/2025', 1, 0, '2026-11-01 01:01:30');
INSERT INTO `course_enrollment` (`enrollment_id`, `student_id`, `course_id`, `academic_year`, `semester`, `is_repeat`, `enrolled_at`) VALUES (27, 16, 2, '2024/2025', 1, 0, '2026-11-01 01:01:30');
INSERT INTO `course_enrollment` (`enrollment_id`, `student_id`, `course_id`, `academic_year`, `semester`, `is_repeat`, `enrolled_at`) VALUES (28, 16, 3, '2024/2025', 1, 0, '2026-11-01 01:01:30');
INSERT INTO `course_enrollment` (`enrollment_id`, `student_id`, `course_id`, `academic_year`, `semester`, `is_repeat`, `enrolled_at`) VALUES (29, 16, 4, '2024/2025', 1, 0, '2026-11-01 01:01:30');
INSERT INTO `course_enrollment` (`enrollment_id`, `student_id`, `course_id`, `academic_year`, `semester`, `is_repeat`, `enrolled_at`) VALUES (30, 16, 5, '2024/2025', 1, 0, '2026-11-01 01:01:30');
INSERT INTO `course_enrollment` (`enrollment_id`, `student_id`, `course_id`, `academic_year`, `semester`, `is_repeat`, `enrolled_at`) VALUES (31, 17, 1, '2024/2025', 1, 0, '2026-11-01 01:01:30');
INSERT INTO `course_enrollment` (`enrollment_id`, `student_id`, `course_id`, `academic_year`, `semester`, `is_repeat`, `enrolled_at`) VALUES (32, 17, 2, '2024/2025', 1, 0, '2026-11-01 01:01:30');
INSERT INTO `course_enrollment` (`enrollment_id`, `student_id`, `course_id`, `academic_year`, `semester`, `is_repeat`, `enrolled_at`) VALUES (33, 17, 3, '2024/2025', 1, 0, '2026-11-01 01:01:30');
INSERT INTO `course_enrollment` (`enrollment_id`, `student_id`, `course_id`, `academic_year`, `semester`, `is_repeat`, `enrolled_at`) VALUES (34, 17, 4, '2024/2025', 1, 0, '2026-11-01 01:01:30');
INSERT INTO `course_enrollment` (`enrollment_id`, `student_id`, `course_id`, `academic_year`, `semester`, `is_repeat`, `enrolled_at`) VALUES (35, 17, 5, '2024/2025', 1, 0, '2026-11-01 01:01:30');
INSERT INTO `course_enrollment` (`enrollment_id`, `student_id`, `course_id`, `academic_year`, `semester`, `is_repeat`, `enrolled_at`) VALUES (36, 18, 1, '2024/2025', 1, 0, '2026-11-01 01:01:30');
INSERT INTO `course_enrollment` (`enrollment_id`, `student_id`, `course_id`, `academic_year`, `semester`, `is_repeat`, `enrolled_at`) VALUES (37, 18, 2, '2024/2025', 1, 0, '2026-11-01 01:01:30');
INSERT INTO `course_enrollment` (`enrollment_id`, `student_id`, `course_id`, `academic_year`, `semester`, `is_repeat`, `enrolled_at`) VALUES (38, 18, 3, '2024/2025', 1, 0, '2026-11-01 01:01:30');
INSERT INTO `course_enrollment` (`enrollment_id`, `student_id`, `course_id`, `academic_year`, `semester`, `is_repeat`, `enrolled_at`) VALUES (39, 18, 4, '2024/2025', 1, 0, '2026-11-01 01:01:30');
INSERT INTO `course_enrollment` (`enrollment_id`, `student_id`, `course_id`, `academic_year`, `semester`, `is_repeat`, `enrolled_at`) VALUES (40, 18, 5, '2024/2025', 1, 0, '2026-11-01 01:01:30');
INSERT INTO `course_enrollment` (`enrollment_id`, `student_id`, `course_id`, `academic_year`, `semester`, `is_repeat`, `enrolled_at`) VALUES (41, 19, 1, '2024/2025', 1, 0, '2026-11-01 01:01:30');
INSERT INTO `course_enrollment` (`enrollment_id`, `student_id`, `course_id`, `academic_year`, `semester`, `is_repeat`, `enrolled_at`) VALUES (42, 19, 2, '2024/2025', 1, 0, '2026-11-01 01:01:30');
INSERT INTO `course_enrollment` (`enrollment_id`, `student_id`, `course_id`, `academic_year`, `semester`, `is_repeat`, `enrolled_at`) VALUES (43, 19, 3, '2024/2025', 1, 0, '2026-11-01 01:01:30');
INSERT INTO `course_enrollment` (`enrollment_id`, `student_id`, `course_id`, `academic_year`, `semester`, `is_repeat`, `enrolled_at`) VALUES (44, 19, 4, '2024/2025', 1, 0, '2026-11-01 01:01:30');
INSERT INTO `course_enrollment` (`enrollment_id`, `student_id`, `course_id`, `academic_year`, `semester`, `is_repeat`, `enrolled_at`) VALUES (45, 19, 5, '2024/2025', 1, 0, '2026-11-01 01:01:30');
INSERT INTO `course_enrollment` (`enrollment_id`, `student_id`, `course_id`, `academic_year`, `semester`, `is_repeat`, `enrolled_at`) VALUES (46, 20, 1, '2024/2025', 1, 0, '2026-11-01 01:01:30');
INSERT INTO `course_enrollment` (`enrollment_id`, `student_id`, `course_id`, `academic_year`, `semester`, `is_repeat`, `enrolled_at`) VALUES (47, 20, 2, '2024/2025', 1, 0, '2026-11-01 01:01:30');
INSERT INTO `course_enrollment` (`enrollment_id`, `student_id`, `course_id`, `academic_year`, `semester`, `is_repeat`, `enrolled_at`) VALUES (48, 20, 3, '2024/2025', 1, 0, '2026-11-01 01:01:30');
INSERT INTO `course_enrollment` (`enrollment_id`, `student_id`, `course_id`, `academic_year`, `semester`, `is_repeat`, `enrolled_at`) VALUES (49, 20, 4, '2024/2025', 1, 0, '2026-11-01 01:01:30');
INSERT INTO `course_enrollment` (`enrollment_id`, `student_id`, `course_id`, `academic_year`, `semester`, `is_repeat`, `enrolled_at`) VALUES (50, 20, 5, '2024/2025', 1, 0, '2026-11-01 01:01:30');
INSERT INTO `course_enrollment` (`enrollment_id`, `student_id`, `course_id`, `academic_year`, `semester`, `is_repeat`, `enrolled_at`) VALUES (51, 21, 1, '2024/2025', 1, 0, '2026-11-01 01:01:30');
INSERT INTO `course_enrollment` (`enrollment_id`, `student_id`, `course_id`, `academic_year`, `semester`, `is_repeat`, `enrolled_at`) VALUES (52, 21, 2, '2024/2025', 1, 0, '2026-11-01 01:01:30');
INSERT INTO `course_enrollment` (`enrollment_id`, `student_id`, `course_id`, `academic_year`, `semester`, `is_repeat`, `enrolled_at`) VALUES (53, 21, 3, '2024/2025', 1, 0, '2026-11-01 01:01:30');
INSERT INTO `course_enrollment` (`enrollment_id`, `student_id`, `course_id`, `academic_year`, `semester`, `is_repeat`, `enrolled_at`) VALUES (54, 21, 4, '2024/2025', 1, 0, '2026-11-01 01:01:30');
INSERT INTO `course_enrollment` (`enrollment_id`, `student_id`, `course_id`, `academic_year`, `semester`, `is_repeat`, `enrolled_at`) VALUES (55, 21, 5, '2024/2025', 1, 0, '2026-11-01 01:01:30');
INSERT INTO `course_enrollment` (`enrollment_id`, `student_id`, `course_id`, `academic_year`, `semester`, `is_repeat`, `enrolled_at`) VALUES (56, 22, 1, '2024/2025', 1, 0, '2026-11-01 01:01:30');
INSERT INTO `course_enrollment` (`enrollment_id`, `student_id`, `course_id`, `academic_year`, `semester`, `is_repeat`, `enrolled_at`) VALUES (57, 22, 2, '2024/2025', 1, 0, '2026-11-01 01:01:30');
INSERT INTO `course_enrollment` (`enrollment_id`, `student_id`, `course_id`, `academic_year`, `semester`, `is_repeat`, `enrolled_at`) VALUES (58, 22, 3, '2024/2025', 1, 0, '2026-11-01 01:01:30');
INSERT INTO `course_enrollment` (`enrollment_id`, `student_id`, `course_id`, `academic_year`, `semester`, `is_repeat`, `enrolled_at`) VALUES (59, 22, 4, '2024/2025', 1, 0, '2026-11-01 01:01:30');
INSERT INTO `course_enrollment` (`enrollment_id`, `student_id`, `course_id`, `academic_year`, `semester`, `is_repeat`, `enrolled_at`) VALUES (60, 22, 5, '2024/2025', 1, 0, '2026-11-01 01:01:30');
INSERT INTO `course_enrollment` (`enrollment_id`, `student_id`, `course_id`, `academic_year`, `semester`, `is_repeat`, `enrolled_at`) VALUES (61, 23, 1, '2024/2025', 1, 0, '2026-11-01 01:01:30');
INSERT INTO `course_enrollment` (`enrollment_id`, `student_id`, `course_id`, `academic_year`, `semester`, `is_repeat`, `enrolled_at`) VALUES (62, 23, 2, '2024/2025', 1, 0, '2026-11-01 01:01:30');
INSERT INTO `course_enrollment` (`enrollment_id`, `student_id`, `course_id`, `academic_year`, `semester`, `is_repeat`, `enrolled_at`) VALUES (63, 23, 3, '2024/2025', 1, 0, '2026-11-01 01:01:30');
INSERT INTO `course_enrollment` (`enrollment_id`, `student_id`, `course_id`, `academic_year`, `semester`, `is_repeat`, `enrolled_at`) VALUES (64, 23, 4, '2024/2025', 1, 0, '2026-11-01 01:01:30');
INSERT INTO `course_enrollment` (`enrollment_id`, `student_id`, `course_id`, `academic_year`, `semester`, `is_repeat`, `enrolled_at`) VALUES (65, 23, 5, '2024/2025', 1, 0, '2026-11-01 01:01:30');
INSERT INTO `course_enrollment` (`enrollment_id`, `student_id`, `course_id`, `academic_year`, `semester`, `is_repeat`, `enrolled_at`) VALUES (66, 24, 1, '2024/2025', 1, 0, '2026-11-01 01:01:30');
INSERT INTO `course_enrollment` (`enrollment_id`, `student_id`, `course_id`, `academic_year`, `semester`, `is_repeat`, `enrolled_at`) VALUES (67, 24, 2, '2024/2025', 1, 0, '2026-11-01 01:01:30');
INSERT INTO `course_enrollment` (`enrollment_id`, `student_id`, `course_id`, `academic_year`, `semester`, `is_repeat`, `enrolled_at`) VALUES (68, 24, 3, '2024/2025', 1, 0, '2026-11-01 01:01:30');
INSERT INTO `course_enrollment` (`enrollment_id`, `student_id`, `course_id`, `academic_year`, `semester`, `is_repeat`, `enrolled_at`) VALUES (69, 24, 4, '2024/2025', 1, 0, '2026-11-01 01:01:30');
INSERT INTO `course_enrollment` (`enrollment_id`, `student_id`, `course_id`, `academic_year`, `semester`, `is_repeat`, `enrolled_at`) VALUES (70, 24, 5, '2024/2025', 1, 0, '2026-11-01 01:01:30');
INSERT INTO `course_enrollment` (`enrollment_id`, `student_id`, `course_id`, `academic_year`, `semester`, `is_repeat`, `enrolled_at`) VALUES (71, 25, 1, '2024/2025', 1, 0, '2026-11-01 01:01:30');
INSERT INTO `course_enrollment` (`enrollment_id`, `student_id`, `course_id`, `academic_year`, `semester`, `is_repeat`, `enrolled_at`) VALUES (72, 25, 2, '2024/2025', 1, 0, '2026-11-01 01:01:30');
INSERT INTO `course_enrollment` (`enrollment_id`, `student_id`, `course_id`, `academic_year`, `semester`, `is_repeat`, `enrolled_at`) VALUES (73, 25, 3, '2024/2025', 1, 0, '2026-11-01 01:01:30');
INSERT INTO `course_enrollment` (`enrollment_id`, `student_id`, `course_id`, `academic_year`, `semester`, `is_repeat`, `enrolled_at`) VALUES (74, 25, 4, '2024/2025', 1, 0, '2026-11-01 01:01:30');
INSERT INTO `course_enrollment` (`enrollment_id`, `student_id`, `course_id`, `academic_year`, `semester`, `is_repeat`, `enrolled_at`) VALUES (75, 25, 5, '2024/2025', 1, 0, '2026-11-01 01:01:30');
INSERT INTO `course_enrollment` (`enrollment_id`, `student_id`, `course_id`, `academic_year`, `semester`, `is_repeat`, `enrolled_at`) VALUES (76, 26, 1, '2024/2025', 1, 0, '2026-11-01 01:01:30');
INSERT INTO `course_enrollment` (`enrollment_id`, `student_id`, `course_id`, `academic_year`, `semester`, `is_repeat`, `enrolled_at`) VALUES (77, 26, 2, '2024/2025', 1, 0, '2026-11-01 01:01:30');
INSERT INTO `course_enrollment` (`enrollment_id`, `student_id`, `course_id`, `academic_year`, `semester`, `is_repeat`, `enrolled_at`) VALUES (78, 26, 3, '2024/2025', 1, 0, '2026-11-01 01:01:30');
INSERT INTO `course_enrollment` (`enrollment_id`, `student_id`, `course_id`, `academic_year`, `semester`, `is_repeat`, `enrolled_at`) VALUES (79, 26, 4, '2024/2025', 1, 0, '2026-11-01 01:01:30');
INSERT INTO `course_enrollment` (`enrollment_id`, `student_id`, `course_id`, `academic_year`, `semester`, `is_repeat`, `enrolled_at`) VALUES (80, 26, 5, '2024/2025', 1, 0, '2026-11-01 01:01:30');
INSERT INTO `course_enrollment` (`enrollment_id`, `student_id`, `course_id`, `academic_year`, `semester`, `is_repeat`, `enrolled_at`) VALUES (81, 27, 1, '2024/2025', 1, 0, '2026-11-01 01:01:30');
INSERT INTO `course_enrollment` (`enrollment_id`, `student_id`, `course_id`, `academic_year`, `semester`, `is_repeat`, `enrolled_at`) VALUES (82, 27, 2, '2024/2025', 1, 0, '2026-11-01 01:01:30');
INSERT INTO `course_enrollment` (`enrollment_id`, `student_id`, `course_id`, `academic_year`, `semester`, `is_repeat`, `enrolled_at`) VALUES (83, 27, 3, '2024/2025', 1, 0, '2026-11-01 01:01:30');
INSERT INTO `course_enrollment` (`enrollment_id`, `student_id`, `course_id`, `academic_year`, `semester`, `is_repeat`, `enrolled_at`) VALUES (84, 27, 4, '2024/2025', 1, 0, '2026-11-01 01:01:30');
INSERT INTO `course_enrollment` (`enrollment_id`, `student_id`, `course_id`, `academic_year`, `semester`, `is_repeat`, `enrolled_at`) VALUES (85, 27, 5, '2024/2025', 1, 0, '2026-11-01 01:01:30');
INSERT INTO `course_enrollment` (`enrollment_id`, `student_id`, `course_id`, `academic_year`, `semester`, `is_repeat`, `enrolled_at`) VALUES (86, 28, 1, '2024/2025', 1, 0, '2026-11-01 01:01:30');
INSERT INTO `course_enrollment` (`enrollment_id`, `student_id`, `course_id`, `academic_year`, `semester`, `is_repeat`, `enrolled_at`) VALUES (87, 28, 2, '2024/2025', 1, 0, '2026-11-01 01:01:30');
INSERT INTO `course_enrollment` (`enrollment_id`, `student_id`, `course_id`, `academic_year`, `semester`, `is_repeat`, `enrolled_at`) VALUES (88, 28, 3, '2024/2025', 1, 0, '2026-11-01 01:01:30');
INSERT INTO `course_enrollment` (`enrollment_id`, `student_id`, `course_id`, `academic_year`, `semester`, `is_repeat`, `enrolled_at`) VALUES (89, 28, 4, '2024/2025', 1, 0, '2026-11-01 01:01:30');
INSERT INTO `course_enrollment` (`enrollment_id`, `student_id`, `course_id`, `academic_year`, `semester`, `is_repeat`, `enrolled_at`) VALUES (90, 28, 5, '2024/2025', 1, 0, '2026-11-01 01:01:30');
INSERT INTO `course_enrollment` (`enrollment_id`, `student_id`, `course_id`, `academic_year`, `semester`, `is_repeat`, `enrolled_at`) VALUES (128, 29, 3, '2024/2025', 1, 1, '2026-11-01 01:01:30');
INSERT INTO `course_enrollment` (`enrollment_id`, `student_id`, `course_id`, `academic_year`, `semester`, `is_repeat`, `enrolled_at`) VALUES (129, 29, 1, '2024/2025', 1, 0, '2026-11-01 01:01:30');
INSERT INTO `course_enrollment` (`enrollment_id`, `student_id`, `course_id`, `academic_year`, `semester`, `is_repeat`, `enrolled_at`) VALUES (130, 30, 1, '2024/2025', 1, 1, '2026-11-01 01:01:30');
INSERT INTO `course_enrollment` (`enrollment_id`, `student_id`, `course_id`, `academic_year`, `semester`, `is_repeat`, `enrolled_at`) VALUES (131, 30, 3, '2024/2025', 1, 0, '2026-11-01 01:01:30');
INSERT INTO `course_enrollment` (`enrollment_id`, `student_id`, `course_id`, `academic_year`, `semester`, `is_repeat`, `enrolled_at`) VALUES (132, 31, 1, '2024/2025', 1, 0, '2026-11-01 01:01:30');
INSERT INTO `course_enrollment` (`enrollment_id`, `student_id`, `course_id`, `academic_year`, `semester`, `is_repeat`, `enrolled_at`) VALUES (133, 31, 2, '2024/2025', 1, 0, '2026-11-01 01:01:30');
INSERT INTO `course_enrollment` (`enrollment_id`, `student_id`, `course_id`, `academic_year`, `semester`, `is_repeat`, `enrolled_at`) VALUES (134, 31, 3, '2024/2025', 1, 0, '2026-11-01 01:01:30');
INSERT INTO `course_enrollment` (`enrollment_id`, `student_id`, `course_id`, `academic_year`, `semester`, `is_repeat`, `enrolled_at`) VALUES (135, 32, 1, '2024/2025', 1, 0, '2026-11-01 01:01:30');
INSERT INTO `course_enrollment` (`enrollment_id`, `student_id`, `course_id`, `academic_year`, `semester`, `is_repeat`, `enrolled_at`) VALUES (136, 32, 3, '2024/2025', 1, 0, '2026-11-01 01:01:30');
INSERT INTO `course_enrollment` (`enrollment_id`, `student_id`, `course_id`, `academic_year`, `semester`, `is_repeat`, `enrolled_at`) VALUES (137, 32, 4, '2024/2025', 1, 0, '2026-11-01 01:01:30');
INSERT INTO `course_enrollment` (`enrollment_id`, `student_id`, `course_id`, `academic_year`, `semester`, `is_repeat`, `enrolled_at`) VALUES (138, 35, 5, '2024', 1, 0, '2026-11-02 02:23:44');
INSERT INTO `course_enrollment` (`enrollment_id`, `student_id`, `course_id`, `academic_year`, `semester`, `is_repeat`, `enrolled_at`) VALUES (141, 35, 2, '2222', 1, 0, '2026-11-02 02:25:23');
INSERT INTO `course_enrollment` (`enrollment_id`, `student_id`, `course_id`, `academic_year`, `semester`, `is_repeat`, `enrolled_at`) VALUES (143, 12, 7, '2024', 1, 0, '2026-11-03 05:18:38');
INSERT INTO `course_enrollment` (`enrollment_id`, `student_id`, `course_id`, `academic_year`, `semester`, `is_repeat`, `enrolled_at`) VALUES (146, 11, 16, '2024/2025', 1, 0, '2026-11-03 15:41:08');
INSERT INTO `course_enrollment` (`enrollment_id`, `student_id`, `course_id`, `academic_year`, `semester`, `is_repeat`, `enrolled_at`) VALUES (147, 12, 16, '2024/2025', 1, 0, '2026-11-03 15:41:08');
INSERT INTO `course_enrollment` (`enrollment_id`, `student_id`, `course_id`, `academic_year`, `semester`, `is_repeat`, `enrolled_at`) VALUES (148, 13, 16, '2024/2025', 1, 0, '2026-11-03 15:41:08');
INSERT INTO `course_enrollment` (`enrollment_id`, `student_id`, `course_id`, `academic_year`, `semester`, `is_repeat`, `enrolled_at`) VALUES (149, 14, 16, '2024/2025', 1, 0, '2026-11-03 15:41:08');
INSERT INTO `course_enrollment` (`enrollment_id`, `student_id`, `course_id`, `academic_year`, `semester`, `is_repeat`, `enrolled_at`) VALUES (150, 15, 16, '2024/2025', 1, 0, '2026-11-03 15:41:08');
INSERT INTO `course_enrollment` (`enrollment_id`, `student_id`, `course_id`, `academic_year`, `semester`, `is_repeat`, `enrolled_at`) VALUES (151, 16, 16, '2024/2025', 1, 0, '2026-11-03 15:41:08');
INSERT INTO `course_enrollment` (`enrollment_id`, `student_id`, `course_id`, `academic_year`, `semester`, `is_repeat`, `enrolled_at`) VALUES (152, 17, 16, '2024/2025', 1, 0, '2026-11-03 15:41:08');
INSERT INTO `course_enrollment` (`enrollment_id`, `student_id`, `course_id`, `academic_year`, `semester`, `is_repeat`, `enrolled_at`) VALUES (153, 18, 16, '2024/2025', 1, 0, '2026-11-03 15:41:08');
INSERT INTO `course_enrollment` (`enrollment_id`, `student_id`, `course_id`, `academic_year`, `semester`, `is_repeat`, `enrolled_at`) VALUES (154, 19, 16, '2024/2025', 1, 0, '2026-11-03 15:41:08');
INSERT INTO `course_enrollment` (`enrollment_id`, `student_id`, `course_id`, `academic_year`, `semester`, `is_repeat`, `enrolled_at`) VALUES (155, 20, 16, '2024/2025', 1, 0, '2026-11-03 15:41:08');
INSERT INTO `course_enrollment` (`enrollment_id`, `student_id`, `course_id`, `academic_year`, `semester`, `is_repeat`, `enrolled_at`) VALUES (156, 21, 16, '2024/2025', 1, 0, '2026-11-03 15:41:08');
INSERT INTO `course_enrollment` (`enrollment_id`, `student_id`, `course_id`, `academic_year`, `semester`, `is_repeat`, `enrolled_at`) VALUES (157, 50, 17, '2024/2025', 1, 0, '2026-11-03 15:41:08');
INSERT INTO `course_enrollment` (`enrollment_id`, `student_id`, `course_id`, `academic_year`, `semester`, `is_repeat`, `enrolled_at`) VALUES (158, 50, 7, '2024/2025', 1, 0, '2026-11-03 15:41:08');
INSERT INTO `course_enrollment` (`enrollment_id`, `student_id`, `course_id`, `academic_year`, `semester`, `is_repeat`, `enrolled_at`) VALUES (159, 50, 18, '2024/2025', 1, 0, '2026-11-03 15:41:08');
INSERT INTO `course_enrollment` (`enrollment_id`, `student_id`, `course_id`, `academic_year`, `semester`, `is_repeat`, `enrolled_at`) VALUES (160, 50, 19, '2024/2025', 1, 0, '2026-11-03 15:41:08');
INSERT INTO `course_enrollment` (`enrollment_id`, `student_id`, `course_id`, `academic_year`, `semester`, `is_repeat`, `enrolled_at`) VALUES (161, 50, 20, '2024/2025', 1, 0, '2026-11-03 15:41:08');
INSERT INTO `course_enrollment` (`enrollment_id`, `student_id`, `course_id`, `academic_year`, `semester`, `is_repeat`, `enrolled_at`) VALUES (162, 50, 5, '2024/2025', 1, 0, '2026-11-03 15:41:08');
INSERT INTO `course_enrollment` (`enrollment_id`, `student_id`, `course_id`, `academic_year`, `semester`, `is_repeat`, `enrolled_at`) VALUES (163, 50, 25, '2024/2025', 1, 0, '2026-11-03 15:41:08');
INSERT INTO `course_enrollment` (`enrollment_id`, `student_id`, `course_id`, `academic_year`, `semester`, `is_repeat`, `enrolled_at`) VALUES (164, 51, 17, '2024/2025', 1, 0, '2026-11-03 15:41:08');
INSERT INTO `course_enrollment` (`enrollment_id`, `student_id`, `course_id`, `academic_year`, `semester`, `is_repeat`, `enrolled_at`) VALUES (165, 51, 7, '2024/2025', 1, 0, '2026-11-03 15:41:08');
INSERT INTO `course_enrollment` (`enrollment_id`, `student_id`, `course_id`, `academic_year`, `semester`, `is_repeat`, `enrolled_at`) VALUES (166, 51, 18, '2024/2025', 1, 0, '2026-11-03 15:41:08');
INSERT INTO `course_enrollment` (`enrollment_id`, `student_id`, `course_id`, `academic_year`, `semester`, `is_repeat`, `enrolled_at`) VALUES (167, 51, 19, '2024/2025', 1, 0, '2026-11-03 15:41:08');
INSERT INTO `course_enrollment` (`enrollment_id`, `student_id`, `course_id`, `academic_year`, `semester`, `is_repeat`, `enrolled_at`) VALUES (168, 51, 20, '2024/2025', 1, 0, '2026-11-03 15:41:08');
INSERT INTO `course_enrollment` (`enrollment_id`, `student_id`, `course_id`, `academic_year`, `semester`, `is_repeat`, `enrolled_at`) VALUES (169, 51, 5, '2024/2025', 1, 0, '2026-11-03 15:41:08');
INSERT INTO `course_enrollment` (`enrollment_id`, `student_id`, `course_id`, `academic_year`, `semester`, `is_repeat`, `enrolled_at`) VALUES (170, 51, 25, '2024/2025', 1, 0, '2026-11-03 15:41:08');
INSERT INTO `course_enrollment` (`enrollment_id`, `student_id`, `course_id`, `academic_year`, `semester`, `is_repeat`, `enrolled_at`) VALUES (171, 52, 17, '2024/2025', 1, 0, '2026-11-03 15:41:08');
INSERT INTO `course_enrollment` (`enrollment_id`, `student_id`, `course_id`, `academic_year`, `semester`, `is_repeat`, `enrolled_at`) VALUES (172, 52, 7, '2024/2025', 1, 0, '2026-11-03 15:41:08');
INSERT INTO `course_enrollment` (`enrollment_id`, `student_id`, `course_id`, `academic_year`, `semester`, `is_repeat`, `enrolled_at`) VALUES (173, 52, 18, '2024/2025', 1, 0, '2026-11-03 15:41:08');
INSERT INTO `course_enrollment` (`enrollment_id`, `student_id`, `course_id`, `academic_year`, `semester`, `is_repeat`, `enrolled_at`) VALUES (174, 52, 19, '2024/2025', 1, 0, '2026-11-03 15:41:08');
INSERT INTO `course_enrollment` (`enrollment_id`, `student_id`, `course_id`, `academic_year`, `semester`, `is_repeat`, `enrolled_at`) VALUES (175, 52, 20, '2024/2025', 1, 0, '2026-11-03 15:41:08');
INSERT INTO `course_enrollment` (`enrollment_id`, `student_id`, `course_id`, `academic_year`, `semester`, `is_repeat`, `enrolled_at`) VALUES (176, 52, 5, '2024/2025', 1, 0, '2026-11-03 15:41:08');
INSERT INTO `course_enrollment` (`enrollment_id`, `student_id`, `course_id`, `academic_year`, `semester`, `is_repeat`, `enrolled_at`) VALUES (177, 52, 25, '2024/2025', 1, 0, '2026-11-03 15:41:08');
INSERT INTO `course_enrollment` (`enrollment_id`, `student_id`, `course_id`, `academic_year`, `semester`, `is_repeat`, `enrolled_at`) VALUES (178, 53, 17, '2024/2025', 1, 0, '2026-11-03 15:41:08');
INSERT INTO `course_enrollment` (`enrollment_id`, `student_id`, `course_id`, `academic_year`, `semester`, `is_repeat`, `enrolled_at`) VALUES (179, 53, 7, '2024/2025', 1, 0, '2026-11-03 15:41:08');
INSERT INTO `course_enrollment` (`enrollment_id`, `student_id`, `course_id`, `academic_year`, `semester`, `is_repeat`, `enrolled_at`) VALUES (180, 53, 18, '2024/2025', 1, 0, '2026-11-03 15:41:08');
INSERT INTO `course_enrollment` (`enrollment_id`, `student_id`, `course_id`, `academic_year`, `semester`, `is_repeat`, `enrolled_at`) VALUES (181, 53, 19, '2024/2025', 1, 0, '2026-11-03 15:41:08');
INSERT INTO `course_enrollment` (`enrollment_id`, `student_id`, `course_id`, `academic_year`, `semester`, `is_repeat`, `enrolled_at`) VALUES (182, 53, 20, '2024/2025', 1, 0, '2026-11-03 15:41:08');
INSERT INTO `course_enrollment` (`enrollment_id`, `student_id`, `course_id`, `academic_year`, `semester`, `is_repeat`, `enrolled_at`) VALUES (183, 53, 5, '2024/2025', 1, 0, '2026-11-03 15:41:08');
INSERT INTO `course_enrollment` (`enrollment_id`, `student_id`, `course_id`, `academic_year`, `semester`, `is_repeat`, `enrolled_at`) VALUES (184, 53, 25, '2024/2025', 1, 0, '2026-11-03 15:41:08');
INSERT INTO `course_enrollment` (`enrollment_id`, `student_id`, `course_id`, `academic_year`, `semester`, `is_repeat`, `enrolled_at`) VALUES (185, 54, 17, '2024/2025', 1, 0, '2026-11-03 15:41:08');
INSERT INTO `course_enrollment` (`enrollment_id`, `student_id`, `course_id`, `academic_year`, `semester`, `is_repeat`, `enrolled_at`) VALUES (186, 54, 7, '2024/2025', 1, 0, '2026-11-03 15:41:08');
INSERT INTO `course_enrollment` (`enrollment_id`, `student_id`, `course_id`, `academic_year`, `semester`, `is_repeat`, `enrolled_at`) VALUES (187, 54, 18, '2024/2025', 1, 0, '2026-11-03 15:41:08');
INSERT INTO `course_enrollment` (`enrollment_id`, `student_id`, `course_id`, `academic_year`, `semester`, `is_repeat`, `enrolled_at`) VALUES (188, 54, 19, '2024/2025', 1, 0, '2026-11-03 15:41:08');
INSERT INTO `course_enrollment` (`enrollment_id`, `student_id`, `course_id`, `academic_year`, `semester`, `is_repeat`, `enrolled_at`) VALUES (189, 54, 20, '2024/2025', 1, 0, '2026-11-03 15:41:08');
INSERT INTO `course_enrollment` (`enrollment_id`, `student_id`, `course_id`, `academic_year`, `semester`, `is_repeat`, `enrolled_at`) VALUES (190, 54, 5, '2024/2025', 1, 0, '2026-11-03 15:41:08');
INSERT INTO `course_enrollment` (`enrollment_id`, `student_id`, `course_id`, `academic_year`, `semester`, `is_repeat`, `enrolled_at`) VALUES (191, 54, 25, '2024/2025', 1, 0, '2026-11-03 15:41:08');
INSERT INTO `course_enrollment` (`enrollment_id`, `student_id`, `course_id`, `academic_year`, `semester`, `is_repeat`, `enrolled_at`) VALUES (192, 55, 6, '2024/2025', 1, 0, '2026-11-03 15:41:08');
INSERT INTO `course_enrollment` (`enrollment_id`, `student_id`, `course_id`, `academic_year`, `semester`, `is_repeat`, `enrolled_at`) VALUES (193, 55, 21, '2024/2025', 1, 0, '2026-11-03 15:41:08');
INSERT INTO `course_enrollment` (`enrollment_id`, `student_id`, `course_id`, `academic_year`, `semester`, `is_repeat`, `enrolled_at`) VALUES (194, 55, 22, '2024/2025', 1, 0, '2026-11-03 15:41:08');
INSERT INTO `course_enrollment` (`enrollment_id`, `student_id`, `course_id`, `academic_year`, `semester`, `is_repeat`, `enrolled_at`) VALUES (195, 55, 23, '2024/2025', 1, 0, '2026-11-03 15:41:08');
INSERT INTO `course_enrollment` (`enrollment_id`, `student_id`, `course_id`, `academic_year`, `semester`, `is_repeat`, `enrolled_at`) VALUES (196, 55, 24, '2024/2025', 1, 0, '2026-11-03 15:41:08');
INSERT INTO `course_enrollment` (`enrollment_id`, `student_id`, `course_id`, `academic_year`, `semester`, `is_repeat`, `enrolled_at`) VALUES (197, 55, 5, '2024/2025', 1, 0, '2026-11-03 15:41:08');
INSERT INTO `course_enrollment` (`enrollment_id`, `student_id`, `course_id`, `academic_year`, `semester`, `is_repeat`, `enrolled_at`) VALUES (198, 55, 25, '2024/2025', 1, 0, '2026-11-03 15:41:08');
INSERT INTO `course_enrollment` (`enrollment_id`, `student_id`, `course_id`, `academic_year`, `semester`, `is_repeat`, `enrolled_at`) VALUES (199, 56, 6, '2024/2025', 1, 0, '2026-11-03 15:41:08');
INSERT INTO `course_enrollment` (`enrollment_id`, `student_id`, `course_id`, `academic_year`, `semester`, `is_repeat`, `enrolled_at`) VALUES (200, 56, 21, '2024/2025', 1, 0, '2026-11-03 15:41:08');
INSERT INTO `course_enrollment` (`enrollment_id`, `student_id`, `course_id`, `academic_year`, `semester`, `is_repeat`, `enrolled_at`) VALUES (201, 56, 22, '2024/2025', 1, 0, '2026-11-03 15:41:08');
INSERT INTO `course_enrollment` (`enrollment_id`, `student_id`, `course_id`, `academic_year`, `semester`, `is_repeat`, `enrolled_at`) VALUES (202, 56, 23, '2024/2025', 1, 0, '2026-11-03 15:41:08');
INSERT INTO `course_enrollment` (`enrollment_id`, `student_id`, `course_id`, `academic_year`, `semester`, `is_repeat`, `enrolled_at`) VALUES (203, 56, 24, '2024/2025', 1, 0, '2026-11-03 15:41:08');
INSERT INTO `course_enrollment` (`enrollment_id`, `student_id`, `course_id`, `academic_year`, `semester`, `is_repeat`, `enrolled_at`) VALUES (204, 56, 5, '2024/2025', 1, 0, '2026-11-03 15:41:08');
INSERT INTO `course_enrollment` (`enrollment_id`, `student_id`, `course_id`, `academic_year`, `semester`, `is_repeat`, `enrolled_at`) VALUES (205, 56, 25, '2024/2025', 1, 0, '2026-11-03 15:41:08');
INSERT INTO `course_enrollment` (`enrollment_id`, `student_id`, `course_id`, `academic_year`, `semester`, `is_repeat`, `enrolled_at`) VALUES (206, 57, 6, '2024/2025', 1, 0, '2026-11-03 15:41:08');
INSERT INTO `course_enrollment` (`enrollment_id`, `student_id`, `course_id`, `academic_year`, `semester`, `is_repeat`, `enrolled_at`) VALUES (207, 57, 21, '2024/2025', 1, 0, '2026-11-03 15:41:08');
INSERT INTO `course_enrollment` (`enrollment_id`, `student_id`, `course_id`, `academic_year`, `semester`, `is_repeat`, `enrolled_at`) VALUES (208, 57, 22, '2024/2025', 1, 0, '2026-11-03 15:41:08');
INSERT INTO `course_enrollment` (`enrollment_id`, `student_id`, `course_id`, `academic_year`, `semester`, `is_repeat`, `enrolled_at`) VALUES (209, 57, 23, '2024/2025', 1, 0, '2026-11-03 15:41:08');
INSERT INTO `course_enrollment` (`enrollment_id`, `student_id`, `course_id`, `academic_year`, `semester`, `is_repeat`, `enrolled_at`) VALUES (210, 57, 24, '2024/2025', 1, 0, '2026-11-03 15:41:08');
INSERT INTO `course_enrollment` (`enrollment_id`, `student_id`, `course_id`, `academic_year`, `semester`, `is_repeat`, `enrolled_at`) VALUES (211, 57, 5, '2024/2025', 1, 0, '2026-11-03 15:41:08');
INSERT INTO `course_enrollment` (`enrollment_id`, `student_id`, `course_id`, `academic_year`, `semester`, `is_repeat`, `enrolled_at`) VALUES (212, 57, 25, '2024/2025', 1, 0, '2026-11-03 15:41:08');
INSERT INTO `course_enrollment` (`enrollment_id`, `student_id`, `course_id`, `academic_year`, `semester`, `is_repeat`, `enrolled_at`) VALUES (213, 58, 6, '2024/2025', 1, 0, '2026-11-03 15:41:09');
INSERT INTO `course_enrollment` (`enrollment_id`, `student_id`, `course_id`, `academic_year`, `semester`, `is_repeat`, `enrolled_at`) VALUES (214, 58, 21, '2024/2025', 1, 0, '2026-11-03 15:41:09');
INSERT INTO `course_enrollment` (`enrollment_id`, `student_id`, `course_id`, `academic_year`, `semester`, `is_repeat`, `enrolled_at`) VALUES (215, 58, 22, '2024/2025', 1, 0, '2026-11-03 15:41:09');
INSERT INTO `course_enrollment` (`enrollment_id`, `student_id`, `course_id`, `academic_year`, `semester`, `is_repeat`, `enrolled_at`) VALUES (216, 58, 23, '2024/2025', 1, 0, '2026-11-03 15:41:09');
INSERT INTO `course_enrollment` (`enrollment_id`, `student_id`, `course_id`, `academic_year`, `semester`, `is_repeat`, `enrolled_at`) VALUES (217, 58, 24, '2024/2025', 1, 0, '2026-11-03 15:41:09');
INSERT INTO `course_enrollment` (`enrollment_id`, `student_id`, `course_id`, `academic_year`, `semester`, `is_repeat`, `enrolled_at`) VALUES (218, 58, 5, '2024/2025', 1, 0, '2026-11-03 15:41:09');
INSERT INTO `course_enrollment` (`enrollment_id`, `student_id`, `course_id`, `academic_year`, `semester`, `is_repeat`, `enrolled_at`) VALUES (219, 58, 25, '2024/2025', 1, 0, '2026-11-03 15:41:09');
INSERT INTO `course_enrollment` (`enrollment_id`, `student_id`, `course_id`, `academic_year`, `semester`, `is_repeat`, `enrolled_at`) VALUES (220, 59, 6, '2024/2025', 1, 0, '2026-11-03 15:41:09');
INSERT INTO `course_enrollment` (`enrollment_id`, `student_id`, `course_id`, `academic_year`, `semester`, `is_repeat`, `enrolled_at`) VALUES (221, 59, 21, '2024/2025', 1, 0, '2026-11-03 15:41:09');
INSERT INTO `course_enrollment` (`enrollment_id`, `student_id`, `course_id`, `academic_year`, `semester`, `is_repeat`, `enrolled_at`) VALUES (222, 59, 22, '2024/2025', 1, 0, '2026-11-03 15:41:09');
INSERT INTO `course_enrollment` (`enrollment_id`, `student_id`, `course_id`, `academic_year`, `semester`, `is_repeat`, `enrolled_at`) VALUES (223, 59, 23, '2024/2025', 1, 0, '2026-11-03 15:41:09');
INSERT INTO `course_enrollment` (`enrollment_id`, `student_id`, `course_id`, `academic_year`, `semester`, `is_repeat`, `enrolled_at`) VALUES (224, 59, 24, '2024/2025', 1, 0, '2026-11-03 15:41:09');
INSERT INTO `course_enrollment` (`enrollment_id`, `student_id`, `course_id`, `academic_year`, `semester`, `is_repeat`, `enrolled_at`) VALUES (225, 59, 5, '2024/2025', 1, 0, '2026-11-03 15:41:09');
INSERT INTO `course_enrollment` (`enrollment_id`, `student_id`, `course_id`, `academic_year`, `semester`, `is_repeat`, `enrolled_at`) VALUES (226, 59, 25, '2024/2025', 1, 0, '2026-11-03 15:41:09');
INSERT INTO `course_enrollment` (`enrollment_id`, `student_id`, `course_id`, `academic_year`, `semester`, `is_repeat`, `enrolled_at`) VALUES (227, 60, 5, '2024/2025', 1, 0, '2026-11-03 15:41:09');
INSERT INTO `course_enrollment` (`enrollment_id`, `student_id`, `course_id`, `academic_year`, `semester`, `is_repeat`, `enrolled_at`) VALUES (228, 60, 25, '2024/2025', 1, 0, '2026-11-03 15:41:09');
INSERT INTO `course_enrollment` (`enrollment_id`, `student_id`, `course_id`, `academic_year`, `semester`, `is_repeat`, `enrolled_at`) VALUES (229, 60, 26, '2024/2025', 1, 0, '2026-11-03 15:41:09');
INSERT INTO `course_enrollment` (`enrollment_id`, `student_id`, `course_id`, `academic_year`, `semester`, `is_repeat`, `enrolled_at`) VALUES (230, 60, 27, '2024/2025', 1, 0, '2026-11-03 15:41:09');
INSERT INTO `course_enrollment` (`enrollment_id`, `student_id`, `course_id`, `academic_year`, `semester`, `is_repeat`, `enrolled_at`) VALUES (231, 60, 28, '2024/2025', 1, 0, '2026-11-03 15:41:09');
INSERT INTO `course_enrollment` (`enrollment_id`, `student_id`, `course_id`, `academic_year`, `semester`, `is_repeat`, `enrolled_at`) VALUES (232, 61, 5, '2024/2025', 1, 0, '2026-11-03 15:41:09');
INSERT INTO `course_enrollment` (`enrollment_id`, `student_id`, `course_id`, `academic_year`, `semester`, `is_repeat`, `enrolled_at`) VALUES (233, 61, 25, '2024/2025', 1, 0, '2026-11-03 15:41:09');
INSERT INTO `course_enrollment` (`enrollment_id`, `student_id`, `course_id`, `academic_year`, `semester`, `is_repeat`, `enrolled_at`) VALUES (234, 61, 26, '2024/2025', 1, 0, '2026-11-03 15:41:09');
INSERT INTO `course_enrollment` (`enrollment_id`, `student_id`, `course_id`, `academic_year`, `semester`, `is_repeat`, `enrolled_at`) VALUES (235, 61, 27, '2024/2025', 1, 0, '2026-11-03 15:41:09');
INSERT INTO `course_enrollment` (`enrollment_id`, `student_id`, `course_id`, `academic_year`, `semester`, `is_repeat`, `enrolled_at`) VALUES (236, 61, 28, '2024/2025', 1, 0, '2026-11-03 15:41:09');
INSERT INTO `course_enrollment` (`enrollment_id`, `student_id`, `course_id`, `academic_year`, `semester`, `is_repeat`, `enrolled_at`) VALUES (237, 62, 5, '2024/2025', 1, 0, '2026-11-03 15:41:09');
INSERT INTO `course_enrollment` (`enrollment_id`, `student_id`, `course_id`, `academic_year`, `semester`, `is_repeat`, `enrolled_at`) VALUES (238, 62, 25, '2024/2025', 1, 0, '2026-11-03 15:41:09');
INSERT INTO `course_enrollment` (`enrollment_id`, `student_id`, `course_id`, `academic_year`, `semester`, `is_repeat`, `enrolled_at`) VALUES (239, 62, 26, '2024/2025', 1, 0, '2026-11-03 15:41:09');
INSERT INTO `course_enrollment` (`enrollment_id`, `student_id`, `course_id`, `academic_year`, `semester`, `is_repeat`, `enrolled_at`) VALUES (240, 62, 27, '2024/2025', 1, 0, '2026-11-03 15:41:09');
INSERT INTO `course_enrollment` (`enrollment_id`, `student_id`, `course_id`, `academic_year`, `semester`, `is_repeat`, `enrolled_at`) VALUES (241, 62, 28, '2024/2025', 1, 0, '2026-11-03 15:41:09');
INSERT INTO `course_enrollment` (`enrollment_id`, `student_id`, `course_id`, `academic_year`, `semester`, `is_repeat`, `enrolled_at`) VALUES (242, 63, 5, '2024/2025', 1, 0, '2026-11-03 15:41:09');
INSERT INTO `course_enrollment` (`enrollment_id`, `student_id`, `course_id`, `academic_year`, `semester`, `is_repeat`, `enrolled_at`) VALUES (243, 63, 25, '2024/2025', 1, 0, '2026-11-03 15:41:09');
INSERT INTO `course_enrollment` (`enrollment_id`, `student_id`, `course_id`, `academic_year`, `semester`, `is_repeat`, `enrolled_at`) VALUES (244, 63, 26, '2024/2025', 1, 0, '2026-11-03 15:41:09');
INSERT INTO `course_enrollment` (`enrollment_id`, `student_id`, `course_id`, `academic_year`, `semester`, `is_repeat`, `enrolled_at`) VALUES (245, 63, 27, '2024/2025', 1, 0, '2026-11-03 15:41:09');
INSERT INTO `course_enrollment` (`enrollment_id`, `student_id`, `course_id`, `academic_year`, `semester`, `is_repeat`, `enrolled_at`) VALUES (246, 63, 28, '2024/2025', 1, 0, '2026-11-03 15:41:09');
INSERT INTO `course_enrollment` (`enrollment_id`, `student_id`, `course_id`, `academic_year`, `semester`, `is_repeat`, `enrolled_at`) VALUES (247, 64, 5, '2024/2025', 1, 0, '2026-11-03 15:41:09');
INSERT INTO `course_enrollment` (`enrollment_id`, `student_id`, `course_id`, `academic_year`, `semester`, `is_repeat`, `enrolled_at`) VALUES (248, 64, 25, '2024/2025', 1, 0, '2026-11-03 15:41:09');
INSERT INTO `course_enrollment` (`enrollment_id`, `student_id`, `course_id`, `academic_year`, `semester`, `is_repeat`, `enrolled_at`) VALUES (249, 64, 26, '2024/2025', 1, 0, '2026-11-03 15:41:09');
INSERT INTO `course_enrollment` (`enrollment_id`, `student_id`, `course_id`, `academic_year`, `semester`, `is_repeat`, `enrolled_at`) VALUES (250, 64, 27, '2024/2025', 1, 0, '2026-11-03 15:41:09');
INSERT INTO `course_enrollment` (`enrollment_id`, `student_id`, `course_id`, `academic_year`, `semester`, `is_repeat`, `enrolled_at`) VALUES (251, 64, 28, '2024/2025', 1, 0, '2026-11-03 15:41:09');
-- (209 records)

-- --------------------------------------------------------
-- Data for table `evaluations`
-- --------------------------------------------------------
INSERT INTO `evaluations` (`evaluation_id`, `course_id`, `evaluation_name`, `evaluation_type`, `max_marks`, `weightage_percent`, `semester`, `academic_year`) VALUES (1, 3, 'Quiz 01 - Java OOP Fundamentals', 'QUIZ', 100.0, 10.0, 1, '2024/2025');
INSERT INTO `evaluations` (`evaluation_id`, `course_id`, `evaluation_name`, `evaluation_type`, `max_marks`, `weightage_percent`, `semester`, `academic_year`) VALUES (2, 3, 'Quiz 02 - GUI & Event Handling', 'QUIZ', 100.0, 10.0, 1, '2024/2025');
INSERT INTO `evaluations` (`evaluation_id`, `course_id`, `evaluation_name`, `evaluation_type`, `max_marks`, `weightage_percent`, `semester`, `academic_year`) VALUES (3, 3, 'Assignment 01 - Desktop Application Design', 'ASSIGNMENT', 100.0, 20.0, 1, '2024/2025');
INSERT INTO `evaluations` (`evaluation_id`, `course_id`, `evaluation_name`, `evaluation_type`, `max_marks`, `weightage_percent`, `semester`, `academic_year`) VALUES (4, 3, 'Mid Semester Exam - Practical OOP', 'MID_EXAM', 100.0, 20.0, 1, '2024/2025');
INSERT INTO `evaluations` (`evaluation_id`, `course_id`, `evaluation_name`, `evaluation_type`, `max_marks`, `weightage_percent`, `semester`, `academic_year`) VALUES (5, 3, 'Final Theory Examination', 'FINAL_THEORY', 100.0, 50.0, 1, '2024/2025');
INSERT INTO `evaluations` (`evaluation_id`, `course_id`, `evaluation_name`, `evaluation_type`, `max_marks`, `weightage_percent`, `semester`, `academic_year`) VALUES (6, 3, 'Final Practical Examination', 'FINAL_PRACTICAL', 100.0, 50.0, 1, '2024/2025');
INSERT INTO `evaluations` (`evaluation_id`, `course_id`, `evaluation_name`, `evaluation_type`, `max_marks`, `weightage_percent`, `semester`, `academic_year`) VALUES (7, 1, 'Quiz 01 - Relational Algebra & SQL', 'QUIZ', 100.0, 15.0, 1, '2024/2025');
INSERT INTO `evaluations` (`evaluation_id`, `course_id`, `evaluation_name`, `evaluation_type`, `max_marks`, `weightage_percent`, `semester`, `academic_year`) VALUES (8, 1, 'Assignment 01 - Database Schema Normalization', 'ASSIGNMENT', 100.0, 25.0, 1, '2024/2025');
INSERT INTO `evaluations` (`evaluation_id`, `course_id`, `evaluation_name`, `evaluation_type`, `max_marks`, `weightage_percent`, `semester`, `academic_year`) VALUES (9, 1, 'Final Theory Examination', 'FINAL_THEORY', 100.0, 60.0, 1, '2024/2025');
INSERT INTO `evaluations` (`evaluation_id`, `course_id`, `evaluation_name`, `evaluation_type`, `max_marks`, `weightage_percent`, `semester`, `academic_year`) VALUES (10, 2, 'Assignment 01 - SRS Documentation', 'ASSIGNMENT', 100.0, 30.0, 1, '2024/2025');
INSERT INTO `evaluations` (`evaluation_id`, `course_id`, `evaluation_name`, `evaluation_type`, `max_marks`, `weightage_percent`, `semester`, `academic_year`) VALUES (11, 2, 'Final Theory Examination', 'FINAL_THEORY', 100.0, 70.0, 1, '2024/2025');
INSERT INTO `evaluations` (`evaluation_id`, `course_id`, `evaluation_name`, `evaluation_type`, `max_marks`, `weightage_percent`, `semester`, `academic_year`) VALUES (15, 3, 'QUIZ01', 'QUIZ', 100.0, 10.0, 1, '2024/2025');
INSERT INTO `evaluations` (`evaluation_id`, `course_id`, `evaluation_name`, `evaluation_type`, `max_marks`, `weightage_percent`, `semester`, `academic_year`) VALUES (17, 3, 'ABSSSSS', 'ASSIGNMENT', 100.0, 20.0, 1, '2024/2025');
INSERT INTO `evaluations` (`evaluation_id`, `course_id`, `evaluation_name`, `evaluation_type`, `max_marks`, `weightage_percent`, `semester`, `academic_year`) VALUES (23, 16, 'Quiz 01 - HTML & CSS Fundamentals', 'QUIZ', 100.0, 10.0, 1, '2024/2025');
INSERT INTO `evaluations` (`evaluation_id`, `course_id`, `evaluation_name`, `evaluation_type`, `max_marks`, `weightage_percent`, `semester`, `academic_year`) VALUES (24, 16, 'Assignment 01 - Dynamic Webpage Design', 'ASSIGNMENT', 100.0, 20.0, 1, '2024/2025');
INSERT INTO `evaluations` (`evaluation_id`, `course_id`, `evaluation_name`, `evaluation_type`, `max_marks`, `weightage_percent`, `semester`, `academic_year`) VALUES (25, 16, 'Mid Semester Exam', 'MID_EXAM', 100.0, 20.0, 1, '2024/2025');
INSERT INTO `evaluations` (`evaluation_id`, `course_id`, `evaluation_name`, `evaluation_type`, `max_marks`, `weightage_percent`, `semester`, `academic_year`) VALUES (26, 16, 'Final Theory Examination', 'FINAL_THEORY', 100.0, 50.0, 1, '2024/2025');
INSERT INTO `evaluations` (`evaluation_id`, `course_id`, `evaluation_name`, `evaluation_type`, `max_marks`, `weightage_percent`, `semester`, `academic_year`) VALUES (27, 17, 'Quiz 01 - Soil Science Basics', 'QUIZ', 100.0, 10.0, 1, '2024/2025');
INSERT INTO `evaluations` (`evaluation_id`, `course_id`, `evaluation_name`, `evaluation_type`, `max_marks`, `weightage_percent`, `semester`, `academic_year`) VALUES (28, 17, 'Assignment 01 - Crop Management Report', 'ASSIGNMENT', 100.0, 20.0, 1, '2024/2025');
INSERT INTO `evaluations` (`evaluation_id`, `course_id`, `evaluation_name`, `evaluation_type`, `max_marks`, `weightage_percent`, `semester`, `academic_year`) VALUES (29, 17, 'Mid Semester Exam', 'MID_EXAM', 100.0, 20.0, 1, '2024/2025');
INSERT INTO `evaluations` (`evaluation_id`, `course_id`, `evaluation_name`, `evaluation_type`, `max_marks`, `weightage_percent`, `semester`, `academic_year`) VALUES (30, 17, 'Final Theory Examination', 'FINAL_THEORY', 100.0, 50.0, 1, '2024/2025');
INSERT INTO `evaluations` (`evaluation_id`, `course_id`, `evaluation_name`, `evaluation_type`, `max_marks`, `weightage_percent`, `semester`, `academic_year`) VALUES (31, 19, 'Quiz 01 - Microbiology Principles', 'QUIZ', 100.0, 10.0, 1, '2024/2025');
INSERT INTO `evaluations` (`evaluation_id`, `course_id`, `evaluation_name`, `evaluation_type`, `max_marks`, `weightage_percent`, `semester`, `academic_year`) VALUES (32, 19, 'Assignment 01 - Fermentation Lab Report', 'ASSIGNMENT', 100.0, 20.0, 1, '2024/2025');
INSERT INTO `evaluations` (`evaluation_id`, `course_id`, `evaluation_name`, `evaluation_type`, `max_marks`, `weightage_percent`, `semester`, `academic_year`) VALUES (33, 19, 'Mid Semester Exam', 'MID_EXAM', 100.0, 20.0, 1, '2024/2025');
INSERT INTO `evaluations` (`evaluation_id`, `course_id`, `evaluation_name`, `evaluation_type`, `max_marks`, `weightage_percent`, `semester`, `academic_year`) VALUES (34, 19, 'Final Theory Examination', 'FINAL_THEORY', 100.0, 50.0, 1, '2024/2025');
INSERT INTO `evaluations` (`evaluation_id`, `course_id`, `evaluation_name`, `evaluation_type`, `max_marks`, `weightage_percent`, `semester`, `academic_year`) VALUES (35, 21, 'Quiz 01 - Statics and Equilibrium', 'QUIZ', 100.0, 10.0, 1, '2024/2025');
INSERT INTO `evaluations` (`evaluation_id`, `course_id`, `evaluation_name`, `evaluation_type`, `max_marks`, `weightage_percent`, `semester`, `academic_year`) VALUES (36, 21, 'Assignment 01 - Bridge Load Analysis', 'ASSIGNMENT', 100.0, 20.0, 1, '2024/2025');
INSERT INTO `evaluations` (`evaluation_id`, `course_id`, `evaluation_name`, `evaluation_type`, `max_marks`, `weightage_percent`, `semester`, `academic_year`) VALUES (37, 21, 'Mid Semester Exam', 'MID_EXAM', 100.0, 20.0, 1, '2024/2025');
INSERT INTO `evaluations` (`evaluation_id`, `course_id`, `evaluation_name`, `evaluation_type`, `max_marks`, `weightage_percent`, `semester`, `academic_year`) VALUES (38, 21, 'Final Theory Examination', 'FINAL_THEORY', 100.0, 50.0, 1, '2024/2025');
INSERT INTO `evaluations` (`evaluation_id`, `course_id`, `evaluation_name`, `evaluation_type`, `max_marks`, `weightage_percent`, `semester`, `academic_year`) VALUES (39, 22, 'Quiz 01 - PLC Programming', 'QUIZ', 100.0, 10.0, 1, '2024/2025');
INSERT INTO `evaluations` (`evaluation_id`, `course_id`, `evaluation_name`, `evaluation_type`, `max_marks`, `weightage_percent`, `semester`, `academic_year`) VALUES (40, 22, 'Assignment 01 - Automation Project', 'ASSIGNMENT', 100.0, 20.0, 1, '2024/2025');
INSERT INTO `evaluations` (`evaluation_id`, `course_id`, `evaluation_name`, `evaluation_type`, `max_marks`, `weightage_percent`, `semester`, `academic_year`) VALUES (41, 22, 'Final Theory Examination', 'FINAL_THEORY', 100.0, 50.0, 1, '2024/2025');
INSERT INTO `evaluations` (`evaluation_id`, `course_id`, `evaluation_name`, `evaluation_type`, `max_marks`, `weightage_percent`, `semester`, `academic_year`) VALUES (42, 25, 'Quiz 01 - Differentiation & Integration', 'QUIZ', 100.0, 10.0, 1, '2024/2025');
INSERT INTO `evaluations` (`evaluation_id`, `course_id`, `evaluation_name`, `evaluation_type`, `max_marks`, `weightage_percent`, `semester`, `academic_year`) VALUES (43, 25, 'Assignment 01 - Applied Math Problems', 'ASSIGNMENT', 100.0, 20.0, 1, '2024/2025');
INSERT INTO `evaluations` (`evaluation_id`, `course_id`, `evaluation_name`, `evaluation_type`, `max_marks`, `weightage_percent`, `semester`, `academic_year`) VALUES (44, 25, 'Mid Semester Exam', 'MID_EXAM', 100.0, 20.0, 1, '2024/2025');
INSERT INTO `evaluations` (`evaluation_id`, `course_id`, `evaluation_name`, `evaluation_type`, `max_marks`, `weightage_percent`, `semester`, `academic_year`) VALUES (45, 25, 'Final Theory Examination', 'FINAL_THEORY', 100.0, 50.0, 1, '2024/2025');
INSERT INTO `evaluations` (`evaluation_id`, `course_id`, `evaluation_name`, `evaluation_type`, `max_marks`, `weightage_percent`, `semester`, `academic_year`) VALUES (46, 26, 'Quiz 01 - Management Concepts', 'QUIZ', 100.0, 10.0, 1, '2024/2025');
INSERT INTO `evaluations` (`evaluation_id`, `course_id`, `evaluation_name`, `evaluation_type`, `max_marks`, `weightage_percent`, `semester`, `academic_year`) VALUES (47, 26, 'Assignment 01 - Business Plan', 'ASSIGNMENT', 100.0, 20.0, 1, '2024/2025');
INSERT INTO `evaluations` (`evaluation_id`, `course_id`, `evaluation_name`, `evaluation_type`, `max_marks`, `weightage_percent`, `semester`, `academic_year`) VALUES (48, 26, 'Final Theory Examination', 'FINAL_THEORY', 100.0, 50.0, 1, '2024/2025');
-- (39 records)

-- --------------------------------------------------------
-- Data for table `marks`
-- --------------------------------------------------------
INSERT INTO `marks` (`mark_id`, `evaluation_id`, `student_id`, `marks_obtained`, `entered_by`, `updated_at`) VALUES (1, 1, 11, 88.0, 2, '2026-11-01 01:01:30');
INSERT INTO `marks` (`mark_id`, `evaluation_id`, `student_id`, `marks_obtained`, `entered_by`, `updated_at`) VALUES (2, 2, 11, 92.0, 2, '2026-11-01 01:01:30');
INSERT INTO `marks` (`mark_id`, `evaluation_id`, `student_id`, `marks_obtained`, `entered_by`, `updated_at`) VALUES (3, 3, 11, 85.0, 2, '2026-11-01 01:01:30');
INSERT INTO `marks` (`mark_id`, `evaluation_id`, `student_id`, `marks_obtained`, `entered_by`, `updated_at`) VALUES (4, 4, 11, 90.0, 2, '2026-11-01 01:01:30');
INSERT INTO `marks` (`mark_id`, `evaluation_id`, `student_id`, `marks_obtained`, `entered_by`, `updated_at`) VALUES (5, 5, 11, 86.0, 2, '2026-11-01 01:01:30');
INSERT INTO `marks` (`mark_id`, `evaluation_id`, `student_id`, `marks_obtained`, `entered_by`, `updated_at`) VALUES (6, 6, 11, 91.0, 2, '2026-11-01 01:01:30');
INSERT INTO `marks` (`mark_id`, `evaluation_id`, `student_id`, `marks_obtained`, `entered_by`, `updated_at`) VALUES (7, 1, 12, 82.0, 2, '2026-11-01 01:01:30');
INSERT INTO `marks` (`mark_id`, `evaluation_id`, `student_id`, `marks_obtained`, `entered_by`, `updated_at`) VALUES (8, 2, 12, 84.0, 2, '2026-11-01 01:01:30');
INSERT INTO `marks` (`mark_id`, `evaluation_id`, `student_id`, `marks_obtained`, `entered_by`, `updated_at`) VALUES (9, 3, 12, 88.0, 2, '2026-11-01 01:01:30');
INSERT INTO `marks` (`mark_id`, `evaluation_id`, `student_id`, `marks_obtained`, `entered_by`, `updated_at`) VALUES (10, 4, 12, 80.0, 2, '2026-11-01 01:01:30');
INSERT INTO `marks` (`mark_id`, `evaluation_id`, `student_id`, `marks_obtained`, `entered_by`, `updated_at`) VALUES (11, 5, 12, 85.0, 2, '2026-11-01 01:01:30');
INSERT INTO `marks` (`mark_id`, `evaluation_id`, `student_id`, `marks_obtained`, `entered_by`, `updated_at`) VALUES (12, 6, 12, 87.0, 2, '2026-11-01 01:01:30');
INSERT INTO `marks` (`mark_id`, `evaluation_id`, `student_id`, `marks_obtained`, `entered_by`, `updated_at`) VALUES (13, 1, 13, 75.0, 2, '2026-11-01 01:01:30');
INSERT INTO `marks` (`mark_id`, `evaluation_id`, `student_id`, `marks_obtained`, `entered_by`, `updated_at`) VALUES (14, 2, 13, 78.0, 2, '2026-11-01 01:01:30');
INSERT INTO `marks` (`mark_id`, `evaluation_id`, `student_id`, `marks_obtained`, `entered_by`, `updated_at`) VALUES (15, 3, 13, 80.0, 2, '2026-11-01 01:01:30');
INSERT INTO `marks` (`mark_id`, `evaluation_id`, `student_id`, `marks_obtained`, `entered_by`, `updated_at`) VALUES (16, 4, 13, 76.0, 2, '2026-11-01 01:01:30');
INSERT INTO `marks` (`mark_id`, `evaluation_id`, `student_id`, `marks_obtained`, `entered_by`, `updated_at`) VALUES (17, 5, 13, 78.0, 2, '2026-11-01 01:01:30');
INSERT INTO `marks` (`mark_id`, `evaluation_id`, `student_id`, `marks_obtained`, `entered_by`, `updated_at`) VALUES (18, 6, 13, 82.0, 2, '2026-11-01 01:01:30');
INSERT INTO `marks` (`mark_id`, `evaluation_id`, `student_id`, `marks_obtained`, `entered_by`, `updated_at`) VALUES (19, 1, 14, 72.0, 2, '2026-11-01 01:01:30');
INSERT INTO `marks` (`mark_id`, `evaluation_id`, `student_id`, `marks_obtained`, `entered_by`, `updated_at`) VALUES (20, 2, 14, 70.0, 2, '2026-11-01 01:01:30');
INSERT INTO `marks` (`mark_id`, `evaluation_id`, `student_id`, `marks_obtained`, `entered_by`, `updated_at`) VALUES (21, 3, 14, 75.0, 2, '2026-11-01 01:01:30');
INSERT INTO `marks` (`mark_id`, `evaluation_id`, `student_id`, `marks_obtained`, `entered_by`, `updated_at`) VALUES (22, 4, 14, 74.0, 2, '2026-11-01 01:01:30');
INSERT INTO `marks` (`mark_id`, `evaluation_id`, `student_id`, `marks_obtained`, `entered_by`, `updated_at`) VALUES (23, 5, 14, 71.0, 2, '2026-11-01 01:01:30');
INSERT INTO `marks` (`mark_id`, `evaluation_id`, `student_id`, `marks_obtained`, `entered_by`, `updated_at`) VALUES (24, 6, 14, 75.0, 2, '2026-11-01 01:01:30');
INSERT INTO `marks` (`mark_id`, `evaluation_id`, `student_id`, `marks_obtained`, `entered_by`, `updated_at`) VALUES (25, 1, 15, 55.0, 2, '2026-11-01 01:01:30');
INSERT INTO `marks` (`mark_id`, `evaluation_id`, `student_id`, `marks_obtained`, `entered_by`, `updated_at`) VALUES (26, 2, 15, 58.0, 2, '2026-11-01 01:01:30');
INSERT INTO `marks` (`mark_id`, `evaluation_id`, `student_id`, `marks_obtained`, `entered_by`, `updated_at`) VALUES (27, 3, 15, 62.0, 2, '2026-11-01 01:01:30');
INSERT INTO `marks` (`mark_id`, `evaluation_id`, `student_id`, `marks_obtained`, `entered_by`, `updated_at`) VALUES (28, 4, 15, 54.0, 2, '2026-11-01 01:01:30');
INSERT INTO `marks` (`mark_id`, `evaluation_id`, `student_id`, `marks_obtained`, `entered_by`, `updated_at`) VALUES (29, 5, 15, 58.0, 2, '2026-11-01 01:01:30');
INSERT INTO `marks` (`mark_id`, `evaluation_id`, `student_id`, `marks_obtained`, `entered_by`, `updated_at`) VALUES (30, 6, 15, 60.0, 2, '2026-11-01 01:01:30');
INSERT INTO `marks` (`mark_id`, `evaluation_id`, `student_id`, `marks_obtained`, `entered_by`, `updated_at`) VALUES (31, 1, 16, 50.0, 2, '2026-11-01 01:01:30');
INSERT INTO `marks` (`mark_id`, `evaluation_id`, `student_id`, `marks_obtained`, `entered_by`, `updated_at`) VALUES (32, 2, 16, 52.0, 2, '2026-11-01 01:01:30');
INSERT INTO `marks` (`mark_id`, `evaluation_id`, `student_id`, `marks_obtained`, `entered_by`, `updated_at`) VALUES (33, 3, 16, 55.0, 2, '2026-11-01 01:01:30');
INSERT INTO `marks` (`mark_id`, `evaluation_id`, `student_id`, `marks_obtained`, `entered_by`, `updated_at`) VALUES (34, 4, 16, 50.0, 2, '2026-11-01 01:01:30');
INSERT INTO `marks` (`mark_id`, `evaluation_id`, `student_id`, `marks_obtained`, `entered_by`, `updated_at`) VALUES (35, 5, 16, 52.0, 2, '2026-11-01 01:01:30');
INSERT INTO `marks` (`mark_id`, `evaluation_id`, `student_id`, `marks_obtained`, `entered_by`, `updated_at`) VALUES (36, 6, 16, 54.0, 2, '2026-11-01 01:01:30');
INSERT INTO `marks` (`mark_id`, `evaluation_id`, `student_id`, `marks_obtained`, `entered_by`, `updated_at`) VALUES (37, 1, 17, 30.0, 2, '2026-11-01 01:01:30');
INSERT INTO `marks` (`mark_id`, `evaluation_id`, `student_id`, `marks_obtained`, `entered_by`, `updated_at`) VALUES (38, 2, 17, 32.0, 2, '2026-11-01 01:01:30');
INSERT INTO `marks` (`mark_id`, `evaluation_id`, `student_id`, `marks_obtained`, `entered_by`, `updated_at`) VALUES (39, 3, 17, 28.0, 2, '2026-11-01 01:01:30');
INSERT INTO `marks` (`mark_id`, `evaluation_id`, `student_id`, `marks_obtained`, `entered_by`, `updated_at`) VALUES (40, 4, 17, 35.0, 2, '2026-11-01 01:01:30');
INSERT INTO `marks` (`mark_id`, `evaluation_id`, `student_id`, `marks_obtained`, `entered_by`, `updated_at`) VALUES (41, 5, 17, 30.0, 2, '2026-11-01 01:01:30');
INSERT INTO `marks` (`mark_id`, `evaluation_id`, `student_id`, `marks_obtained`, `entered_by`, `updated_at`) VALUES (42, 6, 17, 25.0, 2, '2026-11-01 01:01:30');
INSERT INTO `marks` (`mark_id`, `evaluation_id`, `student_id`, `marks_obtained`, `entered_by`, `updated_at`) VALUES (43, 1, 18, 78.0, 2, '2026-11-01 01:01:30');
INSERT INTO `marks` (`mark_id`, `evaluation_id`, `student_id`, `marks_obtained`, `entered_by`, `updated_at`) VALUES (44, 2, 18, 80.0, 2, '2026-11-01 01:01:30');
INSERT INTO `marks` (`mark_id`, `evaluation_id`, `student_id`, `marks_obtained`, `entered_by`, `updated_at`) VALUES (45, 3, 18, 82.0, 2, '2026-11-01 01:01:30');
INSERT INTO `marks` (`mark_id`, `evaluation_id`, `student_id`, `marks_obtained`, `entered_by`, `updated_at`) VALUES (46, 4, 18, 79.0, 2, '2026-11-01 01:01:30');
INSERT INTO `marks` (`mark_id`, `evaluation_id`, `student_id`, `marks_obtained`, `entered_by`, `updated_at`) VALUES (47, 5, 18, 81.0, 2, '2026-11-01 01:01:30');
INSERT INTO `marks` (`mark_id`, `evaluation_id`, `student_id`, `marks_obtained`, `entered_by`, `updated_at`) VALUES (48, 6, 18, 84.0, 2, '2026-11-01 01:01:30');
INSERT INTO `marks` (`mark_id`, `evaluation_id`, `student_id`, `marks_obtained`, `entered_by`, `updated_at`) VALUES (49, 1, 19, 68.0, 2, '2026-11-01 01:01:30');
INSERT INTO `marks` (`mark_id`, `evaluation_id`, `student_id`, `marks_obtained`, `entered_by`, `updated_at`) VALUES (50, 2, 19, 72.0, 2, '2026-11-01 01:01:30');
INSERT INTO `marks` (`mark_id`, `evaluation_id`, `student_id`, `marks_obtained`, `entered_by`, `updated_at`) VALUES (51, 3, 19, 70.0, 2, '2026-11-01 01:01:30');
INSERT INTO `marks` (`mark_id`, `evaluation_id`, `student_id`, `marks_obtained`, `entered_by`, `updated_at`) VALUES (52, 4, 19, 71.0, 2, '2026-11-01 01:01:30');
INSERT INTO `marks` (`mark_id`, `evaluation_id`, `student_id`, `marks_obtained`, `entered_by`, `updated_at`) VALUES (53, 5, 19, 70.0, 2, '2026-11-01 01:01:30');
INSERT INTO `marks` (`mark_id`, `evaluation_id`, `student_id`, `marks_obtained`, `entered_by`, `updated_at`) VALUES (54, 6, 19, 73.0, 2, '2026-11-01 01:01:30');
INSERT INTO `marks` (`mark_id`, `evaluation_id`, `student_id`, `marks_obtained`, `entered_by`, `updated_at`) VALUES (55, 1, 20, 64.0, 2, '2026-11-01 01:01:30');
INSERT INTO `marks` (`mark_id`, `evaluation_id`, `student_id`, `marks_obtained`, `entered_by`, `updated_at`) VALUES (56, 2, 20, 66.0, 2, '2026-11-01 01:01:30');
INSERT INTO `marks` (`mark_id`, `evaluation_id`, `student_id`, `marks_obtained`, `entered_by`, `updated_at`) VALUES (57, 3, 20, 68.0, 2, '2026-11-01 01:01:30');
INSERT INTO `marks` (`mark_id`, `evaluation_id`, `student_id`, `marks_obtained`, `entered_by`, `updated_at`) VALUES (58, 4, 20, 62.0, 2, '2026-11-01 01:01:30');
INSERT INTO `marks` (`mark_id`, `evaluation_id`, `student_id`, `marks_obtained`, `entered_by`, `updated_at`) VALUES (59, 5, 20, 65.0, 2, '2026-11-01 01:01:30');
INSERT INTO `marks` (`mark_id`, `evaluation_id`, `student_id`, `marks_obtained`, `entered_by`, `updated_at`) VALUES (60, 6, 20, 67.0, 2, '2026-11-01 01:01:30');
INSERT INTO `marks` (`mark_id`, `evaluation_id`, `student_id`, `marks_obtained`, `entered_by`, `updated_at`) VALUES (61, 1, 29, 62.0, 2, '2026-11-01 01:01:30');
INSERT INTO `marks` (`mark_id`, `evaluation_id`, `student_id`, `marks_obtained`, `entered_by`, `updated_at`) VALUES (62, 2, 29, 65.0, 2, '2026-11-01 01:01:30');
INSERT INTO `marks` (`mark_id`, `evaluation_id`, `student_id`, `marks_obtained`, `entered_by`, `updated_at`) VALUES (63, 3, 29, 68.0, 2, '2026-11-01 01:01:30');
INSERT INTO `marks` (`mark_id`, `evaluation_id`, `student_id`, `marks_obtained`, `entered_by`, `updated_at`) VALUES (64, 4, 29, 64.0, 2, '2026-11-01 01:01:30');
INSERT INTO `marks` (`mark_id`, `evaluation_id`, `student_id`, `marks_obtained`, `entered_by`, `updated_at`) VALUES (65, 5, 29, 65.0, 2, '2026-11-01 01:01:30');
INSERT INTO `marks` (`mark_id`, `evaluation_id`, `student_id`, `marks_obtained`, `entered_by`, `updated_at`) VALUES (66, 6, 29, 66.0, 2, '2026-11-01 01:01:30');
INSERT INTO `marks` (`mark_id`, `evaluation_id`, `student_id`, `marks_obtained`, `entered_by`, `updated_at`) VALUES (67, 7, 11, 85.0, 3, '2026-11-01 01:01:30');
INSERT INTO `marks` (`mark_id`, `evaluation_id`, `student_id`, `marks_obtained`, `entered_by`, `updated_at`) VALUES (68, 8, 11, 90.0, 3, '2026-11-01 01:01:30');
INSERT INTO `marks` (`mark_id`, `evaluation_id`, `student_id`, `marks_obtained`, `entered_by`, `updated_at`) VALUES (69, 9, 11, 88.0, 3, '2026-11-01 01:01:30');
INSERT INTO `marks` (`mark_id`, `evaluation_id`, `student_id`, `marks_obtained`, `entered_by`, `updated_at`) VALUES (70, 7, 12, 80.0, 3, '2026-11-01 01:01:30');
INSERT INTO `marks` (`mark_id`, `evaluation_id`, `student_id`, `marks_obtained`, `entered_by`, `updated_at`) VALUES (71, 8, 12, 85.0, 3, '2026-11-01 01:01:30');
INSERT INTO `marks` (`mark_id`, `evaluation_id`, `student_id`, `marks_obtained`, `entered_by`, `updated_at`) VALUES (72, 9, 12, 82.0, 3, '2026-11-01 01:01:30');
INSERT INTO `marks` (`mark_id`, `evaluation_id`, `student_id`, `marks_obtained`, `entered_by`, `updated_at`) VALUES (73, 7, 13, 76.0, 3, '2026-11-01 01:01:30');
INSERT INTO `marks` (`mark_id`, `evaluation_id`, `student_id`, `marks_obtained`, `entered_by`, `updated_at`) VALUES (74, 8, 13, 80.0, 3, '2026-11-01 01:01:30');
INSERT INTO `marks` (`mark_id`, `evaluation_id`, `student_id`, `marks_obtained`, `entered_by`, `updated_at`) VALUES (75, 9, 13, 78.0, 3, '2026-11-01 01:01:30');
INSERT INTO `marks` (`mark_id`, `evaluation_id`, `student_id`, `marks_obtained`, `entered_by`, `updated_at`) VALUES (76, 7, 14, 72.0, 3, '2026-11-01 01:01:30');
INSERT INTO `marks` (`mark_id`, `evaluation_id`, `student_id`, `marks_obtained`, `entered_by`, `updated_at`) VALUES (77, 8, 14, 74.0, 3, '2026-11-01 01:01:30');
INSERT INTO `marks` (`mark_id`, `evaluation_id`, `student_id`, `marks_obtained`, `entered_by`, `updated_at`) VALUES (78, 9, 14, 73.0, 3, '2026-11-01 01:01:30');
INSERT INTO `marks` (`mark_id`, `evaluation_id`, `student_id`, `marks_obtained`, `entered_by`, `updated_at`) VALUES (79, 7, 30, 65.0, 3, '2026-11-01 01:01:30');
INSERT INTO `marks` (`mark_id`, `evaluation_id`, `student_id`, `marks_obtained`, `entered_by`, `updated_at`) VALUES (80, 8, 30, 68.0, 3, '2026-11-01 01:01:30');
INSERT INTO `marks` (`mark_id`, `evaluation_id`, `student_id`, `marks_obtained`, `entered_by`, `updated_at`) VALUES (81, 9, 30, 66.0, 3, '2026-11-01 01:01:30');
INSERT INTO `marks` (`mark_id`, `evaluation_id`, `student_id`, `marks_obtained`, `entered_by`, `updated_at`) VALUES (91, 27, 50, 82.0, 4, '2026-11-03 15:41:09');
INSERT INTO `marks` (`mark_id`, `evaluation_id`, `student_id`, `marks_obtained`, `entered_by`, `updated_at`) VALUES (92, 28, 50, 79.0, 4, '2026-11-03 15:41:09');
INSERT INTO `marks` (`mark_id`, `evaluation_id`, `student_id`, `marks_obtained`, `entered_by`, `updated_at`) VALUES (93, 29, 50, 85.0, 4, '2026-11-03 15:41:09');
INSERT INTO `marks` (`mark_id`, `evaluation_id`, `student_id`, `marks_obtained`, `entered_by`, `updated_at`) VALUES (94, 30, 50, 78.0, 4, '2026-11-03 15:41:09');
INSERT INTO `marks` (`mark_id`, `evaluation_id`, `student_id`, `marks_obtained`, `entered_by`, `updated_at`) VALUES (95, 27, 51, 71.0, 4, '2026-11-03 15:41:09');
INSERT INTO `marks` (`mark_id`, `evaluation_id`, `student_id`, `marks_obtained`, `entered_by`, `updated_at`) VALUES (96, 28, 51, 68.0, 4, '2026-11-03 15:41:09');
INSERT INTO `marks` (`mark_id`, `evaluation_id`, `student_id`, `marks_obtained`, `entered_by`, `updated_at`) VALUES (97, 29, 51, 74.0, 4, '2026-11-03 15:41:09');
INSERT INTO `marks` (`mark_id`, `evaluation_id`, `student_id`, `marks_obtained`, `entered_by`, `updated_at`) VALUES (98, 30, 51, 69.0, 4, '2026-11-03 15:41:09');
INSERT INTO `marks` (`mark_id`, `evaluation_id`, `student_id`, `marks_obtained`, `entered_by`, `updated_at`) VALUES (99, 27, 52, 91.0, 4, '2026-11-03 15:41:10');
INSERT INTO `marks` (`mark_id`, `evaluation_id`, `student_id`, `marks_obtained`, `entered_by`, `updated_at`) VALUES (100, 28, 52, 88.0, 4, '2026-11-03 15:41:10');
INSERT INTO `marks` (`mark_id`, `evaluation_id`, `student_id`, `marks_obtained`, `entered_by`, `updated_at`) VALUES (101, 29, 52, 93.0, 4, '2026-11-03 15:41:10');
INSERT INTO `marks` (`mark_id`, `evaluation_id`, `student_id`, `marks_obtained`, `entered_by`, `updated_at`) VALUES (102, 30, 52, 87.0, 4, '2026-11-03 15:41:10');
INSERT INTO `marks` (`mark_id`, `evaluation_id`, `student_id`, `marks_obtained`, `entered_by`, `updated_at`) VALUES (103, 27, 53, 65.0, 4, '2026-11-03 15:41:10');
INSERT INTO `marks` (`mark_id`, `evaluation_id`, `student_id`, `marks_obtained`, `entered_by`, `updated_at`) VALUES (104, 28, 53, 62.0, 4, '2026-11-03 15:41:10');
INSERT INTO `marks` (`mark_id`, `evaluation_id`, `student_id`, `marks_obtained`, `entered_by`, `updated_at`) VALUES (105, 29, 53, 67.0, 4, '2026-11-03 15:41:10');
INSERT INTO `marks` (`mark_id`, `evaluation_id`, `student_id`, `marks_obtained`, `entered_by`, `updated_at`) VALUES (106, 30, 53, 60.0, 4, '2026-11-03 15:41:10');
INSERT INTO `marks` (`mark_id`, `evaluation_id`, `student_id`, `marks_obtained`, `entered_by`, `updated_at`) VALUES (107, 27, 54, 57.0, 4, '2026-11-03 15:41:10');
INSERT INTO `marks` (`mark_id`, `evaluation_id`, `student_id`, `marks_obtained`, `entered_by`, `updated_at`) VALUES (108, 28, 54, 55.0, 4, '2026-11-03 15:41:10');
INSERT INTO `marks` (`mark_id`, `evaluation_id`, `student_id`, `marks_obtained`, `entered_by`, `updated_at`) VALUES (109, 29, 54, 59.0, 4, '2026-11-03 15:41:10');
INSERT INTO `marks` (`mark_id`, `evaluation_id`, `student_id`, `marks_obtained`, `entered_by`, `updated_at`) VALUES (110, 30, 54, 53.0, 4, '2026-11-03 15:41:10');
INSERT INTO `marks` (`mark_id`, `evaluation_id`, `student_id`, `marks_obtained`, `entered_by`, `updated_at`) VALUES (111, 35, 55, 78.0, 42, '2026-11-03 15:41:10');
INSERT INTO `marks` (`mark_id`, `evaluation_id`, `student_id`, `marks_obtained`, `entered_by`, `updated_at`) VALUES (112, 36, 55, 75.0, 42, '2026-11-03 15:41:10');
INSERT INTO `marks` (`mark_id`, `evaluation_id`, `student_id`, `marks_obtained`, `entered_by`, `updated_at`) VALUES (113, 37, 55, 80.0, 42, '2026-11-03 15:41:10');
INSERT INTO `marks` (`mark_id`, `evaluation_id`, `student_id`, `marks_obtained`, `entered_by`, `updated_at`) VALUES (114, 38, 55, 74.0, 42, '2026-11-03 15:41:10');
INSERT INTO `marks` (`mark_id`, `evaluation_id`, `student_id`, `marks_obtained`, `entered_by`, `updated_at`) VALUES (115, 35, 56, 88.0, 42, '2026-11-03 15:41:10');
INSERT INTO `marks` (`mark_id`, `evaluation_id`, `student_id`, `marks_obtained`, `entered_by`, `updated_at`) VALUES (116, 36, 56, 85.0, 42, '2026-11-03 15:41:10');
INSERT INTO `marks` (`mark_id`, `evaluation_id`, `student_id`, `marks_obtained`, `entered_by`, `updated_at`) VALUES (117, 37, 56, 90.0, 42, '2026-11-03 15:41:10');
INSERT INTO `marks` (`mark_id`, `evaluation_id`, `student_id`, `marks_obtained`, `entered_by`, `updated_at`) VALUES (118, 38, 56, 84.0, 42, '2026-11-03 15:41:10');
INSERT INTO `marks` (`mark_id`, `evaluation_id`, `student_id`, `marks_obtained`, `entered_by`, `updated_at`) VALUES (119, 35, 57, 63.0, 42, '2026-11-03 15:41:10');
INSERT INTO `marks` (`mark_id`, `evaluation_id`, `student_id`, `marks_obtained`, `entered_by`, `updated_at`) VALUES (120, 36, 57, 60.0, 42, '2026-11-03 15:41:10');
INSERT INTO `marks` (`mark_id`, `evaluation_id`, `student_id`, `marks_obtained`, `entered_by`, `updated_at`) VALUES (121, 37, 57, 65.0, 42, '2026-11-03 15:41:10');
INSERT INTO `marks` (`mark_id`, `evaluation_id`, `student_id`, `marks_obtained`, `entered_by`, `updated_at`) VALUES (122, 38, 57, 58.0, 42, '2026-11-03 15:41:10');
INSERT INTO `marks` (`mark_id`, `evaluation_id`, `student_id`, `marks_obtained`, `entered_by`, `updated_at`) VALUES (123, 35, 58, 72.0, 42, '2026-11-03 15:41:10');
INSERT INTO `marks` (`mark_id`, `evaluation_id`, `student_id`, `marks_obtained`, `entered_by`, `updated_at`) VALUES (124, 36, 58, 69.0, 42, '2026-11-03 15:41:10');
INSERT INTO `marks` (`mark_id`, `evaluation_id`, `student_id`, `marks_obtained`, `entered_by`, `updated_at`) VALUES (125, 37, 58, 74.0, 42, '2026-11-03 15:41:10');
INSERT INTO `marks` (`mark_id`, `evaluation_id`, `student_id`, `marks_obtained`, `entered_by`, `updated_at`) VALUES (126, 38, 58, 68.0, 42, '2026-11-03 15:41:10');
INSERT INTO `marks` (`mark_id`, `evaluation_id`, `student_id`, `marks_obtained`, `entered_by`, `updated_at`) VALUES (127, 35, 59, 45.0, 42, '2026-11-03 15:41:10');
INSERT INTO `marks` (`mark_id`, `evaluation_id`, `student_id`, `marks_obtained`, `entered_by`, `updated_at`) VALUES (128, 36, 59, 42.0, 42, '2026-11-03 15:41:10');
INSERT INTO `marks` (`mark_id`, `evaluation_id`, `student_id`, `marks_obtained`, `entered_by`, `updated_at`) VALUES (129, 37, 59, 47.0, 42, '2026-11-03 15:41:10');
INSERT INTO `marks` (`mark_id`, `evaluation_id`, `student_id`, `marks_obtained`, `entered_by`, `updated_at`) VALUES (130, 38, 59, 40.0, 42, '2026-11-03 15:41:10');
INSERT INTO `marks` (`mark_id`, `evaluation_id`, `student_id`, `marks_obtained`, `entered_by`, `updated_at`) VALUES (131, 42, 60, 85.0, 46, '2026-11-03 15:41:10');
INSERT INTO `marks` (`mark_id`, `evaluation_id`, `student_id`, `marks_obtained`, `entered_by`, `updated_at`) VALUES (132, 43, 60, 82.0, 46, '2026-11-03 15:41:10');
INSERT INTO `marks` (`mark_id`, `evaluation_id`, `student_id`, `marks_obtained`, `entered_by`, `updated_at`) VALUES (133, 44, 60, 88.0, 46, '2026-11-03 15:41:10');
INSERT INTO `marks` (`mark_id`, `evaluation_id`, `student_id`, `marks_obtained`, `entered_by`, `updated_at`) VALUES (134, 45, 60, 80.0, 46, '2026-11-03 15:41:10');
INSERT INTO `marks` (`mark_id`, `evaluation_id`, `student_id`, `marks_obtained`, `entered_by`, `updated_at`) VALUES (135, 42, 61, 76.0, 46, '2026-11-03 15:41:10');
INSERT INTO `marks` (`mark_id`, `evaluation_id`, `student_id`, `marks_obtained`, `entered_by`, `updated_at`) VALUES (136, 43, 61, 73.0, 46, '2026-11-03 15:41:10');
INSERT INTO `marks` (`mark_id`, `evaluation_id`, `student_id`, `marks_obtained`, `entered_by`, `updated_at`) VALUES (137, 44, 61, 78.0, 46, '2026-11-03 15:41:10');
INSERT INTO `marks` (`mark_id`, `evaluation_id`, `student_id`, `marks_obtained`, `entered_by`, `updated_at`) VALUES (138, 45, 61, 70.0, 46, '2026-11-03 15:41:10');
INSERT INTO `marks` (`mark_id`, `evaluation_id`, `student_id`, `marks_obtained`, `entered_by`, `updated_at`) VALUES (139, 42, 62, 92.0, 46, '2026-11-03 15:41:10');
INSERT INTO `marks` (`mark_id`, `evaluation_id`, `student_id`, `marks_obtained`, `entered_by`, `updated_at`) VALUES (140, 43, 62, 90.0, 46, '2026-11-03 15:41:10');
INSERT INTO `marks` (`mark_id`, `evaluation_id`, `student_id`, `marks_obtained`, `entered_by`, `updated_at`) VALUES (141, 44, 62, 94.0, 46, '2026-11-03 15:41:10');
INSERT INTO `marks` (`mark_id`, `evaluation_id`, `student_id`, `marks_obtained`, `entered_by`, `updated_at`) VALUES (142, 45, 62, 88.0, 46, '2026-11-03 15:41:10');
INSERT INTO `marks` (`mark_id`, `evaluation_id`, `student_id`, `marks_obtained`, `entered_by`, `updated_at`) VALUES (143, 42, 63, 68.0, 46, '2026-11-03 15:41:10');
INSERT INTO `marks` (`mark_id`, `evaluation_id`, `student_id`, `marks_obtained`, `entered_by`, `updated_at`) VALUES (144, 43, 63, 65.0, 46, '2026-11-03 15:41:10');
INSERT INTO `marks` (`mark_id`, `evaluation_id`, `student_id`, `marks_obtained`, `entered_by`, `updated_at`) VALUES (145, 44, 63, 70.0, 46, '2026-11-03 15:41:10');
INSERT INTO `marks` (`mark_id`, `evaluation_id`, `student_id`, `marks_obtained`, `entered_by`, `updated_at`) VALUES (146, 45, 63, 62.0, 46, '2026-11-03 15:41:10');
INSERT INTO `marks` (`mark_id`, `evaluation_id`, `student_id`, `marks_obtained`, `entered_by`, `updated_at`) VALUES (147, 42, 64, 55.0, 46, '2026-11-03 15:41:10');
INSERT INTO `marks` (`mark_id`, `evaluation_id`, `student_id`, `marks_obtained`, `entered_by`, `updated_at`) VALUES (148, 43, 64, 52.0, 46, '2026-11-03 15:41:10');
INSERT INTO `marks` (`mark_id`, `evaluation_id`, `student_id`, `marks_obtained`, `entered_by`, `updated_at`) VALUES (149, 44, 64, 57.0, 46, '2026-11-03 15:41:10');
INSERT INTO `marks` (`mark_id`, `evaluation_id`, `student_id`, `marks_obtained`, `entered_by`, `updated_at`) VALUES (150, 45, 64, 50.0, 46, '2026-11-03 15:41:10');
-- (141 records)

-- --------------------------------------------------------
-- Data for table `grades`
-- --------------------------------------------------------
INSERT INTO `grades` (`grade_id`, `student_id`, `course_id`, `academic_year`, `ca_mark`, `final_exam_mark`, `total_mark`, `grade_letter`, `grade_point`, `is_repeat`, `calculated_at`) VALUES (1, 11, 3, '2024/2025', 88.7, 88.5, 88.6, 'A+', 4.0, 0, '2026-11-01 01:01:30');
INSERT INTO `grades` (`grade_id`, `student_id`, `course_id`, `academic_year`, `ca_mark`, `final_exam_mark`, `total_mark`, `grade_letter`, `grade_point`, `is_repeat`, `calculated_at`) VALUES (2, 11, 1, '2024/2025', 87.5, 88.0, 87.8, 'A+', 4.0, 0, '2026-11-01 01:01:30');
INSERT INTO `grades` (`grade_id`, `student_id`, `course_id`, `academic_year`, `ca_mark`, `final_exam_mark`, `total_mark`, `grade_letter`, `grade_point`, `is_repeat`, `calculated_at`) VALUES (3, 12, 3, '2024/2025', 83.5, 86.0, 85.0, 'A+', 4.0, 0, '2026-11-01 01:01:30');
INSERT INTO `grades` (`grade_id`, `student_id`, `course_id`, `academic_year`, `ca_mark`, `final_exam_mark`, `total_mark`, `grade_letter`, `grade_point`, `is_repeat`, `calculated_at`) VALUES (4, 12, 1, '2024/2025', 82.5, 82.0, 82.2, 'A', 4.0, 0, '2026-11-01 01:01:30');
INSERT INTO `grades` (`grade_id`, `student_id`, `course_id`, `academic_year`, `ca_mark`, `final_exam_mark`, `total_mark`, `grade_letter`, `grade_point`, `is_repeat`, `calculated_at`) VALUES (5, 13, 3, '2024/2025', 77.2, 80.0, 78.9, 'A-', 3.7, 0, '2026-11-01 01:01:30');
INSERT INTO `grades` (`grade_id`, `student_id`, `course_id`, `academic_year`, `ca_mark`, `final_exam_mark`, `total_mark`, `grade_letter`, `grade_point`, `is_repeat`, `calculated_at`) VALUES (6, 13, 1, '2024/2025', 78.0, 78.0, 78.0, 'A-', 3.7, 0, '2026-11-01 01:01:30');
INSERT INTO `grades` (`grade_id`, `student_id`, `course_id`, `academic_year`, `ca_mark`, `final_exam_mark`, `total_mark`, `grade_letter`, `grade_point`, `is_repeat`, `calculated_at`) VALUES (7, 14, 3, '2024/2025', 72.7, 73.0, 72.9, 'B+', 3.3, 0, '2026-11-01 01:01:30');
INSERT INTO `grades` (`grade_id`, `student_id`, `course_id`, `academic_year`, `ca_mark`, `final_exam_mark`, `total_mark`, `grade_letter`, `grade_point`, `is_repeat`, `calculated_at`) VALUES (8, 14, 1, '2024/2025', 73.0, 73.0, 73.0, 'B+', 3.3, 0, '2026-11-01 01:01:30');
INSERT INTO `grades` (`grade_id`, `student_id`, `course_id`, `academic_year`, `ca_mark`, `final_exam_mark`, `total_mark`, `grade_letter`, `grade_point`, `is_repeat`, `calculated_at`) VALUES (9, 15, 3, '2024/2025', 57.2, 59.0, 58.3, 'C+', 2.3, 0, '2026-11-01 01:01:30');
INSERT INTO `grades` (`grade_id`, `student_id`, `course_id`, `academic_year`, `ca_mark`, `final_exam_mark`, `total_mark`, `grade_letter`, `grade_point`, `is_repeat`, `calculated_at`) VALUES (10, 16, 3, '2024/2025', 51.7, 53.0, 52.5, 'C', 2.0, 0, '2026-11-01 01:01:30');
INSERT INTO `grades` (`grade_id`, `student_id`, `course_id`, `academic_year`, `ca_mark`, `final_exam_mark`, `total_mark`, `grade_letter`, `grade_point`, `is_repeat`, `calculated_at`) VALUES (11, 17, 3, '2024/2025', 31.2, 27.5, 29.0, 'E', 0.0, 0, '2026-11-01 01:01:30');
INSERT INTO `grades` (`grade_id`, `student_id`, `course_id`, `academic_year`, `ca_mark`, `final_exam_mark`, `total_mark`, `grade_letter`, `grade_point`, `is_repeat`, `calculated_at`) VALUES (12, 18, 3, '2024/2025', 79.7, 82.5, 81.4, 'A', 4.0, 0, '2026-11-01 01:01:30');
INSERT INTO `grades` (`grade_id`, `student_id`, `course_id`, `academic_year`, `ca_mark`, `final_exam_mark`, `total_mark`, `grade_letter`, `grade_point`, `is_repeat`, `calculated_at`) VALUES (13, 29, 3, '2024/2025', 64.7, 65.5, 65.2, 'C', 2.0, 1, '2026-11-01 01:01:30');
INSERT INTO `grades` (`grade_id`, `student_id`, `course_id`, `academic_year`, `ca_mark`, `final_exam_mark`, `total_mark`, `grade_letter`, `grade_point`, `is_repeat`, `calculated_at`) VALUES (14, 30, 1, '2024/2025', 66.5, 66.0, 66.2, 'C', 2.0, 1, '2026-11-01 01:01:30');
INSERT INTO `grades` (`grade_id`, `student_id`, `course_id`, `academic_year`, `ca_mark`, `final_exam_mark`, `total_mark`, `grade_letter`, `grade_point`, `is_repeat`, `calculated_at`) VALUES (15, 50, 17, '2024/2025', 82.0, 78.0, 80.0, 'A', 4.0, 0, '2026-11-03 15:41:09');
INSERT INTO `grades` (`grade_id`, `student_id`, `course_id`, `academic_year`, `ca_mark`, `final_exam_mark`, `total_mark`, `grade_letter`, `grade_point`, `is_repeat`, `calculated_at`) VALUES (16, 51, 17, '2024/2025', 71.0, 69.0, 70.0, 'B+', 3.3, 0, '2026-11-03 15:41:09');
INSERT INTO `grades` (`grade_id`, `student_id`, `course_id`, `academic_year`, `ca_mark`, `final_exam_mark`, `total_mark`, `grade_letter`, `grade_point`, `is_repeat`, `calculated_at`) VALUES (17, 52, 17, '2024/2025', 90.6, 87.0, 88.8, 'A+', 4.0, 0, '2026-11-03 15:41:10');
INSERT INTO `grades` (`grade_id`, `student_id`, `course_id`, `academic_year`, `ca_mark`, `final_exam_mark`, `total_mark`, `grade_letter`, `grade_point`, `is_repeat`, `calculated_at`) VALUES (18, 53, 17, '2024/2025', 64.6, 60.0, 62.3, 'B-', 2.7, 0, '2026-11-03 15:41:10');
INSERT INTO `grades` (`grade_id`, `student_id`, `course_id`, `academic_year`, `ca_mark`, `final_exam_mark`, `total_mark`, `grade_letter`, `grade_point`, `is_repeat`, `calculated_at`) VALUES (19, 54, 17, '2024/2025', 57.0, 53.0, 55.0, 'C+', 2.3, 0, '2026-11-03 15:41:10');
INSERT INTO `grades` (`grade_id`, `student_id`, `course_id`, `academic_year`, `ca_mark`, `final_exam_mark`, `total_mark`, `grade_letter`, `grade_point`, `is_repeat`, `calculated_at`) VALUES (20, 55, 21, '2024/2025', 77.6, 74.0, 75.8, 'A-', 3.7, 0, '2026-11-03 15:41:10');
INSERT INTO `grades` (`grade_id`, `student_id`, `course_id`, `academic_year`, `ca_mark`, `final_exam_mark`, `total_mark`, `grade_letter`, `grade_point`, `is_repeat`, `calculated_at`) VALUES (21, 56, 21, '2024/2025', 87.6, 84.0, 85.8, 'A+', 4.0, 0, '2026-11-03 15:41:10');
INSERT INTO `grades` (`grade_id`, `student_id`, `course_id`, `academic_year`, `ca_mark`, `final_exam_mark`, `total_mark`, `grade_letter`, `grade_point`, `is_repeat`, `calculated_at`) VALUES (22, 57, 21, '2024/2025', 62.6, 58.0, 60.3, 'B-', 2.7, 0, '2026-11-03 15:41:10');
INSERT INTO `grades` (`grade_id`, `student_id`, `course_id`, `academic_year`, `ca_mark`, `final_exam_mark`, `total_mark`, `grade_letter`, `grade_point`, `is_repeat`, `calculated_at`) VALUES (23, 58, 21, '2024/2025', 71.6, 68.0, 69.8, 'B', 3.0, 0, '2026-11-03 15:41:10');
INSERT INTO `grades` (`grade_id`, `student_id`, `course_id`, `academic_year`, `ca_mark`, `final_exam_mark`, `total_mark`, `grade_letter`, `grade_point`, `is_repeat`, `calculated_at`) VALUES (24, 59, 21, '2024/2025', 44.6, 40.0, 42.3, 'D', 1.0, 0, '2026-11-03 15:41:10');
INSERT INTO `grades` (`grade_id`, `student_id`, `course_id`, `academic_year`, `ca_mark`, `final_exam_mark`, `total_mark`, `grade_letter`, `grade_point`, `is_repeat`, `calculated_at`) VALUES (25, 60, 25, '2024/2025', 85.0, 80.0, 82.5, 'A', 4.0, 0, '2026-11-03 15:41:10');
INSERT INTO `grades` (`grade_id`, `student_id`, `course_id`, `academic_year`, `ca_mark`, `final_exam_mark`, `total_mark`, `grade_letter`, `grade_point`, `is_repeat`, `calculated_at`) VALUES (26, 61, 25, '2024/2025', 75.6, 70.0, 72.8, 'B+', 3.3, 0, '2026-11-03 15:41:10');
INSERT INTO `grades` (`grade_id`, `student_id`, `course_id`, `academic_year`, `ca_mark`, `final_exam_mark`, `total_mark`, `grade_letter`, `grade_point`, `is_repeat`, `calculated_at`) VALUES (27, 62, 25, '2024/2025', 92.0, 88.0, 90.0, 'A+', 4.0, 0, '2026-11-03 15:41:10');
INSERT INTO `grades` (`grade_id`, `student_id`, `course_id`, `academic_year`, `ca_mark`, `final_exam_mark`, `total_mark`, `grade_letter`, `grade_point`, `is_repeat`, `calculated_at`) VALUES (28, 63, 25, '2024/2025', 67.6, 62.0, 64.8, 'B-', 2.7, 0, '2026-11-03 15:41:10');
INSERT INTO `grades` (`grade_id`, `student_id`, `course_id`, `academic_year`, `ca_mark`, `final_exam_mark`, `total_mark`, `grade_letter`, `grade_point`, `is_repeat`, `calculated_at`) VALUES (29, 64, 25, '2024/2025', 54.6, 50.0, 52.3, 'C', 2.0, 0, '2026-11-03 15:41:10');
-- (29 records)

-- --------------------------------------------------------
-- Data for table `quizzes`
-- --------------------------------------------------------
INSERT INTO `quizzes` (`id`, `course_id`, `lecturer_id`, `evaluation_id`, `title`, `duration_minutes`, `start_time`, `end_time`, `max_attempts`, `status`, `marks_released`, `file_path`, `created_at`) VALUES (4, 3, 34, 15, 'QUIZ01', 30, '2026-11-01T18:30', '2026-11-06T06:30', 1, 'PUBLISHED', 0, NULL, '2026-11-03 05:12:16');
INSERT INTO `quizzes` (`id`, `course_id`, `lecturer_id`, `evaluation_id`, `title`, `duration_minutes`, `start_time`, `end_time`, `max_attempts`, `status`, `marks_released`, `file_path`, `created_at`) VALUES (11, 1, 3, NULL, 'Quiz 01 - SQL Fundamentals', 20, '2026-09-01T08:00', '2026-12-31T23:59:59', 1, 'PUBLISHED', 0, NULL, '2026-11-03 15:41:09');
INSERT INTO `quizzes` (`id`, `course_id`, `lecturer_id`, `evaluation_id`, `title`, `duration_minutes`, `start_time`, `end_time`, `max_attempts`, `status`, `marks_released`, `file_path`, `created_at`) VALUES (12, 2, 2, NULL, 'Quiz 01 - Software Development Models', 25, '2026-09-01T08:00', '2026-12-31T23:59:59', 1, 'PUBLISHED', 0, NULL, '2026-11-03 15:41:09');
INSERT INTO `quizzes` (`id`, `course_id`, `lecturer_id`, `evaluation_id`, `title`, `duration_minutes`, `start_time`, `end_time`, `max_attempts`, `status`, `marks_released`, `file_path`, `created_at`) VALUES (13, 3, 2, NULL, 'Quiz 02 - Java Swing & Event Handling', 20, '2026-09-01T08:00', '2026-12-31T23:59:59', 1, 'PUBLISHED', 0, NULL, '2026-11-03 15:41:09');
INSERT INTO `quizzes` (`id`, `course_id`, `lecturer_id`, `evaluation_id`, `title`, `duration_minutes`, `start_time`, `end_time`, `max_attempts`, `status`, `marks_released`, `file_path`, `created_at`) VALUES (14, 4, 36, NULL, 'Quiz 01 - OSI Model & TCP/IP', 25, '2026-09-01T08:00', '2026-12-31T23:59:59', 1, 'PUBLISHED', 0, NULL, '2026-11-03 15:41:09');
INSERT INTO `quizzes` (`id`, `course_id`, `lecturer_id`, `evaluation_id`, `title`, `duration_minutes`, `start_time`, `end_time`, `max_attempts`, `status`, `marks_released`, `file_path`, `created_at`) VALUES (15, 16, 37, NULL, 'Quiz 01 - HTML & CSS Fundamentals', 20, '2026-09-01T08:00', '2026-12-31T23:59:59', 1, 'PUBLISHED', 0, NULL, '2026-11-03 15:41:09');
INSERT INTO `quizzes` (`id`, `course_id`, `lecturer_id`, `evaluation_id`, `title`, `duration_minutes`, `start_time`, `end_time`, `max_attempts`, `status`, `marks_released`, `file_path`, `created_at`) VALUES (16, 17, 4, NULL, 'Quiz 01 - Soil Science Fundamentals', 20, '2026-09-01T08:00', '2026-12-31T23:59:59', 1, 'PUBLISHED', 0, NULL, '2026-11-03 15:41:09');
INSERT INTO `quizzes` (`id`, `course_id`, `lecturer_id`, `evaluation_id`, `title`, `duration_minutes`, `start_time`, `end_time`, `max_attempts`, `status`, `marks_released`, `file_path`, `created_at`) VALUES (17, 19, 39, NULL, 'Quiz 01 - Microbiology Principles', 25, '2026-09-01T08:00', '2026-12-31T23:59:59', 1, 'PUBLISHED', 0, NULL, '2026-11-03 15:41:09');
INSERT INTO `quizzes` (`id`, `course_id`, `lecturer_id`, `evaluation_id`, `title`, `duration_minutes`, `start_time`, `end_time`, `max_attempts`, `status`, `marks_released`, `file_path`, `created_at`) VALUES (18, 21, 42, NULL, 'Quiz 01 - Statics and Equilibrium', 20, '2026-09-01T08:00', '2026-12-31T23:59:59', 1, 'PUBLISHED', 0, NULL, '2026-11-03 15:41:09');
INSERT INTO `quizzes` (`id`, `course_id`, `lecturer_id`, `evaluation_id`, `title`, `duration_minutes`, `start_time`, `end_time`, `max_attempts`, `status`, `marks_released`, `file_path`, `created_at`) VALUES (19, 22, 43, NULL, 'Quiz 01 - PLC Programming Basics', 25, '2026-09-01T08:00', '2026-12-31T23:59:59', 1, 'PUBLISHED', 0, NULL, '2026-11-03 15:41:09');
INSERT INTO `quizzes` (`id`, `course_id`, `lecturer_id`, `evaluation_id`, `title`, `duration_minutes`, `start_time`, `end_time`, `max_attempts`, `status`, `marks_released`, `file_path`, `created_at`) VALUES (20, 25, 46, NULL, 'Quiz 01 - Calculus Fundamentals', 20, '2026-09-01T08:00', '2026-12-31T23:59:59', 1, 'PUBLISHED', 0, NULL, '2026-11-03 15:41:09');
INSERT INTO `quizzes` (`id`, `course_id`, `lecturer_id`, `evaluation_id`, `title`, `duration_minutes`, `start_time`, `end_time`, `max_attempts`, `status`, `marks_released`, `file_path`, `created_at`) VALUES (21, 26, 47, NULL, 'Quiz 01 - Management Theory', 20, '2026-09-01T08:00', '2026-12-31T23:59:59', 1, 'PUBLISHED', 0, NULL, '2026-11-03 15:41:09');
-- (12 records)

-- --------------------------------------------------------
-- Data for table `quiz_questions`
-- --------------------------------------------------------
INSERT INTO `quiz_questions` (`id`, `quiz_id`, `question_text`, `question_type`, `option_a`, `option_b`, `option_c`, `option_d`, `correct_answer`, `marks`) VALUES (16, 11, 'What SQL command retrieves data?', 'MULTIPLE_CHOICE', 'SELECT', 'INSERT', 'DELETE', 'UPDATE', 'A', 3.00);
INSERT INTO `quiz_questions` (`id`, `quiz_id`, `question_text`, `question_type`, `option_a`, `option_b`, `option_c`, `option_d`, `correct_answer`, `marks`) VALUES (17, 11, 'NULL means zero in SQL.', 'TRUE_FALSE', NULL, NULL, NULL, NULL, 'False', 3.00);
INSERT INTO `quiz_questions` (`id`, `quiz_id`, `question_text`, `question_type`, `option_a`, `option_b`, `option_c`, `option_d`, `correct_answer`, `marks`) VALUES (18, 11, 'Write the SQL keyword for combining tables.', 'SHORT_ANSWER', NULL, NULL, NULL, NULL, 'JOIN', 4.00);
INSERT INTO `quiz_questions` (`id`, `quiz_id`, `question_text`, `question_type`, `option_a`, `option_b`, `option_c`, `option_d`, `correct_answer`, `marks`) VALUES (19, 12, 'Which model is iterative?', 'MULTIPLE_CHOICE', 'Waterfall', 'Agile Scrum', 'V-Model', 'Structured', 'B', 3.00);
INSERT INTO `quiz_questions` (`id`, `quiz_id`, `question_text`, `question_type`, `option_a`, `option_b`, `option_c`, `option_d`, `correct_answer`, `marks`) VALUES (20, 12, 'Agile delivers working software frequently.', 'TRUE_FALSE', NULL, NULL, NULL, NULL, 'True', 3.00);
INSERT INTO `quiz_questions` (`id`, `quiz_id`, `question_text`, `question_type`, `option_a`, `option_b`, `option_c`, `option_d`, `correct_answer`, `marks`) VALUES (21, 12, 'Name the document produced at requirements phase.', 'SHORT_ANSWER', NULL, NULL, NULL, NULL, 'SRS', 4.00);
INSERT INTO `quiz_questions` (`id`, `quiz_id`, `question_text`, `question_type`, `option_a`, `option_b`, `option_c`, `option_d`, `correct_answer`, `marks`) VALUES (22, 13, 'Which class creates a window in Swing?', 'MULTIPLE_CHOICE', 'JPanel', 'JFrame', 'JLabel', 'JButton', 'B', 3.00);
INSERT INTO `quiz_questions` (`id`, `quiz_id`, `question_text`, `question_type`, `option_a`, `option_b`, `option_c`, `option_d`, `correct_answer`, `marks`) VALUES (23, 13, 'JTextField is used for multi-line text input.', 'TRUE_FALSE', NULL, NULL, NULL, NULL, 'False', 3.00);
INSERT INTO `quiz_questions` (`id`, `quiz_id`, `question_text`, `question_type`, `option_a`, `option_b`, `option_c`, `option_d`, `correct_answer`, `marks`) VALUES (24, 13, 'What listener detects button clicks?', 'SHORT_ANSWER', NULL, NULL, NULL, NULL, 'ActionListener', 4.00);
INSERT INTO `quiz_questions` (`id`, `quiz_id`, `question_text`, `question_type`, `option_a`, `option_b`, `option_c`, `option_d`, `correct_answer`, `marks`) VALUES (25, 14, 'How many layers does OSI model have?', 'MULTIPLE_CHOICE', '5', '6', '7', '8', 'C', 3.00);
INSERT INTO `quiz_questions` (`id`, `quiz_id`, `question_text`, `question_type`, `option_a`, `option_b`, `option_c`, `option_d`, `correct_answer`, `marks`) VALUES (26, 14, 'TCP is a connectionless protocol.', 'TRUE_FALSE', NULL, NULL, NULL, NULL, 'False', 3.00);
INSERT INTO `quiz_questions` (`id`, `quiz_id`, `question_text`, `question_type`, `option_a`, `option_b`, `option_c`, `option_d`, `correct_answer`, `marks`) VALUES (27, 14, 'What is the default HTTP port?', 'SHORT_ANSWER', NULL, NULL, NULL, NULL, '80', 4.00);
INSERT INTO `quiz_questions` (`id`, `quiz_id`, `question_text`, `question_type`, `option_a`, `option_b`, `option_c`, `option_d`, `correct_answer`, `marks`) VALUES (28, 15, 'Which tag defines a hyperlink?', 'MULTIPLE_CHOICE', '<link>', '<a>', '<href>', '<url>', 'B', 3.00);
INSERT INTO `quiz_questions` (`id`, `quiz_id`, `question_text`, `question_type`, `option_a`, `option_b`, `option_c`, `option_d`, `correct_answer`, `marks`) VALUES (29, 15, 'CSS stands for Cascading Style Sheets.', 'TRUE_FALSE', NULL, NULL, NULL, NULL, 'True', 3.00);
INSERT INTO `quiz_questions` (`id`, `quiz_id`, `question_text`, `question_type`, `option_a`, `option_b`, `option_c`, `option_d`, `correct_answer`, `marks`) VALUES (30, 15, 'Write the CSS property to change text color.', 'SHORT_ANSWER', NULL, NULL, NULL, NULL, 'color', 4.00);
INSERT INTO `quiz_questions` (`id`, `quiz_id`, `question_text`, `question_type`, `option_a`, `option_b`, `option_c`, `option_d`, `correct_answer`, `marks`) VALUES (31, 16, 'Which soil type retains most water?', 'MULTIPLE_CHOICE', 'Sandy', 'Clay', 'Loam', 'Silt', 'B', 3.00);
INSERT INTO `quiz_questions` (`id`, `quiz_id`, `question_text`, `question_type`, `option_a`, `option_b`, `option_c`, `option_d`, `correct_answer`, `marks`) VALUES (32, 16, 'pH 7 is neutral in soil chemistry.', 'TRUE_FALSE', NULL, NULL, NULL, NULL, 'True', 3.00);
INSERT INTO `quiz_questions` (`id`, `quiz_id`, `question_text`, `question_type`, `option_a`, `option_b`, `option_c`, `option_d`, `correct_answer`, `marks`) VALUES (33, 16, 'Name the process plants use to make food.', 'SHORT_ANSWER', NULL, NULL, NULL, NULL, 'Photosynthesis', 4.00);
INSERT INTO `quiz_questions` (`id`, `quiz_id`, `question_text`, `question_type`, `option_a`, `option_b`, `option_c`, `option_d`, `correct_answer`, `marks`) VALUES (34, 17, 'Bacteria reproduce by which process?', 'MULTIPLE_CHOICE', 'Mitosis', 'Binary fission', 'Meiosis', 'Budding', 'B', 3.00);
INSERT INTO `quiz_questions` (`id`, `quiz_id`, `question_text`, `question_type`, `option_a`, `option_b`, `option_c`, `option_d`, `correct_answer`, `marks`) VALUES (35, 17, 'Viruses are living organisms.', 'TRUE_FALSE', NULL, NULL, NULL, NULL, 'False', 3.00);
INSERT INTO `quiz_questions` (`id`, `quiz_id`, `question_text`, `question_type`, `option_a`, `option_b`, `option_c`, `option_d`, `correct_answer`, `marks`) VALUES (36, 17, 'Name the process of converting sugars to alcohol.', 'SHORT_ANSWER', NULL, NULL, NULL, NULL, 'Fermentation', 4.00);
INSERT INTO `quiz_questions` (`id`, `quiz_id`, `question_text`, `question_type`, `option_a`, `option_b`, `option_c`, `option_d`, `correct_answer`, `marks`) VALUES (37, 18, 'Unit of force in SI system?', 'MULTIPLE_CHOICE', 'Joule', 'Pascal', 'Newton', 'Watt', 'C', 3.00);
INSERT INTO `quiz_questions` (`id`, `quiz_id`, `question_text`, `question_type`, `option_a`, `option_b`, `option_c`, `option_d`, `correct_answer`, `marks`) VALUES (38, 18, 'Moment of a force depends on distance.', 'TRUE_FALSE', NULL, NULL, NULL, NULL, 'True', 3.00);
INSERT INTO `quiz_questions` (`id`, `quiz_id`, `question_text`, `question_type`, `option_a`, `option_b`, `option_c`, `option_d`, `correct_answer`, `marks`) VALUES (39, 18, 'State Newton\'s first law in one sentence.', 'SHORT_ANSWER', NULL, NULL, NULL, NULL, 'An object at rest stays at rest unless acted upon by a force', 4.00);
INSERT INTO `quiz_questions` (`id`, `quiz_id`, `question_text`, `question_type`, `option_a`, `option_b`, `option_c`, `option_d`, `correct_answer`, `marks`) VALUES (40, 19, 'PLC stands for?', 'MULTIPLE_CHOICE', 'Power Logic Control', 'Programmable Logic Controller', 'Process Line Control', 'Program Load Command', 'B', 3.00);
INSERT INTO `quiz_questions` (`id`, `quiz_id`, `question_text`, `question_type`, `option_a`, `option_b`, `option_c`, `option_d`, `correct_answer`, `marks`) VALUES (41, 19, 'PLC programs are written in ladder logic only.', 'TRUE_FALSE', NULL, NULL, NULL, NULL, 'False', 3.00);
INSERT INTO `quiz_questions` (`id`, `quiz_id`, `question_text`, `question_type`, `option_a`, `option_b`, `option_c`, `option_d`, `correct_answer`, `marks`) VALUES (42, 19, 'Name the sensor type that detects metal.', 'SHORT_ANSWER', NULL, NULL, NULL, NULL, 'Inductive sensor', 4.00);
INSERT INTO `quiz_questions` (`id`, `quiz_id`, `question_text`, `question_type`, `option_a`, `option_b`, `option_c`, `option_d`, `correct_answer`, `marks`) VALUES (43, 20, 'Derivative of x^2 is?', 'MULTIPLE_CHOICE', 'x', '2x', '2', 'x^3', 'B', 3.00);
INSERT INTO `quiz_questions` (`id`, `quiz_id`, `question_text`, `question_type`, `option_a`, `option_b`, `option_c`, `option_d`, `correct_answer`, `marks`) VALUES (44, 20, 'Integral is the inverse of differentiation.', 'TRUE_FALSE', NULL, NULL, NULL, NULL, 'True', 3.00);
INSERT INTO `quiz_questions` (`id`, `quiz_id`, `question_text`, `question_type`, `option_a`, `option_b`, `option_c`, `option_d`, `correct_answer`, `marks`) VALUES (45, 20, 'Write the formula for area under a curve.', 'SHORT_ANSWER', NULL, NULL, NULL, NULL, 'Definite integral', 4.00);
INSERT INTO `quiz_questions` (`id`, `quiz_id`, `question_text`, `question_type`, `option_a`, `option_b`, `option_c`, `option_d`, `correct_answer`, `marks`) VALUES (46, 21, 'Maslow\'s hierarchy top level is?', 'MULTIPLE_CHOICE', 'Safety', 'Esteem', 'Physiological', 'Self-actualization', 'D', 3.00);
INSERT INTO `quiz_questions` (`id`, `quiz_id`, `question_text`, `question_type`, `option_a`, `option_b`, `option_c`, `option_d`, `correct_answer`, `marks`) VALUES (47, 21, 'Scientific management was proposed by Taylor.', 'TRUE_FALSE', NULL, NULL, NULL, NULL, 'True', 3.00);
INSERT INTO `quiz_questions` (`id`, `quiz_id`, `question_text`, `question_type`, `option_a`, `option_b`, `option_c`, `option_d`, `correct_answer`, `marks`) VALUES (48, 21, 'Name 3 functions of management.', 'SHORT_ANSWER', NULL, NULL, NULL, NULL, 'Planning, Organizing, Controlling', 4.00);
-- (33 records)

-- --------------------------------------------------------
-- Data for table `quiz_attempts`
-- --------------------------------------------------------
INSERT INTO `quiz_attempts` (`id`, `quiz_id`, `student_id`, `started_at`, `submitted_at`, `score`, `total_marks`, `status`, `graded_by`, `graded_at`) VALUES (8, 11, 11, '2026-10-10T09:00', '2026-10-10T09:28', 10.00, 10.00, 'COMPLETED', 3, '2026-10-11T16:00');
INSERT INTO `quiz_attempts` (`id`, `quiz_id`, `student_id`, `started_at`, `submitted_at`, `score`, `total_marks`, `status`, `graded_by`, `graded_at`) VALUES (9, 11, 12, '2026-10-12T09:00', '2026-11-03T11:21:15', 0.00, 10.00, 'SUBMITTED', 3, '2026-10-13T16:00');
INSERT INTO `quiz_attempts` (`id`, `quiz_id`, `student_id`, `started_at`, `submitted_at`, `score`, `total_marks`, `status`, `graded_by`, `graded_at`) VALUES (10, 11, 13, '2026-10-14T09:00', '2026-10-14T09:28', 7.00, 10.00, 'COMPLETED', 3, '2026-10-15T16:00');
INSERT INTO `quiz_attempts` (`id`, `quiz_id`, `student_id`, `started_at`, `submitted_at`, `score`, `total_marks`, `status`, `graded_by`, `graded_at`) VALUES (11, 11, 14, '2026-10-16T09:00', '2026-10-16T09:28', 10.00, 10.00, 'COMPLETED', 3, '2026-10-17T16:00');
INSERT INTO `quiz_attempts` (`id`, `quiz_id`, `student_id`, `started_at`, `submitted_at`, `score`, `total_marks`, `status`, `graded_by`, `graded_at`) VALUES (12, 12, 11, '2026-10-10T09:00', '2026-10-10T09:28', 10.00, 10.00, 'COMPLETED', 2, '2026-10-11T16:00');
INSERT INTO `quiz_attempts` (`id`, `quiz_id`, `student_id`, `started_at`, `submitted_at`, `score`, `total_marks`, `status`, `graded_by`, `graded_at`) VALUES (13, 12, 12, '2026-10-12T09:00', '2026-10-12T09:28', 7.00, 10.00, 'COMPLETED', 2, '2026-10-13T16:00');
INSERT INTO `quiz_attempts` (`id`, `quiz_id`, `student_id`, `started_at`, `submitted_at`, `score`, `total_marks`, `status`, `graded_by`, `graded_at`) VALUES (14, 12, 13, '2026-10-14T09:00', '2026-10-14T09:28', 7.00, 10.00, 'COMPLETED', 2, '2026-10-15T16:00');
INSERT INTO `quiz_attempts` (`id`, `quiz_id`, `student_id`, `started_at`, `submitted_at`, `score`, `total_marks`, `status`, `graded_by`, `graded_at`) VALUES (15, 12, 14, '2026-10-16T09:00', '2026-10-16T09:28', 10.00, 10.00, 'COMPLETED', 2, '2026-10-17T16:00');
INSERT INTO `quiz_attempts` (`id`, `quiz_id`, `student_id`, `started_at`, `submitted_at`, `score`, `total_marks`, `status`, `graded_by`, `graded_at`) VALUES (16, 13, 11, '2026-10-10T09:00', '2026-10-10T09:28', 10.00, 10.00, 'COMPLETED', 2, '2026-10-11T16:00');
INSERT INTO `quiz_attempts` (`id`, `quiz_id`, `student_id`, `started_at`, `submitted_at`, `score`, `total_marks`, `status`, `graded_by`, `graded_at`) VALUES (17, 13, 12, '2026-10-12T09:00', '2026-10-12T09:28', 7.00, 10.00, 'COMPLETED', 2, '2026-10-13T16:00');
INSERT INTO `quiz_attempts` (`id`, `quiz_id`, `student_id`, `started_at`, `submitted_at`, `score`, `total_marks`, `status`, `graded_by`, `graded_at`) VALUES (18, 13, 13, '2026-10-14T09:00', '2026-10-14T09:28', 7.00, 10.00, 'COMPLETED', 2, '2026-10-15T16:00');
INSERT INTO `quiz_attempts` (`id`, `quiz_id`, `student_id`, `started_at`, `submitted_at`, `score`, `total_marks`, `status`, `graded_by`, `graded_at`) VALUES (19, 13, 14, '2026-10-16T09:00', '2026-10-16T09:28', 10.00, 10.00, 'COMPLETED', 2, '2026-10-17T16:00');
INSERT INTO `quiz_attempts` (`id`, `quiz_id`, `student_id`, `started_at`, `submitted_at`, `score`, `total_marks`, `status`, `graded_by`, `graded_at`) VALUES (20, 14, 11, '2026-10-10T09:00', '2026-10-10T09:28', 10.00, 10.00, 'COMPLETED', 36, '2026-10-11T16:00');
INSERT INTO `quiz_attempts` (`id`, `quiz_id`, `student_id`, `started_at`, `submitted_at`, `score`, `total_marks`, `status`, `graded_by`, `graded_at`) VALUES (21, 14, 12, '2026-10-12T09:00', '2026-10-12T09:28', 7.00, 10.00, 'COMPLETED', 36, '2026-10-13T16:00');
INSERT INTO `quiz_attempts` (`id`, `quiz_id`, `student_id`, `started_at`, `submitted_at`, `score`, `total_marks`, `status`, `graded_by`, `graded_at`) VALUES (22, 14, 13, '2026-10-14T09:00', '2026-10-14T09:28', 7.00, 10.00, 'COMPLETED', 36, '2026-10-15T16:00');
INSERT INTO `quiz_attempts` (`id`, `quiz_id`, `student_id`, `started_at`, `submitted_at`, `score`, `total_marks`, `status`, `graded_by`, `graded_at`) VALUES (23, 14, 14, '2026-10-16T09:00', '2026-10-16T09:28', 10.00, 10.00, 'COMPLETED', 36, '2026-10-17T16:00');
INSERT INTO `quiz_attempts` (`id`, `quiz_id`, `student_id`, `started_at`, `submitted_at`, `score`, `total_marks`, `status`, `graded_by`, `graded_at`) VALUES (24, 15, 11, '2026-10-10T09:00', '2026-10-10T09:28', 10.00, 10.00, 'COMPLETED', 37, '2026-10-11T16:00');
INSERT INTO `quiz_attempts` (`id`, `quiz_id`, `student_id`, `started_at`, `submitted_at`, `score`, `total_marks`, `status`, `graded_by`, `graded_at`) VALUES (25, 15, 12, '2026-10-12T09:00', '2026-10-12T09:28', 7.00, 10.00, 'COMPLETED', 37, '2026-10-13T16:00');
INSERT INTO `quiz_attempts` (`id`, `quiz_id`, `student_id`, `started_at`, `submitted_at`, `score`, `total_marks`, `status`, `graded_by`, `graded_at`) VALUES (26, 15, 13, '2026-10-14T09:00', '2026-10-14T09:28', 7.00, 10.00, 'COMPLETED', 37, '2026-10-15T16:00');
INSERT INTO `quiz_attempts` (`id`, `quiz_id`, `student_id`, `started_at`, `submitted_at`, `score`, `total_marks`, `status`, `graded_by`, `graded_at`) VALUES (27, 15, 14, '2026-10-16T09:00', '2026-10-16T09:28', 10.00, 10.00, 'COMPLETED', 37, '2026-10-17T16:00');
INSERT INTO `quiz_attempts` (`id`, `quiz_id`, `student_id`, `started_at`, `submitted_at`, `score`, `total_marks`, `status`, `graded_by`, `graded_at`) VALUES (28, 16, 50, '2026-10-10T09:00', '2026-10-10T09:28', 10.00, 10.00, 'COMPLETED', 4, '2026-10-11T16:00');
INSERT INTO `quiz_attempts` (`id`, `quiz_id`, `student_id`, `started_at`, `submitted_at`, `score`, `total_marks`, `status`, `graded_by`, `graded_at`) VALUES (29, 16, 51, '2026-10-12T09:00', '2026-10-12T09:28', 7.00, 10.00, 'COMPLETED', 4, '2026-10-13T16:00');
INSERT INTO `quiz_attempts` (`id`, `quiz_id`, `student_id`, `started_at`, `submitted_at`, `score`, `total_marks`, `status`, `graded_by`, `graded_at`) VALUES (30, 16, 52, '2026-10-14T09:00', '2026-10-14T09:28', 7.00, 10.00, 'COMPLETED', 4, '2026-10-15T16:00');
INSERT INTO `quiz_attempts` (`id`, `quiz_id`, `student_id`, `started_at`, `submitted_at`, `score`, `total_marks`, `status`, `graded_by`, `graded_at`) VALUES (31, 16, 53, '2026-10-16T09:00', '2026-10-16T09:28', 10.00, 10.00, 'COMPLETED', 4, '2026-10-17T16:00');
INSERT INTO `quiz_attempts` (`id`, `quiz_id`, `student_id`, `started_at`, `submitted_at`, `score`, `total_marks`, `status`, `graded_by`, `graded_at`) VALUES (32, 17, 50, '2026-10-10T09:00', '2026-10-10T09:28', 10.00, 10.00, 'COMPLETED', 39, '2026-10-11T16:00');
INSERT INTO `quiz_attempts` (`id`, `quiz_id`, `student_id`, `started_at`, `submitted_at`, `score`, `total_marks`, `status`, `graded_by`, `graded_at`) VALUES (33, 17, 51, '2026-10-12T09:00', '2026-10-12T09:28', 7.00, 10.00, 'COMPLETED', 39, '2026-10-13T16:00');
INSERT INTO `quiz_attempts` (`id`, `quiz_id`, `student_id`, `started_at`, `submitted_at`, `score`, `total_marks`, `status`, `graded_by`, `graded_at`) VALUES (34, 17, 52, '2026-10-14T09:00', '2026-10-14T09:28', 7.00, 10.00, 'COMPLETED', 39, '2026-10-15T16:00');
INSERT INTO `quiz_attempts` (`id`, `quiz_id`, `student_id`, `started_at`, `submitted_at`, `score`, `total_marks`, `status`, `graded_by`, `graded_at`) VALUES (35, 17, 53, '2026-10-16T09:00', '2026-10-16T09:28', 10.00, 10.00, 'COMPLETED', 39, '2026-10-17T16:00');
INSERT INTO `quiz_attempts` (`id`, `quiz_id`, `student_id`, `started_at`, `submitted_at`, `score`, `total_marks`, `status`, `graded_by`, `graded_at`) VALUES (36, 18, 55, '2026-10-10T09:00', '2026-10-10T09:28', 10.00, 10.00, 'COMPLETED', 42, '2026-10-11T16:00');
INSERT INTO `quiz_attempts` (`id`, `quiz_id`, `student_id`, `started_at`, `submitted_at`, `score`, `total_marks`, `status`, `graded_by`, `graded_at`) VALUES (37, 18, 56, '2026-10-12T09:00', '2026-10-12T09:28', 7.00, 10.00, 'COMPLETED', 42, '2026-10-13T16:00');
INSERT INTO `quiz_attempts` (`id`, `quiz_id`, `student_id`, `started_at`, `submitted_at`, `score`, `total_marks`, `status`, `graded_by`, `graded_at`) VALUES (38, 18, 57, '2026-10-14T09:00', '2026-10-14T09:28', 7.00, 10.00, 'COMPLETED', 42, '2026-10-15T16:00');
INSERT INTO `quiz_attempts` (`id`, `quiz_id`, `student_id`, `started_at`, `submitted_at`, `score`, `total_marks`, `status`, `graded_by`, `graded_at`) VALUES (39, 18, 58, '2026-10-16T09:00', '2026-10-16T09:28', 10.00, 10.00, 'COMPLETED', 42, '2026-10-17T16:00');
INSERT INTO `quiz_attempts` (`id`, `quiz_id`, `student_id`, `started_at`, `submitted_at`, `score`, `total_marks`, `status`, `graded_by`, `graded_at`) VALUES (40, 19, 55, '2026-10-10T09:00', '2026-10-10T09:28', 10.00, 10.00, 'COMPLETED', 43, '2026-10-11T16:00');
INSERT INTO `quiz_attempts` (`id`, `quiz_id`, `student_id`, `started_at`, `submitted_at`, `score`, `total_marks`, `status`, `graded_by`, `graded_at`) VALUES (41, 19, 56, '2026-10-12T09:00', '2026-10-12T09:28', 7.00, 10.00, 'COMPLETED', 43, '2026-10-13T16:00');
INSERT INTO `quiz_attempts` (`id`, `quiz_id`, `student_id`, `started_at`, `submitted_at`, `score`, `total_marks`, `status`, `graded_by`, `graded_at`) VALUES (42, 19, 57, '2026-10-14T09:00', '2026-10-14T09:28', 7.00, 10.00, 'COMPLETED', 43, '2026-10-15T16:00');
INSERT INTO `quiz_attempts` (`id`, `quiz_id`, `student_id`, `started_at`, `submitted_at`, `score`, `total_marks`, `status`, `graded_by`, `graded_at`) VALUES (43, 19, 58, '2026-10-16T09:00', '2026-10-16T09:28', 10.00, 10.00, 'COMPLETED', 43, '2026-10-17T16:00');
INSERT INTO `quiz_attempts` (`id`, `quiz_id`, `student_id`, `started_at`, `submitted_at`, `score`, `total_marks`, `status`, `graded_by`, `graded_at`) VALUES (44, 20, 50, '2026-10-10T09:00', '2026-10-10T09:28', 10.00, 10.00, 'COMPLETED', 46, '2026-10-11T16:00');
INSERT INTO `quiz_attempts` (`id`, `quiz_id`, `student_id`, `started_at`, `submitted_at`, `score`, `total_marks`, `status`, `graded_by`, `graded_at`) VALUES (45, 20, 51, '2026-10-12T09:00', '2026-10-12T09:28', 7.00, 10.00, 'COMPLETED', 46, '2026-10-13T16:00');
INSERT INTO `quiz_attempts` (`id`, `quiz_id`, `student_id`, `started_at`, `submitted_at`, `score`, `total_marks`, `status`, `graded_by`, `graded_at`) VALUES (46, 20, 52, '2026-10-14T09:00', '2026-10-14T09:28', 7.00, 10.00, 'COMPLETED', 46, '2026-10-15T16:00');
INSERT INTO `quiz_attempts` (`id`, `quiz_id`, `student_id`, `started_at`, `submitted_at`, `score`, `total_marks`, `status`, `graded_by`, `graded_at`) VALUES (47, 20, 53, '2026-10-16T09:00', '2026-10-16T09:28', 10.00, 10.00, 'COMPLETED', 46, '2026-10-17T16:00');
INSERT INTO `quiz_attempts` (`id`, `quiz_id`, `student_id`, `started_at`, `submitted_at`, `score`, `total_marks`, `status`, `graded_by`, `graded_at`) VALUES (48, 21, 60, '2026-10-10T09:00', '2026-10-10T09:28', 10.00, 10.00, 'COMPLETED', 47, '2026-10-11T16:00');
INSERT INTO `quiz_attempts` (`id`, `quiz_id`, `student_id`, `started_at`, `submitted_at`, `score`, `total_marks`, `status`, `graded_by`, `graded_at`) VALUES (49, 21, 61, '2026-10-12T09:00', '2026-10-12T09:28', 7.00, 10.00, 'COMPLETED', 47, '2026-10-13T16:00');
INSERT INTO `quiz_attempts` (`id`, `quiz_id`, `student_id`, `started_at`, `submitted_at`, `score`, `total_marks`, `status`, `graded_by`, `graded_at`) VALUES (50, 21, 62, '2026-10-14T09:00', '2026-10-14T09:28', 7.00, 10.00, 'COMPLETED', 47, '2026-10-15T16:00');
INSERT INTO `quiz_attempts` (`id`, `quiz_id`, `student_id`, `started_at`, `submitted_at`, `score`, `total_marks`, `status`, `graded_by`, `graded_at`) VALUES (51, 21, 63, '2026-10-16T09:00', '2026-10-16T09:28', 10.00, 10.00, 'COMPLETED', 47, '2026-10-17T16:00');
-- (44 records)

-- --------------------------------------------------------
-- Data for table `quiz_answers`
-- --------------------------------------------------------
INSERT INTO `quiz_answers` (`id`, `attempt_id`, `question_id`, `answer_text`, `awarded_marks`, `feedback`) VALUES (18, 8, 16, 'Selected Option 1 based on course lecture material.', 3.00, 'Correct answer.');
INSERT INTO `quiz_answers` (`id`, `attempt_id`, `question_id`, `answer_text`, `awarded_marks`, `feedback`) VALUES (19, 8, 17, 'Selected Option 1 based on course lecture material.', 3.00, 'Correct answer.');
INSERT INTO `quiz_answers` (`id`, `attempt_id`, `question_id`, `answer_text`, `awarded_marks`, `feedback`) VALUES (20, 8, 18, 'Selected Option 1 based on course lecture material.', 4.00, 'Correct answer.');
INSERT INTO `quiz_answers` (`id`, `attempt_id`, `question_id`, `answer_text`, `awarded_marks`, `feedback`) VALUES (21, 9, 16, 'Selected Option 2 based on course lecture material.', 0.00, 'Partially correct or minor inaccuracy.');
INSERT INTO `quiz_answers` (`id`, `attempt_id`, `question_id`, `answer_text`, `awarded_marks`, `feedback`) VALUES (22, 9, 17, 'Selected Option 2 based on course lecture material.', 0.00, 'Partially correct or minor inaccuracy.');
INSERT INTO `quiz_answers` (`id`, `attempt_id`, `question_id`, `answer_text`, `awarded_marks`, `feedback`) VALUES (23, 9, 18, 'Selected Option 2 based on course lecture material.', 0.00, 'Partially correct or minor inaccuracy.');
INSERT INTO `quiz_answers` (`id`, `attempt_id`, `question_id`, `answer_text`, `awarded_marks`, `feedback`) VALUES (24, 10, 16, 'Selected Option 3 based on course lecture material.', 2.10, 'Partially correct or minor inaccuracy.');
INSERT INTO `quiz_answers` (`id`, `attempt_id`, `question_id`, `answer_text`, `awarded_marks`, `feedback`) VALUES (25, 10, 17, 'Selected Option 3 based on course lecture material.', 2.10, 'Partially correct or minor inaccuracy.');
INSERT INTO `quiz_answers` (`id`, `attempt_id`, `question_id`, `answer_text`, `awarded_marks`, `feedback`) VALUES (26, 10, 18, 'Selected Option 3 based on course lecture material.', 2.80, 'Partially correct or minor inaccuracy.');
INSERT INTO `quiz_answers` (`id`, `attempt_id`, `question_id`, `answer_text`, `awarded_marks`, `feedback`) VALUES (27, 11, 16, 'Selected Option 4 based on course lecture material.', 3.00, 'Correct answer.');
INSERT INTO `quiz_answers` (`id`, `attempt_id`, `question_id`, `answer_text`, `awarded_marks`, `feedback`) VALUES (28, 11, 17, 'Selected Option 4 based on course lecture material.', 3.00, 'Correct answer.');
INSERT INTO `quiz_answers` (`id`, `attempt_id`, `question_id`, `answer_text`, `awarded_marks`, `feedback`) VALUES (29, 11, 18, 'Selected Option 4 based on course lecture material.', 4.00, 'Correct answer.');
INSERT INTO `quiz_answers` (`id`, `attempt_id`, `question_id`, `answer_text`, `awarded_marks`, `feedback`) VALUES (30, 12, 19, 'Selected Option 1 based on course lecture material.', 3.00, 'Correct answer.');
INSERT INTO `quiz_answers` (`id`, `attempt_id`, `question_id`, `answer_text`, `awarded_marks`, `feedback`) VALUES (31, 12, 20, 'Selected Option 1 based on course lecture material.', 3.00, 'Correct answer.');
INSERT INTO `quiz_answers` (`id`, `attempt_id`, `question_id`, `answer_text`, `awarded_marks`, `feedback`) VALUES (32, 12, 21, 'Selected Option 1 based on course lecture material.', 4.00, 'Correct answer.');
INSERT INTO `quiz_answers` (`id`, `attempt_id`, `question_id`, `answer_text`, `awarded_marks`, `feedback`) VALUES (33, 13, 19, 'Selected Option 2 based on course lecture material.', 2.00, 'Partially correct or minor inaccuracy.');
INSERT INTO `quiz_answers` (`id`, `attempt_id`, `question_id`, `answer_text`, `awarded_marks`, `feedback`) VALUES (34, 13, 20, 'Selected Option 2 based on course lecture material.', 2.00, 'Partially correct or minor inaccuracy.');
INSERT INTO `quiz_answers` (`id`, `attempt_id`, `question_id`, `answer_text`, `awarded_marks`, `feedback`) VALUES (35, 13, 21, 'Selected Option 2 based on course lecture material.', 3.00, 'Partially correct or minor inaccuracy.');
INSERT INTO `quiz_answers` (`id`, `attempt_id`, `question_id`, `answer_text`, `awarded_marks`, `feedback`) VALUES (36, 14, 19, 'Selected Option 3 based on course lecture material.', 2.10, 'Partially correct or minor inaccuracy.');
INSERT INTO `quiz_answers` (`id`, `attempt_id`, `question_id`, `answer_text`, `awarded_marks`, `feedback`) VALUES (37, 14, 20, 'Selected Option 3 based on course lecture material.', 2.10, 'Partially correct or minor inaccuracy.');
INSERT INTO `quiz_answers` (`id`, `attempt_id`, `question_id`, `answer_text`, `awarded_marks`, `feedback`) VALUES (38, 14, 21, 'Selected Option 3 based on course lecture material.', 2.80, 'Partially correct or minor inaccuracy.');
INSERT INTO `quiz_answers` (`id`, `attempt_id`, `question_id`, `answer_text`, `awarded_marks`, `feedback`) VALUES (39, 15, 19, 'Selected Option 4 based on course lecture material.', 3.00, 'Correct answer.');
INSERT INTO `quiz_answers` (`id`, `attempt_id`, `question_id`, `answer_text`, `awarded_marks`, `feedback`) VALUES (40, 15, 20, 'Selected Option 4 based on course lecture material.', 3.00, 'Correct answer.');
INSERT INTO `quiz_answers` (`id`, `attempt_id`, `question_id`, `answer_text`, `awarded_marks`, `feedback`) VALUES (41, 15, 21, 'Selected Option 4 based on course lecture material.', 4.00, 'Correct answer.');
INSERT INTO `quiz_answers` (`id`, `attempt_id`, `question_id`, `answer_text`, `awarded_marks`, `feedback`) VALUES (42, 16, 22, 'Selected Option 1 based on course lecture material.', 3.00, 'Correct answer.');
INSERT INTO `quiz_answers` (`id`, `attempt_id`, `question_id`, `answer_text`, `awarded_marks`, `feedback`) VALUES (43, 16, 23, 'Selected Option 1 based on course lecture material.', 3.00, 'Correct answer.');
INSERT INTO `quiz_answers` (`id`, `attempt_id`, `question_id`, `answer_text`, `awarded_marks`, `feedback`) VALUES (44, 16, 24, 'Selected Option 1 based on course lecture material.', 4.00, 'Correct answer.');
INSERT INTO `quiz_answers` (`id`, `attempt_id`, `question_id`, `answer_text`, `awarded_marks`, `feedback`) VALUES (45, 17, 22, 'Selected Option 2 based on course lecture material.', 2.00, 'Partially correct or minor inaccuracy.');
INSERT INTO `quiz_answers` (`id`, `attempt_id`, `question_id`, `answer_text`, `awarded_marks`, `feedback`) VALUES (46, 17, 23, 'Selected Option 2 based on course lecture material.', 2.00, 'Partially correct or minor inaccuracy.');
INSERT INTO `quiz_answers` (`id`, `attempt_id`, `question_id`, `answer_text`, `awarded_marks`, `feedback`) VALUES (47, 17, 24, 'Selected Option 2 based on course lecture material.', 3.00, 'Partially correct or minor inaccuracy.');
INSERT INTO `quiz_answers` (`id`, `attempt_id`, `question_id`, `answer_text`, `awarded_marks`, `feedback`) VALUES (48, 18, 22, 'Selected Option 3 based on course lecture material.', 2.10, 'Partially correct or minor inaccuracy.');
INSERT INTO `quiz_answers` (`id`, `attempt_id`, `question_id`, `answer_text`, `awarded_marks`, `feedback`) VALUES (49, 18, 23, 'Selected Option 3 based on course lecture material.', 2.10, 'Partially correct or minor inaccuracy.');
INSERT INTO `quiz_answers` (`id`, `attempt_id`, `question_id`, `answer_text`, `awarded_marks`, `feedback`) VALUES (50, 18, 24, 'Selected Option 3 based on course lecture material.', 2.80, 'Partially correct or minor inaccuracy.');
INSERT INTO `quiz_answers` (`id`, `attempt_id`, `question_id`, `answer_text`, `awarded_marks`, `feedback`) VALUES (51, 19, 22, 'Selected Option 4 based on course lecture material.', 3.00, 'Correct answer.');
INSERT INTO `quiz_answers` (`id`, `attempt_id`, `question_id`, `answer_text`, `awarded_marks`, `feedback`) VALUES (52, 19, 23, 'Selected Option 4 based on course lecture material.', 3.00, 'Correct answer.');
INSERT INTO `quiz_answers` (`id`, `attempt_id`, `question_id`, `answer_text`, `awarded_marks`, `feedback`) VALUES (53, 19, 24, 'Selected Option 4 based on course lecture material.', 4.00, 'Correct answer.');
INSERT INTO `quiz_answers` (`id`, `attempt_id`, `question_id`, `answer_text`, `awarded_marks`, `feedback`) VALUES (54, 20, 25, 'Selected Option 1 based on course lecture material.', 3.00, 'Correct answer.');
INSERT INTO `quiz_answers` (`id`, `attempt_id`, `question_id`, `answer_text`, `awarded_marks`, `feedback`) VALUES (55, 20, 26, 'Selected Option 1 based on course lecture material.', 3.00, 'Correct answer.');
INSERT INTO `quiz_answers` (`id`, `attempt_id`, `question_id`, `answer_text`, `awarded_marks`, `feedback`) VALUES (56, 20, 27, 'Selected Option 1 based on course lecture material.', 4.00, 'Correct answer.');
INSERT INTO `quiz_answers` (`id`, `attempt_id`, `question_id`, `answer_text`, `awarded_marks`, `feedback`) VALUES (57, 21, 25, 'Selected Option 2 based on course lecture material.', 2.00, 'Partially correct or minor inaccuracy.');
INSERT INTO `quiz_answers` (`id`, `attempt_id`, `question_id`, `answer_text`, `awarded_marks`, `feedback`) VALUES (58, 21, 26, 'Selected Option 2 based on course lecture material.', 2.00, 'Partially correct or minor inaccuracy.');
INSERT INTO `quiz_answers` (`id`, `attempt_id`, `question_id`, `answer_text`, `awarded_marks`, `feedback`) VALUES (59, 21, 27, 'Selected Option 2 based on course lecture material.', 3.00, 'Partially correct or minor inaccuracy.');
INSERT INTO `quiz_answers` (`id`, `attempt_id`, `question_id`, `answer_text`, `awarded_marks`, `feedback`) VALUES (60, 22, 25, 'Selected Option 3 based on course lecture material.', 2.10, 'Partially correct or minor inaccuracy.');
INSERT INTO `quiz_answers` (`id`, `attempt_id`, `question_id`, `answer_text`, `awarded_marks`, `feedback`) VALUES (61, 22, 26, 'Selected Option 3 based on course lecture material.', 2.10, 'Partially correct or minor inaccuracy.');
INSERT INTO `quiz_answers` (`id`, `attempt_id`, `question_id`, `answer_text`, `awarded_marks`, `feedback`) VALUES (62, 22, 27, 'Selected Option 3 based on course lecture material.', 2.80, 'Partially correct or minor inaccuracy.');
INSERT INTO `quiz_answers` (`id`, `attempt_id`, `question_id`, `answer_text`, `awarded_marks`, `feedback`) VALUES (63, 23, 25, 'Selected Option 4 based on course lecture material.', 3.00, 'Correct answer.');
INSERT INTO `quiz_answers` (`id`, `attempt_id`, `question_id`, `answer_text`, `awarded_marks`, `feedback`) VALUES (64, 23, 26, 'Selected Option 4 based on course lecture material.', 3.00, 'Correct answer.');
INSERT INTO `quiz_answers` (`id`, `attempt_id`, `question_id`, `answer_text`, `awarded_marks`, `feedback`) VALUES (65, 23, 27, 'Selected Option 4 based on course lecture material.', 4.00, 'Correct answer.');
INSERT INTO `quiz_answers` (`id`, `attempt_id`, `question_id`, `answer_text`, `awarded_marks`, `feedback`) VALUES (66, 24, 28, 'Selected Option 1 based on course lecture material.', 3.00, 'Correct answer.');
INSERT INTO `quiz_answers` (`id`, `attempt_id`, `question_id`, `answer_text`, `awarded_marks`, `feedback`) VALUES (67, 24, 29, 'Selected Option 1 based on course lecture material.', 3.00, 'Correct answer.');
INSERT INTO `quiz_answers` (`id`, `attempt_id`, `question_id`, `answer_text`, `awarded_marks`, `feedback`) VALUES (68, 24, 30, 'Selected Option 1 based on course lecture material.', 4.00, 'Correct answer.');
INSERT INTO `quiz_answers` (`id`, `attempt_id`, `question_id`, `answer_text`, `awarded_marks`, `feedback`) VALUES (69, 25, 28, 'Selected Option 2 based on course lecture material.', 2.00, 'Partially correct or minor inaccuracy.');
INSERT INTO `quiz_answers` (`id`, `attempt_id`, `question_id`, `answer_text`, `awarded_marks`, `feedback`) VALUES (70, 25, 29, 'Selected Option 2 based on course lecture material.', 2.00, 'Partially correct or minor inaccuracy.');
INSERT INTO `quiz_answers` (`id`, `attempt_id`, `question_id`, `answer_text`, `awarded_marks`, `feedback`) VALUES (71, 25, 30, 'Selected Option 2 based on course lecture material.', 3.00, 'Partially correct or minor inaccuracy.');
INSERT INTO `quiz_answers` (`id`, `attempt_id`, `question_id`, `answer_text`, `awarded_marks`, `feedback`) VALUES (72, 26, 28, 'Selected Option 3 based on course lecture material.', 2.10, 'Partially correct or minor inaccuracy.');
INSERT INTO `quiz_answers` (`id`, `attempt_id`, `question_id`, `answer_text`, `awarded_marks`, `feedback`) VALUES (73, 26, 29, 'Selected Option 3 based on course lecture material.', 2.10, 'Partially correct or minor inaccuracy.');
INSERT INTO `quiz_answers` (`id`, `attempt_id`, `question_id`, `answer_text`, `awarded_marks`, `feedback`) VALUES (74, 26, 30, 'Selected Option 3 based on course lecture material.', 2.80, 'Partially correct or minor inaccuracy.');
INSERT INTO `quiz_answers` (`id`, `attempt_id`, `question_id`, `answer_text`, `awarded_marks`, `feedback`) VALUES (75, 27, 28, 'Selected Option 4 based on course lecture material.', 3.00, 'Correct answer.');
INSERT INTO `quiz_answers` (`id`, `attempt_id`, `question_id`, `answer_text`, `awarded_marks`, `feedback`) VALUES (76, 27, 29, 'Selected Option 4 based on course lecture material.', 3.00, 'Correct answer.');
INSERT INTO `quiz_answers` (`id`, `attempt_id`, `question_id`, `answer_text`, `awarded_marks`, `feedback`) VALUES (77, 27, 30, 'Selected Option 4 based on course lecture material.', 4.00, 'Correct answer.');
INSERT INTO `quiz_answers` (`id`, `attempt_id`, `question_id`, `answer_text`, `awarded_marks`, `feedback`) VALUES (78, 28, 31, 'Selected Option 1 based on course lecture material.', 3.00, 'Correct answer.');
INSERT INTO `quiz_answers` (`id`, `attempt_id`, `question_id`, `answer_text`, `awarded_marks`, `feedback`) VALUES (79, 28, 32, 'Selected Option 1 based on course lecture material.', 3.00, 'Correct answer.');
INSERT INTO `quiz_answers` (`id`, `attempt_id`, `question_id`, `answer_text`, `awarded_marks`, `feedback`) VALUES (80, 28, 33, 'Selected Option 1 based on course lecture material.', 4.00, 'Correct answer.');
INSERT INTO `quiz_answers` (`id`, `attempt_id`, `question_id`, `answer_text`, `awarded_marks`, `feedback`) VALUES (81, 29, 31, 'Selected Option 2 based on course lecture material.', 2.00, 'Partially correct or minor inaccuracy.');
INSERT INTO `quiz_answers` (`id`, `attempt_id`, `question_id`, `answer_text`, `awarded_marks`, `feedback`) VALUES (82, 29, 32, 'Selected Option 2 based on course lecture material.', 2.00, 'Partially correct or minor inaccuracy.');
INSERT INTO `quiz_answers` (`id`, `attempt_id`, `question_id`, `answer_text`, `awarded_marks`, `feedback`) VALUES (83, 29, 33, 'Selected Option 2 based on course lecture material.', 3.00, 'Partially correct or minor inaccuracy.');
INSERT INTO `quiz_answers` (`id`, `attempt_id`, `question_id`, `answer_text`, `awarded_marks`, `feedback`) VALUES (84, 30, 31, 'Selected Option 3 based on course lecture material.', 2.10, 'Partially correct or minor inaccuracy.');
INSERT INTO `quiz_answers` (`id`, `attempt_id`, `question_id`, `answer_text`, `awarded_marks`, `feedback`) VALUES (85, 30, 32, 'Selected Option 3 based on course lecture material.', 2.10, 'Partially correct or minor inaccuracy.');
INSERT INTO `quiz_answers` (`id`, `attempt_id`, `question_id`, `answer_text`, `awarded_marks`, `feedback`) VALUES (86, 30, 33, 'Selected Option 3 based on course lecture material.', 2.80, 'Partially correct or minor inaccuracy.');
INSERT INTO `quiz_answers` (`id`, `attempt_id`, `question_id`, `answer_text`, `awarded_marks`, `feedback`) VALUES (87, 31, 31, 'Selected Option 4 based on course lecture material.', 3.00, 'Correct answer.');
INSERT INTO `quiz_answers` (`id`, `attempt_id`, `question_id`, `answer_text`, `awarded_marks`, `feedback`) VALUES (88, 31, 32, 'Selected Option 4 based on course lecture material.', 3.00, 'Correct answer.');
INSERT INTO `quiz_answers` (`id`, `attempt_id`, `question_id`, `answer_text`, `awarded_marks`, `feedback`) VALUES (89, 31, 33, 'Selected Option 4 based on course lecture material.', 4.00, 'Correct answer.');
INSERT INTO `quiz_answers` (`id`, `attempt_id`, `question_id`, `answer_text`, `awarded_marks`, `feedback`) VALUES (90, 32, 34, 'Selected Option 1 based on course lecture material.', 3.00, 'Correct answer.');
INSERT INTO `quiz_answers` (`id`, `attempt_id`, `question_id`, `answer_text`, `awarded_marks`, `feedback`) VALUES (91, 32, 35, 'Selected Option 1 based on course lecture material.', 3.00, 'Correct answer.');
INSERT INTO `quiz_answers` (`id`, `attempt_id`, `question_id`, `answer_text`, `awarded_marks`, `feedback`) VALUES (92, 32, 36, 'Selected Option 1 based on course lecture material.', 4.00, 'Correct answer.');
INSERT INTO `quiz_answers` (`id`, `attempt_id`, `question_id`, `answer_text`, `awarded_marks`, `feedback`) VALUES (93, 33, 34, 'Selected Option 2 based on course lecture material.', 2.00, 'Partially correct or minor inaccuracy.');
INSERT INTO `quiz_answers` (`id`, `attempt_id`, `question_id`, `answer_text`, `awarded_marks`, `feedback`) VALUES (94, 33, 35, 'Selected Option 2 based on course lecture material.', 2.00, 'Partially correct or minor inaccuracy.');
INSERT INTO `quiz_answers` (`id`, `attempt_id`, `question_id`, `answer_text`, `awarded_marks`, `feedback`) VALUES (95, 33, 36, 'Selected Option 2 based on course lecture material.', 3.00, 'Partially correct or minor inaccuracy.');
INSERT INTO `quiz_answers` (`id`, `attempt_id`, `question_id`, `answer_text`, `awarded_marks`, `feedback`) VALUES (96, 34, 34, 'Selected Option 3 based on course lecture material.', 2.10, 'Partially correct or minor inaccuracy.');
INSERT INTO `quiz_answers` (`id`, `attempt_id`, `question_id`, `answer_text`, `awarded_marks`, `feedback`) VALUES (97, 34, 35, 'Selected Option 3 based on course lecture material.', 2.10, 'Partially correct or minor inaccuracy.');
INSERT INTO `quiz_answers` (`id`, `attempt_id`, `question_id`, `answer_text`, `awarded_marks`, `feedback`) VALUES (98, 34, 36, 'Selected Option 3 based on course lecture material.', 2.80, 'Partially correct or minor inaccuracy.');
INSERT INTO `quiz_answers` (`id`, `attempt_id`, `question_id`, `answer_text`, `awarded_marks`, `feedback`) VALUES (99, 35, 34, 'Selected Option 4 based on course lecture material.', 3.00, 'Correct answer.');
INSERT INTO `quiz_answers` (`id`, `attempt_id`, `question_id`, `answer_text`, `awarded_marks`, `feedback`) VALUES (100, 35, 35, 'Selected Option 4 based on course lecture material.', 3.00, 'Correct answer.');
INSERT INTO `quiz_answers` (`id`, `attempt_id`, `question_id`, `answer_text`, `awarded_marks`, `feedback`) VALUES (101, 35, 36, 'Selected Option 4 based on course lecture material.', 4.00, 'Correct answer.');
INSERT INTO `quiz_answers` (`id`, `attempt_id`, `question_id`, `answer_text`, `awarded_marks`, `feedback`) VALUES (102, 36, 37, 'Selected Option 1 based on course lecture material.', 3.00, 'Correct answer.');
INSERT INTO `quiz_answers` (`id`, `attempt_id`, `question_id`, `answer_text`, `awarded_marks`, `feedback`) VALUES (103, 36, 38, 'Selected Option 1 based on course lecture material.', 3.00, 'Correct answer.');
INSERT INTO `quiz_answers` (`id`, `attempt_id`, `question_id`, `answer_text`, `awarded_marks`, `feedback`) VALUES (104, 36, 39, 'Selected Option 1 based on course lecture material.', 4.00, 'Correct answer.');
INSERT INTO `quiz_answers` (`id`, `attempt_id`, `question_id`, `answer_text`, `awarded_marks`, `feedback`) VALUES (105, 37, 37, 'Selected Option 2 based on course lecture material.', 2.00, 'Partially correct or minor inaccuracy.');
INSERT INTO `quiz_answers` (`id`, `attempt_id`, `question_id`, `answer_text`, `awarded_marks`, `feedback`) VALUES (106, 37, 38, 'Selected Option 2 based on course lecture material.', 2.00, 'Partially correct or minor inaccuracy.');
INSERT INTO `quiz_answers` (`id`, `attempt_id`, `question_id`, `answer_text`, `awarded_marks`, `feedback`) VALUES (107, 37, 39, 'Selected Option 2 based on course lecture material.', 3.00, 'Partially correct or minor inaccuracy.');
INSERT INTO `quiz_answers` (`id`, `attempt_id`, `question_id`, `answer_text`, `awarded_marks`, `feedback`) VALUES (108, 38, 37, 'Selected Option 3 based on course lecture material.', 2.10, 'Partially correct or minor inaccuracy.');
INSERT INTO `quiz_answers` (`id`, `attempt_id`, `question_id`, `answer_text`, `awarded_marks`, `feedback`) VALUES (109, 38, 38, 'Selected Option 3 based on course lecture material.', 2.10, 'Partially correct or minor inaccuracy.');
INSERT INTO `quiz_answers` (`id`, `attempt_id`, `question_id`, `answer_text`, `awarded_marks`, `feedback`) VALUES (110, 38, 39, 'Selected Option 3 based on course lecture material.', 2.80, 'Partially correct or minor inaccuracy.');
INSERT INTO `quiz_answers` (`id`, `attempt_id`, `question_id`, `answer_text`, `awarded_marks`, `feedback`) VALUES (111, 39, 37, 'Selected Option 4 based on course lecture material.', 3.00, 'Correct answer.');
INSERT INTO `quiz_answers` (`id`, `attempt_id`, `question_id`, `answer_text`, `awarded_marks`, `feedback`) VALUES (112, 39, 38, 'Selected Option 4 based on course lecture material.', 3.00, 'Correct answer.');
INSERT INTO `quiz_answers` (`id`, `attempt_id`, `question_id`, `answer_text`, `awarded_marks`, `feedback`) VALUES (113, 39, 39, 'Selected Option 4 based on course lecture material.', 4.00, 'Correct answer.');
INSERT INTO `quiz_answers` (`id`, `attempt_id`, `question_id`, `answer_text`, `awarded_marks`, `feedback`) VALUES (114, 40, 40, 'Selected Option 1 based on course lecture material.', 3.00, 'Correct answer.');
INSERT INTO `quiz_answers` (`id`, `attempt_id`, `question_id`, `answer_text`, `awarded_marks`, `feedback`) VALUES (115, 40, 41, 'Selected Option 1 based on course lecture material.', 3.00, 'Correct answer.');
INSERT INTO `quiz_answers` (`id`, `attempt_id`, `question_id`, `answer_text`, `awarded_marks`, `feedback`) VALUES (116, 40, 42, 'Selected Option 1 based on course lecture material.', 4.00, 'Correct answer.');
INSERT INTO `quiz_answers` (`id`, `attempt_id`, `question_id`, `answer_text`, `awarded_marks`, `feedback`) VALUES (117, 41, 40, 'Selected Option 2 based on course lecture material.', 2.00, 'Partially correct or minor inaccuracy.');
INSERT INTO `quiz_answers` (`id`, `attempt_id`, `question_id`, `answer_text`, `awarded_marks`, `feedback`) VALUES (118, 41, 41, 'Selected Option 2 based on course lecture material.', 2.00, 'Partially correct or minor inaccuracy.');
INSERT INTO `quiz_answers` (`id`, `attempt_id`, `question_id`, `answer_text`, `awarded_marks`, `feedback`) VALUES (119, 41, 42, 'Selected Option 2 based on course lecture material.', 3.00, 'Partially correct or minor inaccuracy.');
INSERT INTO `quiz_answers` (`id`, `attempt_id`, `question_id`, `answer_text`, `awarded_marks`, `feedback`) VALUES (120, 42, 40, 'Selected Option 3 based on course lecture material.', 2.10, 'Partially correct or minor inaccuracy.');
INSERT INTO `quiz_answers` (`id`, `attempt_id`, `question_id`, `answer_text`, `awarded_marks`, `feedback`) VALUES (121, 42, 41, 'Selected Option 3 based on course lecture material.', 2.10, 'Partially correct or minor inaccuracy.');
INSERT INTO `quiz_answers` (`id`, `attempt_id`, `question_id`, `answer_text`, `awarded_marks`, `feedback`) VALUES (122, 42, 42, 'Selected Option 3 based on course lecture material.', 2.80, 'Partially correct or minor inaccuracy.');
INSERT INTO `quiz_answers` (`id`, `attempt_id`, `question_id`, `answer_text`, `awarded_marks`, `feedback`) VALUES (123, 43, 40, 'Selected Option 4 based on course lecture material.', 3.00, 'Correct answer.');
INSERT INTO `quiz_answers` (`id`, `attempt_id`, `question_id`, `answer_text`, `awarded_marks`, `feedback`) VALUES (124, 43, 41, 'Selected Option 4 based on course lecture material.', 3.00, 'Correct answer.');
INSERT INTO `quiz_answers` (`id`, `attempt_id`, `question_id`, `answer_text`, `awarded_marks`, `feedback`) VALUES (125, 43, 42, 'Selected Option 4 based on course lecture material.', 4.00, 'Correct answer.');
INSERT INTO `quiz_answers` (`id`, `attempt_id`, `question_id`, `answer_text`, `awarded_marks`, `feedback`) VALUES (126, 44, 43, 'Selected Option 1 based on course lecture material.', 3.00, 'Correct answer.');
INSERT INTO `quiz_answers` (`id`, `attempt_id`, `question_id`, `answer_text`, `awarded_marks`, `feedback`) VALUES (127, 44, 44, 'Selected Option 1 based on course lecture material.', 3.00, 'Correct answer.');
INSERT INTO `quiz_answers` (`id`, `attempt_id`, `question_id`, `answer_text`, `awarded_marks`, `feedback`) VALUES (128, 44, 45, 'Selected Option 1 based on course lecture material.', 4.00, 'Correct answer.');
INSERT INTO `quiz_answers` (`id`, `attempt_id`, `question_id`, `answer_text`, `awarded_marks`, `feedback`) VALUES (129, 45, 43, 'Selected Option 2 based on course lecture material.', 2.00, 'Partially correct or minor inaccuracy.');
INSERT INTO `quiz_answers` (`id`, `attempt_id`, `question_id`, `answer_text`, `awarded_marks`, `feedback`) VALUES (130, 45, 44, 'Selected Option 2 based on course lecture material.', 2.00, 'Partially correct or minor inaccuracy.');
INSERT INTO `quiz_answers` (`id`, `attempt_id`, `question_id`, `answer_text`, `awarded_marks`, `feedback`) VALUES (131, 45, 45, 'Selected Option 2 based on course lecture material.', 3.00, 'Partially correct or minor inaccuracy.');
INSERT INTO `quiz_answers` (`id`, `attempt_id`, `question_id`, `answer_text`, `awarded_marks`, `feedback`) VALUES (132, 46, 43, 'Selected Option 3 based on course lecture material.', 2.10, 'Partially correct or minor inaccuracy.');
INSERT INTO `quiz_answers` (`id`, `attempt_id`, `question_id`, `answer_text`, `awarded_marks`, `feedback`) VALUES (133, 46, 44, 'Selected Option 3 based on course lecture material.', 2.10, 'Partially correct or minor inaccuracy.');
INSERT INTO `quiz_answers` (`id`, `attempt_id`, `question_id`, `answer_text`, `awarded_marks`, `feedback`) VALUES (134, 46, 45, 'Selected Option 3 based on course lecture material.', 2.80, 'Partially correct or minor inaccuracy.');
INSERT INTO `quiz_answers` (`id`, `attempt_id`, `question_id`, `answer_text`, `awarded_marks`, `feedback`) VALUES (135, 47, 43, 'Selected Option 4 based on course lecture material.', 3.00, 'Correct answer.');
INSERT INTO `quiz_answers` (`id`, `attempt_id`, `question_id`, `answer_text`, `awarded_marks`, `feedback`) VALUES (136, 47, 44, 'Selected Option 4 based on course lecture material.', 3.00, 'Correct answer.');
INSERT INTO `quiz_answers` (`id`, `attempt_id`, `question_id`, `answer_text`, `awarded_marks`, `feedback`) VALUES (137, 47, 45, 'Selected Option 4 based on course lecture material.', 4.00, 'Correct answer.');
INSERT INTO `quiz_answers` (`id`, `attempt_id`, `question_id`, `answer_text`, `awarded_marks`, `feedback`) VALUES (138, 48, 46, 'Selected Option 1 based on course lecture material.', 3.00, 'Correct answer.');
INSERT INTO `quiz_answers` (`id`, `attempt_id`, `question_id`, `answer_text`, `awarded_marks`, `feedback`) VALUES (139, 48, 47, 'Selected Option 1 based on course lecture material.', 3.00, 'Correct answer.');
INSERT INTO `quiz_answers` (`id`, `attempt_id`, `question_id`, `answer_text`, `awarded_marks`, `feedback`) VALUES (140, 48, 48, 'Selected Option 1 based on course lecture material.', 4.00, 'Correct answer.');
INSERT INTO `quiz_answers` (`id`, `attempt_id`, `question_id`, `answer_text`, `awarded_marks`, `feedback`) VALUES (141, 49, 46, 'Selected Option 2 based on course lecture material.', 2.00, 'Partially correct or minor inaccuracy.');
INSERT INTO `quiz_answers` (`id`, `attempt_id`, `question_id`, `answer_text`, `awarded_marks`, `feedback`) VALUES (142, 49, 47, 'Selected Option 2 based on course lecture material.', 2.00, 'Partially correct or minor inaccuracy.');
INSERT INTO `quiz_answers` (`id`, `attempt_id`, `question_id`, `answer_text`, `awarded_marks`, `feedback`) VALUES (143, 49, 48, 'Selected Option 2 based on course lecture material.', 3.00, 'Partially correct or minor inaccuracy.');
INSERT INTO `quiz_answers` (`id`, `attempt_id`, `question_id`, `answer_text`, `awarded_marks`, `feedback`) VALUES (144, 50, 46, 'Selected Option 3 based on course lecture material.', 2.10, 'Partially correct or minor inaccuracy.');
INSERT INTO `quiz_answers` (`id`, `attempt_id`, `question_id`, `answer_text`, `awarded_marks`, `feedback`) VALUES (145, 50, 47, 'Selected Option 3 based on course lecture material.', 2.10, 'Partially correct or minor inaccuracy.');
INSERT INTO `quiz_answers` (`id`, `attempt_id`, `question_id`, `answer_text`, `awarded_marks`, `feedback`) VALUES (146, 50, 48, 'Selected Option 3 based on course lecture material.', 2.80, 'Partially correct or minor inaccuracy.');
INSERT INTO `quiz_answers` (`id`, `attempt_id`, `question_id`, `answer_text`, `awarded_marks`, `feedback`) VALUES (147, 51, 46, 'Selected Option 4 based on course lecture material.', 3.00, 'Correct answer.');
INSERT INTO `quiz_answers` (`id`, `attempt_id`, `question_id`, `answer_text`, `awarded_marks`, `feedback`) VALUES (148, 51, 47, 'Selected Option 4 based on course lecture material.', 3.00, 'Correct answer.');
INSERT INTO `quiz_answers` (`id`, `attempt_id`, `question_id`, `answer_text`, `awarded_marks`, `feedback`) VALUES (149, 51, 48, 'Selected Option 4 based on course lecture material.', 4.00, 'Correct answer.');
-- (132 records)

-- --------------------------------------------------------
-- Data for table `assignments`
-- --------------------------------------------------------
INSERT INTO `assignments` (`id`, `course_id`, `lecturer_id`, `evaluation_id`, `title`, `instructions`, `max_marks`, `due_date`, `status`, `marks_released`, `file_path`, `created_at`) VALUES (10, 1, 3, NULL, 'Assignment 01 - ER Diagram Design', 'Design a complete Entity-Relationship diagram for a Hospital Management System. Include entities, attributes, and cardinalities. Submit as PDF.', 25.00, '2026-11-30', 'PUBLISHED', 0, NULL, '2026-11-03 15:41:09');
INSERT INTO `assignments` (`id`, `course_id`, `lecturer_id`, `evaluation_id`, `title`, `instructions`, `max_marks`, `due_date`, `status`, `marks_released`, `file_path`, `created_at`) VALUES (11, 2, 2, NULL, 'Assignment 01 - Agile Sprint Report', 'Conduct a mock sprint planning session for a library system project. Document user stories, sprint backlog, and retrospective notes. Submit PDF report.', 30.00, '2026-12-05', 'PUBLISHED', 0, NULL, '2026-11-03 15:41:09');
INSERT INTO `assignments` (`id`, `course_id`, `lecturer_id`, `evaluation_id`, `title`, `instructions`, `max_marks`, `due_date`, `status`, `marks_released`, `file_path`, `created_at`) VALUES (12, 4, 36, NULL, 'Assignment 01 - Network Topology Design', 'Design a complete network topology for a small-medium enterprise. Include IP addressing, subnetting scheme, router and switch configurations.', 25.00, '2026-11-28', 'PUBLISHED', 0, NULL, '2026-11-03 15:41:09');
INSERT INTO `assignments` (`id`, `course_id`, `lecturer_id`, `evaluation_id`, `title`, `instructions`, `max_marks`, `due_date`, `status`, `marks_released`, `file_path`, `created_at`) VALUES (13, 16, 37, NULL, 'Assignment 01 - Dynamic Webpage Design', 'Build a fully functional multi-page responsive website with navigation, forms, and JavaScript validation. Host on localhost and submit screenshots and source.', 30.00, '2026-12-10', 'PUBLISHED', 0, NULL, '2026-11-03 15:41:09');
INSERT INTO `assignments` (`id`, `course_id`, `lecturer_id`, `evaluation_id`, `title`, `instructions`, `max_marks`, `due_date`, `status`, `marks_released`, `file_path`, `created_at`) VALUES (14, 17, 4, NULL, 'Assignment 01 - Crop Management Report', 'Visit a paddy cultivation field in your area. Document soil preparation, irrigation, pest management, and harvesting techniques. Submit with photographs.', 25.00, '2026-11-25', 'PUBLISHED', 0, NULL, '2026-11-03 15:41:09');
INSERT INTO `assignments` (`id`, `course_id`, `lecturer_id`, `evaluation_id`, `title`, `instructions`, `max_marks`, `due_date`, `status`, `marks_released`, `file_path`, `created_at`) VALUES (15, 19, 39, NULL, 'Assignment 01 - Fermentation Lab Report', 'Conduct a yeast fermentation experiment. Record observations, calculate ethanol yield, and relate findings to industrial biotechnology applications.', 25.00, '2026-11-22', 'PUBLISHED', 0, NULL, '2026-11-03 15:41:09');
INSERT INTO `assignments` (`id`, `course_id`, `lecturer_id`, `evaluation_id`, `title`, `instructions`, `max_marks`, `due_date`, `status`, `marks_released`, `file_path`, `created_at`) VALUES (16, 21, 42, NULL, 'Assignment 01 - Bridge Load Analysis', 'Calculate the static reactions at supports for a simply-supported beam under distributed and concentrated loads. Use free body diagrams and submit PDF.', 30.00, '2026-12-01', 'PUBLISHED', 0, NULL, '2026-11-03 15:41:09');
INSERT INTO `assignments` (`id`, `course_id`, `lecturer_id`, `evaluation_id`, `title`, `instructions`, `max_marks`, `due_date`, `status`, `marks_released`, `file_path`, `created_at`) VALUES (17, 22, 43, NULL, 'Assignment 01 - PLC Automation Project', 'Program a PLC to automate a bottling line. Document ladder logic, sequence diagram, and testing results. Submit project report.', 30.00, '2026-12-08', 'PUBLISHED', 0, NULL, '2026-11-03 15:41:09');
INSERT INTO `assignments` (`id`, `course_id`, `lecturer_id`, `evaluation_id`, `title`, `instructions`, `max_marks`, `due_date`, `status`, `marks_released`, `file_path`, `created_at`) VALUES (18, 25, 46, NULL, 'Assignment 01 - Applied Mathematics Problems', 'Solve 20 problems covering integration, differentiation, linear algebra and matrices. Show all working steps. Submit as handwritten PDF scan.', 25.00, '2026-11-20', 'PUBLISHED', 0, NULL, '2026-11-03 15:41:09');
INSERT INTO `assignments` (`id`, `course_id`, `lecturer_id`, `evaluation_id`, `title`, `instructions`, `max_marks`, `due_date`, `status`, `marks_released`, `file_path`, `created_at`) VALUES (19, 26, 47, NULL, 'Assignment 01 - Business Plan', 'Prepare a complete business plan for a startup of your choice. Include market analysis, financial projections, and marketing strategies.', 30.00, '2026-12-03', 'PUBLISHED', 0, NULL, '2026-11-03 15:41:09');
-- (10 records)

-- --------------------------------------------------------
-- Data for table `assignment_submissions`
-- --------------------------------------------------------
INSERT INTO `assignment_submissions` (`id`, `assignment_id`, `student_id`, `file_path`, `submitted_at`, `status`, `marks`, `feedback`, `graded_by`, `graded_at`) VALUES (6, 10, 11, 'submissions/assignment_10_student_11.pdf', '2026-10-15T14:30', 'GRADED', 17.50, 'Well structured report with clear diagrams and references. Excellent work.', 3, '2026-10-20T10:15');
INSERT INTO `assignment_submissions` (`id`, `assignment_id`, `student_id`, `file_path`, `submitted_at`, `status`, `marks`, `feedback`, `graded_by`, `graded_at`) VALUES (7, 10, 12, 'submissions/assignment_10_student_12.pdf', '2026-10-17T14:30', 'GRADED', 19.30, 'Good analysis and accurate calculations. Minor formatting improvements needed.', 3, '2026-10-22T10:15');
INSERT INTO `assignment_submissions` (`id`, `assignment_id`, `student_id`, `file_path`, `submitted_at`, `status`, `marks`, `feedback`, `graded_by`, `graded_at`) VALUES (8, 10, 13, 'submissions/assignment_10_student_13.pdf', '2026-10-19T14:30', 'GRADED', 21.00, 'Thorough methodology and solid explanations. Well done.', 3, '2026-10-24T10:15');
INSERT INTO `assignment_submissions` (`id`, `assignment_id`, `student_id`, `file_path`, `submitted_at`, `status`, `marks`, `feedback`, `graded_by`, `graded_at`) VALUES (9, 10, 14, 'submissions/assignment_10_student_14.pdf', '2026-10-21T14:30', 'GRADED', 22.70, 'Satisfactory submission. Some theoretical explanations could be deeper.', 3, '2026-10-26T10:15');
INSERT INTO `assignment_submissions` (`id`, `assignment_id`, `student_id`, `file_path`, `submitted_at`, `status`, `marks`, `feedback`, `graded_by`, `graded_at`) VALUES (10, 10, 15, 'submissions/assignment_10_student_15.pdf', '2026-10-23T14:30', 'GRADED', 18.30, 'Comprehensive solution. Demonstrates deep understanding of the course concepts.', 3, '2026-10-28T10:15');
INSERT INTO `assignment_submissions` (`id`, `assignment_id`, `student_id`, `file_path`, `submitted_at`, `status`, `marks`, `feedback`, `graded_by`, `graded_at`) VALUES (11, 11, 11, 'submissions/assignment_11_student_11.pdf', '2026-10-15T14:30', 'GRADED', 21.00, 'Well structured report with clear diagrams and references. Excellent work.', 2, '2026-10-20T10:15');
INSERT INTO `assignment_submissions` (`id`, `assignment_id`, `student_id`, `file_path`, `submitted_at`, `status`, `marks`, `feedback`, `graded_by`, `graded_at`) VALUES (12, 11, 12, 'submissions/assignment_11_student_12.pdf', '2026-10-17T14:30', 'GRADED', 23.10, 'Good analysis and accurate calculations. Minor formatting improvements needed.', 2, '2026-10-22T10:15');
INSERT INTO `assignment_submissions` (`id`, `assignment_id`, `student_id`, `file_path`, `submitted_at`, `status`, `marks`, `feedback`, `graded_by`, `graded_at`) VALUES (13, 11, 13, 'submissions/assignment_11_student_13.pdf', '2026-10-19T14:30', 'GRADED', 25.20, 'Thorough methodology and solid explanations. Well done.', 2, '2026-10-24T10:15');
INSERT INTO `assignment_submissions` (`id`, `assignment_id`, `student_id`, `file_path`, `submitted_at`, `status`, `marks`, `feedback`, `graded_by`, `graded_at`) VALUES (14, 11, 14, 'submissions/assignment_11_student_14.pdf', '2026-10-21T14:30', 'GRADED', 27.30, 'Satisfactory submission. Some theoretical explanations could be deeper.', 2, '2026-10-26T10:15');
INSERT INTO `assignment_submissions` (`id`, `assignment_id`, `student_id`, `file_path`, `submitted_at`, `status`, `marks`, `feedback`, `graded_by`, `graded_at`) VALUES (15, 11, 15, 'submissions/assignment_11_student_15.pdf', '2026-10-23T14:30', 'GRADED', 21.90, 'Comprehensive solution. Demonstrates deep understanding of the course concepts.', 2, '2026-10-28T10:15');
INSERT INTO `assignment_submissions` (`id`, `assignment_id`, `student_id`, `file_path`, `submitted_at`, `status`, `marks`, `feedback`, `graded_by`, `graded_at`) VALUES (16, 12, 11, 'submissions/assignment_12_student_11.pdf', '2026-10-15T14:30', 'GRADED', 17.50, 'Well structured report with clear diagrams and references. Excellent work.', 36, '2026-10-20T10:15');
INSERT INTO `assignment_submissions` (`id`, `assignment_id`, `student_id`, `file_path`, `submitted_at`, `status`, `marks`, `feedback`, `graded_by`, `graded_at`) VALUES (17, 12, 12, 'submissions/assignment_12_student_12.pdf', '2026-10-17T14:30', 'GRADED', 19.30, 'Good analysis and accurate calculations. Minor formatting improvements needed.', 36, '2026-10-22T10:15');
INSERT INTO `assignment_submissions` (`id`, `assignment_id`, `student_id`, `file_path`, `submitted_at`, `status`, `marks`, `feedback`, `graded_by`, `graded_at`) VALUES (18, 12, 13, 'submissions/assignment_12_student_13.pdf', '2026-10-19T14:30', 'GRADED', 21.00, 'Thorough methodology and solid explanations. Well done.', 36, '2026-10-24T10:15');
INSERT INTO `assignment_submissions` (`id`, `assignment_id`, `student_id`, `file_path`, `submitted_at`, `status`, `marks`, `feedback`, `graded_by`, `graded_at`) VALUES (19, 12, 14, 'submissions/assignment_12_student_14.pdf', '2026-10-21T14:30', 'GRADED', 22.70, 'Satisfactory submission. Some theoretical explanations could be deeper.', 36, '2026-10-26T10:15');
INSERT INTO `assignment_submissions` (`id`, `assignment_id`, `student_id`, `file_path`, `submitted_at`, `status`, `marks`, `feedback`, `graded_by`, `graded_at`) VALUES (20, 12, 15, 'submissions/assignment_12_student_15.pdf', '2026-10-23T14:30', 'GRADED', 18.30, 'Comprehensive solution. Demonstrates deep understanding of the course concepts.', 36, '2026-10-28T10:15');
INSERT INTO `assignment_submissions` (`id`, `assignment_id`, `student_id`, `file_path`, `submitted_at`, `status`, `marks`, `feedback`, `graded_by`, `graded_at`) VALUES (21, 13, 11, 'submissions/assignment_13_student_11.pdf', '2026-10-15T14:30', 'GRADED', 21.00, 'Well structured report with clear diagrams and references. Excellent work.', 37, '2026-10-20T10:15');
INSERT INTO `assignment_submissions` (`id`, `assignment_id`, `student_id`, `file_path`, `submitted_at`, `status`, `marks`, `feedback`, `graded_by`, `graded_at`) VALUES (22, 13, 12, 'submissions/assignment_13_student_12.pdf', '2026-10-17T14:30', 'GRADED', 23.10, 'Good analysis and accurate calculations. Minor formatting improvements needed.', 37, '2026-10-22T10:15');
INSERT INTO `assignment_submissions` (`id`, `assignment_id`, `student_id`, `file_path`, `submitted_at`, `status`, `marks`, `feedback`, `graded_by`, `graded_at`) VALUES (23, 13, 13, 'submissions/assignment_13_student_13.pdf', '2026-10-19T14:30', 'GRADED', 25.20, 'Thorough methodology and solid explanations. Well done.', 37, '2026-10-24T10:15');
INSERT INTO `assignment_submissions` (`id`, `assignment_id`, `student_id`, `file_path`, `submitted_at`, `status`, `marks`, `feedback`, `graded_by`, `graded_at`) VALUES (24, 13, 14, 'submissions/assignment_13_student_14.pdf', '2026-10-21T14:30', 'GRADED', 27.30, 'Satisfactory submission. Some theoretical explanations could be deeper.', 37, '2026-10-26T10:15');
INSERT INTO `assignment_submissions` (`id`, `assignment_id`, `student_id`, `file_path`, `submitted_at`, `status`, `marks`, `feedback`, `graded_by`, `graded_at`) VALUES (25, 13, 15, 'submissions/assignment_13_student_15.pdf', '2026-10-23T14:30', 'GRADED', 21.90, 'Comprehensive solution. Demonstrates deep understanding of the course concepts.', 37, '2026-10-28T10:15');
INSERT INTO `assignment_submissions` (`id`, `assignment_id`, `student_id`, `file_path`, `submitted_at`, `status`, `marks`, `feedback`, `graded_by`, `graded_at`) VALUES (26, 14, 50, 'submissions/assignment_14_student_50.pdf', '2026-10-15T14:30', 'GRADED', 17.50, 'Well structured report with clear diagrams and references. Excellent work.', 4, '2026-10-20T10:15');
INSERT INTO `assignment_submissions` (`id`, `assignment_id`, `student_id`, `file_path`, `submitted_at`, `status`, `marks`, `feedback`, `graded_by`, `graded_at`) VALUES (27, 14, 51, 'submissions/assignment_14_student_51.pdf', '2026-10-17T14:30', 'GRADED', 19.30, 'Good analysis and accurate calculations. Minor formatting improvements needed.', 4, '2026-10-22T10:15');
INSERT INTO `assignment_submissions` (`id`, `assignment_id`, `student_id`, `file_path`, `submitted_at`, `status`, `marks`, `feedback`, `graded_by`, `graded_at`) VALUES (28, 14, 52, 'submissions/assignment_14_student_52.pdf', '2026-10-19T14:30', 'GRADED', 21.00, 'Thorough methodology and solid explanations. Well done.', 4, '2026-10-24T10:15');
INSERT INTO `assignment_submissions` (`id`, `assignment_id`, `student_id`, `file_path`, `submitted_at`, `status`, `marks`, `feedback`, `graded_by`, `graded_at`) VALUES (29, 14, 53, 'submissions/assignment_14_student_53.pdf', '2026-10-21T14:30', 'GRADED', 22.70, 'Satisfactory submission. Some theoretical explanations could be deeper.', 4, '2026-10-26T10:15');
INSERT INTO `assignment_submissions` (`id`, `assignment_id`, `student_id`, `file_path`, `submitted_at`, `status`, `marks`, `feedback`, `graded_by`, `graded_at`) VALUES (30, 14, 54, 'submissions/assignment_14_student_54.pdf', '2026-10-23T14:30', 'GRADED', 18.30, 'Comprehensive solution. Demonstrates deep understanding of the course concepts.', 4, '2026-10-28T10:15');
INSERT INTO `assignment_submissions` (`id`, `assignment_id`, `student_id`, `file_path`, `submitted_at`, `status`, `marks`, `feedback`, `graded_by`, `graded_at`) VALUES (31, 15, 50, 'submissions/assignment_15_student_50.pdf', '2026-10-15T14:30', 'GRADED', 17.50, 'Well structured report with clear diagrams and references. Excellent work.', 39, '2026-10-20T10:15');
INSERT INTO `assignment_submissions` (`id`, `assignment_id`, `student_id`, `file_path`, `submitted_at`, `status`, `marks`, `feedback`, `graded_by`, `graded_at`) VALUES (32, 15, 51, 'submissions/assignment_15_student_51.pdf', '2026-10-17T14:30', 'GRADED', 19.30, 'Good analysis and accurate calculations. Minor formatting improvements needed.', 39, '2026-10-22T10:15');
INSERT INTO `assignment_submissions` (`id`, `assignment_id`, `student_id`, `file_path`, `submitted_at`, `status`, `marks`, `feedback`, `graded_by`, `graded_at`) VALUES (33, 15, 52, 'submissions/assignment_15_student_52.pdf', '2026-10-19T14:30', 'GRADED', 21.00, 'Thorough methodology and solid explanations. Well done.', 39, '2026-10-24T10:15');
INSERT INTO `assignment_submissions` (`id`, `assignment_id`, `student_id`, `file_path`, `submitted_at`, `status`, `marks`, `feedback`, `graded_by`, `graded_at`) VALUES (34, 15, 53, 'submissions/assignment_15_student_53.pdf', '2026-10-21T14:30', 'GRADED', 22.70, 'Satisfactory submission. Some theoretical explanations could be deeper.', 39, '2026-10-26T10:15');
INSERT INTO `assignment_submissions` (`id`, `assignment_id`, `student_id`, `file_path`, `submitted_at`, `status`, `marks`, `feedback`, `graded_by`, `graded_at`) VALUES (35, 15, 54, 'submissions/assignment_15_student_54.pdf', '2026-10-23T14:30', 'GRADED', 18.30, 'Comprehensive solution. Demonstrates deep understanding of the course concepts.', 39, '2026-10-28T10:15');
INSERT INTO `assignment_submissions` (`id`, `assignment_id`, `student_id`, `file_path`, `submitted_at`, `status`, `marks`, `feedback`, `graded_by`, `graded_at`) VALUES (36, 16, 55, 'submissions/assignment_16_student_55.pdf', '2026-10-15T14:30', 'GRADED', 21.00, 'Well structured report with clear diagrams and references. Excellent work.', 42, '2026-10-20T10:15');
INSERT INTO `assignment_submissions` (`id`, `assignment_id`, `student_id`, `file_path`, `submitted_at`, `status`, `marks`, `feedback`, `graded_by`, `graded_at`) VALUES (37, 16, 56, 'submissions/assignment_16_student_56.pdf', '2026-10-17T14:30', 'GRADED', 23.10, 'Good analysis and accurate calculations. Minor formatting improvements needed.', 42, '2026-10-22T10:15');
INSERT INTO `assignment_submissions` (`id`, `assignment_id`, `student_id`, `file_path`, `submitted_at`, `status`, `marks`, `feedback`, `graded_by`, `graded_at`) VALUES (38, 16, 57, 'submissions/assignment_16_student_57.pdf', '2026-10-19T14:30', 'GRADED', 25.20, 'Thorough methodology and solid explanations. Well done.', 42, '2026-10-24T10:15');
INSERT INTO `assignment_submissions` (`id`, `assignment_id`, `student_id`, `file_path`, `submitted_at`, `status`, `marks`, `feedback`, `graded_by`, `graded_at`) VALUES (39, 16, 58, 'submissions/assignment_16_student_58.pdf', '2026-10-21T14:30', 'GRADED', 27.30, 'Satisfactory submission. Some theoretical explanations could be deeper.', 42, '2026-10-26T10:15');
INSERT INTO `assignment_submissions` (`id`, `assignment_id`, `student_id`, `file_path`, `submitted_at`, `status`, `marks`, `feedback`, `graded_by`, `graded_at`) VALUES (40, 16, 59, 'submissions/assignment_16_student_59.pdf', '2026-10-23T14:30', 'GRADED', 21.90, 'Comprehensive solution. Demonstrates deep understanding of the course concepts.', 42, '2026-10-28T10:15');
INSERT INTO `assignment_submissions` (`id`, `assignment_id`, `student_id`, `file_path`, `submitted_at`, `status`, `marks`, `feedback`, `graded_by`, `graded_at`) VALUES (41, 17, 55, 'submissions/assignment_17_student_55.pdf', '2026-10-15T14:30', 'GRADED', 21.00, 'Well structured report with clear diagrams and references. Excellent work.', 43, '2026-10-20T10:15');
INSERT INTO `assignment_submissions` (`id`, `assignment_id`, `student_id`, `file_path`, `submitted_at`, `status`, `marks`, `feedback`, `graded_by`, `graded_at`) VALUES (42, 17, 56, 'submissions/assignment_17_student_56.pdf', '2026-10-17T14:30', 'GRADED', 23.10, 'Good analysis and accurate calculations. Minor formatting improvements needed.', 43, '2026-10-22T10:15');
INSERT INTO `assignment_submissions` (`id`, `assignment_id`, `student_id`, `file_path`, `submitted_at`, `status`, `marks`, `feedback`, `graded_by`, `graded_at`) VALUES (43, 17, 57, 'submissions/assignment_17_student_57.pdf', '2026-10-19T14:30', 'GRADED', 25.20, 'Thorough methodology and solid explanations. Well done.', 43, '2026-10-24T10:15');
INSERT INTO `assignment_submissions` (`id`, `assignment_id`, `student_id`, `file_path`, `submitted_at`, `status`, `marks`, `feedback`, `graded_by`, `graded_at`) VALUES (44, 17, 58, 'submissions/assignment_17_student_58.pdf', '2026-10-21T14:30', 'GRADED', 27.30, 'Satisfactory submission. Some theoretical explanations could be deeper.', 43, '2026-10-26T10:15');
INSERT INTO `assignment_submissions` (`id`, `assignment_id`, `student_id`, `file_path`, `submitted_at`, `status`, `marks`, `feedback`, `graded_by`, `graded_at`) VALUES (45, 17, 59, 'submissions/assignment_17_student_59.pdf', '2026-10-23T14:30', 'GRADED', 21.90, 'Comprehensive solution. Demonstrates deep understanding of the course concepts.', 43, '2026-10-28T10:15');
INSERT INTO `assignment_submissions` (`id`, `assignment_id`, `student_id`, `file_path`, `submitted_at`, `status`, `marks`, `feedback`, `graded_by`, `graded_at`) VALUES (46, 18, 50, 'submissions/assignment_18_student_50.pdf', '2026-10-15T14:30', 'GRADED', 17.50, 'Well structured report with clear diagrams and references. Excellent work.', 46, '2026-10-20T10:15');
INSERT INTO `assignment_submissions` (`id`, `assignment_id`, `student_id`, `file_path`, `submitted_at`, `status`, `marks`, `feedback`, `graded_by`, `graded_at`) VALUES (47, 18, 51, 'submissions/assignment_18_student_51.pdf', '2026-10-17T14:30', 'GRADED', 19.30, 'Good analysis and accurate calculations. Minor formatting improvements needed.', 46, '2026-10-22T10:15');
INSERT INTO `assignment_submissions` (`id`, `assignment_id`, `student_id`, `file_path`, `submitted_at`, `status`, `marks`, `feedback`, `graded_by`, `graded_at`) VALUES (48, 18, 52, 'submissions/assignment_18_student_52.pdf', '2026-10-19T14:30', 'GRADED', 21.00, 'Thorough methodology and solid explanations. Well done.', 46, '2026-10-24T10:15');
INSERT INTO `assignment_submissions` (`id`, `assignment_id`, `student_id`, `file_path`, `submitted_at`, `status`, `marks`, `feedback`, `graded_by`, `graded_at`) VALUES (49, 18, 53, 'submissions/assignment_18_student_53.pdf', '2026-10-21T14:30', 'GRADED', 22.70, 'Satisfactory submission. Some theoretical explanations could be deeper.', 46, '2026-10-26T10:15');
INSERT INTO `assignment_submissions` (`id`, `assignment_id`, `student_id`, `file_path`, `submitted_at`, `status`, `marks`, `feedback`, `graded_by`, `graded_at`) VALUES (50, 18, 54, 'submissions/assignment_18_student_54.pdf', '2026-10-23T14:30', 'GRADED', 18.30, 'Comprehensive solution. Demonstrates deep understanding of the course concepts.', 46, '2026-10-28T10:15');
INSERT INTO `assignment_submissions` (`id`, `assignment_id`, `student_id`, `file_path`, `submitted_at`, `status`, `marks`, `feedback`, `graded_by`, `graded_at`) VALUES (51, 19, 60, 'submissions/assignment_19_student_60.pdf', '2026-10-15T14:30', 'GRADED', 21.00, 'Well structured report with clear diagrams and references. Excellent work.', 47, '2026-10-20T10:15');
INSERT INTO `assignment_submissions` (`id`, `assignment_id`, `student_id`, `file_path`, `submitted_at`, `status`, `marks`, `feedback`, `graded_by`, `graded_at`) VALUES (52, 19, 61, 'submissions/assignment_19_student_61.pdf', '2026-10-17T14:30', 'GRADED', 23.10, 'Good analysis and accurate calculations. Minor formatting improvements needed.', 47, '2026-10-22T10:15');
INSERT INTO `assignment_submissions` (`id`, `assignment_id`, `student_id`, `file_path`, `submitted_at`, `status`, `marks`, `feedback`, `graded_by`, `graded_at`) VALUES (53, 19, 62, 'submissions/assignment_19_student_62.pdf', '2026-10-19T14:30', 'GRADED', 25.20, 'Thorough methodology and solid explanations. Well done.', 47, '2026-10-24T10:15');
INSERT INTO `assignment_submissions` (`id`, `assignment_id`, `student_id`, `file_path`, `submitted_at`, `status`, `marks`, `feedback`, `graded_by`, `graded_at`) VALUES (54, 19, 63, 'submissions/assignment_19_student_63.pdf', '2026-10-21T14:30', 'GRADED', 27.30, 'Satisfactory submission. Some theoretical explanations could be deeper.', 47, '2026-10-26T10:15');
INSERT INTO `assignment_submissions` (`id`, `assignment_id`, `student_id`, `file_path`, `submitted_at`, `status`, `marks`, `feedback`, `graded_by`, `graded_at`) VALUES (55, 19, 64, 'submissions/assignment_19_student_64.pdf', '2026-10-23T14:30', 'GRADED', 21.90, 'Comprehensive solution. Demonstrates deep understanding of the course concepts.', 47, '2026-10-28T10:15');
-- (50 records)

-- --------------------------------------------------------
-- Data for table `attendance`
-- --------------------------------------------------------
INSERT INTO `attendance` (`attendance_id`, `student_id`, `course_id`, `session_date`, `session_type`, `session_number`, `hours`, `status`, `recorded_by`, `recorded_at`) VALUES (1, 11, 3, '2026-08-03', 'THEORY', 1, 2, 'PRESENT', 7, '2026-11-01 01:01:30');
INSERT INTO `attendance` (`attendance_id`, `student_id`, `course_id`, `session_date`, `session_type`, `session_number`, `hours`, `status`, `recorded_by`, `recorded_at`) VALUES (2, 11, 3, '2026-08-03', 'PRACTICAL', 1, 2, 'PRESENT', 7, '2026-11-01 01:01:30');
INSERT INTO `attendance` (`attendance_id`, `student_id`, `course_id`, `session_date`, `session_type`, `session_number`, `hours`, `status`, `recorded_by`, `recorded_at`) VALUES (3, 12, 3, '2026-08-03', 'THEORY', 1, 2, 'PRESENT', 7, '2026-11-01 01:01:30');
INSERT INTO `attendance` (`attendance_id`, `student_id`, `course_id`, `session_date`, `session_type`, `session_number`, `hours`, `status`, `recorded_by`, `recorded_at`) VALUES (4, 12, 3, '2026-08-03', 'PRACTICAL', 1, 2, 'PRESENT', 7, '2026-11-01 01:01:30');
INSERT INTO `attendance` (`attendance_id`, `student_id`, `course_id`, `session_date`, `session_type`, `session_number`, `hours`, `status`, `recorded_by`, `recorded_at`) VALUES (5, 13, 3, '2026-08-03', 'THEORY', 1, 2, 'PRESENT', 7, '2026-11-01 01:01:30');
INSERT INTO `attendance` (`attendance_id`, `student_id`, `course_id`, `session_date`, `session_type`, `session_number`, `hours`, `status`, `recorded_by`, `recorded_at`) VALUES (6, 13, 3, '2026-08-03', 'PRACTICAL', 1, 2, 'PRESENT', 7, '2026-11-01 01:01:30');
INSERT INTO `attendance` (`attendance_id`, `student_id`, `course_id`, `session_date`, `session_type`, `session_number`, `hours`, `status`, `recorded_by`, `recorded_at`) VALUES (7, 14, 3, '2026-08-03', 'THEORY', 1, 2, 'PRESENT', 7, '2026-11-01 01:01:30');
INSERT INTO `attendance` (`attendance_id`, `student_id`, `course_id`, `session_date`, `session_type`, `session_number`, `hours`, `status`, `recorded_by`, `recorded_at`) VALUES (8, 14, 3, '2026-08-03', 'PRACTICAL', 1, 2, 'PRESENT', 7, '2026-11-01 01:01:30');
INSERT INTO `attendance` (`attendance_id`, `student_id`, `course_id`, `session_date`, `session_type`, `session_number`, `hours`, `status`, `recorded_by`, `recorded_at`) VALUES (9, 15, 3, '2026-08-03', 'THEORY', 1, 2, 'PRESENT', 7, '2026-11-01 01:01:30');
INSERT INTO `attendance` (`attendance_id`, `student_id`, `course_id`, `session_date`, `session_type`, `session_number`, `hours`, `status`, `recorded_by`, `recorded_at`) VALUES (10, 15, 3, '2026-08-03', 'PRACTICAL', 1, 2, 'PRESENT', 7, '2026-11-01 01:01:30');
INSERT INTO `attendance` (`attendance_id`, `student_id`, `course_id`, `session_date`, `session_type`, `session_number`, `hours`, `status`, `recorded_by`, `recorded_at`) VALUES (11, 16, 3, '2026-08-03', 'THEORY', 1, 2, 'PRESENT', 7, '2026-11-01 01:01:30');
INSERT INTO `attendance` (`attendance_id`, `student_id`, `course_id`, `session_date`, `session_type`, `session_number`, `hours`, `status`, `recorded_by`, `recorded_at`) VALUES (12, 16, 3, '2026-08-03', 'PRACTICAL', 1, 2, 'PRESENT', 7, '2026-11-01 01:01:30');
INSERT INTO `attendance` (`attendance_id`, `student_id`, `course_id`, `session_date`, `session_type`, `session_number`, `hours`, `status`, `recorded_by`, `recorded_at`) VALUES (13, 17, 3, '2026-08-03', 'THEORY', 1, 2, 'PRESENT', 7, '2026-11-01 01:01:30');
INSERT INTO `attendance` (`attendance_id`, `student_id`, `course_id`, `session_date`, `session_type`, `session_number`, `hours`, `status`, `recorded_by`, `recorded_at`) VALUES (14, 17, 3, '2026-08-03', 'PRACTICAL', 1, 2, 'PRESENT', 7, '2026-11-01 01:01:30');
INSERT INTO `attendance` (`attendance_id`, `student_id`, `course_id`, `session_date`, `session_type`, `session_number`, `hours`, `status`, `recorded_by`, `recorded_at`) VALUES (15, 18, 3, '2026-08-03', 'THEORY', 1, 2, 'PRESENT', 7, '2026-11-01 01:01:30');
INSERT INTO `attendance` (`attendance_id`, `student_id`, `course_id`, `session_date`, `session_type`, `session_number`, `hours`, `status`, `recorded_by`, `recorded_at`) VALUES (16, 18, 3, '2026-08-03', 'PRACTICAL', 1, 2, 'PRESENT', 7, '2026-11-01 01:01:30');
INSERT INTO `attendance` (`attendance_id`, `student_id`, `course_id`, `session_date`, `session_type`, `session_number`, `hours`, `status`, `recorded_by`, `recorded_at`) VALUES (17, 29, 3, '2026-08-03', 'THEORY', 1, 2, 'PRESENT', 7, '2026-11-01 01:01:30');
INSERT INTO `attendance` (`attendance_id`, `student_id`, `course_id`, `session_date`, `session_type`, `session_number`, `hours`, `status`, `recorded_by`, `recorded_at`) VALUES (18, 29, 3, '2026-08-03', 'PRACTICAL', 1, 2, 'PRESENT', 7, '2026-11-01 01:01:30');
INSERT INTO `attendance` (`attendance_id`, `student_id`, `course_id`, `session_date`, `session_type`, `session_number`, `hours`, `status`, `recorded_by`, `recorded_at`) VALUES (19, 11, 3, '2026-08-10', 'THEORY', 2, 2, 'PRESENT', 7, '2026-11-01 01:01:30');
INSERT INTO `attendance` (`attendance_id`, `student_id`, `course_id`, `session_date`, `session_type`, `session_number`, `hours`, `status`, `recorded_by`, `recorded_at`) VALUES (20, 11, 3, '2026-08-10', 'PRACTICAL', 2, 2, 'PRESENT', 7, '2026-11-01 01:01:30');
INSERT INTO `attendance` (`attendance_id`, `student_id`, `course_id`, `session_date`, `session_type`, `session_number`, `hours`, `status`, `recorded_by`, `recorded_at`) VALUES (21, 12, 3, '2026-08-10', 'THEORY', 2, 2, 'PRESENT', 7, '2026-11-01 01:01:30');
INSERT INTO `attendance` (`attendance_id`, `student_id`, `course_id`, `session_date`, `session_type`, `session_number`, `hours`, `status`, `recorded_by`, `recorded_at`) VALUES (22, 12, 3, '2026-08-10', 'PRACTICAL', 2, 2, 'PRESENT', 7, '2026-11-01 01:01:30');
INSERT INTO `attendance` (`attendance_id`, `student_id`, `course_id`, `session_date`, `session_type`, `session_number`, `hours`, `status`, `recorded_by`, `recorded_at`) VALUES (23, 13, 3, '2026-08-10', 'THEORY', 2, 2, 'PRESENT', 7, '2026-11-01 01:01:30');
INSERT INTO `attendance` (`attendance_id`, `student_id`, `course_id`, `session_date`, `session_type`, `session_number`, `hours`, `status`, `recorded_by`, `recorded_at`) VALUES (24, 13, 3, '2026-08-10', 'PRACTICAL', 2, 2, 'PRESENT', 7, '2026-11-01 01:01:30');
INSERT INTO `attendance` (`attendance_id`, `student_id`, `course_id`, `session_date`, `session_type`, `session_number`, `hours`, `status`, `recorded_by`, `recorded_at`) VALUES (25, 14, 3, '2026-08-10', 'THEORY', 2, 2, 'PRESENT', 7, '2026-11-01 01:01:30');
INSERT INTO `attendance` (`attendance_id`, `student_id`, `course_id`, `session_date`, `session_type`, `session_number`, `hours`, `status`, `recorded_by`, `recorded_at`) VALUES (26, 14, 3, '2026-08-10', 'PRACTICAL', 2, 2, 'PRESENT', 7, '2026-11-01 01:01:30');
INSERT INTO `attendance` (`attendance_id`, `student_id`, `course_id`, `session_date`, `session_type`, `session_number`, `hours`, `status`, `recorded_by`, `recorded_at`) VALUES (27, 15, 3, '2026-08-10', 'THEORY', 2, 2, 'PRESENT', 7, '2026-11-01 01:01:30');
INSERT INTO `attendance` (`attendance_id`, `student_id`, `course_id`, `session_date`, `session_type`, `session_number`, `hours`, `status`, `recorded_by`, `recorded_at`) VALUES (28, 15, 3, '2026-08-10', 'PRACTICAL', 2, 2, 'PRESENT', 7, '2026-11-01 01:01:30');
INSERT INTO `attendance` (`attendance_id`, `student_id`, `course_id`, `session_date`, `session_type`, `session_number`, `hours`, `status`, `recorded_by`, `recorded_at`) VALUES (29, 16, 3, '2026-08-10', 'THEORY', 2, 2, 'PRESENT', 7, '2026-11-01 01:01:30');
INSERT INTO `attendance` (`attendance_id`, `student_id`, `course_id`, `session_date`, `session_type`, `session_number`, `hours`, `status`, `recorded_by`, `recorded_at`) VALUES (30, 16, 3, '2026-08-10', 'PRACTICAL', 2, 2, 'PRESENT', 7, '2026-11-01 01:01:30');
INSERT INTO `attendance` (`attendance_id`, `student_id`, `course_id`, `session_date`, `session_type`, `session_number`, `hours`, `status`, `recorded_by`, `recorded_at`) VALUES (31, 17, 3, '2026-08-10', 'THEORY', 2, 2, 'PRESENT', 7, '2026-11-01 01:01:30');
INSERT INTO `attendance` (`attendance_id`, `student_id`, `course_id`, `session_date`, `session_type`, `session_number`, `hours`, `status`, `recorded_by`, `recorded_at`) VALUES (32, 17, 3, '2026-08-10', 'PRACTICAL', 2, 2, 'PRESENT', 7, '2026-11-01 01:01:30');
INSERT INTO `attendance` (`attendance_id`, `student_id`, `course_id`, `session_date`, `session_type`, `session_number`, `hours`, `status`, `recorded_by`, `recorded_at`) VALUES (33, 18, 3, '2026-08-10', 'THEORY', 2, 2, 'PRESENT', 7, '2026-11-01 01:01:30');
INSERT INTO `attendance` (`attendance_id`, `student_id`, `course_id`, `session_date`, `session_type`, `session_number`, `hours`, `status`, `recorded_by`, `recorded_at`) VALUES (34, 18, 3, '2026-08-10', 'PRACTICAL', 2, 2, 'PRESENT', 7, '2026-11-01 01:01:30');
INSERT INTO `attendance` (`attendance_id`, `student_id`, `course_id`, `session_date`, `session_type`, `session_number`, `hours`, `status`, `recorded_by`, `recorded_at`) VALUES (35, 29, 3, '2026-08-10', 'THEORY', 2, 2, 'PRESENT', 7, '2026-11-01 01:01:30');
INSERT INTO `attendance` (`attendance_id`, `student_id`, `course_id`, `session_date`, `session_type`, `session_number`, `hours`, `status`, `recorded_by`, `recorded_at`) VALUES (36, 29, 3, '2026-08-10', 'PRACTICAL', 2, 2, 'PRESENT', 7, '2026-11-01 01:01:30');
INSERT INTO `attendance` (`attendance_id`, `student_id`, `course_id`, `session_date`, `session_type`, `session_number`, `hours`, `status`, `recorded_by`, `recorded_at`) VALUES (37, 11, 3, '2026-08-17', 'THEORY', 3, 2, 'PRESENT', 7, '2026-11-01 01:01:30');
INSERT INTO `attendance` (`attendance_id`, `student_id`, `course_id`, `session_date`, `session_type`, `session_number`, `hours`, `status`, `recorded_by`, `recorded_at`) VALUES (38, 11, 3, '2026-08-17', 'PRACTICAL', 3, 2, 'PRESENT', 7, '2026-11-01 01:01:30');
INSERT INTO `attendance` (`attendance_id`, `student_id`, `course_id`, `session_date`, `session_type`, `session_number`, `hours`, `status`, `recorded_by`, `recorded_at`) VALUES (39, 12, 3, '2026-08-17', 'THEORY', 3, 2, 'PRESENT', 7, '2026-11-01 01:01:30');
INSERT INTO `attendance` (`attendance_id`, `student_id`, `course_id`, `session_date`, `session_type`, `session_number`, `hours`, `status`, `recorded_by`, `recorded_at`) VALUES (40, 12, 3, '2026-08-17', 'PRACTICAL', 3, 2, 'PRESENT', 7, '2026-11-01 01:01:30');
INSERT INTO `attendance` (`attendance_id`, `student_id`, `course_id`, `session_date`, `session_type`, `session_number`, `hours`, `status`, `recorded_by`, `recorded_at`) VALUES (41, 13, 3, '2026-08-17', 'THEORY', 3, 2, 'PRESENT', 7, '2026-11-01 01:01:30');
INSERT INTO `attendance` (`attendance_id`, `student_id`, `course_id`, `session_date`, `session_type`, `session_number`, `hours`, `status`, `recorded_by`, `recorded_at`) VALUES (42, 13, 3, '2026-08-17', 'PRACTICAL', 3, 2, 'PRESENT', 7, '2026-11-01 01:01:30');
INSERT INTO `attendance` (`attendance_id`, `student_id`, `course_id`, `session_date`, `session_type`, `session_number`, `hours`, `status`, `recorded_by`, `recorded_at`) VALUES (43, 14, 3, '2026-08-17', 'THEORY', 3, 2, 'PRESENT', 7, '2026-11-01 01:01:30');
INSERT INTO `attendance` (`attendance_id`, `student_id`, `course_id`, `session_date`, `session_type`, `session_number`, `hours`, `status`, `recorded_by`, `recorded_at`) VALUES (44, 14, 3, '2026-08-17', 'PRACTICAL', 3, 2, 'PRESENT', 7, '2026-11-01 01:01:30');
INSERT INTO `attendance` (`attendance_id`, `student_id`, `course_id`, `session_date`, `session_type`, `session_number`, `hours`, `status`, `recorded_by`, `recorded_at`) VALUES (45, 15, 3, '2026-08-17', 'THEORY', 3, 2, 'PRESENT', 7, '2026-11-01 01:01:30');
INSERT INTO `attendance` (`attendance_id`, `student_id`, `course_id`, `session_date`, `session_type`, `session_number`, `hours`, `status`, `recorded_by`, `recorded_at`) VALUES (46, 15, 3, '2026-08-17', 'PRACTICAL', 3, 2, 'PRESENT', 7, '2026-11-01 01:01:30');
INSERT INTO `attendance` (`attendance_id`, `student_id`, `course_id`, `session_date`, `session_type`, `session_number`, `hours`, `status`, `recorded_by`, `recorded_at`) VALUES (47, 16, 3, '2026-08-17', 'THEORY', 3, 2, 'PRESENT', 7, '2026-11-01 01:01:30');
INSERT INTO `attendance` (`attendance_id`, `student_id`, `course_id`, `session_date`, `session_type`, `session_number`, `hours`, `status`, `recorded_by`, `recorded_at`) VALUES (48, 16, 3, '2026-08-17', 'PRACTICAL', 3, 2, 'PRESENT', 7, '2026-11-01 01:01:30');
INSERT INTO `attendance` (`attendance_id`, `student_id`, `course_id`, `session_date`, `session_type`, `session_number`, `hours`, `status`, `recorded_by`, `recorded_at`) VALUES (49, 17, 3, '2026-08-17', 'THEORY', 3, 2, 'PRESENT', 7, '2026-11-01 01:01:30');
INSERT INTO `attendance` (`attendance_id`, `student_id`, `course_id`, `session_date`, `session_type`, `session_number`, `hours`, `status`, `recorded_by`, `recorded_at`) VALUES (50, 17, 3, '2026-08-17', 'PRACTICAL', 3, 2, 'PRESENT', 7, '2026-11-01 01:01:30');
INSERT INTO `attendance` (`attendance_id`, `student_id`, `course_id`, `session_date`, `session_type`, `session_number`, `hours`, `status`, `recorded_by`, `recorded_at`) VALUES (51, 18, 3, '2026-08-17', 'THEORY', 3, 2, 'PRESENT', 7, '2026-11-01 01:01:30');
INSERT INTO `attendance` (`attendance_id`, `student_id`, `course_id`, `session_date`, `session_type`, `session_number`, `hours`, `status`, `recorded_by`, `recorded_at`) VALUES (52, 18, 3, '2026-08-17', 'PRACTICAL', 3, 2, 'PRESENT', 7, '2026-11-01 01:01:30');
INSERT INTO `attendance` (`attendance_id`, `student_id`, `course_id`, `session_date`, `session_type`, `session_number`, `hours`, `status`, `recorded_by`, `recorded_at`) VALUES (53, 29, 3, '2026-08-17', 'THEORY', 3, 2, 'ABSENT', 7, '2026-11-01 01:01:30');
INSERT INTO `attendance` (`attendance_id`, `student_id`, `course_id`, `session_date`, `session_type`, `session_number`, `hours`, `status`, `recorded_by`, `recorded_at`) VALUES (54, 29, 3, '2026-08-17', 'PRACTICAL', 3, 2, 'ABSENT', 7, '2026-11-01 01:01:30');
INSERT INTO `attendance` (`attendance_id`, `student_id`, `course_id`, `session_date`, `session_type`, `session_number`, `hours`, `status`, `recorded_by`, `recorded_at`) VALUES (55, 11, 3, '2026-08-24', 'THEORY', 4, 2, 'PRESENT', 7, '2026-11-01 01:01:30');
INSERT INTO `attendance` (`attendance_id`, `student_id`, `course_id`, `session_date`, `session_type`, `session_number`, `hours`, `status`, `recorded_by`, `recorded_at`) VALUES (56, 11, 3, '2026-08-24', 'PRACTICAL', 4, 2, 'PRESENT', 7, '2026-11-01 01:01:30');
INSERT INTO `attendance` (`attendance_id`, `student_id`, `course_id`, `session_date`, `session_type`, `session_number`, `hours`, `status`, `recorded_by`, `recorded_at`) VALUES (57, 12, 3, '2026-08-24', 'THEORY', 4, 2, 'PRESENT', 7, '2026-11-01 01:01:30');
INSERT INTO `attendance` (`attendance_id`, `student_id`, `course_id`, `session_date`, `session_type`, `session_number`, `hours`, `status`, `recorded_by`, `recorded_at`) VALUES (58, 12, 3, '2026-08-24', 'PRACTICAL', 4, 2, 'PRESENT', 7, '2026-11-01 01:01:30');
INSERT INTO `attendance` (`attendance_id`, `student_id`, `course_id`, `session_date`, `session_type`, `session_number`, `hours`, `status`, `recorded_by`, `recorded_at`) VALUES (59, 13, 3, '2026-08-24', 'THEORY', 4, 2, 'PRESENT', 7, '2026-11-01 01:01:30');
INSERT INTO `attendance` (`attendance_id`, `student_id`, `course_id`, `session_date`, `session_type`, `session_number`, `hours`, `status`, `recorded_by`, `recorded_at`) VALUES (60, 13, 3, '2026-08-24', 'PRACTICAL', 4, 2, 'PRESENT', 7, '2026-11-01 01:01:30');
INSERT INTO `attendance` (`attendance_id`, `student_id`, `course_id`, `session_date`, `session_type`, `session_number`, `hours`, `status`, `recorded_by`, `recorded_at`) VALUES (61, 14, 3, '2026-08-24', 'THEORY', 4, 2, 'PRESENT', 7, '2026-11-01 01:01:31');
INSERT INTO `attendance` (`attendance_id`, `student_id`, `course_id`, `session_date`, `session_type`, `session_number`, `hours`, `status`, `recorded_by`, `recorded_at`) VALUES (62, 14, 3, '2026-08-24', 'PRACTICAL', 4, 2, 'PRESENT', 7, '2026-11-01 01:01:31');
INSERT INTO `attendance` (`attendance_id`, `student_id`, `course_id`, `session_date`, `session_type`, `session_number`, `hours`, `status`, `recorded_by`, `recorded_at`) VALUES (63, 15, 3, '2026-08-24', 'THEORY', 4, 2, 'PRESENT', 7, '2026-11-01 01:01:31');
INSERT INTO `attendance` (`attendance_id`, `student_id`, `course_id`, `session_date`, `session_type`, `session_number`, `hours`, `status`, `recorded_by`, `recorded_at`) VALUES (64, 15, 3, '2026-08-24', 'PRACTICAL', 4, 2, 'PRESENT', 7, '2026-11-01 01:01:31');
INSERT INTO `attendance` (`attendance_id`, `student_id`, `course_id`, `session_date`, `session_type`, `session_number`, `hours`, `status`, `recorded_by`, `recorded_at`) VALUES (65, 16, 3, '2026-08-24', 'THEORY', 4, 2, 'PRESENT', 7, '2026-11-01 01:01:31');
INSERT INTO `attendance` (`attendance_id`, `student_id`, `course_id`, `session_date`, `session_type`, `session_number`, `hours`, `status`, `recorded_by`, `recorded_at`) VALUES (66, 16, 3, '2026-08-24', 'PRACTICAL', 4, 2, 'PRESENT', 7, '2026-11-01 01:01:31');
INSERT INTO `attendance` (`attendance_id`, `student_id`, `course_id`, `session_date`, `session_type`, `session_number`, `hours`, `status`, `recorded_by`, `recorded_at`) VALUES (67, 17, 3, '2026-08-24', 'THEORY', 4, 2, 'PRESENT', 7, '2026-11-01 01:01:31');
INSERT INTO `attendance` (`attendance_id`, `student_id`, `course_id`, `session_date`, `session_type`, `session_number`, `hours`, `status`, `recorded_by`, `recorded_at`) VALUES (68, 17, 3, '2026-08-24', 'PRACTICAL', 4, 2, 'PRESENT', 7, '2026-11-01 01:01:31');
INSERT INTO `attendance` (`attendance_id`, `student_id`, `course_id`, `session_date`, `session_type`, `session_number`, `hours`, `status`, `recorded_by`, `recorded_at`) VALUES (69, 18, 3, '2026-08-24', 'THEORY', 4, 2, 'PRESENT', 7, '2026-11-01 01:01:31');
INSERT INTO `attendance` (`attendance_id`, `student_id`, `course_id`, `session_date`, `session_type`, `session_number`, `hours`, `status`, `recorded_by`, `recorded_at`) VALUES (70, 18, 3, '2026-08-24', 'PRACTICAL', 4, 2, 'PRESENT', 7, '2026-11-01 01:01:31');
INSERT INTO `attendance` (`attendance_id`, `student_id`, `course_id`, `session_date`, `session_type`, `session_number`, `hours`, `status`, `recorded_by`, `recorded_at`) VALUES (71, 29, 3, '2026-08-24', 'THEORY', 4, 2, 'PRESENT', 7, '2026-11-01 01:01:31');
INSERT INTO `attendance` (`attendance_id`, `student_id`, `course_id`, `session_date`, `session_type`, `session_number`, `hours`, `status`, `recorded_by`, `recorded_at`) VALUES (72, 29, 3, '2026-08-24', 'PRACTICAL', 4, 2, 'PRESENT', 7, '2026-11-01 01:01:31');
INSERT INTO `attendance` (`attendance_id`, `student_id`, `course_id`, `session_date`, `session_type`, `session_number`, `hours`, `status`, `recorded_by`, `recorded_at`) VALUES (73, 11, 3, '2026-08-31', 'THEORY', 5, 2, 'PRESENT', 7, '2026-11-01 01:01:31');
INSERT INTO `attendance` (`attendance_id`, `student_id`, `course_id`, `session_date`, `session_type`, `session_number`, `hours`, `status`, `recorded_by`, `recorded_at`) VALUES (74, 11, 3, '2026-08-31', 'PRACTICAL', 5, 2, 'PRESENT', 7, '2026-11-01 01:01:31');
INSERT INTO `attendance` (`attendance_id`, `student_id`, `course_id`, `session_date`, `session_type`, `session_number`, `hours`, `status`, `recorded_by`, `recorded_at`) VALUES (75, 12, 3, '2026-08-31', 'THEORY', 5, 2, 'ABSENT', 7, '2026-11-01 01:01:31');
INSERT INTO `attendance` (`attendance_id`, `student_id`, `course_id`, `session_date`, `session_type`, `session_number`, `hours`, `status`, `recorded_by`, `recorded_at`) VALUES (76, 12, 3, '2026-08-31', 'PRACTICAL', 5, 2, 'PRESENT', 7, '2026-11-01 01:01:31');
INSERT INTO `attendance` (`attendance_id`, `student_id`, `course_id`, `session_date`, `session_type`, `session_number`, `hours`, `status`, `recorded_by`, `recorded_at`) VALUES (77, 13, 3, '2026-08-31', 'THEORY', 5, 2, 'PRESENT', 7, '2026-11-01 01:01:31');
INSERT INTO `attendance` (`attendance_id`, `student_id`, `course_id`, `session_date`, `session_type`, `session_number`, `hours`, `status`, `recorded_by`, `recorded_at`) VALUES (78, 13, 3, '2026-08-31', 'PRACTICAL', 5, 2, 'PRESENT', 7, '2026-11-01 01:01:31');
INSERT INTO `attendance` (`attendance_id`, `student_id`, `course_id`, `session_date`, `session_type`, `session_number`, `hours`, `status`, `recorded_by`, `recorded_at`) VALUES (79, 14, 3, '2026-08-31', 'THEORY', 5, 2, 'PRESENT', 7, '2026-11-01 01:01:31');
INSERT INTO `attendance` (`attendance_id`, `student_id`, `course_id`, `session_date`, `session_type`, `session_number`, `hours`, `status`, `recorded_by`, `recorded_at`) VALUES (80, 14, 3, '2026-08-31', 'PRACTICAL', 5, 2, 'PRESENT', 7, '2026-11-01 01:01:31');
INSERT INTO `attendance` (`attendance_id`, `student_id`, `course_id`, `session_date`, `session_type`, `session_number`, `hours`, `status`, `recorded_by`, `recorded_at`) VALUES (81, 15, 3, '2026-08-31', 'THEORY', 5, 2, 'PRESENT', 7, '2026-11-01 01:01:31');
INSERT INTO `attendance` (`attendance_id`, `student_id`, `course_id`, `session_date`, `session_type`, `session_number`, `hours`, `status`, `recorded_by`, `recorded_at`) VALUES (82, 15, 3, '2026-08-31', 'PRACTICAL', 5, 2, 'PRESENT', 7, '2026-11-01 01:01:31');
INSERT INTO `attendance` (`attendance_id`, `student_id`, `course_id`, `session_date`, `session_type`, `session_number`, `hours`, `status`, `recorded_by`, `recorded_at`) VALUES (83, 16, 3, '2026-08-31', 'THEORY', 5, 2, 'PRESENT', 7, '2026-11-01 01:01:31');
INSERT INTO `attendance` (`attendance_id`, `student_id`, `course_id`, `session_date`, `session_type`, `session_number`, `hours`, `status`, `recorded_by`, `recorded_at`) VALUES (84, 16, 3, '2026-08-31', 'PRACTICAL', 5, 2, 'PRESENT', 7, '2026-11-01 01:01:31');
INSERT INTO `attendance` (`attendance_id`, `student_id`, `course_id`, `session_date`, `session_type`, `session_number`, `hours`, `status`, `recorded_by`, `recorded_at`) VALUES (85, 17, 3, '2026-08-31', 'THEORY', 5, 2, 'PRESENT', 7, '2026-11-01 01:01:31');
INSERT INTO `attendance` (`attendance_id`, `student_id`, `course_id`, `session_date`, `session_type`, `session_number`, `hours`, `status`, `recorded_by`, `recorded_at`) VALUES (86, 17, 3, '2026-08-31', 'PRACTICAL', 5, 2, 'PRESENT', 7, '2026-11-01 01:01:31');
INSERT INTO `attendance` (`attendance_id`, `student_id`, `course_id`, `session_date`, `session_type`, `session_number`, `hours`, `status`, `recorded_by`, `recorded_at`) VALUES (87, 18, 3, '2026-08-31', 'THEORY', 5, 2, 'PRESENT', 7, '2026-11-01 01:01:31');
INSERT INTO `attendance` (`attendance_id`, `student_id`, `course_id`, `session_date`, `session_type`, `session_number`, `hours`, `status`, `recorded_by`, `recorded_at`) VALUES (88, 18, 3, '2026-08-31', 'PRACTICAL', 5, 2, 'PRESENT', 7, '2026-11-01 01:01:31');
INSERT INTO `attendance` (`attendance_id`, `student_id`, `course_id`, `session_date`, `session_type`, `session_number`, `hours`, `status`, `recorded_by`, `recorded_at`) VALUES (89, 29, 3, '2026-08-31', 'THEORY', 5, 2, 'PRESENT', 7, '2026-11-01 01:01:31');
INSERT INTO `attendance` (`attendance_id`, `student_id`, `course_id`, `session_date`, `session_type`, `session_number`, `hours`, `status`, `recorded_by`, `recorded_at`) VALUES (90, 29, 3, '2026-08-31', 'PRACTICAL', 5, 2, 'PRESENT', 7, '2026-11-01 01:01:31');
INSERT INTO `attendance` (`attendance_id`, `student_id`, `course_id`, `session_date`, `session_type`, `session_number`, `hours`, `status`, `recorded_by`, `recorded_at`) VALUES (91, 11, 3, '2026-09-07', 'THEORY', 6, 2, 'PRESENT', 7, '2026-11-01 01:01:31');
INSERT INTO `attendance` (`attendance_id`, `student_id`, `course_id`, `session_date`, `session_type`, `session_number`, `hours`, `status`, `recorded_by`, `recorded_at`) VALUES (92, 11, 3, '2026-09-07', 'PRACTICAL', 6, 2, 'PRESENT', 7, '2026-11-01 01:01:31');
INSERT INTO `attendance` (`attendance_id`, `student_id`, `course_id`, `session_date`, `session_type`, `session_number`, `hours`, `status`, `recorded_by`, `recorded_at`) VALUES (93, 12, 3, '2026-09-07', 'THEORY', 6, 2, 'PRESENT', 7, '2026-11-01 01:01:31');
INSERT INTO `attendance` (`attendance_id`, `student_id`, `course_id`, `session_date`, `session_type`, `session_number`, `hours`, `status`, `recorded_by`, `recorded_at`) VALUES (94, 12, 3, '2026-09-07', 'PRACTICAL', 6, 2, 'PRESENT', 7, '2026-11-01 01:01:31');
INSERT INTO `attendance` (`attendance_id`, `student_id`, `course_id`, `session_date`, `session_type`, `session_number`, `hours`, `status`, `recorded_by`, `recorded_at`) VALUES (95, 13, 3, '2026-09-07', 'THEORY', 6, 2, 'PRESENT', 7, '2026-11-01 01:01:31');
INSERT INTO `attendance` (`attendance_id`, `student_id`, `course_id`, `session_date`, `session_type`, `session_number`, `hours`, `status`, `recorded_by`, `recorded_at`) VALUES (96, 13, 3, '2026-09-07', 'PRACTICAL', 6, 2, 'PRESENT', 7, '2026-11-01 01:01:31');
INSERT INTO `attendance` (`attendance_id`, `student_id`, `course_id`, `session_date`, `session_type`, `session_number`, `hours`, `status`, `recorded_by`, `recorded_at`) VALUES (97, 14, 3, '2026-09-07', 'THEORY', 6, 2, 'PRESENT', 7, '2026-11-01 01:01:31');
INSERT INTO `attendance` (`attendance_id`, `student_id`, `course_id`, `session_date`, `session_type`, `session_number`, `hours`, `status`, `recorded_by`, `recorded_at`) VALUES (98, 14, 3, '2026-09-07', 'PRACTICAL', 6, 2, 'PRESENT', 7, '2026-11-01 01:01:31');
INSERT INTO `attendance` (`attendance_id`, `student_id`, `course_id`, `session_date`, `session_type`, `session_number`, `hours`, `status`, `recorded_by`, `recorded_at`) VALUES (99, 15, 3, '2026-09-07', 'THEORY', 6, 2, 'PRESENT', 7, '2026-11-01 01:01:31');
INSERT INTO `attendance` (`attendance_id`, `student_id`, `course_id`, `session_date`, `session_type`, `session_number`, `hours`, `status`, `recorded_by`, `recorded_at`) VALUES (100, 15, 3, '2026-09-07', 'PRACTICAL', 6, 2, 'PRESENT', 7, '2026-11-01 01:01:31');
INSERT INTO `attendance` (`attendance_id`, `student_id`, `course_id`, `session_date`, `session_type`, `session_number`, `hours`, `status`, `recorded_by`, `recorded_at`) VALUES (101, 16, 3, '2026-09-07', 'THEORY', 6, 2, 'PRESENT', 7, '2026-11-01 01:01:31');
INSERT INTO `attendance` (`attendance_id`, `student_id`, `course_id`, `session_date`, `session_type`, `session_number`, `hours`, `status`, `recorded_by`, `recorded_at`) VALUES (102, 16, 3, '2026-09-07', 'PRACTICAL', 6, 2, 'PRESENT', 7, '2026-11-01 01:01:31');
INSERT INTO `attendance` (`attendance_id`, `student_id`, `course_id`, `session_date`, `session_type`, `session_number`, `hours`, `status`, `recorded_by`, `recorded_at`) VALUES (103, 17, 3, '2026-09-07', 'THEORY', 6, 2, 'PRESENT', 7, '2026-11-01 01:01:31');
INSERT INTO `attendance` (`attendance_id`, `student_id`, `course_id`, `session_date`, `session_type`, `session_number`, `hours`, `status`, `recorded_by`, `recorded_at`) VALUES (104, 17, 3, '2026-09-07', 'PRACTICAL', 6, 2, 'PRESENT', 7, '2026-11-01 01:01:31');
INSERT INTO `attendance` (`attendance_id`, `student_id`, `course_id`, `session_date`, `session_type`, `session_number`, `hours`, `status`, `recorded_by`, `recorded_at`) VALUES (105, 18, 3, '2026-09-07', 'THEORY', 6, 2, 'PRESENT', 7, '2026-11-01 01:01:31');
INSERT INTO `attendance` (`attendance_id`, `student_id`, `course_id`, `session_date`, `session_type`, `session_number`, `hours`, `status`, `recorded_by`, `recorded_at`) VALUES (106, 18, 3, '2026-09-07', 'PRACTICAL', 6, 2, 'PRESENT', 7, '2026-11-01 01:01:31');
INSERT INTO `attendance` (`attendance_id`, `student_id`, `course_id`, `session_date`, `session_type`, `session_number`, `hours`, `status`, `recorded_by`, `recorded_at`) VALUES (107, 29, 3, '2026-09-07', 'THEORY', 6, 2, 'PRESENT', 7, '2026-11-01 01:01:31');
INSERT INTO `attendance` (`attendance_id`, `student_id`, `course_id`, `session_date`, `session_type`, `session_number`, `hours`, `status`, `recorded_by`, `recorded_at`) VALUES (108, 29, 3, '2026-09-07', 'PRACTICAL', 6, 2, 'PRESENT', 7, '2026-11-01 01:01:31');
INSERT INTO `attendance` (`attendance_id`, `student_id`, `course_id`, `session_date`, `session_type`, `session_number`, `hours`, `status`, `recorded_by`, `recorded_at`) VALUES (109, 11, 3, '2026-09-14', 'THEORY', 7, 2, 'PRESENT', 7, '2026-11-01 01:01:31');
INSERT INTO `attendance` (`attendance_id`, `student_id`, `course_id`, `session_date`, `session_type`, `session_number`, `hours`, `status`, `recorded_by`, `recorded_at`) VALUES (110, 11, 3, '2026-09-14', 'PRACTICAL', 7, 2, 'PRESENT', 7, '2026-11-01 01:01:31');
INSERT INTO `attendance` (`attendance_id`, `student_id`, `course_id`, `session_date`, `session_type`, `session_number`, `hours`, `status`, `recorded_by`, `recorded_at`) VALUES (111, 12, 3, '2026-09-14', 'THEORY', 7, 2, 'PRESENT', 7, '2026-11-01 01:01:31');
INSERT INTO `attendance` (`attendance_id`, `student_id`, `course_id`, `session_date`, `session_type`, `session_number`, `hours`, `status`, `recorded_by`, `recorded_at`) VALUES (112, 12, 3, '2026-09-14', 'PRACTICAL', 7, 2, 'PRESENT', 7, '2026-11-01 01:01:31');
INSERT INTO `attendance` (`attendance_id`, `student_id`, `course_id`, `session_date`, `session_type`, `session_number`, `hours`, `status`, `recorded_by`, `recorded_at`) VALUES (113, 13, 3, '2026-09-14', 'THEORY', 7, 2, 'PRESENT', 7, '2026-11-01 01:01:31');
INSERT INTO `attendance` (`attendance_id`, `student_id`, `course_id`, `session_date`, `session_type`, `session_number`, `hours`, `status`, `recorded_by`, `recorded_at`) VALUES (114, 13, 3, '2026-09-14', 'PRACTICAL', 7, 2, 'PRESENT', 7, '2026-11-01 01:01:31');
INSERT INTO `attendance` (`attendance_id`, `student_id`, `course_id`, `session_date`, `session_type`, `session_number`, `hours`, `status`, `recorded_by`, `recorded_at`) VALUES (115, 14, 3, '2026-09-14', 'THEORY', 7, 2, 'PRESENT', 7, '2026-11-01 01:01:31');
INSERT INTO `attendance` (`attendance_id`, `student_id`, `course_id`, `session_date`, `session_type`, `session_number`, `hours`, `status`, `recorded_by`, `recorded_at`) VALUES (116, 14, 3, '2026-09-14', 'PRACTICAL', 7, 2, 'PRESENT', 7, '2026-11-01 01:01:31');
INSERT INTO `attendance` (`attendance_id`, `student_id`, `course_id`, `session_date`, `session_type`, `session_number`, `hours`, `status`, `recorded_by`, `recorded_at`) VALUES (117, 15, 3, '2026-09-14', 'THEORY', 7, 2, 'PRESENT', 7, '2026-11-01 01:01:31');
INSERT INTO `attendance` (`attendance_id`, `student_id`, `course_id`, `session_date`, `session_type`, `session_number`, `hours`, `status`, `recorded_by`, `recorded_at`) VALUES (118, 15, 3, '2026-09-14', 'PRACTICAL', 7, 2, 'PRESENT', 7, '2026-11-01 01:01:31');
INSERT INTO `attendance` (`attendance_id`, `student_id`, `course_id`, `session_date`, `session_type`, `session_number`, `hours`, `status`, `recorded_by`, `recorded_at`) VALUES (119, 16, 3, '2026-09-14', 'THEORY', 7, 2, 'ABSENT', 7, '2026-11-01 01:01:31');
INSERT INTO `attendance` (`attendance_id`, `student_id`, `course_id`, `session_date`, `session_type`, `session_number`, `hours`, `status`, `recorded_by`, `recorded_at`) VALUES (120, 16, 3, '2026-09-14', 'PRACTICAL', 7, 2, 'ABSENT', 7, '2026-11-01 01:01:31');
INSERT INTO `attendance` (`attendance_id`, `student_id`, `course_id`, `session_date`, `session_type`, `session_number`, `hours`, `status`, `recorded_by`, `recorded_at`) VALUES (121, 17, 3, '2026-09-14', 'THEORY', 7, 2, 'PRESENT', 7, '2026-11-01 01:01:31');
INSERT INTO `attendance` (`attendance_id`, `student_id`, `course_id`, `session_date`, `session_type`, `session_number`, `hours`, `status`, `recorded_by`, `recorded_at`) VALUES (122, 17, 3, '2026-09-14', 'PRACTICAL', 7, 2, 'PRESENT', 7, '2026-11-01 01:01:31');
INSERT INTO `attendance` (`attendance_id`, `student_id`, `course_id`, `session_date`, `session_type`, `session_number`, `hours`, `status`, `recorded_by`, `recorded_at`) VALUES (123, 18, 3, '2026-09-14', 'THEORY', 7, 2, 'PRESENT', 7, '2026-11-01 01:01:31');
INSERT INTO `attendance` (`attendance_id`, `student_id`, `course_id`, `session_date`, `session_type`, `session_number`, `hours`, `status`, `recorded_by`, `recorded_at`) VALUES (124, 18, 3, '2026-09-14', 'PRACTICAL', 7, 2, 'PRESENT', 7, '2026-11-01 01:01:31');
INSERT INTO `attendance` (`attendance_id`, `student_id`, `course_id`, `session_date`, `session_type`, `session_number`, `hours`, `status`, `recorded_by`, `recorded_at`) VALUES (125, 29, 3, '2026-09-14', 'THEORY', 7, 2, 'ABSENT', 7, '2026-11-01 01:01:31');
INSERT INTO `attendance` (`attendance_id`, `student_id`, `course_id`, `session_date`, `session_type`, `session_number`, `hours`, `status`, `recorded_by`, `recorded_at`) VALUES (126, 29, 3, '2026-09-14', 'PRACTICAL', 7, 2, 'ABSENT', 7, '2026-11-01 01:01:31');
INSERT INTO `attendance` (`attendance_id`, `student_id`, `course_id`, `session_date`, `session_type`, `session_number`, `hours`, `status`, `recorded_by`, `recorded_at`) VALUES (127, 11, 3, '2026-09-21', 'THEORY', 8, 2, 'PRESENT', 7, '2026-11-01 01:01:31');
INSERT INTO `attendance` (`attendance_id`, `student_id`, `course_id`, `session_date`, `session_type`, `session_number`, `hours`, `status`, `recorded_by`, `recorded_at`) VALUES (128, 11, 3, '2026-09-21', 'PRACTICAL', 8, 2, 'PRESENT', 7, '2026-11-01 01:01:31');
INSERT INTO `attendance` (`attendance_id`, `student_id`, `course_id`, `session_date`, `session_type`, `session_number`, `hours`, `status`, `recorded_by`, `recorded_at`) VALUES (129, 12, 3, '2026-09-21', 'THEORY', 8, 2, 'PRESENT', 7, '2026-11-01 01:01:31');
INSERT INTO `attendance` (`attendance_id`, `student_id`, `course_id`, `session_date`, `session_type`, `session_number`, `hours`, `status`, `recorded_by`, `recorded_at`) VALUES (130, 12, 3, '2026-09-21', 'PRACTICAL', 8, 2, 'PRESENT', 7, '2026-11-01 01:01:31');
INSERT INTO `attendance` (`attendance_id`, `student_id`, `course_id`, `session_date`, `session_type`, `session_number`, `hours`, `status`, `recorded_by`, `recorded_at`) VALUES (131, 13, 3, '2026-09-21', 'THEORY', 8, 2, 'ABSENT', 7, '2026-11-01 01:01:31');
INSERT INTO `attendance` (`attendance_id`, `student_id`, `course_id`, `session_date`, `session_type`, `session_number`, `hours`, `status`, `recorded_by`, `recorded_at`) VALUES (132, 13, 3, '2026-09-21', 'PRACTICAL', 8, 2, 'ABSENT', 7, '2026-11-01 01:01:31');
INSERT INTO `attendance` (`attendance_id`, `student_id`, `course_id`, `session_date`, `session_type`, `session_number`, `hours`, `status`, `recorded_by`, `recorded_at`) VALUES (133, 14, 3, '2026-09-21', 'THEORY', 8, 2, 'PRESENT', 7, '2026-11-01 01:01:31');
INSERT INTO `attendance` (`attendance_id`, `student_id`, `course_id`, `session_date`, `session_type`, `session_number`, `hours`, `status`, `recorded_by`, `recorded_at`) VALUES (134, 14, 3, '2026-09-21', 'PRACTICAL', 8, 2, 'PRESENT', 7, '2026-11-01 01:01:31');
INSERT INTO `attendance` (`attendance_id`, `student_id`, `course_id`, `session_date`, `session_type`, `session_number`, `hours`, `status`, `recorded_by`, `recorded_at`) VALUES (135, 15, 3, '2026-09-21', 'THEORY', 8, 2, 'PRESENT', 7, '2026-11-01 01:01:31');
INSERT INTO `attendance` (`attendance_id`, `student_id`, `course_id`, `session_date`, `session_type`, `session_number`, `hours`, `status`, `recorded_by`, `recorded_at`) VALUES (136, 15, 3, '2026-09-21', 'PRACTICAL', 8, 2, 'PRESENT', 7, '2026-11-01 01:01:31');
INSERT INTO `attendance` (`attendance_id`, `student_id`, `course_id`, `session_date`, `session_type`, `session_number`, `hours`, `status`, `recorded_by`, `recorded_at`) VALUES (137, 16, 3, '2026-09-21', 'THEORY', 8, 2, 'ABSENT', 7, '2026-11-01 01:01:31');
INSERT INTO `attendance` (`attendance_id`, `student_id`, `course_id`, `session_date`, `session_type`, `session_number`, `hours`, `status`, `recorded_by`, `recorded_at`) VALUES (138, 16, 3, '2026-09-21', 'PRACTICAL', 8, 2, 'ABSENT', 7, '2026-11-01 01:01:31');
INSERT INTO `attendance` (`attendance_id`, `student_id`, `course_id`, `session_date`, `session_type`, `session_number`, `hours`, `status`, `recorded_by`, `recorded_at`) VALUES (139, 17, 3, '2026-09-21', 'THEORY', 8, 2, 'PRESENT', 7, '2026-11-01 01:01:31');
INSERT INTO `attendance` (`attendance_id`, `student_id`, `course_id`, `session_date`, `session_type`, `session_number`, `hours`, `status`, `recorded_by`, `recorded_at`) VALUES (140, 17, 3, '2026-09-21', 'PRACTICAL', 8, 2, 'PRESENT', 7, '2026-11-01 01:01:31');
INSERT INTO `attendance` (`attendance_id`, `student_id`, `course_id`, `session_date`, `session_type`, `session_number`, `hours`, `status`, `recorded_by`, `recorded_at`) VALUES (141, 18, 3, '2026-09-21', 'THEORY', 8, 2, 'PRESENT', 7, '2026-11-01 01:01:31');
INSERT INTO `attendance` (`attendance_id`, `student_id`, `course_id`, `session_date`, `session_type`, `session_number`, `hours`, `status`, `recorded_by`, `recorded_at`) VALUES (142, 18, 3, '2026-09-21', 'PRACTICAL', 8, 2, 'PRESENT', 7, '2026-11-01 01:01:31');
INSERT INTO `attendance` (`attendance_id`, `student_id`, `course_id`, `session_date`, `session_type`, `session_number`, `hours`, `status`, `recorded_by`, `recorded_at`) VALUES (143, 29, 3, '2026-09-21', 'THEORY', 8, 2, 'PRESENT', 7, '2026-11-01 01:01:31');
INSERT INTO `attendance` (`attendance_id`, `student_id`, `course_id`, `session_date`, `session_type`, `session_number`, `hours`, `status`, `recorded_by`, `recorded_at`) VALUES (144, 29, 3, '2026-09-21', 'PRACTICAL', 8, 2, 'PRESENT', 7, '2026-11-01 01:01:31');
INSERT INTO `attendance` (`attendance_id`, `student_id`, `course_id`, `session_date`, `session_type`, `session_number`, `hours`, `status`, `recorded_by`, `recorded_at`) VALUES (145, 11, 3, '2026-09-28', 'THEORY', 9, 2, 'PRESENT', 7, '2026-11-01 01:01:31');
INSERT INTO `attendance` (`attendance_id`, `student_id`, `course_id`, `session_date`, `session_type`, `session_number`, `hours`, `status`, `recorded_by`, `recorded_at`) VALUES (146, 11, 3, '2026-09-28', 'PRACTICAL', 9, 2, 'PRESENT', 7, '2026-11-01 01:01:31');
INSERT INTO `attendance` (`attendance_id`, `student_id`, `course_id`, `session_date`, `session_type`, `session_number`, `hours`, `status`, `recorded_by`, `recorded_at`) VALUES (147, 12, 3, '2026-09-28', 'THEORY', 9, 2, 'PRESENT', 7, '2026-11-01 01:01:31');
INSERT INTO `attendance` (`attendance_id`, `student_id`, `course_id`, `session_date`, `session_type`, `session_number`, `hours`, `status`, `recorded_by`, `recorded_at`) VALUES (148, 12, 3, '2026-09-28', 'PRACTICAL', 9, 2, 'PRESENT', 7, '2026-11-01 01:01:31');
INSERT INTO `attendance` (`attendance_id`, `student_id`, `course_id`, `session_date`, `session_type`, `session_number`, `hours`, `status`, `recorded_by`, `recorded_at`) VALUES (149, 13, 3, '2026-09-28', 'THEORY', 9, 2, 'PRESENT', 7, '2026-11-01 01:01:31');
INSERT INTO `attendance` (`attendance_id`, `student_id`, `course_id`, `session_date`, `session_type`, `session_number`, `hours`, `status`, `recorded_by`, `recorded_at`) VALUES (150, 13, 3, '2026-09-28', 'PRACTICAL', 9, 2, 'PRESENT', 7, '2026-11-01 01:01:31');
INSERT INTO `attendance` (`attendance_id`, `student_id`, `course_id`, `session_date`, `session_type`, `session_number`, `hours`, `status`, `recorded_by`, `recorded_at`) VALUES (151, 14, 3, '2026-09-28', 'THEORY', 9, 2, 'PRESENT', 7, '2026-11-01 01:01:31');
INSERT INTO `attendance` (`attendance_id`, `student_id`, `course_id`, `session_date`, `session_type`, `session_number`, `hours`, `status`, `recorded_by`, `recorded_at`) VALUES (152, 14, 3, '2026-09-28', 'PRACTICAL', 9, 2, 'PRESENT', 7, '2026-11-01 01:01:31');
INSERT INTO `attendance` (`attendance_id`, `student_id`, `course_id`, `session_date`, `session_type`, `session_number`, `hours`, `status`, `recorded_by`, `recorded_at`) VALUES (153, 15, 3, '2026-09-28', 'THEORY', 9, 2, 'PRESENT', 7, '2026-11-01 01:01:31');
INSERT INTO `attendance` (`attendance_id`, `student_id`, `course_id`, `session_date`, `session_type`, `session_number`, `hours`, `status`, `recorded_by`, `recorded_at`) VALUES (154, 15, 3, '2026-09-28', 'PRACTICAL', 9, 2, 'PRESENT', 7, '2026-11-01 01:01:31');
INSERT INTO `attendance` (`attendance_id`, `student_id`, `course_id`, `session_date`, `session_type`, `session_number`, `hours`, `status`, `recorded_by`, `recorded_at`) VALUES (155, 16, 3, '2026-09-28', 'THEORY', 9, 2, 'ABSENT', 7, '2026-11-01 01:01:31');
INSERT INTO `attendance` (`attendance_id`, `student_id`, `course_id`, `session_date`, `session_type`, `session_number`, `hours`, `status`, `recorded_by`, `recorded_at`) VALUES (156, 16, 3, '2026-09-28', 'PRACTICAL', 9, 2, 'ABSENT', 7, '2026-11-01 01:01:31');
INSERT INTO `attendance` (`attendance_id`, `student_id`, `course_id`, `session_date`, `session_type`, `session_number`, `hours`, `status`, `recorded_by`, `recorded_at`) VALUES (157, 17, 3, '2026-09-28', 'THEORY', 9, 2, 'ABSENT', 7, '2026-11-01 01:01:31');
INSERT INTO `attendance` (`attendance_id`, `student_id`, `course_id`, `session_date`, `session_type`, `session_number`, `hours`, `status`, `recorded_by`, `recorded_at`) VALUES (158, 17, 3, '2026-09-28', 'PRACTICAL', 9, 2, 'ABSENT', 7, '2026-11-01 01:01:31');
INSERT INTO `attendance` (`attendance_id`, `student_id`, `course_id`, `session_date`, `session_type`, `session_number`, `hours`, `status`, `recorded_by`, `recorded_at`) VALUES (159, 18, 3, '2026-09-28', 'THEORY', 9, 2, 'PRESENT', 7, '2026-11-01 01:01:31');
INSERT INTO `attendance` (`attendance_id`, `student_id`, `course_id`, `session_date`, `session_type`, `session_number`, `hours`, `status`, `recorded_by`, `recorded_at`) VALUES (160, 18, 3, '2026-09-28', 'PRACTICAL', 9, 2, 'PRESENT', 7, '2026-11-01 01:01:31');
INSERT INTO `attendance` (`attendance_id`, `student_id`, `course_id`, `session_date`, `session_type`, `session_number`, `hours`, `status`, `recorded_by`, `recorded_at`) VALUES (161, 29, 3, '2026-09-28', 'THEORY', 9, 2, 'PRESENT', 7, '2026-11-01 01:01:31');
INSERT INTO `attendance` (`attendance_id`, `student_id`, `course_id`, `session_date`, `session_type`, `session_number`, `hours`, `status`, `recorded_by`, `recorded_at`) VALUES (162, 29, 3, '2026-09-28', 'PRACTICAL', 9, 2, 'PRESENT', 7, '2026-11-01 01:01:31');
INSERT INTO `attendance` (`attendance_id`, `student_id`, `course_id`, `session_date`, `session_type`, `session_number`, `hours`, `status`, `recorded_by`, `recorded_at`) VALUES (163, 11, 3, '2026-10-05', 'THEORY', 10, 2, 'PRESENT', 7, '2026-11-01 01:01:31');
INSERT INTO `attendance` (`attendance_id`, `student_id`, `course_id`, `session_date`, `session_type`, `session_number`, `hours`, `status`, `recorded_by`, `recorded_at`) VALUES (164, 11, 3, '2026-10-05', 'PRACTICAL', 10, 2, 'PRESENT', 7, '2026-11-01 01:01:31');
INSERT INTO `attendance` (`attendance_id`, `student_id`, `course_id`, `session_date`, `session_type`, `session_number`, `hours`, `status`, `recorded_by`, `recorded_at`) VALUES (165, 12, 3, '2026-10-05', 'THEORY', 10, 2, 'PRESENT', 7, '2026-11-01 01:01:31');
INSERT INTO `attendance` (`attendance_id`, `student_id`, `course_id`, `session_date`, `session_type`, `session_number`, `hours`, `status`, `recorded_by`, `recorded_at`) VALUES (166, 12, 3, '2026-10-05', 'PRACTICAL', 10, 2, 'PRESENT', 7, '2026-11-01 01:01:31');
INSERT INTO `attendance` (`attendance_id`, `student_id`, `course_id`, `session_date`, `session_type`, `session_number`, `hours`, `status`, `recorded_by`, `recorded_at`) VALUES (167, 13, 3, '2026-10-05', 'THEORY', 10, 2, 'PRESENT', 7, '2026-11-01 01:01:31');
INSERT INTO `attendance` (`attendance_id`, `student_id`, `course_id`, `session_date`, `session_type`, `session_number`, `hours`, `status`, `recorded_by`, `recorded_at`) VALUES (168, 13, 3, '2026-10-05', 'PRACTICAL', 10, 2, 'PRESENT', 7, '2026-11-01 01:01:31');
INSERT INTO `attendance` (`attendance_id`, `student_id`, `course_id`, `session_date`, `session_type`, `session_number`, `hours`, `status`, `recorded_by`, `recorded_at`) VALUES (169, 14, 3, '2026-10-05', 'THEORY', 10, 2, 'PRESENT', 7, '2026-11-01 01:01:31');
INSERT INTO `attendance` (`attendance_id`, `student_id`, `course_id`, `session_date`, `session_type`, `session_number`, `hours`, `status`, `recorded_by`, `recorded_at`) VALUES (170, 14, 3, '2026-10-05', 'PRACTICAL', 10, 2, 'PRESENT', 7, '2026-11-01 01:01:31');
INSERT INTO `attendance` (`attendance_id`, `student_id`, `course_id`, `session_date`, `session_type`, `session_number`, `hours`, `status`, `recorded_by`, `recorded_at`) VALUES (171, 15, 3, '2026-10-05', 'THEORY', 10, 2, 'ABSENT', 7, '2026-11-01 01:01:31');
INSERT INTO `attendance` (`attendance_id`, `student_id`, `course_id`, `session_date`, `session_type`, `session_number`, `hours`, `status`, `recorded_by`, `recorded_at`) VALUES (172, 15, 3, '2026-10-05', 'PRACTICAL', 10, 2, 'ABSENT', 7, '2026-11-01 01:01:31');
INSERT INTO `attendance` (`attendance_id`, `student_id`, `course_id`, `session_date`, `session_type`, `session_number`, `hours`, `status`, `recorded_by`, `recorded_at`) VALUES (173, 16, 3, '2026-10-05', 'THEORY', 10, 2, 'ABSENT', 7, '2026-11-01 01:01:31');
INSERT INTO `attendance` (`attendance_id`, `student_id`, `course_id`, `session_date`, `session_type`, `session_number`, `hours`, `status`, `recorded_by`, `recorded_at`) VALUES (174, 16, 3, '2026-10-05', 'PRACTICAL', 10, 2, 'ABSENT', 7, '2026-11-01 01:01:31');
INSERT INTO `attendance` (`attendance_id`, `student_id`, `course_id`, `session_date`, `session_type`, `session_number`, `hours`, `status`, `recorded_by`, `recorded_at`) VALUES (175, 17, 3, '2026-10-05', 'THEORY', 10, 2, 'ABSENT', 7, '2026-11-01 01:01:31');
INSERT INTO `attendance` (`attendance_id`, `student_id`, `course_id`, `session_date`, `session_type`, `session_number`, `hours`, `status`, `recorded_by`, `recorded_at`) VALUES (176, 17, 3, '2026-10-05', 'PRACTICAL', 10, 2, 'ABSENT', 7, '2026-11-01 01:01:31');
INSERT INTO `attendance` (`attendance_id`, `student_id`, `course_id`, `session_date`, `session_type`, `session_number`, `hours`, `status`, `recorded_by`, `recorded_at`) VALUES (177, 18, 3, '2026-10-05', 'THEORY', 10, 2, 'PRESENT', 7, '2026-11-01 01:01:31');
INSERT INTO `attendance` (`attendance_id`, `student_id`, `course_id`, `session_date`, `session_type`, `session_number`, `hours`, `status`, `recorded_by`, `recorded_at`) VALUES (178, 18, 3, '2026-10-05', 'PRACTICAL', 10, 2, 'PRESENT', 7, '2026-11-01 01:01:31');
INSERT INTO `attendance` (`attendance_id`, `student_id`, `course_id`, `session_date`, `session_type`, `session_number`, `hours`, `status`, `recorded_by`, `recorded_at`) VALUES (179, 29, 3, '2026-10-05', 'THEORY', 10, 2, 'PRESENT', 7, '2026-11-01 01:01:31');
INSERT INTO `attendance` (`attendance_id`, `student_id`, `course_id`, `session_date`, `session_type`, `session_number`, `hours`, `status`, `recorded_by`, `recorded_at`) VALUES (180, 29, 3, '2026-10-05', 'PRACTICAL', 10, 2, 'PRESENT', 7, '2026-11-01 01:01:31');
INSERT INTO `attendance` (`attendance_id`, `student_id`, `course_id`, `session_date`, `session_type`, `session_number`, `hours`, `status`, `recorded_by`, `recorded_at`) VALUES (181, 11, 3, '2026-10-12', 'THEORY', 11, 2, 'PRESENT', 7, '2026-11-01 01:01:31');
INSERT INTO `attendance` (`attendance_id`, `student_id`, `course_id`, `session_date`, `session_type`, `session_number`, `hours`, `status`, `recorded_by`, `recorded_at`) VALUES (182, 11, 3, '2026-10-12', 'PRACTICAL', 11, 2, 'PRESENT', 7, '2026-11-01 01:01:31');
INSERT INTO `attendance` (`attendance_id`, `student_id`, `course_id`, `session_date`, `session_type`, `session_number`, `hours`, `status`, `recorded_by`, `recorded_at`) VALUES (183, 12, 3, '2026-10-12', 'THEORY', 11, 2, 'PRESENT', 7, '2026-11-01 01:01:31');
INSERT INTO `attendance` (`attendance_id`, `student_id`, `course_id`, `session_date`, `session_type`, `session_number`, `hours`, `status`, `recorded_by`, `recorded_at`) VALUES (184, 12, 3, '2026-10-12', 'PRACTICAL', 11, 2, 'PRESENT', 7, '2026-11-01 01:01:31');
INSERT INTO `attendance` (`attendance_id`, `student_id`, `course_id`, `session_date`, `session_type`, `session_number`, `hours`, `status`, `recorded_by`, `recorded_at`) VALUES (185, 13, 3, '2026-10-12', 'THEORY', 11, 2, 'PRESENT', 7, '2026-11-01 01:01:31');
INSERT INTO `attendance` (`attendance_id`, `student_id`, `course_id`, `session_date`, `session_type`, `session_number`, `hours`, `status`, `recorded_by`, `recorded_at`) VALUES (186, 13, 3, '2026-10-12', 'PRACTICAL', 11, 2, 'PRESENT', 7, '2026-11-01 01:01:31');
INSERT INTO `attendance` (`attendance_id`, `student_id`, `course_id`, `session_date`, `session_type`, `session_number`, `hours`, `status`, `recorded_by`, `recorded_at`) VALUES (187, 14, 3, '2026-10-12', 'THEORY', 11, 2, 'PRESENT', 7, '2026-11-01 01:01:31');
INSERT INTO `attendance` (`attendance_id`, `student_id`, `course_id`, `session_date`, `session_type`, `session_number`, `hours`, `status`, `recorded_by`, `recorded_at`) VALUES (188, 14, 3, '2026-10-12', 'PRACTICAL', 11, 2, 'PRESENT', 7, '2026-11-01 01:01:31');
INSERT INTO `attendance` (`attendance_id`, `student_id`, `course_id`, `session_date`, `session_type`, `session_number`, `hours`, `status`, `recorded_by`, `recorded_at`) VALUES (189, 15, 3, '2026-10-12', 'THEORY', 11, 2, 'ABSENT', 7, '2026-11-01 01:01:31');
INSERT INTO `attendance` (`attendance_id`, `student_id`, `course_id`, `session_date`, `session_type`, `session_number`, `hours`, `status`, `recorded_by`, `recorded_at`) VALUES (190, 15, 3, '2026-10-12', 'PRACTICAL', 11, 2, 'ABSENT', 7, '2026-11-01 01:01:31');
INSERT INTO `attendance` (`attendance_id`, `student_id`, `course_id`, `session_date`, `session_type`, `session_number`, `hours`, `status`, `recorded_by`, `recorded_at`) VALUES (191, 16, 3, '2026-10-12', 'THEORY', 11, 2, 'ABSENT', 7, '2026-11-01 01:01:31');
INSERT INTO `attendance` (`attendance_id`, `student_id`, `course_id`, `session_date`, `session_type`, `session_number`, `hours`, `status`, `recorded_by`, `recorded_at`) VALUES (192, 16, 3, '2026-10-12', 'PRACTICAL', 11, 2, 'ABSENT', 7, '2026-11-01 01:01:31');
INSERT INTO `attendance` (`attendance_id`, `student_id`, `course_id`, `session_date`, `session_type`, `session_number`, `hours`, `status`, `recorded_by`, `recorded_at`) VALUES (193, 17, 3, '2026-10-12', 'THEORY', 11, 2, 'ABSENT', 7, '2026-11-01 01:01:31');
INSERT INTO `attendance` (`attendance_id`, `student_id`, `course_id`, `session_date`, `session_type`, `session_number`, `hours`, `status`, `recorded_by`, `recorded_at`) VALUES (194, 17, 3, '2026-10-12', 'PRACTICAL', 11, 2, 'ABSENT', 7, '2026-11-01 01:01:31');
INSERT INTO `attendance` (`attendance_id`, `student_id`, `course_id`, `session_date`, `session_type`, `session_number`, `hours`, `status`, `recorded_by`, `recorded_at`) VALUES (195, 18, 3, '2026-10-12', 'THEORY', 11, 2, 'PRESENT', 7, '2026-11-01 01:01:31');
INSERT INTO `attendance` (`attendance_id`, `student_id`, `course_id`, `session_date`, `session_type`, `session_number`, `hours`, `status`, `recorded_by`, `recorded_at`) VALUES (196, 18, 3, '2026-10-12', 'PRACTICAL', 11, 2, 'PRESENT', 7, '2026-11-01 01:01:31');
INSERT INTO `attendance` (`attendance_id`, `student_id`, `course_id`, `session_date`, `session_type`, `session_number`, `hours`, `status`, `recorded_by`, `recorded_at`) VALUES (197, 29, 3, '2026-10-12', 'THEORY', 11, 2, 'PRESENT', 7, '2026-11-01 01:01:31');
INSERT INTO `attendance` (`attendance_id`, `student_id`, `course_id`, `session_date`, `session_type`, `session_number`, `hours`, `status`, `recorded_by`, `recorded_at`) VALUES (198, 29, 3, '2026-10-12', 'PRACTICAL', 11, 2, 'PRESENT', 7, '2026-11-01 01:01:31');
INSERT INTO `attendance` (`attendance_id`, `student_id`, `course_id`, `session_date`, `session_type`, `session_number`, `hours`, `status`, `recorded_by`, `recorded_at`) VALUES (199, 11, 3, '2026-10-19', 'THEORY', 12, 2, 'PRESENT', 7, '2026-11-01 01:01:31');
INSERT INTO `attendance` (`attendance_id`, `student_id`, `course_id`, `session_date`, `session_type`, `session_number`, `hours`, `status`, `recorded_by`, `recorded_at`) VALUES (200, 11, 3, '2026-10-19', 'PRACTICAL', 12, 2, 'PRESENT', 7, '2026-11-01 01:01:31');
INSERT INTO `attendance` (`attendance_id`, `student_id`, `course_id`, `session_date`, `session_type`, `session_number`, `hours`, `status`, `recorded_by`, `recorded_at`) VALUES (201, 12, 3, '2026-10-19', 'THEORY', 12, 2, 'PRESENT', 7, '2026-11-01 01:01:31');
INSERT INTO `attendance` (`attendance_id`, `student_id`, `course_id`, `session_date`, `session_type`, `session_number`, `hours`, `status`, `recorded_by`, `recorded_at`) VALUES (202, 12, 3, '2026-10-19', 'PRACTICAL', 12, 2, 'PRESENT', 7, '2026-11-01 01:01:31');
INSERT INTO `attendance` (`attendance_id`, `student_id`, `course_id`, `session_date`, `session_type`, `session_number`, `hours`, `status`, `recorded_by`, `recorded_at`) VALUES (203, 13, 3, '2026-10-19', 'THEORY', 12, 2, 'PRESENT', 7, '2026-11-01 01:01:31');
INSERT INTO `attendance` (`attendance_id`, `student_id`, `course_id`, `session_date`, `session_type`, `session_number`, `hours`, `status`, `recorded_by`, `recorded_at`) VALUES (204, 13, 3, '2026-10-19', 'PRACTICAL', 12, 2, 'PRESENT', 7, '2026-11-01 01:01:31');
INSERT INTO `attendance` (`attendance_id`, `student_id`, `course_id`, `session_date`, `session_type`, `session_number`, `hours`, `status`, `recorded_by`, `recorded_at`) VALUES (205, 14, 3, '2026-10-19', 'THEORY', 12, 2, 'PRESENT', 7, '2026-11-01 01:01:31');
INSERT INTO `attendance` (`attendance_id`, `student_id`, `course_id`, `session_date`, `session_type`, `session_number`, `hours`, `status`, `recorded_by`, `recorded_at`) VALUES (206, 14, 3, '2026-10-19', 'PRACTICAL', 12, 2, 'PRESENT', 7, '2026-11-01 01:01:31');
INSERT INTO `attendance` (`attendance_id`, `student_id`, `course_id`, `session_date`, `session_type`, `session_number`, `hours`, `status`, `recorded_by`, `recorded_at`) VALUES (207, 15, 3, '2026-10-19', 'THEORY', 12, 2, 'ABSENT', 7, '2026-11-01 01:01:31');
INSERT INTO `attendance` (`attendance_id`, `student_id`, `course_id`, `session_date`, `session_type`, `session_number`, `hours`, `status`, `recorded_by`, `recorded_at`) VALUES (208, 15, 3, '2026-10-19', 'PRACTICAL', 12, 2, 'ABSENT', 7, '2026-11-01 01:01:31');
INSERT INTO `attendance` (`attendance_id`, `student_id`, `course_id`, `session_date`, `session_type`, `session_number`, `hours`, `status`, `recorded_by`, `recorded_at`) VALUES (209, 16, 3, '2026-10-19', 'THEORY', 12, 2, 'PRESENT', 7, '2026-11-01 01:01:31');
INSERT INTO `attendance` (`attendance_id`, `student_id`, `course_id`, `session_date`, `session_type`, `session_number`, `hours`, `status`, `recorded_by`, `recorded_at`) VALUES (210, 16, 3, '2026-10-19', 'PRACTICAL', 12, 2, 'PRESENT', 7, '2026-11-01 01:01:31');
INSERT INTO `attendance` (`attendance_id`, `student_id`, `course_id`, `session_date`, `session_type`, `session_number`, `hours`, `status`, `recorded_by`, `recorded_at`) VALUES (211, 17, 3, '2026-10-19', 'THEORY', 12, 2, 'ABSENT', 7, '2026-11-01 01:01:31');
INSERT INTO `attendance` (`attendance_id`, `student_id`, `course_id`, `session_date`, `session_type`, `session_number`, `hours`, `status`, `recorded_by`, `recorded_at`) VALUES (212, 17, 3, '2026-10-19', 'PRACTICAL', 12, 2, 'ABSENT', 7, '2026-11-01 01:01:31');
INSERT INTO `attendance` (`attendance_id`, `student_id`, `course_id`, `session_date`, `session_type`, `session_number`, `hours`, `status`, `recorded_by`, `recorded_at`) VALUES (213, 18, 3, '2026-10-19', 'THEORY', 12, 2, 'PRESENT', 7, '2026-11-01 01:01:31');
INSERT INTO `attendance` (`attendance_id`, `student_id`, `course_id`, `session_date`, `session_type`, `session_number`, `hours`, `status`, `recorded_by`, `recorded_at`) VALUES (214, 18, 3, '2026-10-19', 'PRACTICAL', 12, 2, 'PRESENT', 7, '2026-11-01 01:01:31');
INSERT INTO `attendance` (`attendance_id`, `student_id`, `course_id`, `session_date`, `session_type`, `session_number`, `hours`, `status`, `recorded_by`, `recorded_at`) VALUES (215, 29, 3, '2026-10-19', 'THEORY', 12, 2, 'PRESENT', 7, '2026-11-01 01:01:31');
INSERT INTO `attendance` (`attendance_id`, `student_id`, `course_id`, `session_date`, `session_type`, `session_number`, `hours`, `status`, `recorded_by`, `recorded_at`) VALUES (216, 29, 3, '2026-10-19', 'PRACTICAL', 12, 2, 'PRESENT', 7, '2026-11-01 01:01:31');
INSERT INTO `attendance` (`attendance_id`, `student_id`, `course_id`, `session_date`, `session_type`, `session_number`, `hours`, `status`, `recorded_by`, `recorded_at`) VALUES (217, 11, 3, '2026-10-26', 'THEORY', 13, 2, 'PRESENT', 7, '2026-11-01 01:01:31');
INSERT INTO `attendance` (`attendance_id`, `student_id`, `course_id`, `session_date`, `session_type`, `session_number`, `hours`, `status`, `recorded_by`, `recorded_at`) VALUES (218, 11, 3, '2026-10-26', 'PRACTICAL', 13, 2, 'PRESENT', 7, '2026-11-01 01:01:31');
INSERT INTO `attendance` (`attendance_id`, `student_id`, `course_id`, `session_date`, `session_type`, `session_number`, `hours`, `status`, `recorded_by`, `recorded_at`) VALUES (219, 12, 3, '2026-10-26', 'THEORY', 13, 2, 'PRESENT', 7, '2026-11-01 01:01:31');
INSERT INTO `attendance` (`attendance_id`, `student_id`, `course_id`, `session_date`, `session_type`, `session_number`, `hours`, `status`, `recorded_by`, `recorded_at`) VALUES (220, 12, 3, '2026-10-26', 'PRACTICAL', 13, 2, 'PRESENT', 7, '2026-11-01 01:01:31');
INSERT INTO `attendance` (`attendance_id`, `student_id`, `course_id`, `session_date`, `session_type`, `session_number`, `hours`, `status`, `recorded_by`, `recorded_at`) VALUES (221, 13, 3, '2026-10-26', 'THEORY', 13, 2, 'PRESENT', 7, '2026-11-01 01:01:31');
INSERT INTO `attendance` (`attendance_id`, `student_id`, `course_id`, `session_date`, `session_type`, `session_number`, `hours`, `status`, `recorded_by`, `recorded_at`) VALUES (222, 13, 3, '2026-10-26', 'PRACTICAL', 13, 2, 'PRESENT', 7, '2026-11-01 01:01:31');
INSERT INTO `attendance` (`attendance_id`, `student_id`, `course_id`, `session_date`, `session_type`, `session_number`, `hours`, `status`, `recorded_by`, `recorded_at`) VALUES (223, 14, 3, '2026-10-26', 'THEORY', 13, 2, 'ABSENT', 7, '2026-11-01 01:01:31');
INSERT INTO `attendance` (`attendance_id`, `student_id`, `course_id`, `session_date`, `session_type`, `session_number`, `hours`, `status`, `recorded_by`, `recorded_at`) VALUES (224, 14, 3, '2026-10-26', 'PRACTICAL', 13, 2, 'ABSENT', 7, '2026-11-01 01:01:31');
INSERT INTO `attendance` (`attendance_id`, `student_id`, `course_id`, `session_date`, `session_type`, `session_number`, `hours`, `status`, `recorded_by`, `recorded_at`) VALUES (225, 15, 3, '2026-10-26', 'THEORY', 13, 2, 'ABSENT', 7, '2026-11-01 01:01:31');
INSERT INTO `attendance` (`attendance_id`, `student_id`, `course_id`, `session_date`, `session_type`, `session_number`, `hours`, `status`, `recorded_by`, `recorded_at`) VALUES (226, 15, 3, '2026-10-26', 'PRACTICAL', 13, 2, 'ABSENT', 7, '2026-11-01 01:01:31');
INSERT INTO `attendance` (`attendance_id`, `student_id`, `course_id`, `session_date`, `session_type`, `session_number`, `hours`, `status`, `recorded_by`, `recorded_at`) VALUES (227, 16, 3, '2026-10-26', 'THEORY', 13, 2, 'PRESENT', 7, '2026-11-01 01:01:31');
INSERT INTO `attendance` (`attendance_id`, `student_id`, `course_id`, `session_date`, `session_type`, `session_number`, `hours`, `status`, `recorded_by`, `recorded_at`) VALUES (228, 16, 3, '2026-10-26', 'PRACTICAL', 13, 2, 'PRESENT', 7, '2026-11-01 01:01:31');
INSERT INTO `attendance` (`attendance_id`, `student_id`, `course_id`, `session_date`, `session_type`, `session_number`, `hours`, `status`, `recorded_by`, `recorded_at`) VALUES (229, 17, 3, '2026-10-26', 'THEORY', 13, 2, 'ABSENT', 7, '2026-11-01 01:01:31');
INSERT INTO `attendance` (`attendance_id`, `student_id`, `course_id`, `session_date`, `session_type`, `session_number`, `hours`, `status`, `recorded_by`, `recorded_at`) VALUES (230, 17, 3, '2026-10-26', 'PRACTICAL', 13, 2, 'ABSENT', 7, '2026-11-01 01:01:31');
INSERT INTO `attendance` (`attendance_id`, `student_id`, `course_id`, `session_date`, `session_type`, `session_number`, `hours`, `status`, `recorded_by`, `recorded_at`) VALUES (231, 18, 3, '2026-10-26', 'THEORY', 13, 2, 'PRESENT', 7, '2026-11-01 01:01:31');
INSERT INTO `attendance` (`attendance_id`, `student_id`, `course_id`, `session_date`, `session_type`, `session_number`, `hours`, `status`, `recorded_by`, `recorded_at`) VALUES (232, 18, 3, '2026-10-26', 'PRACTICAL', 13, 2, 'PRESENT', 7, '2026-11-01 01:01:31');
INSERT INTO `attendance` (`attendance_id`, `student_id`, `course_id`, `session_date`, `session_type`, `session_number`, `hours`, `status`, `recorded_by`, `recorded_at`) VALUES (233, 29, 3, '2026-10-26', 'THEORY', 13, 2, 'PRESENT', 7, '2026-11-01 01:01:31');
INSERT INTO `attendance` (`attendance_id`, `student_id`, `course_id`, `session_date`, `session_type`, `session_number`, `hours`, `status`, `recorded_by`, `recorded_at`) VALUES (234, 29, 3, '2026-10-26', 'PRACTICAL', 13, 2, 'PRESENT', 7, '2026-11-01 01:01:31');
INSERT INTO `attendance` (`attendance_id`, `student_id`, `course_id`, `session_date`, `session_type`, `session_number`, `hours`, `status`, `recorded_by`, `recorded_at`) VALUES (235, 11, 3, '2026-11-02', 'THEORY', 14, 2, 'PRESENT', 7, '2026-11-01 01:01:31');
INSERT INTO `attendance` (`attendance_id`, `student_id`, `course_id`, `session_date`, `session_type`, `session_number`, `hours`, `status`, `recorded_by`, `recorded_at`) VALUES (236, 11, 3, '2026-11-02', 'PRACTICAL', 14, 2, 'PRESENT', 7, '2026-11-01 01:01:31');
INSERT INTO `attendance` (`attendance_id`, `student_id`, `course_id`, `session_date`, `session_type`, `session_number`, `hours`, `status`, `recorded_by`, `recorded_at`) VALUES (237, 12, 3, '2026-11-02', 'THEORY', 14, 2, 'PRESENT', 7, '2026-11-01 01:01:31');
INSERT INTO `attendance` (`attendance_id`, `student_id`, `course_id`, `session_date`, `session_type`, `session_number`, `hours`, `status`, `recorded_by`, `recorded_at`) VALUES (238, 12, 3, '2026-11-02', 'PRACTICAL', 14, 2, 'PRESENT', 7, '2026-11-01 01:01:31');
INSERT INTO `attendance` (`attendance_id`, `student_id`, `course_id`, `session_date`, `session_type`, `session_number`, `hours`, `status`, `recorded_by`, `recorded_at`) VALUES (239, 13, 3, '2026-11-02', 'THEORY', 14, 2, 'PRESENT', 7, '2026-11-01 01:01:31');
INSERT INTO `attendance` (`attendance_id`, `student_id`, `course_id`, `session_date`, `session_type`, `session_number`, `hours`, `status`, `recorded_by`, `recorded_at`) VALUES (240, 13, 3, '2026-11-02', 'PRACTICAL', 14, 2, 'PRESENT', 7, '2026-11-01 01:01:31');
INSERT INTO `attendance` (`attendance_id`, `student_id`, `course_id`, `session_date`, `session_type`, `session_number`, `hours`, `status`, `recorded_by`, `recorded_at`) VALUES (241, 14, 3, '2026-11-02', 'THEORY', 14, 2, 'ABSENT', 7, '2026-11-01 01:01:31');
INSERT INTO `attendance` (`attendance_id`, `student_id`, `course_id`, `session_date`, `session_type`, `session_number`, `hours`, `status`, `recorded_by`, `recorded_at`) VALUES (242, 14, 3, '2026-11-02', 'PRACTICAL', 14, 2, 'ABSENT', 7, '2026-11-01 01:01:31');
INSERT INTO `attendance` (`attendance_id`, `student_id`, `course_id`, `session_date`, `session_type`, `session_number`, `hours`, `status`, `recorded_by`, `recorded_at`) VALUES (243, 15, 3, '2026-11-02', 'THEORY', 14, 2, 'ABSENT', 7, '2026-11-01 01:01:31');
INSERT INTO `attendance` (`attendance_id`, `student_id`, `course_id`, `session_date`, `session_type`, `session_number`, `hours`, `status`, `recorded_by`, `recorded_at`) VALUES (244, 15, 3, '2026-11-02', 'PRACTICAL', 14, 2, 'ABSENT', 7, '2026-11-01 01:01:31');
INSERT INTO `attendance` (`attendance_id`, `student_id`, `course_id`, `session_date`, `session_type`, `session_number`, `hours`, `status`, `recorded_by`, `recorded_at`) VALUES (245, 16, 3, '2026-11-02', 'THEORY', 14, 2, 'PRESENT', 7, '2026-11-01 01:01:31');
INSERT INTO `attendance` (`attendance_id`, `student_id`, `course_id`, `session_date`, `session_type`, `session_number`, `hours`, `status`, `recorded_by`, `recorded_at`) VALUES (246, 16, 3, '2026-11-02', 'PRACTICAL', 14, 2, 'PRESENT', 7, '2026-11-01 01:01:31');
INSERT INTO `attendance` (`attendance_id`, `student_id`, `course_id`, `session_date`, `session_type`, `session_number`, `hours`, `status`, `recorded_by`, `recorded_at`) VALUES (247, 17, 3, '2026-11-02', 'THEORY', 14, 2, 'ABSENT', 7, '2026-11-01 01:01:31');
INSERT INTO `attendance` (`attendance_id`, `student_id`, `course_id`, `session_date`, `session_type`, `session_number`, `hours`, `status`, `recorded_by`, `recorded_at`) VALUES (248, 17, 3, '2026-11-02', 'PRACTICAL', 14, 2, 'ABSENT', 7, '2026-11-01 01:01:31');
INSERT INTO `attendance` (`attendance_id`, `student_id`, `course_id`, `session_date`, `session_type`, `session_number`, `hours`, `status`, `recorded_by`, `recorded_at`) VALUES (249, 18, 3, '2026-11-02', 'THEORY', 14, 2, 'PRESENT', 7, '2026-11-01 01:01:31');
INSERT INTO `attendance` (`attendance_id`, `student_id`, `course_id`, `session_date`, `session_type`, `session_number`, `hours`, `status`, `recorded_by`, `recorded_at`) VALUES (250, 18, 3, '2026-11-02', 'PRACTICAL', 14, 2, 'PRESENT', 7, '2026-11-01 01:01:31');
INSERT INTO `attendance` (`attendance_id`, `student_id`, `course_id`, `session_date`, `session_type`, `session_number`, `hours`, `status`, `recorded_by`, `recorded_at`) VALUES (251, 29, 3, '2026-11-02', 'THEORY', 14, 2, 'PRESENT', 7, '2026-11-01 01:01:31');
INSERT INTO `attendance` (`attendance_id`, `student_id`, `course_id`, `session_date`, `session_type`, `session_number`, `hours`, `status`, `recorded_by`, `recorded_at`) VALUES (252, 29, 3, '2026-11-02', 'PRACTICAL', 14, 2, 'PRESENT', 7, '2026-11-01 01:01:31');
INSERT INTO `attendance` (`attendance_id`, `student_id`, `course_id`, `session_date`, `session_type`, `session_number`, `hours`, `status`, `recorded_by`, `recorded_at`) VALUES (253, 11, 3, '2026-11-09', 'THEORY', 15, 2, 'PRESENT', 7, '2026-11-01 01:01:31');
INSERT INTO `attendance` (`attendance_id`, `student_id`, `course_id`, `session_date`, `session_type`, `session_number`, `hours`, `status`, `recorded_by`, `recorded_at`) VALUES (254, 11, 3, '2026-11-09', 'PRACTICAL', 15, 2, 'PRESENT', 7, '2026-11-01 01:01:31');
INSERT INTO `attendance` (`attendance_id`, `student_id`, `course_id`, `session_date`, `session_type`, `session_number`, `hours`, `status`, `recorded_by`, `recorded_at`) VALUES (255, 12, 3, '2026-11-09', 'THEORY', 15, 2, 'PRESENT', 7, '2026-11-01 01:01:31');
INSERT INTO `attendance` (`attendance_id`, `student_id`, `course_id`, `session_date`, `session_type`, `session_number`, `hours`, `status`, `recorded_by`, `recorded_at`) VALUES (256, 12, 3, '2026-11-09', 'PRACTICAL', 15, 2, 'PRESENT', 7, '2026-11-01 01:01:31');
INSERT INTO `attendance` (`attendance_id`, `student_id`, `course_id`, `session_date`, `session_type`, `session_number`, `hours`, `status`, `recorded_by`, `recorded_at`) VALUES (257, 13, 3, '2026-11-09', 'THEORY', 15, 2, 'PRESENT', 7, '2026-11-01 01:01:31');
INSERT INTO `attendance` (`attendance_id`, `student_id`, `course_id`, `session_date`, `session_type`, `session_number`, `hours`, `status`, `recorded_by`, `recorded_at`) VALUES (258, 13, 3, '2026-11-09', 'PRACTICAL', 15, 2, 'PRESENT', 7, '2026-11-01 01:01:31');
INSERT INTO `attendance` (`attendance_id`, `student_id`, `course_id`, `session_date`, `session_type`, `session_number`, `hours`, `status`, `recorded_by`, `recorded_at`) VALUES (259, 14, 3, '2026-11-09', 'THEORY', 15, 2, 'ABSENT', 7, '2026-11-01 01:01:31');
INSERT INTO `attendance` (`attendance_id`, `student_id`, `course_id`, `session_date`, `session_type`, `session_number`, `hours`, `status`, `recorded_by`, `recorded_at`) VALUES (260, 14, 3, '2026-11-09', 'PRACTICAL', 15, 2, 'ABSENT', 7, '2026-11-01 01:01:31');
INSERT INTO `attendance` (`attendance_id`, `student_id`, `course_id`, `session_date`, `session_type`, `session_number`, `hours`, `status`, `recorded_by`, `recorded_at`) VALUES (261, 15, 3, '2026-11-09', 'THEORY', 15, 2, 'ABSENT', 7, '2026-11-01 01:01:31');
INSERT INTO `attendance` (`attendance_id`, `student_id`, `course_id`, `session_date`, `session_type`, `session_number`, `hours`, `status`, `recorded_by`, `recorded_at`) VALUES (262, 15, 3, '2026-11-09', 'PRACTICAL', 15, 2, 'ABSENT', 7, '2026-11-01 01:01:31');
INSERT INTO `attendance` (`attendance_id`, `student_id`, `course_id`, `session_date`, `session_type`, `session_number`, `hours`, `status`, `recorded_by`, `recorded_at`) VALUES (263, 16, 3, '2026-11-09', 'THEORY', 15, 2, 'PRESENT', 7, '2026-11-01 01:01:31');
INSERT INTO `attendance` (`attendance_id`, `student_id`, `course_id`, `session_date`, `session_type`, `session_number`, `hours`, `status`, `recorded_by`, `recorded_at`) VALUES (264, 16, 3, '2026-11-09', 'PRACTICAL', 15, 2, 'PRESENT', 7, '2026-11-01 01:01:31');
INSERT INTO `attendance` (`attendance_id`, `student_id`, `course_id`, `session_date`, `session_type`, `session_number`, `hours`, `status`, `recorded_by`, `recorded_at`) VALUES (265, 17, 3, '2026-11-09', 'THEORY', 15, 2, 'ABSENT', 7, '2026-11-01 01:01:31');
INSERT INTO `attendance` (`attendance_id`, `student_id`, `course_id`, `session_date`, `session_type`, `session_number`, `hours`, `status`, `recorded_by`, `recorded_at`) VALUES (266, 17, 3, '2026-11-09', 'PRACTICAL', 15, 2, 'ABSENT', 7, '2026-11-01 01:01:31');
INSERT INTO `attendance` (`attendance_id`, `student_id`, `course_id`, `session_date`, `session_type`, `session_number`, `hours`, `status`, `recorded_by`, `recorded_at`) VALUES (267, 18, 3, '2026-11-09', 'THEORY', 15, 2, 'PRESENT', 7, '2026-11-01 01:01:31');
INSERT INTO `attendance` (`attendance_id`, `student_id`, `course_id`, `session_date`, `session_type`, `session_number`, `hours`, `status`, `recorded_by`, `recorded_at`) VALUES (268, 18, 3, '2026-11-09', 'PRACTICAL', 15, 2, 'PRESENT', 7, '2026-11-01 01:01:31');
INSERT INTO `attendance` (`attendance_id`, `student_id`, `course_id`, `session_date`, `session_type`, `session_number`, `hours`, `status`, `recorded_by`, `recorded_at`) VALUES (269, 29, 3, '2026-11-09', 'THEORY', 15, 2, 'PRESENT', 7, '2026-11-01 01:01:31');
INSERT INTO `attendance` (`attendance_id`, `student_id`, `course_id`, `session_date`, `session_type`, `session_number`, `hours`, `status`, `recorded_by`, `recorded_at`) VALUES (270, 29, 3, '2026-11-09', 'PRACTICAL', 15, 2, 'PRESENT', 7, '2026-11-01 01:01:31');
-- (270 records)

-- --------------------------------------------------------
-- Data for table `medical`
-- --------------------------------------------------------
INSERT INTO `medical` (`medical_id`, `student_id`, `course_id`, `session_type`, `session_date`, `submission_date`, `reason`, `medical_doc_path`, `status`, `reviewed_by`, `reviewed_at`, `remarks`) VALUES (1, 16, 3, 'THEORY', '2026-09-15', '2026-09-17', 'Viral fever - treated at Base Hospital Kamburupitiya', 'medicals/tg2006_med01.pdf', 'APPROVED', 7, '2026-09-18 15:30:00', 'Medical certificate verified by University Medical Officer');
INSERT INTO `medical` (`medical_id`, `student_id`, `course_id`, `session_type`, `session_date`, `submission_date`, `reason`, `medical_doc_path`, `status`, `reviewed_by`, `reviewed_at`, `remarks`) VALUES (2, 16, 3, 'THEORY', '2026-09-22', '2026-09-24', 'Viral fever rest recommended by doctor', 'medicals/tg2006_med02.pdf', 'APPROVED', 7, '2026-09-25 17:00:00', 'Approved by UMO');
INSERT INTO `medical` (`medical_id`, `student_id`, `course_id`, `session_type`, `session_date`, `submission_date`, `reason`, `medical_doc_path`, `status`, `reviewed_by`, `reviewed_at`, `remarks`) VALUES (3, 16, 3, 'PRACTICAL', '2026-09-23', '2026-09-25', 'Doctor recommended recovery rest', 'medicals/tg2006_med03.pdf', 'APPROVED', 7, '2026-09-26 14:45:00', 'Approved by UMO');
INSERT INTO `medical` (`medical_id`, `student_id`, `course_id`, `session_type`, `session_date`, `submission_date`, `reason`, `medical_doc_path`, `status`, `reviewed_by`, `reviewed_at`, `remarks`) VALUES (4, 17, 3, 'THEORY', '2026-09-08', '2026-09-10', 'Dengue fever admission', 'medicals/tg2007_med01.pdf', 'APPROVED', 7, '2026-09-12 19:30:00', 'Approved by UMO');
INSERT INTO `medical` (`medical_id`, `student_id`, `course_id`, `session_type`, `session_date`, `submission_date`, `reason`, `medical_doc_path`, `status`, `reviewed_by`, `reviewed_at`, `remarks`) VALUES (5, 17, 3, 'PRACTICAL', '2026-09-09', '2026-09-11', 'Hospitalization for Dengue', 'medicals/tg2007_med02.pdf', 'APPROVED', 7, '2026-09-12 19:40:00', 'Approved by UMO');
INSERT INTO `medical` (`medical_id`, `student_id`, `course_id`, `session_type`, `session_date`, `submission_date`, `reason`, `medical_doc_path`, `status`, `reviewed_by`, `reviewed_at`, `remarks`) VALUES (6, 15, 3, 'THEORY', '2026-09-29', '2026-10-05', 'Headache - submitted after 14-day university deadline', 'medicals/tg2005_med01.pdf', 'NOT_APPROVED', 7, '2026-10-06 14:30:00', 'Rejected: Submitted after deadline of 7 days from illness');
INSERT INTO `medical` (`medical_id`, `student_id`, `course_id`, `session_type`, `session_date`, `submission_date`, `reason`, `medical_doc_path`, `status`, `reviewed_by`, `reviewed_at`, `remarks`) VALUES (7, 50, 17, 'THEORY', '2026-09-29', '2026-10-02', 'Severe migraine certified by Ruhuna Medical Center', 'medicals/bs50_migraine.pdf', 'APPROVED', 8, '2026-10-04 15:00:00', 'Approved with valid medical certificate');
INSERT INTO `medical` (`medical_id`, `student_id`, `course_id`, `session_type`, `session_date`, `submission_date`, `reason`, `medical_doc_path`, `status`, `reviewed_by`, `reviewed_at`, `remarks`) VALUES (8, 55, 21, 'THEORY', '2026-09-22', '2026-09-25', 'Acute gastroenteritis - treated at Matara General Hospital', 'medicals/et55_gastro.pdf', 'APPROVED', 9, '2026-09-27 16:30:00', 'Official hospital certificate verified');
INSERT INTO `medical` (`medical_id`, `student_id`, `course_id`, `session_type`, `session_date`, `submission_date`, `reason`, `medical_doc_path`, `status`, `reviewed_by`, `reviewed_at`, `remarks`) VALUES (9, 60, 25, 'THEORY', '2026-10-06', '2026-10-08', 'Sports sprain injury during Inter-Faculty Badminton', 'medicals/md60_injury.pdf', 'APPROVED', 7, '2026-10-10 19:30:00', 'Medical leave granted by Physical Education Unit');
INSERT INTO `medical` (`medical_id`, `student_id`, `course_id`, `session_type`, `session_date`, `submission_date`, `reason`, `medical_doc_path`, `status`, `reviewed_by`, `reviewed_at`, `remarks`) VALUES (10, 53, 17, 'PRACTICAL', '2026-10-13', '2026-10-28', 'Asthma exacerbation', 'medicals/bs53_asthma.pdf', 'NOT_APPROVED', 8, '2026-10-29 15:30:00', 'Rejected: Submitted after 14-day regulation deadline');
-- (10 records)

-- --------------------------------------------------------
-- Data for table `course_materials`
-- --------------------------------------------------------
INSERT INTO `course_materials` (`material_id`, `course_id`, `lecturer_id`, `title`, `description`, `file_path`, `uploaded_at`) VALUES (1, 3, 2, 'Lecture 01 - Introduction to OOP Concepts', 'Fundamental principles of OOP: Abstraction, Encapsulation, Inheritance, Polymorphism in Java', 'materials/ict2132_lec01_oop_basics.pdf', '2026-09-01 14:30:00');
INSERT INTO `course_materials` (`material_id`, `course_id`, `lecturer_id`, `title`, `description`, `file_path`, `uploaded_at`) VALUES (2, 3, 2, 'Lecture 02 - Java Swing GUI Development', 'Building user interfaces using JFrame, JPanel, LayoutManagers and Listeners', 'materials/ict2132_lec02_swing_gui.pdf', '2026-09-08 16:00:00');
INSERT INTO `course_materials` (`material_id`, `course_id`, `lecturer_id`, `title`, `description`, `file_path`, `uploaded_at`) VALUES (3, 3, 2, 'Lecture 03 - JDBC Architecture & DAO Pattern', 'Connecting Java applications to MySQL database with PreparedStatement and DAO layer', 'materials/ict2132_lec03_jdbc_dao.pdf', '2026-09-15 16:30:00');
INSERT INTO `course_materials` (`material_id`, `course_id`, `lecturer_id`, `title`, `description`, `file_path`, `uploaded_at`) VALUES (4, 3, 2, 'Lab Sheet 01 - Swing Forms & Event Handling', 'Hands-on practice exercises for building responsive desktop forms', 'materials/ict2132_lab01_forms.pdf', '2026-09-16 19:30:00');
INSERT INTO `course_materials` (`material_id`, `course_id`, `lecturer_id`, `title`, `description`, `file_path`, `uploaded_at`) VALUES (5, 1, 3, 'Lecture 01 - Relational Database Concepts & SQL', 'Overview of DBMS, relational algebra, DDL and DML queries', 'materials/ict2113_lec01_relational_sql.pdf', '2026-09-02 14:00:00');
INSERT INTO `course_materials` (`material_id`, `course_id`, `lecturer_id`, `title`, `description`, `file_path`, `uploaded_at`) VALUES (6, 1, 3, 'Lecture 02 - Normalization (1NF, 2NF, 3NF, BCNF)', 'Database design rules to prevent insertion, update and deletion anomalies', 'materials/ict2113_lec02_normalization.pdf', '2026-09-09 15:00:00');
INSERT INTO `course_materials` (`material_id`, `course_id`, `lecturer_id`, `title`, `description`, `file_path`, `uploaded_at`) VALUES (7, 2, 2, 'Lecture 01 - Software Requirements Specification', 'Writing SRS documents, functional & non-functional requirements IEEE 830', 'materials/ict2122_lec01_srs_standards.pdf', '2026-09-03 18:30:00');
INSERT INTO `course_materials` (`material_id`, `course_id`, `lecturer_id`, `title`, `description`, `file_path`, `uploaded_at`) VALUES (8, 3, 34, 'labsheet', 'pactical', 'D:\\campus\\Level 02 Semester 01\\Object Oriented Programming\\LAB\\ICT2132 - OOPP - Practical 15.pdf', '2026-11-01 01:19:31');
INSERT INTO `course_materials` (`material_id`, `course_id`, `lecturer_id`, `title`, `description`, `file_path`, `uploaded_at`) VALUES (11, 3, 34, 'LABBBBB', 'ASD', 'C:\\Users\\msi\\FacultyAcademicManagementSystem\\course-materials\\85dba75c-26f7-495d-a83b-ce77cd4e504d_Activity1-_lec1.docx', '2026-11-03 05:07:14');
INSERT INTO `course_materials` (`material_id`, `course_id`, `lecturer_id`, `title`, `description`, `file_path`, `uploaded_at`) VALUES (14, 4, 34, 'ASDF', 'ASDF', 'C:\\Users\\msi\\FacultyAcademicManagementSystem\\course-materials\\149643c9-7622-4163-adcb-0db26cc46549_Screenshot_2025-09-13_220522.png', '2026-11-03 05:09:06');
-- (10 records)

-- --------------------------------------------------------
-- Data for table `notices`
-- --------------------------------------------------------
INSERT INTO `notices` (`notice_id`, `title`, `content`, `published_date`, `created_by`, `target_role`, `status`, `created_at`) VALUES (1, 'Commencement of Semester I Academic Activities - 2024/2025', 'Academic activities for Level II Semester I of the Faculty of Technology will commence on Monday, 3rd August 2026. All students must register for their elective modules via the academic portal within the first two weeks.', '2026-08-01', 1, 'ALL', 'ACTIVE', '2026-11-01 01:01:31');
INSERT INTO `notices` (`notice_id`, `title`, `content`, `published_date`, `created_by`, `target_role`, `status`, `created_at`) VALUES (2, 'Submission of Medical Certificates for Missed Lectures/Labs', 'Students who were absent for lectures or laboratory practicals due to illness must submit their medical certificates to the Technical Officer / Department office within 7 days of returning, endorsed by the University Medical Officer (UMO). Late submissions will be rejected without consideration.', '2026-08-10', 1, 'ALL', 'ACTIVE', '2026-11-01 01:01:31');
INSERT INTO `notices` (`notice_id`, `title`, `content`, `published_date`, `created_by`, `target_role`, `status`, `created_at`) VALUES (3, 'Continuous Assessment (CA) Marks Verification Notice', 'Continuous Assessment (CA) marks for ICT2132 and ICT2113 have been published by the respective course coordinators. All undergraduates are requested to verify their marks before 25th October 2026. Queries must be addressed to the course coordinator directly.', '2026-10-01', 1, 'ALL', 'ACTIVE', '2026-11-01 01:01:31');
INSERT INTO `notices` (`notice_id`, `title`, `content`, `published_date`, `created_by`, `target_role`, `status`, `created_at`) VALUES (4, 'Semester I Final Examination Timetable Release', 'The final examination timetable for the 2024/2025 academic year will be released by the Dean\'s Office on 20th October 2026. Please check the timetable tab for room allocations and session timings. Students with less than 80% attendance or CA < 40% are barred from sitting the exam.', '2026-10-05', 1, 'ALL', 'ACTIVE', '2026-11-01 01:01:31');
INSERT INTO `notices` (`notice_id`, `title`, `content`, `published_date`, `created_by`, `target_role`, `status`, `created_at`) VALUES (5, 'Lab Equipment Maintenance & Lab Access Hours', 'Computer Lab 01 and Network Systems Lab will undergo scheduled server maintenance this Saturday from 8:00 AM to 1:00 PM. Lab users are advised to backup all local project files.', '2026-10-06', 1, 'ALL', 'ACTIVE', '2026-11-01 01:01:31');
-- (5 records)

-- --------------------------------------------------------
-- Data for table `timetables`
-- --------------------------------------------------------
INSERT INTO `timetables` (`timetable_id`, `department_id`, `course_id`, `day_of_week`, `start_time`, `end_time`, `room`, `lecturer_id`, `session_type`, `semester`, `academic_year`) VALUES (1, 1, 3, 'MONDAY', '08:00:00', '10:00:00', 'Lecture Theatre 01', 2, 'THEORY', 1, '2024/2025');
INSERT INTO `timetables` (`timetable_id`, `department_id`, `course_id`, `day_of_week`, `start_time`, `end_time`, `room`, `lecturer_id`, `session_type`, `semester`, `academic_year`) VALUES (2, 1, 3, 'MONDAY', '10:00:00', '12:00:00', 'Computer Lab 01', 2, 'PRACTICAL', 1, '2024/2025');
INSERT INTO `timetables` (`timetable_id`, `department_id`, `course_id`, `day_of_week`, `start_time`, `end_time`, `room`, `lecturer_id`, `session_type`, `semester`, `academic_year`) VALUES (3, 1, 1, 'TUESDAY', '08:00:00', '10:00:00', 'Lecture Theatre 02', 3, 'THEORY', 1, '2024/2025');
INSERT INTO `timetables` (`timetable_id`, `department_id`, `course_id`, `day_of_week`, `start_time`, `end_time`, `room`, `lecturer_id`, `session_type`, `semester`, `academic_year`) VALUES (4, 1, 1, 'TUESDAY', '10:00:00', '12:00:00', 'Computer Lab 02', 3, 'PRACTICAL', 1, '2024/2025');
INSERT INTO `timetables` (`timetable_id`, `department_id`, `course_id`, `day_of_week`, `start_time`, `end_time`, `room`, `lecturer_id`, `session_type`, `semester`, `academic_year`) VALUES (5, 1, 2, 'WEDNESDAY', '13:00:00', '15:00:00', 'Lecture Theatre 01', 2, 'THEORY', 1, '2024/2025');
INSERT INTO `timetables` (`timetable_id`, `department_id`, `course_id`, `day_of_week`, `start_time`, `end_time`, `room`, `lecturer_id`, `session_type`, `semester`, `academic_year`) VALUES (6, 1, 4, 'THURSDAY', '08:00:00', '10:00:00', 'Lecture Theatre 01', 2, 'THEORY', 1, '2024/2025');
INSERT INTO `timetables` (`timetable_id`, `department_id`, `course_id`, `day_of_week`, `start_time`, `end_time`, `room`, `lecturer_id`, `session_type`, `semester`, `academic_year`) VALUES (7, 1, 4, 'THURSDAY', '10:00:00', '12:00:00', 'Network Systems Lab', 2, 'PRACTICAL', 1, '2024/2025');
INSERT INTO `timetables` (`timetable_id`, `department_id`, `course_id`, `day_of_week`, `start_time`, `end_time`, `room`, `lecturer_id`, `session_type`, `semester`, `academic_year`) VALUES (8, 4, 5, 'FRIDAY', '08:00:00', '10:00:00', 'Lecture Hall MDT', 6, 'THEORY', 1, '2024/2025');
INSERT INTO `timetables` (`timetable_id`, `department_id`, `course_id`, `day_of_week`, `start_time`, `end_time`, `room`, `lecturer_id`, `session_type`, `semester`, `academic_year`) VALUES (9, 3, 6, 'FRIDAY', '10:00:00', '12:00:00', 'Engineering Workshop', 5, 'PRACTICAL', 1, '2024/2025');
-- (9 records)

-- ============================================================================
-- INTEGRITY & SEQUENCE CHECKS
-- ============================================================================
SET FOREIGN_KEY_CHECKS = @OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS = @OLD_UNIQUE_CHECKS;
SET SQL_MODE = @OLD_SQL_MODE;
COMMIT;

-- End of consolidated database script
