package com.xq.tmall.service;

import com.xq.tmall.entity.User;
import com.xq.tmall.util.OrderUtil;
import com.xq.tmall.util.PageUtil;

import java.util.List;

public interface UserService {
    /**
     * 新增用户
     *
     * @param user
     * @return
     */
    boolean add(User user);

    /**
     * 更新用户
     *
     * @param user
     * @return
     */
    boolean update(User user);

    /**
     * 查询用户列表
     *
     * @param user
     * @param orderUtil
     * @param pageUtil
     * @return
     */
    List<User> getList(User user, OrderUtil orderUtil, PageUtil pageUtil);

    /**
     * 查询单条用户
     *
     * @param user_id
     * @return
     */
    User get(Integer user_id);

    /**
     * 查询用户登录信息
     *
     * @param user_name
     * @param user_password
     * @return
     */
    User login(String user_name, String user_password);

    /**
     * 查询用户总数
     *
     * @param user
     * @return
     */
    Integer getTotal(User user);

    User getUserByName(String user_name);
}
