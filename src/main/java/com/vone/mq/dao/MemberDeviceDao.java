package com.vone.mq.dao;

import com.vone.mq.entity.MemberDevice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface MemberDeviceDao extends JpaRepository<MemberDevice, Long> {

    // 根据会员码查询所有设备
    List<MemberDevice> findByMemberCode(String memberCode);

    // 根据会员码和设备ID查询
    MemberDevice findByMemberCodeAndDeviceId(String memberCode, String deviceId);

    // 统计会员码的设备数量
    @Query("SELECT COUNT(d) FROM MemberDevice d WHERE d.memberCode = ?1")
    int countByMemberCode(String memberCode);

    // 更新使用次数和最后使用时间
    @Modifying
    @Query("UPDATE MemberDevice d SET d.useCount = d.useCount + 1, d.lastUseTime = ?2 WHERE d.id = ?1")
    int updateUseCountAndLastUseTime(Long id, Long lastUseTime);

    // 删除会员码的所有设备记录
    @Modifying
    @Query("DELETE FROM MemberDevice d WHERE d.memberCode = ?1")
    int deleteByMemberCode(String memberCode);
}
