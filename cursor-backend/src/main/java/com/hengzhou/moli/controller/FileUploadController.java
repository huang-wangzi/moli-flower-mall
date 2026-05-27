package com.hengzhou.moli.controller;

import com.hengzhou.moli.common.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

/**
 * 文件上传控制器
 * 功能：处理图片等文件的上传
 * 存储位置：本地服务器 uploads 目录
 */
@RestController
@RequestMapping("/upload")
@CrossOrigin
@Tag(name = "文件上传", description = "文件上传相关接口")
public class FileUploadController {

    /**
     * 文件存储根目录
     * 从application.yml中读取配置
     */
    @Value("${upload.path:D:/uploads}")
    private String uploadPath;

    /**
     * 文件访问URL前缀
     * 前端通过此路径访问上传的文件
     */
    @Value("${upload.url-prefix:/uploads}")
    private String urlPrefix;

    /**
     * 允许上传的图片类型
     */
    private static final String[] ALLOWED_IMAGE_TYPES = {
        ".jpg", ".jpeg", ".png", ".gif", ".bmp", ".webp"
    };

    /**
     * 最大文件大小（字节）- 5MB
     */
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;

    /**
     * 上传图片文件
     * 
     * @param file 上传的文件
     * @return 上传成功返回文件访问URL，失败返回错误信息
     */
    @PostMapping("/image")
    @Operation(summary = "上传图片", description = "上传单张图片，返回访问URL")
    public Result<?> uploadImage(@RequestParam("file") MultipartFile file) {
        // 1. 检查文件是否为空
        if (file == null || file.isEmpty()) {
            return Result.error("请选择要上传的文件");
        }

        // 2. 检查文件大小
        if (file.getSize() > MAX_FILE_SIZE) {
            return Result.error("文件大小不能超过5MB");
        }

        // 3. 检查文件类型
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !isAllowedImageType(originalFilename)) {
            return Result.error("不支持的图片格式，仅支持：jpg、png、gif、bmp、webp");
        }

        try {
            // 4. 创建上传目录（按日期分组）
            String datePath = new SimpleDateFormat("yyyy/MM/dd").format(new Date());
            String uploadDir = uploadPath + File.separator + datePath;
            Path dirPath = Paths.get(uploadDir);
            if (!Files.exists(dirPath)) {
                Files.createDirectories(dirPath);
            }

            // 5. 生成唯一文件名（避免文件名冲突）
            String extension = getFileExtension(originalFilename);
            String newFileName = UUID.randomUUID().toString().replace("-", "") + extension;

            // 6. 保存文件
            Path filePath = dirPath.resolve(newFileName);
            file.transferTo(filePath.toFile());

            // 7. 返回完整可访问的URL（包含服务器地址）
            String fileUrl = "/uploads/" + datePath.replace('\\', '/') + "/" + newFileName;
            
            System.out.println("[FileUpload] 文件保存成功: " + filePath.toString());
            System.out.println("[FileUpload] 返回URL: " + fileUrl);

            return Result.<String>success(fileUrl);

        } catch (IOException e) {
            e.printStackTrace();
            return Result.error("文件上传失败：" + e.getMessage());
        }
    }

    /**
     * 检查文件扩展名是否为允许的图片类型
     * 
     * @param filename 文件名
     * @return 是否允许
     */
    private boolean isAllowedImageType(String filename) {
        String lowerFilename = filename.toLowerCase();
        for (String type : ALLOWED_IMAGE_TYPES) {
            if (lowerFilename.endsWith(type)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 获取文件扩展名
     * 
     * @param filename 文件名
     * @return 扩展名（包含点号）
     */
    private String getFileExtension(String filename) {
        int lastIndexOf = filename.lastIndexOf(".");
        if (lastIndexOf == -1) {
            return "";
        }
        return filename.substring(lastIndexOf);
    }

    /**
     * 上传多张图片
     * 
     * @param files 多个文件
     * @return 所有上传成功文件的URL列表
     */
    @PostMapping("/images")
    @Operation(summary = "批量上传图片", description = "一次上传多张图片，返回URL列表")
    public Result<?> uploadImages(@RequestParam("files") MultipartFile[] files) {
        if (files == null || files.length == 0) {
            return Result.error("请选择要上传的文件");
        }

        java.util.List<String> urls = new java.util.ArrayList<>();
        java.util.List<String> errors = new java.util.ArrayList<>();

        for (int i = 0; i < files.length; i++) {
            MultipartFile file = files[i];
            Result<?> result = uploadImage(file);
            
            if (result.getCode() == 200) {
                urls.add((String) result.getData());
            } else {
                errors.add("第" + (i + 1) + "张图片：" + result.getMessage());
            }
        }

        if (!errors.isEmpty() && urls.isEmpty()) {
            return Result.error("上传失败：" + String.join("；", errors));
        }

        return Result.success(urls);
    }
}
