package com.xq.tmall.controller.fore;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.xq.tmall.controller.BaseController;
import com.xq.tmall.entity.*;
import com.xq.tmall.pay.PayFace;
import com.xq.tmall.pay.req.TradeCloseReq;
import com.xq.tmall.pay.req.TradeNotifyReq;
import com.xq.tmall.pay.req.TradeOrderReq;
import com.xq.tmall.pay.resp.TradeOrderResp;
import com.xq.tmall.pay.util.PayModelEnum;
import com.xq.tmall.service.*;
import com.xq.tmall.util.Constants;
import com.xq.tmall.util.OrderUtil;
import com.xq.tmall.util.PageUtil;
import com.xq.tmall.util.PayTypeEnum;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.validation.constraints.NotNull;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.*;

@Api(tags = "前台天猫-订单")
@Controller
public class ForeOrderController extends BaseController {
    @Resource
    private ProductService productService;
    @Resource
    private UserService userService;
    @Resource
    private ProductOrderItemService productOrderItemService;
    @Resource
    private AddressService addressService;
    @Resource
    private CategoryService categoryService;
    @Resource
    private ProductImageService productImageService;
    @Resource
    private ProductOrderService productOrderService;
    @Resource
    private ReviewService reviewService;
    @Resource
    private LastIDService lastIDService;
    public static final String ORDER = "/order/0/10";
    public static final String LOGIN = "/login";
    public static final String CART = "/cart";
    @NotNull
    private final Map<String, PayFace> payFaceMap;

    public ForeOrderController(Map<String, PayFace> payFaceMap) {
        this.payFaceMap = payFaceMap;
    }

