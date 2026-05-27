package com.hengzhou.moli.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hengzhou.moli.entity.User;
import com.hengzhou.moli.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.Collections;

/**
 * 用户Service层
 * 负责处理用户相关的业务逻辑，包括用户认证、注册、信息查询等功能
 * 实现了Spring Security的UserDetailsService接口，用于登录认证
 *
 * @author 横州茉莉花
 */
@Service
@RequiredArgsConstructor
public class UserService extends ServiceImpl<UserMapper, User> implements UserDetailsService {

    /**
     * 密码加密器
     * 用于对用户密码进行加密存储，确保密码安全
     */
    private final PasswordEncoder passwordEncoder;

    /**
     * 根据用户名加载用户信息
     * 这是Spring Security认证流程的核心方法，用于验证用户登录
     * @param username 用户名
     * @return UserDetails Spring Security标准的用户详情对象
     * @throws UsernameNotFoundException 当用户名不存在时抛出
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 使用MyBatis-Plus的条件构造器查询用户
        User user = this.getOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, username));

        // 如果用户不存在，抛出用户名未找到异常
        if (user == null) {
            throw new UsernameNotFoundException("用户不存在: " + username);
        }

        // 构建Spring Security的UserDetails对象
        // 参数依次：用户名、密码、账户是否启用、账户是否过期、凭证是否过期、账户是否锁定、权限列表
        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                user.getStatus() == 1,  // 状态1为正常，转换为enabled
                true, true, true,       // 账户未过期、凭证未过期、账户未锁定
                // 将角色数字转换为Spring Security需要的权限格式（ROLE_xxx）
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + getRoleName(user.getRole())))
        );
    }

    /**
     * 根据角色获取角色名称
     * 将数字角色转换为Spring Security的权限标识符
     * @param role 角色数字：1-普通用户，2-商家，3-园艺师，4-管理员
     * @return 角色名称字符串，用于权限标识
     */
    private String getRoleName(Integer role) {
        return switch (role) {
            case 1 -> "USER";      // 普通用户角色
            case 2 -> "SHOP";       // 商家角色
            case 3 -> "EXPERT";     // 园艺师角色
            case 4 -> "ADMIN";      // 管理员角色
            default -> "USER";      // 默认为普通用户
        };
    }

    /**
     * 分页查询用户
     * 支持按关键字（用户名或手机号）搜索和按角色筛选
     * @param pageNum 页码，从1开始
     * @param pageSize 每页记录数
     * @param keyword 搜索关键字（匹配用户名或手机号）
     * @param role 用户角色筛选条件
     * @return 分页后的用户列表
     */
    public Page<User> getUserPage(Integer pageNum, Integer pageSize, String keyword, Integer role) {
        // 创建分页对象，指定页码和每页大小
        Page<User> page = new Page<>(pageNum, pageSize);

        // 构建查询条件
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        // 只查询未删除的用户（软删除标志为0）
        wrapper.eq(User::getDeleted, 0);

        // 如果指定了角色筛选条件
        if (role != null) {
            wrapper.eq(User::getRole, role);
        }

        // 如果有关键字搜索条件
        if (StringUtils.hasText(keyword)) {
            // 使用and连接多个or条件：用户名模糊匹配 OR 手机号模糊匹配
            wrapper.and(w -> w.like(User::getUsername, keyword)
                    .or().like(User::getPhone, keyword));
        }

        // 按创建时间倒序排列，最新注册的用户排在前面
        wrapper.orderByDesc(User::getCreateTime);
        return this.page(page, wrapper);
    }

    /**
     * 分页查询商家（支持按状态筛选）
     * 用于管理后台的商家列表管理
     * @param pageNum 页码
     * @param pageSize 每页记录数
     * @param status 商家状态：0-待审核，1-通过，2-拒绝
     * @return 分页后的商家用户列表
     */
    public Page<User> getShopPage(Integer pageNum, Integer pageSize, Integer status) {
        Page<User> page = new Page<>(pageNum, pageSize);

        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        // 排除已删除的用户
        wrapper.eq(User::getDeleted, 0);
        // 筛选商家角色的用户（role=2）
        wrapper.eq(User::getRole, 2);

        // 如果指定了状态筛选条件
        if (status != null) {
            wrapper.eq(User::getStatus, status);
        }

        // 按创建时间倒序排列
        wrapper.orderByDesc(User::getCreateTime);
        return this.page(page, wrapper);
    }

    /**
     * 注册用户
     * 支持普通用户、商家、收购商、管理员等多种角色注册
     * @param user 用户信息（包含用户名、密码等）
     * @param roleStr 角色字符串：user/shop/acquirer/admin
     * @return 注册是否成功
     */
    public boolean register(User user, String roleStr) {
        // 检查用户名是否已存在
        long count = this.count(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, user.getUsername()));
        if (count > 0) {
            throw new RuntimeException("用户名已存在");
        }

        // 对密码进行BCrypt加密存储，确保安全性
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        // 根据角色字符串设置数字角色和商家类型
        Integer role = 1; // 默认为普通用户
        Integer shopType = null; // 商家类型
        if ("shop".equalsIgnoreCase(roleStr)) {
            role = 2; // 商家
            shopType = 1; // 普通商家
        } else if ("acquirer".equalsIgnoreCase(roleStr)) {
            role = 2; // 收购商也是商家角色（在用户表中）
            shopType = 2; // 收购商
        } else if ("admin".equalsIgnoreCase(roleStr)) {
            role = 4; // 管理员
        }
        user.setRole(role);
        user.setShopType(shopType);

        // 普通用户注册后直接生效，商家和收购商需要管理员审核
        if ("user".equalsIgnoreCase(roleStr)) {
            user.setStatus(1); // 普通用户注册后直接正常
        } else {
            user.setStatus(2); // 商家和收购商需要审核
        }

        // 初始化用户其他字段
        user.setMemberLevel("普通会员");  // 默认会员等级
        user.setMemberPoints(0);          // 初始积分为0
        user.setBalance(new BigDecimal("0")); // 初始余额为0

        // 保存用户到数据库
        return this.save(user);
    }

    /**
     * 注册用户（兼容旧调用）
     * 当不指定角色时，默认为普通用户
     * @param user 用户信息
     * @return 注册是否成功
     */
    public boolean register(User user) {
        return register(user, "user");
    }

    /**
     * 根据ID获取用户信息
     * @param id 用户ID
     * @return 用户实体对象
     */
    public User getUserById(Long id) {
        return this.getById(id);
    }

    /**
     * 更新用户信息
     * 使用MyBatis-Plus的updateById方法进行更新
     * @param user 包含更新后用户信息的对象
     * @return 更新是否成功
     */
    public boolean updateUser(User user) {
        return this.updateById(user);
    }

    /**
     * 注销账号（软删除）
     * 将用户的deleted字段设为1，而不是真正从数据库删除数据
     * 这样可以保留用户数据用于审计和分析
     * @param userId 要注销的用户ID
     * @return 注销是否成功
     */
    public boolean cancelAccount(Long userId) {
        // 先查询用户是否存在
        User user = this.getById(userId);
        if (user == null) return false;
        // 设置软删除标志为1
        user.setDeleted(1);
        return this.updateById(user);
    }
}
