package com.hengzhou.moli.resolver;

import com.hengzhou.moli.entity.User;
import com.hengzhou.moli.mapper.UserMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.core.MethodParameter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

/**
 * 用户ID参数解析器
 * 自动从请求头、SecurityContext、Token中获取当前用户ID
 * 支持 userId 和 shopId 两种参数名（shopId 在收购商场景下就是用户ID）
 */
@Component
@RequiredArgsConstructor
public class UserIdArgumentResolver implements HandlerMethodArgumentResolver {

    private final UserMapper userMapper;

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        // 处理 Long 类型的 userId 或 shopId 参数
        Class<?> type = parameter.getParameterType();
        String name = parameter.getParameterName();
        return (type.equals(Long.class) || type.equals(long.class)) 
                && ("userId".equals(name) || "shopId".equals(name));
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
        HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);
        
        if (request == null) {
            return null;
        }

        String paramName = parameter.getParameterName();
        
        // 1. 优先从请求头获取（前端直接传递 X-User-Id）
        String userIdHeader = request.getHeader("X-User-Id");
        if (StringUtils.hasText(userIdHeader)) {
            try {
                Long userId = Long.parseLong(userIdHeader);
                // 如果是 shopId 参数，需要验证该用户是否为收购商
                if ("shopId".equals(paramName)) {
                    return validateAcquirerAndGetShopId(userId);
                }
                return userId;
            } catch (NumberFormatException ignored) {
            }
        }

        // 2. 从请求属性获取（JWT过滤器设置）
        Object userIdAttr = request.getAttribute("userId");
        if (userIdAttr instanceof Long) {
            return userIdAttr;
        }
        if (userIdAttr instanceof String) {
            try {
                Long userId = Long.parseLong((String) userIdAttr);
                if ("shopId".equals(paramName)) {
                    return validateAcquirerAndGetShopId(userId);
                }
                return userId;
            } catch (NumberFormatException ignored) {
            }
        }

        // 3. 从SecurityContext获取（Spring Security认证）
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() 
                && !"anonymousUser".equals(authentication.getPrincipal())) {
            User user = getUserFromAuthentication(authentication);
            if (user != null) {
                if ("shopId".equals(paramName)) {
                    return validateAcquirerAndGetShopId(user.getId());
                }
                return user.getId();
            }
        }

        // 4. 从Token解析（兜底方案）
        String token = extractToken(request);
        if (StringUtils.hasText(token)) {
            try {
                Long userId = parseTokenUserId(token);
                if (userId != null) {
                    if ("shopId".equals(paramName)) {
                        return validateAcquirerAndGetShopId(userId);
                    }
                    return userId;
                }
            } catch (Exception ignored) {
            }
        }

        return null;
    }

    /**
     * 验证用户是否为收购商，返回收购商ID
     * 收购商场景下，shopId 就是用户的 ID
     */
    private Long validateAcquirerAndGetShopId(Long userId) {
        if (userId == null) {
            return null;
        }
        try {
            User user = userMapper.selectById(userId);
            if (user != null && user.getRole() != null && user.getRole() == 2 
                    && user.getShopType() != null && user.getShopType() == 2) {
                // 是收购商，返回用户ID作为shopId
                return user.getId();
            }
        } catch (Exception e) {
            // 忽略
        }
        return userId; // 如果不是收购商也返回userId，让业务逻辑判断
    }

    /**
     * 从认证信息中获取用户
     */
    private User getUserFromAuthentication(Authentication authentication) {
        try {
            Object principal = authentication.getPrincipal();
            if (principal instanceof User) {
                return (User) principal;
            }
            // 从用户名查询用户
            String username = authentication.getName();
            if (StringUtils.hasText(username) && !username.equals("anonymousUser")) {
                return userMapper.selectOne(
                        new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<User>()
                                .eq(User::getUsername, username));
            }
        } catch (Exception e) {
            // 忽略
        }
        return null;
    }

    /**
     * 从请求中提取Token
     */
    private String extractToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    /**
     * 解析Token获取用户ID
     */
    private Long parseTokenUserId(String token) {
        try {
            // 获取JWT secret
            String jwtSecret = "***JWT_SECRET_REMOVED***==";
            
            javax.crypto.SecretKey key = io.jsonwebtoken.security.Keys.hmacShaKeyFor(
                    jwtSecret.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            
            io.jsonwebtoken.Claims claims = io.jsonwebtoken.Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            
            String userId = claims.getSubject();
            if (StringUtils.hasText(userId)) {
                return Long.parseLong(userId);
            }
        } catch (Exception ignored) {
        }
        return null;
    }
}
