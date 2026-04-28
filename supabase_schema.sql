-- Supabase PostgreSQL 建表语句（与实体类完全匹配）
-- 执行前请先删除现有表（如有）：DROP TABLE IF EXISTS pay_order, pay_qrcode, tmp_price, setting, member_type, member_info, member_device CASCADE;

-- 1. tmp_price 表
CREATE TABLE IF NOT EXISTS tmp_price (
    price VARCHAR(255) PRIMARY KEY
);

-- 2. setting 表
CREATE TABLE IF NOT EXISTS setting (
    vkey VARCHAR(255) PRIMARY KEY,
    vvalue TEXT
);

-- 3. pay_qrcode 表
CREATE TABLE IF NOT EXISTS pay_qrcode (
    id BIGSERIAL PRIMARY KEY,
    pay_url TEXT,
    price DOUBLE PRECISION,
    type INTEGER
);

-- 4. pay_order 表
CREATE TABLE IF NOT EXISTS pay_order (
    id BIGSERIAL PRIMARY KEY,
    order_id VARCHAR(255),
    pay_id VARCHAR(255),
    create_date BIGINT,
    pay_date BIGINT,
    close_date BIGINT,
    param TEXT,
    type INTEGER,
    price DOUBLE PRECISION,
    really_price DOUBLE PRECISION,
    notify_url TEXT,
    return_url TEXT,
    state INTEGER,
    is_auto INTEGER,
    pay_url TEXT,
    member_type_id BIGINT
);

-- 创建索引（提升查询性能）
CREATE INDEX IF NOT EXISTS idx_pay_order_pay_id ON pay_order(pay_id);
CREATE INDEX IF NOT EXISTS idx_pay_order_order_id ON pay_order(order_id);
CREATE INDEX IF NOT EXISTS idx_pay_order_create_date ON pay_order(create_date);
CREATE INDEX IF NOT EXISTS idx_pay_order_state ON pay_order(state);
CREATE INDEX IF NOT EXISTS idx_pay_order_type ON pay_order(type);
CREATE INDEX IF NOT EXISTS idx_pay_qrcode_type ON pay_qrcode(type);
CREATE INDEX IF NOT EXISTS idx_pay_order_member_type_id ON pay_order(member_type_id);

-- 5. member_type 表（会员类型）
CREATE TABLE IF NOT EXISTS member_type (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    type_code VARCHAR(20) NOT NULL,
    duration_days INTEGER,
    max_devices INTEGER DEFAULT 1,
    total_uses INTEGER,
    price DOUBLE PRECISION NOT NULL,
    description TEXT,
    is_active BOOLEAN DEFAULT TRUE,
    create_time BIGINT,
    update_time BIGINT
);

CREATE INDEX IF NOT EXISTS idx_member_type_code ON member_type(type_code);
CREATE INDEX IF NOT EXISTS idx_member_type_active ON member_type(is_active);

-- 6. member_info 表（会员信息）
CREATE TABLE IF NOT EXISTS member_info (
    id BIGSERIAL PRIMARY KEY,
    member_code VARCHAR(64) UNIQUE NOT NULL,
    member_type_id BIGINT NOT NULL,
    order_id VARCHAR(64),
    pay_id VARCHAR(64),
    user_identifier TEXT,
    activate_time BIGINT,
    expire_time BIGINT,
    remaining_uses INTEGER,
    status INTEGER DEFAULT 0,
    devices_used INTEGER DEFAULT 0,
    last_use_time BIGINT,
    create_time BIGINT
);

CREATE INDEX IF NOT EXISTS idx_member_info_code ON member_info(member_code);
CREATE INDEX IF NOT EXISTS idx_member_info_order_id ON member_info(order_id);
CREATE INDEX IF NOT EXISTS idx_member_info_pay_id ON member_info(pay_id);
CREATE INDEX IF NOT EXISTS idx_member_info_status ON member_info(status);

