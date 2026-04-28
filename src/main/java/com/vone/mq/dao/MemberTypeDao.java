package com.vone.mq.dao;

import com.vone.mq.entity.MemberType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface MemberTypeDao extends JpaRepository<MemberType, Long> {

    // 根据类型代码查询
    MemberType findByTypeCodeAndIsActive(String typeCode, Boolean isActive);

    // 查询所有启用的会员类型
    List<MemberType> findByIsActive(Boolean isActive);

    // 根据ID和启用状态查询
    MemberType findByIdAndIsActive(Long id, Boolean isActive);
}
