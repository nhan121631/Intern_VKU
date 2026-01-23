-- --------------------------------------------------------
-- Máy chủ:                      127.0.0.1
-- Phiên bản máy chủ:            9.4.0 - MySQL Community Server - GPL
-- HĐH máy chủ:                  Linux
-- HeidiSQL Phiên bản:           12.11.0.7065
-- --------------------------------------------------------

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET NAMES utf8 */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;


-- Đang kết xuất đổ cấu trúc cơ sở dữ liệu cho job_management
CREATE DATABASE IF NOT EXISTS `job_management` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;
USE `job_management`;

-- Đang kết xuất đổ cấu trúc cho bảng job_management.roles
CREATE TABLE IF NOT EXISTS `roles` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `createby` varchar(255) DEFAULT NULL,
  `createddate` datetime(6) DEFAULT NULL,
  `updateby` varchar(255) DEFAULT NULL,
  `updateddate` datetime(6) DEFAULT NULL,
  `code` varchar(255) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_role_name` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Đang kết xuất đổ dữ liệu cho bảng job_management.roles: ~2 rows (xấp xỉ)
INSERT INTO `roles` (`id`, `createby`, `createddate`, `updateby`, `updateddate`, `code`, `name`) VALUES
	(1, NULL, NULL, NULL, NULL, 'ADMIN', 'Administrators'),
	(2, NULL, NULL, NULL, NULL, 'USER', 'Users');

-- Đang kết xuất đổ cấu trúc cho bảng job_management.tasks
CREATE TABLE IF NOT EXISTS `tasks` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `createby` varchar(255) DEFAULT NULL,
  `createddate` datetime(6) DEFAULT NULL,
  `updateby` varchar(255) DEFAULT NULL,
  `updateddate` datetime(6) DEFAULT NULL,
  `allow_user_update` bit(1) NOT NULL,
  `deadline` date NOT NULL,
  `description` text,
  `status` enum('CANCELED','DONE','IN_PROGRESS','OPEN') DEFAULT NULL,
  `title` varchar(255) NOT NULL,
  `user_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK6s1ob9k4ihi75xbxe2w0ylsdh` (`user_id`),
  CONSTRAINT `FK6s1ob9k4ihi75xbxe2w0ylsdh` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=23 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Đang kết xuất đổ dữ liệu cho bảng job_management.tasks: ~21 rows (xấp xỉ)
INSERT INTO `tasks` (`id`, `createby`, `createddate`, `updateby`, `updateddate`, `allow_user_update`, `deadline`, `description`, `status`, `title`, `user_id`) VALUES
	(1, NULL, '2026-01-14 09:56:17.446766', NULL, '2026-01-15 16:14:11.084118', b'1', '2026-03-27', 'task for 543', 'IN_PROGRESS', 'task 543', 2),
	(2, NULL, '2026-01-14 09:56:48.721806', NULL, '2026-01-14 15:34:02.637136', b'0', '2026-01-25', 'task 2', 'OPEN', 'task 2', 2),
	(3, NULL, '2026-01-14 09:58:13.955454', NULL, '2026-01-14 16:28:39.312084', b'1', '2026-01-30', 'zvdsvdsfdsss', 'IN_PROGRESS', 'task 32', 2),
	(5, NULL, '2026-01-14 10:03:18.937052', NULL, '2026-01-14 15:33:05.829817', b'1', '2026-01-20', 'This is the 555 task', 'CANCELED', 'Task 5555', 2),
	(6, NULL, '2026-01-14 10:03:42.597564', NULL, '2026-01-14 15:35:47.832639', b'1', '2026-01-31', 'tasskkk 21a', 'OPEN', 'nhanaaa', 2),
	(7, NULL, '2026-01-14 10:04:05.435127', NULL, '2026-01-14 10:04:05.435127', b'0', '2026-01-23', 'abcc dddd', 'OPEN', 'abcc task', 2),
	(8, NULL, '2026-01-14 10:13:05.021145', NULL, '2026-01-14 10:13:05.021145', b'1', '2026-01-15', 'description task for user', 'OPEN', 'task for user', 6),
	(9, NULL, '2026-01-14 10:15:55.362632', NULL, '2026-01-14 10:15:55.362632', b'1', '2026-01-31', 'taks done aaa', 'DONE', 'taks done', 6),
	(10, NULL, '2026-01-14 10:18:13.525311', NULL, '2026-01-14 15:33:50.488866', b'0', '2026-01-31', 'task is cancel', 'IN_PROGRESS', 'task cancel', 2),
	(11, NULL, '2026-01-15 16:01:21.523134', NULL, '2026-01-15 16:01:21.523134', b'1', '2026-01-24', 'taskkkk', 'OPEN', 'tak', 3),
	(12, NULL, '2026-01-15 16:02:30.898118', NULL, '2026-01-15 16:02:30.898118', b'1', '2026-01-23', 'ssssss', 'OPEN', 'aaaaa', 3),
	(13, NULL, '2026-01-16 14:38:09.967027', NULL, '2026-01-16 14:38:20.947780', b'1', '2026-01-24', 'abc', 'IN_PROGRESS', 'task admin', 1),
	(14, NULL, '2026-01-18 19:57:32.664654', NULL, '2026-01-18 19:57:32.664654', b'1', '2026-01-25', 'task notification a', 'OPEN', 'task notification', 7),
	(15, NULL, '2026-01-18 20:26:51.528118', NULL, '2026-01-18 20:26:51.528118', b'1', '2026-01-25', 'task111', 'OPEN', 'task111', 7),
	(16, NULL, '2026-01-18 20:40:55.347715', NULL, '2026-01-18 20:40:55.347715', b'1', '2026-01-24', 'my task admin', 'OPEN', 'my task admin', 1),
	(17, NULL, '2026-01-18 20:41:19.189546', NULL, '2026-01-18 20:41:19.189546', b'1', '2026-02-01', 'task n nhan', 'OPEN', 'task n nhan', 7),
	(18, NULL, '2026-01-18 20:43:21.487227', NULL, '2026-01-18 20:43:21.487227', b'1', '2026-01-31', 'task vana', 'IN_PROGRESS', 'task vana', 7),
	(19, NULL, '2026-01-18 20:44:50.935663', NULL, '2026-01-18 20:44:50.935663', b'1', '2026-01-31', 'vanaa', 'IN_PROGRESS', 'vanaa', 7),
	(20, NULL, '2026-01-19 08:51:30.888806', NULL, '2026-01-19 08:51:30.888806', b'1', '2026-01-22', 'Frontend abc', 'OPEN', 'Frontend', 3),
	(21, NULL, '2026-01-20 08:43:04.630228', NULL, '2026-01-20 08:43:37.092197', b'1', '2026-01-23', 'task for vo van b', 'IN_PROGRESS', 'task for vo van b', 8),
	(22, NULL, '2026-01-20 09:38:57.996051', NULL, '2026-01-21 14:10:25.392712', b'0', '2026-01-23', 'task 3', 'IN_PROGRESS', 'task 3', 2);

-- Đang kết xuất đổ cấu trúc cho bảng job_management.task_history
CREATE TABLE IF NOT EXISTS `task_history` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `createby` varchar(255) DEFAULT NULL,
  `createddate` datetime(6) DEFAULT NULL,
  `updateby` varchar(255) DEFAULT NULL,
  `updateddate` datetime(6) DEFAULT NULL,
  `new_data` json DEFAULT NULL,
  `old_data` json DEFAULT NULL,
  `task_id` bigint NOT NULL,
  `updated_by` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKjqraeud129avhcva579fhioj3` (`task_id`),
  KEY `FKpb4mnrv83un6xgq75nyt5x249` (`updated_by`),
  CONSTRAINT `FKjqraeud129avhcva579fhioj3` FOREIGN KEY (`task_id`) REFERENCES `tasks` (`id`),
  CONSTRAINT `FKpb4mnrv83un6xgq75nyt5x249` FOREIGN KEY (`updated_by`) REFERENCES `users` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=17 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Đang kết xuất đổ dữ liệu cho bảng job_management.task_history: ~13 rows (xấp xỉ)
