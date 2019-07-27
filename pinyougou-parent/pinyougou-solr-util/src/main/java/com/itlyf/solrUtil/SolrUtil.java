package com.itlyf.solrUtil;

import com.alibaba.fastjson.JSON;
import com.pinyougou.mapper.TbItemMapper;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.pojo.TbItemExample;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.SimpleQuery;
import org.springframework.stereotype.Component;

import javax.management.Query;
import java.util.List;
import java.util.Map;

@Component
public class SolrUtil {

    @Autowired
    private TbItemMapper itemMapper;

    @Autowired
    private SolrTemplate solrTemplate;

    public void importItemData(){
        TbItemExample example = new TbItemExample();
        TbItemExample.Criteria criteria = example.createCriteria();
        criteria.andStatusEqualTo("1");
        List<TbItem> tbItems = itemMapper.selectByExample(example);
        System.out.println("----开始----");
        for (TbItem tbItem : tbItems) {
            Map map = JSON.parseObject(tbItem.getSpec(), Map.class);
            tbItem.setSpecMap(map);
            System.out.println(tbItem);
        }
        solrTemplate.saveBeans(tbItems);
        solrTemplate.commit();
        System.out.println("----结束----");
    }

    public void del(){
        SimpleQuery query = new SimpleQuery("*:*");
        solrTemplate.delete(query);
        solrTemplate.commit();
    }

    public static void main(String[] args) {
        ApplicationContext applicationContext = new ClassPathXmlApplicationContext("classpath*:spring/applicationContext*.xml");
        SolrUtil solrUtil = applicationContext.getBean("solrUtil",SolrUtil.class);
        solrUtil.del();
//        solrUtil.importItemData();
    }

}
