-- 智账 SmartLedger 数据库初始化脚本

-- 用户表
CREATE TABLE IF NOT EXISTS `user` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
    `email` varchar(100) NOT NULL COMMENT '邮箱',
    `password` varchar(255) NOT NULL COMMENT '加密密码',
    `nickname` varchar(50) DEFAULT NULL COMMENT '昵称',
    `avatar` varchar(255) DEFAULT NULL COMMENT '头像URL',
    `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_email` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 账本表
CREATE TABLE IF NOT EXISTS `book` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id` bigint NOT NULL COMMENT '用户ID',
    `name` varchar(50) NOT NULL COMMENT '账本名称',
    `icon` varchar(50) DEFAULT 'fa-book' COMMENT '图标',
    `description` varchar(200) DEFAULT NULL COMMENT '描述',
    `is_default` tinyint NOT NULL DEFAULT 0 COMMENT '是否默认账本',
    `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='账本表';

-- 分类表
CREATE TABLE IF NOT EXISTS `category` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id` bigint NOT NULL DEFAULT 0 COMMENT '用户ID(0为系统预设)',
    `name` varchar(50) NOT NULL COMMENT '分类名称',
    `icon` varchar(50) DEFAULT 'fa-tag' COMMENT '图标',
    `type` tinyint NOT NULL COMMENT '类型(1收入2支出)',
    `is_system` tinyint NOT NULL DEFAULT 0 COMMENT '是否系统预设',
    `sort_order` int NOT NULL DEFAULT 0 COMMENT '排序',
    `is_hidden` tinyint NOT NULL DEFAULT 0 COMMENT '是否隐藏',
    `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_type` (`user_id`, `type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='分类表';

-- 标签表
CREATE TABLE IF NOT EXISTS `tag` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id` bigint NOT NULL COMMENT '用户ID',
    `name` varchar(50) NOT NULL COMMENT '标签名称',
    `color` varchar(20) DEFAULT '#409EFF' COMMENT '颜色',
    `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='标签表';

-- 记录表
CREATE TABLE IF NOT EXISTS `transaction` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id` bigint NOT NULL COMMENT '用户ID',
    `book_id` bigint NOT NULL COMMENT '账本ID',
    `category_id` bigint NOT NULL COMMENT '分类ID',
    `type` tinyint NOT NULL COMMENT '类型(1收入2支出)',
    `amount` decimal(12,2) NOT NULL COMMENT '金额',
    `transaction_date` date NOT NULL COMMENT '交易日期',
    `remark` varchar(200) DEFAULT NULL COMMENT '备注',
    `payment_method` varchar(20) DEFAULT NULL COMMENT '支付方式',
    `images` text DEFAULT NULL COMMENT '图片JSON数组',
    `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_book` (`user_id`, `book_id`),
    KEY `idx_transaction_date` (`transaction_date`),
    KEY `idx_category_id` (`category_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='记录表';

-- 记录-标签关联表
CREATE TABLE IF NOT EXISTS `transaction_tag` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
    `transaction_id` bigint NOT NULL COMMENT '记录ID',
    `tag_id` bigint NOT NULL COMMENT '标签ID',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_transaction_tag` (`transaction_id`, `tag_id`),
    KEY `idx_tag_id` (`tag_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='记录-标签关联表';

