-- ========================================
-- 图书馆管理系统 - 数据库初始化脚本
-- 可重复执行：会先删除旧表再重建
-- ========================================

DROP DATABASE IF EXISTS library_system;

-- 创建数据库
CREATE DATABASE IF NOT EXISTS library_system CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE library_system;

-- 删除旧表（按外键依赖逆序）
DROP TABLE IF EXISTS scheduling;
DROP TABLE IF EXISTS borrowing;
DROP TABLE IF EXISTS reservation;
DROP TABLE IF EXISTS seat;
DROP TABLE IF EXISTS book;
DROP TABLE IF EXISTS reader;
DROP TABLE IF EXISTS librarian;
DROP TABLE IF EXISTS admin;

-- 1. 管理员表
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

-- 2. 图书管理员表
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

-- 3. 读者表
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
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 4. 图书表
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
    status INT DEFAULT 1 COMMENT '状态：0-下架 1-正常',
    cover_url VARCHAR(255),
    description TEXT,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 5. 座位表
CREATE TABLE IF NOT EXISTS seat (
    id INT PRIMARY KEY AUTO_INCREMENT,
    seat_number VARCHAR(20) UNIQUE NOT NULL,
    area VARCHAR(50),
    status TINYINT DEFAULT 0,
    device_id VARCHAR(50),
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 6. 预约表（图书预约/座位预约）
CREATE TABLE IF NOT EXISTS reservation (
    id INT PRIMARY KEY AUTO_INCREMENT,
    reader_id INT NOT NULL,
    book_id INT,
    seat_id INT,
    reservation_date DATETIME DEFAULT CURRENT_TIMESTAMP,
    status TINYINT DEFAULT 0,
    expiry_date DATETIME,
    FOREIGN KEY (reader_id) REFERENCES reader(id),
    FOREIGN KEY (book_id) REFERENCES book(id),
    FOREIGN KEY (seat_id) REFERENCES seat(id)
);

-- 7. 借阅表
CREATE TABLE IF NOT EXISTS borrowing (
    id INT PRIMARY KEY AUTO_INCREMENT,
    reader_id INT NOT NULL,
    book_id INT NOT NULL,
    borrow_date DATETIME DEFAULT CURRENT_TIMESTAMP,
    due_date DATETIME NOT NULL,
    return_date DATETIME,
    renew_count INT DEFAULT 0,
    status TINYINT DEFAULT 1,
    fine_amount DECIMAL(10, 2) DEFAULT 0.00,
    FOREIGN KEY (reader_id) REFERENCES reader(id),
    FOREIGN KEY (book_id) REFERENCES book(id),
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 8. 排班表（座位预约）
CREATE TABLE IF NOT EXISTS scheduling (
    id INT PRIMARY KEY AUTO_INCREMENT,
    seat_id INT NOT NULL,
    reader_id INT NOT NULL,
    start_time DATETIME NOT NULL,
    end_time DATETIME NOT NULL,
    status TINYINT DEFAULT 0,
    FOREIGN KEY (seat_id) REFERENCES seat(id),
    FOREIGN KEY (reader_id) REFERENCES reader(id)
);

-- ========================================
-- 为关键查询字段添加索引
-- ========================================
ALTER TABLE book ADD INDEX idx_book_title (title);
ALTER TABLE book ADD INDEX idx_book_author (author);
ALTER TABLE book ADD INDEX idx_book_category (category);
ALTER TABLE borrowing ADD INDEX idx_borrowing_reader_id (reader_id);
ALTER TABLE borrowing ADD INDEX idx_borrowing_book_id (book_id);
ALTER TABLE borrowing ADD INDEX idx_borrowing_status (status);
ALTER TABLE reservation ADD INDEX idx_reservation_reader_id (reader_id);
ALTER TABLE reservation ADD INDEX idx_reservation_book_id (book_id);
ALTER TABLE reservation ADD INDEX idx_reservation_seat_id (seat_id);
ALTER TABLE reservation ADD INDEX idx_reservation_status (status);
ALTER TABLE seat ADD INDEX idx_seat_area (area);

-- 插入初始数据
-- 管理员账号（密码：admin123）
INSERT INTO admin (username, password, real_name, phone, email) VALUES
('admin', '$2a$10$SxV1MCbYPs/VY7DCQjiSVu4JUZGhh79IQzFUdZaowh7gGUkAN5GcG', '系统管理员', '13800138000', 'admin@library.com');

-- 图书管理员账号（密码：admin123）
INSERT INTO librarian (username, password, real_name, phone, email) VALUES
('librarian', '$2a$10$SxV1MCbYPs/VY7DCQjiSVu4JUZGhh79IQzFUdZaowh7gGUkAN5GcG', '图书管理员', '13800138001', 'librarian@library.com');

-- 读者账号（密码：admin123）
INSERT INTO reader (reader_id, password, real_name, gender, phone, email, department) VALUES
('2024001', '$2a$10$SxV1MCbYPs/VY7DCQjiSVu4JUZGhh79IQzFUdZaowh7gGUkAN5GcG', '张三', 1, '13800138002', 'zhangsan@library.com', '计算机学院'),
('2024002', '$2a$10$SxV1MCbYPs/VY7DCQjiSVu4JUZGhh79IQzFUdZaowh7gGUkAN5GcG', '李四', 0, '13800138003', 'lisi@library.com', '数学学院');

-- 插入示例图书数据
INSERT INTO book (isbn, title, author, publisher, category, location, total_count, available_count, price, status) VALUES
('9787115546081', 'Java编程思想', 'Bruce Eckel', '人民邮电出版社', '计算机', 'A-01-01', 5, 5, 89.00, 1),
('9787111558606', '深入理解计算机系统', 'Randal E. Bryant', '机械工业出版社', '计算机', 'A-01-02', 3, 3, 128.00, 1),
('9787115428028', '算法导论', 'Thomas H. Cormen', '人民邮电出版社', '计算机', 'A-01-03', 4, 4, 129.00, 1),
('9787100042895', '数据库系统概念', 'Abraham Silberschatz', '机械工业出版社', '计算机', 'A-01-04', 2, 2, 99.00, 1),
('9787115517227', '计算机网络', 'Andrew S. Tanenbaum', '人民邮电出版社', '计算机', 'A-01-05', 3, 3, 89.00, 1);

-- 插入示例座位数据
INSERT INTO seat (seat_number, area, status) VALUES
('A001', '阅览室A', 0),
('A002', '阅览室A', 0),
('A003', '阅览室A', 0),
('B001', '阅览室B', 0),
('B002', '阅览室B', 0),
('C001', '自习室C', 0),
('C002', '自习室C', 0),
('C003', '自习室C', 0);

-- ========================================
-- V2 新增功能模块表
-- ========================================

-- 9. 系统参数配置表
CREATE TABLE IF NOT EXISTS system_config (
    id INT PRIMARY KEY AUTO_INCREMENT,
    config_key VARCHAR(100) UNIQUE NOT NULL,
    config_value VARCHAR(500) NOT NULL,
    description VARCHAR(200),
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 10. 操作日志表
CREATE TABLE IF NOT EXISTS operation_log (
    id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT,
    username VARCHAR(50),
    role VARCHAR(20),
    module VARCHAR(50),
    action VARCHAR(50),
    detail TEXT,
    ip VARCHAR(50),
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- 11. 黑名单表
CREATE TABLE IF NOT EXISTS blacklist (
    id INT PRIMARY KEY AUTO_INCREMENT,
    reader_id INT NOT NULL,
    reason VARCHAR(200),
    violation_count INT DEFAULT 1,
    blacklisted TINYINT DEFAULT 1,
    start_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    end_time DATETIME,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (reader_id) REFERENCES reader(id)
);

-- 12. 读者等级积分表
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

-- 13. 座位签到表
CREATE TABLE IF NOT EXISTS seat_checkin (
    id INT PRIMARY KEY AUTO_INCREMENT,
    seat_id INT NOT NULL,
    reader_id INT NOT NULL,
    reservation_id INT,
    checkin_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    checkout_time DATETIME,
    status TINYINT DEFAULT 1,
    FOREIGN KEY (seat_id) REFERENCES seat(id),
    FOREIGN KEY (reader_id) REFERENCES reader(id)
);

-- 14. 通知消息表
CREATE TABLE IF NOT EXISTS notification (
    id INT PRIMARY KEY AUTO_INCREMENT,
    reader_id INT NOT NULL,
    title VARCHAR(100) NOT NULL,
    content TEXT,
    type VARCHAR(20) DEFAULT 'system',
    is_read TINYINT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (reader_id) REFERENCES reader(id)
);

-- V2 索引
ALTER TABLE operation_log ADD INDEX idx_oplog_user_id (user_id);
ALTER TABLE operation_log ADD INDEX idx_oplog_module (module);
ALTER TABLE operation_log ADD INDEX idx_oplog_create_time (create_time);
ALTER TABLE blacklist ADD INDEX idx_blacklist_reader_id (reader_id);
ALTER TABLE reader_level ADD INDEX idx_reader_level_reader_id (reader_id);
ALTER TABLE seat_checkin ADD INDEX idx_seat_checkin_reader_id (reader_id);
ALTER TABLE seat_checkin ADD INDEX idx_seat_checkin_seat_id (seat_id);
ALTER TABLE notification ADD INDEX idx_notification_reader_id (reader_id);
ALTER TABLE notification ADD INDEX idx_notification_is_read (is_read);

-- V2 插入系统参数默认数据
INSERT INTO system_config (config_key, config_value, description) VALUES
('library.name', '智慧图书馆', '图书馆名称'),
('library.borrow.default_days', '30', '默认借阅天数'),
('library.borrow.max_count', '5', '读者最大借阅数量'),
('library.borrow.max_renew_count', '2', '最大续借次数'),
('library.fine.daily_rate', '0.10', '逾期罚款每日费率（元）'),
('library.seat.auto_release_minutes', '30', '签到超时自动释放时间（分钟）'),
('library.blacklist.violation_threshold', '3', '违规次数达到此值自动加入黑名单'),
('library.notification.borrow_remind_days', '3', '借阅到期前提醒天数');
