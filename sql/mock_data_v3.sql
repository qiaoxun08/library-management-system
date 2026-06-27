-- ========================================
-- 图书馆管理系统 V3 - 创新功能模拟数据
-- 需先执行 init.sql、V2_new_tables.sql、mock_data.sql
-- 本脚本可重复执行（使用 INSERT IGNORE / UPDATE）
-- ========================================

USE library_system;

-- ========================================
-- 方向1：AI智能荐书 - 设置读者偏好分类
-- ========================================
UPDATE reader SET preferred_categories = '["计算机","文学"]'
WHERE reader_id = '2024001';

UPDATE reader SET preferred_categories = '["经济","历史"]'
WHERE reader_id = '2024002';

-- ========================================
-- 方向4：国际化 - 设置读者语言偏好
-- 所有现有读者默认中文
-- ========================================
UPDATE reader SET language = 'zh_CN';

-- 特定读者设置英文界面
UPDATE reader SET language = 'en_US'
WHERE reader_id = '2024003';

-- 为部分图书补充英文信息
UPDATE book SET
    title_en = 'Thinking in Java',
    author_en = 'Bruce Eckel',
    description_en = 'A comprehensive introduction to Java programming language and object-oriented programming.'
WHERE isbn = '9787115546081';

UPDATE book SET
    title_en = 'Introduction to Algorithms',
    author_en = 'Thomas H. Cormen',
    description_en = 'A comprehensive textbook covering a broad range of algorithms in depth.'
WHERE isbn = '9787115428028';

UPDATE book SET
    title_en = 'Dream of the Red Chamber',
    author_en = 'Cao Xueqin',
    description_en = 'One of the Four Great Classical Novels of Chinese literature.'
WHERE isbn = '9787020002207';

UPDATE book SET
    title_en = 'Computer Networks',
    author_en = 'Andrew S. Tanenbaum',
    description_en = 'A classic textbook on computer network architecture and protocols.'
WHERE isbn = '9787115517227';

UPDATE book SET
    title_en = 'Data Structures and Algorithm Analysis',
    author_en = 'Mark Allen Weiss',
    description_en = 'A comprehensive treatment of data structures and algorithm analysis in C/C++/Java.'
WHERE isbn = '9787115428029';

-- ========================================
-- 方向2：社交书评 - 插入书评数据
-- reader.id=1 对应 reader_id='2024001'
-- reader.id=2 对应 reader_id='2024002'
-- ========================================
INSERT IGNORE INTO book_review (reader_id, book_id, rating, title, content) VALUES
(1, 1, 5, 'Java经典入门必读',
 '这本书对Java面向对象的思想讲解得非常透彻，适合有一定编程基础的读者深入学习。每章的练习题也很有质量。'),
(1, 12, 4, '中国古典文学巅峰之作',
 '曹雪芹笔下的人物刻画入木三分，虽然有些地方读起来比较晦涩，但整体来说是一部不可多得的文学巨著。'),
(1, 9, 5, '机器学习入门好书',
 '周志华教授的这本书理论与实践结合得很好，数学推导清晰，适合想入门机器学习的同学。');

-- ========================================
-- 方向2：社交书评 - 插入点赞数据
-- ========================================
INSERT IGNORE INTO review_like (review_id, reader_id) VALUES
(1, 2),
(2, 2),
(3, 2);

-- ========================================
-- 方向2：社交书评 - 插入读者关注数据
-- 2024001 关注 2024002
-- ========================================
INSERT IGNORE INTO reader_follow (follower_id, following_id) VALUES
(1, 2);

-- ========================================
-- 方向5：智能座位 - 设置预约时段偏好
-- ========================================
UPDATE reservation SET preferred_time_slot = '08:00-12:00'
WHERE reader_id = 1 AND seat_id IS NOT NULL AND status = 0;

UPDATE reservation SET preferred_time_slot = '14:00-18:00'
WHERE reader_id = 3 AND seat_id IS NOT NULL AND status = 0;

-- ========================================
-- 方向5：智能座位 - 插入学习搭子数据
-- ========================================
INSERT IGNORE INTO study_buddy (reader_id, preferred_slot, preferred_area, study_goal, status) VALUES
(1, '08:00-12:00', '阅览室A', '准备期末考试，需要安静环境', 1),
(2, '14:00-18:00', '自习室D', '考研复习，寻找一起学习的伙伴', 1),
(3, '08:00-12:00', '电子阅览室', '学习编程，寻找编程爱好者', 1);