    // 订单列表页 (返回JSON)
    @ApiOperation(value = "转到前台天猫-订单列表页", notes = "转到前台天猫-订单列表页")
    @ResponseBody
    @GetMapping(value = "order", produces = "application/json;charset=utf-8")
    public String goToPageSimple() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("success", true);
        jsonObject.put("redirect", ORDER);
        return String.valueOf(jsonObject);
    }

    // 订单列表 (返回JSON)
    @ApiOperation(value = "订单列表", notes = "订单列表")
    @ResponseBody
    @GetMapping(value = "order/{index}/{count}", produces = "application/json;charset=utf-8")
    public String goToPage(HttpSession session,
                           @RequestParam(required = false) Byte status,
                           @PathVariable("index") Integer index,
                           @PathVariable("count") Integer count) {
        JSONObject jsonObject = new JSONObject();
        Object userId = checkUser(session);
        if (userId == null) {
            jsonObject.put("success", false);
            jsonObject.put("url", LOGIN);
            return String.valueOf(jsonObject);
        }
        Byte[] status_array = status != null ? new Byte[]{status} : null;
        PageUtil pageUtil = new PageUtil(index, count);
        User user1 = new User();
        user1.setUser_id(Integer.valueOf(userId.toString()));
        ProductOrder productOrder = new ProductOrder();
        productOrder.setProductOrder_user(user1);
        List<ProductOrder> productOrderList = productOrderService.getList(productOrder, status_array, new OrderUtil("productOrder_id", true), pageUtil);
        Integer orderCount = 0;
        if (CollectionUtil.isNotEmpty(productOrderList)) {
            orderCount = productOrderService.getTotal(productOrder, status_array);
            for (ProductOrder order : productOrderList) {
                List<ProductOrderItem> productOrderItemList = productOrderItemService.getListByOrderId(order.getProductOrder_id(), null);
                if (CollectionUtil.isNotEmpty(productOrderItemList)) {
                    for (ProductOrderItem productOrderItem : productOrderItemList) {
                        Integer product_id = productOrderItem.getProductOrderItem_product().getProduct_id();
                        Product product = productService.get(product_id);
                        product.setSingleProductImageList(productImageService.getList(product_id, (byte) 0, new PageUtil(0, 1)));
                        productOrderItem.setProductOrderItem_product(product);
                        if (order.getProductOrder_status() == 3) {
                            productOrderItem.setIsReview(reviewService.getTotalByOrderItemId(productOrderItem.getProductOrderItem_id()) > 0);
                        }
                    }
                }
                order.setProductOrderItemList(productOrderItemList);
            }
        }
        pageUtil.setTotal(orderCount);
        jsonObject.put("productOrderList", JSON.parseArray(JSON.toJSONString(productOrderList)));
        jsonObject.put("pageUtil", JSON.parseObject(JSON.toJSONString(pageUtil)));
        jsonObject.put("status", status);
        jsonObject.put("success", true);
        return String.valueOf(jsonObject);
    }

    // 订单建立页 (返回JSON)
    @ApiOperation(value = "订单建立页", notes = "订单建立页")
    @ResponseBody
    @GetMapping(value = "order/create/{product_id}", produces = "application/json;charset=utf-8")
    public String goToOrderConfirmPage(@PathVariable("product_id") Integer product_id,
                                       @RequestParam(required = false, defaultValue = "1") Short product_number,
                                       HttpSession session,
                                       HttpServletRequest request) throws UnsupportedEncodingException {
        JSONObject jsonObject = new JSONObject();
        Object userId = checkUser(session);
        if (userId == null) {
            jsonObject.put("success", false);
            jsonObject.put("url", LOGIN);
            return String.valueOf(jsonObject);
        }
        Product product = productService.get(product_id);
        if (product == null) {
            jsonObject.put("success", false);
            jsonObject.put("url", "/");
            return String.valueOf(jsonObject);
        }
        product.setProduct_category(categoryService.get(product.getProduct_category().getCategory_id()));
        product.setSingleProductImageList(productImageService.getList(product_id, (byte) 0, new PageUtil(0, 1)));
        ProductOrderItem productOrderItem = new ProductOrderItem();
        productOrderItem.setProductOrderItem_product(product);
        productOrderItem.setProductOrderItem_number(product_number);
        productOrderItem.setProductOrderItem_price(product.getProduct_sale_price() * product_number);
        User user1 = new User();
        user1.setUser_id(Integer.valueOf(userId.toString()));
        productOrderItem.setProductOrderItem_user(user1);

        String addressId = "110000", cityAddressId = "110100", districtAddressId = "110101";
        String detailsAddress = null, order_post = null, order_receiver = null, order_phone = null;
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                switch (cookie.getName()) {
                    case "addressId": addressId = cookie.getValue(); break;
                    case "cityAddressId": cityAddressId = cookie.getValue(); break;
                    case "districtAddressId": districtAddressId = cookie.getValue(); break;
                    case "order_post": order_post = URLDecoder.decode(cookie.getValue(), Constants.UTF); break;
                    case "order_receiver": order_receiver = URLDecoder.decode(cookie.getValue(), Constants.UTF); break;
                    case "order_phone": order_phone = URLDecoder.decode(cookie.getValue(), Constants.UTF); break;
                    case "detailsAddress": detailsAddress = URLDecoder.decode(cookie.getValue(), Constants.UTF); break;
                }
            }
        }
        List<ProductOrderItem> productOrderItemList = new ArrayList<>();
        productOrderItemList.add(productOrderItem);
        jsonObject.put("orderItemList", JSON.parseArray(JSON.toJSONString(productOrderItemList)));
        jsonObject.put("addressList", JSON.parseArray(JSON.toJSONString(addressService.getRoot())));
        jsonObject.put("cityList", JSON.parseArray(JSON.toJSONString(addressService.getList(null, addressId))));
        jsonObject.put("districtList", JSON.parseArray(JSON.toJSONString(addressService.getList(null, cityAddressId))));
        jsonObject.put("orderTotalPrice", productOrderItem.getProductOrderItem_price());
        jsonObject.put("addressId", addressId);
        jsonObject.put("cityAddressId", cityAddressId);
        jsonObject.put("districtAddressId", districtAddressId);
        jsonObject.put("order_post", order_post);
        jsonObject.put("order_receiver", order_receiver);
        jsonObject.put("order_phone", order_phone);
        jsonObject.put("detailsAddress", detailsAddress);
        jsonObject.put("success", true);
        return String.valueOf(jsonObject);
    }

    // 购物车订单建立页 (返回JSON)
    @ApiOperation(value = "购物车订单建立页", notes = "购物车订单建立页")
    @ResponseBody
    @GetMapping(value = "order/create/byCart", produces = "application/json;charset=utf-8")
    public String goToOrderConfirmPageByCart(HttpSession session, HttpServletRequest request,
                                             @RequestParam(required = false) Integer[] order_item_list) throws UnsupportedEncodingException {
        JSONObject jsonObject = new JSONObject();
        Object userId = checkUser(session);
        if (userId == null) {
            jsonObject.put("success", false);
            jsonObject.put("url", LOGIN);
            return String.valueOf(jsonObject);
        }
        if (order_item_list == null || order_item_list.length == 0) {
            jsonObject.put("success", false);
            jsonObject.put("url", CART);
            return String.valueOf(jsonObject);
        }
        List<ProductOrderItem> orderItemList = new ArrayList<>(order_item_list.length);
        for (Integer orderItem_id : order_item_list) {
            orderItemList.add(productOrderItemService.get(orderItem_id));
        }
        if (orderItemList.size() == 0) {
            jsonObject.put("success", false);
            jsonObject.put("url", CART);
            return String.valueOf(jsonObject);
        }
        for (ProductOrderItem orderItem : orderItemList) {
            if (orderItem.getProductOrderItem_user().getUser_id() != userId) {
                jsonObject.put("success", false);
                jsonObject.put("url", CART);
                return String.valueOf(jsonObject);
            }
            if (orderItem.getProductOrderItem_order() != null) {
                jsonObject.put("success", false);
                jsonObject.put("url", CART);
                return String.valueOf(jsonObject);
            }
        }
        double orderTotalPrice = 0.0;
        for (ProductOrderItem orderItem : orderItemList) {
            Product product = productService.get(orderItem.getProductOrderItem_product().getProduct_id());
            product.setProduct_category(categoryService.get(product.getProduct_category().getCategory_id()));
            product.setSingleProductImageList(productImageService.getList(product.getProduct_id(), (byte) 0, new PageUtil(0, 1)));
            orderItem.setProductOrderItem_product(product);
            orderTotalPrice += orderItem.getProductOrderItem_price();
        }
        String addressId = "110000", cityAddressId = "110100", districtAddressId = "110101";
        String detailsAddress = null, order_post = null, order_receiver = null, order_phone = null;
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                switch (cookie.getName()) {
                    case "addressId": addressId = cookie.getValue(); break;
                    case "cityAddressId": cityAddressId = cookie.getValue(); break;
                    case "districtAddressId": districtAddressId = cookie.getValue(); break;
                    case "order_post": order_post = URLDecoder.decode(cookie.getValue(), Constants.UTF); break;
                    case "order_receiver": order_receiver = URLDecoder.decode(cookie.getValue(), Constants.UTF); break;
                    case "order_phone": order_phone = URLDecoder.decode(cookie.getValue(), Constants.UTF); break;
                    case "detailsAddress": detailsAddress = URLDecoder.decode(cookie.getValue(), Constants.UTF); break;
                }
            }
        }
        jsonObject.put("orderItemList", JSON.parseArray(JSON.toJSONString(orderItemList)));
        jsonObject.put("addressList", JSON.parseArray(JSON.toJSONString(addressService.getRoot())));
        jsonObject.put("cityList", JSON.parseArray(JSON.toJSONString(addressService.getList(null, addressId))));
        jsonObject.put("districtList", JSON.parseArray(JSON.toJSONString(addressService.getList(null, cityAddressId))));
        jsonObject.put("orderTotalPrice", orderTotalPrice);
        jsonObject.put("addressId", addressId);
        jsonObject.put("cityAddressId", cityAddressId);
        jsonObject.put("districtAddressId", districtAddressId);
        jsonObject.put("order_post", order_post);
        jsonObject.put("order_receiver", order_receiver);
        jsonObject.put("order_phone", order_phone);
        jsonObject.put("detailsAddress", detailsAddress);
        jsonObject.put("success", true);
        return String.valueOf(jsonObject);
    }

    // 订单支付页 (返回JSON)
    @ApiOperation(value = "订单支付页", notes = "订单支付页")
    @ResponseBody
    @GetMapping(value = "order/pay/{order_code}", produces = "application/json;charset=utf-8")
    public String goToOrderPayPage(HttpSession session, @PathVariable("order_code") String order_code) {
        JSONObject jsonObject = new JSONObject();
        Object userId = checkUser(session);
        if (userId == null) {
            jsonObject.put("success", false);
            jsonObject.put("url", LOGIN);
            return String.valueOf(jsonObject);
        }
        ProductOrder order = productOrderService.getByCode(order_code);
        if (order == null || order.getProductOrder_status() != 0) {
            jsonObject.put("success", false);
            jsonObject.put("url", ORDER);
            return String.valueOf(jsonObject);
        }
        if (order.getProductOrder_user().getUser_id() != Integer.parseInt(userId.toString())) {
            jsonObject.put("success", false);
            jsonObject.put("url", ORDER);
            return String.valueOf(jsonObject);
        }
        order.setProductOrderItemList(productOrderItemService.getListByOrderId(order.getProductOrder_id(), null));
        double orderTotalPrice = 0.00;
        if (order.getProductOrderItemList().size() == 1) {
            ProductOrderItem productOrderItem = order.getProductOrderItemList().get(0);
            Product product = productService.get(productOrderItem.getProductOrderItem_product().getProduct_id());
            product.setProduct_category(categoryService.get(product.getProduct_category().getCategory_id()));
            productOrderItem.setProductOrderItem_product(product);
            orderTotalPrice = productOrderItem.getProductOrderItem_price();
        } else {
            for (ProductOrderItem productOrderItem : order.getProductOrderItemList()) {
                orderTotalPrice += productOrderItem.getProductOrderItem_price();
            }
        }
        jsonObject.put("productOrder", JSON.parseObject(JSON.toJSONString(order)));
        jsonObject.put("orderTotalPrice", orderTotalPrice);
        jsonObject.put("success", true);
        return String.valueOf(jsonObject);
    }

    // 订单支付成功页 (返回JSON)
    @ApiOperation(value = "订单支付成功页", notes = "订单支付成功页")
    @ResponseBody
    @GetMapping(value = "order/pay/success/{order_code}", produces = "application/json;charset=utf-8")
    public String goToOrderPaySuccessPage(HttpSession session, @PathVariable("order_code") String order_code) {
        JSONObject jsonObject = new JSONObject();
        Object userId = checkUser(session);
        if (userId == null) {
            jsonObject.put("success", false);
            jsonObject.put("url", LOGIN);
            return String.valueOf(jsonObject);
        }
        ProductOrder order = productOrderService.getByCode(order_code);
        if (order == null || order.getProductOrder_status() != 1) {
            jsonObject.put("success", false);
            jsonObject.put("url", ORDER);
            return String.valueOf(jsonObject);
        }
        if (order.getProductOrder_user().getUser_id() != Integer.parseInt(userId.toString())) {
            jsonObject.put("success", false);
            jsonObject.put("url", ORDER);
            return String.valueOf(jsonObject);
        }
        order.setProductOrderItemList(productOrderItemService.getListByOrderId(order.getProductOrder_id(), null));
        double orderTotalPrice = 0.00;
        if (order.getProductOrderItemList().size() == 1) {
            orderTotalPrice = order.getProductOrderItemList().get(0).getProductOrderItem_price();
        } else {
            for (ProductOrderItem productOrderItem : order.getProductOrderItemList()) {
                orderTotalPrice += productOrderItem.getProductOrderItem_price();
            }
        }
        Address address = addressService.get(order.getProductOrder_address().getAddress_areaId());
        Stack<String> addressStack = new Stack<>();
        addressStack.push(order.getProductOrder_detail_address());
        addressStack.push(address.getAddress_name() + " ");
        while (!address.getAddress_areaId().equals(address.getAddress_regionId().getAddress_areaId())) {
            address = addressService.get(address.getAddress_regionId().getAddress_areaId());
            addressStack.push(address.getAddress_name() + " ");
        }
        StringBuilder builder = new StringBuilder();
        while (!addressStack.empty()) { builder.append(addressStack.pop()); }
        order.setProductOrder_detail_address(builder.toString());
        jsonObject.put("productOrder", JSON.parseObject(JSON.toJSONString(order)));
        jsonObject.put("orderTotalPrice", orderTotalPrice);
        jsonObject.put("success", true);
        return String.valueOf(jsonObject);
    }

    // 订单确认页 (返回JSON)
    @ApiOperation(value = "订单确认页", notes = "订单确认页")
    @ResponseBody
    @GetMapping(value = "order/confirm/{order_code}", produces = "application/json;charset=utf-8")
    public String goToOrderConfirmPage(HttpSession session, @PathVariable("order_code") String order_code) {
        JSONObject jsonObject = new JSONObject();
        Object userId = checkUser(session);
        if (userId == null) {
            jsonObject.put("success", false);
            jsonObject.put("url", LOGIN);
            return String.valueOf(jsonObject);
        }
        ProductOrder order = productOrderService.getByCode(order_code);
        if (order == null || order.getProductOrder_status() != 2) {
            jsonObject.put("success", false);
            jsonObject.put("url", ORDER);
            return String.valueOf(jsonObject);
        }
        if (order.getProductOrder_user().getUser_id() != Integer.parseInt(userId.toString())) {
            jsonObject.put("success", false);
            jsonObject.put("url", ORDER);
            return String.valueOf(jsonObject);
        }
        order.setProductOrderItemList(productOrderItemService.getListByOrderId(order.getProductOrder_id(), null));
        double orderTotalPrice = 0.00;
        for (ProductOrderItem productOrderItem : order.getProductOrderItemList()) {
            Integer product_id = productOrderItem.getProductOrderItem_product().getProduct_id();
            Product product = productService.get(product_id);
            product.setSingleProductImageList(productImageService.getList(product_id, (byte) 0, new PageUtil(0, 1)));
            productOrderItem.setProductOrderItem_product(product);
            orderTotalPrice += productOrderItem.getProductOrderItem_price();
        }
        jsonObject.put("productOrder", JSON.parseObject(JSON.toJSONString(order)));
        jsonObject.put("orderTotalPrice", orderTotalPrice);
        jsonObject.put("success", true);
        return String.valueOf(jsonObject);
    }

    // 订单完成页 (返回JSON)
    @ApiOperation(value = "订单完成页", notes = "订单完成页")
    @ResponseBody
    @GetMapping(value = "order/success/{order_code}", produces = "application/json;charset=utf-8")
    public String goToOrderSuccessPage(HttpSession session, @PathVariable("order_code") String order_code) {
        JSONObject jsonObject = new JSONObject();
        Object userId = checkUser(session);
        if (userId == null) {
            jsonObject.put("success", false);
            jsonObject.put("url", LOGIN);
            return String.valueOf(jsonObject);
        }
        ProductOrder order = productOrderService.getByCode(order_code);
        if (order == null || order.getProductOrder_status() != 3) {
            jsonObject.put("success", false);
            jsonObject.put("url", ORDER);
            return String.valueOf(jsonObject);
        }
        if (order.getProductOrder_user().getUser_id() != Integer.parseInt(userId.toString())) {
            jsonObject.put("success", false);
            jsonObject.put("url", ORDER);
            return String.valueOf(jsonObject);
        }
        Integer count = productOrderItemService.getTotalByOrderId(order.getProductOrder_id());
        Product product = null;
        ProductOrderItem singleOrderItem = null;
        if (count == 1) {
            singleOrderItem = productOrderItemService.getListByOrderId(order.getProductOrder_id(), new PageUtil(0, 1)).get(0);
            if (singleOrderItem != null) {
                count = reviewService.getTotalByOrderItemId(singleOrderItem.getProductOrderItem_id());
                if (count == 0) {
                    product = productService.get(singleOrderItem.getProductOrderItem_product().getProduct_id());
                    if (product != null) {
                        product.setSingleProductImageList(productImageService.getList(product.getProduct_id(), (byte) 0, new PageUtil(0, 1)));
                    }
                }
            }
        }
        jsonObject.put("orderItem", singleOrderItem != null ? JSON.parseObject(JSON.toJSONString(singleOrderItem)) : null);
        jsonObject.put("product", product != null ? JSON.parseObject(JSON.toJSONString(product)) : null);
        jsonObject.put("success", true);
        return String.valueOf(jsonObject);
    }

    // 购物车页 (返回JSON)
    @ApiOperation(value = "购物车页", notes = "购物车页")
    @ResponseBody
    @GetMapping(value = "cart", produces = "application/json;charset=utf-8")
    public String goToCartPage(HttpSession session) {
        JSONObject jsonObject = new JSONObject();
        Object userId = checkUser(session);
        if (userId == null) {
            jsonObject.put("success", false);
            jsonObject.put("url", LOGIN);
            return String.valueOf(jsonObject);
        }
        List<ProductOrderItem> orderItemList = productOrderItemService.getListByUserId(Integer.valueOf(userId.toString()), null);
        Integer orderItemTotal = 0;
        if (CollectionUtil.isNotEmpty(orderItemList)) {
            orderItemTotal = productOrderItemService.getTotalByUserId(Integer.valueOf(userId.toString()));
            for (ProductOrderItem orderItem : orderItemList) {
                Integer product_id = orderItem.getProductOrderItem_product().getProduct_id();
                Product product = productService.get(product_id);
                product.setSingleProductImageList(productImageService.getList(product_id, (byte) 0, null));
                product.setProduct_category(categoryService.get(product.getProduct_category().getCategory_id()));
                orderItem.setProductOrderItem_product(product);
            }
        }
        jsonObject.put("orderItemList", JSON.parseArray(JSON.toJSONString(orderItemList)));
        jsonObject.put("orderItemTotal", orderItemTotal);
        jsonObject.put("success", true);
        return String.valueOf(jsonObject);
    }

    // 更新订单信息为已支付，待发货-ajax
    @ApiOperation(value = "更新订单信息为已支付，待发货", notes = "更新订单信息为已支付，待发货")
    @ResponseBody
    @PutMapping(value = "order/pay/{order_code}")
    public String orderPay(HttpSession session, @PathVariable("order_code") String order_code) {
        JSONObject object = new JSONObject();
        Object userId = checkUser(session);
        if (userId == null) { object.put(Constants.SUCCESS, false); object.put("url", LOGIN); return String.valueOf(object); }
        ProductOrder order = productOrderService.getByCode(order_code);
        if (order == null || order.getProductOrder_status() != 0) { object.put(Constants.SUCCESS, false); object.put("url", ORDER); return String.valueOf(object); }
        if (order.getProductOrder_user().getUser_id() != Integer.parseInt(userId.toString())) { object.put(Constants.SUCCESS, false); object.put("url", ORDER); return String.valueOf(object); }
        order.setProductOrderItemList(productOrderItemService.getListByOrderId(order.getProductOrder_id(), null));
        if (order.getProductOrderItemList().size() == 1) {
            ProductOrderItem productOrderItem = order.getProductOrderItemList().get(0);
            Product product = productService.get(productOrderItem.getProductOrderItem_product().getProduct_id());
            product.setProduct_category(categoryService.get(product.getProduct_category().getCategory_id()));
            productOrderItem.setProductOrderItem_product(product);
        }
        ProductOrder productOrder = new ProductOrder();
        productOrder.setProductOrder_id(order.getProductOrder_id());
        SimpleDateFormat time = new SimpleDateFormat(Constants.DATE, Locale.UK);
        productOrder.setProductOrder_pay_date(time.format(new Date()));
        productOrder.setProductOrder_status((byte) 1);
        boolean yn = productOrderService.update(productOrder);
        if (yn) {
            object.put(Constants.SUCCESS, true);
            object.put("url", "/order/pay/success/" + order_code);
        } else {
            object.put(Constants.SUCCESS, false);
            object.put("url", ORDER);
        }
        return String.valueOf(object);
    }

    // 更新订单信息为已发货，待确认-ajax
    @ApiOperation(value = "更新订单信息为已发货，待确认", notes = "更新订单信息为已发货，待确认")
    @ResponseBody
    @GetMapping(value = "order/delivery/{order_code}", produces = "application/json;charset=utf-8")
    public String orderDelivery(HttpSession session, @PathVariable("order_code") String order_code) {
        JSONObject object = new JSONObject();
        Object userId = checkUser(session);
        if (userId == null) { object.put(Constants.SUCCESS, false); return String.valueOf(object); }
        ProductOrder order = productOrderService.getByCode(order_code);
        if (order == null || order.getProductOrder_status() != 1) { object.put(Constants.SUCCESS, false); return String.valueOf(object); }
        if (order.getProductOrder_user().getUser_id() != Integer.parseInt(userId.toString())) { object.put(Constants.SUCCESS, false); return String.valueOf(object); }
        ProductOrder productOrder = new ProductOrder();
        productOrder.setProductOrder_id(order.getProductOrder_id());
        SimpleDateFormat time = new SimpleDateFormat(Constants.DATE, Locale.UK);
        productOrder.setProductOrder_delivery_date(time.format(new Date()));
        productOrder.setProductOrder_status((byte) 2);
        productOrderService.update(productOrder);
        object.put(Constants.SUCCESS, true);
        return String.valueOf(object);
    }

    // 更新订单信息为交易成功-ajax
    @ApiOperation(value = "更新订单信息为交易成功", notes = "更新订单信息为交易成功")
    @ResponseBody
    @PutMapping(value = "order/success/{order_code}", produces = "application/json;charset=utf-8")
    public String orderSuccess(HttpSession session, @PathVariable("order_code") String order_code) {
        JSONObject object = new JSONObject();
        Object userId = checkUser(session);
        if (userId == null) { object.put(Constants.SUCCESS, false); object.put("url", LOGIN); return String.valueOf(object); }
        ProductOrder order = productOrderService.getByCode(order_code);
        if (order == null || order.getProductOrder_status() != 2) { object.put(Constants.SUCCESS, false); object.put("url", ORDER); return String.valueOf(object); }
        if (order.getProductOrder_user().getUser_id() != Integer.parseInt(userId.toString())) { object.put(Constants.SUCCESS, false); object.put("url", ORDER); return String.valueOf(object); }
        ProductOrder productOrder = new ProductOrder();
        productOrder.setProductOrder_id(order.getProductOrder_id());
        productOrder.setProductOrder_status((byte) 3);
        SimpleDateFormat time = new SimpleDateFormat(Constants.DATE, Locale.UK);
        productOrder.setProductOrder_confirm_date(time.format(new Date()));
        boolean yn = productOrderService.update(productOrder);
        object.put(Constants.SUCCESS, yn);
        return String.valueOf(object);
    }

    // 更新订单信息为交易关闭-ajax
    @ApiOperation(value = "更新订单信息为交易关闭", notes = "更新订单信息为交易关闭")
    @ResponseBody
    @PutMapping(value = "order/close/{order_code}", produces = "application/json;charset=utf-8")
    public String orderClose(HttpSession session, @PathVariable("order_code") String order_code) {
        JSONObject object = new JSONObject();
        Object userId = checkUser(session);
        if (userId == null) { object.put(Constants.SUCCESS, false); object.put("url", LOGIN); return String.valueOf(object); }
        ProductOrder order = productOrderService.getByCode(order_code);
        if (order == null || order.getProductOrder_status() != 0) { object.put(Constants.SUCCESS, false); object.put("url", ORDER); return String.valueOf(object); }
        if (order.getProductOrder_user().getUser_id() != Integer.parseInt(userId.toString())) { object.put(Constants.SUCCESS, false); object.put("url", ORDER); return String.valueOf(object); }
        ProductOrder productOrder = new ProductOrder();
        productOrder.setProductOrder_id(order.getProductOrder_id());
        productOrder.setProductOrder_status((byte) 4);
        boolean yn = productOrderService.update(productOrder);
        object.put(Constants.SUCCESS, yn);
        return String.valueOf(object);
    }

    // 更新购物车订单项数量-ajax
    @ApiOperation(value = "更新购物车订单项数量", notes = "更新购物车订单项数量")
    @ResponseBody
    @PutMapping(value = "orderItem", produces = "application/json;charset=utf-8")
    public String updateOrderItem(HttpSession session, @RequestParam String orderItemMap) {
        JSONObject object = new JSONObject();
        Object userId = checkUser(session);
        if (userId == null) { object.put(Constants.SUCCESS, false); return String.valueOf(object); }
        JSONObject orderItemString = JSON.parseObject(orderItemMap);
        Set<String> orderItemIDSet = orderItemString.keySet();
        if (CollectionUtil.isNotEmpty(orderItemIDSet)) {
            for (String key : orderItemIDSet) {
                ProductOrderItem productOrderItem = productOrderItemService.get(Integer.valueOf(key));
                if (productOrderItem == null || !productOrderItem.getProductOrderItem_user().getUser_id().equals(userId)) {
                    object.put(Constants.SUCCESS, false);
                    return String.valueOf(object);
                }
                if (productOrderItem.getProductOrderItem_order() != null) {
                    object.put(Constants.SUCCESS, false);
                    return String.valueOf(object);
                }
                Short number = Short.valueOf(orderItemString.getString(key));
                if (number <= 0 || number > 500) {
                    object.put(Constants.SUCCESS, false);
                    return String.valueOf(object);
                }
                double price = productOrderItem.getProductOrderItem_price() / productOrderItem.getProductOrderItem_number();
                ProductOrderItem productOrderItem1 = new ProductOrderItem();
                productOrderItem1.setProductOrderItem_id(Integer.valueOf(key));
                productOrderItem1.setProductOrderItem_number(number);
                productOrderItem1.setProductOrderItem_price(number * price);
                Boolean yn = productOrderItemService.update(productOrderItem1);
                if (!yn) { throw new RuntimeException(); }
            }
            Object[] orderItemIDArray = orderItemIDSet.toArray();
            object.put(Constants.SUCCESS, true);
            object.put("orderItemIDArray", orderItemIDArray);
            return String.valueOf(object);
        } else {
            object.put(Constants.SUCCESS, false);
            return String.valueOf(object);
        }
    }

    // 创建新订单-单订单项-ajax
    @ApiOperation(value = "创建新订单-单订单项", notes = "创建新订单-单订单项")
    @ResponseBody
    @PostMapping(value = "order", produces = "application/json;charset=utf-8")
    public String createOrderByOne(HttpSession session, HttpServletResponse response,
                                   @RequestParam String addressId,
                                   @RequestParam String cityAddressId,
                                   @RequestParam String districtAddressId,
                                   @RequestParam String productOrder_detail_address,
                                   @RequestParam String productOrder_post,
                                   @RequestParam String productOrder_receiver,
                                   @RequestParam String productOrder_mobile,
                                   @RequestParam String userMessage,
                                   @RequestParam Integer orderItem_product_id,
                                   @RequestParam Short orderItem_number) throws UnsupportedEncodingException {
        JSONObject object = new JSONObject();
        Object userId = checkUser(session);
        if (userId == null) { object.put(Constants.SUCCESS, false); object.put("url", LOGIN); return String.valueOf(object); }
        Product product = productService.get(orderItem_product_id);
        if (product == null) { object.put(Constants.SUCCESS, false); object.put("url", "/"); return String.valueOf(object); }
        Cookie cookie1 = new Cookie("addressId", addressId);
        Cookie cookie2 = new Cookie("cityAddressId", cityAddressId);
        Cookie cookie3 = new Cookie("districtAddressId", districtAddressId);
        Cookie cookie4 = new Cookie("order_post", URLEncoder.encode(productOrder_post, Constants.UTF));
        Cookie cookie5 = new Cookie("order_receiver", URLEncoder.encode(productOrder_receiver, Constants.UTF));
        Cookie cookie6 = new Cookie("order_phone", URLEncoder.encode(productOrder_mobile, Constants.UTF));
        Cookie cookie7 = new Cookie("detailsAddress", URLEncoder.encode(productOrder_detail_address, Constants.UTF));
        int maxAge = 60 * 60 * 24 * 365;
        cookie1.setMaxAge(maxAge); cookie2.setMaxAge(maxAge); cookie3.setMaxAge(maxAge);
        cookie4.setMaxAge(maxAge); cookie5.setMaxAge(maxAge); cookie6.setMaxAge(maxAge); cookie7.setMaxAge(maxAge);
        response.addCookie(cookie1); response.addCookie(cookie2); response.addCookie(cookie3);
        response.addCookie(cookie4); response.addCookie(cookie5); response.addCookie(cookie6); response.addCookie(cookie7);
        StringBuilder productOrder_code = new StringBuilder()
                .append(new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()))
                .append(0).append(userId);
        ProductOrder productOrder = new ProductOrder();
        productOrder.setProductOrder_status((byte) 0);
        Address address = new Address();
        address.setAddress_areaId(districtAddressId);
        productOrder.setProductOrder_address(address);
        productOrder.setProductOrder_post(productOrder_post);
        User user = new User();
        user.setUser_id(Integer.valueOf(userId.toString()));
        productOrder.setProductOrder_user(user);
        productOrder.setProductOrder_mobile(productOrder_mobile);
        productOrder.setProductOrder_receiver(productOrder_receiver);
        productOrder.setProductOrder_detail_address(productOrder_detail_address);
        SimpleDateFormat time = new SimpleDateFormat(Constants.DATE, Locale.UK);
        productOrder.setProductOrder_pay_date(time.format(new Date()));
        productOrder.setProductOrder_code(productOrder_code.toString());
        Boolean yn = productOrderService.add(productOrder);
        if (!yn) { throw new RuntimeException(); }
        Integer order_id = lastIDService.selectLastID();
        ProductOrderItem productOrderItem = new ProductOrderItem();
        productOrderItem.setProductOrderItem_user(user);
        productOrderItem.setProductOrderItem_product(productService.get(orderItem_product_id));
        productOrderItem.setProductOrderItem_number(orderItem_number);
        productOrderItem.setProductOrderItem_price(product.getProduct_sale_price() * orderItem_number);
        productOrderItem.setProductOrderItem_userMessage(userMessage);
        ProductOrder productOrder1 = new ProductOrder();
        productOrder1.setProductOrder_id(order_id);
        productOrderItem.setProductOrderItem_order(productOrder1);
        yn = productOrderItemService.add(productOrderItem);
        if (!yn) { throw new RuntimeException(); }
        object.put(Constants.SUCCESS, true);
        object.put("url", "/order/pay/" + productOrder.getProductOrder_code());
        return String.valueOf(object);
    }

    // 创建新订单-多订单项-ajax
    @ApiOperation(value = "创建新订单-多订单项", notes = "创建新订单-多订单项")
    @ResponseBody
    @PostMapping(value = "order/list", produces = "application/json;charset=utf-8")
    public String createOrderByList(HttpSession session, HttpServletResponse response,
                                    @RequestParam String addressId,
                                    @RequestParam String cityAddressId,
                                    @RequestParam String districtAddressId,
                                    @RequestParam String productOrder_detail_address,
                                    @RequestParam String productOrder_post,
                                    @RequestParam String productOrder_receiver,
                                    @RequestParam String productOrder_mobile,
                                    @RequestParam String orderItemJSON) throws UnsupportedEncodingException {
        JSONObject object = new JSONObject();
        Object userId = checkUser(session);
        if (userId == null) { object.put(Constants.SUCCESS, false); object.put("url", LOGIN); return String.valueOf(object); }
        JSONObject orderItemMap = JSONObject.parseObject(orderItemJSON);
        Set<String> orderItem_id = orderItemMap.keySet();
        List<ProductOrderItem> productOrderItemList = new ArrayList<>(3);
        if (CollectionUtil.isNotEmpty(orderItem_id)) {
            for (String id : orderItem_id) {
                ProductOrderItem orderItem = productOrderItemService.get(Integer.valueOf(id));
                if (orderItem == null || !orderItem.getProductOrderItem_user().getUser_id().equals(userId)) {
                    object.put(Constants.SUCCESS, false); object.put("url", CART); return String.valueOf(object);
                }
                if (orderItem.getProductOrderItem_order() != null) {
                    object.put(Constants.SUCCESS, false); object.put("url", CART); return String.valueOf(object);
                }
                ProductOrderItem productOrderItem = new ProductOrderItem();
                productOrderItem.setProductOrderItem_id(Integer.valueOf(id));
                productOrderItem.setProductOrderItem_userMessage(orderItemMap.getString(id));
                boolean yn = productOrderItemService.update(productOrderItem);
                if (!yn) { throw new RuntimeException(); }
                orderItem.setProductOrderItem_product(productService.get(orderItem.getProductOrderItem_product().getProduct_id()));
                productOrderItemList.add(orderItem);
            }
        } else {
            object.put(Constants.SUCCESS, false); object.put("url", CART); return String.valueOf(object);
        }
        Cookie cookie1 = new Cookie("addressId", addressId);
        Cookie cookie2 = new Cookie("cityAddressId", cityAddressId);
        Cookie cookie3 = new Cookie("districtAddressId", districtAddressId);
        Cookie cookie4 = new Cookie("order_post", URLEncoder.encode(productOrder_post, Constants.UTF));
        Cookie cookie5 = new Cookie("order_receiver", URLEncoder.encode(productOrder_receiver, Constants.UTF));
        Cookie cookie6 = new Cookie("order_phone", URLEncoder.encode(productOrder_mobile, Constants.UTF));
        Cookie cookie7 = new Cookie("detailsAddress", URLEncoder.encode(productOrder_detail_address, Constants.UTF));
        int maxAge = 60 * 60 * 24 * 365;
        cookie1.setMaxAge(maxAge); cookie2.setMaxAge(maxAge); cookie3.setMaxAge(maxAge);
        cookie4.setMaxAge(maxAge); cookie5.setMaxAge(maxAge); cookie6.setMaxAge(maxAge); cookie7.setMaxAge(maxAge);
        response.addCookie(cookie1); response.addCookie(cookie2); response.addCookie(cookie3);
        response.addCookie(cookie4); response.addCookie(cookie5); response.addCookie(cookie6); response.addCookie(cookie7);
        StringBuilder productOrder_code = new StringBuilder()
                .append(new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()))
                .append(0).append(userId);
        ProductOrder productOrder = new ProductOrder();
        productOrder.setProductOrder_status((byte) 0);
        Address address = new Address();
        address.setAddress_areaId(districtAddressId);
        productOrder.setProductOrder_address(address);
        productOrder.setProductOrder_post(productOrder_post);
        User user = new User();
        user.setUser_id(Integer.valueOf(userId.toString()));
        productOrder.setProductOrder_user(user);
        productOrder.setProductOrder_mobile(productOrder_mobile);
        productOrder.setProductOrder_receiver(productOrder_receiver);
        productOrder.setProductOrder_detail_address(productOrder_detail_address);
        SimpleDateFormat time = new SimpleDateFormat(Constants.DATE, Locale.UK);
        productOrder.setProductOrder_pay_date(time.format(new Date()));
        productOrder.setProductOrder_code(productOrder_code.toString());
        Boolean yn = productOrderService.add(productOrder);
        if (!yn) { throw new RuntimeException(); }
        Integer order_id = lastIDService.selectLastID();
        for (ProductOrderItem orderItem : productOrderItemList) {
            ProductOrder productOrder1 = new ProductOrder();
            productOrder1.setProductOrder_id(order_id);
            orderItem.setProductOrderItem_order(productOrder1);
            yn = productOrderItemService.update(orderItem);
        }
        if (!yn) { throw new RuntimeException(); }
        object.put(Constants.SUCCESS, true);
        object.put("url", "/order/pay/" + productOrder.getProductOrder_code());
        return String.valueOf(object);
    }

    // 创建订单项-购物车-ajax
    @ApiOperation(value = "创建订单项-购物车", notes = "创建订单项-购物车")
    @ResponseBody
    @PostMapping(value = "orderItem/create/{product_id}", produces = "application/json;charset=utf-8")
    public String createOrderItem(@PathVariable("product_id") Integer product_id,
                                  @RequestParam(required = false, defaultValue = "1") Short product_number,
                                  HttpSession session) {
        JSONObject object = new JSONObject();
        Object userId = checkUser(session);
        if (userId == null) { object.put("url", LOGIN); object.put(Constants.SUCCESS, false); return String.valueOf(object); }
        Product product = productService.get(product_id);
        if (product == null) { object.put("url", LOGIN); object.put(Constants.SUCCESS, false); return String.valueOf(object); }
        ProductOrderItem productOrderItem = new ProductOrderItem();
        List<ProductOrderItem> orderItemList = productOrderItemService.getListByUserId(Integer.valueOf(userId.toString()), null);
        if (CollectionUtil.isNotEmpty(orderItemList)) {
            for (ProductOrderItem orderItem : orderItemList) {
                if (orderItem.getProductOrderItem_product().getProduct_id().equals(product_id)) {
                    int number = orderItem.getProductOrderItem_number() + 1;
                    productOrderItem.setProductOrderItem_id(orderItem.getProductOrderItem_id());
                    productOrderItem.setProductOrderItem_number((short) number);
                    productOrderItem.setProductOrderItem_price(number * product.getProduct_sale_price());
                    boolean yn = productOrderItemService.update(productOrderItem);
                    object.put(Constants.SUCCESS, yn);
                    return String.valueOf(object);
                }
            }
        }
        productOrderItem.setProductOrderItem_product(product);
        productOrderItem.setProductOrderItem_number(product_number);
        productOrderItem.setProductOrderItem_price(product.getProduct_sale_price() * product_number);
        User user = new User();
        user.setUser_id(Integer.valueOf(userId.toString()));
        productOrderItem.setProductOrderItem_user(user);
        boolean yn = productOrderItemService.add(productOrderItem);
        object.put(Constants.SUCCESS, yn);
        return String.valueOf(object);
    }

    // 删除订单项-购物车-ajax
    @ApiOperation(value = "删除订单项-购物车", notes = "删除订单项-购物车")
    @ResponseBody
    @DeleteMapping(value = "orderItem/{orderItem_id}", produces = "application/json;charset=utf-8")
    public String deleteOrderItem(@PathVariable("orderItem_id") Integer orderItem_id, HttpSession session) {
        JSONObject object = new JSONObject();
        Object userId = checkUser(session);
        if (userId == null) { object.put("url", LOGIN); object.put(Constants.SUCCESS, false); return String.valueOf(object); }
        List<ProductOrderItem> orderItemList = productOrderItemService.getListByUserId(Integer.valueOf(userId.toString()), null);
        boolean isMine = false;
        for (ProductOrderItem orderItem : orderItemList) {
            if (orderItem.getProductOrderItem_id().equals(orderItem_id)) { isMine = true; break; }
        }
        if (isMine) {
            boolean yn = productOrderItemService.deleteList(new Integer[]{orderItem_id});
            object.put(Constants.SUCCESS, yn);
        } else {
            object.put(Constants.SUCCESS, false);
        }
        return String.valueOf(object);
    }
}
