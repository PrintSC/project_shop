package com.xq.tmall.controller.fore;

import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.LineCaptcha;
import com.alibaba.fastjson.JSONObject;
import com.xq.tmall.controller.BaseController;
import com.xq.tmall.entity.ApiVerCodeResp;
import com.xq.tmall.entity.User;
import com.xq.tmall.service.UserService;
import com.xq.tmall.util.AesUtil;
import com.xq.tmall.util.Constants;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.crypto.SecretKey;
import javax.servlet.http.HttpSession;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;

/**
 * 前台天猫-登陆页
 */
@Api(tags = "前台天猫-登陆页")
@Controller
@RequiredArgsConstructor
public class ForeLoginController extends BaseController {
    private final UserService userService;

    // 转到前台天猫-登录页 (返回JSON)
    @ApiOperation(value = "转到前台天猫-登录页", notes = "转到前台天猫-登录页")
    @ResponseBody
    @GetMapping(value = "login", produces = "application/json;charset=utf-8")
    public String goToPage() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("success", true);
        return String.valueOf(jsonObject);
    }

    // 登陆验证-ajax
    @ApiOperation(value = "登陆验证", notes = "登陆验证")
    @ResponseBody

    @PostMapping(value = "login/doLogin", produces = "application/json;charset=utf-8")
    public String checkLogin(HttpSession session, @RequestParam String username, @RequestParam String password, @RequestParam String captchaCode) throws Exception {
        JSONObject jsonObject = new JSONObject();

        // 验证码校验
        String expectedCode = session.getAttribute("captchaCode") != null ? session.getAttribute("captchaCode").toString() : null;
        session.removeAttribute("captchaCode");
        if (expectedCode == null || !expectedCode.equalsIgnoreCase(captchaCode)) {
            jsonObject.put("success", false);
            jsonObject.put("msg", "验证码错误！");
            return jsonObject.toString();
        }

        // 根据用户名查找用户
        User user = userService.getUserByName(username);
        System.out.println(user);

        if (user == null) {
            // 用户不存在
            jsonObject.put("success", false);
            jsonObject.put("msg", "用户名或密码错误！");
            return jsonObject.toString();
        }

        // 获取存储的盐值和IV，并解码为字节数组
        byte[] salt = Base64.getDecoder().decode(user.getSaltBase64());
        byte[] iv = Base64.getDecoder().decode(user.getIvBase64());

        // 从用户密码和盐生成AES密钥
        SecretKey secretKey = AesUtil.generateKeyFromPassword(password, salt);

        // 对原始密码进行AES加密
        String encryptedPassword = AesUtil.encrypt(password, secretKey, iv);

        // 验证加密后的密码是否与数据库中存储的密码一致
        if (!encryptedPassword.equals(user.getUser_password())) {
            // 密码不正确
            jsonObject.put("success", false);
            jsonObject.put("msg", "用户名或密码错误！");
            return jsonObject.toString();
        }

        // 登录成功
        session.setAttribute(Constants.USER_ID, user.getUser_id());
        jsonObject.put("success", true);
        jsonObject.put("msg", "登录成功");
        return jsonObject.toString();
    }

    // 退出当前账号 (返回JSON)
    @ApiOperation(value = "退出当前账号", notes = "退出当前账号")
    @ResponseBody
    @GetMapping(value = "login/logout", produces = "application/json;charset=utf-8")
    public String logout(HttpSession session) {
        Object o = session.getAttribute(Constants.USER_ID);
        if (o != null) {
            session.removeAttribute(Constants.USER_ID);
            session.invalidate();
        }
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(Constants.SUCCESS, true);
        return String.valueOf(jsonObject);
    }

    @ApiOperation(value = "登录验证码", notes = "登录验证码")
    @ResponseBody
    @GetMapping(value = "login/code")
    public ApiVerCodeResp getVerCode(HttpSession session) {
        LineCaptcha captcha = CaptchaUtil.createLineCaptcha(140, 38, 4, 60);
        String code = captcha.getCode().toLowerCase();
        session.setAttribute("captchaCode", code);
        return new ApiVerCodeResp(String.valueOf(UUID.randomUUID()), captcha.getImageBase64Data(), "");
    }
}
