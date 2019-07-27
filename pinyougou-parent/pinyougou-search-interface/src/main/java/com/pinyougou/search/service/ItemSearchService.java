package com.pinyougou.search.service;

import com.pinyougou.pojo.TbItem;

import java.util.List;
import java.util.Map;

public interface ItemSearchService {

    /**
     * 搜索方法
     * @param searchMap
     * @return
     */
    public Map search(Map searchMap);

    /**
     *  导入列表
     * @param list
     * @return
     */
    public void importList(List list);

    public void deleteByGoodsIds(List goodsIds);

}
