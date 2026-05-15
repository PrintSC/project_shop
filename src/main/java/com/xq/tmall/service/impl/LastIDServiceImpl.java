package com.xq.tmall.service.impl;

import com.xq.tmall.dao.LastIDMapper;
import com.xq.tmall.service.LastIDService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LastIDServiceImpl implements LastIDService {
    private final LastIDMapper lastIDMapper;

    @Override
    public int selectLastID() {
        return lastIDMapper.selectLastID();
    }
}