INSERT INTO `task_history` (`id`, `createby`, `createddate`, `updateby`, `updateddate`, `new_data`, `old_data`, `task_id`, `updated_by`) VALUES
	(3, NULL, '2026-01-14 16:27:50.716439', NULL, '2026-01-14 16:27:50.716439', '{"id": 1, "title": "task 111", "status": "IN_PROGRESS", "deadline": [2026, 3, 1], "description": "task for 111", "assignedUserId": 2, "allowUserUpdate": false}', '{"id": 1, "title": "task 222", "status": "OPEN", "deadline": [2026, 2, 28], "description": "task for 222", "assignedUserId": 3, "allowUserUpdate": true}', 1, 1),
	(4, NULL, '2026-01-14 16:28:39.347955', NULL, '2026-01-14 16:28:39.347955', '{"id": 3, "title": "task 32", "status": "IN_PROGRESS", "deadline": [2026, 1, 30], "description": "zvdsvdsfdsss", "assignedUserId": 2, "allowUserUpdate": true}', '{"id": 3, "title": "task 3", "status": "OPEN", "deadline": [2026, 1, 29], "description": "zvdsvdsfds", "assignedUserId": 2, "allowUserUpdate": true}', 3, 2),
	(5, NULL, '2026-01-14 16:58:26.680155', NULL, '2026-01-14 16:58:26.680155', '{"id": 1, "title": "task 543", "status": "DONE", "deadline": [2026, 2, 28], "description": "task for 543", "assignedUserId": 2, "allowUserUpdate": false}', '{"id": 1, "title": "task 111", "status": "IN_PROGRESS", "deadline": [2026, 3, 1], "description": "task for 111", "assignedUserId": 2, "allowUserUpdate": false}', 1, 1),
	(6, NULL, '2026-01-15 13:48:48.521120', NULL, '2026-01-15 13:48:48.521120', '{"id": 1, "title": "task 543", "status": "DONE", "deadline": [2026, 2, 28], "description": "task for 543", "assignedUserId": 2, "allowUserUpdate": true}', '{"id": 1, "title": "task 543", "status": "DONE", "deadline": [2026, 2, 28], "description": "task for 543", "assignedUserId": 2, "allowUserUpdate": false}', 1, 1),
	(7, NULL, '2026-01-15 13:49:24.609786', NULL, '2026-01-15 13:49:24.609786', '{"id": 1, "title": "task 543", "status": "IN_PROGRESS", "deadline": [2026, 3, 1], "description": "task for 543", "assignedUserId": 2, "allowUserUpdate": true}', '{"id": 1, "title": "task 543", "status": "DONE", "deadline": [2026, 2, 28], "description": "task for 543", "assignedUserId": 2, "allowUserUpdate": true}', 1, 2),
	(8, NULL, '2026-01-15 15:59:48.425614', NULL, '2026-01-15 15:59:48.425614', '{"id": 1, "title": "task 543", "status": "IN_PROGRESS", "deadline": [2026, 3, 1], "description": "task for 543", "assignedUserId": 2, "allowUserUpdate": true}', '{"id": 1, "title": "task 543", "status": "IN_PROGRESS", "deadline": [2026, 3, 1], "description": "task for 543", "assignedUserId": 2, "allowUserUpdate": true}', 1, 1),
	(9, NULL, '2026-01-15 16:14:11.189506', NULL, '2026-01-15 16:14:11.189506', '{"id": 1, "title": "task 543", "status": "IN_PROGRESS", "deadline": [2026, 3, 27], "description": "task for 543", "assignedUserId": 2, "allowUserUpdate": true}', '{"id": 1, "title": "task 543", "status": "IN_PROGRESS", "deadline": [2026, 3, 1], "description": "task for 543", "assignedUserId": 2, "allowUserUpdate": true}', 1, 2),
	(10, NULL, '2026-01-16 14:38:20.975551', NULL, '2026-01-16 14:38:20.975551', '{"id": 13, "title": "task admin", "status": "IN_PROGRESS", "deadline": [2026, 1, 24], "description": "abc", "assignedUserId": 1, "allowUserUpdate": true}', '{"id": 13, "title": "task admin", "status": "IN_PROGRESS", "deadline": [2026, 1, 24], "description": "abc", "assignedUserId": 2, "allowUserUpdate": true}', 13, 1),
	(11, NULL, '2026-01-20 08:43:19.315876', NULL, '2026-01-20 08:43:19.315876', '{"id": 21, "title": "task for vo van b", "status": "IN_PROGRESS", "deadline": [2026, 1, 23], "description": "task for vo van b", "assignedUserId": 8, "allowUserUpdate": true}', '{"id": 21, "title": "task for vo van b", "status": "OPEN", "deadline": [2026, 1, 23], "description": "task for vo van b", "assignedUserId": 8, "allowUserUpdate": true}', 21, 8),
	(12, NULL, '2026-01-20 08:43:27.972718', NULL, '2026-01-20 08:43:27.972718', '{"id": 21, "title": "task for vo van b", "status": "IN_PROGRESS", "deadline": [2026, 1, 23], "description": "task for vo van b", "assignedUserId": 8, "allowUserUpdate": false}', '{"id": 21, "title": "task for vo van b", "status": "IN_PROGRESS", "deadline": [2026, 1, 23], "description": "task for vo van b", "assignedUserId": 8, "allowUserUpdate": true}', 21, 1),
	(13, NULL, '2026-01-20 08:43:37.137715', NULL, '2026-01-20 08:43:37.137715', '{"id": 21, "title": "task for vo van b", "status": "IN_PROGRESS", "deadline": [2026, 1, 23], "description": "task for vo van b", "assignedUserId": 8, "allowUserUpdate": true}', '{"id": 21, "title": "task for vo van b", "status": "IN_PROGRESS", "deadline": [2026, 1, 23], "description": "task for vo van b", "assignedUserId": 8, "allowUserUpdate": false}', 21, 1),
	(14, NULL, '2026-01-20 09:39:17.435127', NULL, '2026-01-20 09:39:17.435127', '{"id": 22, "title": "task 3", "status": "IN_PROGRESS", "deadline": [2026, 1, 23], "description": "task 3", "assignedUserId": 9, "allowUserUpdate": true}', '{"id": 22, "title": "task 3", "status": "OPEN", "deadline": [2026, 1, 23], "description": "task 3", "assignedUserId": 9, "allowUserUpdate": true}', 22, 9),
	(15, NULL, '2026-01-20 09:39:32.849294', NULL, '2026-01-20 09:39:32.849294', '{"id": 22, "title": "task 3", "status": "IN_PROGRESS", "deadline": [2026, 1, 23], "description": "task 3", "assignedUserId": 9, "allowUserUpdate": false}', '{"id": 22, "title": "task 3", "status": "IN_PROGRESS", "deadline": [2026, 1, 23], "description": "task 3", "assignedUserId": 9, "allowUserUpdate": true}', 22, 1),
	(16, NULL, '2026-01-21 14:10:25.620609', NULL, '2026-01-21 14:10:25.620609', '{"id": 22, "title": "task 3", "status": "IN_PROGRESS", "deadline": [2026, 1, 23], "description": "task 3", "assignedUserId": 2, "allowUserUpdate": false}', '{"id": 22, "title": "task 3", "status": "IN_PROGRESS", "deadline": [2026, 1, 23], "description": "task 3", "assignedUserId": 9, "allowUserUpdate": false}', 22, 1);

