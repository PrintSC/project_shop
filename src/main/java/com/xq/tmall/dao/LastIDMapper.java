package com.xq.tmall.dao;

import org.springframework.stereotype.Repository;

@Repository
public interface LastIDMapper {
    /**
     * 查询最新id
     * @return
     */
    int selectLastID();
}
