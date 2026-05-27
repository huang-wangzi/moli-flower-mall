package com.hengzhou.moli.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web配置类
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${upload.path:D:/uploads}")
    private String uploadPath;

    @Value("${upload.url-prefix:/uploads}")
    private String urlPrefix;

    /**
     * 配置静态资源映射
     * 不再自动添加 /api 前缀，因为 Controller 已经包含了正确的路径
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler(urlPrefix + "/**")
                .addResourceLocations("file:" + uploadPath + "/");
        
        System.out.println("[WebConfig] 文件上传路径: " + uploadPath);
        System.out.println("[WebConfig] 文件访问前缀: " + urlPrefix);
    }
}