-- 7. member_device 表（会员设备）
CREATE TABLE IF NOT EXISTS member_device (
    id BIGSERIAL PRIMARY KEY,
    member_code VARCHAR(64) NOT NULL,
    device_id VARCHAR(128) NOT NULL,
    first_use_time BIGINT,
    last_use_time BIGINT,
    use_count INTEGER DEFAULT 1
);

CREATE INDEX IF NOT EXISTS idx_member_device_code ON member_device(member_code);
CREATE UNIQUE INDEX IF NOT EXISTS idx_member_device_code_device ON member_device(member_code, device_id);

-- 初始化 setting 表基础数据
INSERT INTO setting (vkey, vvalue) VALUES ('key', '') ON CONFLICT (vkey) DO NOTHING;
INSERT INTO setting (vkey, vvalue) VALUES ('user', 'admin') ON CONFLICT (vkey) DO NOTHING;
INSERT INTO setting (vkey, vvalue) VALUES ('pass', 'admin123') ON CONFLICT (vkey) DO NOTHING;
INSERT INTO setting (vkey, vvalue) VALUES ('close', '30') ON CONFLICT (vkey) DO NOTHING;
INSERT INTO setting (vkey, vvalue) VALUES ('lastheart', '0') ON CONFLICT (vkey) DO NOTHING;
INSERT INTO setting (vkey, vvalue) VALUES ('jkstate', '0') ON CONFLICT (vkey) DO NOTHING;
INSERT INTO setting (vkey, vvalue) VALUES ('payQf', '1') ON CONFLICT (vkey) DO NOTHING;
INSERT INTO setting (vkey, vvalue) VALUES ('wxpay', '') ON CONFLICT (vkey) DO NOTHING;
INSERT INTO setting (vkey, vvalue) VALUES ('zfbpay', '') ON CONFLICT (vkey) DO NOTHING;
INSERT INTO setting (vkey, vvalue) VALUES ('notifyUrl', '') ON CONFLICT (vkey) DO NOTHING;
INSERT INTO setting (vkey, vvalue) VALUES ('returnUrl', '') ON CONFLICT (vkey) DO NOTHING;
INSERT INTO setting (vkey, vvalue) VALUES ('lastpay', '0') ON CONFLICT (vkey) DO NOTHING;

-- 初始化会员类型示例数据（可根据需要修改）
INSERT INTO member_type (name, type_code, duration_days, max_devices, total_uses, price, description, is_active, create_time, update_time) VALUES
('单次解锁', 'TIMES', 0, 3, 1, 4.90, '单次解锁', TRUE, EXTRACT(EPOCH FROM NOW()) * 1000, EXTRACT(EPOCH FROM NOW()) * 1000),
('日卡', 'DAY', 1, 2, NULL, 9.90, '1天有效期，最多2台设备', TRUE, EXTRACT(EPOCH FROM NOW()) * 1000, EXTRACT(EPOCH FROM NOW()) * 1000),
('月卡', 'MONTH', 30, 3, NULL, 99.00, '30天有效期，最多3台设备', TRUE, EXTRACT(EPOCH FROM NOW()) * 1000, EXTRACT(EPOCH FROM NOW()) * 1000),
('季卡', 'QUARTER', 90, 3, NULL, 259.00, '90天有效期，最多3台设备', TRUE, EXTRACT(EPOCH FROM NOW()) * 1000, EXTRACT(EPOCH FROM NOW()) * 1000),
('年卡', 'YEAR', 365, 5, NULL, 899.00, '365天有效期，最多5台设备', TRUE, EXTRACT(EPOCH FROM NOW()) * 1000, EXTRACT(EPOCH FROM NOW()) * 1000),
('终身卡', 'LIFETIME', 99999, 10, NULL, 2999.00, '永久有效，最多10台设备', TRUE, EXTRACT(EPOCH FROM NOW()) * 1000, EXTRACT(EPOCH FROM NOW()) * 1000)
ON CONFLICT DO NOTHING;