-- Đang kết xuất đổ cấu trúc cho bảng job_management.users
CREATE TABLE IF NOT EXISTS `users` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `createby` varchar(255) DEFAULT NULL,
  `createddate` datetime(6) DEFAULT NULL,
  `updateby` varchar(255) DEFAULT NULL,
  `updateddate` datetime(6) DEFAULT NULL,
  `email` varchar(255) NOT NULL,
  `email_otp_attempts` int NOT NULL DEFAULT '0',
  `email_otp_expiry` bigint DEFAULT NULL,
  `email_otp_hash` varchar(255) DEFAULT NULL,
  `email_verified` tinyint(1) NOT NULL DEFAULT '0',
  `is_active` int NOT NULL DEFAULT '0',
  `password` varchar(255) DEFAULT NULL,
  `username` varchar(255) NOT NULL,
  `profile_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK6dotkott2kjsp8vw4d0m25fb7` (`email`),
  UNIQUE KEY `UKr43af9ap4edm43mmtq01oddj6` (`username`),
  UNIQUE KEY `UK7s5nlreekaxdbfml4ofky7utw` (`profile_id`),
  CONSTRAINT `FK9ni9y01cgm4kt2lp4d8smxm45` FOREIGN KEY (`profile_id`) REFERENCES `user_profiles` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Đang kết xuất đổ dữ liệu cho bảng job_management.users: ~7 rows (xấp xỉ)
