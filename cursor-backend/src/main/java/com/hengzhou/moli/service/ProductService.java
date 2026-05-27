package com.hengzhou.moli.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hengzhou.moli.common.ProductImagePayloadValidator;
import com.hengzhou.moli.entity.Product;
import com.hengzhou.moli.entity.User;
import com.hengzhou.moli.mapper.ProductMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 商品Service层
 * 
 * @author 横州茉莉花
 */
@Service
@RequiredArgsConstructor
public class ProductService extends ServiceImpl<ProductMapper, Product> {

    private final UserService userService;
    private final MessageService messageService;

    /**
     * 分页查询商品列表（用户端仅返回已审核通过的商品）
     */
    public Page<Product> getProductPage(Integer pageNum, Integer pageSize, Long categoryId,
                                       String keyword, Integer status) {
        Page<Product> page = new Page<>(pageNum, pageSize);

        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Product::getDeleted, 0);

        // 用户端只显示已审核通过(status=1)的商品
        if (status == null) {
            wrapper.eq(Product::getStatus, 1);
        }

        if (categoryId != null) {
            wrapper.eq(Product::getCategoryId, categoryId);
        }
        if (StringUtils.hasText(keyword)) {
            wrapper.like(Product::getName, keyword);
        }
        if (status != null) {
            wrapper.eq(Product::getStatus, status);
        }

        wrapper.orderByDesc(Product::getCreateTime);

