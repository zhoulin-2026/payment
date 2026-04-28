package com.vone.mq.service;

import com.vone.mq.dao.MemberDeviceDao;
import com.vone.mq.dao.MemberInfoDao;
import com.vone.mq.dao.MemberTypeDao;
import com.vone.mq.entity.MemberDevice;
import com.vone.mq.entity.MemberInfo;
import com.vone.mq.entity.MemberType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class MemberService {

    @Autowired
    private MemberTypeDao memberTypeDao;

    @Autowired
    private MemberInfoDao memberInfoDao;

    @Autowired
    private MemberDeviceDao memberDeviceDao;

    /**
     * 验证会员类型是否有效
     */
    public MemberType validateMemberType(String typeCode) {
        if (typeCode == null || typeCode.isEmpty()) {
            return null;
        }
        return memberTypeDao.findByTypeCodeAndIsActive(typeCode, true);
    }

    /**
     * 根据ID验证会员类型
     */
    public MemberType validateMemberTypeId(Long memberTypeId) {
        if (memberTypeId == null) {
            return null;
        }
        return memberTypeDao.findByIdAndIsActive(memberTypeId, true);
    }

    /**
     * 生成会员码
     */
    public String generateMemberCode() {
        // 格式：VIP + 时间戳后8位 + UUID前8位
        String timestamp = String.valueOf(System.currentTimeMillis());
        String uuid = UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
        return "VIP" + timestamp.substring(timestamp.length() - 8) + uuid;
    }

    /**
     * 创建会员记录
     */
    @Transactional
    public MemberInfo createMember(Long memberTypeId, String orderId, String payId) {
        // 验证会员类型
        MemberType memberType = validateMemberTypeId(memberTypeId);
        if (memberType == null) {
            return null;
        }

        // 检查是否已存在会员记录（幂等性）
        MemberInfo existingMember = memberInfoDao.findByOrderId(orderId);
        if (existingMember != null) {
            return existingMember;
        }

        // 生成会员码
        String memberCode = generateMemberCode();

        // 计算过期时间
        Long expireTime = null;
        if (!"TIMES".equals(memberType.getTypeCode())) {
            // 非次卡，计算有效期
            long currentTime = new Date().getTime();
            expireTime = currentTime + (memberType.getDurationDays() * 24L * 60 * 60 * 1000);
        }

        // 初始化剩余次数
        Integer remainingUses = null;
        if ("TIMES".equals(memberType.getTypeCode())) {
            remainingUses = memberType.getTotalUses();
        }

        // 创建会员记录
        MemberInfo memberInfo = new MemberInfo();
        memberInfo.setMemberCode(memberCode);
        memberInfo.setMemberTypeId(memberTypeId);
        memberInfo.setOrderId(orderId);
        memberInfo.setPayId(payId);
        memberInfo.setActivateTime(null); // 未激活
        memberInfo.setExpireTime(expireTime);
        memberInfo.setRemainingUses(remainingUses);
        memberInfo.setStatus(0); // 未激活状态
        memberInfo.setDevicesUsed(0);
        memberInfo.setLastUseTime(null);
        memberInfo.setCreateTime(new Date().getTime());

        return memberInfoDao.save(memberInfo);
    }

    /**
     * 验证并使用会员
     */
    @Transactional
    public Map<String, Object> verifyAndUseMember(String memberCode, String deviceId) {
        Map<String, Object> result = new HashMap<>();

        // 查询会员信息
        MemberInfo memberInfo = memberInfoDao.findByMemberCode(memberCode);
        if (memberInfo == null) {
            result.put("success", false);
            result.put("message", "会员码无效");
            return result;
        }

        // 查询会员类型
        MemberType memberType = memberTypeDao.findById(memberInfo.getMemberTypeId()).orElse(null);
        if (memberType == null) {
            result.put("success", false);
            result.put("message", "会员类型不存在");
            return result;
        }

        // 检查状态
        if (memberInfo.getStatus() == 2) {
            result.put("success", false);
            result.put("message", "会员已过期");
            return result;
        }

        if (memberInfo.getStatus() == 3) {
            result.put("success", false);
            result.put("message", "会员次数已用完");
            return result;
        }

        // 检查有效期（仅时间卡）
        if (!"TIMES".equals(memberType.getTypeCode()) && memberInfo.getExpireTime() != null) {
            long currentTime = new Date().getTime();
            if (currentTime > memberInfo.getExpireTime()) {
                // 更新状态为已过期
                memberInfoDao.updateStatus(memberInfo.getId(), 2);
                result.put("success", false);
                result.put("message", "会员已过期");
                return result;
            }
        }

        // 首次使用需要激活
        if (memberInfo.getStatus() == 0) {
            long currentTime = new Date().getTime();
            memberInfoDao.updateActivateTimeAndStatus(memberInfo.getId(), currentTime, 1);
            memberInfo.setStatus(1);
            memberInfo.setActivateTime(currentTime);
        }

        // 检查设备限制
        MemberDevice device = memberDeviceDao.findByMemberCodeAndDeviceId(memberCode, deviceId);
        if (device == null) {
            // 新设备，检查是否超限
            int deviceCount = memberDeviceDao.countByMemberCode(memberCode);
            if (deviceCount >= memberType.getMaxDevices()) {
                result.put("success", false);
                result.put("message", "超出设备限制（最多" + memberType.getMaxDevices() + "台设备）");
                return result;
            }

            // 添加新设备
            device = new MemberDevice();
            device.setMemberCode(memberCode);
            device.setDeviceId(deviceId);
            long currentTime = new Date().getTime();
            device.setFirstUseTime(currentTime);
            device.setLastUseTime(currentTime);
            device.setUseCount(1);
            memberDeviceDao.save(device);

            // 更新设备数量
            memberInfoDao.updateDevicesUsed(memberInfo.getId(), deviceCount + 1);
        } else {
            // 已知设备，更新使用记录
            long currentTime = new Date().getTime();
            memberDeviceDao.updateUseCountAndLastUseTime(device.getId(), currentTime);
        }

        // 处理次卡扣减
        if ("TIMES".equals(memberType.getTypeCode())) {
            if (memberInfo.getRemainingUses() == null || memberInfo.getRemainingUses() <= 0) {
                memberInfoDao.updateStatus(memberInfo.getId(), 3);
                result.put("success", false);
                result.put("message", "次数已用完");
                return result;
            }

            // 扣减次数
            int newRemainingUses = memberInfo.getRemainingUses() - 1;
            memberInfoDao.updateRemainingUses(memberInfo.getId(), newRemainingUses);
            memberInfo.setRemainingUses(newRemainingUses);

            // 如果次数用完，更新状态
            if (newRemainingUses <= 0) {
                memberInfoDao.updateStatus(memberInfo.getId(), 3);
            }
        }

        // 更新最后使用时间
        long currentTime = new Date().getTime();
        memberInfoDao.updateLastUseTime(memberInfo.getId(), currentTime);

        // 返回成功信息
        result.put("success", true);
        result.put("message", "验证通过");
        result.put("memberCode", memberCode);
        result.put("memberType", memberType.getTypeCode());
        result.put("memberTypeName", memberType.getName());

        if ("TIMES".equals(memberType.getTypeCode())) {
            result.put("remainingUses", memberInfo.getRemainingUses());
        } else {
            result.put("expireTime", memberInfo.getExpireTime());
        }

        result.put("status", memberInfo.getStatus());
        result.put("devicesUsed", memberInfo.getDevicesUsed() + 1);

        return result;
    }

    /**
     * 查询会员信息
     */
    public Map<String, Object> getMemberInfo(String memberCode) {
        Map<String, Object> result = new HashMap<>();

        MemberInfo memberInfo = memberInfoDao.findByMemberCode(memberCode);
        if (memberInfo == null) {
            result.put("success", false);
            result.put("message", "会员码无效");
            return result;
        }

        MemberType memberType = memberTypeDao.findById(memberInfo.getMemberTypeId()).orElse(null);

        result.put("success", true);
        result.put("memberCode", memberCode);
        result.put("memberType", memberType != null ? memberType.getTypeCode() : "");
        result.put("memberTypeName", memberType != null ? memberType.getName() : "");
        result.put("status", memberInfo.getStatus());
        result.put("activateTime", memberInfo.getActivateTime());
        result.put("expireTime", memberInfo.getExpireTime());
        result.put("remainingUses", memberInfo.getRemainingUses());
        result.put("devicesUsed", memberInfo.getDevicesUsed());
        result.put("lastUseTime", memberInfo.getLastUseTime());

        return result;
    }

    /**
     * 获取所有启用的会员类型列表
     */
    public List<MemberType> getActiveMemberTypes() {
        return memberTypeDao.findByIsActive(true);
    }
}
