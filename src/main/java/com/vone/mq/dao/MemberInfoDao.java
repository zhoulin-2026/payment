package com.vone.mq.dao;

import com.vone.mq.entity.MemberInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface MemberInfoDao extends JpaRepository<MemberInfo, Long> {

    // 根据会员码查询
    MemberInfo findByMemberCode(String memberCode);

    // 根据订单ID查询
    MemberInfo findByOrderId(String orderId);

    // 根据商户订单号查询
    MemberInfo findByPayId(String payId);

    // 根据会员类型ID和状态查询
    List<MemberInfo> findByMemberTypeIdAndStatus(Long memberTypeId, Integer status);

    // 更新会员状态
    @Modifying
    @Query("UPDATE MemberInfo m SET m.status = ?2 WHERE m.id = ?1")
    int updateStatus(Long id, Integer status);

    // 更新激活时间和状态
    @Modifying
    @Query("UPDATE MemberInfo m SET m.activateTime = ?2, m.status = ?3 WHERE m.id = ?1")
    int updateActivateTimeAndStatus(Long id, Long activateTime, Integer status);

    // 更新剩余次数
    @Modifying
    @Query("UPDATE MemberInfo m SET m.remainingUses = ?2 WHERE m.id = ?1")
    int updateRemainingUses(Long id, Integer remainingUses);

    // 更新设备使用数量
    @Modifying
    @Query("UPDATE MemberInfo m SET m.devicesUsed = ?2 WHERE m.id = ?1")
    int updateDevicesUsed(Long id, Integer devicesUsed);

    // 更新最后使用时间
    @Modifying
    @Query("UPDATE MemberInfo m SET m.lastUseTime = ?2 WHERE m.id = ?1")
    int updateLastUseTime(Long id, Long lastUseTime);
}
