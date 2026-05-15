package com.xq.tmall.controller.fore;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.xq.tmall.controller.BaseController;
import com.xq.tmall.entity.Category;
import com.xq.tmall.entity.Product;
import com.xq.tmall.service.*;
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
import java.util.List;

@Api(tags = "前台天猫-产品搜索列表")
@Controller
@RequiredArgsConstructor
public class ForeProductListController extends BaseController {
    private final ProductService productService;
    private final CategoryService categoryService;
    private final ProductImageService productImageService;
    private final ReviewService reviewService;
    private final ProductOrderItemService productOrderItemService;

    // 转到前台天猫-产品搜索列表页 (返回JSON)
    @ApiOperation(value = "转到前台天猫-产品搜索列表页", notes = "转到前台天猫-产品搜索列表页")
    @ResponseBody
    @GetMapping(value = "product", produces = "application/json;charset=utf-8")
    public String goToPage(HttpSession session,
                           @RequestParam(value = "category_id", required = false) Integer category_id,
                           @RequestParam(value = "product_name", required = false) String product_name) throws UnsupportedEncodingException {
        JSONObject jsonObject = new JSONObject();
        if (category_id == null && product_name == null) {
            jsonObject.put("success", false);
            jsonObject.put("message", "参数错误");
            return String.valueOf(jsonObject);
        }
        Product product = new Product();
        if (category_id != null) {
            Category category = new Category();
            category.setCategory_id(category_id);
            product.setProduct_category(category);
        }
        if (product_name != null && "".equals(product_name.trim())) {
            product_name = null;
        }
        if (product_name != null) {
            product.setProduct_name(product_name);
        }
        PageUtil pageUtil = new PageUtil(0, 20);
        List<Product> productList = productService.getList(product, new Byte[]{0, 2}, null, pageUtil);
        Integer productCount = productService.getTotal(product, new Byte[]{0, 2});
        for (Product p : productList) {
            p.setSingleProductImageList(productImageService.getList(p.getProduct_id(), (byte) 0, null));
            p.setProduct_sale_count(productOrderItemService.getSaleCountByProductId(p.getProduct_id()));
            p.setProduct_review_count(reviewService.getTotalByProductId(p.getProduct_id()));
            p.setProduct_category(categoryService.get(p.getProduct_category().getCategory_id()));
        }
        List<Category> categoryList = categoryService.getList(null, new PageUtil(0, 5));
        pageUtil.setTotal(productCount);
        jsonObject.put("productList", JSON.parseArray(JSON.toJSONString(productList)));
        jsonObject.put("productCount", productCount);
        jsonObject.put("totalPage", pageUtil.getTotalPage());
        jsonObject.put("pageUtil", pageUtil);
        jsonObject.put("categoryList", JSON.parseArray(JSON.toJSONString(categoryList)));
        jsonObject.put("success", true);
        return String.valueOf(jsonObject);
    }

    // 产品高级查询 (返回JSON)
    @ApiOperation(value = "产品高级查询", notes = "产品高级查询")
    @ResponseBody
    @GetMapping(value = "product/{index}/{count}", produces = "application/json;charset=utf-8")
    public String searchProduct(HttpSession session,
                                @PathVariable("index") Integer index,
                                @PathVariable("count") Integer count,
                                @RequestParam(value = "category_id", required = false) Integer category_id,
                                @RequestParam(value = "product_name", required = false) String product_name,
                                @RequestParam(required = false) String orderBy,
                                @RequestParam(required = false, defaultValue = "true") Boolean isDesc) {
        Product product = new Product();
        OrderUtil orderUtil = null;
        if (category_id != null) {
            Category category = new Category();
            category.setCategory_id(category_id);
            product.setProduct_category(category);
        }
        if (product_name != null) {
            product.setProduct_name(product_name);
        }
        if (orderBy != null) {
            orderBy = orderBy.replaceAll("[()]", "");
            orderUtil = new OrderUtil(orderBy, isDesc);
        }
        PageUtil pageUtil = new PageUtil(index, count);
        List<Product> productList = productService.getList(product, new Byte[]{0, 2}, orderUtil, pageUtil);
        Integer productCount = productService.getTotal(product, new Byte[]{0, 2});
        for (Product p : productList) {
            p.setSingleProductImageList(productImageService.getList(p.getProduct_id(), (byte) 0, null));
            p.setProduct_sale_count(productOrderItemService.getSaleCountByProductId(p.getProduct_id()));
            p.setProduct_review_count(reviewService.getTotalByProductId(p.getProduct_id()));
            p.setProduct_category(categoryService.get(p.getProduct_category().getCategory_id()));
        }
        List<Category> categoryList = categoryService.getList(null, new PageUtil(0, 5));
        pageUtil.setTotal(productCount);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("productList", JSON.parseArray(JSON.toJSONString(productList)));
        jsonObject.put("productCount", productCount);
        jsonObject.put("totalPage", pageUtil.getTotalPage());
        jsonObject.put("pageUtil", pageUtil);
        jsonObject.put("categoryList", JSON.parseArray(JSON.toJSONString(categoryList)));
        jsonObject.put("orderBy", orderBy);
        jsonObject.put("isDesc", isDesc);
        jsonObject.put("success", true);
        return String.valueOf(jsonObject);
    }
}
