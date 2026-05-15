package com.xq.tmall.controller.fore;

import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.xq.tmall.controller.BaseController;
import com.xq.tmall.entity.*;
import com.xq.tmall.service.*;
import com.xq.tmall.util.PageUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * 前台天猫-评论添加
 */
@Api(tags = "前台天猫-评论添加")
@Controller
@RequiredArgsConstructor
public class ForeReviewController extends BaseController {
    private final ReviewService reviewService;
    private final UserService userService;
    private final ProductOrderItemService productOrderItemService;
    private final ProductOrderService productOrderService;
    private final ProductService productService;
    private final ProductImageService productImageService;

    // 转到前台天猫-评论添加页 (返回JSON)
    @ApiOperation(value = "转到前台天猫-评论添加页", notes = "转到前台天猫-评论添加页")
    @ResponseBody
    @GetMapping(value = "review/item/{orderItem_id}", produces = "application/json;charset=utf-8")
    public String goToPage(HttpSession session,
                           @PathVariable("orderItem_id") Integer orderItem_id) {
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
        // 获取订单项信息
        ProductOrderItem orderItem = productOrderItemService.get(orderItem_id);
        if (orderItem == null) {
            jsonObject.put("success", false);
            jsonObject.put("message", "订单项不存在");
            return jsonObject.toString();
        }
        if (!orderItem.getProductOrderItem_user().getUser_id().equals(userId)) {
            jsonObject.put("success", false);
            jsonObject.put("message", "无权访问");
            return jsonObject.toString();
        }
        if (orderItem.getProductOrderItem_order() == null) {
            jsonObject.put("success", false);
            jsonObject.put("message", "订单项状态有误");
            return jsonObject.toString();
        }
        ProductOrder order = productOrderService.get(orderItem.getProductOrderItem_order().getProductOrder_id());
        if (order == null || order.getProductOrder_status() != 3) {
            jsonObject.put("success", false);
            jsonObject.put("message", "订单状态有误");
            return jsonObject.toString();
        }
        if (reviewService.getTotalByOrderItemId(orderItem_id) > 0) {
            jsonObject.put("success", false);
            jsonObject.put("message", "该商品已被评价");
            return jsonObject.toString();
        }
        // 获取订单项所属产品信息和产品评论信息
        Product product = productService.get(orderItem.getProductOrderItem_product().getProduct_id());
        product.setProduct_review_count(reviewService.getTotalByProductId(product.getProduct_id()));
        product.setSingleProductImageList(productImageService.getList(product.getProduct_id(), (byte) 0, new PageUtil(0, 1)));
        orderItem.setProductOrderItem_product(product);
        jsonObject.put("orderItem", JSON.parseObject(JSON.toJSONString(orderItem)));
        jsonObject.put("success", true);
        return jsonObject.toString();
    }

    // 添加一条评论 (返回JSON)
    @ApiOperation(value = "添加一条评论", notes = "添加一条评论")
    @ResponseBody
    @PostMapping(value = "review", produces = "application/json;charset=utf-8")
    public String addReview(HttpSession session,
                            @RequestParam Integer orderItem_id,
                            @RequestParam String review_content) {
        JSONObject jsonObject = new JSONObject();
        // 检查用户是否登录
        Object userId = checkUser(session);
        if (userId == null) {
            jsonObject.put("success", false);
            jsonObject.put("message", "未登录");
            return jsonObject.toString();
        }
        User user = userService.get(Integer.parseInt(userId.toString()));
        // 获取订单项信息
        ProductOrderItem orderItem = productOrderItemService.get(orderItem_id);
        if (orderItem == null) {
            jsonObject.put("success", false);
            jsonObject.put("message", "订单项不存在");
            return jsonObject.toString();
        }
        if (!orderItem.getProductOrderItem_user().getUser_id().equals(userId)) {
            jsonObject.put("success", false);
            jsonObject.put("message", "无权访问");
            return jsonObject.toString();
        }
        if (orderItem.getProductOrderItem_order() == null) {
            jsonObject.put("success", false);
            jsonObject.put("message", "订单项状态有误");
            return jsonObject.toString();
        }
        ProductOrder order = productOrderService.get(orderItem.getProductOrderItem_order().getProductOrder_id());
        if (order == null || order.getProductOrder_status() != 3) {
            jsonObject.put("success", false);
            jsonObject.put("message", "订单状态有误");
            return jsonObject.toString();
        }
        if (reviewService.getTotalByOrderItemId(orderItem_id) > 0) {
            jsonObject.put("success", false);
            jsonObject.put("message", "该商品已被评价");
            return jsonObject.toString();
        }
        // 整合评论信息
        Review review = new Review();
        review.setReview_product(orderItem.getProductOrderItem_product());
        review.setReview_content(review_content);
        SimpleDateFormat time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.UK);
        review.setReview_createDate(time.format(new Date()));
        review.setReview_user(user);
        review.setReview_orderItem(orderItem);
        // 添加评论
        if (!reviewService.add(review)) {
            jsonObject.put("success", false);
            jsonObject.put("message", "评论添加失败");
            return jsonObject.toString();
        }
        jsonObject.put("success", true);
        jsonObject.put("productId", orderItem.getProductOrderItem_product().getProduct_id());
        return jsonObject.toString();
    }

    // 获取产品评论信息-ajax
    @ApiOperation(value = "获取产品评论信息", notes = "获取产品评论信息")
    @ResponseBody
    @GetMapping(value = "review", produces = "application/json;charset=utf-8")
    public String getReviewInfo(@RequestParam("product_id") Integer product_id,
                                @RequestParam("index") Integer index/* 页数 */,
                                @RequestParam("count") Integer count/* 行数*/) {
        // 获取产品评论信息
        List<Review> reviewList = reviewService.getListByProductId(product_id, new PageUtil(index, 10));
        if (CollectionUtil.isNotEmpty(reviewList)) {
            for (Review review : reviewList) {
                review.setReview_user(userService.get(review.getReview_user().getUser_id()));
            }
        }
        Integer total = reviewService.getTotalByProductId(product_id);
        JSONObject object = new JSONObject();
        object.put("reviewList", reviewList);
        object.put("pageUtil", new PageUtil().setTotal(total).setIndex(index).setCount(count));
        return String.valueOf(object);
    }
}
