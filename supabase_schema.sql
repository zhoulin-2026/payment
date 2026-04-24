-- Supabase PostgreSQL 建表语句（与实体类完全匹配）
-- 执行前请先删除现有表（如有）：DROP TABLE IF EXISTS pay_order, pay_qrcode, tmp_price, setting CASCADE;

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
    pay_url TEXT
);

-- 创建索引（提升查询性能）
CREATE INDEX IF NOT EXISTS idx_pay_order_pay_id ON pay_order(pay_id);
CREATE INDEX IF NOT EXISTS idx_pay_order_order_id ON pay_order(order_id);
CREATE INDEX IF NOT EXISTS idx_pay_order_create_date ON pay_order(create_date);
CREATE INDEX IF NOT EXISTS idx_pay_order_state ON pay_order(state);
CREATE INDEX IF NOT EXISTS idx_pay_order_type ON pay_order(type);
CREATE INDEX IF NOT EXISTS idx_pay_qrcode_type ON pay_qrcode(type);

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
