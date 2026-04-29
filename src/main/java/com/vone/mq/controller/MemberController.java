package com.vone.mq.controller;

import com.google.gson.Gson;
import com.vone.mq.dto.CommonRes;
import com.vone.mq.entity.MemberType;
import com.vone.mq.service.MemberService;
import com.vone.mq.utils.ResUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class MemberController {

    @Autowired
    private MemberService memberService;

    /**
     * 验证并使用会员
     * 
     * @param memberCode 会员码
     * @param deviceId 设备ID
     * @return 验证结果
     */
    @RequestMapping("/verifyMember")
    public String verifyMember(String memberCode, String deviceId) {
        if (memberCode == null || memberCode.isEmpty()) {
            return new Gson().toJson(ResUtil.error("请传入会员码"));
        }
        
        if (deviceId == null || deviceId.isEmpty()) {
            return new Gson().toJson(ResUtil.error("请传入设备ID"));
        }
        
        Map<String, Object> result = memberService.verifyAndUseMember(memberCode, deviceId);
        
        if ((Boolean) result.get("success")) {
            return new Gson().toJson(ResUtil.success(result));
        } else {
            return new Gson().toJson(ResUtil.error((String) result.get("message")));
        }
    }

    /**
     * 查询会员信息
     * 
     * @param memberCode 会员码
     * @return 会员信息
     */
    @RequestMapping("/getMemberInfo")
    public String getMemberInfo(String memberCode) {
        if (memberCode == null || memberCode.isEmpty()) {
            return new Gson().toJson(ResUtil.error("请传入会员码"));
        }
        
        Map<String, Object> result = memberService.getMemberInfo(memberCode);
        
        if ((Boolean) result.get("success")) {
            return new Gson().toJson(ResUtil.success(result));
        } else {
            return new Gson().toJson(ResUtil.error((String) result.get("message")));
        }
    }

    /**
     * 获取会员类型列表（供前端展示购买）
     * 
     * @return 会员类型列表
     */
    @RequestMapping("/getMemberTypes")
    public String getMemberTypes() {
        List<MemberType> memberTypes = memberService.getActiveMemberTypes();
        //排序按ID
        memberTypes.sort((mt1, mt2) -> mt1.getId().compareTo(mt2.getId()));
        
        // 转换为前端友好的格式
        List<Map<String, Object>> resultList = new java.util.ArrayList<>();
        for (MemberType mt : memberTypes) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", mt.getId());
            item.put("name", mt.getName());
            item.put("typeCode", mt.getTypeCode());
            item.put("price", mt.getPrice());
            item.put("maxDevices", mt.getMaxDevices());
            item.put("description", mt.getDescription());
            
            // 根据类型代码设置显示文本
            if ("TIMES".equals(mt.getTypeCode())) {
                item.put("durationText", mt.getTotalUses() + "次使用");
                item.put("isTimeBased", false);
            } else if ("LIFETIME".equals(mt.getTypeCode())) {
                item.put("durationText", "永久有效");
                item.put("isTimeBased", true);
            } else if ("DAY".equals(mt.getTypeCode())) {
                item.put("durationText", "1天");
                item.put("isTimeBased", true);
            } else if ("MONTH".equals(mt.getTypeCode())) {
                item.put("durationText", "30天");
                item.put("isTimeBased", true);
            } else if ("QUARTER".equals(mt.getTypeCode())) {
                item.put("durationText", "90天");
                item.put("isTimeBased", true);
            } else if ("YEAR".equals(mt.getTypeCode())) {
                item.put("durationText", "365天");
                item.put("isTimeBased", true);
            } else {
                item.put("durationText", mt.getDurationDays() + "天");
                item.put("isTimeBased", true);
            }
            
            resultList.add(item);
        }
        
        return new Gson().toJson(ResUtil.success(resultList));
    }
}
