package com.xq.tmall.service;

import com.xq.tmall.entity.ProductImage;
import com.xq.tmall.util.PageUtil;

import java.util.List;

public interface ProductImageService {
    /**
     * 新增产品图片
     *
     * @param productImage
     * @return
     */
    boolean add(ProductImage productImage);

    /**
     * 批量新增产品图片
     *
     * @param productImageList
     * @return
     */
    boolean addList(List<ProductImage> productImageList);

    /**
     * 更新产品图片
     *
     * @param productImage
     * @return
     */
    boolean update(ProductImage productImage);

    /**
     * 批量删除产品图片
     *
     * @param productImage_id_list
     * @return
     */
    boolean deleteList(Integer[] productImage_id_list);

    /**
     * 查询产品图片列表
     *
     * @param product_id
     * @param productImage_type
     * @param pageUtil
     * @return
     */
    List<ProductImage> getList(Integer product_id, Byte productImage_type, PageUtil pageUtil);

    /**
     * 查询单条产品图片
     *
     * @param productImage_id
     * @return
     */
    ProductImage get(Integer productImage_id);

    /**
     * 查询产品图片总数
     *
     * @param product_id
     * @param productImage_type
     * @return
     */
    Integer getTotal(Integer product_id, Byte productImage_type);
}
