# 会员系统使用说明

## 功能概述

本系统支持在支付订单中集成会员功能，主要特性包括：

1. **多种会员类型**：次卡、日卡、月卡、季卡、年卡、终身卡
2. **设备限制**：每个会员码可限制在指定数量的设备上使用
3. **自动激活**：首次使用时自动激活会员
4. **回调通知**：支付成功后自动在回调中携带会员码信息
5. **向后兼容**：会员类型为可选参数，不影响现有订单流程

## 数据库初始化

执行 `supabase_schema.sql` 文件创建所有必要的表和索引：

```bash
psql -U your_user -d your_database -f supabase_schema.sql
```

这将创建以下表：
- `member_type` - 会员类型定义表
- `member_info` - 会员信息表
- `member_device` - 会员设备绑定表

并初始化示例会员类型数据。

## API 接口说明

### 1. 获取会员类型列表

**接口地址**：`/getMemberTypes`

**请求参数**：无

**返回示例**：
```json
{
  "code": 1,
  "msg": "成功",
  "data": [
    {
      "id": 1,
      "name": "次卡-10次",
      "typeCode": "TIMES",
      "price": 50.00,
      "maxDevices": 3,
      "description": "10次使用次数，最多3台设备",
      "durationText": "10次使用",
      "isTimeBased": false
    },
    {
      "id": 3,
      "name": "月卡",
      "typeCode": "MONTH",
      "price": 99.00,
      "maxDevices": 3,
      "description": "30天有效期，最多3台设备",
      "durationText": "30天",
      "isTimeBased": true
    }
  ]
}
```

**字段说明**：
- `id` - 会员类型ID（创建订单时使用）
- `name` - 会员类型名称
- `typeCode` - 类型代码（TIMES/DAY/MONTH/QUARTER/YEAR/LIFETIME）
- `price` - 价格
- `maxDevices` - 最大设备数
- `description` - 描述信息
- `durationText` - 显示用的时长文本
- `isTimeBased` - 是否为时间卡（false表示次卡）

### 2. 创建订单（带会员类型）

**接口地址**：`/createOrder`

**请求参数**：
- `payId` - 商户订单号（必填）
- `param` - 订单参数（可选）
- `type` - 支付方式：1=微信，2=支付宝（必填）
- `price` - 订单价格（必填）
- `notifyUrl` - 异步通知地址（可选）
- `returnUrl` - 同步跳转地址（可选）
- `sign` - 签名（必填）
- `isHtml` - 返回格式：0=JSON，1=HTML（可选）
- `memberType` - **会员类型代码（可选）**：TIMES/DAY/MONTH/QUARTER/YEAR/LIFETIME

**示例请求**：
```
GET /createOrder?payId=ORDER123&param=test&type=1&price=99.00&notifyUrl=http://example.com/notify&sign=xxx&memberType=MONTH
```

**说明**：
- 如果不传 `memberType` 或传入无效值，将按普通订单处理
- 会员类型不参与签名计算

### 3. 验证并使用会员

**接口地址**：`/verifyMember`

**请求参数**：
- `memberCode` - 会员码（必填）
- `deviceId` - 设备ID（必填）

**返回示例**：
```json
{
  "code": 1,
  "msg": "成功",
  "data": {
    "success": true,
    "message": "验证通过",
    "memberCode": "VIP26042812ABCD",
    "memberType": "MONTH",
    "memberTypeName": "月卡",
    "expireTime": 1717027200000,
    "status": 1,
    "devicesUsed": 1
  }
}
```

**次卡返回示例**：
```json
{
  "code": 1,
  "msg": "成功",
  "data": {
    "success": true,
    "message": "验证通过",
    "memberCode": "VIP26042812ABCD",
    "memberType": "TIMES",
    "memberTypeName": "次卡-10次",
    "remainingUses": 9,
    "status": 1,
    "devicesUsed": 1
  }
}
```

### 4. 查询会员信息

**接口地址**：`/getMemberInfo`

**请求参数**：
- `memberCode` - 会员码（必填）

**返回示例**：
```json
{
  "code": 1,
  "msg": "成功",
  "data": {
    "success": true,
    "memberCode": "VIP26042812ABCD",
    "memberType": "MONTH",
    "memberTypeName": "月卡",
    "status": 1,
    "activateTime": 1714435200000,
    "expireTime": 1717027200000,
    "remainingUses": null,
    "devicesUsed": 2,
    "lastUseTime": 1714521600000
  }
}
```

## 回调通知说明

### 异步通知（notifyUrl）

支付成功后，系统会向 `notifyUrl` 发送HTTP GET请求，包含以下参数：

