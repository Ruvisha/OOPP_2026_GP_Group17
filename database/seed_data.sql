-- ====================================================================
-- FOT - AMS Complete Database Seed Script (Idempotent)
-- Faculty of Technology, University of Ruhuna
-- ====================================================================

-- 1. Ensure user_notifications table exists
CREATE TABLE IF NOT EXISTS `user_notifications` (
  `id` INT AUTO_INCREMENT PRIMARY KEY,
  `user_id` INT NOT NULL,
  `notice_id` INT NOT NULL,
  `is_read` TINYINT(1) NOT NULL DEFAULT 0,
  `read_at` TIMESTAMP NULL DEFAULT NULL,
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY `uq_user_notice` (`user_id`, `notice_id`),
  KEY `idx_un_user` (`user_id`),
  KEY `idx_un_notice` (`notice_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2. Insert / Update 30 Demo Users
-- Passwords:
-- Admin: Admin@123 -> e86f78a8a3caf0b60d8e74e5942aa6d86dc150cd3c03338aef25b7d2d7e3acc7
-- Lecturer: Lecturer@123 -> 88d11e52e9ce89aa80722a18a2a9238ff998ec7159945f3219bfc07e2c6e59f8
-- Technical Officer: Tech@1234 -> 57256bf15393e6e33588f2bf71d581a72b0159f15fa8315226df09a1a0cf54a2
-- Undergraduate: Student@123 -> b2a1f4fd0a460606b34c8913e2981dac8d2e283d778aba586c416ee2629bfa54

-- 2.1 Administrator
INSERT INTO `users` (`username`, `password_hash`, `role_id`, `full_name`, `email`, `phone`, `address`, `department_id`, `status`, `student_reg_no`, `academic_year`, `current_semester`, `student_type`, `designation`, `office_room`, `assigned_lab`, `role_specific_id`)
VALUES ('admin', 'e86f78a8a3caf0b60d8e74e5942aa6d86dc150cd3c03338aef25b7d2d7e3acc7', 1, 'Prof. K. L. Jayasinghe (Dean / SAR)', 'admin@fot.ruh.ac.lk', '0771122334', 'Dean''s Office, Faculty of Technology, Karagoda Uyangoda', 1, 'ACTIVE', NULL, NULL, 1, NULL, 'Senior Assistant Registrar', 'ADM-01', NULL, 'A001')
ON DUPLICATE KEY UPDATE `password_hash`=VALUES(`password_hash`), `full_name`=VALUES(`full_name`), `status`='ACTIVE';

-- 2.2 5 Lecturers
INSERT INTO `users` (`username`, `password_hash`, `role_id`, `full_name`, `email`, `phone`, `address`, `department_id`, `status`, `student_reg_no`, `academic_year`, `current_semester`, `student_type`, `designation`, `office_room`, `assigned_lab`, `role_specific_id`) VALUES
('lecturer1', '88d11e52e9ce89aa80722a18a2a9238ff998ec7159945f3219bfc07e2c6e59f8', 2, 'Dr. Kamal Perera', 'lecturer1@fot.ruh.ac.lk', '0772233441', 'Faculty of Technology, University of Ruhuna', 1, 'ACTIVE', NULL, NULL, 1, NULL, 'Senior Lecturer Gr. I', 'ICT-B301', NULL, 'L001'),
('lecturer2', '88d11e52e9ce89aa80722a18a2a9238ff998ec7159945f3219bfc07e2c6e59f8', 2, 'Prof. Sunethra Silva', 'lecturer2@fot.ruh.ac.lk', '0772233442', 'Faculty of Technology, University of Ruhuna', 1, 'ACTIVE', NULL, NULL, 1, NULL, 'Professor in Computer Technology', 'ICT-B302', NULL, 'L002'),
('lecturer3', '88d11e52e9ce89aa80722a18a2a9238ff998ec7159945f3219bfc07e2c6e59f8', 2, 'Dr. Nimal Wickramasinghe', 'lecturer3@fot.ruh.ac.lk', '0772233443', 'Faculty of Technology, University of Ruhuna', 3, 'ACTIVE', NULL, NULL, 1, NULL, 'Senior Lecturer Gr. II', 'ET-A201', NULL, 'L003'),
('lecturer4', '88d11e52e9ce89aa80722a18a2a9238ff998ec7159945f3219bfc07e2c6e59f8', 2, 'Dr. Anoma Jayasuriya', 'lecturer4@fot.ruh.ac.lk', '0772233444', 'Faculty of Technology, University of Ruhuna', 2, 'ACTIVE', NULL, NULL, 1, NULL, 'Senior Lecturer Gr. I', 'BST-C101', NULL, 'L004'),
('lecturer5', '88d11e52e9ce89aa80722a18a2a9238ff998ec7159945f3219bfc07e2c6e59f8', 2, 'Dr. Rohan De Silva', 'lecturer5@fot.ruh.ac.lk', '0772233445', 'Faculty of Technology, University of Ruhuna', 4, 'ACTIVE', NULL, NULL, 1, NULL, 'Senior Lecturer Gr. II', 'MDT-D102', NULL, 'L005')
ON DUPLICATE KEY UPDATE `password_hash`=VALUES(`password_hash`), `full_name`=VALUES(`full_name`), `status`='ACTIVE';

-- 2.3 4 Technical Officers
INSERT INTO `users` (`username`, `password_hash`, `role_id`, `full_name`, `email`, `phone`, `address`, `department_id`, `status`, `student_reg_no`, `academic_year`, `current_semester`, `student_type`, `designation`, `office_room`, `assigned_lab`, `role_specific_id`) VALUES
('to1', '57256bf15393e6e33588f2bf71d581a72b0159f15fa8315226df09a1a0cf54a2', 3, 'Mr. Nuwan Fernando', 'to1@fot.ruh.ac.lk', '0773344551', 'Faculty of Technology, University of Ruhuna', 1, 'ACTIVE', NULL, NULL, 1, NULL, 'Technical Officer Gr. I', NULL, 'Software Engineering & DB Lab', 'T001'),
('to2', '57256bf15393e6e33588f2bf71d581a72b0159f15fa8315226df09a1a0cf54a2', 3, 'Ms. Dilani Samarakoon', 'to2@fot.ruh.ac.lk', '0773344552', 'Faculty of Technology, University of Ruhuna', 1, 'ACTIVE', NULL, NULL, 1, NULL, 'Technical Officer Gr. II', NULL, 'Networking & Hardware Lab', 'T002'),
('to3', '57256bf15393e6e33588f2bf71d581a72b0159f15fa8315226df09a1a0cf54a2', 3, 'Mr. Kasun Rajapaksha', 'to3@fot.ruh.ac.lk', '0773344553', 'Faculty of Technology, University of Ruhuna', 3, 'ACTIVE', NULL, NULL, 1, NULL, 'Senior Technical Officer', NULL, 'Engineering Technology Workshop', 'T003'),
('to4', '57256bf15393e6e33588f2bf71d581a72b0159f15fa8315226df09a1a0cf54a2', 3, 'Ms. Sachini Gamage', 'to4@fot.ruh.ac.lk', '0773344554', 'Faculty of Technology, University of Ruhuna', 2, 'ACTIVE', NULL, NULL, 1, NULL, 'Technical Officer Gr. I', NULL, 'Bio Systems Analytical Lab', 'T004')
ON DUPLICATE KEY UPDATE `password_hash`=VALUES(`password_hash`), `full_name`=VALUES(`full_name`), `status`='ACTIVE';

-- 2.4 20 Undergraduates (14 Regular, 4 Repeat, 2 Batch-Missed)
INSERT INTO `users` (`username`, `password_hash`, `role_id`, `full_name`, `email`, `phone`, `address`, `department_id`, `status`, `student_reg_no`, `academic_year`, `current_semester`, `student_type`, `designation`, `office_room`, `assigned_lab`, `role_specific_id`) VALUES
('TG/2024/0001', 'b2a1f4fd0a460606b34c8913e2981dac8d2e283d778aba586c416ee2629bfa54', 4, 'Kasun Tharaka Bandara', 'tg20240001@fot.ruh.ac.lk', '0714455001', 'Karagoda Uyangoda', 1, 'ACTIVE', 'TG/2024/0001', '2023/2024', 1, 'REGULAR', NULL, NULL, NULL, 'S001'),
('TG/2024/0002', 'b2a1f4fd0a460606b34c8913e2981dac8d2e283d778aba586c416ee2629bfa54', 4, 'Sanduni Oshadi Fernando', 'tg20240002@fot.ruh.ac.lk', '0714455002', 'Karagoda Uyangoda', 1, 'ACTIVE', 'TG/2024/0002', '2023/2024', 1, 'REGULAR', NULL, NULL, NULL, 'S002'),
('TG/2024/0003', 'b2a1f4fd0a460606b34c8913e2981dac8d2e283d778aba586c416ee2629bfa54', 4, 'Chamath Kavinda Perera', 'tg20240003@fot.ruh.ac.lk', '0714455003', 'Karagoda Uyangoda', 1, 'ACTIVE', 'TG/2024/0003', '2023/2024', 1, 'REGULAR', NULL, NULL, NULL, 'S003'),
('TG/2024/0004', 'b2a1f4fd0a460606b34c8913e2981dac8d2e283d778aba586c416ee2629bfa54', 4, 'Nadeesha Dilrukshi Gamage', 'tg20240004@fot.ruh.ac.lk', '0714455004', 'Karagoda Uyangoda', 1, 'ACTIVE', 'TG/2024/0004', '2023/2024', 1, 'REGULAR', NULL, NULL, NULL, 'S004'),
('TG/2024/0005', 'b2a1f4fd0a460606b34c8913e2981dac8d2e283d778aba586c416ee2629bfa54', 4, 'Thisara Madushan Jayawardena', 'tg20240005@fot.ruh.ac.lk', '0714455005', 'Karagoda Uyangoda', 1, 'ACTIVE', 'TG/2024/0005', '2023/2024', 1, 'REGULAR', NULL, NULL, NULL, 'S005'),
('TG/2024/0006', 'b2a1f4fd0a460606b34c8913e2981dac8d2e283d778aba586c416ee2629bfa54', 4, 'Hiruni Rashmika Wickramasinghe', 'tg20240006@fot.ruh.ac.lk', '0714455006', 'Karagoda Uyangoda', 1, 'ACTIVE', 'TG/2024/0006', '2023/2024', 1, 'REGULAR', NULL, NULL, NULL, 'S006'),
('TG/2024/0007', 'b2a1f4fd0a460606b34c8913e2981dac8d2e283d778aba586c416ee2629bfa54', 4, 'Danushka Malith Senanayake', 'tg20240007@fot.ruh.ac.lk', '0714455007', 'Karagoda Uyangoda', 1, 'ACTIVE', 'TG/2024/0007', '2023/2024', 1, 'REGULAR', NULL, NULL, NULL, 'S007'),
('TG/2024/0008', 'b2a1f4fd0a460606b34c8913e2981dac8d2e283d778aba586c416ee2629bfa54', 4, 'Kavindi Samudrika Rajapaksha', 'tg20240008@fot.ruh.ac.lk', '0714455008', 'Karagoda Uyangoda', 1, 'ACTIVE', 'TG/2024/0008', '2023/2024', 1, 'REGULAR', NULL, NULL, NULL, 'S008'),
('TG/2024/0009', 'b2a1f4fd0a460606b34c8913e2981dac8d2e283d778aba586c416ee2629bfa54', 4, 'Supun Lakshan Silva', 'tg20240009@fot.ruh.ac.lk', '0714455009', 'Karagoda Uyangoda', 1, 'ACTIVE', 'TG/2024/0009', '2023/2024', 1, 'REGULAR', NULL, NULL, NULL, 'S009'),
('TG/2024/0010', 'b2a1f4fd0a460606b34c8913e2981dac8d2e283d778aba586c416ee2629bfa54', 4, 'Chathurika Prabhashini Dissanayake', 'tg20240010@fot.ruh.ac.lk', '0714455010', 'Karagoda Uyangoda', 1, 'ACTIVE', 'TG/2024/0010', '2023/2024', 1, 'REGULAR', NULL, NULL, NULL, 'S010'),
('TG/2024/0011', 'b2a1f4fd0a460606b34c8913e2981dac8d2e283d778aba586c416ee2629bfa54', 4, 'Ashan Madhushanka Weerasinghe', 'tg20240011@fot.ruh.ac.lk', '0714455011', 'Karagoda Uyangoda', 1, 'ACTIVE', 'TG/2024/0011', '2023/2024', 1, 'REGULAR', NULL, NULL, NULL, 'S011'),
('TG/2024/0012', 'b2a1f4fd0a460606b34c8913e2981dac8d2e283d778aba586c416ee2629bfa54', 4, 'Sachini Nimanthi Rathnayake', 'tg20240012@fot.ruh.ac.lk', '0714455012', 'Karagoda Uyangoda', 1, 'ACTIVE', 'TG/2024/0012', '2023/2024', 1, 'REGULAR', NULL, NULL, NULL, 'S012'),
('TG/2024/0013', 'b2a1f4fd0a460606b34c8913e2981dac8d2e283d778aba586c416ee2629bfa54', 4, 'Isuru Dhananjaya Pathirana', 'tg20240013@fot.ruh.ac.lk', '0714455013', 'Karagoda Uyangoda', 1, 'ACTIVE', 'TG/2024/0013', '2023/2024', 1, 'REGULAR', NULL, NULL, NULL, 'S013'),
('TG/2024/0014', 'b2a1f4fd0a460606b34c8913e2981dac8d2e283d778aba586c416ee2629bfa54', 4, 'Hansani Kaushalya Gunasekara', 'tg20240014@fot.ruh.ac.lk', '0714455014', 'Karagoda Uyangoda', 1, 'ACTIVE', 'TG/2024/0014', '2023/2024', 1, 'REGULAR', NULL, NULL, NULL, 'S014'),
-- 4 Repeat Students
('TG/2024/0015', 'b2a1f4fd0a460606b34c8913e2981dac8d2e283d778aba586c416ee2629bfa54', 4, 'Lahiru Prabath Karunaratne', 'tg20240015@fot.ruh.ac.lk', '0714455015', 'Karagoda Uyangoda', 1, 'ACTIVE', 'TG/2024/0015', '2023/2024', 1, 'REPEAT', NULL, NULL, NULL, 'S015'),
('TG/2024/0016', 'b2a1f4fd0a460606b34c8913e2981dac8d2e283d778aba586c416ee2629bfa54', 4, 'Maleesha Devindi Alwis', 'tg20240016@fot.ruh.ac.lk', '0714455016', 'Karagoda Uyangoda', 1, 'ACTIVE', 'TG/2024/0016', '2023/2024', 1, 'REPEAT', NULL, NULL, NULL, 'S016'),
('TG/2024/0017', 'b2a1f4fd0a460606b34c8913e2981dac8d2e283d778aba586c416ee2629bfa54', 4, 'Gayan Chanaka Samarasinghe', 'tg20240017@fot.ruh.ac.lk', '0714455017', 'Karagoda Uyangoda', 1, 'ACTIVE', 'TG/2024/0017', '2023/2024', 1, 'REPEAT', NULL, NULL, NULL, 'S017'),
('TG/2024/0018', 'b2a1f4fd0a460606b34c8913e2981dac8d2e283d778aba586c416ee2629bfa54', 4, 'Bhagya Nilakshi Mendis', 'tg20240018@fot.ruh.ac.lk', '0714455018', 'Karagoda Uyangoda', 1, 'ACTIVE', 'TG/2024/0018', '2023/2024', 1, 'REPEAT', NULL, NULL, NULL, 'S018'),
-- 2 Batch-Missed Students
('TG/2024/0019', 'b2a1f4fd0a460606b34c8913e2981dac8d2e283d778aba586c416ee2629bfa54', 4, 'Dilan Chathuranga Nanayakkara', 'tg20240019@fot.ruh.ac.lk', '0714455019', 'Karagoda Uyangoda', 1, 'ACTIVE', 'TG/2024/0019', '2023/2024', 1, 'BATCH_MISSED', NULL, NULL, NULL, 'S019'),
('TG/2024/0020', 'b2a1f4fd0a460606b34c8913e2981dac8d2e283d778aba586c416ee2629bfa54', 4, 'Oshadhi Sewwandi Hettiarachchi', 'tg20240020@fot.ruh.ac.lk', '0714455020', 'Karagoda Uyangoda', 1, 'ACTIVE', 'TG/2024/0020', '2023/2024', 1, 'BATCH_MISSED', NULL, NULL, NULL, 'S020')
ON DUPLICATE KEY UPDATE `password_hash`=VALUES(`password_hash`), `full_name`=VALUES(`full_name`), `student_type`=VALUES(`student_type`), `status`='ACTIVE';