-- ========================================
-- RBAC 权限控制表 (v5.4)
-- ========================================

-- 1. 角色表
CREATE TABLE IF NOT EXISTS sys_role (
    id INT PRIMARY KEY AUTO_INCREMENT,
    role_key VARCHAR(50) UNIQUE NOT NULL COMMENT '角色标识：SUPER_ADMIN/LIBRARIAN/READER',
    role_name VARCHAR(50) NOT NULL COMMENT '角色名称：超级管理员/图书管理员/读者',
    description VARCHAR(200),
    status TINYINT DEFAULT 1 COMMENT '1-启用 0-禁用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) COMMENT '角色表';

-- 2. 权限表
CREATE TABLE IF NOT EXISTS sys_permission (
    id INT PRIMARY KEY AUTO_INCREMENT,
    perm_key VARCHAR(100) UNIQUE NOT NULL COMMENT '权限标识：book:create, borrow:return',
    perm_name VARCHAR(100) NOT NULL COMMENT '权限名称：创建图书、归还图书',
    module VARCHAR(50) NOT NULL COMMENT '所属模块：book/borrow/seat/reader/config/log',
    description VARCHAR(200),
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
) COMMENT '权限表';

-- 3. 用户-角色关联表（reader/admin/librarian 共用）
CREATE TABLE IF NOT EXISTS sys_user_role (
    id INT PRIMARY KEY AUTO_INCREMENT,
    user_type VARCHAR(20) NOT NULL COMMENT '用户类型：READER/ADMIN/LIBRARIAN',
    user_id INT NOT NULL COMMENT '用户ID（对应 reader.id/admin.id/librarian.id）',
    role_id INT NOT NULL COMMENT '角色ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_role (user_type, user_id, role_id),
    FOREIGN KEY (role_id) REFERENCES sys_role(id)
) COMMENT '用户-角色关联表';

-- 4. 角色-权限关联表
CREATE TABLE IF NOT EXISTS sys_role_permission (
    id INT PRIMARY KEY AUTO_INCREMENT,
    role_id INT NOT NULL,
    permission_id INT NOT NULL,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_role_perm (role_id, permission_id),
    FOREIGN KEY (role_id) REFERENCES sys_role(id),
    FOREIGN KEY (permission_id) REFERENCES sys_permission(id)
) COMMENT '角色-权限关联表';

-- ========================================
-- 初始化数据
-- ========================================

-- 角色
INSERT INTO sys_role (role_key, role_name, description) VALUES
    ('SUPER_ADMIN', '超级管理员', '系统最高权限，管理所有功能'),
    ('LIBRARIAN', '图书管理员', '负责图书借还、预约审批、罚款处理'),
    ('READER', '读者', '普通读者，可借书、预约座位、发表书评');

-- 权限（按模块分组）
-- 图书管理
INSERT INTO sys_permission (perm_key, perm_name, module) VALUES
    ('book:view', '查看图书', 'book'),
    ('book:create', '创建图书', 'book'),
    ('book:update', '编辑图书', 'book'),
    ('book:delete', '删除图书', 'book'),
    ('book:import', '导入图书', 'book'),
    ('book:export', '导出图书', 'book');

-- 借阅管理
INSERT INTO sys_permission (perm_key, perm_name, module) VALUES
    ('borrow:view', '查看借阅记录', 'borrow'),
    ('borrow:create', '借书', 'borrow'),
    ('borrow:return', '还书', 'borrow'),
    ('borrow:renew', '续借', 'borrow'),
    ('borrow:approve', '审批借阅', 'borrow'),
    ('borrow:pay_fine', '缴纳罚款', 'borrow');

-- 座位管理
INSERT INTO sys_permission (perm_key, perm_name, module) VALUES
    ('seat:view', '查看座位', 'seat'),
    ('seat:reserve', '预约座位', 'seat'),
    ('seat:checkin', '座位签到', 'seat'),
    ('seat:manage', '管理座位状态', 'seat');

-- 读者管理
INSERT INTO sys_permission (perm_key, perm_name, module) VALUES
    ('reader:view', '查看读者', 'reader'),
    ('reader:update', '编辑读者', 'reader'),
    ('reader:ban', '禁用读者', 'reader'),
    ('reader:reset_password', '重置密码', 'reader'),
    ('reader:view_blacklist', '查看黑名单', 'reader'),
    ('reader:manage_blacklist', '管理黑名单', 'reader');

-- 书评管理
INSERT INTO sys_permission (perm_key, perm_name, module) VALUES
    ('review:view', '查看书评', 'review'),
    ('review:create', '发表书评', 'review'),
    ('review:delete', '删除书评', 'review'),
    ('review:reply', '回复书评', 'review');

-- 系统管理
INSERT INTO sys_permission (perm_key, perm_name, module) VALUES
    ('config:view', '查看系统配置', 'config'),
    ('config:update', '修改系统配置', 'config'),
    ('log:view', '查看操作日志', 'log'),
    ('log:export', '导出日志', 'log'),
    ('stats:view', '查看数据统计', 'stats'),
    ('export:data', '导出数据', 'export');

-- 用户管理
INSERT INTO sys_permission (perm_key, perm_name, module) VALUES
    ('user:view', '查看用户', 'user'),
    ('user:create', '创建用户', 'user'),
    ('user:update', '编辑用户', 'user'),
    ('user:delete', '删除用户', 'user');

-- ========================================
-- 角色-权限分配
-- ========================================

-- 超级管理员：全部权限
INSERT INTO sys_role_permission (role_id, permission_id)
SELECT r.id, p.id
FROM sys_role r, sys_permission p
WHERE r.role_key = 'SUPER_ADMIN';

-- 图书管理员：图书管理 + 借阅管理 + 座位管理 + 书评查看
INSERT INTO sys_role_permission (role_id, permission_id)
SELECT r.id, p.id
FROM sys_role r, sys_permission p
WHERE r.role_key = 'LIBRARIAN'
AND p.perm_key IN (
    'book:view', 'book:create', 'book:update', 'book:import', 'book:export',
    'borrow:view', 'borrow:create', 'borrow:return', 'borrow:renew', 'borrow:approve', 'borrow:pay_fine',
    'seat:view', 'seat:manage',
    'reader:view', 'reader:update',
    'review:view',
    'stats:view', 'export:data'
);

-- 读者：借阅 + 座位预约 + 书评 + 查看自己的数据
INSERT INTO sys_role_permission (role_id, permission_id)
SELECT r.id, p.id
FROM sys_role r, sys_permission p
WHERE r.role_key = 'READER'
AND p.perm_key IN (
    'book:view',
    'borrow:view', 'borrow:create', 'borrow:renew',
    'seat:view', 'seat:reserve', 'seat:checkin',
    'review:view', 'review:create', 'review:reply',
    'reader:view'
);

-- ========================================
-- 为现有用户分配默认角色
-- ========================================

-- admin 用户 -> SUPER_ADMIN
INSERT INTO sys_user_role (user_type, user_id, role_id)
SELECT 'ADMIN', a.id, r.id
FROM admin a, sys_role r
WHERE a.username = 'admin' AND r.role_key = 'SUPER_ADMIN'
ON DUPLICATE KEY UPDATE role_id = r.id;

-- librarian 用户 -> LIBRARIAN
INSERT INTO sys_user_role (user_type, user_id, role_id)
SELECT 'LIBRARIAN', l.id, r.id
FROM librarian l, sys_role r
WHERE l.username = 'librarian' AND r.role_key = 'LIBRARIAN'
ON DUPLICATE KEY UPDATE role_id = r.id;

-- 所有读者 -> READER
INSERT INTO sys_user_role (user_type, user_id, role_id)
SELECT 'READER', rd.id, r.id
FROM reader rd, sys_role r
WHERE r.role_key = 'READER'
ON DUPLICATE KEY UPDATE role_id = r.id;
