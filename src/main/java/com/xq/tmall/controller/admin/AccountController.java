package com.xq.tmall.controller.admin;

import com.alibaba.fastjson.JSONObject;
import com.xq.tmall.controller.BaseController;
import com.xq.tmall.entity.Admin;
import com.xq.tmall.service.AdminService;
import com.xq.tmall.util.Constants;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.IOException;
import java.util.UUID;

@Api(tags = "后台管理-账户页")
@Controller
@RequiredArgsConstructor
public class AccountController extends BaseController {
    @Value("${tmall.file-upload-path}")
    private String uploadPath;
    private final AdminService adminService;

    // 转到后台管理-账户页 (返回JSON)
    @ApiOperation(value = "转到后台管理-账户页", notes = "转到后台管理-账户页")
    @ResponseBody
    @GetMapping(value = "/admin/account", produces = "application/json;charset=utf-8")
    public String goToPage(HttpSession session) {
        Object adminId = checkAdmin(session);
        if (adminId == null) {
            JSONObject obj = new JSONObject();
            obj.put("success", false);
            obj.put("message", "未登录");
            return String.valueOf(obj);
        }
        Admin admin = adminService.get(null, Integer.parseInt(adminId.toString()));
        JSONObject object = new JSONObject();
        object.put("admin", JSONObject.parseObject(JSONObject.toJSONString(admin)));
        object.put("success", true);
        return String.valueOf(object);
    }

    // 退出当前账号 (返回JSON)
    @ApiOperation(value = "退出当前账号", notes = "退出当前账号")
    @ResponseBody
    @GetMapping(value = "/admin/account/logout", produces = "application/json;charset=utf-8")
    public String logout(HttpSession session) {
        Object o = session.getAttribute(Constants.ADMIN_ID);
        if (o != null) {
            session.removeAttribute(Constants.ADMIN_ID);
            session.invalidate();
        }
        JSONObject object = new JSONObject();
        object.put(Constants.SUCCESS, true);
        return String.valueOf(object);
    }

    // 管理员头像上传
    @ApiOperation(value = "管理员头像上传", notes = "管理员头像上传")
    @ResponseBody
    @PostMapping(value = "/admin/uploadAdminHeadImage", produces = "application/json;charset=UTF-8")
    public String uploadAdminHeadImage(@RequestParam MultipartFile file, HttpSession session) {
        String originalFileName = file.getOriginalFilename();
        if (StringUtils.isEmpty(originalFileName)) {
            throw new RuntimeException("上传失败！");
        }
        String extension = originalFileName.substring(originalFileName.lastIndexOf('.'));
        String allowedExtensions = ".jpg,.jpeg,.png,.gif";
        if (!allowedExtensions.contains(extension)) {
            extension = ".jpg";
        }
        String fileName = UUID.randomUUID() + extension;
        String filePath = uploadPath + "/res/images/item/adminProfilePicture/" + fileName;
        JSONObject jsonObject = new JSONObject();
        try {
            File dest = new File(filePath);
            dest.getParentFile().mkdirs();
            file.transferTo(dest);
            jsonObject.put(Constants.SUCCESS, true);
            jsonObject.put("fileName", fileName);
        } catch (IOException e) {
            jsonObject.put(Constants.SUCCESS, false);
        }
        return String.valueOf(jsonObject);
    }

    // 更新管理员信息
    @ApiOperation(value = "更新管理员信息", notes = "更新管理员信息")
    @ResponseBody
    @PutMapping(value = "/admin/account/{admin_id}", produces = "application/json;charset=UTF-8")
    public String updateAdmin(HttpSession session, @RequestParam String admin_nickname,
                              @RequestParam(required = false) String admin_password,
                              @RequestParam(required = false) String admin_newPassword,
                              @RequestParam(required = false) String admin_profile_picture_src,
                              @PathVariable("admin_id") String admin_id) {
        Object adminId = checkAdmin(session);
        if (adminId == null) {
            JSONObject obj = new JSONObject();
            obj.put("success", false);
            obj.put("message", "未登录");
            return String.valueOf(obj);
        }
        JSONObject jsonObject = new JSONObject();
        Admin putAdmin = new Admin();
        putAdmin.setAdmin_id(Integer.valueOf(admin_id));
        putAdmin.setAdmin_nickname(admin_nickname);
        if (StringUtils.isNotEmpty(admin_password) && StringUtils.isNotEmpty(admin_newPassword)) {
            Admin admin = adminService.get(null, Integer.valueOf(adminId.toString()));
            if (adminService.login(admin.getAdmin_name(), admin_password) > 0) {
                putAdmin.setAdmin_password(admin_newPassword);
            } else {
                jsonObject.put(Constants.SUCCESS, false);
                jsonObject.put("message", "原密码输入有误！");
                return String.valueOf(jsonObject);
            }
        }
        if (StringUtils.isNotEmpty(admin_profile_picture_src)) {
            putAdmin.setAdmin_profile_picture_src(admin_profile_picture_src.substring(admin_profile_picture_src.lastIndexOf("/") + 1));
        }
        Boolean yn = adminService.update(putAdmin);
        if (yn) {
            jsonObject.put(Constants.SUCCESS, true);
            session.removeAttribute(Constants.ADMIN_ID);
            session.invalidate();
        } else {
            jsonObject.put(Constants.SUCCESS, false);
            throw new RuntimeException();
        }
        return String.valueOf(jsonObject);
    }
}
