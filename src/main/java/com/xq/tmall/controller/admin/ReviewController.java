package com.xq.tmall.controller.admin;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.xq.tmall.controller.BaseController;
import com.xq.tmall.entity.Product;
import com.xq.tmall.entity.Review;
import com.xq.tmall.entity.User;
import com.xq.tmall.service.ReviewService;
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


@Api(tags = "后台管理-评论页")
@Controller
@RequiredArgsConstructor
public class ReviewController extends BaseController {
    private final ReviewService reviewService;

    // 转到后台管理-评论页 (返回JSON)
    @ApiOperation(value = "转到后台管理-评论页", notes = "转到后台管理-评论页")
    @ResponseBody
    @GetMapping(value = "admin/review", produces = "application/json;charset=utf-8")
    public String goToPage(HttpSession session) {
        Object adminId = checkAdmin(session);
        if (adminId == null) {
            JSONObject obj = new JSONObject();
            obj.put("success", false);
            obj.put("message", "未登录");
            return String.valueOf(obj);
        }
        OrderUtil orderUtil = new OrderUtil("review_createdate", true);
        PageUtil pageUtil = new PageUtil(0, 10);
        List<Review> reviewList = reviewService.getList(new Review(), orderUtil, pageUtil);
        Integer reviewCount = reviewService.getTotal(new Review());
        pageUtil.setTotal(reviewCount);
        JSONObject object = new JSONObject();
        object.put("reviewList", JSON.parseArray(JSON.toJSONString(reviewList)));
        object.put("reviewCount", reviewCount);
        object.put("totalPage", pageUtil.getTotalPage());
        object.put("pageUtil", pageUtil);
        object.put("success", true);
        return String.valueOf(object);
    }

    // 转到后台管理-评论详情页 (返回JSON)
    @ApiOperation(value = "转到后台管理-评论详情页", notes = "转到后台管理-评论详情页")
    @ResponseBody
    @GetMapping(value = "admin/review/{cid}", produces = "application/json;charset=utf-8")
    public String goToDetailsPage(HttpSession session, @PathVariable Integer cid) {
        Object adminId = checkAdmin(session);
        if (adminId == null) {
            JSONObject obj = new JSONObject();
            obj.put("success", false);
            obj.put("message", "未登录");
            return String.valueOf(obj);
        }
        Review review = reviewService.get(cid);
        JSONObject object = new JSONObject();
        object.put("review", JSON.parseObject(JSON.toJSONString(review)));
        object.put("success", true);
        return String.valueOf(object);
    }

    // 按条件查询评论-ajax
    @ApiOperation(value = "按条件查询评论", notes = "按条件查询评论")
    @ResponseBody
    @GetMapping(value = "admin/review/{index}/{count}", produces = "application/json;charset=utf-8")
    public String getReviewBySearch(@RequestParam(required = false) String review_name,
                                    @RequestParam(required = false) String review_content,
                                    @RequestParam(required = false) String review_userName,
                                    @RequestParam(required = false) String review_createDate,
                                    @RequestParam(required = false) String orderBy,
                                    @RequestParam(required = false, defaultValue = "true") Boolean isDesc,
                                    @PathVariable Integer index,
                                    @PathVariable Integer count) throws UnsupportedEncodingException {
        if (review_name != null) {
            review_name = "".equals(review_name) ? null : URLDecoder.decode(review_name, "UTF-8");
        }
        if (review_content != null) {
            review_content = "".equals(review_content) ? null : URLDecoder.decode(review_content, "UTF-8");
        }
        if (review_userName != null) {
            review_userName = "".equals(review_userName) ? null : URLDecoder.decode(review_userName, "UTF-8");
        }
        if (orderBy == null || "".equals(orderBy)) {
            orderBy = "review_createdate";
        }
        OrderUtil orderUtil = null;
        if (orderBy != null) {
            orderUtil = new OrderUtil(orderBy, isDesc);
        }
        JSONObject object = new JSONObject();
        Review review = new Review();
        Product product = new Product();
        product.setProduct_name(review_name);
        review.setReview_product(product);
        review.setReview_content(review_content);
        User user = new User();
        user.setUser_name(review_userName);
        review.setReview_user(user);
        review.setReview_createDate(review_createDate);
        PageUtil pageUtil = new PageUtil(index, count);
        List<Review> reviewList = reviewService.getList(review, orderUtil, pageUtil);
        object.put("reviewList", JSON.parseArray(JSON.toJSONString(reviewList)));
        Integer reviewCount = reviewService.getTotal(review);
        object.put("reviewCount", reviewCount);
        pageUtil.setTotal(reviewCount);
        object.put("totalPage", pageUtil.getTotalPage());
        object.put("pageUtil", pageUtil);
        return String.valueOf(object);
    }

    // 按ID删除评论并返回最新结果-ajax
    @ApiOperation(value = "按ID删除评论并返回最新结果", notes = "按ID删除评论并返回最新结果")
    @ResponseBody
    @GetMapping(value = "admin/review/del/{id}", produces = "application/json;charset=utf-8")
    public String deleteProductById(@PathVariable Integer id) {
        JSONObject object = new JSONObject();
        boolean yn = reviewService.deleteData(id);
        if (yn) {
            object.put(Constants.SUCCESS, true);
        } else {
            object.put(Constants.SUCCESS, false);
            throw new RuntimeException();
        }
        return String.valueOf(object);
    }
}
