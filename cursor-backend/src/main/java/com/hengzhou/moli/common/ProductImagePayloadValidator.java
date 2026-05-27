package com.hengzhou.moli.common;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.util.StringUtils;

import java.util.Base64;
import java.util.List;
import java.util.regex.Pattern;

/**
 * 校验商品 images 字段：JSON 字符串数组，元素为 data:image/*;base64,... 或 URL/上传路径。
 */
public final class ProductImagePayloadValidator {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    /** 单条商品所有图片 JSON 总长度上限（字符），避免异常大包 */
    private static final int MAX_JSON_LENGTH = 12_000_000;
    private static final int MAX_IMAGES = 5;
    private static final Pattern DATA_IMAGE = Pattern.compile("^data:image/(jpeg|jpg|png);base64,", Pattern.CASE_INSENSITIVE);

    private ProductImagePayloadValidator() {
    }

    public static void validateOrThrow(String imagesJson) {
        if (!StringUtils.hasText(imagesJson)) {
            throw new IllegalArgumentException("请至少上传一张商品图片");
        }
        if (imagesJson.length() > MAX_JSON_LENGTH) {
            throw new IllegalArgumentException("商品图片数据过大，请减少张数或压缩后重试");
        }
        List<String> list;
        try {
            list = MAPPER.readValue(imagesJson.trim(), new TypeReference<>() {
            });
        } catch (Exception e) {
            throw new IllegalArgumentException("商品图片格式无效，应为 JSON 数组");
        }
        if (list == null || list.isEmpty()) {
            throw new IllegalArgumentException("请至少上传一张商品图片");
        }
        if (list.size() > MAX_IMAGES) {
            throw new IllegalArgumentException("最多上传 " + MAX_IMAGES + " 张商品图片");
        }
        for (int i = 0; i < list.size(); i++) {
            validateOne(list.get(i), i + 1);
        }
    }

    private static void validateOne(String raw, int index) {
        if (!StringUtils.hasText(raw)) {
            throw new IllegalArgumentException("第 " + index + " 张图片为空");
        }
        String s = raw.trim();
        if (DATA_IMAGE.matcher(s).find()) {
            int comma = s.indexOf(',');
            if (comma < 0 || comma >= s.length() - 1) {
                throw new IllegalArgumentException("第 " + index + " 张图片 Base64 格式不完整");
            }
            String b64 = s.substring(comma + 1).replaceAll("\\s", "");
            if (b64.isEmpty()) {
                throw new IllegalArgumentException("第 " + index + " 张图片 Base64 为空");
            }
            try {
                Base64.getDecoder().decode(b64);
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("第 " + index + " 张图片 Base64 解码失败，请重新选择图片");
            }
            return;
        }
        if (s.startsWith("http://") || s.startsWith("https://")) {
            return;
        }
        if (s.startsWith("/uploads/") || s.startsWith("/api/") || s.startsWith("/")) {
            return;
        }
        throw new IllegalArgumentException("第 " + index + " 张图片格式不支持（请使用 JPG/PNG 上传或有效图片地址）");
    }
}
