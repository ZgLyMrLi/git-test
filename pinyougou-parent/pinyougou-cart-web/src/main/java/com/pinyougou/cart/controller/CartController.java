package com.pinyougou.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.pinyougou.cart.service.CartService;
import com.pinyougou.pojogroup.Cart;
import entity.Result;
import org.apache.http.HttpResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import utils.CookieUtil;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/cart")
public class CartController {

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private HttpServletResponse response;

    @Reference
    private CartService cartService;

    @RequestMapping("/findCartList")
    public List<Cart> findCartList() {
        String name = SecurityContextHolder.getContext().getAuthentication().getName();
        System.out.println("当前登陆人" + name);

        String cartListString = CookieUtil.getCookieValue(request, "cartList", "UTF-8");
        if ("".equals(cartListString) || cartListString == null) {
            cartListString = "[]";
        }
        List<Cart> cartList_c = JSON.parseArray(cartListString, Cart.class);

        if ("anonymousUser".equals(name)) {//如果未登录
            System.out.println("从Cookie中提取购物车");
            return cartList_c;
        } else {//如果已登录
            System.out.println("从Redis中提取购物车");
            List<Cart> cartList_r = cartService.findCartListFromRedis(name);
            if (cartList_c.size() > 0) {
                //合并购物车
                List<Cart> cartList = cartService.mergeCartList(cartList_c, cartList_r);
                //将合并后的购物车加入Redis
                cartService.saveCartListToRedis(name, cartList);
                //本地购物车清除
                CookieUtil.deleteCookie(request, response, "cartList");
                return cartList;
            } else {
                return cartList_r;
            }
        }
    }

    @RequestMapping("/addGoodsToCartList")
    @CrossOrigin(origins = "http://localhost:9105",allowCredentials = "true")
    public Result addGoodsToCartList(Long itemId, Integer num) {

        //response.setHeader("Access-Control-Allow-Origin","http://localhost:9105");//允许访问的域,第二个参数为*全都可以访问
        //当上面的方法不需要操作Cookie则不需要下面的方法
        //此方法代表允许使用Cookie，使用Cookie必须要使用下面方法
        //response.setHeader("Access-Control-Allow-Credentials","true");

        try {
            //从提取购物车
            List<Cart> cartList = findCartList();

            //调用服务方法操作购物车
            cartList = cartService.addGoodsToCartList(cartList, itemId, num);

            String name = SecurityContextHolder.getContext().getAuthentication().getName();
            if ("anonymousUser".equals(name)) {//如果未登录
                //将新的购物车存入Cookie
                String cartListString = JSON.toJSONString(cartList);
                CookieUtil.setCookie(request, response, "cartList", cartListString, 3600 * 24, "UTF-8");
            } else {
                //将新的购物车存入Redis
                cartService.saveCartListToRedis(name, cartList);
            }

            return new Result(true, "添加购物车成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "添加购物车失败");
        }
    }

}
