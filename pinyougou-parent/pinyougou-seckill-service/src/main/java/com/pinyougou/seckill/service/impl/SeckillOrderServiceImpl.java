package com.pinyougou.seckill.service.impl;
import java.util.Date;
import java.util.List;

import com.pinyougou.mapper.TbSeckillGoodsMapper;
import com.pinyougou.mapper.TbSeckillOrderMapper;
import com.pinyougou.pojo.TbSeckillGoods;
import com.pinyougou.seckill.service.SeckillOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;

import com.pinyougou.pojo.TbSeckillOrder;
import com.pinyougou.pojo.TbSeckillOrderExample;
import com.pinyougou.pojo.TbSeckillOrderExample.Criteria;

import entity.PageResult;
import org.springframework.data.redis.core.RedisTemplate;
import utils.IdWorker;

/**
 * 服务实现层
 * @author Administrator
 *
 */
@Service
public class SeckillOrderServiceImpl implements SeckillOrderService {

	@Autowired
	private TbSeckillOrderMapper seckillOrderMapper;
	
	/**
	 * 查询全部
	 */
	@Override
	public List<TbSeckillOrder> findAll() {
		return seckillOrderMapper.selectByExample(null);
	}

	/**
	 * 按分页查询
	 */
	@Override
	public PageResult findPage(int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);		
		Page<TbSeckillOrder> page=   (Page<TbSeckillOrder>) seckillOrderMapper.selectByExample(null);
		return new PageResult(page.getTotal(), page.getResult());
	}

	/**
	 * 增加
	 */
	@Override
	public void add(TbSeckillOrder seckillOrder) {
		seckillOrderMapper.insert(seckillOrder);		
	}

	
	/**
	 * 修改
	 */
	@Override
	public void update(TbSeckillOrder seckillOrder){
		seckillOrderMapper.updateByPrimaryKey(seckillOrder);
	}	
	
	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	@Override
	public TbSeckillOrder findOne(Long id){
		return seckillOrderMapper.selectByPrimaryKey(id);
	}

	/**
	 * 批量删除
	 */
	@Override
	public void delete(Long[] ids) {
		for(Long id:ids){
			seckillOrderMapper.deleteByPrimaryKey(id);
		}		
	}
	
	
		@Override
	public PageResult findPage(TbSeckillOrder seckillOrder, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		
		TbSeckillOrderExample example=new TbSeckillOrderExample();
		Criteria criteria = example.createCriteria();
		
		if(seckillOrder!=null){			
						if(seckillOrder.getUserId()!=null && seckillOrder.getUserId().length()>0){
				criteria.andUserIdLike("%"+seckillOrder.getUserId()+"%");
			}
			if(seckillOrder.getSellerId()!=null && seckillOrder.getSellerId().length()>0){
				criteria.andSellerIdLike("%"+seckillOrder.getSellerId()+"%");
			}
			if(seckillOrder.getStatus()!=null && seckillOrder.getStatus().length()>0){
				criteria.andStatusLike("%"+seckillOrder.getStatus()+"%");
			}
			if(seckillOrder.getReceiverAddress()!=null && seckillOrder.getReceiverAddress().length()>0){
				criteria.andReceiverAddressLike("%"+seckillOrder.getReceiverAddress()+"%");
			}
			if(seckillOrder.getReceiverMobile()!=null && seckillOrder.getReceiverMobile().length()>0){
				criteria.andReceiverMobileLike("%"+seckillOrder.getReceiverMobile()+"%");
			}
			if(seckillOrder.getReceiver()!=null && seckillOrder.getReceiver().length()>0){
				criteria.andReceiverLike("%"+seckillOrder.getReceiver()+"%");
			}
			if(seckillOrder.getTransactionId()!=null && seckillOrder.getTransactionId().length()>0){
				criteria.andTransactionIdLike("%"+seckillOrder.getTransactionId()+"%");
			}
	
		}
		
		Page<TbSeckillOrder> page= (Page<TbSeckillOrder>)seckillOrderMapper.selectByExample(example);		
		return new PageResult(page.getTotal(), page.getResult());
	}

	@Autowired
	private RedisTemplate redisTemplate;

	@Autowired
	private TbSeckillGoodsMapper tbSeckillGoodsMapper;

	@Autowired
	private IdWorker idWorker;

	@Override
	public void submitOrder(Long seckillId, String userId) {
		TbSeckillGoods tbSeckillGoods = (TbSeckillGoods) redisTemplate.boundHashOps("seckillGoods").get(seckillId);
		if(tbSeckillGoods==null){
			throw new RuntimeException("商品不存在或已经下架");
		}
		if(tbSeckillGoods.getStockCount()<=0){
			throw new RuntimeException("商品已经被抢光");
		}

		tbSeckillGoods.setStockCount(tbSeckillGoods.getStockCount()-1);
		redisTemplate.boundHashOps("seckillGoods").put(seckillId,tbSeckillGoods);
		if(tbSeckillGoods.getStockCount()==0){
			redisTemplate.boundHashOps("seckillGoods").delete(seckillId);
			tbSeckillGoodsMapper.updateByPrimaryKey(tbSeckillGoods);
			System.out.println("将商品同步到数据库");
		}

		//存储秒杀订单（因为还未支付，所以不向数据库存放）
		TbSeckillOrder tbSeckillOrder = new TbSeckillOrder();
		tbSeckillOrder.setId(idWorker.nextId());
		tbSeckillOrder.setSeckillId(seckillId);
		tbSeckillOrder.setMoney(tbSeckillGoods.getCostPrice());
		tbSeckillOrder.setUserId(userId);
		tbSeckillOrder.setSellerId(tbSeckillGoods.getSellerId());
		tbSeckillOrder.setCreateTime(new Date());
		tbSeckillOrder.setStatus("0");

		redisTemplate.boundHashOps("seckillOrder").put(userId,tbSeckillOrder);
		System.out.println("保存订单成功（Redis）");
	}

	@Override
	public TbSeckillOrder  searchOrderFromRedisByUserId(String userId) {
		return (TbSeckillOrder) redisTemplate.boundHashOps("seckillOrder").get(userId);
	}

	@Override
	public void saveOrderFromRedisToDb(String userId, Long orderId, String transactionId) {
		TbSeckillOrder seckillOrder = (TbSeckillOrder) redisTemplate.boundHashOps("seckillOrder").get(userId);
		if(seckillOrder==null){
			throw new RuntimeException("订单不存在");
		}
		//如果与传递过来的订单号不符
		if(seckillOrder.getId().longValue()!=orderId.longValue()){
			throw new RuntimeException("订单不相符");
		}
		seckillOrder.setTransactionId(transactionId);//交易流水号
		seckillOrder.setPayTime(new Date());//支付时间
		seckillOrder.setStatus("1");//状态
		seckillOrderMapper.insert(seckillOrder);//保存到数据库
		redisTemplate.boundHashOps("seckillOrder").delete(userId);//从redis中清除
	}

	@Override
	public void deleteOrderFromRedis(String userId, Long orderId) {
		System.out.println("删除订单");
		//查询出缓存中的订单
		TbSeckillOrder tbSeckillOrder = searchOrderFromRedisByUserId(userId);
		if(tbSeckillOrder!=null){
			//删除缓存
			redisTemplate.boundHashOps("seckillOrder").delete(userId);
			//库存回退
			TbSeckillGoods seckillGoods = (TbSeckillGoods) redisTemplate.boundHashOps("seckillOrder").get(tbSeckillOrder.getSeckillId());
			if(seckillGoods!=null){
				System.out.println("seckillGoods!=null");
				seckillGoods.setStockCount(seckillGoods.getStockCount()+1);
				redisTemplate.boundHashOps("seckillOrder").put(tbSeckillOrder.getSeckillId(),seckillGoods);
			}else {
				System.out.println("seckillGoods==null");
				seckillGoods = new TbSeckillGoods();
				seckillGoods.setId(tbSeckillOrder.getSeckillId());
				seckillGoods.setStockCount(1);
				redisTemplate.boundHashOps("seckillOrder").put(tbSeckillOrder.getSeckillId(),seckillGoods);
			}
		}
	}

}
