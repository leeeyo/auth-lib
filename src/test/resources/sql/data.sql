INSERT INTO roles (role_name) VALUES ('ADMIN');
INSERT INTO roles (role_name) VALUES ('USER');
INSERT INTO roles (role_name) VALUES ('GUEST');
INSERT INTO users (username, email, password, create_time, role_id,status) VALUES
('aziz0', '000@exemple.com', '123', '2025-01-10', (SELECT role_id FROM roles WHERE role_name = 'ADMIN'),'offline'),
('aziz1', '111@exemple.com', '123', '2025-01-10', (SELECT role_id FROM roles WHERE role_name = 'USER'),'offline'),
('aziz2', '222@exemple.com', '122', '2025-01-10', (SELECT role_id FROM roles WHERE role_name = 'GUEST'),'offline'),
('aziz3', '333@exemple.com', '000', '2025-01-10', (SELECT role_id FROM roles WHERE role_name = 'GUEST'),'offline');