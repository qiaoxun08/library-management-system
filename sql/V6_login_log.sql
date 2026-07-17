CREATE TABLE IF NOT EXISTS login_log (
    id INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL COMMENT '用户名',
    role VARCHAR(20) NOT NULL COMMENT '角色：admin/librarian/reader',
    login_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '登录时间',
    ip VARCHAR(50) COMMENT 'IP地址',
    user_agent VARCHAR(500) COMMENT '浏览器UA',
    status TINYINT DEFAULT 1 COMMENT '1-成功 0-失败',
    fail_reason VARCHAR(200) COMMENT '失败原因',
    INDEX idx_login_time (login_time),
    INDEX idx_username (username)
) COMMENT '登录审计日志表';
