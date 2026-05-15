package com.xq.tmall.controller.admin;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.xq.tmall.controller.BaseController;
import com.xq.tmall.entity.Category;
import com.xq.tmall.entity.Property;
import com.xq.tmall.service.CategoryService;
import com.xq.tmall.service.LastIDService;
import com.xq.tmall.service.PropertyService;
import com.xq.tmall.util.Constants;
import com.xq.tmall.util.PageUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.List;
import java.util.UUID;


@Api(tags = "后台管理-分类页")
@Controller
@RequiredArgsConstructor
public class CategoryController extends BaseController {
    @Value("${tmall.file-upload-path}")
    private String uploadPath;
    private final CategoryService categoryService;
    private final LastIDService lastIDService;
    private final PropertyService propertyService;

    // 转到后台管理-分类页 (返回JSON)
    @ApiOperation(value = "转到后台管理-分类页", notes = "转到后台管理-分类页")
    @ResponseBody
    @GetMapping(value = "admin/category", produces = "application/json;charset=utf-8")
    public String goToPage(HttpSession session) {
        Object adminId = checkAdmin(session);
        if (adminId == null) {
            JSONObject obj = new JSONObject();
            obj.put("success", false);
            obj.put("message", "未登录");
            return String.valueOf(obj);
        }
        PageUtil pageUtil = new PageUtil(0, 10);
        List<Category> categoryList = categoryService.getList(null, pageUtil);
        Integer categoryCount = categoryService.getTotal(null);
        pageUtil.setTotal(categoryCount);
        JSONObject object = new JSONObject();
        object.put("categoryList", JSONArray.parseArray(JSON.toJSONString(categoryList)));
        object.put("categoryCount", categoryCount);
        object.put("totalPage", pageUtil.getTotalPage());
        object.put("pageUtil", pageUtil);
        object.put("success", true);
        return String.valueOf(object);
    }

    // 转到后台管理-分类详情页 (返回JSON)
    @ApiOperation(value = "转到后台管理-分类详情页", notes = "转到后台管理-分类详情页")
    @ResponseBody
    @GetMapping(value = "admin/category/{cid}", produces = "application/json;charset=utf-8")
    public String goToDetailsPage(HttpSession session, @PathVariable Integer cid) {
        Object adminId = checkAdmin(session);
        if (adminId == null) {
            JSONObject obj = new JSONObject();
            obj.put("success", false);
            obj.put("message", "未登录");
            return String.valueOf(obj);
        }
        Category category = categoryService.get(cid);
        Property property = new Property();
        property.setProperty_category(category);
        category.setPropertyList(propertyService.getList(property, null));
        JSONObject object = new JSONObject();
        object.put("category", JSON.parseObject(JSON.toJSONString(category)));
        object.put("success", true);
        return String.valueOf(object);
    }

    // 转到后台管理-分类添加页 (返回JSON)
    @ApiOperation(value = "转到后台管理-分类添加页", notes = "转到后台管理-分类添加页")
    @ResponseBody
    @GetMapping(value = "admin/category/new", produces = "application/json;charset=utf-8")
    public String goToAddPage(HttpSession session) {
        Object adminId = checkAdmin(session);
        if (adminId == null) {
            JSONObject obj = new JSONObject();
            obj.put("success", false);
            obj.put("message", "未登录");
            return String.valueOf(obj);
        }
        JSONObject object = new JSONObject();
        object.put("success", true);
        return String.valueOf(object);
    }

    // 添加分类信息-ajax
    @ApiOperation(value = "添加分类信息", notes = "添加分类信息")
    @ResponseBody
    @PostMapping(value = "admin/category", produces = "application/json;charset=utf-8")
    public String addCategory(@RequestParam String category_name,
                              @RequestParam String category_image_src) {
        JSONObject jsonObject = new JSONObject();
        Category category = new Category();
        category.setCategory_name(category_name);
        category.setCategory_image_src(category_image_src.substring(category_image_src.lastIndexOf("/") + 1));
        boolean yn = categoryService.add(category);
        if (yn) {
            int category_id = lastIDService.selectLastID();
            jsonObject.put(Constants.SUCCESS, true);
            jsonObject.put("category_id", category_id);
        } else {
            jsonObject.put(Constants.SUCCESS, false);
            throw new RuntimeException();
        }
        return String.valueOf(jsonObject);
    }

