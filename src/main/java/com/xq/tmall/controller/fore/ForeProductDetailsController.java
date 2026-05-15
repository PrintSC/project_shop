package com.xq.tmall.controller.fore;

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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Api(tags = "前台天猫-产品详情页")
@Controller
@RequiredArgsConstructor
public class ForeProductDetailsController extends BaseController {
    private final ProductService productService;
    private final UserService userService;
    private final ProductImageService productImageService;
    private final CategoryService categoryService;
    private final PropertyValueService propertyValueService;
    private final PropertyService propertyService;
    private final ReviewService reviewService;
    private final ProductOrderItemService productOrderItemService;

    // 转到前台天猫-产品详情页 (返回JSON)
    @ApiOperation(value = "转到前台天猫-产品详情页", notes = "转到前台天猫-产品详情页")
    @ResponseBody
    @GetMapping(value = "product/{pid}", produces = "application/json;charset=utf-8")
    public String goToPage(HttpSession session, @PathVariable("pid") String pid) {
        JSONObject jsonObject = new JSONObject();
        Object userId = checkUser(session);
        if (userId != null) {
            User user = userService.get(Integer.parseInt(userId.toString()));
            jsonObject.put("user", JSON.parseObject(JSON.toJSONString(user)));
        }
        Integer product_id = Integer.parseInt(pid);
        Product product = productService.get(product_id);
        if (product == null || product.getProduct_isEnabled() == 1) {
            jsonObject.put("success", false);
            jsonObject.put("message", "产品不存在");
            return String.valueOf(jsonObject);
        }
        product.setProduct_category(categoryService.get(product.getProduct_category().getCategory_id()));
        List<ProductImage> productImageList = productImageService.getList(product_id, null, null);
        List<ProductImage> singleProductImageList = new ArrayList<>(5);
        List<ProductImage> detailsProductImageList = new ArrayList<>(8);
        for (ProductImage productImage : productImageList) {
            if (productImage.getProductImage_type() == 0) {
                singleProductImageList.add(productImage);
            } else {
                detailsProductImageList.add(productImage);
            }
        }
        product.setSingleProductImageList(singleProductImageList);
        product.setDetailProductImageList(detailsProductImageList);
        PropertyValue propertyValue1 = new PropertyValue();
        propertyValue1.setPropertyValue_product(product);
        List<PropertyValue> propertyValueList = propertyValueService.getList(propertyValue1, null);
        Property property1 = new Property();
        property1.setProperty_category(product.getProduct_category());
        List<Property> propertyList = propertyService.getList(property1, null);
        for (Property property : propertyList) {
            for (PropertyValue propertyValue : propertyValueList) {
                if (property.getProperty_id().equals(propertyValue.getPropertyValue_property().getProperty_id())) {
                    List<PropertyValue> property_value_item = new ArrayList<>(1);
                    property_value_item.add(propertyValue);
                    property.setPropertyValueList(property_value_item);
                    break;
                }
            }
        }
        product.setReviewList(reviewService.getListByProductId(product_id, null));
        if (product.getReviewList() != null) {
            for (Review review : product.getReviewList()) {
                review.setReview_user(userService.get(review.getReview_user().getUser_id()));
            }
        }
        product.setProduct_sale_count(productOrderItemService.getSaleCountByProductId(product_id));
        product.setProduct_review_count(reviewService.getTotalByProductId(product_id));
        Integer category_id = product.getProduct_category().getCategory_id();
        Product product1 = new Product();
        Category category = new Category();
        category.setCategory_id(category_id);
        product1.setProduct_category(category);
        Integer total = productService.getTotal(product1, new Byte[]{0, 2});
        int i = new Random().nextInt(total);
        if (i + 2 >= total) { i = total - 3; }
        if (i < 0) { i = 0; }
        List<Product> loveProductList = productService.getList(product1, new Byte[]{0, 2}, null, new PageUtil().setCount(3).setPageStart(i));
        if (loveProductList != null) {
            for (Product loveProduct : loveProductList) {
                loveProduct.setSingleProductImageList(productImageService.getList(loveProduct.getProduct_id(), (byte) 0, new PageUtil(0, 1)));
            }
        }
        jsonObject.put("product", JSON.parseObject(JSON.toJSONString(product)));
        jsonObject.put("propertyList", JSON.parseArray(JSON.toJSONString(propertyList)));
        jsonObject.put("loveProductList", JSON.parseArray(JSON.toJSONString(loveProductList)));
        jsonObject.put("guessNumber", i);
        jsonObject.put("pageUtil", new PageUtil(0, 10).setTotal(product.getProduct_review_count()));
        jsonObject.put("success", true);
        return String.valueOf(jsonObject);
    }

