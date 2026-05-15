package com.xq.tmall.controller.admin;

import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.xq.tmall.controller.BaseController;
import com.xq.tmall.entity.Address;
import com.xq.tmall.entity.Product;
import com.xq.tmall.entity.ProductOrder;
import com.xq.tmall.entity.ProductOrderItem;
import com.xq.tmall.service.*;
import com.xq.tmall.util.Constants;
import com.xq.tmall.util.OrderUtil;
import com.xq.tmall.util.PageUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.*;

@Api(tags = "后台管理-订单页")
@Controller
@RequiredArgsConstructor
public class OrderController extends BaseController {
    private final ProductOrderService productOrderService;
    private final AddressService addressService;
    private final UserService userService;
    private final ProductOrderItemService productOrderItemService;
    private final ProductService productService;
    private final ProductImageService productImageService;

    // 转到后台管理-订单页 (返回JSON)
    @ApiOperation(value = "转到后台管理-订单页", notes = "转到后台管理-订单页")
    @ResponseBody
    @GetMapping(value = "admin/order", produces = "application/json;charset=utf-8")
    public String goToPage(HttpSession session) {
        Object adminId = checkAdmin(session);
        if (adminId == null) {
            JSONObject obj = new JSONObject();
            obj.put("success", false);
            obj.put("message", "未登录");
            return String.valueOf(obj);
        }
        PageUtil pageUtil = new PageUtil(0, 10);
        List<ProductOrder> productOrderList = productOrderService.getList(null, null, new OrderUtil("productOrder_id", true), pageUtil);
        Integer productOrderCount = productOrderService.getTotal(null, null);
        pageUtil.setTotal(productOrderCount);
        JSONObject object = new JSONObject();
        object.put("productOrderList", JSON.parseArray(JSON.toJSONString(productOrderList)));
        object.put("productOrderCount", productOrderCount);
        object.put("totalPage", pageUtil.getTotalPage());
        object.put("pageUtil", pageUtil);
        object.put("success", true);
        return String.valueOf(object);
    }

    // 转到后台管理-订单详情页 (返回JSON)
    @ApiOperation(value = "转到后台管理-订单详情页", notes = "转到后台管理-订单详情页")
    @ResponseBody
    @GetMapping(value = "admin/order/{oid}", produces = "application/json;charset=utf-8")
    public String goToDetailsPage(HttpSession session, @PathVariable Integer oid) {
        Object adminId = checkAdmin(session);
        if (adminId == null) {
            JSONObject obj = new JSONObject();
            obj.put("success", false);
            obj.put("message", "未登录");
            return String.valueOf(obj);
        }
        ProductOrder order = productOrderService.get(oid);
        Address address = addressService.get(order.getProductOrder_address().getAddress_areaId());
        Stack<String> addressStack = new Stack<>();
        addressStack.push(order.getProductOrder_detail_address());
        addressStack.push(address.getAddress_name() + " ");
        while (!address.getAddress_areaId().equals(address.getAddress_regionId().getAddress_areaId())) {
            address = addressService.get(address.getAddress_regionId().getAddress_areaId());
            addressStack.push(address.getAddress_name() + " ");
        }
        StringBuilder builder = new StringBuilder();
        while (!addressStack.empty()) {
            builder.append(addressStack.pop());
        }
        order.setProductOrder_detail_address(builder.toString());
        order.setProductOrder_user(userService.get(order.getProductOrder_user().getUser_id()));
        List<ProductOrderItem> productOrderItemList = productOrderItemService.getListByOrderId(oid, null);
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
        order.setProductOrderItemList(productOrderItemList);
        JSONObject object = new JSONObject();
        object.put("order", JSON.parseObject(JSON.toJSONString(order)));
        object.put("success", true);
        return String.valueOf(object);
    }

