package com.pinyougou.manager.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.pojo.TbSeckillGoods;
import com.pinyougou.seckill.service.SeckillGoodsService;
import entity.Result;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/managerSeckill")
public class SeckillGoodsController {

    @Reference
    private SeckillGoodsService seckillGoodsService;

    @RequestMapping("/findAll")
    public List<TbSeckillGoods> findAll(){
        return seckillGoodsService.findAll();
    }

    @RequestMapping("/findCheckedSeckillGoods")
    public List<TbSeckillGoods> findCheckedSeckillGoods(){
        return seckillGoodsService.findCheckedSeckillGoods();
    }

    @RequestMapping("/findOne")
    public TbSeckillGoods findOne(Long id){
        return seckillGoodsService.findOne(id);
    }

    @RequestMapping("/delete")
    public Result dele(Long[] ids){
        try {
            seckillGoodsService.delete(ids);
            return new Result(true,"删除成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(true,"删除失败");
        }
    }

    @RequestMapping("/add")
    public Result add(@RequestBody TbSeckillGoods seckillGoods){
        try {
            seckillGoodsService.add(seckillGoods);
            return new Result(true,"新建成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"新建失败");
        }
    }

    @RequestMapping("/update")
    public Result update(@RequestBody TbSeckillGoods seckillGoods){
        try {
            seckillGoodsService.update(seckillGoods);
            return new Result(true,"修改成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"修改失败");
        }
    }

    @RequestMapping("/alterStatus")
    public Result alterStatus(Long id, String status){
        try {
            seckillGoodsService.alterStatus(id,status);
            return new Result(true,"修改成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"修改失败");
        }
    }
}