INSERT INTO `users` (`id`, `createby`, `createddate`, `updateby`, `updateddate`, `email`, `email_otp_attempts`, `email_otp_expiry`, `email_otp_hash`, `email_verified`, `is_active`, `password`, `username`, `profile_id`) VALUES
	(1, NULL, NULL, NULL, NULL, 'phamphunhan625@gmail.com', 0, NULL, NULL, 1, 0, '$2a$10$wS8O4NGVlcWEUbHXMAUOH.jiwYekFSyHC5ooJJ4/3sOf7Mbx/Hxdu', 'admin', 2),
	(2, NULL, '2026-01-13 14:43:47.233714', NULL, '2026-01-19 14:11:11.210259', 'phamphunhan624@gmail.com', 0, NULL, NULL, 1, 0, '$2a$10$4nXqdwAcp5KZ9zkMYrJR3.PmC7pFUF2qDjZtBi3ZlW/qP5fxT/nXy', 'vanteo', 1),
	(3, NULL, '2026-01-13 16:29:49.542398', NULL, '2026-01-20 12:23:20.616341', 'nhanpp.21it@vku.udn.vn', 0, 1768887200613, '$2a$10$iTZH/C9O2r3nSe/fMfkCo.k0NJZNnBBWJpJI1vwmPpiYlZMjLd4PO', 1, 0, '$2a$10$wrpg/DuKelm.DxVWrJlvfukQguOINP2LPniOMm9bhVHvgNue4mGK6', 'ppnhan', 3),
	(6, NULL, '2026-01-13 16:47:57.159949', NULL, '2026-01-13 16:47:57.159949', 'phamphunhanpham8@gmail.com', 0, NULL, NULL, 1, 0, NULL, 'phamphunhanpham8@gmail.com', 4),
	(7, NULL, '2026-01-18 18:46:31.453621', NULL, '2026-01-18 19:30:27.581928', 'nhanpham02062003@gmail.com', 0, NULL, NULL, 1, 0, '$2a$10$WfxGjYmyBLmOGN/oXCUTT.tZiEYHZfrOh8L7AnDGCWMIpFsXwxrZm', 'nvnhan', 5),
	(8, NULL, '2026-01-20 08:39:52.431953', NULL, '2026-01-20 08:44:36.631818', 'teney58595@elafans.com', 0, NULL, NULL, 1, 0, '$2a$10$.4oSHmPN0dnAr/1zYB8PcOUGB86MWRUViYTuZ5QJaP3l5Gk3gAhem', 'vovanb', 6),
	(9, NULL, '2026-01-20 09:34:38.111270', NULL, '2026-01-20 12:08:34.035417', 'nhanaaa@imail.edu.vn', 0, NULL, NULL, 1, 0, '$2a$10$pzkOO9BlX50QomQrsu64luHllxiXyEEtK1ghbt507w/z1rHxWr3Uy', 'vovanc', 7);

