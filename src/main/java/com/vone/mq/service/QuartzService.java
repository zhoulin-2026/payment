package com.vone.mq.service;

import com.vone.mq.dao.PayOrderDao;
import com.vone.mq.dao.SettingDao;
import com.vone.mq.dao.TmpPriceDao;
import com.vone.mq.entity.PayOrder;
import com.vone.mq.entity.Setting;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

@Component
public class QuartzService {
    @Autowired
    private SettingDao settingDao;
    @Autowired
    private PayOrderDao payOrderDao;
    @Autowired
    private TmpPriceDao tmpPriceDao;

    @Scheduled(fixedRate = 30000)
    public void timerToZZP(){

        try {
            System.out.println("开始清理过期订单...");
            String timeoutSetting = settingDao.findById("close").map(Setting::getVvalue).orElse("30");
            long closeTime = new Date().getTime();
            long timeout = closeTime - Integer.parseInt(timeoutSetting)*60*1000L;

            int row = payOrderDao.setTimeout(timeout,closeTime);

            List<PayOrder> payOrders = payOrderDao.findAllByCloseDate(closeTime);
            for (PayOrder payOrder: payOrders) {
                tmpPriceDao.delprice(payOrder.getType()+"-"+payOrder.getReallyPrice());
            }
            System.out.println(row+"成功清理" + row + "个订单");
        }catch (Exception e){
            System.out.println("清理订单出错：" + e.getMessage());
            e.printStackTrace();
        }

        try {
            String lastheart = settingDao.findById("lastheart").map(Setting::getVvalue).orElse("0");
            String state = settingDao.findById("jkstate").map(Setting::getVvalue).orElse("0");
            if (state.equals("1") && new Date().getTime() - Long.parseLong(lastheart) > 60*1000){
                Setting setting = new Setting();
                setting.setVkey("jkstate");
                setting.setVvalue("0");
                settingDao.save(setting);
            }
        } catch (Exception e) {
            System.out.println("检查心跳出错：" + e.getMessage());
        }
    }
}