        return this.page(page, wrapper);
    }

    /**
     * 获取商品详情（通用，可指定是否为用户端）
     * @param id 商品ID
     * @param isUserClient 是否为用户端（用户端只能看到上架商品）
     */
    public Product getProductDetail(Long id, boolean isUserClient) {
        Product product = this.getOne(new LambdaQueryWrapper<Product>()
                .eq(Product::getId, id)
                .eq(Product::getDeleted, 0));
        
        // 如果是用户端，只显示已审核通过(status=1)的商品
        if (isUserClient && product != null && product.getStatus() != 1) {
            return null; // 商品未审核或已下架，返回null
        }
        return product;
    }

    /**
     * 获取商品详情（旧接口兼容）
     */
    public Product getProductDetail(Long id) {
        return getProductDetail(id, true);
    }
    
    /**
     * 获取商品详情（通用，不受状态限制）
     */
    public Product getProductDetailForAdmin(Long id) {
        return this.getOne(new LambdaQueryWrapper<Product>()
                .eq(Product::getId, id)
                .eq(Product::getDeleted, 0));
    }
    
    /**
     * 根据商家ID获取商品列表
     */
    public List<Product> getProductsByShopId(Long shopId) {
        return this.list(new LambdaQueryWrapper<Product>()
                .eq(Product::getShopId, shopId)
                .eq(Product::getDeleted, 0)
                .orderByDesc(Product::getCreateTime));
    }
    
    /**
     * 获取商家自己的商品列表（包含所有状态）
     */
    public List<Product> getProductsByShopIdForShop(Long shopId) {
        return this.list(new LambdaQueryWrapper<Product>()
                .eq(Product::getShopId, shopId)
                .eq(Product::getDeleted, 0)
                .orderByDesc(Product::getCreateTime));
    }

    /**
     * 获取热门商品（仅已上架商品）
     */
    public List<Product> getHotProducts(Integer limit) {
        return this.list(new LambdaQueryWrapper<Product>()
                .eq(Product::getStatus, 1)
                .eq(Product::getDeleted, 0)
                .orderByDesc(Product::getSales)
                .last("LIMIT " + limit));
    }

    /**
     * 新增商品
     * @param product 商品信息
     * @param userId 当前登录用户ID
     * @return 是否成功
     */
    public boolean addProduct(Product product, Long userId) {
        System.out.println("=== ProductService.addProduct 调用 ===");
        System.out.println("传入 userId: " + userId);
        System.out.println("product.getName(): " + product.getName());
        System.out.println("product.getShopId() 传入前: " + product.getShopId());

        // 检查商家是否已审核通过
        User shopUser = userService.getById(userId);
        if (shopUser == null || shopUser.getRole() != 2) {
            throw new RuntimeException("只有商家可以添加商品");
        }
        System.out.println("商家用户: " + shopUser.getUsername() + ", 状态: " + shopUser.getStatus());

        // 商家入驻审核通过后才能添加商品 (status=1)
        if (shopUser.getStatus() == null || shopUser.getStatus() != 1) {
            throw new RuntimeException("商家入驻审核通过后才能添加商品");
        }

        // 商家需要资质审核通过后才能添加商品 (shopQualificationStatus=2)
        // 收购商不需要资质审核
        if (shopUser.getShopType() == null || shopUser.getShopType() != 2) {
            if (shopUser.getShopQualificationStatus() == null || shopUser.getShopQualificationStatus() != 2) {
                throw new RuntimeException("请先提交资质材料，管理员审核通过后再添加商品");
            }
        }

        product.setShopId(userId);
        product.setSales(0);
        // 新增商品默认待审核状态
        product.setStatus(0);
        ProductImagePayloadValidator.validateOrThrow(product.getImages());

        System.out.println("product.setShopId 后: " + product.getShopId());
        boolean saved = this.save(product);
        System.out.println("商品保存结果: " + saved + ", 保存后ID: " + product.getId());
        return saved;
    }

    /**
     * 更新商品（商家可编辑自己已上架或下架的商品）
     * 编辑后状态改为待审核(0)，需管理员重新审核
     */
    public boolean updateProduct(Product incoming, Long operatorUserId) {
        if (incoming == null || incoming.getId() == null) {
            return false;
        }
        if (operatorUserId == null) {
            throw new IllegalArgumentException("请先登录后再操作");
        }

        Product existing = this.getById(incoming.getId());
        if (existing == null || (existing.getDeleted() != null && existing.getDeleted() == 1)) {
            return false;
        }
        if (!operatorUserId.equals(existing.getShopId())) {
            throw new IllegalArgumentException("无权修改该商品");
        }

        // 商家可以编辑已上架(status=1)或已下架(status=2)的商品
        // 编辑后状态改为待审核(0)，需管理员重新审核
        if (existing.getStatus() != 0 && existing.getStatus() != 1 && existing.getStatus() != 2) {
            throw new IllegalArgumentException("该商品状态不支持编辑");
        }

        if (incoming.getName() != null) {
            existing.setName(incoming.getName());
        }
        if (incoming.getCategoryId() != null) {
            existing.setCategoryId(incoming.getCategoryId());
        }
        if (incoming.getPrice() != null) {
            existing.setPrice(incoming.getPrice());
        }
        if (incoming.getStock() != null) {
            existing.setStock(incoming.getStock());
        }
        if (incoming.getDescription() != null) {
            existing.setDescription(incoming.getDescription());
        }
        if (incoming.getOriginalPrice() != null) {
            existing.setOriginalPrice(incoming.getOriginalPrice());
        }
        if (incoming.getSpecs() != null) {
            existing.setSpecs(incoming.getSpecs());
        }
        if (StringUtils.hasText(incoming.getImages())) {
            ProductImagePayloadValidator.validateOrThrow(incoming.getImages());
            existing.setImages(incoming.getImages());
        }
        // 编辑后状态改为待审核，等待管理员审核
        existing.setStatus(0);
        existing.setUpdateTime(LocalDateTime.now());
        return this.updateById(existing);
    }

    /**
     * 上架/下架商品（商家自主操作）
     * 商家只能操作自己的商品
     * 商家可以自主下架(status=2)商品，无需审核
     * 商家不能自主上架未审核(status=0)的商品，必须等管理员审核通过
     */
    public boolean updateProductStatus(Long id, Integer status, Long operatorUserId) {
        Product existing = this.getById(id);
        if (existing == null || (existing.getDeleted() != null && existing.getDeleted() == 1)) {
            return false;
        }

        // 商家只能操作自己的商品
        if (operatorUserId != null && !operatorUserId.equals(existing.getShopId())) {
            throw new IllegalArgumentException("无权操作该商品");
        }

        // 商家自主下架无需审核
        if (status == 2 && existing.getStatus() == 1) {
            Product product = new Product();
            product.setId(id);
            product.setStatus(2);
            return this.updateById(product);
        }

        // 【重要】商家不能自主上架未审核(status=0)的商品
        // 只有管理员审核通过后(status=1)，商家才能自主下架
        if (status == 1 && existing.getStatus() == 0) {
            throw new IllegalArgumentException("商品需经管理员审核通过后才能上架");
        }

        Product product = new Product();
        product.setId(id);
        product.setStatus(status);
        return this.updateById(product);
    }
    
    /**
     * 上架/下架商品（兼容旧接口，不检查操作人）
     */
    public boolean updateProductStatus(Long id, Integer status) {
        Product product = new Product();
        product.setId(id);
        product.setStatus(status);
        return this.updateById(product);
    }

    /**
     * 管理员审核商品通过
     */
    public boolean approveProduct(Long id) {
        Product product = new Product();
        product.setId(id);
        product.setStatus(1); // 审核通过，在售
        return this.updateById(product);
    }

    /**
     * 管理员审核商品拒绝
     */
    public boolean rejectProduct(Long id) {
        Product product = new Product();
        product.setId(id);
        product.setStatus(2); // 审核拒绝
        return this.updateById(product);
    }

    /**
     * 更新库存
     */
    public boolean updateStock(Long productId, Integer quantity) {
        Product product = this.getById(productId);
        if (product == null) {
            return false;
        }

        int newStock = product.getStock() - quantity;
        if (newStock < 0) {
            return false;
        }

        product.setStock(newStock);
        product.setSales(product.getSales() + quantity);
        return this.updateById(product);
    }

    /**
     * 删除商品（物理删除，直接从数据库删除）
     * 商家只能删除自己的商品
     * @param productId 商品ID
     * @param operatorUserId 操作用户ID
     * @return 是否删除成功
     */
    public boolean deleteProduct(Long productId, Long operatorUserId) {
        Product product = this.getById(productId);
        if (product == null) {
            return false;
        }
        // 商家只能删除自己的商品
        if (operatorUserId != null && !operatorUserId.equals(product.getShopId())) {
            throw new IllegalArgumentException("无权删除该商品");
        }
        // 物理删除（绕过 MyBatis Plus 的逻辑删除，彻底从数据库删除）
        return baseMapper.deleteByIdPhysical(productId) > 0;
    }
}
