package com.vone.mq.entity;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "member_device")
public class MemberDevice {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 会员码
    private String memberCode;

    // 设备ID
    private String deviceId;

    // 首次使用时间
    private Long firstUseTime;

    // 最后使用时间
    private Long lastUseTime;

    // 使用次数
    private Integer useCount;

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

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public Long getFirstUseTime() {
        return firstUseTime;
    }

    public void setFirstUseTime(Long firstUseTime) {
        this.firstUseTime = firstUseTime;
    }

    public Long getLastUseTime() {
        return lastUseTime;
    }

    public void setLastUseTime(Long lastUseTime) {
        this.lastUseTime = lastUseTime;
    }

    public Integer getUseCount() {
        return useCount;
    }

    public void setUseCount(Integer useCount) {
        this.useCount = useCount;
    }
}
