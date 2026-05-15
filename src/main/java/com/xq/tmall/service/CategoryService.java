package com.xq.tmall.service;

import com.xq.tmall.entity.Category;
import com.xq.tmall.util.PageUtil;

import java.util.List;

public interface CategoryService {
    /**
     * 新增类别
     *
     * @param category
     * @return
     */
    boolean add(Category category);

    /**
     * 更新类别
     *
     * @param category
     * @return
     */
    boolean update(Category category);

    /**
     * 查询类别列表
     *
     * @param category_name
     * @param pageUtil
     * @return
     */
    List<Category> getList(String category_name, PageUtil pageUtil);

    /**
     * 查询单条类别
     *
     * @param category_id
     * @return
     */
    Category get(Integer category_id);

    /**
     * 查询类别总数
     *
     * @param category_name
     * @return
     */
    Integer getTotal(String category_name);
}
