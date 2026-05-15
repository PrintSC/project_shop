package com.xq.tmall.controller.admin;

import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.druid.util.StringUtils;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.xq.tmall.controller.BaseController;
import com.xq.tmall.entity.Address;
import com.xq.tmall.entity.Product;
import com.xq.tmall.entity.ProductOrderItem;
import com.xq.tmall.entity.User;
import com.xq.tmall.service.*;
import com.xq.tmall.util.Constants;
import com.xq.tmall.util.OrderUtil;
import com.xq.tmall.util.PageUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.List;
import java.util.Stack;

@Api(tags = "后台管理-用户页")
@Controller
@RequiredArgsConstructor
public class UserController extends BaseController {
    private final UserService userService;
    private final AddressService addressService;
    private final ProductOrderItemService productOrderItemService;
    private final ProductService productService;
    private final ProductImageService productImageService;

    // 转到后台管理-用户页 (返回JSON)
    @ApiOperation(value = "转到后台管理-用户页", notes = "转到后台管理-用户页")
    @ResponseBody
    @GetMapping(value = "admin/user", produces = "application/json;charset=utf-8")
    public String goUserManagePage(HttpSession session) {
        Object adminId = checkAdmin(session);
        if (adminId == null) {
            JSONObject obj = new JSONObject();
            obj.put("success", false);
            obj.put("message", "未登录");
            return String.valueOf(obj);
        }
        PageUtil pageUtil = new PageUtil(0, 10);
        List<User> userList = userService.getList(null, null, pageUtil);
        Integer userCount = userService.getTotal(null);
        pageUtil.setTotal(userCount);
        JSONObject object = new JSONObject();
        object.put("userList", JSON.parseArray(JSON.toJSONString(userList)));
        object.put("userCount", userCount);
        object.put("totalPage", pageUtil.getTotalPage());
        object.put("pageUtil", pageUtil);
        object.put("success", true);
        return String.valueOf(object);
    }

    // 转到后台管理-用户详情页 (返回JSON)
    @ApiOperation(value = "转到后台管理-用户详情页", notes = "转到后台管理-用户详情页")
    @ResponseBody
    @GetMapping(value = "admin/user/{uid}", produces = "application/json;charset=utf-8")
    public String getUserById(HttpSession session, @PathVariable Integer uid) {
        Object adminId = checkAdmin(session);
        if (adminId == null) {
            JSONObject obj = new JSONObject();
            obj.put("success", false);
            obj.put("message", "未登录");
            return String.valueOf(obj);
        }
        User user = userService.get(uid);
        Address address = addressService.get(user.getUser_address().getAddress_areaId());
        Stack<String> addressStack = new Stack<>();
        addressStack.push(address.getAddress_name() + " ");
        while (!address.getAddress_areaId().equals(address.getAddress_regionId().getAddress_areaId())) {
            address = addressService.get(address.getAddress_regionId().getAddress_areaId());
            addressStack.push(address.getAddress_name() + " ");
        }
        StringBuilder builder = new StringBuilder();
        while (!addressStack.empty()) {
            builder.append(addressStack.pop());
        }
        Address add = new Address();
        add.setAddress_name(builder.toString());
        user.setUser_address(add);

        address = addressService.get(user.getUser_homeplace().getAddress_areaId());
        addressStack.push(address.getAddress_name() + " ");
        while (!address.getAddress_areaId().equals(address.getAddress_regionId().getAddress_areaId())) {
            address = addressService.get(address.getAddress_regionId().getAddress_areaId());
            addressStack.push(address.getAddress_name() + " ");
        }
        builder = new StringBuilder();
        while (!addressStack.empty()) {
            builder.append(addressStack.pop());
        }
        user.setUser_homeplace(add);

        List<ProductOrderItem> productOrderItemList = productOrderItemService.getListByUserId(user.getUser_id(), null);
        if (CollectionUtil.isNotEmpty(productOrderItemList)) {
            for (ProductOrderItem productOrderItem : productOrderItemList) {
                Integer productId = productOrderItem.getProductOrderItem_product().getProduct_id();
                Product product = productService.get(productId);
                if (product != null) {
                    product.setSingleProductImageList(productImageService.getList(productId, (byte) 0, new PageUtil(0, 1)));
                }
                productOrderItem.setProductOrderItem_product(product);
            }
        }
        user.setProductOrderItemList(productOrderItemList);
        if (!StringUtils.isEmpty(user.getUser_realname())) {
            user.setUser_realname(user.getUser_realname().charAt(0) + "*");
        } else {
            user.setUser_realname("未命名");
        }
        JSONObject object = new JSONObject();
        object.put("user", JSON.parseObject(JSON.toJSONString(user)));
        object.put("success", true);
        return String.valueOf(object);
    }

    // 按条件查询用户-ajax
    @ApiOperation(value = "按条件查询用户", notes = "按条件查询用户")
    @ResponseBody
    @GetMapping(value = "admin/user/{index}/{count}", produces = "application/json;charset=UTF-8")
    public String getUserBySearch(@RequestParam(required = false) String user_name,
                                  @RequestParam(required = false) Byte[] user_gender_array,
                                  @RequestParam(required = false) String orderBy,
                                  @RequestParam(required = false, defaultValue = "true") Boolean isDesc,
                                  @PathVariable Integer index,
                                  @PathVariable Integer count) throws UnsupportedEncodingException {
        Byte gender = null;
        if (user_gender_array != null && user_gender_array.length == 1) {
            gender = user_gender_array[0];
        }
        if (user_name != null) {
            user_name = "".equals(user_name) ? null : URLDecoder.decode(user_name, "UTF-8");
        }
        if (orderBy != null && "".equals(orderBy)) {
            orderBy = null;
        }
        User user = new User();
        user.setUser_name(user_name);
        user.setUser_gender(gender);
        OrderUtil orderUtil = null;
        if (orderBy != null) {
            orderBy = orderBy.replaceAll("[()]", "");
            orderUtil = new OrderUtil(orderBy, isDesc);
        }
        JSONObject object = new JSONObject();
        PageUtil pageUtil = new PageUtil(index, count);
        List<User> userList = userService.getList(user, orderUtil, pageUtil);
        object.put("userList", JSON.parseArray(JSON.toJSONString(userList)));
        Integer userCount = userService.getTotal(user);
        object.put("userCount", userCount);
        pageUtil.setTotal(userCount);
        object.put("totalPage", pageUtil.getTotalPage());
        object.put("pageUtil", pageUtil);
        return String.valueOf(object);
    }

    // 按ID删除用户并返回最新结果-ajax
    @ApiOperation(value = "按ID删除用户并返回最新结果", notes = "按ID删除用户并返回最新结果")
    @ResponseBody
    @GetMapping(value = "admin/user/del/{id}", produces = "application/json;charset=utf-8")
    public String deleteProductById(@PathVariable Integer id) {
        JSONObject object = new JSONObject();
        User user = userService.get(id);
        user.setDel_flag(1);
        boolean yn = userService.update(user);
        if (yn) {
            object.put(Constants.SUCCESS, true);
        } else {
            object.put(Constants.SUCCESS, false);
            throw new RuntimeException();
        }
        return String.valueOf(object);
    }
}
