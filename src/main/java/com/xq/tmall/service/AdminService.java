package com.xq.tmall.service;

import com.xq.tmall.entity.Admin;
import com.xq.tmall.util.PageUtil;

import java.util.List;

public interface AdminService {
    /**
     * 新增管理员
     *
     * @param admin
     * @return
     */
    boolean add(Admin admin);

    /**
     * 更新管理员
     *
     * @param admin
     * @return
     */
    boolean update(Admin admin);

    /**
     * 查询管理员列表
     *
     * @param admin_name
     * @param pageUtil
     * @return
     */
    List<Admin> getList(String admin_name, PageUtil pageUtil);

    /**
     * 查询管理员
     *
     * @param admin_name
     * @param admin_id
     * @return
     */
    Admin get(String admin_name, Integer admin_id);

    /**
     * 查询管理员是否存在
     *
     * @param admin_name
     * @param admin_password
     * @return
     */
    Integer login(String admin_name, String admin_password);

    /**
     * 查询管理员总数
     *
     * @param admin_name
     * @return
     */
    Integer getTotal(String admin_name);

    /**
     * 查询管理员-通过用户名和密码
     *
     * @param userName
     * @param password
     * @return
     */
    Admin getAdmin(String userName, String password);
}
