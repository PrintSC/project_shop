package com.xq.tmall.service;

import com.xq.tmall.entity.OrderGroup;
import com.xq.tmall.entity.ProductOrderItem;
import com.xq.tmall.util.PageUtil;

import java.util.Date;
import java.util.List;

public interface ProductOrderItemService {
    /**
     * 新增产品订单项
     *
     * @param productOrderItem
     * @return
     */
    boolean add(ProductOrderItem productOrderItem);

    /**
     * 更新产品订单项
     *
     * @param productOrderItem
     * @return
     */
    boolean update(ProductOrderItem productOrderItem);

    /**
     * 批量删除产品订单项
     *
     * @param productOrderItem_id_list
     * @return
     */
    boolean deleteList(Integer[] productOrderItem_id_list);

    /**
     * 查询产品订单项列表
     *
     * @param pageUtil
     * @return
     */
    List<ProductOrderItem> getList(PageUtil pageUtil);

    /**
     * 根据订单id查询产品订单项
     *
     * @param order_id
     * @param pageUtil
     * @return
     */
    List<ProductOrderItem> getListByOrderId(Integer order_id, PageUtil pageUtil);

    /**
     * 根据用户id查询产品订单项
     *
     * @param user_id
     * @param pageUtil
     * @return
     */
    List<ProductOrderItem> getListByUserId(Integer user_id, PageUtil pageUtil);

    /**
     * 根据产品id查询产品订单项
     *
     * @param product_id
     * @param pageUtil
     * @return
     */
    List<ProductOrderItem> getListByProductId(Integer product_id, PageUtil pageUtil);

    /**
     * 查询单条产品订单项
     *
     * @param productOrderItem_id
     * @return
     */
    ProductOrderItem get(Integer productOrderItem_id);

    /**
     * 查询产品订单项总数
     *
     * @return
     */
    Integer getTotal();

    /**
     * 根据订单id查询产品订单项总数
     *
     * @param order_id
     * @return
     */
    Integer getTotalByOrderId(Integer order_id);

    /**
     * 根据用户id查询产品订单项总数
     *
     * @param user_id
     * @return
     */
    Integer getTotalByUserId(Integer user_id);

    /**
     * 根据产品id查询订单组
     *
     * @param product_id
     * @param beginDate
     * @param endDate
     * @return
     */
    List<OrderGroup> getTotalByProductId(Integer product_id, Date beginDate, Date endDate);

    /**
     * 根据产品id查询产品订单项
     *
     * @param product_id
     * @return
     */
    Integer getSaleCountByProductId(Integer product_id);
}
