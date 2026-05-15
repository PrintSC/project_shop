package com.xq.tmall.service;

import com.xq.tmall.entity.Address;

import java.util.List;

public interface AddressService {
    /**
     * 新增地址
     *
     * @param address
     * @return
     */
    boolean add(Address address);

    /**
     * 更新地址
     *
     * @param address
     * @return
     */
    boolean update(Address address);

    /**
     * 按条件查询地址
     *
     * @param address_name
     * @param address_regionId
     * @return
     */
    List<Address> getList(String address_name, String address_regionId);

    /**
     * 根据id查询地址
     *
     * @param address_areaId
     * @return
     */
    Address get(String address_areaId);

    /**
     * 查询地区编码==父级省市id的地址
     *
     * @return
     */
    List<Address> getRoot();
}
