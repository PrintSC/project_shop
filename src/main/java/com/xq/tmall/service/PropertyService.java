package com.xq.tmall.service;

import com.xq.tmall.entity.Property;
import com.xq.tmall.util.PageUtil;

import java.util.List;

public interface PropertyService {
    /**
     * 新增类别属性
     *
     * @param property
     * @return
     */
    boolean add(Property property);

    /**
     * 批量新增类别属性
     *
     * @param propertyList
     * @return
     */
    boolean addList(List<Property> propertyList);

    /**
     * 更新类别属性
     *
     * @param property
     * @return
     */
    boolean update(Property property);

    /**
     * 批量删除类别属性
     *
     * @param property_id_list
     * @return
     */
    boolean deleteList(Integer[] property_id_list);

    /**
     * 查询类别属性列表
     *
     * @param property
     * @param pageUtil
     * @return
     */
    List<Property> getList(Property property, PageUtil pageUtil);

    /**
     * 查询单条类别属性
     *
     * @param property_id
     * @return
     */
    Property get(Integer property_id);

    /**
     * 查询类别属性总数
     *
     * @param property
     * @return
     */
    Integer getTotal(Property property);

    /**
     * 删除类别属性
     * @param propertyId
     * @return
     */
    boolean delete(Integer propertyId);
}
