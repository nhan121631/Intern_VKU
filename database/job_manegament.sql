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
  `deadline` date DEFAULT NULL,
  `description` text,
  `status` enum('CANCELED','DONE','IN_PROGRESS','OPEN') DEFAULT NULL,
  `title` varchar(255) NOT NULL,
  `user_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK6s1ob9k4ihi75xbxe2w0ylsdh` (`user_id`),
  CONSTRAINT `FK6s1ob9k4ihi75xbxe2w0ylsdh` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=18 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Đang kết xuất đổ dữ liệu cho bảng job_management.tasks: ~4 rows (xấp xỉ)
INSERT INTO `tasks` (`id`, `createby`, `createddate`, `updateby`, `updateddate`, `deadline`, `description`, `status`, `title`, `user_id`) VALUES
	(4, NULL, '2026-01-08 15:52:27.247037', NULL, '2026-01-12 14:27:40.980676', '2026-01-17', 'This is the aaa task', 'DONE', 'Task aaaa', 4),
	(5, NULL, '2026-01-12 09:35:03.638446', NULL, '2026-01-12 10:40:51.764006', '2026-01-19', 'This is the abcd task', 'IN_PROGRESS', 'Task abc', 1),
	(6, NULL, '2026-01-12 09:35:15.878678', NULL, '2026-01-12 13:44:54.264914', '2026-01-19', 'This is the 2 task', 'OPEN', 'Task 6', 1),
	(7, NULL, '2026-01-12 09:35:22.264572', NULL, '2026-01-12 13:44:39.947727', '2026-01-19', 'This is the 1 task', 'OPEN', 'Task 7', 1),
	(8, NULL, '2026-01-12 10:04:52.611178', NULL, '2026-01-12 10:04:52.611178', '2026-01-19', 'This is the 8 task', 'OPEN', 'Task 8', 2),
	(9, NULL, '2026-01-12 10:05:16.657297', NULL, '2026-01-12 10:05:16.657297', '2026-01-19', 'This is the 9 task', 'OPEN', 'Task 9', 3),
	(11, NULL, '2026-01-12 10:51:36.251287', NULL, '2026-01-12 10:51:36.251287', '2026-01-19', 'This is the 11 task', 'IN_PROGRESS', 'Task 11', 4),
	(13, NULL, '2026-01-12 10:58:54.478228', NULL, '2026-01-12 10:58:54.478228', '2026-01-15', 'shot', 'OPEN', 'abc', 1),
	(14, NULL, '2026-01-12 10:59:28.159424', NULL, '2026-01-12 10:59:28.159424', '2026-01-13', 'aaaa', 'OPEN', 'aaa', 2),
	(15, NULL, '2026-01-12 10:59:54.922922', NULL, '2026-01-12 10:59:54.922922', '2026-01-14', 'âsasasasa', 'OPEN', 'sâs', 3),
	(16, NULL, '2026-01-12 11:15:31.481661', NULL, '2026-01-12 11:15:31.481661', '2026-01-12', 'Task for van a abc', 'IN_PROGRESS', 'Task for van a', 2),
	(17, NULL, '2026-01-12 11:24:06.231477', NULL, '2026-01-12 11:24:06.231477', '2026-01-13', 'task for van b abc', 'DONE', 'task for van b', 4);

