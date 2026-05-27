package com.hengzhou.moli.controller;

import com.hengzhou.moli.common.Result;
import com.hengzhou.moli.entity.User;
import com.hengzhou.moli.service.UserService;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * 认证控制器
 * 负责处理用户登录、注册、退出等认证相关的所有HTTP请求
 * 使用JWT（JSON Web Token）实现无状态认证
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AuthController {

    /**
     * 用户服务：处理用户相关的业务逻辑
     */
    private final UserService userService;

    /**
     * 密码加密器：用于密码的BCrypt加密和验证
     */
    private final PasswordEncoder passwordEncoder;

    /**
     * JWT密钥：从配置文件读取，用于签名和验证Token
     */
    @Value("${jwt.secret}")
    private String jwtSecret;

    /**
     * JWT有效期：从配置文件读取，Token过期时间（毫秒）
     */
    @Value("${jwt.expiration}")
    private Long jwtExpiration;

    /**
     * 用户登录
     * 支持多种登录类型：普通用户、商家、收购商、管理员
     * @param loginUser 登录用户信息（包含用户名和密码）
     * @param loginType 登录类型参数（可选）：user/shop/acquirer/admin
     * @return 登录结果，包含JWT Token和用户信息
     */
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody User loginUser,
                                              @RequestParam(required = false) String loginType) {
        // 登录端：优先 URL 参数，其次 JSON 中的 loginType（避免 query 在部分代理/客户端下丢失）
        String lt = loginType != null && !loginType.isBlank() ? loginType.trim() : "user";
        if (loginUser.getLoginType() != null && !loginUser.getLoginType().isBlank()) {
            lt = loginUser.getLoginType().trim();
        }

        // 1. 查找用户：根据用户名查询用户是否存在
        User user = userService.getOne(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<User>()
                .eq(User::getUsername, loginUser.getUsername()));

        // 用户不存在
        if (user == null) {
            return Result.error("用户不存在");
        }

        // 2. 验证密码：使用BCrypt验证输入密码与存储密码是否匹配
        if (!passwordEncoder.matches(loginUser.getPassword(), user.getPassword())) {
            return Result.error("密码错误");
        }

        // 3. 检查状态：管理员端不套用商家审核状态；禁用仍统一拦截
        if (user.getStatus() != null && user.getStatus() == 0) {
            return Result.error("账号已被禁用");
        }
        // 4. 验证角色：根据登录类型验证用户角色是否匹配
        Integer userRole = user.getRole();

        // 管理员登录验证
        if ("admin".equals(lt)) {
            if (userRole == null || userRole != 4) {
                return Result.error("该账号不是管理员账号");
            }
        }
        // 收购商登录验证：role=2 且 shop_type=2
        else if ("acquirer".equals(lt)) {
            // 验证是否为收购商账号
            if (userRole == null || userRole != 2) {
                return Result.error("该账号不是收购商账号");
            }
            if (user.getShopType() == null || user.getShopType() != 2) {
                return Result.error("该账号不是收购商账号");
            }
            // 收购商注册后 status=2（待审核），管理员审核通过后改为 status=1（已审核）
            if (user.getStatus() == null || user.getStatus() == 0) {
                return Result.error("收购商账号已被禁用");
            }
            if (user.getStatus() == 2) {
                return Result.error("收购商账号正在审核中，请耐心等待管理员审核通过后再登录");
            }
        }
        // 普通商家登录验证：role=2 且 shop_type=1 或 null
        else if ("shop".equals(lt)) {
            // 验证是否为商家账号
            if (userRole == null || userRole != 2) {
                return Result.error("该账号不是商家账号");
            }
            // 普通商家不能是收购商类型
            if (user.getShopType() != null && user.getShopType() == 2) {
                return Result.error("该账号是收购商账号，请使用收购商入口登录");
            }
            // 商家注册后 status=2（待审核），管理员审核通过后改为 status=1（已审核）
            if (user.getStatus() == null || user.getStatus() == 0) {
                return Result.error("商家账号已被禁用");
            }
            if (user.getStatus() == 2) {
                return Result.error("商家账号正在审核中，请耐心等待管理员审核通过后再登录");
            }
        }
        // 普通用户登录验证
        else {
            if (userRole == null || userRole != 1) {
                return Result.error("该账号不是用户账号");
            }
        }

        // 5. 生成Token：使用JWT生成访问令牌
        String token = generateToken(user);

        // 6. 构建返回数据：包含Token和用户信息
        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("user", buildUserData(user));

        return Result.success(data);
    }

    /**
     * 用户注册
     * 支持普通用户、商家、收购商等多种角色注册
     * @param user 注册用户信息（包含用户名、密码、角色等）
     * @return 注册结果提示
     */
    @PostMapping("/register")
    public Result<String> register(@RequestBody User user) {
        try {
            System.out.println("[注册] 收到注册请求: " + user.getUsername());

            // 获取注册角色类型，默认为普通用户
            String roleStr = user.getRoleStr();
            if (roleStr == null) {
                roleStr = "user";
            }

            // 调用服务层执行注册
            boolean ok = userService.register(user, roleStr);
            System.out.println("[注册] userService.register 返回: " + ok);

            if (!ok) {
                System.out.println("[注册] 注册失败");
                return Result.error("注册失败，请重试");
            }

            // 根据角色返回不同提示信息
            String message;
            if ("shop".equalsIgnoreCase(roleStr)) {
                message = "注册成功！商家账号需管理员审核通过后，方可登录经营，请耐心等待。";
            } else if ("acquirer".equalsIgnoreCase(roleStr)) {
                message = "注册成功！收购商账号需管理员审核通过后，方可登录发布收购需求，请耐心等待。";
            } else {
                message = "注册成功";
            }

            System.out.println("[注册] 注册成功，返回: " + message);
            return Result.success(message);

        } catch (Exception e) {
            System.out.println("[注册] 注册异常: " + e.getMessage());
            e.printStackTrace();
            String errMsg = e.getMessage();
            if (errMsg == null || errMsg.isBlank()) {
                errMsg = "注册失败，请稍后再试";
            }
            return Result.error(errMsg);
        }
    }

    /**
     * 获取当前登录用户信息
     * 通过Spring Security的Authentication对象获取当前用户
     * @param authentication Spring Security认证信息
     * @return 当前用户信息
     */
    @GetMapping("/info")
    public Result<User> getUserInfo(org.springframework.security.core.Authentication authentication) {
        // 验证用户是否已登录
        if (authentication == null || !authentication.isAuthenticated()) {
            return Result.error("未登录");
        }
        // 从认证信息中获取用户名
        String username = authentication.getName();
        // 查询用户完整信息
        User user = userService.getOne(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<User>()
                .eq(User::getUsername, username));
        return Result.success(user);
    }

    /**
     * 退出登录
     * 前端清除Token后即视为退出，后端无需特殊处理
     * @return 退出成功提示
     */
    @PostMapping("/logout")
    public Result<String> logout() {
        return Result.success("退出成功");
    }

    /**
     * 根据用户ID获取用户信息
     * @param userId 用户ID
     * @return 用户信息
     */
    @GetMapping("/user/{userId}")
    public Result<User> getUserById(@PathVariable Long userId) {
        User user = userService.getById(userId);
        if (user == null) {
            return Result.error("用户不存在");
        }
        return Result.success(user);
    }

    /**
     * 健康检查接口
     * 用于服务监控和负载均衡探活
     * @return 服务状态信息
     */
    @GetMapping("/health")
    public Result<Map<String, Object>> health() {
        Map<String, Object> info = new HashMap<>();
        info.put("status", "ok");
        info.put("timestamp", System.currentTimeMillis());
        info.put("service", "moli-mall");
        return Result.success(info);
    }

    /**
     * 生成JWT Token
     * 使用HMAC-SHA算法签名，包含用户ID、用户名、角色等信息
     * @param user 用户信息
     * @return JWT Token字符串
     */
    private String generateToken(User user) {
        // 使用配置的密钥创建签名Key
        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        // 构建JWT Token
        return Jwts.builder()
                .subject(user.getId().toString())  // Token主题：用户ID
                .claim("username", user.getUsername())  // 附加信息：用户名
                .claim("role", user.getRole())        // 附加信息：角色
                .issuedAt(new Date())                  // 签发时间
                .expiration(new Date(System.currentTimeMillis() + jwtExpiration))  // 过期时间
                .signWith(key)                        // 使用密钥签名
                .compact();
    }

    /**
     * 构建用户返回数据
     * 过滤敏感信息，构建适合前端使用的用户数据结构
     * @param user 用户实体
     * @return 用户数据Map
     */
    private Map<String, Object> buildUserData(User user) {
        Map<String, Object> userData = new HashMap<>();
        // 基本信息
        userData.put("id", user.getId());
        userData.put("username", user.getUsername());
        userData.put("nickname", user.getNickname() != null ? user.getNickname() : user.getUsername());
        // 头像：使用默认头像或用户上传的头像
        userData.put("avatar", user.getAvatar() != null ? user.getAvatar() : "https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png");
        userData.put("gender", user.getGender() != null ? user.getGender() : 0);
        userData.put("phone", user.getPhone());
        userData.put("email", user.getEmail());
        userData.put("balance", user.getBalance());
        userData.put("memberPoints", user.getMemberPoints());
        userData.put("memberLevel", user.getMemberLevel() != null ? user.getMemberLevel() : "普通会员");
        userData.put("createdAt", user.getCreateTime());
        userData.put("role", getFullRoleString(user));
        userData.put("roleNum", user.getRole());
        userData.put("status", user.getStatus());

        // 商家/收购商信息
        if (user.getShopType() != null) {
            userData.put("shopType", user.getShopType());
            userData.put("shopName", user.getShopName());
            userData.put("shopCategory", user.getShopCategory());
            userData.put("shopQualificationStatus", user.getShopQualificationStatus() != null ? user.getShopQualificationStatus() : 0);
        }

        // 收购商专属字段
        if (user.getShopType() != null && user.getShopType() == 2) {
            userData.put("merchantName", user.getMerchantName());
        }

        // 园艺师专属字段
        if (user.getSpecialty() != null) {
            userData.put("specialty", user.getSpecialty());
            userData.put("experience", user.getExperience());
            userData.put("bio", user.getBio());
        }
        return userData;
    }

    /**
     * 获取角色字符串表示
     * @param role 角色数字
     * @return 角色字符串
     */
    private String getRoleString(Integer role) {
        if (role == null) return "user";
        return switch (role) {
            case 1 -> "user";
            case 2 -> "shop";
            case 3 -> "expert";
            case 4 -> "admin";
            default -> "user";
        };
    }

    /**
     * 获取完整角色标识（包含商家类型）
     * 区分普通商家和收购商
     * @param user 用户实体
     * @return 完整角色字符串
     */
    private String getFullRoleString(User user) {
        if (user.getRole() == null) return "user";
        // 收购商：role=2 且 shop_type=2
        if (user.getRole() == 2 && user.getShopType() != null && user.getShopType() == 2) {
            return "acquirer";
        }
        return switch (user.getRole()) {
            case 1 -> "user";
            case 2 -> "shop";
            case 3 -> "expert";
            case 4 -> "admin";
            default -> "user";
        };
    }
}
