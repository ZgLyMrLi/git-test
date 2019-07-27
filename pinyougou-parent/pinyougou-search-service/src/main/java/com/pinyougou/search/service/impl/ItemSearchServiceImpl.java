package com.pinyougou.search.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.pojo.TbBrand;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.pojo.TbSpecification;
import com.pinyougou.search.service.ItemSearchService;
import org.apache.zookeeper.Op;
import org.aspectj.weaver.ast.Var;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.*;
import org.springframework.data.solr.core.query.result.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service(timeout = 5000)
public class ItemSearchServiceImpl implements ItemSearchService {

    @Autowired
    private SolrTemplate solrTemplate;

    @Override
    public Map search(Map searchMap) {
        Map map = new HashMap();
        //空格处理
        String keywords = (String) searchMap.get("keywords");
        if(keywords!=null){
            searchMap.put("keywords",keywords.replace(" ", ""));
        }//关键字去空格

        /*Query query = new SimpleQuery("*:*");
        Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));
        query.addCriteria(criteria);
        ScoredPage<TbItem> tbItems = solrTemplate.queryForPage(query, TbItem.class);

        map.put("rows",tbItems.getContent());*/

        //查询列表
        map.putAll(searchList(searchMap));
        List<String> categoryList = searchCategoryList(searchMap);
        map.put("categoryList", categoryList);
        String category = (String) searchMap.get("category");
        if (!"".equals(category)) {
            map.putAll(searchBrandAndSpecList(category));
        } else {
            if (categoryList.size() > 0) {
                map.putAll(searchBrandAndSpecList(categoryList.get(0)));
            }
        }
        return map;
    }

    @Override
    public void importList(List list) {
        solrTemplate.saveBeans(list);
        solrTemplate.commit();
    }

    @Override
    public void deleteByGoodsIds(List goodsIds) {
        Query query=new SimpleQuery("*:*");
        Criteria criteria=new Criteria("item_goodsid").in(goodsIds);
        query.addCriteria(criteria);
        solrTemplate.delete(query);
        solrTemplate.commit();
    }

    @Autowired
    private RedisTemplate redisTemplate;

    private Map searchBrandAndSpecList(String category) {
        Map map = new HashMap();
        Long templateId = (Long) redisTemplate.boundHashOps("itemCat").get(category);
        if (templateId != null) {
            List brandList = (List) redisTemplate.boundHashOps("brandList").get(templateId);
            map.put("brandList", brandList);
            List specList = (List) redisTemplate.boundHashOps("specList").get(templateId);
            map.put("specList", specList);
        }
        return map;
    }

