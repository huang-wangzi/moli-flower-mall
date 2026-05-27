# 文件上传功能配置说明

## 问题描述

商家上传商品图片时，图片无法永久保存和显示。主要原因是：
1. 前端使用 blob URL（临时本地路径），刷新后失效
2. 后端没有文件上传接口
3. 图片数据直接存储为 JSON 数组，但图片本身没有上传到服务器

## 解决方案

### 1. 后端：新增文件上传接口

已在 `cursor-backend/src/main/java/com/hengzhou/moli/controller/FileUploadController.java` 创建文件上传控制器。

### 2. 配置静态资源访问

在 Spring Boot 中配置静态资源映射，使得上传的文件可以通过 URL 访问。

#### 方式一：代码配置（推荐）

创建配置类：

```java
package com.hengzhou.moli.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web配置类
 * 功能：配置静态资源映射，使上传的文件可以通过URL访问
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    /**
     * 文件上传路径，从application.yml读取
     */
    @Value("${upload.path:D:/uploads}")
    private String uploadPath;

    /**
     * 文件访问URL前缀
     */
    @Value("${upload.url-prefix:/uploads}")
    private String urlPrefix;

    /**
     * 配置静态资源映射
     * 将 /uploads/** 请求映射到本地文件系统目录
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 配置上传文件的访问路径
        // 请求 URL: /uploads/2026/03/26/xxx.jpg
        // 实际文件: D:/uploads/2026/03/26/xxx.jpg
        registry.addResourceHandler(urlPrefix + "/**")
                .addResourceLocations("file:" + uploadPath + "/");
    }
}
```

#### 方式二：application.yml 配置

在 `application.yml` 中添加：

```yaml
# 文件上传配置
upload:
  # 文件存储路径
  path: D:/uploads
  # 文件访问URL前缀
  url-prefix: /uploads

# Spring MVC 静态资源配置
spring:
  mvc:
    static-path-pattern: /uploads/**
  web:
    resources:
      static-locations: file:${upload.path}/
```

### 3. 前端：改造图片上传组件

修改商家商品发布页面，使用真正的文件上传：

```vue
<template>
  <!-- 商品图片 - 使用 el-upload 组件 -->
  <el-form-item label="商品图片">
    <el-upload
      v-model:file-list="fileList"
      :action="uploadUrl"
      list-type="picture-card"
      :auto-upload="true"
      :on-success="handleUploadSuccess"
      :on-error="handleUploadError"
      :on-remove="handleRemove"
      :on-preview="handlePictureCardPreview"
      :limit="5"
      accept="image/*"
      :headers="uploadHeaders"
    >
      <el-icon><Plus /></el-icon>
    </el-upload>
    <div class="upload-tip">支持jpg、png格式，最多上传5张图片，单张不超过5MB</div>
  </el-form-item>
</template>

<script setup>
import { ref } from 'vue'

// 上传地址
const uploadUrl = 'http://localhost:8080/api/upload/image'
// 请求头（如果需要认证）
const uploadHeaders = {
  // 'Authorization': 'Bearer ' + localStorage.getItem('token')
}

// 文件列表
const fileList = ref([])

// 上传成功回调
const handleUploadSuccess = (response, file) => {
  // response 是后端返回的数据，格式为 { code: 200, data: '/uploads/2026/03/26/xxx.jpg' }
  if (response.code === 200) {
    // 将返回的URL设置到file对象中
    file.url = response.data
  }
}

// 删除图片
const handleRemove = (file, fileList) => {
  // 更新文件列表
}

// 提交表单时获取所有图片URL
const getImageUrls = () => {
  return fileList.value.map(file => file.url).filter(url => url)
}
</script>
```

## 文件结构

```
cursor-backend/
├── src/main/java/com/hengzhou/moli/
│   ├── controller/
│   │   └── FileUploadController.java    # 文件上传接口
│   └── config/
│       └── WebConfig.java              # 静态资源映射配置
├── src/main/resources/
│   └── application.yml                 # 上传路径配置
└── D:/uploads/                         # 文件存储目录
    └── 2026/03/26/
        └── abc123.jpg
```

## 测试步骤

1. 确保 `D:/uploads` 目录存在
2. 重启后端服务
3. 测试上传接口：
   ```bash
   curl -X POST "http://localhost:8080/api/upload/image" \
     -F "file=@/path/to/image.jpg"
   ```
4. 访问上传后的文件：
   ```
   http://localhost:8080/uploads/2026/03/26/abc123.jpg
   ```

## 注意事项

1. **目录权限**：确保应用程序有权限写入 `D:/uploads` 目录
2. **跨域问题**：如果前后端分离部署，需要配置 CORS
3. **文件安全**：建议对上传的文件类型和大小进行严格限制
4. **生产环境**：建议使用对象存储服务（如阿里云OSS、腾讯云COS等）

## 临时解决方案（不推荐用于生产）

如果暂时无法配置后端，可以使用：
- **外部图床**：如阿里云OSS、七牛云等
- **Base64**：适用于小图片，但会增加数据库负担

但这些方案不如本地文件上传可靠和高效。
