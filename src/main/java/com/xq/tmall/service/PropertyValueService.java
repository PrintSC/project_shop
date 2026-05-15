package com.xq.tmall.service;

import com.xq.tmall.entity.PropertyValue;
import com.xq.tmall.util.PageUtil;

import java.util.List;

public interface PropertyValueService {
    /**
     * 新增产品属性
     *
     * @param propertyValue
     * @return
     */
    boolean add(PropertyValue propertyValue);

    /**
     * 批量新增产品属性
     *
     * @param propertyValueList
     * @return
     */

    boolean addList(List<PropertyValue> propertyValueList);

    /**
     * 更新产品属性
     *
     * @param propertyValue
     * @return
     */
    boolean update(PropertyValue propertyValue);

    /**
     * 批量删除产品属性
     *
     * @param propertyValue_id_list
     * @return
     */
    boolean deleteList(Integer[] propertyValue_id_list);

    /**
     * 删除对应产品属性
     *
     * @param id
     * @return
     */
    boolean delete(Integer id);

    /**
     * 查询产品属性列表
     *
     * @param propertyValue
     * @param pageUtil
     * @return
     */
    List<PropertyValue> getList(PropertyValue propertyValue, PageUtil pageUtil);

    /**
     * 查询单条产品属性
     *
     * @param propertyValue_id
     * @return
     */
    PropertyValue get(Integer propertyValue_id);

    /**
     * 查询产品属性总数
     *
     * @param propertyValue
     * @return
     */
    Integer getTotal(PropertyValue propertyValue);
}
