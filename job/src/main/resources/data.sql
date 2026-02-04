-- data.sql
INSERT IGNORE INTO `roles` (`id`, `code`, `name`) VALUES (1, 'ADMIN', 'Administrators'), (2, 'USER', 'Users');

INSERT IGNORE INTO `user_profiles` (`id`, `full_name`, `address`, `phone_number`) VALUES (2, 'Nguyen Admin', 'Da Nang', '01928337212');

INSERT IGNORE INTO `users` (`id`, `username`, `email`, `password`, `email_verified`, `is_active`, `profile_id`) VALUES 
(1, 'admin', 'phamphunhan625@gmail.com', '$2a$10$wS8O4NGVlcWEUbHXMAUOH.jiwYekFSyHC5ooJJ4/3sOf7Mbx/Hxdu', 1, 0, 2);

INSERT IGNORE INTO user_roles (user_id, role_id) VALUES (1, 1);
