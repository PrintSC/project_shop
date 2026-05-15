package com.xq.tmall.service.impl;

import com.xq.tmall.dao.PropertyMapper;
import com.xq.tmall.entity.Property;
import com.xq.tmall.service.PropertyService;
import com.xq.tmall.util.PageUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PropertyServiceImpl implements PropertyService {
    private final PropertyMapper propertyMapper;

    @Override
    public boolean add(Property property) {
        return propertyMapper.insertOne(property) > 0;
    }

    @Override
    public boolean addList(List<Property> propertyList) {
        return propertyMapper.insertList(propertyList) > 0;
    }

    @Override
    public boolean update(Property property) {
        return propertyMapper.updateOne(property) > 0;
    }

    @Override
    public boolean deleteList(Integer[] property_id_list) {
        return propertyMapper.deleteList(property_id_list) > 0;
    }

    @Override
    public List<Property> getList(Property property, PageUtil pageUtil) {
        return propertyMapper.selectPropertyList(property, pageUtil);
    }

    @Override
    public Property get(Integer property_id) {
        return propertyMapper.selectOne(property_id);
    }

    @Override
    public Integer getTotal(Property property) {
        return propertyMapper.selectTotal(property);
    }

    @Override
    public boolean delete(Integer propertyId) {
        return propertyMapper.delete(propertyId);
    }
}
