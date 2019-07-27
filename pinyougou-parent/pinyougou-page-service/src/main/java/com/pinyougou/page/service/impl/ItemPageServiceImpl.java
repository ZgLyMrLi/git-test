package com.pinyougou.page.service.impl;

import com.pinyougou.mapper.TbGoodsDescMapper;
import com.pinyougou.mapper.TbGoodsMapper;
import com.pinyougou.mapper.TbItemCatMapper;
import com.pinyougou.mapper.TbItemMapper;
import com.pinyougou.page.ItemPageService;
import com.pinyougou.pojo.*;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;

import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ItemPageServiceImpl implements ItemPageService {

    @Autowired
    private FreeMarkerConfigurer freeMarkerConfigurer;

    @Value("${pagedir}")
    private String pageDir;

    @Autowired
    private TbGoodsMapper tbGoodsMapper;

    @Autowired
    private TbGoodsDescMapper tbGoodsDescMapper;

    @Autowired
    private TbItemCatMapper tbItemCatMapper;

    @Autowired
    private TbItemMapper itemMapper;

    @Override
    public boolean genItemHtml(Long goodsId) {
        Configuration configuration = freeMarkerConfigurer.getConfiguration();
        try {
            Template template = configuration.getTemplate("item.ftl");
            //创建数据模型
            Map dataModel = new HashMap<>();
            //商品主表数据
            TbGoods goods = tbGoodsMapper.selectByPrimaryKey(goodsId);
            //商品扩展表数据
            TbGoodsDesc goodsDesc = tbGoodsDescMapper.selectByPrimaryKey(goodsId);

            dataModel.put("goods",goods);
            dataModel.put("goodsDesc",goodsDesc);

            //读取商品分类
            String tbItemCat1 = tbItemCatMapper.selectByPrimaryKey(goods.getCategory1Id()).getName();
            String tbItemCat2 = tbItemCatMapper.selectByPrimaryKey(goods.getCategory2Id()).getName();
            String tbItemCat3 = tbItemCatMapper.selectByPrimaryKey(goods.getCategory3Id()).getName();
            dataModel.put("tbItemCat1",tbItemCat1);
            dataModel.put("tbItemCat2",tbItemCat2);
            dataModel.put("tbItemCat3",tbItemCat3);

            //读取SKU列表
            TbItemExample example = new TbItemExample();
            TbItemExample.Criteria criteria = example.createCriteria();
            criteria.andGoodsIdEqualTo(goods.getId());
            criteria.andStatusEqualTo("1");
            example.setOrderByClause("is_default desc");//按是否默认字段进行降序排序，目的是返回的结果第一条默认为SKU
            List<TbItem> tbItems = itemMapper.selectByExample(example);
            dataModel.put("tbItems",tbItems);

            OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(pageDir+goodsId+".html"), "UTF-8");
            PrintWriter out = new PrintWriter(writer);

            template.process(dataModel,out);//输出
            out.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean deleteItemHtml(Long[] goodIds) {
        try {
            for (Long goodId : goodIds) {
                new File(pageDir+goodId+".html").delete();
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
