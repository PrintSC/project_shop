package com.xq.tmall.service;

import com.xq.tmall.entity.Review;
import com.xq.tmall.util.OrderUtil;
import com.xq.tmall.util.PageUtil;

import java.util.List;

public interface ReviewService {
    /**
     * 新增评论
     *
     * @param review
     * @return
     */
    boolean add(Review review);

    /**
     * 更新评论
     *
     * @param review
     * @return
     */
    boolean update(Review review);

    /**
     * 批量删除评论
     *
     * @param review_id_list
     * @return
     */
    boolean deleteList(Integer[] review_id_list);

    /**
     * 删除单条数据
     *
     * @param id
     * @return
     */
    boolean deleteData(Integer id);

    /**
     * 查询评论列表
     *
     * @param review
     * @param orderUtil
     * @param pageUtil
     * @return
     */
    List<Review> getList(Review review, OrderUtil orderUtil, PageUtil pageUtil);

    /**
     * 通过用户id查询评论
     *
     * @param user_id
     * @param pageUtil
     * @return
     */
    List<Review> getListByUserId(Integer user_id, PageUtil pageUtil);

    /**
     * 通过产品id查询评论
     *
     * @param product_id
     * @param pageUtil
     * @return
     */
    List<Review> getListByProductId(Integer product_id, PageUtil pageUtil);

    /**
     * 查询单条评论
     *
     * @param review_id
     * @return
     */
    Review get(Integer review_id);

    /**
     * 查询评论总数
     *
     * @param review
     * @return
     */
    Integer getTotal(Review review);

    /**
     * 根据用户id查询评论总数
     *
     * @param user_id
     * @return
     */
    Integer getTotalByUserId(Integer user_id);

    /**
     * 根据产品id查询评论总数
     *
     * @param product_id
     * @return
     */
    Integer getTotalByProductId(Integer product_id);

    /**
     * 根据产品订单id查询评论总数
     *
     * @param productOrderItem_id
     * @return
     */
    Integer getTotalByOrderItemId(Integer productOrderItem_id);
}
