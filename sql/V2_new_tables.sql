-- ========================================
-- 图书馆管理系统 V2 - 新增功能模块表
-- ========================================

USE library_system;

-- 1. 系统参数配置表
CREATE TABLE IF NOT EXISTS system_config (
    id INT PRIMARY KEY AUTO_INCREMENT,
    config_key VARCHAR(100) UNIQUE NOT NULL,
    config_value VARCHAR(500) NOT NULL,
    description VARCHAR(200),
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 2. 操作日志表
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

-- 3. 黑名单表
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

-- 4. 读者等级积分表
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

-- 5. 座位签到表
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

-- 6. 通知消息表
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

-- ========================================
-- 索引
-- ========================================
ALTER TABLE operation_log ADD INDEX idx_oplog_user_id (user_id);
ALTER TABLE operation_log ADD INDEX idx_oplog_module (module);
ALTER TABLE operation_log ADD INDEX idx_oplog_create_time (create_time);
ALTER TABLE blacklist ADD INDEX idx_blacklist_reader_id (reader_id);
ALTER TABLE reader_level ADD INDEX idx_reader_level_reader_id (reader_id);
ALTER TABLE seat_checkin ADD INDEX idx_seat_checkin_reader_id (reader_id);
ALTER TABLE seat_checkin ADD INDEX idx_seat_checkin_seat_id (seat_id);
ALTER TABLE notification ADD INDEX idx_notification_reader_id (reader_id);
ALTER TABLE notification ADD INDEX idx_notification_is_read (is_read);

-- ========================================
-- 插入系统参数默认数据
-- ========================================
INSERT INTO system_config (config_key, config_value, description) VALUES
('library.name', '智慧图书馆', '图书馆名称'),
('library.borrowing.default-days', '30', '默认借阅天数'),
('library.borrowing.renew-days', '30', '续借天数'),
('library.borrowing.max-renew-count', '2', '最大续借次数'),
('library.reader.max-borrow-count', '5', '读者最大借阅数量'),
('library.fine.daily-rate', '0.10', '逾期罚款每日费率（元）'),
('library.seat.auto-release-minutes', '30', '签到超时自动释放时间（分钟）'),
('library.blacklist.violation-threshold', '3', '违规次数达到此值自动加入黑名单'),
('library.notification.borrow-remind-days', '3', '借阅到期前提醒天数');
