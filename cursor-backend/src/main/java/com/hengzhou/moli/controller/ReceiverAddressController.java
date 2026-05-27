package com.hengzhou.moli.controller;

import com.hengzhou.moli.common.Result;
import com.hengzhou.moli.entity.ReceiverAddress;
import com.hengzhou.moli.service.ReceiverAddressService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 收货地址管理接口
 */
@RestController
@RequestMapping("/address")
@RequiredArgsConstructor
@Tag(name = "收货地址")
@CrossOrigin(origins = "*")
public class ReceiverAddressController {

    private final ReceiverAddressService addressService;

    @GetMapping("/list")
    @Operation(summary = "获取当前用户的收货地址列表")
    public Result<List<ReceiverAddress>> getList(
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        if (userId == null) {
            return Result.error("请先登录");
        }
        return Result.success(addressService.getByUserId(userId));
    }

    @PostMapping("/add")
    @Operation(summary = "新增收货地址")
    public Result<ReceiverAddress> add(
            @RequestBody ReceiverAddress addr,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        if (userId == null) {
            return Result.error("请先登录");
        }
        addr.setUserId(userId);
        return Result.success(addressService.add(addr));
    }

    @PutMapping("/update")
    @Operation(summary = "更新收货地址")
    public Result<String> update(
            @RequestBody ReceiverAddress addr,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        if (userId == null) {
            return Result.error("请先登录");
        }
        addr.setUserId(userId);
        if (addressService.update(addr)) {
            return Result.success("更新成功");
        }
        return Result.error("更新失败，地址不存在或无权限");
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除收货地址")
    public Result<String> remove(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        if (userId == null) {
            return Result.error("请先登录");
        }
        if (addressService.remove(id, userId)) {
            return Result.success("删除成功");
        }
        return Result.error("删除失败，地址不存在或无权限");
    }

    @PutMapping("/default/{id}")
    @Operation(summary = "设为默认地址")
    public Result<String> setDefault(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        if (userId == null) {
            return Result.error("请先登录");
        }
        if (addressService.setDefault(id, userId)) {
            return Result.success("设置成功");
        }
        return Result.error("设置失败，地址不存在或无权限");
    }
}