-- Đang kết xuất đổ cấu trúc cho bảng job_management.users
CREATE TABLE IF NOT EXISTS `users` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `createby` varchar(255) DEFAULT NULL,
  `createddate` datetime(6) DEFAULT NULL,
  `updateby` varchar(255) DEFAULT NULL,
  `updateddate` datetime(6) DEFAULT NULL,
  `full_name` varchar(255) DEFAULT NULL,
  `is_active` int NOT NULL DEFAULT '0',
  `password` varchar(255) DEFAULT NULL,
  `username` varchar(255) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKr43af9ap4edm43mmtq01oddj6` (`username`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Đang kết xuất đổ dữ liệu cho bảng job_management.users: ~7 rows (xấp xỉ)
INSERT INTO `users` (`id`, `createby`, `createddate`, `updateby`, `updateddate`, `full_name`, `is_active`, `password`, `username`) VALUES
	(1, NULL, NULL, NULL, NULL, 'Admin', 0, '$2a$10$/OFBlczyZmfK.42zc1ucTej.PEMJj02hpSRLE4S66/IqU84h7FD1W', 'Admin'),
	(2, NULL, NULL, NULL, '2026-01-12 14:26:09.565124', 'Nguyễn Văn A', 0, '$2a$10$/OFBlczyZmfK.42zc1ucTej.PEMJj02hpSRLE4S66/IqU84h7FD1W', 'vana'),
	(3, NULL, '2026-01-08 16:18:11.379335', NULL, '2026-01-12 14:31:26.753145', 'Phạm Phú Nhân', 0, '$2a$10$q4iN4k8IWDwJm6All2qGi.gn4ZCeRjWDYWyrpwcFi/4Z/K8nUKO6i', 'nhan'),
	(4, NULL, '2026-01-08 16:20:42.691642', NULL, '2026-01-12 14:27:00.028273', 'NguyenVanB', 0, '$2a$10$IkFDtvEuAw77AEdGUASKX.czYyahs2rd9VZ8FAJH4f5bnuaCnZJva', 'nguyenvanb'),
	(5, NULL, '2026-01-09 10:25:18.177513', NULL, '2026-01-12 14:26:32.610564', 'Nguyễn Văn C', 0, '$2a$10$WsPNWhEYwYftAZUsu9DPyufFma9EK1SfOYG3PlYWefeZD21RYHEsi', 'nguyenvanc'),
	(6, NULL, '2026-01-09 11:07:13.337726', NULL, '2026-01-09 11:07:13.337726', 'PHẠM PHÚ NHÂN', 0, NULL, 'nhanpp.21it@vku.udn.vn'),
	(7, NULL, '2026-01-09 11:23:26.788871', NULL, '2026-01-09 11:23:26.788871', 'Nhân Phạm Phú', 0, NULL, 'phamphunhanpham8@gmail.com'),
	(8, NULL, '2026-01-09 15:45:42.172804', NULL, '2026-01-12 14:26:33.564810', 'Nguyễn Văn D', 0, '$2a$10$0cyJXekqxiTBGwrT2vRuWOM0JpFdhoWwJHWWRFsEVnw6k5IfnTsei', 'nvand');

-- Đang kết xuất đổ cấu trúc cho bảng job_management.user_roles
CREATE TABLE IF NOT EXISTS `user_roles` (
  `user_id` bigint NOT NULL,
  `role_id` bigint NOT NULL,
  KEY `FKh8ciramu9cc9q3qcqiv4ue8a6` (`role_id`),
  KEY `FKhfh9dx7w3ubf1co1vdev94g3f` (`user_id`),
  CONSTRAINT `FKh8ciramu9cc9q3qcqiv4ue8a6` FOREIGN KEY (`role_id`) REFERENCES `roles` (`id`),
  CONSTRAINT `FKhfh9dx7w3ubf1co1vdev94g3f` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Đang kết xuất đổ dữ liệu cho bảng job_management.user_roles: ~6 rows (xấp xỉ)
INSERT INTO `user_roles` (`user_id`, `role_id`) VALUES
	(1, 1),
	(2, 2),
	(3, 2),
	(4, 2),
	(5, 2),
	(6, 2),
	(7, 2),
	(8, 2);

/*!40103 SET TIME_ZONE=IFNULL(@OLD_TIME_ZONE, 'system') */;
/*!40101 SET SQL_MODE=IFNULL(@OLD_SQL_MODE, '') */;
/*!40014 SET FOREIGN_KEY_CHECKS=IFNULL(@OLD_FOREIGN_KEY_CHECKS, 1) */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40111 SET SQL_NOTES=IFNULL(@OLD_SQL_NOTES, 1) */;