    /**
     * 查询列表
     *
     * @param searchMap
     * @return
     */
    private Map searchList(Map searchMap) {
        Map map = new HashMap();
        //高亮显示
        //构建高亮选项对象
        HighlightQuery highlightQuery = new SimpleHighlightQuery();
        HighlightOptions highlightOptions = new HighlightOptions().addField("item_title");//高亮域
        highlightOptions.setSimplePrefix("<em style='color:red'>");//前缀
        highlightOptions.setSimplePostfix("</em>");//后缀
        highlightQuery.setHighlightOptions(highlightOptions);

        //关键字查询
        Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));
        highlightQuery.addCriteria(criteria);

        //通过商品分类进行查询过滤
        if (!"".equals(searchMap.get("category"))) {
            FilterQuery filterQuery = new SimpleFilterQuery();
            Criteria filtercriteria = new Criteria("item_category").is(searchMap.get("category"));
            filterQuery.addCriteria(filtercriteria);
            highlightQuery.addFilterQuery(filterQuery);
        }

        //通过品牌进行查询过滤
        if (!"".equals(searchMap.get("brand"))) {
            FilterQuery filterQuery = new SimpleFilterQuery();
            Criteria filtercriteria = new Criteria("item_brand").is(searchMap.get("brand"));
            filterQuery.addCriteria(filtercriteria);
            highlightQuery.addFilterQuery(filterQuery);
        }

        //通过规格进行查询过滤
        if (searchMap.get("spec") != null) {
            Map<String, String> specMap = (Map<String, String>) searchMap.get("spec");
            for (String key : specMap.keySet()) {
                FilterQuery filterQuery = new SimpleFilterQuery();
                Criteria filtercriteria = new Criteria("item_spec_" + key).is(searchMap.get(key));
                filterQuery.addCriteria(filtercriteria);
                highlightQuery.addFilterQuery(filterQuery);
            }
        }

        //通过价格进行查询过滤
        if (!"".equals(searchMap.get("price"))) {
            String[] price = ((String) searchMap.get("price")).split("-");
            System.out.println(price[1].equals("*"));
            if (!price[1].equals("*")) {
                FilterQuery filterQuery1 = new SimpleFilterQuery();
                Criteria filtercriteria1 = new Criteria("item_price").greaterThanEqual(price[0]);
                filterQuery1.addCriteria(filtercriteria1);
                highlightQuery.addFilterQuery(filterQuery1);

                FilterQuery filterQuery2 = new SimpleFilterQuery();
                Criteria filtercriteria2 = new Criteria("item_price").lessThanEqual(price[1]);
                filterQuery2.addCriteria(filtercriteria2);
                highlightQuery.addFilterQuery(filterQuery2);
            } else {
                FilterQuery filterQuery = new SimpleFilterQuery();
                Criteria filtercriteria = new Criteria("item_price").greaterThanEqual(price[0]);
                filterQuery.addCriteria(filtercriteria);
                highlightQuery.addFilterQuery(filterQuery);
            }
        }

        Integer pageNo = (Integer) searchMap.get("pageNo");//获取前端传过来的页码
        if (pageNo == null) {
            pageNo = 1;
        }
        Integer pageSize = (Integer) searchMap.get("pageSize");//获取前端传入的每页大小
        if (pageSize == null) {
            pageSize = 20;
        }
        highlightQuery.setOffset((pageNo-1)*pageSize);//设置开始记录数
        highlightQuery.setRows(pageSize);//设置每页条数

        //按价格排序,参数一
        String sortValue = (String) searchMap.get("sort");//升序降序
        String sortField = (String) searchMap.get("sortField");

        if(sortValue!=null&&!"".equals(sortField)){
            if(sortValue.equals("ASC")){
                Sort sort = new Sort(Sort.Direction.ASC,"item_"+sortField);
                highlightQuery.addSort(sort);
            }
            if(sortValue.equals("DESC")){
                Sort sort = new Sort(Sort.Direction.DESC,"item_"+sortField);
                highlightQuery.addSort(sort);
            }
        }

        //高亮页对象
        HighlightPage<TbItem> tbItems = solrTemplate.queryForHighlightPage(highlightQuery, TbItem.class);
        //高亮入口集合(每条记录的高亮入口)
        List<HighlightEntry<TbItem>> highlighted = tbItems.getHighlighted();
        for (HighlightEntry<TbItem> tbItemHighlightEntry : highlighted) {
            //获取高亮列表(高亮域的个数)
            List<HighlightEntry.Highlight> highlights = tbItemHighlightEntry.getHighlights();
            /*for (HighlightEntry.Highlight highlight : highlights) {
                List<String> snipplets = highlight.getSnipplets();//每个域有可能存储多个值,如果是单列的话，用结果直接,get(0)
                System.out.println(snipplets);
            }*/
            if (highlights.size() > 0 && highlights.get(0).getSnipplets().size() > 0) {
                TbItem item = tbItemHighlightEntry.getEntity();
                item.setTitle(highlights.get(0).getSnipplets().get(0));
            }
        }
        map.put("rows", tbItems.getContent());
        map.put("totalPages",tbItems.getTotalPages());
        map.put("totalElements",tbItems.getTotalElements());
        return map;
    }

    /**
     * 分组查询（查询商品分类列表）
     *
     * @param searchMap
     * @return
     */
    private List<String> searchCategoryList(Map searchMap) {
        List<String> list = new ArrayList<>();
        Query query = new SimpleQuery("*:*");
        //根据关键字查询,相当于sql语句中的where
        Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));
        query.addCriteria(criteria);
        //设置分组选项，根据哪个域进行分组，相当于sql语句中的group by
        GroupOptions groupOptions = new GroupOptions().addGroupByField("item_category");
        query.setGroupOptions(groupOptions);
        //设置分组页，即根据条件分组之后的数据
        GroupPage<TbItem> page = solrTemplate.queryForGroupPage(query, TbItem.class);
        //获取分组结果对象,必须是上面groupby过的域，因为可能根据多个域进行分组，所以要指定使用哪个域。
        GroupResult<TbItem> groupResult = page.getGroupResult("item_category");
        //获取分组入口页
        Page<GroupEntry<TbItem>> groupEntries = groupResult.getGroupEntries();
        //获取分组入口集合
        List<GroupEntry<TbItem>> content = groupEntries.getContent();

        for (GroupEntry<TbItem> tbItemGroupEntry : content) {
            list.add(tbItemGroupEntry.getGroupValue());//将分组的结果添加到返回值中
        }

        return list;
    }

    /*@Override
    public void deleteByGoodsIds(List goodsIds) {
        Query query=new SimpleQuery("*:*");
        Criteria criteria=new Criteria("item_goodsid").in(goodsIds);
        query.addCriteria(criteria);
        solrTemplate.delete(query);
        solrTemplate.commit();
    }*/

}
