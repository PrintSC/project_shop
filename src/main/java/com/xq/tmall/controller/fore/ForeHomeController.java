package com.xq.tmall.controller.fore;

import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.xq.tmall.controller.BaseController;
import com.xq.tmall.entity.Category;
import com.xq.tmall.entity.Product;
import com.xq.tmall.entity.User;
import com.xq.tmall.service.CategoryService;
import com.xq.tmall.service.ProductImageService;
import com.xq.tmall.service.ProductService;
import com.xq.tmall.service.UserService;
import com.xq.tmall.util.Constants;
import com.xq.tmall.util.OrderUtil;
import com.xq.tmall.util.PageUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;

@Api(tags = "前台天猫-主页")
@Controller
@RequiredArgsConstructor
public class ForeHomeController extends BaseController {
    private final UserService userService;
    private final CategoryService categoryService;
    private final ProductService productService;
    private final ProductImageService productImageService;

    // 转到前台主页 (返回JSON)
    @ApiOperation(value = "转到前台主页", notes = "转到前台主页")
    @ResponseBody
    @GetMapping(value = "/", produces = "application/json;charset=utf-8")
    public String goToPage(HttpSession session) {
        JSONObject jsonObject = new JSONObject();
        Object userId = checkUser(session);
        if (userId != null) {
            User user = userService.get(Integer.parseInt(userId.toString()));
            jsonObject.put("user", JSON.parseObject(JSON.toJSONString(user)));
        }
        List<Category> categoryList = categoryService.getList(null, null);
        if (CollectionUtil.isNotEmpty(categoryList)) {
            for (Category category : categoryList) {
                Product product1 = new Product();
                product1.setProduct_category(category);
                List<Product> productList = productService.getList(product1, new Byte[]{0, 2}, new OrderUtil("product_id", true), new PageUtil(0, 8));
                if (CollectionUtil.isNotEmpty(productList)) {
                    for (Product product : productList) {
                        product.setSingleProductImageList(productImageService.getList(product.getProduct_id(), (byte) 0, new PageUtil(0, 1)));
                    }
                }
                category.setProductList(productList);
            }
        }
        jsonObject.put("categoryList", JSON.parseArray(JSON.toJSONString(categoryList)));
        List<Product> specialProductList = productService.getList(null, new Byte[]{2}, null, new PageUtil(0, 6));
        jsonObject.put("specialProductList", JSON.parseArray(JSON.toJSONString(specialProductList)));
        jsonObject.put("success", true);
        return String.valueOf(jsonObject);
    }

    // 转到前台天猫-错误页
    @ApiOperation(value = "转到前台天猫-错误页", notes = "转到前台天猫-错误页")
    @ResponseBody
    @GetMapping(value = "error", produces = "application/json;charset=utf-8")
    public String goToErrorPage() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("success", false);
        jsonObject.put("message", "发生异常");
        return String.valueOf(jsonObject);
    }

    // 获取主页分类下产品信息-ajax
    @ApiOperation(value = "获取主页分类下产品信息", notes = "获取主页分类下产品信息")
    @ResponseBody
    @GetMapping(value = "product/nav/{category_id}", produces = "application/json;charset=utf-8")
    public String getProductByNav(@PathVariable("category_id") Integer category_id) {
        JSONObject object = new JSONObject();
        if (category_id == null) {
            object.put(Constants.SUCCESS, false);
            return String.valueOf(object);
        }
        Category category1 = new Category();
        category1.setCategory_id(category_id);
        Product product = new Product();
        product.setProduct_category(category1);
        List<Product> productList = productService.getTitle(product, new PageUtil(0, 40));
        List<List<Product>> complexProductList = new ArrayList<>(8);
        List<Product> products = new ArrayList<>(5);
        if (CollectionUtil.isNotEmpty(productList)) {
            for (int i = 0; i < productList.size(); i++) {
                if (i % 5 == 0) {
                    complexProductList.add(products);
                    products = new ArrayList<>(5);
                }
                products.add(productList.get(i));
            }
        }
        complexProductList.add(products);
        Category category = new Category();
        category.setCategory_id(category_id);
        category.setComplexProductList(complexProductList);
        object.put(Constants.SUCCESS, true);
        object.put("category", category);
        return String.valueOf(object);
    }
}