    // 更新订单信息-ajax
    @ApiOperation(value = "更新订单信息", notes = "更新订单信息")
    @ResponseBody
    @PutMapping(value = "admin/order/{order_id}", produces = "application/json;charset=UTF-8")
    public String updateOrder(@PathVariable("order_id") String order_id) {
        JSONObject jsonObject = new JSONObject();
        ProductOrder productOrder = new ProductOrder();
        productOrder.setProductOrder_id(Integer.valueOf(order_id));
        productOrder.setProductOrder_status((byte) 2);
        SimpleDateFormat time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.UK);
        productOrder.setProductOrder_delivery_date(time.format(new Date()));
        boolean yn = productOrderService.update(productOrder);
        if (yn) {
            jsonObject.put(Constants.SUCCESS, true);
        } else {
            jsonObject.put(Constants.SUCCESS, false);
            throw new RuntimeException();
        }
        jsonObject.put("order_id", order_id);
        return String.valueOf(jsonObject);
    }

    // 按条件查询订单-ajax
    @ApiOperation(value = "按条件查询订单", notes = "按条件查询订单")
    @ResponseBody
    @GetMapping(value = "admin/order/{index}/{count}", produces = "application/json;charset=UTF-8")
    public String getOrderBySearch(HttpSession session,
                                   @RequestParam(required = false) String productOrder_code,
                                   @RequestParam(required = false) String productOrder_post,
                                   @RequestParam(required = false) String productOrder_receiver,
                                   @RequestParam(required = false) String productOrder_mobile,
                                   @RequestParam(required = false) Byte[] productOrder_status_array,
                                   @RequestParam(required = false) String orderBy,
                                   @RequestParam(required = false, defaultValue = "true") Boolean isDesc,
                                   @PathVariable Integer index,
                                   @PathVariable Integer count) throws UnsupportedEncodingException {
        Object adminId = checkAdmin(session);
        if (adminId == null) {
            JSONObject obj = new JSONObject();
            obj.put("success", false);
            obj.put("message", "未登录");
            return String.valueOf(obj);
        }
        if (productOrder_status_array != null && (productOrder_status_array.length <= 0 || productOrder_status_array.length >= 5)) {
            productOrder_status_array = null;
        }
        if (productOrder_code != null) {
            productOrder_code = "".equals(productOrder_code) ? null : productOrder_code;
        }
        if (productOrder_post != null) {
            productOrder_post = "".equals(productOrder_post) ? null : productOrder_post;
        }
        if (productOrder_receiver != null) {
            productOrder_receiver = "".equals(productOrder_receiver) ? null : URLDecoder.decode(productOrder_receiver, "UTF-8");
        }
        if (productOrder_mobile != null) {
            productOrder_mobile = "".equals(productOrder_mobile) ? null : productOrder_mobile;
        }
        if (orderBy == null || "".equals(orderBy)) {
            orderBy = "productorder_pay_date";
        }
        ProductOrder productOrder = new ProductOrder();
        productOrder.setProductOrder_code(productOrder_code);
        productOrder.setProductOrder_post(productOrder_post);
        productOrder.setProductOrder_receiver(productOrder_receiver);
        productOrder.setProductOrder_mobile(productOrder_mobile);
        OrderUtil orderUtil = null;
        if (orderBy != null) {
            orderBy = orderBy.replaceAll("[()]", "");
            orderUtil = new OrderUtil(orderBy, isDesc);
        }
        JSONObject object = new JSONObject();
        PageUtil pageUtil = new PageUtil(index, count);
        List<ProductOrder> productOrderList = productOrderService.getList(productOrder, productOrder_status_array, orderUtil, pageUtil);
        object.put("productOrderList", JSON.parseArray(JSON.toJSONString(productOrderList)));
        Integer productOrderCount = productOrderService.getTotal(productOrder, productOrder_status_array);
        object.put("productOrderCount", productOrderCount);
        pageUtil.setTotal(productOrderCount);
        object.put("totalPage", pageUtil.getTotalPage());
        object.put("pageUtil", pageUtil);
        return String.valueOf(object);
    }

    // 删除订单-ajax
    @ApiOperation(value = "删除订单", notes = "删除订单")
    @ResponseBody
    @GetMapping(value = "admin/order/del/{order_id}", produces = "application/json;charset=utf-8")
    public String deleteOrder(HttpSession session, @PathVariable Integer order_id) {
        Object adminId = checkAdmin(session);
        if (adminId == null) {
            JSONObject obj = new JSONObject();
            obj.put("success", false);
            obj.put("message", "未登录");
            return String.valueOf(obj);
        }
        JSONObject jsonObject = new JSONObject();
        boolean yn = productOrderService.deleteList(new Integer[]{order_id});
        jsonObject.put(Constants.SUCCESS, yn);
        return String.valueOf(jsonObject);
    }
}
