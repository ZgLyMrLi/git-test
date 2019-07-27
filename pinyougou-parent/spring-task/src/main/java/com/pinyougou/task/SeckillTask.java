package com.pinyougou.task;

import com.pinyougou.mapper.TbSeckillGoodsMapper;
import com.pinyougou.pojo.TbSeckillGoods;
import com.pinyougou.pojo.TbSeckillGoodsExample;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Component
public class SeckillTask {

    @Autowired
    private TbSeckillGoodsMapper seckillGoodsMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    @Scheduled(cron = "0/20 * * * * ?")
    public void refreshSeckillGoods() {
        List goodsIdList = new ArrayList(redisTemplate.boundHashOps("seckillGoods").keys());
        System.out.println(goodsIdList);
        TbSeckillGoodsExample example = new TbSeckillGoodsExample();
        TbSeckillGoodsExample.Criteria criteria = example.createCriteria();
        criteria.andStartTimeLessThanOrEqualTo(new Date());
        criteria.andEndTimeGreaterThanOrEqualTo(new Date());
        criteria.andStatusEqualTo("1");
        criteria.andStockCountGreaterThan(0);

        if (goodsIdList.size() > 0) {
            criteria.andIdNotIn(goodsIdList);
        }

        System.out.println("缓存中的秒杀");
        List<TbSeckillGoods> seckillGoods = seckillGoodsMapper.selectByExample(example);

        for (TbSeckillGoods seckillGood : seckillGoods) {
            redisTemplate.boundHashOps("seckillGoods").put(seckillGood.getId(), seckillGood);
            System.out.println(seckillGood.getId());
        }
    }

    @Scheduled(cron = "* * * * * ?")
    public void removeSeckillGoods() {
        System.out.println(new Date());
        List<TbSeckillGoods> seckillGoods = redisTemplate.boundHashOps("seckillGoods").values();
        for (TbSeckillGoods seckillGood : seckillGoods) {
            System.out.println(seckillGood.getEndTime().getTime());
            System.out.println(new Date().getTime());
            if (seckillGood.getEndTime().getTime() < new Date().getTime()) {
                seckillGoodsMapper.updateByPrimaryKey(seckillGood);
                redisTemplate.boundHashOps("seckillGoods").delete(seckillGood.getId());
                System.out.println("删除缓存中");
            }
        }
        System.out.println("-----------end-----------");
    }
}