    // 更新分类信息-ajax
    @ApiOperation(value = "更新分类信息", notes = "更新分类信息")
    @ResponseBody
    @PutMapping(value = "admin/category/{category_id}", produces = "application/json;charset=utf-8")
    public String updateCategory(@RequestParam String category_name,
                                 @RequestParam String category_image_src,
                                 @PathVariable("category_id") Integer category_id) {
        JSONObject jsonObject = new JSONObject();
        Category category = new Category();
        category.setCategory_id(category_id);
        category.setCategory_name(category_name);
        category.setCategory_image_src(category_image_src.substring(category_image_src.lastIndexOf("/") + 1));
        boolean yn = categoryService.update(category);
        if (yn) {
            jsonObject.put(Constants.SUCCESS, true);
            jsonObject.put("category_id", category_id);
        } else {
            jsonObject.put(Constants.SUCCESS, false);
            throw new RuntimeException();
        }
        return String.valueOf(jsonObject);
    }

    // 按条件查询分类-ajax
    @ApiOperation(value = "按条件查询分类", notes = "按条件查询分类")
    @ResponseBody
    @GetMapping(value = "admin/category/{index}/{count}", produces = "application/json;charset=utf-8")
    public String getCategoryBySearch(@RequestParam(required = false) String category_name,
                                      @PathVariable Integer index,
                                      @PathVariable Integer count) throws UnsupportedEncodingException {
        if (category_name != null) {
            category_name = "".equals(category_name) ? null : URLDecoder.decode(category_name, "UTF-8");
        }
        JSONObject object = new JSONObject();
        PageUtil pageUtil = new PageUtil(index, count);
        List<Category> categoryList = categoryService.getList(category_name, pageUtil);
        object.put("categoryList", JSONArray.parseArray(JSON.toJSONString(categoryList)));
        Integer categoryCount = categoryService.getTotal(category_name);
        object.put("categoryCount", categoryCount);
        pageUtil.setTotal(categoryCount);
        object.put("totalPage", pageUtil.getTotalPage());
        object.put("pageUtil", pageUtil);
        return String.valueOf(object);
    }

    // 上传分类图片-ajax
    @ApiOperation(value = "上传分类图片", notes = "上传分类图片")
    @ResponseBody
    @PostMapping(value = "admin/uploadCategoryImage", produces = "application/json;charset=utf-8")
    public String uploadCategoryImage(@RequestParam MultipartFile file, HttpSession session) {
        String originalFileName = file.getOriginalFilename();
        String extension = originalFileName.substring(originalFileName.lastIndexOf('.'));
        String allowedExtensions = ".jpg,.jpeg,.png,.gif";
        if (!allowedExtensions.contains(extension)) {
            extension = ".jpg";
        }
        String fileName = UUID.randomUUID() + extension;
        String filePath = uploadPath + "/res/images/item/categoryPicture/" + fileName;
        JSONObject object = new JSONObject();
        try {
            File dest = new File(filePath);
            dest.getParentFile().mkdirs();
            file.transferTo(dest);
            object.put(Constants.SUCCESS, true);
            object.put("fileName", fileName);
        } catch (IOException e) {
            logger.error("文件上传失败!");
        }
        return String.valueOf(object);
    }

    // 按ID删除分类并返回最新结果-ajax
    @ApiOperation(value = "按ID删除分类并返回最新结果", notes = "按ID删除分类并返回最新结果")
    @ResponseBody
    @GetMapping(value = "admin/category/del/{id}", produces = "application/json;charset=utf-8")
    public String deleteProductById(@PathVariable Integer id) {
        JSONObject object = new JSONObject();
        Category category = categoryService.get(id);
        category.setDel_flag(1);
        boolean yn = categoryService.update(category);
        if (yn) {
            object.put(Constants.SUCCESS, true);
        } else {
            object.put(Constants.SUCCESS, false);
            throw new RuntimeException();
        }
        return String.valueOf(object);
    }
}
