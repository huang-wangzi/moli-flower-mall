package com.hengzhou.moli.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hengzhou.moli.entity.PriceMonitor;
import com.hengzhou.moli.entity.Product;
import com.hengzhou.moli.entity.User;
import com.hengzhou.moli.service.PriceMonitorService;
import com.hengzhou.moli.service.ProductService;
import com.hengzhou.moli.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

/**
 * 开发/演示环境：确保测试账号密码正确且价格表有走势数据，便于用户端、商家端、管理端联调。
 */
@Component
@Order(100)
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.seed-data", havingValue = "true", matchIfMissing = true)
public class SeedDataRunner implements ApplicationRunner {

    private static final String MARKET = "横州茉莉花交易市场";

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final PriceMonitorService priceMonitorService;
    private final ProductService productService;

    @Override
    public void run(ApplicationArguments args) {
        // 管理员账号: role=4, status=1
        upsertUser("admin", "admin123", 4, 1, null, null);
        // 普通用户: role=1, status=1
        upsertUser("testuser", "123456", 1, 1, null, null);
        // 商家: role=2, status=2(审核通过)
        upsertUser("testshop", "123456", 2, 2, "茉莉花旗舰店", "fresh");

        // 预置审核通过的测试商品（用户端可立即看到，商家:testshop role=2 status=2）
        seedApprovedProductsIfEmpty();

        seedPriceMonitorIfEmpty();
    }

    private void upsertUser(String username, String rawPassword, int role, int status,
                            String shopName, String shopCategory) {
        User user = userService.getOne(new LambdaQueryWrapper<User>().eq(User::getUsername, username));
        if (user == null) {
            User u = new User();
            u.setUsername(username);
            u.setPassword(passwordEncoder.encode(rawPassword));
            u.setNickname(username);
            u.setRole(role);
            u.setStatus(status);
            u.setMemberLevel("普通会员");
            u.setMemberPoints(0);
            u.setBalance(BigDecimal.ZERO);
            if (shopName != null) {
                u.setShopName(shopName);
                u.setShopCategory(shopCategory);
            }
            userService.save(u);
            return;
        }
        if (!passwordEncoder.matches(rawPassword, user.getPassword())) {
            user.setPassword(passwordEncoder.encode(rawPassword));
            userService.updateById(user);
        }
    }

    private void seedPriceMonitorIfEmpty() {
        LocalDate today = LocalDate.now();
        
        // 检查是否有有效数据（min_price 和 max_price 都有值）
        long validCount = priceMonitorService.list(
            new LambdaQueryWrapper<PriceMonitor>()
                .isNotNull(PriceMonitor::getMinPrice)
                .isNotNull(PriceMonitor::getMaxPrice)
                .ne(PriceMonitor::getMinPrice, BigDecimal.ZERO)
                .ne(PriceMonitor::getMaxPrice, BigDecimal.ZERO)
        ).size();
        
        // 如果有有效数据，跳过初始化
        if (validCount > 0) {
            return;
        }
        
        // 没有有效数据，清空并重新初始化
        priceMonitorService.remove(null);
        
        for (int i = 29; i >= 0; i--) {
            LocalDate d = today.minusDays(i);
            double t = (29 - i) * 0.25;
            BigDecimal p1 = BigDecimal.valueOf(24.5 + Math.sin(t) * 2.2 + i * 0.02).setScale(2, RoundingMode.HALF_UP);
            BigDecimal p2 = BigDecimal.valueOf(135 + Math.sin(t * 0.8) * 12 + i * 0.3).setScale(2, RoundingMode.HALF_UP);
            BigDecimal p3 = BigDecimal.valueOf(38 + Math.cos(t * 0.6) * 4 + i * 0.05).setScale(2, RoundingMode.HALF_UP);
            // p1_max = p1 + 2, p2_max = p2 + 5, p3_max = p3 + 3
            priceMonitorService.save(buildRow(1, p1, p1.add(BigDecimal.valueOf(2)), "元/斤", d));
            priceMonitorService.save(buildRow(2, p2, p2.add(BigDecimal.valueOf(5)), "元/斤", d));
            priceMonitorService.save(buildRow(3, p3, p3.add(BigDecimal.valueOf(3)), "元/盆", d));
        }
    }

    private PriceMonitor buildRow(int category, BigDecimal minPrice, BigDecimal maxPrice, String unit, LocalDate date) {
        PriceMonitor m = new PriceMonitor();
        m.setCategory(category);
        m.setMarket(MARKET);
        m.setMinPrice(minPrice);
        m.setMaxPrice(maxPrice);
        m.setUnit(unit);
        m.setRecordDate(date);
        return m;
    }

    /**
     * 预置已审核通过的测试商品（用户端立即可见）
     * 商家: testshop (role=2, status=2)
     */
    private void seedApprovedProductsIfEmpty() {
        // 只要 product 表有数据就不再重复插入
        if (productService.count() > 0) {
            return;
        }
        User shop = userService.getOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, "testshop")
                .eq(User::getRole, 2));
        if (shop == null) {
            return;
        }
        Long shopId = shop.getId();

        productService.save(buildProduct(shopId, "茉莉花茶 特级",
                1,
                new BigDecimal("128.00"), 500, 100,
                "正宗横州茉莉花茶，产自中国茉莉花之乡"));

        productService.save(buildProduct(shopId, "茉莉花盆栽 四季开花",
                2,
                new BigDecimal("68.00"), 200, 50,
                "室内外观赏花卉，易养活，四季开花"));

        productService.save(buildProduct(shopId, "茉莉花手工香皂 纯天然",
                3,
                new BigDecimal("38.00"), 1000, 200,
                "纯手工制作，天然茉莉花香，洁面护肤"));

        productService.save(buildProduct(shopId, "茉莉花苗 嫁接苗",
                4,
                new BigDecimal("15.00"), 3000, 500,
                "优质嫁接苗，成活率高，适合大面积种植"));
    }

    private Product buildProduct(Long shopId, String name, int categoryId,
                                  BigDecimal price, int stock, int sales,
                                  String description) {
        Product p = new Product();
        p.setShopId(shopId);
        p.setName(name);
        p.setCategoryId((long) categoryId);
        p.setPrice(price);
        p.setStock(stock);
        p.setSales(sales);
        p.setImages("[]");
        p.setDescription(description);
        // 预置商品直接审核通过，用户端可见
        p.setStatus(1);
        return p;
    }
}
