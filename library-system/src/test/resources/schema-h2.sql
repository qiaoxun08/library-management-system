-- H2 兼容的建表脚本（MySQL 模式）

-- 1. 管理员表
CREATE TABLE IF NOT EXISTS admin (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    real_name VARCHAR(50),
    phone VARCHAR(20),
    email VARCHAR(100),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 2. 图书管理员表
CREATE TABLE IF NOT EXISTS librarian (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    real_name VARCHAR(50),
    phone VARCHAR(20),
    email VARCHAR(100),
    status SMALLINT DEFAULT 1,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 3. 读者表
CREATE TABLE IF NOT EXISTS reader (
    id INT AUTO_INCREMENT PRIMARY KEY,
    reader_id VARCHAR(20) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    real_name VARCHAR(50) NOT NULL,
    gender SMALLINT,
    phone VARCHAR(20),
    email VARCHAR(100),
    department VARCHAR(100),
    preferred_categories VARCHAR(255),
    language VARCHAR(10) DEFAULT 'zh_CN',
    max_borrow_count INT DEFAULT 5,
    current_borrow_count INT DEFAULT 0,
    fine_amount DECIMAL(10, 2) DEFAULT 0.00,
    status SMALLINT DEFAULT 1,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 4. 图书表
CREATE TABLE IF NOT EXISTS book (
    id INT AUTO_INCREMENT PRIMARY KEY,
    isbn VARCHAR(20) UNIQUE NOT NULL,
    title VARCHAR(255) NOT NULL,
    title_en VARCHAR(255),
    author VARCHAR(100),
    author_en VARCHAR(100),
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
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 5. 座位表
CREATE TABLE IF NOT EXISTS seat (
    id INT AUTO_INCREMENT PRIMARY KEY,
    seat_number VARCHAR(20) UNIQUE NOT NULL,
    area VARCHAR(50),
    status SMALLINT DEFAULT 0,
    device_id VARCHAR(50),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 6. 预约表
CREATE TABLE IF NOT EXISTS reservation (
    id INT AUTO_INCREMENT PRIMARY KEY,
    reader_id INT NOT NULL,
    book_id INT,
    seat_id INT,
    reservation_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status SMALLINT DEFAULT 0,
    expiry_date TIMESTAMP,
    preferred_time_slot VARCHAR(20),
    FOREIGN KEY (reader_id) REFERENCES reader(id),
    FOREIGN KEY (book_id) REFERENCES book(id),
    FOREIGN KEY (seat_id) REFERENCES seat(id)
);

-- 7. 借阅表
CREATE TABLE IF NOT EXISTS borrowing (
    id INT AUTO_INCREMENT PRIMARY KEY,
    reader_id INT NOT NULL,
    book_id INT NOT NULL,
    borrow_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    due_date TIMESTAMP NOT NULL,
    return_date TIMESTAMP,
    renew_count INT DEFAULT 0,
    status SMALLINT DEFAULT 1,
    fine_amount DECIMAL(10, 2) DEFAULT 0.00,
    FOREIGN KEY (reader_id) REFERENCES reader(id),
    FOREIGN KEY (book_id) REFERENCES book(id),
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 8. 排班表
CREATE TABLE IF NOT EXISTS scheduling (
    id INT AUTO_INCREMENT PRIMARY KEY,
    seat_id INT NOT NULL,
    reader_id INT NOT NULL,
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP NOT NULL,
    status SMALLINT DEFAULT 0,
    FOREIGN KEY (seat_id) REFERENCES seat(id),
    FOREIGN KEY (reader_id) REFERENCES reader(id)
);

-- 9. 系统参数配置表
CREATE TABLE IF NOT EXISTS system_config (
    id INT AUTO_INCREMENT PRIMARY KEY,
    config_key VARCHAR(100) UNIQUE NOT NULL,
    config_value VARCHAR(500) NOT NULL,
    description VARCHAR(200),
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 10. 操作日志表
CREATE TABLE IF NOT EXISTS operation_log (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT,
    username VARCHAR(50),
    role VARCHAR(20),
    module VARCHAR(50),
    action VARCHAR(50),
    detail TEXT,
    ip VARCHAR(50),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 11. 黑名单表
CREATE TABLE IF NOT EXISTS blacklist (
    id INT AUTO_INCREMENT PRIMARY KEY,
    reader_id INT NOT NULL,
    reason VARCHAR(200),
    violation_count INT DEFAULT 1,
    blacklisted SMALLINT DEFAULT 1,
    start_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    end_time TIMESTAMP,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (reader_id) REFERENCES reader(id)
);

-- 12. 读者等级积分表
CREATE TABLE IF NOT EXISTS reader_level (
    id INT AUTO_INCREMENT PRIMARY KEY,
    reader_id INT UNIQUE NOT NULL,
    points INT DEFAULT 0,
    level VARCHAR(20) DEFAULT '普通',
    total_borrow_count INT DEFAULT 0,
    total_return_on_time INT DEFAULT 0,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (reader_id) REFERENCES reader(id)
);

-- 13. 座位签到表
CREATE TABLE IF NOT EXISTS seat_checkin (
    id INT AUTO_INCREMENT PRIMARY KEY,
    seat_id INT NOT NULL,
    reader_id INT NOT NULL,
    reservation_id INT,
    checkin_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    checkout_time TIMESTAMP,
    status SMALLINT DEFAULT 1,
    FOREIGN KEY (seat_id) REFERENCES seat(id),
    FOREIGN KEY (reader_id) REFERENCES reader(id)
);

-- 14. 通知消息表
CREATE TABLE IF NOT EXISTS notification (
    id INT AUTO_INCREMENT PRIMARY KEY,
    reader_id INT NOT NULL,
    title VARCHAR(100) NOT NULL,
    content TEXT,
    type VARCHAR(20) DEFAULT 'system',
    is_read SMALLINT DEFAULT 0,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (reader_id) REFERENCES reader(id)
);

-- 15. 书评表
CREATE TABLE IF NOT EXISTS book_review (
    id INT AUTO_INCREMENT PRIMARY KEY,
    book_id INT NOT NULL,
    reader_id INT NOT NULL,
    content TEXT NOT NULL,
    rating INT NOT NULL,
    status SMALLINT DEFAULT 1,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (book_id) REFERENCES book(id),
    FOREIGN KEY (reader_id) REFERENCES reader(id)
);

-- 16. 书评点赞表
CREATE TABLE IF NOT EXISTS review_like (
    id INT AUTO_INCREMENT PRIMARY KEY,
    review_id INT NOT NULL,
    reader_id INT NOT NULL,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (review_id) REFERENCES book_review(id),
    FOREIGN KEY (reader_id) REFERENCES reader(id),
    UNIQUE (review_id, reader_id)
);

-- 17. 关注关系表
CREATE TABLE IF NOT EXISTS reader_follow (
    id INT AUTO_INCREMENT PRIMARY KEY,
    follower_id INT NOT NULL,
    followee_id INT NOT NULL,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (follower_id) REFERENCES reader(id),
    FOREIGN KEY (followee_id) REFERENCES reader(id),
    UNIQUE (follower_id, followee_id)
);

-- 18. 学习伙伴表
CREATE TABLE IF NOT EXISTS study_buddy (
    id INT AUTO_INCREMENT PRIMARY KEY,
    reader_id INT NOT NULL,
    preferred_slot VARCHAR(20),
    preferred_area VARCHAR(50),
    study_goal VARCHAR(200),
    status SMALLINT DEFAULT 1,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (reader_id) REFERENCES reader(id)
);

-- 19. 书评回复表
CREATE TABLE IF NOT EXISTS review_reply (
    id INT AUTO_INCREMENT PRIMARY KEY,
    review_id INT NOT NULL,
    reader_id INT NOT NULL,
    content TEXT NOT NULL,
    reply_to_reader_id INT,
    status SMALLINT DEFAULT 1,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (review_id) REFERENCES book_review(id),
    FOREIGN KEY (reader_id) REFERENCES reader(id),
    FOREIGN KEY (reply_to_reader_id) REFERENCES reader(id)
);

-- 插入测试用系统配置
INSERT INTO system_config (config_key, config_value, description) VALUES
('library.borrowing.default-days', '30', '默认借阅天数'),
('library.borrowing.renew-days', '30', '续借天数'),
('library.borrowing.max-renew-count', '2', '最大续借次数'),
('library.borrowing.renew-window-days', '7', '续借窗口期'),
('library.fine.daily-rate', '0.10', '日罚款率'),
('library.fine.grace-days', '3', '罚款宽限期'),
('library.reader.max-borrow-count', '5', '最大借阅数'),
('library.blacklist.violation-threshold', '3', '黑名单违约阈值'),
('library.seat.checkin-timeout-minutes', '30', '签到超时时间'),
('library.blacklist.cancel-threshold', '3', '一周取消预约次数阈值');
