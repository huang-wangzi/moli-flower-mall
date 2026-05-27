package com.hengzhou.moli.config;

import com.hengzhou.moli.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        System.out.println("[Security] 初始化 SecurityFilterChain...");
        
        http
            .csrf(AbstractHttpConfigurer::disable)
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(registry -> {
                System.out.println("[Security] 配置请求授权规则...");
                
                // 公开接口
                registry.requestMatchers("/auth/**", "/api/auth/**").permitAll();
                registry.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll();
                
                // 商品浏览（所有人可访问）
                registry.requestMatchers(HttpMethod.GET, "/product/list", "/product/hot", "/product/category/**",
                        "/api/product/list", "/api/product/hot", "/api/product/category/**").permitAll();
                registry.requestMatchers(HttpMethod.GET, "/product/*/detail", "/product/{id}",
                        "/api/product/*/detail", "/api/product/{id}").permitAll();
                
                // 商家商品管理接口
                registry.requestMatchers(HttpMethod.GET, "/product/shop/{shopId}",
                        "/api/product/shop/{shopId}").authenticated();
                registry.requestMatchers(HttpMethod.POST, "/product", "/api/product").authenticated();
                registry.requestMatchers(HttpMethod.PUT, "/product", "/api/product").authenticated();
                registry.requestMatchers(HttpMethod.DELETE, "/product/**", "/api/product/**").authenticated();
                
                // 收购需求浏览（所有人可看）
                registry.requestMatchers(HttpMethod.GET, "/api/acquisition/demands", "/api/acquisition/demands/**",
                        "/api/acquisition/demand/*", "/api/acquisition/demand/{id}",
                        "/api/acquisition/demands/active").permitAll();
                registry.requestMatchers(HttpMethod.GET, "/acquisition/demands", "/acquisition/demands/**",
                        "/acquisition/demand/*", "/acquisition/demand/{id}",
                        "/acquisition/demands/active").permitAll();
                
                // 收购商功能
                registry.requestMatchers("/api/acquisition/merchant/**").authenticated();
                registry.requestMatchers("/acquisition/merchant/**").authenticated();
                
                // 收购申请接口（用户端）
                registry.requestMatchers("/api/acquisition/application/**").authenticated();
                registry.requestMatchers("/acquisition/application/**").authenticated();
                
                // 收购订单接口
                registry.requestMatchers("/api/acquisition/order/**").authenticated();
                registry.requestMatchers("/acquisition/order/**").authenticated();
                
                // 收购收入接口
                registry.requestMatchers("/api/acquisition/income/**").authenticated();
                registry.requestMatchers("/acquisition/income/**").authenticated();
                
                // 管理员收购管理接口
                registry.requestMatchers("/api/acquisition/admin/**").hasAuthority("ROLE_ADMIN");
                registry.requestMatchers("/acquisition/admin/**").hasAuthority("ROLE_ADMIN");
                
                // 分类
                registry.requestMatchers("/api/category", "/api/category/**").permitAll();
                registry.requestMatchers("/category", "/category/**").permitAll();
                
                // 市场行情
                registry.requestMatchers("/api/price/market", "/api/price/monitor", "/api/price/latest").permitAll();
                registry.requestMatchers("/price/market", "/price/monitor", "/price/latest").permitAll();
                
                // 天气预报
                registry.requestMatchers("/api/weather/**", "/weather/**").permitAll();
                
                // 首页内容
                registry.requestMatchers("/api/home", "/home/**").permitAll();
                
                // 文件上传下载
                registry.requestMatchers("/uploads/**", "/upload/**").permitAll();
                
                // 消息接口
                registry.requestMatchers("/message/**", "/api/message/**").authenticated();
                
                // 商家资质接口 - 商家查看自己的资质（需登录）
                registry.requestMatchers("/shop/qualification/my", "/api/shop/qualification/my").authenticated();
                // 商家查看待审核列表（需登录）
                registry.requestMatchers("/shop/qualification/pending", "/api/shop/qualification/pending").authenticated();
                // 商家提交资质申请（需登录）
                registry.requestMatchers(HttpMethod.POST, "/shop/qualification/apply", "/api/shop/qualification/apply").authenticated();
                // 管理员查看所有资质
                registry.requestMatchers("/shop/qualification/all", "/api/shop/qualification/all").hasRole("ADMIN");
                
                // 用户、购物车、订单、评价、聊天
                registry.requestMatchers("/api/user/**").authenticated();
                registry.requestMatchers("/api/cart/**").authenticated();
                registry.requestMatchers("/api/order/**").authenticated();
                registry.requestMatchers("/api/review/**").authenticated();
                registry.requestMatchers("/api/chat/**").authenticated();
                
                // 商家功能接口
                registry.requestMatchers("/api/shop/**").authenticated();
                registry.requestMatchers("/api/merchant/**").authenticated();
                registry.requestMatchers("/shop/**", "/merchant/**").authenticated();
                
                // 管理员接口
                registry.requestMatchers("/api/admin/**").hasRole("ADMIN");
                registry.requestMatchers("/admin/**").hasRole("ADMIN");
                
                // Swagger
                registry.requestMatchers("/swagger/**", "/v3/api-docs/**", "/actuator/**").permitAll();
                
                // 其他所有接口都需要登录
                registry.anyRequest().authenticated();
            })
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            .formLogin(AbstractHttpConfigurer::disable)
            .httpBasic(AbstractHttpConfigurer::disable);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(List.of("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setExposedHeaders(Arrays.asList("Authorization", "X-User-Id", "X-Shop-Id"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
