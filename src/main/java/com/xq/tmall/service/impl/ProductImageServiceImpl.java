package com.xq.tmall.service.impl;

import com.xq.tmall.dao.ProductImageMapper;
import com.xq.tmall.entity.ProductImage;
import com.xq.tmall.service.ProductImageService;
import com.xq.tmall.util.PageUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductImageServiceImpl implements ProductImageService {

    private final ProductImageMapper productImageMapper;

    @Override
    public boolean add(ProductImage productImage) {
        return productImageMapper.insertOne(productImage) > 0;
    }

    @Override
    public boolean addList(List<ProductImage> productImageList) {
        return productImageMapper.insertList(productImageList) > 0;
    }

    @Override
    public boolean update(ProductImage productImage) {
        return productImageMapper.updateOne(productImage) > 0;
    }

    @Override
    public boolean deleteList(Integer[] productImage_id_list) {
        return productImageMapper.deleteList(productImage_id_list) > 0;
    }

    @Override
    public List<ProductImage> getList(Integer product_id, Byte productImage_type, PageUtil pageUtil) {
        return productImageMapper.selectProductImageList(product_id, productImage_type, pageUtil);
    }

    @Override
    public ProductImage get(Integer productImage_id) {
        return productImageMapper.selectOne(productImage_id);
    }

    @Override
    public Integer getTotal(Integer product_id, Byte productImage_type) {
        return productImageMapper.selectTotal(product_id, productImage_type);
    }
}
