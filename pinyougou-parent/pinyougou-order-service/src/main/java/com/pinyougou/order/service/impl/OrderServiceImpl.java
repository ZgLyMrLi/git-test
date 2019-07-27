package com.pinyougou.order.service.impl;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.pinyougou.mapper.TbOrderItemMapper;
import com.pinyougou.mapper.TbPayLogMapper;
import com.pinyougou.order.service.OrderService;
import com.pinyougou.pojo.TbOrderItem;
import com.pinyougou.pojo.TbPayLog;
import com.pinyougou.pojogroup.Cart;
import org.springframework.beans.factory.annotation.Autowired;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pinyougou.mapper.TbOrderMapper;
import com.pinyougou.pojo.TbOrder;
import com.pinyougou.pojo.TbOrderExample;
import com.pinyougou.pojo.TbOrderExample.Criteria;

import entity.PageResult;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;
import utils.IdWorker;

/**
 * 服务实现层
 * @author Administrator
 *
 */
@Service
@Transactional
public class OrderServiceImpl implements OrderService {

	@Autowired
	private TbOrderMapper orderMapper;
	
	/**
	 * 查询全部
	 */
	@Override
	public List<TbOrder> findAll() {
		return orderMapper.selectByExample(null);
	}

	/**
	 * 按分页查询
	 */
	@Override
	public PageResult findPage(int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);		
		Page<TbOrder> page=   (Page<TbOrder>) orderMapper.selectByExample(null);
		return new PageResult(page.getTotal(), page.getResult());
	}

	@Autowired
	private RedisTemplate redisTemplate;

	@Autowired
	private IdWorker idWorker;

	@Autowired
	private TbOrderItemMapper orderItemMapper;

	@Autowired
	private TbPayLogMapper payLogMapper;

	/**
	 * 增加
	 */
	@Override
	public void add(TbOrder order) {
		System.out.println(order.getUserId());
		//从Redis中获取购物车列表
		List<Cart> cartList = (List<Cart>) redisTemplate.boundHashOps("cartList").get(order.getUserId());

		List<String> orderIdList = new ArrayList();
		double totalMoney = 0;

		//循环购物车列表添加订单
		for (Cart cart : cartList) {
			TbOrder tbOrder = new TbOrder();
			long orderId = idWorker.nextId();
			tbOrder.setOrderId(orderId);//
			tbOrder.setPaymentType(order.getPaymentType());
			tbOrder.setStatus("1");//
			tbOrder.setCreateTime(new Date());//
			tbOrder.setUpdateTime(new Date());//
			tbOrder.setUserId(order.getUserId());//
			tbOrder.setReceiverAreaName(order.getReceiverAreaName());
			tbOrder.setReceiverMobile(order.getReceiverMobile());
			tbOrder.setReceiver(order.getReceiver());
			tbOrder.setSourceType(order.getSourceType());//
			tbOrder.setSellerId(order.getSellerId());

			double money = 0;
			for (TbOrderItem tbOrderItem : cart.getOrderItemList()) {
				tbOrderItem.setId(idWorker.nextId());
				tbOrderItem.setOrderId(orderId);
				tbOrderItem.setSellerId(cart.getSellerId());
				orderItemMapper.insert(tbOrderItem);
				money += tbOrderItem.getTotalFee().doubleValue();
			}
			tbOrder.setPayment(new BigDecimal(money));

			orderMapper.insert(tbOrder);

			orderIdList.add(orderId+"");
			totalMoney+=money;
		}

		if("1".equals(order.getPaymentType())){
			TbPayLog tbPayLog = new TbPayLog();
			tbPayLog.setOutTradeNo(idWorker.nextId()+"");
			tbPayLog.setCreateTime(new Date());
			tbPayLog.setUserId(order.getUserId());
			tbPayLog.setOrderList(cartList.toString().replace("[","").replace("]",""));
			tbPayLog.setTotalFee((long)(totalMoney*100));
			tbPayLog.setTradeState("0");

			payLogMapper.insert(tbPayLog);

			redisTemplate.boundHashOps("payLog").put(order.getUserId(),tbPayLog);
		}

		//清除Redis中的缓存
		redisTemplate.boundHashOps("cartList").delete(order.getUserId());
	}

	@Override
	public TbPayLog searchPayLogFromRedis(String userId){
		return (TbPayLog) redisTemplate.boundHashOps("payLog").get(userId);
	}

	@Override
	public void updateOrderStatus(String out_trade_no, String transaction_id) {
		//修改支付日志的状态以及相关字段
		TbPayLog tbPayLog = payLogMapper.selectByPrimaryKey(out_trade_no);
			//支付时间
		tbPayLog.setPayTime(new Date());
			//交易成功的状态
		tbPayLog.setTradeState("1");
			//微信的交易流水号
		tbPayLog.setTransactionId(transaction_id);
			//修改
		payLogMapper.updateByPrimaryKey(tbPayLog);

		//修改订单表的状态
		String orderList = tbPayLog.getOrderList();
		String[] split = orderList.split(",");
		//订单ID串
		for (String s : split) {
			TbOrder tbOrder = orderMapper.selectByPrimaryKey(Long.valueOf(s));
			tbOrder.setStatus("2");
			tbOrder.setPaymentTime(new Date());
			orderMapper.updateByPrimaryKey(tbOrder);
		}

		//清除缓存表的payLog
		redisTemplate.boundHashOps("payLog").delete(tbPayLog.getUserId());
	}


	/**
	 * 修改
	 */
	@Override
	public void update(TbOrder order){
		orderMapper.updateByPrimaryKey(order);
	}	
	
	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	@Override
	public TbOrder findOne(Long id){
		return orderMapper.selectByPrimaryKey(id);
	}

	/**
	 * 批量删除
	 */
	@Override
	public void delete(Long[] ids) {
		for(Long id:ids){
			orderMapper.deleteByPrimaryKey(id);
		}		
	}
	
	
		@Override
	public PageResult findPage(TbOrder order, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		
		TbOrderExample example=new TbOrderExample();
		Criteria criteria = example.createCriteria();
		
		if(order!=null){			
						if(order.getPaymentType()!=null && order.getPaymentType().length()>0){
				criteria.andPaymentTypeLike("%"+order.getPaymentType()+"%");
			}
			if(order.getPostFee()!=null && order.getPostFee().length()>0){
				criteria.andPostFeeLike("%"+order.getPostFee()+"%");
			}
			if(order.getStatus()!=null && order.getStatus().length()>0){
				criteria.andStatusLike("%"+order.getStatus()+"%");
			}
			if(order.getShippingName()!=null && order.getShippingName().length()>0){
				criteria.andShippingNameLike("%"+order.getShippingName()+"%");
			}
			if(order.getShippingCode()!=null && order.getShippingCode().length()>0){
				criteria.andShippingCodeLike("%"+order.getShippingCode()+"%");
			}
			if(order.getUserId()!=null && order.getUserId().length()>0){
				criteria.andUserIdLike("%"+order.getUserId()+"%");
			}
			if(order.getBuyerMessage()!=null && order.getBuyerMessage().length()>0){
				criteria.andBuyerMessageLike("%"+order.getBuyerMessage()+"%");
			}
			if(order.getBuyerNick()!=null && order.getBuyerNick().length()>0){
				criteria.andBuyerNickLike("%"+order.getBuyerNick()+"%");
			}
			if(order.getBuyerRate()!=null && order.getBuyerRate().length()>0){
				criteria.andBuyerRateLike("%"+order.getBuyerRate()+"%");
			}
			if(order.getReceiverAreaName()!=null && order.getReceiverAreaName().length()>0){
				criteria.andReceiverAreaNameLike("%"+order.getReceiverAreaName()+"%");
			}
			if(order.getReceiverMobile()!=null && order.getReceiverMobile().length()>0){
				criteria.andReceiverMobileLike("%"+order.getReceiverMobile()+"%");
			}
			if(order.getReceiverZipCode()!=null && order.getReceiverZipCode().length()>0){
				criteria.andReceiverZipCodeLike("%"+order.getReceiverZipCode()+"%");
			}
			if(order.getReceiver()!=null && order.getReceiver().length()>0){
				criteria.andReceiverLike("%"+order.getReceiver()+"%");
			}
			if(order.getInvoiceType()!=null && order.getInvoiceType().length()>0){
				criteria.andInvoiceTypeLike("%"+order.getInvoiceType()+"%");
			}
			if(order.getSourceType()!=null && order.getSourceType().length()>0){
				criteria.andSourceTypeLike("%"+order.getSourceType()+"%");
			}
			if(order.getSellerId()!=null && order.getSellerId().length()>0){
				criteria.andSellerIdLike("%"+order.getSellerId()+"%");
			}
	
		}
		
		Page<TbOrder> page= (Page<TbOrder>)orderMapper.selectByExample(example);		
		return new PageResult(page.getTotal(), page.getResult());
	}
	
}
