package com.xq.tmall.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Value("${tmall.file-upload-path}")
    private String uploadPath;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 解析为绝对路径，支持相对路径配置
        String absolutePath = new File(uploadPath, "res/images").getAbsolutePath();
        registry.addResourceHandler("/res/images/**")
                .addResourceLocations("file:" + absolutePath + "/");
    }
}
