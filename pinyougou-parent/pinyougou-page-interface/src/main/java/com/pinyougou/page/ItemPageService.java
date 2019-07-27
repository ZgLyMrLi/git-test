package com.pinyougou.page;

public interface ItemPageService {

    /**
     * 生成商品详细页
     * @param goodsId
     * @return
     */
    public boolean genItemHtml(Long goodsId);

    /**
     * 删除商品详细页
     * @param goodIds
     * @return
     */
    public boolean deleteItemHtml(Long[] goodIds);

}
