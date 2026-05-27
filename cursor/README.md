# 横州茉莉花商城 - 基于Vue 3 + Element Plus的电商系统

## 项目简介

这是一个基于Vue 3 + Element Plus的横州茉莉花电商交易与价格监测系统（用户端），采用拟态UI设计风格，包含完整的电商功能模块。

## 技术栈

- **前端框架**: Vue 3 (Composition API)
- **UI组件库**: Element Plus
- **路由管理**: Vue Router
- **状态管理**: Pinia
- **图表库**: ECharts (vue-echarts)
- **HTTP客户端**: Axios
- **构建工具**: Vite

## 核心功能

### 1. 首页 - 价格监测系统
- 展示茉莉鲜花、花茶、文创产品三类商品的价格
- 实时价格涨跌显示（按日/周/月）
- 点击可跳转至价格详情页
- 价格走势图表可视化

### 2. 商品模块
- 商品列表（支持分类筛选、销量/价格排序）
- 商品详情页（大图预览、规格选择、加入购物车）
- 用户评价展示
- 退换货申请功能

### 3. 消息中心
- 物流助手通知
- 优惠活动通知
- 聊天记录
- 未读消息小红点标记

### 4. 个人中心
- 用户信息查看/修改
- 收货地址管理（新增/编辑/删除/默认地址）
- 订单管理（待付款/待收货/已完成/售后）
- 优惠券查看
- 浏览历史
- 评价管理

## 项目结构

```
├── src/
│   ├── assets/          # 静态资源
│   ├── components/      # 公共组件
│   ├── data/            # 模拟数据
│   │   ├── products.js  # 商品数据
│   │   ├── prices.js    # 价格数据
│   │   ├── addresses.js # 地址数据
│   │   ├── coupons.js   # 优惠券数据
│   │   └── chats.js     # 聊天记录数据
│   ├── router/          # 路由配置
│   ├── stores/          # Pinia状态管理
│   │   ├── user.js      # 用户状态
│   │   ├── cart.js      # 购物车状态
│   │   ├── message.js   # 消息状态
│   │   └── order.js     # 订单状态
│   ├── styles/          # 全局样式
│   ├── views/           # 页面组件
│   │   ├── Home.vue             # 首页
│   │   ├── PriceDetail.vue      # 价格详情
│   │   ├── ProductList.vue      # 商品列表
│   │   ├── ProductDetail.vue    # 商品详情
│   │   ├── Cart.vue             # 购物车
│   │   ├── MessageCenter.vue    # 消息中心
│   │   ├── Chat.vue             # 聊天详情
│   │   ├── Profile.vue          # 个人中心
│   │   ├── AddressManage.vue    # 地址管理
│   │   ├── OrderList.vue        # 订单列表
│   │   ├── OrderDetail.vue      # 订单详情
│   │   ├── RefundForm.vue       # 退换货申请
│   │   ├── CouponList.vue       # 优惠券
│   │   ├── BrowseHistory.vue    # 浏览历史
│   │   ├── MyReviews.vue       # 我的评价
│   │   └── AfterSales.vue       # 售后记录
│   ├── App.vue         # 根组件
│   └── main.js         # 入口文件
├── index.html          # HTML入口
├── package.json        # 依赖配置
├── vite.config.js      # Vite配置
└── SPEC.md            # 规范文档
```

## 安装与运行

### 1. 安装依赖

```bash
npm install
```

### 2. 启动开发服务器

```bash
npm run dev
```

项目将在 `http://localhost:3000` 启动。

### 3. 构建生产版本

```bash
npm run build
```

### 4. 预览生产版本

```bash
npm run preview
```

## 核心功能调试指引

### 价格监测模块
- 首页展示三类茉莉花商品的价格概览
- 点击任意价格卡片可进入价格详情页
- 价格详情页展示30天价格走势图表
- 可查看各市场价格对比

### 商品模块
- 商品列表支持按分类筛选
- 支持按销量、价格排序
- 点击商品卡片进入详情页
- 详情页支持图片轮播（大图预览）
- 规格选择影响价格显示
- 可加入购物车或立即购买
- 已购用户可发表评价、申请退换货

### 消息中心
- 顶部导航栏消息图标显示未读数量小红点
- 消息分类：全部/物流/优惠活动/聊天记录
- 点击消息可跳转相关页面（订单详情、聊天窗口）
- 支持标记全部已读

### 个人中心
- 顶部显示用户信息、积分、余额
- 订单快捷入口带数量标记
- 地址管理支持完整的CRUD操作
- 订单列表支持按状态筛选

### 购物车
- 商品数量加减
- 删除商品
- 结算功能

## 页面路由

| 路由 | 页面 |
|------|------|
| `/` | 首页（价格监测） |
| `/price-detail` | 价格详情 |
| `/products` | 商品列表 |
| `/product/:id` | 商品详情 |
| `/cart` | 购物车 |
| `/messages` | 消息中心 |
| `/chat/:id` | 聊天详情 |
| `/profile` | 个人中心 |
| `/address` | 地址管理 |
| `/orders` | 订单列表 |
| `/order/:id` | 订单详情 |
| `/refund-form/:orderId` | 退换货申请 |
| `/coupons` | 优惠券 |
| `/history` | 浏览历史 |
| `/reviews` | 我的评价 |
| `/after-sales` | 售后记录 |

## UI规范说明

项目全局采用拟态UI设计风格：

```css
/* 拟态按钮 */
.neumorphic-btn {
  border-radius: 16px;
  box-shadow: 8px 8px 16px #b6b9ba, -8px -8px 16px #fafafd;
  background-image: linear-gradient(145deg, #b6b9ba, #f4f6f8);
}

/* 拟态卡片 */
.neumorphic-card {
  border-radius: 20px;
  box-shadow: 8px 8px 16px #b6b9ba, -8px -8px 16px #fafafd;
  background: #e3e5e7;
}

/* 拟态输入框 */
.neumorphic-input {
  border-radius: 12px;
  box-shadow: inset 4px 4px 8px #b6b9ba, inset -4px -4px 8px #fafafd;
  background: #e3e5e7;
}
```

## 数据说明

项目使用本地模拟数据，位于 `src/data/` 目录下。数据在页面刷新后会重置为初始状态。

## 注意事项

1. 确保Node.js版本 >= 16
2. 首次运行需要执行 `npm install` 安装依赖
3. 项目使用端口3000，如被占用可在 `vite.config.js` 中修改
4. 模拟数据仅供演示，实际项目中需要替换为真实API

## 许可证

MIT License
