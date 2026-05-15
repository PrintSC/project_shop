package com.xq.tmall.controller.fore;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.xq.tmall.controller.BaseController;
import com.xq.tmall.entity.Address;
import com.xq.tmall.entity.User;
import com.xq.tmall.service.AddressService;
import com.xq.tmall.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

/**
 * 前台天猫-用户
 */
@Api(tags = "前台天猫-用户")
@Controller
@RequiredArgsConstructor
public class ForeUserController extends BaseController {
    @Value("${tmall.file-upload-path}")
    private String uploadPath;
    private final AddressService addressService;
    private final UserService userService;

    // 转到前台天猫-用户详情页 (返回JSON)
    @ApiOperation(value = "转到前台天猫-用户详情页", notes = "转到前台天猫-用户详情页")
    @ResponseBody
    @GetMapping(value = "userDetails", produces = "application/json;charset=utf-8")
    public String goToUserDetail(HttpSession session) {
        JSONObject jsonObject = new JSONObject();
        // 检查用户是否登录
        Object userId = checkUser(session);
        if (userId == null) {
            jsonObject.put("success", false);
            jsonObject.put("message", "未登录");
            return jsonObject.toString();
        }
        // 获取用户信息
        User user = userService.get(Integer.parseInt(userId.toString()));
        jsonObject.put("user", JSON.parseObject(JSON.toJSONString(user)));
        // 获取用户所在地区级地址
        String districtAddressId = user.getUser_address().getAddress_areaId();
        Address districtAddress = addressService.get(districtAddressId);
        // 获取市级地址信息
        Address cityAddress = addressService.get(districtAddress.getAddress_regionId().getAddress_areaId());
        // 获取其他地址信息
        List<Address> addressList = addressService.getRoot();
        List<Address> cityList = addressService.getList(null, cityAddress.getAddress_regionId().getAddress_areaId());
        List<Address> districtList = addressService.getList(null, cityAddress.getAddress_areaId());
        jsonObject.put("addressList", JSON.parseArray(JSON.toJSONString(addressList)));
        jsonObject.put("cityList", JSON.parseArray(JSON.toJSONString(cityList)));
        jsonObject.put("districtList", JSON.parseArray(JSON.toJSONString(districtList)));
        jsonObject.put("addressId", cityAddress.getAddress_regionId().getAddress_areaId());
        jsonObject.put("cityAddressId", cityAddress.getAddress_areaId());
        jsonObject.put("districtAddressId", districtAddressId);
        jsonObject.put("success", true);
        return jsonObject.toString();
    }

    // 前台天猫-用户更换头像
    @ApiOperation(value = "前台天猫-用户更换头像", notes = "前台天猫-用户更换头像")
    @ResponseBody
    @PostMapping(value = "user/uploadUserHeadImage", produces = "application/json;charset=utf-8")
    public String uploadUserHeadImage(@RequestParam MultipartFile file, HttpSession session
    ) {
        String originalFileName = file.getOriginalFilename();
        if (StringUtils.isEmpty(originalFileName)) {
            throw new RuntimeException("上传失败！");
        }
        // 获取图片原始文件名：{}, originalFileName
        String extension = originalFileName.substring(originalFileName.lastIndexOf('.'));
        // 允许的文件类型字符串
        String allowedExtensions = ".jpg,.jpeg,.png,.gif";
        
        // 如果文件类型不在允许范围内，修改为 .jpg 扩展名
        if (!allowedExtensions.contains(extension)) {
            extension = ".jpg";
        }
        String fileName = UUID.randomUUID() + extension;
        String filePath = uploadPath + "/res/images/item/userProfilePicture/" + fileName;
        // 文件上传路径：{}, filePath
        JSONObject jsonObject = new JSONObject();
        try {
            // 文件上传中...
            File dest = new File(filePath);
            dest.getParentFile().mkdirs();
            file.transferTo(dest);
            // 文件上传成功！
            jsonObject.put("success", true);
            jsonObject.put("fileName", fileName);
        } catch (IOException e) {
            logger.warn("文件上传失败！");
            e.printStackTrace();
            jsonObject.put("success", false);
        }
        return String.valueOf(jsonObject);
    }

    // 前台天猫-用户详情更新 (返回JSON)
    @ApiOperation(value = "前台天猫-用户详情更新", notes = "前台天猫-用户详情更新")
    @ResponseBody
    @PostMapping(value = "user/update", produces = "application/json;charset=utf-8")
    public String userUpdate(HttpSession session,
                             @RequestParam(value = "user_nickname") String user_nickname,
                             @RequestParam(value = "user_realname") String user_realname,
                             @RequestParam(value = "user_gender") String user_gender,
                             @RequestParam(value = "user_birthday") String user_birthday,
                             @RequestParam(value = "user_address") String user_address,
                             @RequestParam(value = "user_profile_picture_src", required = false) String user_profile_picture_src,
                             @RequestParam(value = "user_password", required = false) String user_password) {
        JSONObject jsonObject = new JSONObject();
        // 检查用户是否登录
        Object userId = checkUser(session);
        if (userId == null) {
            jsonObject.put("success", false);
            jsonObject.put("message", "未登录");
            return jsonObject.toString();
        }
        // 创建用户对象
        if (user_profile_picture_src != null && "".equals(user_profile_picture_src)) {
            user_profile_picture_src = null;
        }
        User userUpdate = new User();
        userUpdate.setUser_id(Integer.parseInt(userId.toString()));
        userUpdate.setUser_nickname(user_nickname);
        userUpdate.setUser_realname(user_realname);
        userUpdate.setUser_gender(Byte.valueOf(user_gender));
        userUpdate.setUser_birthday(user_birthday);
        Address address = new Address();
        address.setAddress_areaId(user_address);
        userUpdate.setUser_address(address);
        userUpdate.setUser_profile_picture_src(user_profile_picture_src);
        userUpdate.setUser_password(user_password);
        // 执行修改
        if (userService.update(userUpdate)) {
            jsonObject.put("success", true);
            return jsonObject.toString();
        }
        jsonObject.put("success", false);
        jsonObject.put("message", "更新失败");
        return jsonObject.toString();
    }
}
