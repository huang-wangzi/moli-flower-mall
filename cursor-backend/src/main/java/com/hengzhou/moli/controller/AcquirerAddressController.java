package com.hengzhou.moli.controller;

import com.hengzhou.moli.common.Result;
import com.hengzhou.moli.entity.AcquirerAddress;
import com.hengzhou.moli.service.AcquirerAddressService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 收购商地址管理接口
 */
@RestController
@RequestMapping("/acquirer/address")
@RequiredArgsConstructor
@Tag(name = "收购商地址管理")
@CrossOrigin(origins = "*")
public class AcquirerAddressController {

    private final AcquirerAddressService addressService;

    @GetMapping("/list")
    @Operation(summary = "获取当前收购商的地址列表")
    public Result<List<AcquirerAddress>> getList(
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        if (userId == null) {
            return Result.error("请先登录");
        }
        return Result.success(addressService.getByUserId(userId));
    }

    @PostMapping("/add")
    @Operation(summary = "新增收购商地址")
    public Result<AcquirerAddress> add(
            @RequestBody AcquirerAddress addr,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        if (userId == null) {
            return Result.error("请先登录");
        }
        addr.setUserId(userId);
        return Result.success(addressService.add(addr));
    }

    @PutMapping("/update")
    @Operation(summary = "更新收购商地址")
    public Result<String> update(
            @RequestBody AcquirerAddress addr,
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
    @Operation(summary = "删除收购商地址")
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