    // 按产品ID加载产品评论列表-ajax
    @ApiOperation(value = "按产品ID加载产品评论列表", notes = "按产品ID加载产品评论列表")
    @ResponseBody
    @GetMapping(value = "review/{pid}", produces = "application/json;charset=utf-8")
    public String loadProductReviewList(@PathVariable("pid") String pid,
                                        @RequestParam Integer index,
                                        @RequestParam Integer count) {
        Integer product_id = Integer.parseInt(pid);
        List<Review> reviewList = reviewService.getListByProductId(product_id, new PageUtil(index, count));
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("reviewList", JSON.parseArray(JSON.toJSONString(reviewList)));
        return String.valueOf(jsonObject);
    }

    // 按产品ID加载产品属性列表-ajax
    @ApiOperation(value = "按产品ID加载产品属性列表", notes = "按产品ID加载产品属性列表")
    @ResponseBody
    @GetMapping(value = "property/{pid}", produces = "application/json;charset=utf-8")
    public String loadProductPropertyList(@PathVariable("pid") String pid) {
        Integer product_id = Integer.parseInt(pid);
        Product product = new Product();
        product.setProduct_id(product_id);
        PropertyValue propertyValue1 = new PropertyValue();
        propertyValue1.setPropertyValue_product(product);
        List<PropertyValue> propertyValueList = propertyValueService.getList(propertyValue1, null);
        Property property1 = new Property();
        property1.setProperty_category(product.getProduct_category());
        List<Property> propertyList = propertyService.getList(property1, null);
        for (Property property : propertyList) {
            for (PropertyValue propertyValue : propertyValueList) {
                if (property.getProperty_id().equals(propertyValue.getPropertyValue_property().getProperty_id())) {
                    List<PropertyValue> property_value_item = new ArrayList<>(1);
                    property_value_item.add(propertyValue);
                    property.setPropertyValueList(property_value_item);
                    break;
                }
            }
        }
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("propertyList", JSON.parseArray(JSON.toJSONString(propertyList)));
        return String.valueOf(jsonObject);
    }

    // 加载猜你喜欢列表-ajax
    @ApiOperation(value = "加载猜你喜欢列表", notes = "加载猜你喜欢列表")
    @ResponseBody
    @GetMapping(value = "guess/{cid}", produces = "application/json;charset=utf-8")
    public String guessYouLike(@PathVariable("cid") Integer cid, @RequestParam Integer guessNumber) {
        Random rand = new Random();
        Category category = new Category();
        category.setCategory_id(cid);
        Product product = new Product();
        product.setProduct_category(category);
        Integer total = productService.getTotal(product, new Byte[]{0, 2});
        int i = rand.nextInt(total);
        if (i + 2 >= total) { i = total - 3; }
        if (i < 0) { i = 0; }
        while (i == guessNumber) {
            i = rand.nextInt(total);
            if (i + 2 >= total) { i = total - 3; }
            if (i < 0) { i = 0; break; }
        }
        List<Product> loveProductList = productService.getList(product, new Byte[]{0, 2}, null, new PageUtil().setCount(3).setPageStart(i));
        if (loveProductList != null) {
            for (Product loveProduct : loveProductList) {
                loveProduct.setSingleProductImageList(productImageService.getList(loveProduct.getProduct_id(), (byte) 0, new PageUtil(0, 1)));
            }
        }
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("success", true);
        jsonObject.put("loveProductList", JSON.parseArray(JSON.toJSONString(loveProductList)));
        jsonObject.put("guessNumber", i);
        return String.valueOf(jsonObject);
    }
}
