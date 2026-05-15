package com.xq.tmall.controller.admin;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.xq.tmall.controller.BaseController;
import com.xq.tmall.entity.*;
import com.xq.tmall.service.*;
import com.xq.tmall.template.ProductTemplate;
import com.xq.tmall.util.Constants;
import com.xq.tmall.util.OrderUtil;
import com.xq.tmall.util.PageUtil;
import com.xq.tmall.util.excel.ExportExcel;
import com.xq.tmall.util.excel.ImportExcel;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 后台管理-产品页
 */
@Api(tags = "后台管理-产品页")
@Controller
@RequiredArgsConstructor
public class ProductController extends BaseController {
    @Value("${tmall.file-upload-path}")
    private String uploadPath;
    private final CategoryService categoryService;
    private final ProductService productService;
    private final ProductImageService productImageService;
    private final PropertyService propertyService;
    private final PropertyValueService propertyValueService;
    private final LastIDService lastIDService;
    public static final String URL = "admin/include/loginMessage";

    // 转到后台管理-产品页 (返回JSON)
    @ApiOperation(value = "转到后台管理-产品页", notes = "转到后台管理-产品页")
    @ResponseBody
    @GetMapping(value = "admin/product", produces = "application/json;charset=utf-8")
    public String goToPage(HttpSession session) {
        Object adminId = checkAdmin(session);
        if (adminId == null) {
            JSONObject obj = new JSONObject();
            obj.put("success", false);
            obj.put("message", "未登录");
            return String.valueOf(obj);
        }
        List<Category> categoryList = categoryService.getList(null, null);
        OrderUtil orderUtil = new OrderUtil("product_create_date", true);
        PageUtil pageUtil = new PageUtil(0, 10);
        List<Product> productList = productService.getList(null, null, orderUtil, pageUtil);
        Integer productCount = productService.getTotal(null, null);
        pageUtil.setTotal(productCount);
        JSONObject object = new JSONObject();
        object.put("categoryList", JSON.parseArray(JSON.toJSONString(categoryList)));
        object.put("productList", JSON.parseArray(JSON.toJSONString(productList)));
        object.put("productCount", productCount);
        object.put("totalPage", pageUtil.getTotalPage());
        object.put("pageUtil", pageUtil);
        object.put("success", true);
        return String.valueOf(object);
    }

    // 转到后台管理-产品详情页 (返回JSON)
    @ApiOperation(value = "转到后台管理-产品详情页", notes = "转到后台管理-产品详情页")
    @ResponseBody
    @GetMapping(value = "admin/product/{pid}", produces = "application/json;charset=utf-8")
    public String goToDetailsPage(HttpSession session, @PathVariable Integer pid) {
        Object adminId = checkAdmin(session);
        if (adminId == null) {
            JSONObject obj = new JSONObject();
            obj.put("success", false);
            obj.put("message", "未登录");
            return String.valueOf(obj);
        }
        Product product = productService.get(pid);
        Integer product_id = product.getProduct_id();
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
        PropertyValue propertyVa = new PropertyValue();
        propertyVa.setPropertyValue_product(product);
        List<PropertyValue> propertyValueList = propertyValueService.getList(propertyVa, null);
        Property proper = new Property();
        proper.setProperty_category(product.getProduct_category());
        List<Property> propertyList = propertyService.getList(proper, null);
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
        List<Category> categoryList = categoryService.getList(null, null);
        JSONObject object = new JSONObject();
        object.put("product", JSON.parseObject(JSON.toJSONString(product)));
        object.put("propertyList", JSON.parseArray(JSON.toJSONString(propertyList)));
        object.put("categoryList", JSON.parseArray(JSON.toJSONString(categoryList)));
        object.put("success", true);
        return String.valueOf(object);
    }