**普通订单**：
```
payId=xxx&param=xxx&type=xxx&price=xxx&reallyPrice=xxx&sign=xxx
```

**会员订单**（额外增加）：
```
payId=xxx&param=xxx&type=xxx&price=xxx&reallyPrice=xxx&memberCode=xxx&memberType=xxx&sign=xxx
```

### 同步跳转（returnUrl）

前端调用 `/checkOrder` 接口后，返回的URL中也会包含会员码信息（如果有）。

## 会员类型配置

系统中预置了以下会员类型：

| 类型代码 | 名称 | 有效期 | 最大设备数 | 次数 | 示例价格 |
|---------|------|--------|-----------|------|---------|
| TIMES | 次卡-10次 | 无限制 | 3 | 10 | ¥50 |
| DAY | 日卡 | 1天 | 2 | 不限 | ¥10 |
| MONTH | 月卡 | 30天 | 3 | 不限 | ¥99 |
| QUARTER | 季卡 | 90天 | 3 | 不限 | ¥259 |
| YEAR | 年卡 | 365天 | 5 | 不限 | ¥899 |
| LIFETIME | 终身卡 | 永久 | 10 | 不限 | ¥2999 |

可通过修改数据库 `member_type` 表来自定义会员类型。

## 使用流程

### 完整流程图

```
1. 前端创建订单（可选传入 memberType）
   ↓
2. 后端验证会员类型并创建订单
   ↓
3. 用户完成支付
   ↓
4. 后端检测到支付成功，自动生成会员码
   ↓
5. 后端调用 notifyUrl，携带会员码
   ↓
6. 商户服务器接收会员码并保存
   ↓
7. 用户使用会员码调用 verifyMember 接口
   ↓
8. 后端验证会员状态、设备限制、扣减次数等
   ↓
9. 返回验证结果给前端
```

### 前端示例代码

```javascript
// 1. 获取会员类型列表并展示
const typesResponse = await fetch('/getMemberTypes');
const typesData = await typesResponse.json();

if (typesData.code === 1) {
  // 渲染会员类型列表
  const memberTypes = typesData.data;
  memberTypes.forEach(type => {
    console.log(`${type.name} - ¥${type.price} - ${type.durationText}`);
    console.log(`最大设备数: ${type.maxDevices}`);
    console.log(`描述: ${type.description}`);
  });
}

// 2. 用户选择会员类型后创建订单
const selectedType = memberTypes[0]; // 假设用户选择了第一个
const response = await fetch(`/createOrder?payId=${orderId}&type=1&price=${selectedType.price}&memberType=${selectedType.typeCode}&sign=${sign}`);
const orderData = await response.json();

// 3. 等待支付完成后，获取会员码（从回调或轮询）
// 方式A：从异步通知获取（推荐）
// 商户服务器接收 notifyUrl 回调，保存 memberCode

// 方式B：从同步跳转获取
// returnUrl 会携带 memberCode 参数

// 4. 使用会员码
const verifyResponse = await fetch(`/verifyMember?memberCode=${memberCode}&deviceId=${deviceId}`);
const verifyData = await verifyResponse.json();

if (verifyData.code === 1) {
  console.log('会员验证成功');
  console.log('剩余次数:', verifyData.data.remainingUses);
  console.log('过期时间:', verifyData.data.expireTime);
} else {
  console.error('验证失败:', verifyData.msg);
}
```

## 注意事项

1. **幂等性**：同一订单只会生成一次会员码，重复支付不会重复生成
2. **设备限制**：超过最大设备数后，新设备无法使用该会员码
3. **次卡扣减**：每次调用 `verifyMember` 都会扣减一次次数
4. **自动激活**：首次验证时自动激活会员，开始计算有效期
5. **签名安全**：会员码不参与签名计算，但建议在传输过程中使用HTTPS
6. **兼容性**：不传 `memberType` 时完全按原流程处理，不影响现有业务

## 常见问题

### Q: 如何修改会员类型的价格和限制？
A: 直接修改数据库 `member_type` 表中的对应记录即可。

### Q: 会员码格式是什么？
A: 格式为 `VIP + 时间戳后8位 + UUID前8位`，例如：`VIP26042812ABCD`

### Q: 如何处理设备解绑？
A: 当前版本暂不支持自动解绑，需要手动删除 `member_device` 表中的记录。

### Q: 次卡用完后再调用会怎样？
A: 返回错误提示"次数已用完"，状态更新为3。

### Q: 会员过期后会自动清理吗？
A: 不会自动清理，建议在验证时检查过期状态并更新。

## 技术支持

如有问题，请查看日志输出或联系开发人员。