-- 预算表
CREATE TABLE IF NOT EXISTS `budget` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id` bigint NOT NULL COMMENT '用户ID',
    `book_id` bigint NOT NULL COMMENT '账本ID',
    `category_id` bigint NOT NULL DEFAULT 0 COMMENT '分类ID(0表示总预算)',
    `year_month` varchar(7) NOT NULL COMMENT '年月(2024-01)',
    `amount` decimal(12,2) NOT NULL COMMENT '预算金额',
    `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_budget` (`user_id`, `book_id`, `category_id`, `year_month`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='预算表';

-- 操作日志表
CREATE TABLE IF NOT EXISTS `operation_log` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id` bigint NOT NULL COMMENT '用户ID',
    `operation_type` varchar(50) NOT NULL COMMENT '操作类型',
    `target_type` varchar(50) NOT NULL COMMENT '目标类型',
    `target_id` bigint DEFAULT NULL COMMENT '目标ID',
    `content` text DEFAULT NULL COMMENT '操作内容JSON',
    `ip_address` varchar(50) DEFAULT NULL COMMENT 'IP地址',
    `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作日志表';

-- ===================== 初始化数据 =====================

-- 系统预设支出分类
INSERT INTO `category` (`user_id`, `name`, `icon`, `type`, `is_system`, `sort_order`) VALUES
(0, '餐饮', 'fa-utensils', 2, 1, 1),
(0, '交通', 'fa-car', 2, 1, 2),
(0, '购物', 'fa-shopping-bag', 2, 1, 3),
(0, '娱乐', 'fa-gamepad', 2, 1, 4),
(0, '居家', 'fa-home', 2, 1, 5),
(0, '医疗', 'fa-hospital', 2, 1, 6),
(0, '教育', 'fa-graduation-cap', 2, 1, 7),
(0, '通讯', 'fa-phone', 2, 1, 8),
(0, '人情', 'fa-gift', 2, 1, 9),
(0, '其他', 'fa-ellipsis-h', 2, 1, 10);

-- 系统预设收入分类
INSERT INTO `category` (`user_id`, `name`, `icon`, `type`, `is_system`, `sort_order`) VALUES
(0, '工资', 'fa-money-bill', 1, 1, 1),
(0, '奖金', 'fa-trophy', 1, 1, 2),
(0, '投资', 'fa-chart-line', 1, 1, 3),
(0, '兼职', 'fa-briefcase', 1, 1, 4),
(0, '红包', 'fa-envelope', 1, 1, 5),
(0, '其他', 'fa-ellipsis-h', 1, 1, 6);

-- 测试用户 (密码: 123456，使用 BCrypt 加密)
-- $2a$10$AvBcNmwEtscI2Bp6IMManuXxVB1J4CSe5cC0PGapBGpm8CrU03HX.
INSERT INTO `user` (`id`, `email`, `password`, `nickname`, `created_at`) VALUES
(1, 'admin', '$2a$10$AvBcNmwEtscI2Bp6IMManuXxVB1J4CSe5cC0PGapBGpm8CrU03HX.', '超级管理员', '2025-01-01 00:00:00'),
(2, 'test@smartledger.com', '$2a$10$AvBcNmwEtscI2Bp6IMManuXxVB1J4CSe5cC0PGapBGpm8CrU03HX.', '测试用户', '2025-06-01 00:00:00');

-- 测试账本
INSERT INTO `book` (`id`, `user_id`, `name`, `icon`, `description`, `is_default`) VALUES
(1, 1, '日常账本', 'fa-home', '日常生活开支', 1),
(2, 1, '旅行基金', 'fa-plane', '2026年旅行计划', 0),
(3, 2, '我的账本', 'fa-book', NULL, 1);

-- 测试标签
INSERT INTO `tag` (`user_id`, `name`, `color`) VALUES
(1, '日常', '#409EFF'),
(1, '网购', '#E6A23C'),
(1, '必需品', '#67C23A'),
(2, '日常', '#409EFF');

-- 测试记录数据 (admin 用户 30 天的记录)
INSERT INTO `transaction` (`user_id`, `book_id`, `category_id`, `type`, `amount`, `transaction_date`, `remark`, `payment_method`) VALUES
-- 今天
(1, 1, 1, 2, 35.50, CURDATE(), '午餐', '微信'),
(1, 1, 1, 2, 28.00, CURDATE(), '早餐+咖啡', '支付宝'),
-- 昨天
(1, 1, 2, 2, 15.00, DATE_SUB(CURDATE(), INTERVAL 1 DAY), '地铁', '微信'),
(1, 1, 1, 2, 68.00, DATE_SUB(CURDATE(), INTERVAL 1 DAY), '晚餐聚会', '支付宝'),
-- 前天
(1, 1, 3, 2, 199.00, DATE_SUB(CURDATE(), INTERVAL 2 DAY), '网购日用品', '支付宝'),
(1, 1, 1, 2, 42.00, DATE_SUB(CURDATE(), INTERVAL 2 DAY), '外卖', '微信'),
-- 3天前
(1, 1, 4, 2, 88.00, DATE_SUB(CURDATE(), INTERVAL 3 DAY), '电影票', '微信'),
-- 5天前 - 工资收入
(1, 1, 11, 1, 15000.00, DATE_SUB(CURDATE(), INTERVAL 5 DAY), '12月工资', '银行卡'),
-- 一周前
(1, 1, 5, 2, 350.00, DATE_SUB(CURDATE(), INTERVAL 7 DAY), '水电费', '支付宝'),
(1, 1, 2, 2, 120.00, DATE_SUB(CURDATE(), INTERVAL 7 DAY), '加油', '银行卡'),
-- 两周前
(1, 1, 3, 2, 588.00, DATE_SUB(CURDATE(), INTERVAL 14 DAY), '衣服', '支付宝'),
(1, 1, 6, 2, 280.00, DATE_SUB(CURDATE(), INTERVAL 14 DAY), '感冒药+诊费', '微信'),
-- 三周前
(1, 1, 1, 2, 258.00, DATE_SUB(CURDATE(), INTERVAL 21 DAY), '火锅', '微信'),
(1, 1, 9, 2, 500.00, DATE_SUB(CURDATE(), INTERVAL 21 DAY), '朋友生日礼物', '微信'),
-- 月初
(1, 1, 8, 2, 198.00, DATE_SUB(CURDATE(), INTERVAL 25 DAY), '话费充值', '支付宝'),
(1, 1, 12, 1, 2000.00, DATE_SUB(CURDATE(), INTERVAL 25 DAY), '年终奖', '银行卡');

-- 测试预算
INSERT INTO `budget` (`user_id`, `book_id`, `category_id`, `year_month`, `amount`) VALUES
(1, 1, 0, DATE_FORMAT(CURDATE(), '%Y-%m'), 5000.00),
(1, 1, 1, DATE_FORMAT(CURDATE(), '%Y-%m'), 1500.00),
(1, 1, 3, DATE_FORMAT(CURDATE(), '%Y-%m'), 1000.00);
