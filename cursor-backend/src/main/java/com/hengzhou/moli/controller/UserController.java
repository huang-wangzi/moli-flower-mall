package com.hengzhou.moli.controller;

import com.hengzhou.moli.common.Result;
import com.hengzhou.moli.entity.User;
import com.hengzhou.moli.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;

/**
 * 用户个人中心控制器
 * 功能：修改个人信息、注销账号
 * 负责处理用户个人中心的各类请求，包括获取用户信息、更新用户资料、注销账户等操作
 */
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
@Tag(name = "用户个人中心")
@CrossOrigin(origins = "*")
public class UserController {

    /**
     * 用户服务实例
     * 通过构造函数注入，用于处理用户相关的业务逻辑
     */
    private final UserService userService;

    /**
     * 获取当前用户信息
     * 根据用户ID查询用户详细信息，用于个人中心页面展示
     * @param userId 用户ID，从请求参数中获取
     * @return Result<User> 包含用户信息的统一返回结果
     */
    @GetMapping("/info")
    @Operation(summary = "获取当前用户信息")
    public Result<User> getUserInfo(@RequestParam Long userId) {
        // 根据用户ID查询用户信息
        User user = userService.getById(userId);
        // 如果用户不存在，返回错误信息
        if (user == null) {
            return Result.error("用户不存在");
        }
        // 安全考虑：不返回密码字段给前端
        user.setPassword(null);
        // 返回成功结果，包含用户信息
        return Result.success(user);
    }

    /**
     * 更新用户信息（头像、昵称、手机号、邮箱）
     * 支持部分更新，只有传入非空参数才会更新对应字段
     * @param userId 用户ID
     * @param nickname 昵称（可选）
     * @param avatar 头像URL（可选）
     * @param gender 性别：0-保密 1-男 2-女（可选）
     * @param phone 手机号（可选）
     * @param email 邮箱（可选）
     * @return Result<String> 更新操作的结果信息
     */
    @PutMapping("/update")
    @Operation(summary = "更新用户信息")
    public Result<String> updateUserInfo(@RequestParam Long userId,
                                          @RequestParam(required = false) String nickname,
                                          @RequestParam(required = false) String avatar,
                                          @RequestParam(required = false) Integer gender,
                                          @RequestParam(required = false) String phone,
                                          @RequestParam(required = false) String email) {
        // 先查询现有用户信息
        User user = userService.getById(userId);
        // 如果用户不存在，返回错误
        if (user == null) {
            return Result.error("用户不存在");
        }
        // 如果传入了昵称且不为空，则更新昵称（去除首尾空格）
        if (nickname != null && !nickname.isBlank()) {
            user.setNickname(nickname.trim());
        }
        // 如果传入了头像且不为空，则更新头像
        if (avatar != null && !avatar.isBlank()) {
            user.setAvatar(avatar.trim());
        }
        // 如果传入了性别且值合法（0、1、2），则更新性别
        if (gender != null && (gender == 0 || gender == 1 || gender == 2)) {
            user.setGender(gender);
        }
        // 如果传入了手机号且不为空，进行格式验证后更新
        if (phone != null && !phone.isBlank()) {
            // 手机号格式验证：中国大陆手机号（1开头，11位数字）
            if (!phone.matches("^1[3-9]\\d{9}$")) {
                return Result.error("手机号格式不正确");
            }
            user.setPhone(phone.trim());
        }
        // 如果传入了邮箱且不为空，进行格式验证后更新
        if (email != null && !email.isBlank()) {
            // 邮箱格式验证：包含@和域名
            if (!email.matches("^[\\w.-]+@[\\w.-]+\\.\\w+$")) {
                return Result.error("邮箱格式不正确");
            }
            user.setEmail(email.trim());
        }
        // 更新用户信息的修改时间
        user.setUpdateTime(LocalDateTime.now());
        // 执行更新操作并返回结果
        boolean ok = userService.updateUser(user);
        return ok ? Result.success("修改成功") : Result.error("修改失败");
    }

    /**
     * 注销账号（逻辑删除）
     * 将用户的deleted字段设为1，实现软删除，不会真正从数据库删除数据
     * @param userId 要注销的用户ID
     * @return Result<String> 注销操作的结果信息
     */
    @DeleteMapping("/cancel")
    @Operation(summary = "注销账号")
    public Result<String> cancelAccount(@RequestParam Long userId) {
        // 调用服务层执行账号注销操作
        boolean ok = userService.cancelAccount(userId);
        // 如果注销失败（用户不存在），返回错误信息
        if (!ok) {
            return Result.error("注销失败，用户不存在");
        }
        // 注销成功返回提示信息
        return Result.success("账号已注销，所有数据已清除");
    }
}
