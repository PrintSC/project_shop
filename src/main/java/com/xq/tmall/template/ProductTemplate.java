package com.xq.tmall.template;

import com.xq.tmall.util.excel.ExcelField;
import lombok.Data;

/**
 * 产品导出模版
 */
@Data
public class ProductTemplate {
    /**
     * 产品名称
     */
    @ExcelField(title = "产品名称", align = 2, sort = 1)
    private String product_name;

    /**
     * 产品标题
     */
    @ExcelField(title = "产品标题", align = 2, sort = 2)
    private String product_title;

    /**
     * 原价
     */
    @ExcelField(title = "原价", align = 2, sort = 3)
    private Double product_price;

    /**
     * 促销价
     */
    @ExcelField(title = "促销价", align = 2, sort = 4)
    private Double product_sale_price;

}
