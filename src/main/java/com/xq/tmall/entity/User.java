package com.xq.tmall.entity;

import lombok.Data;

import java.util.Base64;
import java.util.List;

/**
 * 用户
 */
@Data
public class User {
    private Integer user_id;
    /**
     * 用户名
     */
    private String user_name;
    /**
     * 昵称
     */
    private String user_nickname;
    /**
     * 密码
     */
    private String user_password;
    /**
     * 姓名
     */
    private String user_realname;
    /**
     * 性别
     */
    private Byte user_gender;
    /**
     * 出生日期
     */
    private String user_birthday;
    /**
     * 所在地地址
     */
    private Address user_address;
    /**
     * 家乡
     */
    private Address user_homeplace;
    /**
     * 用户头像
     */
    private String user_profile_picture_src;
    /**
     * 删除标识(1删除 0未删除）
     */
    private Integer del_flag;
    /**
     * 评论
     */
    private List<Review> reviewList;
    /**
     * 产品订单项
     */
    private List<ProductOrderItem> productOrderItemList;
    /**
     * 产品订单
     */
    private List<ProductOrder> productOrderList;

    /**
     * 加密的盐值
     */
    private byte[] user_salt;

    /**
     * 初始化向量 (IV)
     */
    private byte[] user_iv;

    // 如果你需要在某些地方使用 Base64 编解码，可以保留这些方法
    // Getter for salt encoded as Base64 string for storage or transmission
    public String getSaltBase64() {
        return user_salt != null ? Base64.getEncoder().encodeToString(user_salt) : null;
    }

    // Setter for salt from a Base64 string, used when loading from storage or receiving over network
    public void setSaltBase64(String saltBase64) {
        this.user_salt = saltBase64 != null ? Base64.getDecoder().decode(saltBase64) : null;
    }

    // Getter for IV encoded as Base64 string for storage or transmission
    public String getIvBase64() {
        return user_iv != null ? Base64.getEncoder().encodeToString(user_iv) : null;
    }

    // Setter for IV from a Base64 string, used when loading from storage or receiving over network
    public void setIvBase64(String ivBase64) {
        this.user_iv = ivBase64 != null ? Base64.getDecoder().decode(ivBase64) : null;
    }
}