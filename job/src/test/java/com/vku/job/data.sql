-- 1. Tạo bảng Roles nếu chưa có
CREATE TABLE IF NOT EXISTS `roles` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `code` varchar(255) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_role_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 2. Tạo bảng User Profiles nếu chưa có
CREATE TABLE IF NOT EXISTS `user_profiles` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `full_name` varchar(255) DEFAULT NULL,
  `address` varchar(500) DEFAULT NULL,
  `phone_number` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_phone` (`phone_number`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 3. Tạo bảng Users nếu chưa có
CREATE TABLE IF NOT EXISTS `users` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `username` varchar(255) NOT NULL,
  `email` varchar(255) NOT NULL,
  `password` varchar(255) DEFAULT NULL,
  `email_verified` tinyint(1) NOT NULL DEFAULT '0',
  `is_active` int NOT NULL DEFAULT '0',
  `profile_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_email` (`email`),
  UNIQUE KEY `UK_username` (`username`),
  CONSTRAINT `FK_profile` FOREIGN KEY (`profile_id`) REFERENCES `user_profiles` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 4. Tạo bảng User_Roles nếu chưa có
CREATE TABLE IF NOT EXISTS `user_roles` (
  `user_id` bigint NOT NULL,
  `role_id` bigint NOT NULL,
  KEY `FK_role` (`role_id`),
  KEY `FK_user` (`user_id`),
  CONSTRAINT `FK_role_link` FOREIGN KEY (`role_id`) REFERENCES `roles` (`id`),
  CONSTRAINT `FK_user_link` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ---------------------------------------------------------
-- CHÈN DỮ LIỆU (Sử dụng INSERT IGNORE để không bị lỗi khi chạy lại)
-- ---------------------------------------------------------

INSERT IGNORE INTO `roles` (`id`, `code`, `name`) VALUES (1, 'ADMIN', 'Administrators'), (2, 'USER', 'Users');

INSERT IGNORE INTO `user_profiles` (`id`, `full_name`, `address`, `phone_number`) VALUES (2, 'Nguyen Admin', 'Da Nang', '01928337212');

INSERT IGNORE INTO `users` (`id`, `username`, `email`, `password`, `email_verified`, `is_active`, `profile_id`) VALUES 
(1, 'admin', 'phamphunhan625@gmail.com', '$2a$10$wS8O4NGVlcWEUbHXMAUOH.jiwYekFSyHC5ooJJ4/3sOf7Mbx/Hxdu', 1, 0, 2);

INSERT IGNORE INTO `user_roles` (`user_id`, `role_id`) VALUES (1, 1);