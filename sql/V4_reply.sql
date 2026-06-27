-- ========================================
-- V4 书评回复功能（楼中楼）
-- ========================================

-- 书评回复表
CREATE TABLE IF NOT EXISTS review_reply (
    id INT PRIMARY KEY AUTO_INCREMENT,
    review_id INT NOT NULL COMMENT '关联书评ID',
    reader_id INT NOT NULL COMMENT '回复者ID',
    content TEXT NOT NULL COMMENT '回复内容',
    reply_to_reader_id INT DEFAULT NULL COMMENT '被回复者ID（一级回复为NULL，楼中楼回复指向被回复人）',
    status TINYINT DEFAULT 1 COMMENT '状态：0-已删除 1-正常',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (review_id) REFERENCES book_review(id),
    FOREIGN KEY (reader_id) REFERENCES reader(id),
    FOREIGN KEY (reply_to_reader_id) REFERENCES reader(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 索引
CREATE INDEX idx_review_reply_review_id ON review_reply(review_id);
CREATE INDEX idx_review_reply_reader_id ON review_reply(reader_id);
CREATE INDEX idx_review_reply_status ON review_reply(status);
