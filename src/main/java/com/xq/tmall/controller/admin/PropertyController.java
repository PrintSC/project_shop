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
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.List;


@Api(tags = "后台管理-属性页")
@Controller
@RequiredArgsConstructor
public class PropertyController extends BaseController {
    private final CategoryService categoryService;
    private final LastIDService lastIDService;
    private final PropertyService propertyService;

    // 转到后台管理-属性页 (返回JSON)
    @ApiOperation(value = "转到后台管理-属性页", notes = "转到后台管理-属性页")
    @ResponseBody
    @GetMapping(value = "admin/property", produces = "application/json;charset=utf-8")
    public String goToPage(HttpSession session) {
        Object adminId = checkAdmin(session);
        if (adminId == null) {
            JSONObject obj = new JSONObject();
            obj.put("success", false);
            obj.put("message", "未登录");
            return String.valueOf(obj);
        }
        PageUtil pageUtil = new PageUtil(0, 10);
        List<Property> propertyList = propertyService.getList(null, pageUtil);
        Integer propertyCount = propertyService.getTotal(null);
        List<Category> categoryList = categoryService.getList(null, null);
        pageUtil.setTotal(propertyCount);
        JSONObject object = new JSONObject();
        object.put("propertyList", JSONArray.parseArray(JSON.toJSONString(propertyList)));
        object.put("propertyCount", propertyCount);
        object.put("categoryList", JSONArray.parseArray(JSON.toJSONString(categoryList)));
        object.put("totalPage", pageUtil.getTotalPage());
        object.put("pageUtil", pageUtil);
        object.put("success", true);
        return String.valueOf(object);
    }

    // 转到后台管理-属性详情页 (返回JSON)
    @ApiOperation(value = "转到后台管理-属性详情页", notes = "转到后台管理-属性详情页")
    @ResponseBody
    @GetMapping(value = "admin/property/{cid}", produces = "application/json;charset=utf-8")
    public String goToDetailsPage(HttpSession session, @PathVariable Integer cid) {
        Object adminId = checkAdmin(session);
        if (adminId == null) {
            JSONObject obj = new JSONObject();
            obj.put("success", false);
            obj.put("message", "未登录");
            return String.valueOf(obj);
        }
        JSONObject object = new JSONObject();
        object.put("property", JSON.parseObject(JSON.toJSONString(propertyService.get(cid))));
        List<Category> categoryList = categoryService.getList(null, null);
        object.put("categoryList", JSONArray.parseArray(JSON.toJSONString(categoryList)));
        object.put("success", true);
        return String.valueOf(object);
    }

    // 转到后台管理-属性添加页 (返回JSON)
    @ApiOperation(value = "转到后台管理-属性添加页", notes = "转到后台管理-属性添加页")
    @ResponseBody
    @GetMapping(value = "admin/property/new", produces = "application/json;charset=utf-8")
    public String goToAddPage(HttpSession session) {
        Object adminId = checkAdmin(session);
        if (adminId == null) {
            JSONObject obj = new JSONObject();
            obj.put("success", false);
            obj.put("message", "未登录");
            return String.valueOf(obj);
        }
        JSONObject object = new JSONObject();
        List<Category> categoryList = categoryService.getList(null, null);
        object.put("categoryList", JSONArray.parseArray(JSON.toJSONString(categoryList)));
        object.put("success", true);
        return String.valueOf(object);
    }

    // 添加属性信息-ajax
    @ApiOperation(value = "添加属性信息", notes = "添加属性信息")
    @ResponseBody
    @PostMapping(value = "admin/property/{category_id}", produces = "application/json;charset=utf-8")
    public String addCategory(@RequestParam String property_name, @PathVariable("category_id") Integer category_id) {
        JSONObject jsonObject = new JSONObject();
        Property property = new Property();
        property.setProperty_name(property_name);
        Category category = new Category();
        category.setCategory_id(category_id);
        property.setProperty_category(category);
        boolean yn = propertyService.add(property);
        if (yn) {
            int property_id = lastIDService.selectLastID();
            jsonObject.put(Constants.SUCCESS, true);
            jsonObject.put("property_id", property_id);
        } else {
            jsonObject.put(Constants.SUCCESS, false);
            throw new RuntimeException();
        }
        return String.valueOf(jsonObject);
    }

    // 更新属性信息-ajax
    @ApiOperation(value = "更新属性信息", notes = "更新属性信息")
    @ResponseBody
    @PutMapping(value = "admin/property/{property_id}/{category_id}", produces = "application/json;charset=utf-8")
    public String updateCategory(@RequestParam String property_name,
                                 @PathVariable("property_id") Integer property_id,
                                 @PathVariable("category_id") Integer category_id) {
        JSONObject jsonObject = new JSONObject();
        Property property = new Property();
        property.setProperty_id(property_id);
        property.setProperty_name(property_name);
        Category category = new Category();
        category.setCategory_id(category_id);
        property.setProperty_category(category);
        boolean yn = propertyService.update(property);
        if (yn) {
            jsonObject.put(Constants.SUCCESS, true);
            jsonObject.put("property_id", property_id);
        } else {
            jsonObject.put(Constants.SUCCESS, false);
            throw new RuntimeException();
        }
        return String.valueOf(jsonObject);
    }

    // 按条件查询属性-ajax
    @ApiOperation(value = "按条件查询属性", notes = "按条件查询属性")
    @ResponseBody
    @GetMapping(value = "admin/property/{index}/{count}", produces = "application/json;charset=utf-8")
    public String getCategoryBySearch(@RequestParam(required = false) String property_name,
                                      @RequestParam(required = false) Integer category_id,
                                      @PathVariable Integer index,
                                      @PathVariable Integer count) throws UnsupportedEncodingException {
        if (property_name != null) {
            property_name = "".equals(property_name) ? null : URLDecoder.decode(property_name, "UTF-8");
        }
        JSONObject object = new JSONObject();
        PageUtil pageUtil = new PageUtil(index, count);
        Property property = new Property();
        property.setProperty_name(property_name);
        Category category = new Category();
        category.setCategory_id(category_id);
        property.setProperty_category(category);
        List<Property> propertyList = propertyService.getList(property, pageUtil);
        object.put("propertyList", JSONArray.parseArray(JSON.toJSONString(propertyList)));
        Integer propertyCount = propertyService.getTotal(property);
        object.put("propertyCount", propertyCount);
        pageUtil.setTotal(propertyCount);
        object.put("totalPage", pageUtil.getTotalPage());
        object.put("pageUtil", pageUtil);
        return String.valueOf(object);
    }

    // 按ID删除属性并返回最新结果-ajax
    @ApiOperation(value = "按ID删除属性并返回最新结果", notes = "按ID删除属性并返回最新结果")
    @ResponseBody
    @GetMapping(value = "admin/property/del/{id}", produces = "application/json;charset=utf-8")
    public String deleteProductById(@PathVariable Integer id) {
        JSONObject object = new JSONObject();
        boolean yn = propertyService.delete(id);
        if (yn) {
            object.put(Constants.SUCCESS, true);
        } else {
            object.put(Constants.SUCCESS, false);
            throw new RuntimeException();
        }
        return String.valueOf(object);
    }
}
