-- ========================================
-- 图书馆管理系统 V3 - 创新功能模块
-- 包含5个创新方向：AI智能荐书、社交书评、
-- 大数据预测、国际化、智能座位
-- ========================================

USE library_system;

-- ========================================
-- 方向1：AI智能荐书
-- 为读者添加偏好的图书分类，用于推荐算法
-- ========================================
ALTER TABLE reader
    ADD COLUMN preferred_categories VARCHAR(500) DEFAULT NULL
    COMMENT '偏好图书分类（JSON数组格式，如["计算机","文学"]）';

-- ========================================
-- 方向2：社交书评
-- 读者可以对图书撰写书评、点赞、互相关注
-- ========================================

-- 图书书评表
CREATE TABLE IF NOT EXISTS book_review (
    id INT PRIMARY KEY AUTO_INCREMENT,
    reader_id INT NOT NULL COMMENT '评论者ID',
    book_id INT NOT NULL COMMENT '图书ID',
    rating TINYINT NOT NULL COMMENT '评分（1-5星）',
    title VARCHAR(100) DEFAULT NULL COMMENT '评论标题',
    content TEXT COMMENT '评论内容',
    status TINYINT DEFAULT 1 COMMENT '状态：0-隐藏 1-正常',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (reader_id) REFERENCES reader(id),
    FOREIGN KEY (book_id) REFERENCES book(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='图书书评表';

-- 书评点赞表
CREATE TABLE IF NOT EXISTS review_like (
    id INT PRIMARY KEY AUTO_INCREMENT,
    review_id INT NOT NULL COMMENT '书评ID',
    reader_id INT NOT NULL COMMENT '点赞者ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '点赞时间',
    FOREIGN KEY (review_id) REFERENCES book_review(id),
    FOREIGN KEY (reader_id) REFERENCES reader(id),
    UNIQUE KEY uk_review_reader (review_id, reader_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='书评点赞表';

-- 读者关注表
CREATE TABLE IF NOT EXISTS reader_follow (
    id INT PRIMARY KEY AUTO_INCREMENT,
    follower_id INT NOT NULL COMMENT '关注者ID',
    following_id INT NOT NULL COMMENT '被关注者ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '关注时间',
    FOREIGN KEY (follower_id) REFERENCES reader(id),
    FOREIGN KEY (following_id) REFERENCES reader(id),
    UNIQUE KEY uk_follower_following (follower_id, following_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='读者关注表';

-- 社交书评相关索引
ALTER TABLE book_review ADD INDEX idx_review_reader_id (reader_id);
ALTER TABLE book_review ADD INDEX idx_review_book_id (book_id);
ALTER TABLE book_review ADD INDEX idx_review_rating (rating);
ALTER TABLE book_review ADD INDEX idx_review_create_time (create_time);
ALTER TABLE review_like ADD INDEX idx_rlike_reader_id (reader_id);
ALTER TABLE reader_follow ADD INDEX idx_rfollow_follower_id (follower_id);
ALTER TABLE reader_follow ADD INDEX idx_rfollow_following_id (following_id);

-- ========================================
-- 方向3：大数据预测
-- 无需新建表，利用现有 borrowing、reservation、
-- reader_level 等表进行借阅趋势预测和热门分析
-- ========================================

-- ========================================
-- 方向4：国际化
-- 为读者添加语言偏好，为图书添加英文信息
-- ========================================
ALTER TABLE reader
    ADD COLUMN language VARCHAR(20) DEFAULT 'zh_CN'
    COMMENT '界面语言偏好（如 zh_CN、en_US）';

ALTER TABLE book
    ADD COLUMN title_en VARCHAR(255) DEFAULT NULL
    COMMENT '英文书名',
    ADD COLUMN author_en VARCHAR(100) DEFAULT NULL
    COMMENT '英文作者名',
    ADD COLUMN description_en TEXT DEFAULT NULL
    COMMENT '英文简介';

-- ========================================
-- 方向5：智能座位
-- 座位预约增加时段偏好，支持学习搭子功能
-- ========================================
ALTER TABLE reservation
    ADD COLUMN preferred_time_slot VARCHAR(50) DEFAULT NULL
    COMMENT '偏好时段（如 08:00-12:00, 14:00-18:00）';

-- 学习搭子表（寻找相同学习时段/兴趣的伙伴）
CREATE TABLE IF NOT EXISTS study_buddy (
    id INT PRIMARY KEY AUTO_INCREMENT,
    reader_id INT NOT NULL COMMENT '读者ID',
    preferred_slot VARCHAR(50) DEFAULT NULL COMMENT '偏好学习时段',
    preferred_area VARCHAR(50) DEFAULT NULL COMMENT '偏好区域',
    study_goal VARCHAR(200) DEFAULT NULL COMMENT '学习目标',
    status TINYINT DEFAULT 1 COMMENT '状态：0-已关闭 1-开放匹配',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (reader_id) REFERENCES reader(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='学习搭子表';

-- 智能座位相关索引
ALTER TABLE reservation ADD INDEX idx_reservation_time_slot (preferred_time_slot);
ALTER TABLE study_buddy ADD INDEX idx_buddy_reader_id (reader_id);
ALTER TABLE study_buddy ADD INDEX idx_buddy_area (preferred_area);

-- ========================================
-- system_config 新增参数
-- ========================================
INSERT INTO system_config (config_key, config_value, description) VALUES
('library.recommend.enabled', 'true', '是否启用AI智能荐书功能'),
('library.recommend.weight_borrow_history', '0.4', '借阅历史在推荐算法中的权重'),
('library.recommend.weight_preferred_categories', '0.3', '偏好分类在推荐算法中的权重'),
('library.recommend.weight_hot_books', '0.2', '热门图书在推荐算法中的权重'),
('library.recommend.weight_peer_rating', '0.1', '同伴评分在推荐算法中的权重'),
('library.review.min_words', '10', '书评最少字数要求'),
('library.review.auto_approve', 'true', '书评是否自动审核通过'),
('library.i18n.default_language', 'zh_CN', '系统默认语言'),
('library.i18n.supported_languages', 'zh_CN,en_US', '支持的语言列表'),
('library.seat.buddy_matching_enabled', 'true', '是否启用学习搭子匹配功能'),
('library.seat.time_slot_interval', '2', '座位预约时段间隔（小时）'),
('library.borrowing.renew-window-days', '7', '续借窗口期（到期前X天内才可续借）'),
('library.fine.grace-days', '3', '罚款宽限期（逾期X天内免罚）');
