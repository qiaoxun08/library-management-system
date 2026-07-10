-- ========================================
-- Testcontainers MySQL 初始化脚本
-- ========================================

-- 管理员表
CREATE TABLE IF NOT EXISTS admin (
    id INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    real_name VARCHAR(50),
    phone VARCHAR(20),
    email VARCHAR(100),
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 图书管理员表
CREATE TABLE IF NOT EXISTS librarian (
    id INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    real_name VARCHAR(50),
    phone VARCHAR(20),
    email VARCHAR(100),
    status TINYINT DEFAULT 1,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 读者表
CREATE TABLE IF NOT EXISTS reader (
    id INT PRIMARY KEY AUTO_INCREMENT,
    reader_id VARCHAR(20) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    real_name VARCHAR(50) NOT NULL,
    gender TINYINT,
    phone VARCHAR(20),
    email VARCHAR(100),
    department VARCHAR(100),
    max_borrow_count INT DEFAULT 5,
    current_borrow_count INT DEFAULT 0,
    fine_amount DECIMAL(10, 2) DEFAULT 0.00,
    status TINYINT DEFAULT 1,
    preferred_categories VARCHAR(500),
    language VARCHAR(10) DEFAULT 'zh_CN',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 图书表
CREATE TABLE IF NOT EXISTS book (
    id INT PRIMARY KEY AUTO_INCREMENT,
    isbn VARCHAR(20) UNIQUE NOT NULL,
    title VARCHAR(255) NOT NULL,
    author VARCHAR(100),
    publisher VARCHAR(100),
    publish_date DATE,
    category VARCHAR(50),
    location VARCHAR(100),
    total_count INT DEFAULT 0,
    available_count INT DEFAULT 0,
    price DECIMAL(10, 2),
    status INT DEFAULT 1,
    cover_url VARCHAR(255),
    description TEXT,
    title_en VARCHAR(255),
    author_en VARCHAR(100),
    description_en TEXT,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 座位表
CREATE TABLE IF NOT EXISTS seat (
    id INT PRIMARY KEY AUTO_INCREMENT,
    seat_number VARCHAR(20) UNIQUE NOT NULL,
    area VARCHAR(50),
    status INT DEFAULT 0,
    device_id VARCHAR(50),
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 借阅记录表
CREATE TABLE IF NOT EXISTS borrowing (
    id INT PRIMARY KEY AUTO_INCREMENT,
    reader_id INT NOT NULL,
    book_id INT NOT NULL,
    borrow_date DATETIME,
    due_date DATETIME,
    return_date DATETIME,
    status INT DEFAULT 1,
    fine_amount DECIMAL(10, 2) DEFAULT 0.00,
    renew_count INT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 预约记录表
CREATE TABLE IF NOT EXISTS reservation (
    id INT PRIMARY KEY AUTO_INCREMENT,
    reader_id INT NOT NULL,
    book_id INT,
    seat_id INT,
    status INT DEFAULT 0,
    expiry_date DATETIME,
    start_time DATETIME,
    end_time DATETIME,
    preferred_time_slot VARCHAR(20),
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 座位签到表
CREATE TABLE IF NOT EXISTS seat_checkin (
    id INT PRIMARY KEY AUTO_INCREMENT,
    seat_id INT NOT NULL,
    reader_id INT NOT NULL,
    reservation_id INT,
    checkin_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    checkout_time DATETIME,
    status INT DEFAULT 1,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- 读者等级表
CREATE TABLE IF NOT EXISTS reader_level (
    id INT PRIMARY KEY AUTO_INCREMENT,
    reader_id INT UNIQUE NOT NULL,
    points INT DEFAULT 0,
    level VARCHAR(20) DEFAULT '普通',
    total_borrow_count INT DEFAULT 0,
    total_return_on_time INT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (reader_id) REFERENCES reader(id)
);

-- 系统配置表
CREATE TABLE IF NOT EXISTS system_config (
    id INT PRIMARY KEY AUTO_INCREMENT,
    config_key VARCHAR(100) UNIQUE NOT NULL,
    config_value VARCHAR(500),
    description VARCHAR(200),
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 操作日志表
CREATE TABLE IF NOT EXISTS operation_log (
    id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT,
    username VARCHAR(50),
    role VARCHAR(20),
    module VARCHAR(50),
    action VARCHAR(100),
    detail TEXT,
    ip VARCHAR(50),
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- 黑名单表
CREATE TABLE IF NOT EXISTS blacklist (
    id INT PRIMARY KEY AUTO_INCREMENT,
    reader_id INT UNIQUE NOT NULL,
    violation_count INT DEFAULT 0,
    reason VARCHAR(500),
    blacklisted TINYINT DEFAULT 0,
    start_time DATETIME,
    end_time DATETIME,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 通知表
CREATE TABLE IF NOT EXISTS notification (
    id INT PRIMARY KEY AUTO_INCREMENT,
    reader_id INT NOT NULL,
    title VARCHAR(100),
    content TEXT,
    type VARCHAR(20),
    is_read TINYINT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- 书评表
CREATE TABLE IF NOT EXISTS book_review (
    id INT PRIMARY KEY AUTO_INCREMENT,
    book_id INT NOT NULL,
    reader_id INT NOT NULL,
    content TEXT,
    rating INT,
    status TINYINT DEFAULT 1,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 书评点赞表
CREATE TABLE IF NOT EXISTS review_like (
    id INT PRIMARY KEY AUTO_INCREMENT,
    review_id INT NOT NULL,
    reader_id INT NOT NULL,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_review_reader (review_id, reader_id)
);

-- 关注关系表
CREATE TABLE IF NOT EXISTS reader_follow (
    id INT PRIMARY KEY AUTO_INCREMENT,
    follower_id INT NOT NULL,
    followee_id INT NOT NULL,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_follow (follower_id, followee_id)
);

-- 书评回复表
CREATE TABLE IF NOT EXISTS review_reply (
    id INT PRIMARY KEY AUTO_INCREMENT,
    review_id INT NOT NULL,
    reader_id INT NOT NULL,
    content TEXT,
    reply_to_reader_id INT,
    parent_reply_id INT,
    status TINYINT DEFAULT 1,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- 学习伙伴表
CREATE TABLE IF NOT EXISTS study_buddy (
    id INT PRIMARY KEY AUTO_INCREMENT,
    reader_id INT NOT NULL,
    preferred_slot VARCHAR(20),
    preferred_area VARCHAR(50),
    study_goal VARCHAR(200),
    status TINYINT DEFAULT 1,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 座位排期表
CREATE TABLE IF NOT EXISTS scheduling (
    id INT PRIMARY KEY AUTO_INCREMENT,
    seat_id INT NOT NULL,
    reader_id INT NOT NULL,
    start_time DATETIME NOT NULL,
    end_time DATETIME NOT NULL,
    status INT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- RBAC 表
CREATE TABLE IF NOT EXISTS sys_role (
    id INT PRIMARY KEY AUTO_INCREMENT,
    role_key VARCHAR(50) UNIQUE NOT NULL,
    role_name VARCHAR(50) NOT NULL,
    description VARCHAR(200),
    status TINYINT DEFAULT 1,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS sys_permission (
    id INT PRIMARY KEY AUTO_INCREMENT,
    perm_key VARCHAR(100) UNIQUE NOT NULL,
    perm_name VARCHAR(100) NOT NULL,
    module VARCHAR(50) NOT NULL,
    description VARCHAR(200),
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS sys_user_role (
    id INT PRIMARY KEY AUTO_INCREMENT,
    user_type VARCHAR(20) NOT NULL,
    user_id INT NOT NULL,
    role_id INT NOT NULL,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_role (user_type, user_id, role_id),
    FOREIGN KEY (role_id) REFERENCES sys_role(id)
);

CREATE TABLE IF NOT EXISTS sys_role_permission (
    id INT PRIMARY KEY AUTO_INCREMENT,
    role_id INT NOT NULL,
    permission_id INT NOT NULL,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_role_perm (role_id, permission_id),
    FOREIGN KEY (role_id) REFERENCES sys_role(id),
    FOREIGN KEY (permission_id) REFERENCES sys_permission(id)
);

-- 初始化数据
INSERT INTO admin (username, password, real_name) VALUES
    ('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '系统管理员');

INSERT INTO reader (reader_id, password, real_name, status) VALUES
    ('2024001', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '张三', 1),
    ('2024002', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '李四', 1);

INSERT INTO sys_role (role_key, role_name, description) VALUES
    ('SUPER_ADMIN', '超级管理员', '系统最高权限'),
    ('LIBRARIAN', '图书管理员', '图书管理'),
    ('READER', '读者', '普通读者');

INSERT INTO sys_permission (perm_key, perm_name, module) VALUES
    ('book:view', '查看图书', 'book'),
    ('book:create', '创建图书', 'book'),
    ('borrow:create', '借书', 'borrow');

INSERT INTO sys_user_role (user_type, user_id, role_id)
SELECT 'ADMIN', id, (SELECT id FROM sys_role WHERE role_key = 'SUPER_ADMIN')
FROM admin WHERE username = 'admin';

INSERT INTO sys_user_role (user_type, user_id, role_id)
SELECT 'READER', id, (SELECT id FROM sys_role WHERE role_key = 'READER')
FROM reader WHERE reader_id = '2024001';
