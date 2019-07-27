package com.pinyougou.cart.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.cart.service.CartService;
import com.pinyougou.mapper.TbItemMapper;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.pojo.TbOrderItem;
import com.pinyougou.pojogroup.Cart;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class CartServiceImpl implements CartService {

    @Autowired
    private TbItemMapper itemMapper;

    @Autowired
    private RedisTemplate redisTemplate;


    @Override
    public List<Cart> findCartListFromRedis(String username) {
        System.out.println("从Redis中提取购物车" + username);
        List<Cart> cartList = (List<Cart>) redisTemplate.boundHashOps("cartList").get(username);
        if (cartList == null) {
            cartList = new ArrayList<>();
        }
        return cartList;
    }

    @Override
    public void saveCartListToRedis(String username, List<Cart> cartList) {
        System.out.println("向Redis中存入购物车" + username);
        redisTemplate.boundHashOps("cartList").put(username, cartList);
    }

    @Override
    public List<Cart> mergeCartList(List<Cart> cartList1, List<Cart> cartList2) {
        for (Cart cart : cartList1) {
            for (TbOrderItem tbOrderItem : cart.getOrderItemList()) {
                cartList1 = addGoodsToCartList(cartList1,tbOrderItem.getItemId(),tbOrderItem.getNum());
            }
        }
        return cartList1;
    }

    @Override
    public List<Cart> addGoodsToCartList(List<Cart> cartList, Long itemId, Integer num) {
        //1.根据SKUID查询商品明细SKU的对象
        TbItem item = itemMapper.selectByPrimaryKey(itemId);
        if (item == null) {
            throw new RuntimeException("商品不存在");
        }
        if (!"1".equals(item.getStatus())) {
            throw new RuntimeException("商品状态不合法");
        }

        //2.根据SKU对象得到商家ID
        String sellerId = item.getSellerId();

        //3.根据商家ID在购物车列表中查询购物车对象
        Cart cart = searchCartByCartList(cartList, sellerId);

        if (cart == null) {//4.如果购物车列表中不存在该商家的购物车
            //4.1创建一个新的购物车对象
            cart = new Cart();
            cart.setSellerId(sellerId);
            cart.setSellerName(item.getSellerId());
            List<TbOrderItem> orderItemList = new ArrayList<>();//创建购物车明细列表
            TbOrderItem orderItem = createNewCart(item, num);
            orderItemList.add(orderItem);
            cart.setOrderItemList(orderItemList);

            //4.2将新的购物车对象添加到购物车列表中
            cartList.add(cart);
        } else {//5.如果购物车列表中存在该商家的购物车
            //判断该商品是否在该购物车的明细列表中存在
            List<TbOrderItem> orderItemList = cart.getOrderItemList();
            TbOrderItem tbOrderItem = searchOrderItemByItemId(orderItemList, itemId);

            if (tbOrderItem == null) {
                //5.1如果不存在，创建新的购物车明细对象，并添加到该购物车明细列表中
                tbOrderItem = createNewCart(item, num);
                cart.getOrderItemList().add(tbOrderItem);
            } else {
                //5.2如果存在，在原有的数量上添加数量，并更新金钱
                tbOrderItem.setNum(tbOrderItem.getNum() + num);
                tbOrderItem.setTotalFee(new BigDecimal(tbOrderItem.getNum() * tbOrderItem.getPrice().doubleValue()));
                if (tbOrderItem.getNum() <= 0) {
                    cart.getOrderItemList().remove(tbOrderItem);
                }
                if (cart.getOrderItemList().size() == 0) {
                    cartList.remove(cart);
                }
            }
        }
        return cartList;
    }

    /**
     * 根据购物车明细列表和skuID查询购物车明细
     *
     * @param orderItemList
     * @param itemId
     * @return
     */
    private TbOrderItem searchOrderItemByItemId(List<TbOrderItem> orderItemList, Long itemId) {
        for (TbOrderItem tbOrderItem : orderItemList) {
            if (itemId.longValue() == tbOrderItem.getItemId().longValue()) {
                return tbOrderItem;
            }
        }
        return null;
    }

    /**
     * 根据商家ID在购物车列表中查询购物车对象
     *
     * @param cartList
     * @param sellerId
     * @return
     */
    private Cart searchCartByCartList(List<Cart> cartList, String sellerId) {
        for (Cart cart : cartList) {
            if (sellerId.equals(cart.getSellerId())) {
                return cart;
            }
        }
        return null;
    }

    /**
     * 创建新的购物车明细对象
     *
     * @param item
     * @param num
     * @return
     */
    private TbOrderItem createNewCart(TbItem item, Integer num) {
        TbOrderItem orderItem = new TbOrderItem();
        orderItem.setGoodsId(item.getGoodsId());
        orderItem.setItemId(item.getId());
        orderItem.setNum(num);
        orderItem.setPicPath(item.getImage());
        orderItem.setPrice(item.getPrice());
        orderItem.setTotalFee(new BigDecimal(item.getPrice().doubleValue() * num));
        orderItem.setSellerId(item.getSellerId());
        orderItem.setTitle(item.getTitle());
        return orderItem;
    }
}
