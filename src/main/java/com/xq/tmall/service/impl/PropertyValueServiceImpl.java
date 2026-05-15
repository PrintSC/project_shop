package com.xq.tmall.service.impl;

import com.xq.tmall.dao.PropertyValueMapper;
import com.xq.tmall.entity.PropertyValue;
import com.xq.tmall.service.PropertyValueService;
import com.xq.tmall.util.PageUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PropertyValueServiceImpl implements PropertyValueService {
    private final PropertyValueMapper propertyValueMapper;

    @Override
    public boolean add(PropertyValue propertyValue) {
        return propertyValueMapper.insertOne(propertyValue) > 0;
    }

    @Override
    public boolean addList(List<PropertyValue> propertyValueList) {
        return propertyValueMapper.insertList(propertyValueList) > 0;
    }

    @Override
    public boolean update(PropertyValue propertyValue) {
        return propertyValueMapper.updateOne(propertyValue) > 0;
    }

    @Override
    public boolean deleteList(Integer[] propertyValue_id_list) {
        return propertyValueMapper.deleteList(propertyValue_id_list) > 0;
    }

    @Override
    public boolean delete(Integer id) {
        return propertyValueMapper.deleteOne(id) > 0;
    }

    @Override
    public List<PropertyValue> getList(PropertyValue propertyValue, PageUtil pageUtil) {
        return propertyValueMapper.selectPropertyValueList(propertyValue, pageUtil);
    }

    @Override
    public PropertyValue get(Integer propertyValue_id) {
        return propertyValueMapper.selectOne(propertyValue_id);
    }

    @Override
    public Integer getTotal(PropertyValue propertyValue) {
        return propertyValueMapper.selectTotal(propertyValue);
    }
}
