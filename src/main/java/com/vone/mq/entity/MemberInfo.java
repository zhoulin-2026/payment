package com.vone.mq.entity;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "member_info")
public class MemberInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 会员码（唯一标识）
    private String memberCode;

    // 会员类型ID
    private Long memberTypeId;

    // 关联订单ID
    private String orderId;

    // 关联商户订单号
    private String payId;

    // 用户标识（可选）
    private String userIdentifier;

    // 激活时间
    private Long activateTime;

    // 过期时间
    private Long expireTime;

    // 剩余次数（仅次卡有效）
    private Integer remainingUses;

    // 状态：0-未激活，1-正常，2-已过期，3-已用完
    private Integer status;

    // 已绑定设备数
    private Integer devicesUsed;

    // 最后使用时间
    private Long lastUseTime;

    // 创建时间
    private Long createTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMemberCode() {
        return memberCode;
    }

    public void setMemberCode(String memberCode) {
        this.memberCode = memberCode;
    }

    public Long getMemberTypeId() {
        return memberTypeId;
    }

    public void setMemberTypeId(Long memberTypeId) {
        this.memberTypeId = memberTypeId;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getPayId() {
        return payId;
    }

    public void setPayId(String payId) {
        this.payId = payId;
    }

    public String getUserIdentifier() {
        return userIdentifier;
    }

    public void setUserIdentifier(String userIdentifier) {
        this.userIdentifier = userIdentifier;
    }

    public Long getActivateTime() {
        return activateTime;
    }

    public void setActivateTime(Long activateTime) {
        this.activateTime = activateTime;
    }

    public Long getExpireTime() {
        return expireTime;
    }

    public void setExpireTime(Long expireTime) {
        this.expireTime = expireTime;
    }

    public Integer getRemainingUses() {
        return remainingUses;
    }

    public void setRemainingUses(Integer remainingUses) {
        this.remainingUses = remainingUses;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getDevicesUsed() {
        return devicesUsed;
    }

    public void setDevicesUsed(Integer devicesUsed) {
        this.devicesUsed = devicesUsed;
    }

    public Long getLastUseTime() {
        return lastUseTime;
    }

    public void setLastUseTime(Long lastUseTime) {
        this.lastUseTime = lastUseTime;
    }

    public Long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Long createTime) {
        this.createTime = createTime;
    }
}
