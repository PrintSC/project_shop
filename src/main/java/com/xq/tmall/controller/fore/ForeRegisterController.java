package com.xq.tmall.controller.fore;

import com.alibaba.fastjson.JSONObject;
import com.xq.tmall.controller.BaseController;
import com.xq.tmall.entity.Address;
import com.xq.tmall.entity.User;
import com.xq.tmall.service.AddressService;
import com.xq.tmall.service.UserService;
import com.xq.tmall.util.AesUtil;
import com.xq.tmall.util.Md5Util;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.crypto.SecretKey;
import java.util.List;
import java.util.Map;

/**
 * 前台天猫-用户注册
 */
@Api(tags = "前台天猫-用户注册")
@Controller
@RequiredArgsConstructor
public class ForeRegisterController extends BaseController {
    private final AddressService addressService;
    private final UserService userService;

    // 转到前台天猫-用户注册页 (返回JSON)
    @ApiOperation(value = "转到前台天猫-用户注册页", notes = "转到前台天猫-用户注册页")
    @ResponseBody
    @GetMapping(value = "register", produces = "application/json;charset=utf-8")
    public String goToPage() {
        String addressId = "110000";
        String cityAddressId = "110100";
        List<Address> addressList = addressService.getRoot();
        List<Address> cityAddress = addressService.getList(null, addressId);
        List<Address> districtAddress = addressService.getList(null, cityAddressId);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("addressList", com.alibaba.fastjson.JSON.parseArray(com.alibaba.fastjson.JSON.toJSONString(addressList)));
        jsonObject.put("cityList", com.alibaba.fastjson.JSON.parseArray(com.alibaba.fastjson.JSON.toJSONString(cityAddress)));
        jsonObject.put("districtList", com.alibaba.fastjson.JSON.parseArray(com.alibaba.fastjson.JSON.toJSONString(districtAddress)));
        jsonObject.put("addressId", addressId);
        jsonObject.put("cityAddressId", cityAddressId);
        jsonObject.put("success", true);
        return String.valueOf(jsonObject);
    }

    // 天猫前台-用户注册-ajax
    @ApiOperation(value = "天猫前台-用户注册", notes = "天猫前台-用户注册")
    @ResponseBody
    @PostMapping(value = "register/doRegister", produces = "application/json;charset=UTF-8")

    public String register(
            @RequestParam(value = "user_name") String user_name, /* 用户名 */
            @RequestParam(value = "user_nickname") String user_nickname, /* 用户昵称 */
            @RequestParam(value = "user_password") String user_password, /* 用户密码 */
            @RequestParam(value = "user_gender") String user_gender, /* 用户性别 */
            @RequestParam(value = "user_birthday") String user_birthday, /* 用户生日 */
            @RequestParam(value = "user_address") String user_address /* 用户所在地 */) throws Exception {

        // 验证用户名是否存在
        User user1 = new User();
        user1.setUser_name(user_name);
        Integer count = userService.getTotal(user1);
        if (count > 0) {
            // 用户名已存在，返回错误信息!
            JSONObject object = new JSONObject();
            object.put("success", false);
            object.put("msg", "用户名已存在，请重新输入！");
            return object.toString(); // 使用toString()方法转换为字符串
        }

        // 创建用户对象并设置基本信息
        User user = new User();
        user.setUser_name(user_name);
        user.setUser_nickname(user_nickname);
        user.setUser_gender(Byte.valueOf(user_gender));
        user.setUser_birthday(user_birthday);

        // 地址对象
        Address address = new Address();
        address.setAddress_areaId(user_address);
        user.setUser_address(address);
        user.setUser_homeplace(address); // 如果家乡和所在地相同，则可以这样设置
        user.setUser_realname(user_name); // 可能需要根据实际情况调整
        user.setDel_flag(0);

        // 密码加密
        byte[] salt = AesUtil.generateSalt(); // 生成盐值
        byte[] iv = AesUtil.generateIv();     // 生成初始化向量

        // 从用户密码和盐生成AES密钥
        SecretKey secretKey = AesUtil.generateKeyFromPassword(user_password, salt);

        // 对原始密码进行AES加密
        String encryptedPassword = AesUtil.encrypt(user_password, secretKey, iv);

        // 设置加密后的密码、盐值和IV到用户对象
        user.setUser_password(encryptedPassword);
        user.setUser_salt(salt);
        user.setUser_iv(iv);

        // 用户注册
        if (userService.add(user)) {
            // 注册成功
            JSONObject object = new JSONObject();
            object.put("success", true);
            return object.toString(); // 使用toString()方法转换为字符串
        } else {
            throw new RuntimeException("用户注册失败");
        }
    }


//    public String register(@RequestParam(value = "user_name") String user_name  /*用户名 */, @RequestParam(value = "user_nickname") String user_nickname  /*用户昵称 */, @RequestParam(value = "user_password") String user_password  /*用户密码*/, @RequestParam(value = "user_gender") String user_gender  /*用户性别*/, @RequestParam(value = "user_birthday") String user_birthday /*用户生日*/, @RequestParam(value = "user_address") String user_address  /*用户所在地 */) throws Exception {
//        // 验证用户名是否存在
//        User user1 = new User();
//        user1.setUser_name(user_name);
//        Integer count = userService.getTotal(user1);
//        if (count > 0) {
//            // 用户名已存在，返回错误信息!
//            JSONObject object = new JSONObject();
//            object.put("success", false);
//            object.put("msg", "用户名已存在，请重新输入！");
//            return String.valueOf(object);
//        }
//        // 创建用户对象
//        User user = new User();
//        user.setUser_name(user_name);
//        user.setUser_nickname(user_nickname);
//
//        user.setUser_gender(Byte.valueOf(user_gender));
//        user.setUser_birthday(user_birthday);
//        // 地址对象
//        Address address = new Address();
//        address.setAddress_areaId(user_address);
//        user.setUser_address(address);
//        user.setUser_homeplace(address);
//        user.setUser_realname(user_name);
//        user.setDel_flag(0);
//        // 用户注册
//        if (userService.add(user)) {
//            // 注册成功
//            JSONObject object = new JSONObject();
//            object.put("success", true);
//            return String.valueOf(object);
//        } else {
//            throw new RuntimeException();
//        }
//    }
}
