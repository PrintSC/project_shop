package com.xq.tmall.service;

import com.xq.tmall.entity.OrderGroup;
import com.xq.tmall.entity.ProductOrder;
import com.xq.tmall.util.OrderUtil;
import com.xq.tmall.util.PageUtil;

import java.util.Date;
import java.util.List;

public interface ProductOrderService {
    /**
     * 新增产品订单
     *
     * @param productOrder
     * @return
     */
    boolean add(ProductOrder productOrder);

    /***
     * 更新产品订单
     * @param productOrder
     * @return
     */
    boolean update(ProductOrder productOrder);

    /**
     * 批量删除产品订单
     *
     * @param productOrder_id_list
     * @return
     */
    boolean deleteList(Integer[] productOrder_id_list);

    /**
     * 查询产品订单列表
     *
     * @param productOrder
     * @param productOrder_status_array
     * @param orderUtil
     * @param pageUtil
     * @return
     */
    List<ProductOrder> getList(ProductOrder productOrder, Byte[] productOrder_status_array, OrderUtil orderUtil, PageUtil pageUtil);

    /**
     * 查询订单组列表
     *
     * @param beginDate
     * @param endDate
     * @return
     */
    List<OrderGroup> getTotalByDate(Date beginDate, Date endDate);

    /**
     * 查询单条产品订单
     *
     * @param productOrder_id
     * @return
     */
    ProductOrder get(Integer productOrder_id);

    /**
     * 根据订单号查询产品订单
     *
     * @param productOrder_code
     * @return
     */
    ProductOrder getByCode(String productOrder_code);

    /**
     * 查询产品订单总数
     *
     * @param productOrder
     * @param productOrder_status_array
     * @return
     */
    Integer getTotal(ProductOrder productOrder, Byte[] productOrder_status_array);
}
