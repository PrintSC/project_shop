package com.xq.tmall.controller.admin;

import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.LineCaptcha;
import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSONObject;
import com.xq.tmall.controller.BaseController;
import com.xq.tmall.entity.Admin;
import com.xq.tmall.entity.ApiVerCodeResp;
import com.xq.tmall.service.AdminService;
import com.xq.tmall.util.Constants;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;
import java.util.UUID;

/**
 * 后台管理-登录页
 */
@Api(tags = "后台管理-登录页")
@Controller
@RequiredArgsConstructor
public class AdminLoginController extends BaseController {
    private final AdminService adminService;

    // 转到后台管理-登录页 (返回JSON给Vue前端)
    @ApiOperation(value = "转到后台管理-登录页", notes = "转到后台管理-登录页")
    @ResponseBody
    @GetMapping(value = "admin/login", produces = "application/json;charset=utf-8")
    public String goToPage() {
        JSONObject object = new JSONObject();
        object.put("success", true);
        return String.valueOf(object);
    }

    // 登陆验证-ajax
    @ApiOperation(value = "登陆验证", notes = "登陆验证")
    @ResponseBody
    @PostMapping(value = "admin/login/doLogin", produces = "application/json;charset=utf-8")
    public String checkLogin(HttpSession session, @RequestParam String username, @RequestParam String password, @RequestParam String captchaCode) {
        JSONObject object = new JSONObject();
        // 验证码校验
        String expectedCode = session.getAttribute("adminCaptchaCode") != null ? session.getAttribute("adminCaptchaCode").toString() : null;
        session.removeAttribute("adminCaptchaCode");
        if (expectedCode == null || !expectedCode.equalsIgnoreCase(captchaCode)) {
            object.put(Constants.SUCCESS, false);
            object.put("msg", "验证码错误！");
            return String.valueOf(object);
        }
        // 管理员登录验证
        Admin admin = adminService.getAdmin(username, password);
        if (ObjectUtil.isEmpty(admin)) {
            // 登录验证失败
            object.put(Constants.SUCCESS, false);
        } else {
            // 登录验证成功，管理员ID传入会话
            session.setAttribute(Constants.ADMIN_ID, admin.getAdmin_id());
            object.put(Constants.SUCCESS, true);
        }
        return String.valueOf(object);
    }

    // 获取管理员头像路径-ajax
    @ApiOperation(value = "获取管理员头像路径", notes = "获取管理员头像路径")
    @ResponseBody
    @GetMapping(value = "admin/login/profile_picture", produces = "application/json;charset=utf-8")
    public String getAdminProfilePicture(@RequestParam String username) {
        // 根据用户名获取管理员头像路径
        Admin admin = adminService.get(username, null);
        JSONObject object = new JSONObject();
        if (admin == null) {
            // 未找到头像路径
            object.put(Constants.SUCCESS, false);
        } else {
            // 成功获取头像路径
            object.put(Constants.SUCCESS, true);
            object.put("srcString", admin.getAdmin_profile_picture_src());
        }
        return String.valueOf(object);
    }

    @ApiOperation(value = "获取验证码", notes = "获取图片验证码")
    @ResponseBody
    @GetMapping(value = "admin/login/code")
    public ApiVerCodeResp getVerCode(HttpSession session) {
        LineCaptcha captcha = CaptchaUtil.createLineCaptcha(200, 50, 4, 80);
        String code = captcha.getCode().toLowerCase();
        session.setAttribute("adminCaptchaCode", code);
        return new ApiVerCodeResp(String.valueOf(UUID.randomUUID()), captcha.getImageBase64Data(), "");
    }
}