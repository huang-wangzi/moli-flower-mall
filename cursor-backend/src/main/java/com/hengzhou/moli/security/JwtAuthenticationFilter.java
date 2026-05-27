package com.hengzhou.moli.security;

import com.hengzhou.moli.entity.User;
import com.hengzhou.moli.mapper.UserMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final UserMapper userMapper;

    @Value("${jwt.secret}")
    private String jwtSecret;

    // 公开路径列表 - 所有需要免登录访问的接口
    private static final List<String> PUBLIC_PATHS = Arrays.asList(
            // 认证相关（无需Token）
            "/auth/login",
            "/auth/register",
            "/auth/logout",
            "/auth/health",
            "/auth/info",
            "/auth/send-code",
            "/auth/reset-password",

            // 商品相关（用户端浏览）
            "/api/product/list",
            "/api/product/hot",
            "/api/product/category",
            "/product/list",
            "/product/hot",
            "/product/category",

            // 收购需求（用户端浏览）
            // 收购模块公开接口（用户端浏览）
            "/api/acquisition/demands",
            "/api/acquisition/demands/active",
            "/api/acquisition/demand/",
            "/acquisition/demands",
            "/acquisition/demand/",
            // 注意：收购商管理接口（/api/acquisition/merchant/**）需要登录认证，
            // 已从公开路径中移除，由 SecurityConfig 处理

            // 分类
            "/api/category",
            "/api/category/list",
            "/category",
            "/category/list",

            // 市场行情
            "/api/price/market",
            "/api/price/monitor",
            "/api/price/latest",
            "/price/market",
            "/price/monitor",
            "/price/latest",

            // 天气预报（无需Token）
            "/api/weather",
            "/weather",

            // 首页内容
            "/api/home",
            "/home",

            // Swagger/健康检查
            "/swagger",
            "/v3/api-docs",
            "/actuator"
            // 注意：/shop/qualification/my 等商家资质接口需要登录认证，已从公开路径中移除
    );

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String uri = request.getRequestURI();
        String method = request.getMethod();

        try {
            System.out.println("[JWT] 正在处理请求: " + uri + " [" + method + "]");

            // 获取请求头中的 Authorization
            String authHeader = request.getHeader("Authorization");
            String token = null;
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                token = authHeader.substring(7);
            }
            
            // 如果没有 Token 且是公开路径，则直接放行
            if (!StringUtils.hasText(token) && isPublicPath(uri, method)) {
                System.out.println("[JWT] 公开路径且无Token，直接放行: " + uri);
                filterChain.doFilter(request, response);
                return;
            }

            // 如果有 Token，则尝试验证（无论是否为公开路径）
            if (StringUtils.hasText(token)) {
                try {
                    // 解析JWT Token
                    SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
                    Claims claims = Jwts.parser()
                            .verifyWith(key)
                            .build()
                            .parseSignedClaims(token)
                            .getPayload();

                    String userId = claims.getSubject();
                    System.out.println("[JWT] Token解析成功, userId: " + userId);

                    if (StringUtils.hasText(userId)) {
                        User user = userMapper.selectById(Long.parseLong(userId));
                        System.out.println("[JWT] 查询用户: " + user);

                        // 验证用户存在且状态正常
                        // status: 0=禁用, 1=正常, 2=待审核
                        if (user != null && user.getStatus() != null && user.getStatus() != 0) {
                            String roleName = getRoleName(user.getRole(), user.getShopType());
                            System.out.println("[JWT] 用户状态正常，roleName: " + roleName);
                            
                            UsernamePasswordAuthenticationToken authentication =
                                    new UsernamePasswordAuthenticationToken(
                                            user.getUsername(),
                                            null,
                                            Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + roleName))
                                    );

                            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                            SecurityContextHolder.getContext().setAuthentication(authentication);

                            // 将用户ID设置到请求属性中，供控制器和参数解析器使用
                            request.setAttribute("userId", user.getId());
                            request.setAttribute("userRole", user.getRole());
                            request.setAttribute("shopType", user.getShopType());
                            
                            System.out.println("[JWT] 认证成功: userId=" + user.getId() + ", role=" + roleName);
                        } else {
                            System.out.println("[JWT] 用户不存在或状态异常: " + (user == null ? "user=null" : "status=" + user.getStatus()));
                        }
                    }
                } catch (Exception e) {
                    // JWT解析失败，记录日志但不阻止请求
                    // 让请求继续，后续Spring Security会处理未认证的情况
                    System.out.println("[JWT] Token解析失败: " + e.getMessage());
                    log.debug("[JWT] Token解析失败: {}", e.getMessage());
                }
            } else {
                System.out.println("[JWT] Token为空，无法认证");
            }
            
            // 继续过滤器链
            filterChain.doFilter(request, response);
            
        } catch (Exception e) {
            log.error("[JWT] Filter处理异常: {}", e.getMessage(), e);
            try {
                filterChain.doFilter(request, response);
            } catch (Exception ex) {
                log.error("[JWT] FilterChain执行异常", ex);
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"code\":500,\"message\":\"服务器内部错误\"}");
            }
        }
    }

    private boolean isPublicPath(String uri, String method) {
        if (uri == null) return false;
        
        // 只有 GET 请求可能是公开的商品浏览接口
        boolean isGet = "GET".equalsIgnoreCase(method);
        
        // 精确匹配公开路径列表
        for (String path : PUBLIC_PATHS) {
            if (uri.equals(path)) {
                // 如果是商品相关的公开路径，必须是 GET 请求
                if (path.contains("/product/") || path.contains("/category") || 
                    path.contains("/price/") || path.contains("/weather") || 
                    path.contains("/home")) {
                    return isGet;
                }
                return true;
            }
        }
        
        // 特殊处理：商品详情页 /product/{id} 或 /api/product/{id}（数字ID）
        // 只有 GET 请求才是公开的
        if (isGet && (uri.matches("^/product/\\d+$") || uri.matches("^/api/product/\\d+$"))) {
            return true;
        }
        
        // 特殊处理：商品浏览列表 /product/list, /product/hot 等
        if (isGet && (uri.equals("/product/list") || uri.equals("/product/hot") || 
            uri.equals("/api/product/list") || uri.equals("/api/product/hot"))) {
            return true;
        }
        
        return false;
    }

    private String getTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    /**
     * 获取角色名称
     * @param role 角色值: 1=用户, 2=商家(含收购商), 3=园艺师, 4=管理员
     * @param shopType 商家类型: null=普通用户, 1=普通商家, 2=收购商
     */
    private String getRoleName(Integer role, Integer shopType) {
        if (role == null) return "USER";
        
        // 如果是商家角色，检查是否是收购商
        if (role == 2 && shopType != null && shopType == 2) {
            return "ACQUIRER";
        }
        
        return switch (role) {
            case 1 -> "USER";
            case 2 -> "SHOP";
            case 3 -> "EXPERT";
            case 4 -> "ADMIN";
            default -> "USER";
        };
    }
}