-- Đang kết xuất đổ cấu trúc cho bảng job_management.user_profiles
CREATE TABLE IF NOT EXISTS `user_profiles` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `createby` varchar(255) DEFAULT NULL,
  `createddate` datetime(6) DEFAULT NULL,
  `updateby` varchar(255) DEFAULT NULL,
  `updateddate` datetime(6) DEFAULT NULL,
  `address` varchar(500) DEFAULT NULL,
  `avatar` varchar(255) DEFAULT NULL,
  `full_name` varchar(255) DEFAULT NULL,
  `phone_number` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKmynugghte22mpww9q1cpdn2ev` (`phone_number`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Đang kết xuất đổ dữ liệu cho bảng job_management.user_profiles: ~7 rows (xấp xỉ)
INSERT INTO `user_profiles` (`id`, `createby`, `createddate`, `updateby`, `updateddate`, `address`, `avatar`, `full_name`, `phone_number`) VALUES
	(1, NULL, '2026-01-13 14:43:47.247353', NULL, '2026-01-16 16:36:08.044653', '90 Nguyễn Thức Tự', '/uploads/1768551804306_avt-luffy.jpg', 'Nguyễn Tèo abc', '0919282832'),
	(2, NULL, NULL, NULL, '2026-01-16 14:37:33.977317', '92 Nguyen Thuc Tu, Ngu Hanh Son, Da Nang', '/uploads/1768549053970_avt-luffy.jpg', 'Nguyen Admin', '01928337212'),
	(3, NULL, '2026-01-13 16:29:49.555884', NULL, '2026-01-19 08:53:14.068694', '356 Đường Xô Viết Nghệ Tĩnh', NULL, 'Phạm Phú Nhân', '0922112223'),
	(4, NULL, '2026-01-13 16:47:57.189202', NULL, '2026-01-13 16:47:57.189202', NULL, NULL, 'Nhân Phạm Phú', NULL),
	(5, NULL, '2026-01-18 18:46:31.456229', NULL, '2026-01-18 18:47:25.043254', 'Dà nẵng', '/uploads/1768736844982_avt.webp', 'Nguyễn Văn Nhân', '0393222112'),
	(6, NULL, '2026-01-20 08:39:52.441110', NULL, '2026-01-20 08:43:58.901043', '90 Nguyễn Thức Tự', '/uploads/1768873438881_avt-luffy.jpg', 'Võ Văn B', '0933221223'),
	(7, NULL, '2026-01-20 09:34:38.111270', NULL, '2026-01-20 09:35:53.576316', '356 Đường Xô Viết Nghệ Tĩnh', '/uploads/1768876553558_avt-luffy.jpg', 'Võ Văn C', '04944444333');

-- Đang kết xuất đổ cấu trúc cho bảng job_management.user_roles
CREATE TABLE IF NOT EXISTS `user_roles` (
  `user_id` bigint NOT NULL,
  `role_id` bigint NOT NULL,
  KEY `FKh8ciramu9cc9q3qcqiv4ue8a6` (`role_id`),
  KEY `FKhfh9dx7w3ubf1co1vdev94g3f` (`user_id`),
  CONSTRAINT `FKh8ciramu9cc9q3qcqiv4ue8a6` FOREIGN KEY (`role_id`) REFERENCES `roles` (`id`),
  CONSTRAINT `FKhfh9dx7w3ubf1co1vdev94g3f` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Đang kết xuất đổ dữ liệu cho bảng job_management.user_roles: ~7 rows (xấp xỉ)
INSERT INTO `user_roles` (`user_id`, `role_id`) VALUES
	(2, 2),
	(1, 1),
	(3, 2),
	(6, 2),
	(7, 2),
	(8, 2),
	(9, 2);

/*!40103 SET TIME_ZONE=IFNULL(@OLD_TIME_ZONE, 'system') */;
/*!40101 SET SQL_MODE=IFNULL(@OLD_SQL_MODE, '') */;
/*!40014 SET FOREIGN_KEY_CHECKS=IFNULL(@OLD_FOREIGN_KEY_CHECKS, 1) */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40111 SET SQL_NOTES=IFNULL(@OLD_SQL_NOTES, 1) */;