    // 转到后台管理-产品添加页 (返回JSON)
    @ApiOperation(value = "转到后台管理-产品添加页", notes = "转到后台管理-产品添加页")
    @ResponseBody
    @GetMapping(value = "admin/product/new", produces = "application/json;charset=utf-8")
    public String goToAddPage(HttpSession session) {
        Object adminId = checkAdmin(session);
        if (adminId == null) {
            JSONObject obj = new JSONObject();
            obj.put("success", false);
            obj.put("message", "未登录");
            return String.valueOf(obj);
        }
        List<Category> categoryList = categoryService.getList(null, null);
        Property property = new Property();
        property.setProperty_category(categoryList.get(0));
        List<Property> propertyList = propertyService.getList(property, null);
        JSONObject object = new JSONObject();
        object.put("categoryList", JSON.parseArray(JSON.toJSONString(categoryList)));
        object.put("propertyList", JSON.parseArray(JSON.toJSONString(propertyList)));
        object.put("success", true);
        return String.valueOf(object);
    }

    // 添加产品信息-ajax.
    @ApiOperation(value = "添加产品信息", notes = "添加产品信息")
    @ResponseBody
    @PostMapping(value = "admin/product", produces = "application/json;charset=utf-8")
    public String addProduct(@RequestParam String product_name/* 产品名称 */,
                             @RequestParam String product_title/* 产品标题 */,
                             @RequestParam Integer product_category_id/* 产品类型ID */,
                             @RequestParam Double product_sale_price/* 产品最低价 */,
                             @RequestParam Double product_price/* 产品最高价 */,
                             @RequestParam Byte product_isEnabled/* 产品状态 */,
                             @RequestParam String propertyJson/* 产品属性JSON */,
                             @RequestParam(required = false) String[] productSingleImageList/*产品预览图片名称数组*/,
                             @RequestParam(required = false) String[] productDetailsImageList/*产品详情图片名称数组*/) {
        JSONObject jsonObject = new JSONObject();
        // 整合产品信息
        Product product = new Product();
        product.setProduct_name(product_name);
        product.setProduct_title(product_title);
        Category category = new Category();
        category.setCategory_id(product_category_id);
        product.setProduct_category(category);
        product.setProduct_sale_price(product_sale_price);
        product.setProduct_price(product_price);
        product.setProduct_isEnabled(product_isEnabled);
        SimpleDateFormat time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.UK);
        product.setProduct_create_date(time.format(new Date()));
        // 添加产品信息
        boolean yn = productService.add(product);
        if (!yn) {
            // 产品添加失败！事务回滚
            jsonObject.put(Constants.SUCCESS, false);
            throw new RuntimeException();
        }
        int product_id = lastIDService.selectLastID();
        // 添加成功！,新增产品的ID值为：{}, product_id
        JSONObject object = JSON.parseObject(propertyJson);
        Set<String> propertyIdSet = object.keySet();
        if (CollectionUtil.isNotEmpty(propertyIdSet)) {
            // 整合产品子信息-产品属性
            List<PropertyValue> propertyValueList = new ArrayList<>(5);
            for (String key : propertyIdSet) {
                String value = object.getString(key);
                PropertyValue propertyValue = new PropertyValue();
                propertyValue.setPropertyValue_value(value);
                Property property = new Property();
                property.setProperty_id(Integer.valueOf(key));
                Product pro = new Product();
                pro.setProduct_id(product_id);
                propertyValue.setPropertyValue_property(property);
                propertyValue.setPropertyValue_product(pro);
                propertyValueList.add(propertyValue);
            }
            // 共有{}条产品属性数据, propertyValueList.size()
            yn = propertyValueService.addList(propertyValueList);
            if (yn) {
                // 产品属性添加成功！
            } else {
                // 产品属性添加失败！事务回滚
                jsonObject.put(Constants.SUCCESS, false);
                throw new RuntimeException();
            }
        }
        if (productSingleImageList != null && productSingleImageList.length > 0) {
            // 整合产品子信息-产品预览图片
            List<ProductImage> productImageList = new ArrayList<>(5);
            for (String imageName : productSingleImageList) {
                ProductImage productImage = new ProductImage();
                productImage.setProductImage_type((byte) 0);
                productImage.setProductImage_src(imageName.substring(imageName.lastIndexOf("/") + 1));
                Product pro = new Product();
                pro.setProduct_id(product_id);
                productImage.setProductImage_product(pro);
                productImageList.add(productImage);
            }
            // 共有{}条产品预览图片数据, productImageList.size()
            yn = productImageService.addList(productImageList);
            if (yn) {
                // 产品预览图片添加成功！
            } else {
                // 产品预览图片添加失败！事务回滚
                jsonObject.put(Constants.SUCCESS, false);
                throw new RuntimeException();
            }
        }
        if (productDetailsImageList != null && productDetailsImageList.length > 0) {
            // 整合产品子信息-产品详情图片
            List<ProductImage> productImageList = new ArrayList<>(5);
            for (String imageName : productDetailsImageList) {
                ProductImage productImage = new ProductImage();
                productImage.setProductImage_type((byte) 1);
                productImage.setProductImage_src(imageName.substring(imageName.lastIndexOf("/") + 1));
                Product product1 = new Product();
                product1.setProduct_id(product_id);
                productImage.setProductImage_product(product1);
                productImageList.add(productImage);
            }
            // 共有{}条产品详情图片数据, productImageList.size()
            yn = productImageService.addList(productImageList);
            if (yn) {
                // 产品详情图片添加成功！
            } else {
                // 产品详情图片添加失败！事务回滚
                jsonObject.put(Constants.SUCCESS, false);
                throw new RuntimeException();
            }
        }
        // 产品信息及其子信息添加成功！
        jsonObject.put(Constants.SUCCESS, true);
        jsonObject.put("product_id", product_id);
        return String.valueOf(jsonObject);
    }

    // 更新产品信息-ajax
    @ApiOperation(value = "更新产品信息", notes = "更新产品信息")
    @ResponseBody
    @PutMapping(value = "admin/product/{product_id}", produces = "application/json;charset=utf-8")
    public String updateProduct(@RequestParam String product_name/* 产品名称 */,
                                @RequestParam String product_title/* 产品标题 */,
                                @RequestParam Integer product_category_id/* 产品类型ID */,
                                @RequestParam Double product_sale_price/* 产品最低价 */,
                                @RequestParam Double product_price/* 产品最高价 */,
                                @RequestParam Byte product_isEnabled/* 产品状态 */,
                                @RequestParam String propertyAddJson/* 产品添加属性JSON */,
                                @RequestParam String propertyUpdateJson/* 产品更新属性JSON */,
                                @RequestParam(required = false) Integer[] propertyDeleteList/* 产品删除属性ID数组 */,
                                @RequestParam(required = false) String[] productSingleImageList/*产品预览图片名称数组*/,
                                @RequestParam(required = false) String[] productDetailsImageList/*产品详情图片名称数组*/,
                                @PathVariable("product_id") Integer product_id/* 产品ID */) {
        JSONObject jsonObject = new JSONObject();
        // 整合产品信息
        Product product = new Product();
        product.setProduct_id(product_id);
        product.setProduct_name(product_name);
        product.setProduct_title(product_title);
        Category category = new Category();
        category.setCategory_id(product_category_id);
        product.setProduct_category(category);
        product.setProduct_sale_price(product_sale_price);
        product.setProduct_price(product_price);
        product.setProduct_isEnabled(product_isEnabled);
        SimpleDateFormat time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.UK);
        product.setProduct_create_date(time.format(new Date()));
        // 更新产品信息，产品ID值为：{}, product_id
        boolean yn = productService.update(product);
        if (!yn) {
            // 产品信息更新失败！事务回滚
            jsonObject.put(Constants.SUCCESS, false);
            throw new RuntimeException();
        }
        // 产品信息更新成功！
        JSONObject object = JSON.parseObject(propertyAddJson);
        Set<String> propertyIdSet = object.keySet();
        if (CollectionUtil.isNotEmpty(propertyIdSet)) {
            // 整合产品子信息-需要添加的产品属性
            List<PropertyValue> propertyValueList = new ArrayList<>(5);
            for (String key : propertyIdSet) {
                String value = object.getString(key);
                PropertyValue propertyValue = new PropertyValue();
                propertyValue.setPropertyValue_value(value);
                Property property = new Property();
                property.setProperty_id(Integer.valueOf(key));
                propertyValue.setPropertyValue_property(property);
                propertyValue.setPropertyValue_product(product);
                propertyValueList.add(propertyValue);
            }
            // 共有{}条需要添加的产品属性数据, propertyValueList.size()
            yn = propertyValueService.addList(propertyValueList);
            if (!yn) {
                // 产品属性添加失败！事务回滚
                jsonObject.put(Constants.SUCCESS, false);
                throw new RuntimeException();
            }
        }
        object = JSON.parseObject(propertyUpdateJson);
        propertyIdSet = object.keySet();
        if (CollectionUtil.isNotEmpty(propertyIdSet)) {
            // 整合产品子信息-需要更新的产品属性
            List<PropertyValue> propertyValueList = new ArrayList<>(5);
            for (String key : propertyIdSet) {
                String value = object.getString(key);
                PropertyValue propertyValue = new PropertyValue();
                propertyValue.setPropertyValue_value(value);
                propertyValue.setPropertyValue_id(Integer.valueOf(key));
                propertyValueList.add(propertyValue);
            }
            // 共有{}条需要更新的产品属性数据, propertyValueList.size()
            for (int i = 0; i < propertyValueList.size(); i++) {
                yn = propertyValueService.update(propertyValueList.get(i));
                if (!yn) {
                    jsonObject.put(Constants.SUCCESS, false);
                    throw new RuntimeException();
                }
            }
        }
        if (propertyDeleteList != null && propertyDeleteList.length > 0) {
            // 整合产品子信息-需要删除的产品属性
            // 共有{}条需要删除的产品属性数据, propertyDeleteList.length
            yn = propertyValueService.deleteList(propertyDeleteList);
            if (!yn) {
                jsonObject.put(Constants.SUCCESS, false);
                throw new RuntimeException();
            }
        }
        if (productSingleImageList != null && productSingleImageList.length > 0) {
            // 整合产品子信息-产品预览图片
            List<ProductImage> productImageList = new ArrayList<>(5);
            for (String imageName : productSingleImageList) {
                ProductImage productImage = new ProductImage();
                productImage.setProductImage_type((byte) 0);
                productImage.setProductImage_src(imageName.substring(imageName.lastIndexOf("/") + 1));
                productImage.setProductImage_product(product);
                productImageList.add(productImage);
            }
            // 共有{}条产品预览图片数据, productImageList.size()
            yn = productImageService.addList(productImageList);
            if (!yn) {
                jsonObject.put(Constants.SUCCESS, false);
                throw new RuntimeException();
            }
        }
        if (productDetailsImageList != null && productDetailsImageList.length > 0) {
            // 整合产品子信息-产品详情图片
            List<ProductImage> productImageList = new ArrayList<>(5);
            for (String imageName : productDetailsImageList) {
                ProductImage productImage = new ProductImage();
                productImage.setProductImage_type((byte) 1);
                productImage.setProductImage_src(imageName.substring(imageName.lastIndexOf("/") + 1));
                productImage.setProductImage_product(product);
                productImageList.add(productImage);
            }
            // 共有{}条产品详情图片数据, productImageList.size()
            yn = productImageService.addList(productImageList);
            if (!yn) {
                jsonObject.put(Constants.SUCCESS, false);
                throw new RuntimeException();
            }
        }
        jsonObject.put(Constants.SUCCESS, true);
        jsonObject.put("product_id", product_id);
        return String.valueOf(jsonObject);
    }

    // 按条件查询产品-ajax
    @ApiOperation(value = "按条件查询产品", notes = "按条件查询产品")
    @ResponseBody
    @GetMapping(value = "admin/product/{index}/{count}", produces = "application/json;charset=utf-8")
    public String getProductBySearch(@RequestParam(required = false) String product_name/* 产品名称 */,
                                     @RequestParam(required = false) Integer category_id/* 产品类型ID */,
                                     @RequestParam(required = false) Double product_sale_price/* 产品最低价 */,
                                     @RequestParam(required = false) Double product_price/* 产品最高价 */,
                                     @RequestParam(required = false) Byte[] product_isEnabled_array/* 产品状态数组 */,
                                     @RequestParam(required = false) String orderBy/* 排序字段 */,
                                     @RequestParam(required = false, defaultValue = "true") Boolean isDesc/* 是否倒序 */,
                                     @PathVariable Integer index/* 页数 */,
                                     @PathVariable Integer count/* 行数 */) throws UnsupportedEncodingException {
        // 移除不必要条件
        if (product_isEnabled_array != null && (product_isEnabled_array.length <= 0 || product_isEnabled_array.length >= 3)) {
            product_isEnabled_array = null;
        }
        if (category_id != null && category_id == 0) {
            category_id = null;
        }
        if (product_name != null) {
            // 如果为非空字符串则解决中文乱码
            product_name = "".equals(product_name) ? null : URLDecoder.decode(product_name, "UTF-8");
        }
        if (orderBy == null || "".equals(orderBy)) {
            orderBy = "product_create_date";
        }
        // 封装查询条件
        Product product = new Product();
        product.setProduct_name(product_name);
        Category category = new Category();
        category.setCategory_id(category_id);
        product.setProduct_category(category);
        product.setProduct_price(product_price);
        product.setProduct_sale_price(product_sale_price);
        OrderUtil orderUtil = null;
        if (orderBy != null) {
            // 根据{}排序，是否倒序:{}, orderBy, isDesc
            orderUtil = new OrderUtil(orderBy, isDesc);
        }
        JSONObject object = new JSONObject();
        // 按条件获取第{}页的{}条产品, index + 1, count
        PageUtil pageUtil = new PageUtil(index, count);
        List<Product> productList = productService.getList(product, product_isEnabled_array, orderUtil, pageUtil);
        object.put("productList", JSON.parseArray(JSON.toJSONString(productList)));
        // 按条件获取产品总数量
        Integer productCount = productService.getTotal(product, product_isEnabled_array);
        object.put("productCount", productCount);
        // 获取分页信息
        pageUtil.setTotal(productCount);
        object.put("totalPage", pageUtil.getTotalPage());
        object.put("pageUtil", pageUtil);
        return String.valueOf(object);
    }

    // 按类型ID查询属性-ajax
    @ApiOperation(value = "按类型ID查询属性", notes = "按类型ID查询属性")
    @ResponseBody
    @GetMapping(value = "admin/property/type/{property_category_id}", produces = "application/json;charset=utf-8")
    public String getPropertyByCategoryId(@PathVariable Integer property_category_id/* 属性所属类型ID*/) {
        // 封装查询条件
        Category category = new Category();
        category.setCategory_id(property_category_id);
        JSONObject object = new JSONObject();
        // 按类型获取属性列表，类型ID：{}, property_category_id
        Property property = new Property();
        property.setProperty_category(category);
        List<Property> propertyList = propertyService.getList(property, null);
        object.put(Constants.PROPERTY_LIST, JSON.parseArray(JSON.toJSONString(propertyList)));
        return String.valueOf(object);
    }

    // 按ID删除产品图片并返回最新结果-ajax
    @ApiOperation(value = "按ID删除产品图片并返回最新结果", notes = "按ID删除产品图片并返回最新结果")
    @ResponseBody
    @DeleteMapping(value = "admin/productImage/{productImage_id}", produces = "application/json;charset=utf-8")
    public String deleteProductImageById(@PathVariable Integer productImage_id/* 产品图片ID */) {
        JSONObject object = new JSONObject();
        // 删除产品图片
        Boolean yn = productImageService.deleteList(new Integer[]{productImage_id});
        if (yn) {
            // 删除图片成功！
            object.put(Constants.SUCCESS, true);
        } else {
            // 删除图片失败！事务回滚
            object.put(Constants.SUCCESS, false);
            throw new RuntimeException();
        }
        return String.valueOf(object);
    }

    // 上传产品图片-ajax
    @ApiOperation(value = "上传产品图片", notes = "上传产品图片")
    @ResponseBody
    @PostMapping(value = "admin/uploadProductImage", produces = "application/json;charset=utf-8")
    public String uploadProductImage(@RequestParam MultipartFile file, @RequestParam String imageType, HttpSession session) {
        String originalFileName = file.getOriginalFilename();
        if (StringUtils.isEmpty(originalFileName)) {
            throw new RuntimeException("上传失败！");
        }
        // 获取图片原始文件名：{}", originalFileName
        String extension = originalFileName.substring(originalFileName.lastIndexOf('.'));
        String filePath;
        String fileName = UUID.randomUUID() + extension;
        if ("single".equals(imageType)) {
            filePath = uploadPath + "/res/images/item/productSinglePicture/" + fileName;
        } else {
            filePath = uploadPath + "/res/images/item/productDetailsPicture/" + fileName;
        }
        // 文件上传路径：{}, filePath
        JSONObject object = new JSONObject();
        try {
            // 文件上传中...
            File dest = new File(filePath);
            dest.getParentFile().mkdirs();
            file.transferTo(dest);
            // 文件上传完成
            object.put(Constants.SUCCESS, true);
            object.put("fileName", fileName);
        } catch (IOException e) {
            logger.warn("文件上传失败！");
            e.printStackTrace();
            object.put(Constants.SUCCESS, false);
        }
        return String.valueOf(object);
    }

    // 按ID删除产品并返回最新结果-ajax
    @ApiOperation(value = "按ID删除产品并返回最新结果", notes = "按ID删除产品并返回最新结果")
    @ResponseBody
    @GetMapping(value = "admin/product/del/{id}", produces = "application/json;charset=utf-8")
    public String deleteProductById(HttpSession session, @PathVariable Integer id) {
        Object adminId = checkAdmin(session);
        if (adminId == null) {
            JSONObject obj = new JSONObject();
            obj.put("success", false);
            obj.put("message", "未登录");
            return String.valueOf(obj);
        }
        JSONObject object = new JSONObject();
        // 删除产品属性
        propertyValueService.delete(id);
        boolean yn = productService.delete(id);
        if (yn) {
            // 删除成功！
            object.put(Constants.SUCCESS, true);
        } else {
            // 删除失败！事务回滚
            object.put(Constants.SUCCESS, false);
            throw new RuntimeException();
        }
        return String.valueOf(object);
    }

    /**
     * 导入产品excel文件
     *
     * @param file
     * @return
     */
    @ApiOperation(value = "导入产品excel文件", notes = "导入产品excel文件")
    @ResponseBody
    @PostMapping(value = "admin/product/importFile")
    public String importFile(@RequestParam("file") MultipartFile file) {
        JSONObject object = new JSONObject();
        try {
            ImportExcel ei = new ImportExcel(file, 1, 0);
            List<ProductTemplate> list = ei.getDataList(ProductTemplate.class);
            if (CollUtil.isNotEmpty(list)) {
                List<Product> products = new ArrayList<>();
                // 整合产品信息
                Product product;
                for (ProductTemplate productTemplate : list) {
                    product = new Product();
                    product.setProduct_name(productTemplate.getProduct_name());
                    product.setProduct_title(productTemplate.getProduct_title());
                    product.setProduct_sale_price(productTemplate.getProduct_sale_price());
                    product.setProduct_price(productTemplate.getProduct_price());
                    product.setProduct_isEnabled((byte) 1);
                    SimpleDateFormat time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.UK);
                    product.setProduct_create_date(time.format(new Date()));
                    products.add(product);
                }
                // 添加产品信息
                int yn = productService.saveScheme(products);
                if (yn > 0) {
                    object.put(Constants.SUCCESS, true);
                } else {
                    object.put(Constants.SUCCESS, false);
                    // 产品添加失败！事务回滚
                    throw new RuntimeException();
                }
            }
        } catch (Exception e) {
            logger.error(e);
        }
        return String.valueOf(object);
    }

    /**
     * 下载产品导出数据模板
     */
    @ApiOperation(value = "下载产品导出数据模板", notes = "下载产品导出数据模板")
    @GetMapping(value = "admin/product/template")
    public Object importFileTemplate(HttpServletResponse response) {
        try {
            String fileName = "产品导入模板.xlsx";
            List<ProductTemplate> list = Lists.newArrayList();
            new ExportExcel("产品导入模板", ProductTemplate.class, 1).setDataList(list).write(response, fileName).dispose();
            return null;
        } catch (Exception e) {
            return "导入模板下载失败！";
        }
    }
}