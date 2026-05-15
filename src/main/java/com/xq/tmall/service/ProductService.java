package com.xq.tmall.service;

import com.xq.tmall.entity.Product;
import com.xq.tmall.util.OrderUtil;
import com.xq.tmall.util.PageUtil;

import java.util.List;

public interface ProductService {
    /**
     * 新增产品
     *
     * @param product
     * @return
     */
    boolean add(Product product);

    /**
     * 更新产品
     *
     * @param product
     * @return
     */
    boolean update(Product product);

    /**
     * 删除产品
     *
     * @param id
     * @return
     */
    boolean delete(Integer id);

    /**
     * 查询列表
     *
     * @param product
     * @param product_isEnabled_array
     * @param orderUtil
     * @param pageUtil
     * @return
     */
    List<Product> getList(Product product, Byte[] product_isEnabled_array, OrderUtil orderUtil, PageUtil pageUtil);

    /**
     * 查询产品标题列表
     *
     * @param product
     * @param pageUtil
     * @return
     */
    List<Product> getTitle(Product product, PageUtil pageUtil);

    /**
     * 查询单条产品
     *
     * @param product_Id
     * @return
     */
    Product get(Integer product_Id);

    /**
     * 查询产品总数
     *
     * @param product
     * @param product_isEnabled_array
     * @return
     */
    Integer getTotal(Product product, Byte[] product_isEnabled_array);

    /**
     * 获取组合商品列表
     *
     * @param product
     * @param bytes
     * @param orderUtil
     * @param pageUtil
     * @param product_name_split
     * @return
     */
    List<Product> getMoreList(Product product, Byte[] bytes, OrderUtil orderUtil, PageUtil pageUtil, String[] product_name_split);

    /**
     * 按组合条件获取产品总数量
     *
     * @param product
     * @param bytes
     * @param product_name_split
     * @return
     */
    Integer getMoreListTotal(Product product, Byte[] bytes, String[] product_name_split);

    /**
     * 批量添加产品
     *
     * @param infos
     * @return
     */
    int saveScheme(List<Product> infos);
}
