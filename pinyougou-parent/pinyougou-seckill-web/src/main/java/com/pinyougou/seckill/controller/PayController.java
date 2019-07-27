package com.pinyougou.seckill.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.pay.service.WeixinPayService;
import com.pinyougou.pojo.TbPayLog;
import com.pinyougou.pojo.TbSeckillOrder;
import com.pinyougou.seckill.service.SeckillOrderService;
import entity.Result;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/pay")
public class PayController {

    @Reference
    private WeixinPayService weixinPayService;

    @Reference
    private SeckillOrderService seckillOrderService;

    @RequestMapping("/createNative")
    public Map createNative() {
        String name = SecurityContextHolder.getContext().getAuthentication().getName();
        TbSeckillOrder tbSeckillOrder = seckillOrderService.searchOrderFromRedisByUserId(name);
        if (tbSeckillOrder != null) {
            return weixinPayService.createNative(tbSeckillOrder.getId() + "", (long) (tbSeckillOrder.getMoney().doubleValue() * 100) + "");
        } else {
            return new HashMap();
        }
    }

    @RequestMapping("/queryPayStatus")
    public Result queryPayStatus(String out_trade_no) {
        String name = SecurityContextHolder.getContext().getAuthentication().getName();
        Result result = null;
        int x = 0;
        while (true) {
            Map<String, String> map = weixinPayService.queryPayStatus(out_trade_no);
            if (map == null) {
                result = new Result(false, "支付发生错误");
                break;
            }
            if ("SUCCESS".equals(map.get("trade_state"))) {
                result = new Result(true, "支付成功");
                seckillOrderService.saveOrderFromRedisToDb(name, Long.valueOf(out_trade_no), map.get("transaction_id"));
                break;
            }
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            x++;
            if (x >= 101) {
                result = new Result(false, "二维码超时");
                //删除订单
                //关闭支付
                Map<String, String> closePay = weixinPayService.closePay(out_trade_no);

                if (closePay != null && "FAIL".equals(closePay.get("return_code"))) {
                    if ("ORDERPAID".equals(closePay.get("err_code"))) {
                        result = new Result(true, "支付成功");
                        seckillOrderService.saveOrderFromRedisToDb(name, Long.valueOf(out_trade_no), map.get("transaction_id"));
                    }
                }
                if (result.isSuccess() == false) {
                    seckillOrderService.deleteOrderFromRedis(name, Long.valueOf(out_trade_no));
                }
                break;
            }
        }

        return result;
    }

}
