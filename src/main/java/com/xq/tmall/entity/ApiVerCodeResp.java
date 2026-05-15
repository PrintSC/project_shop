package com.xq.tmall.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 验证码
 *
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(value = "ApiVerCodeResp", description = "验证码")
public class ApiVerCodeResp implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "verToken")
    private String verToken;

    @ApiModelProperty(value = "验证码图片")
    private String img;

    @ApiModelProperty(value = "验证码")
    private String code;
}
