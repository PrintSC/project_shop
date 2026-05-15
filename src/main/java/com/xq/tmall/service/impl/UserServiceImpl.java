package com.xq.tmall.service.impl;

import com.xq.tmall.dao.UserMapper;
import com.xq.tmall.entity.User;
import com.xq.tmall.service.UserService;
import com.xq.tmall.util.OrderUtil;
import com.xq.tmall.util.PageUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserMapper userMapper;

    @Override
    public boolean add(User user) {
        return userMapper.insertOne(user) > 0;
    }

    @Override
    public boolean update(User user) {
        return userMapper.updateOne(user) > 0;
    }

    @Override
    public List<User> getList(User user, OrderUtil orderUtil, PageUtil pageUtil) {
        return userMapper.selectUserList(user, orderUtil, pageUtil);
    }

    @Override
    public User get(Integer user_id) {
        return userMapper.selectOne(user_id);
    }

    @Override
    public User login(String user_name, String user_password) {
        return userMapper.selectByLogin(user_name, user_password);
    }

    @Override
    public Integer getTotal(User user) {
        return userMapper.selectTotal(user);
    }

    @Override
    public User getUserByName(String user_name) {

        long startTime = System.currentTimeMillis(); // 或者使用 System.nanoTime() 获取更精确的时间

        User user = userMapper.getUserByName(user_name);

        long endTime = System.currentTimeMillis(); // 或者使用 System.nanoTime()
        long duration = (endTime - startTime);  // 计算毫秒数

        // 打印或记录日志
        System.out.println("Query took " + duration + " ms");
        return user;
    }
}


