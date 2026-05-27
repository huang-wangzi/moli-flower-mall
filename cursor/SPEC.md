# 横州茉莉花电商交易与价格监测系统（用户端）规范文档

## 1. 项目概述

### 项目名称
基于Vue 3 + Element Plus的横州茉莉花电商交易与价格监测系统（用户端）

### 项目类型
电商Web应用（用户端）

### 核心功能
横州茉莉花产业相关的电商平台，包含价格监测、商品交易、消息中心、个人中心等核心模块。

### 目标用户
- 普通消费者：购买茉莉花相关商品（鲜花、花茶、文创产品）
- 商家：上传商品、管理订单、与客户沟通

---

## 2. UI/UX 规范

### 2.1 整体布局

#### 页面结构
- 顶部导航栏（固定高度60px）
- 主体内容区域（自适应高度）
- 底部信息栏（可选）

#### 响应式断点
- 移动端：< 768px（单列布局）
- 平板：768px - 1024px（双列布局）
- 桌面：> 1024px（多列布局）

### 2.2 拟态UI规范

#### 全局样式
```css
body {
  background: #e3e5e7;
  font-family: 'Microsoft YaHei', 'PingFang SC', sans-serif;
}

/* 拟态按钮 */
.neumorphic-btn {
  width: 80px;
  height: 80px;
  border-radius: 16px;
  box-shadow: 8px 8px 16px #b6b9ba, -8px -8px 16px #fafafd;
  background-image: linear-gradient(145deg, #b6b9ba, #f4f6f8);
  border: none;
  cursor: pointer;
  transition: all 0.3s ease;
}

.neumorphic-btn:hover {
  box-shadow: 12px 12px 20px #b6b9ba, -12px -12px 20px #fafafd;
}

.neumorphic-btn:active {
  box-shadow: inset 4px 4px 8px #b6b9ba, inset -4px -4px 8px #fafafd;
}

/* 拟态卡片 */
.neumorphic-card {
  border-radius: 20px;
  box-shadow: 8px 8px 16px #b6b9ba, -8px -8px 16px #fafafd;
  background: #e3e5e7;
  padding: 20px;
  transition: all 0.3s ease;
}

.neumorphic-card:hover {
  box-shadow: 12px 12px 20px #b6b9ba, -12px -12px 20px #fafafd;
}

/* 拟态输入框 */
.neumorphic-input {
  border-radius: 12px;
  box-shadow: inset 4px 4px 8px #b6b9ba, inset -4px -4px 8px #fafafd;
  background: #e3e5e7;
  border: none;
  padding: 12px 16px;
  outline: none;
  transition: all 0.3s ease;
}

.neumorphic-input:focus {
  box-shadow: inset 6px 6px 12px #b6b9ba, inset -6px -6px 12px #fafafd;
}
```

#### 色彩规范
- 主色调：#4A7C59（茉莉绿）
- 辅助色：#F5F5DC（米色/茉莉白）
- 强调色：#FFD700（金色/茉莉花色）
- 背景色：#E3E5E7（拟态灰）
- 文字主色：#333333
- 文字次色：#666666
- 错误色：#E74C3C
- 成功色：#27AE60

### 2.3 组件规范

#### 导航栏
- 高度：60px
- 背景：#E3E5E7 配合拟态阴影
- 包含：Logo、搜索框、消息图标、个人中心图标

#### 消息图标小红点
- 位置：图标左上角
- 样式：红色圆形背景 + 白色数字
- 动画：新消息到达时闪烁

#### 商品卡片
- 圆角：20px
- 阴影：拟态风格
- 包含：商品图片、标题、价格、销量

#### 按钮类型
- 主按钮：茉莉绿背景，白色文字
- 次按钮：拟态风格
- 图标按钮：圆形拟态

---

## 3. 功能规范

### 3.1 首页 - 价格监测系统

#### 功能描述
展示横州茉莉花价格监测数据，仅供查看不可修改。

#### 数据展示
- 分类：茉莉鲜花、花茶、文创产品
- 指标：当日价格、历史均价、涨跌幅度
- 图表：价格走势折线图

#### 交互规则
- 点击价格监测模块 → 跳转价格详情页
- 价格详情页展示：
  - 价格走势图表（30天数据）
  - 各市场价格对比
  - 按品类分类的价格列表

### 3.2 商品模块

#### 商品列表页
- 展示形式：网格布局（4列桌面/2列移动）
- 交互：点击商品任意位置 → 跳转商品详情页

#### 商品详情页
- 图片展示：
  - 主图 + 缩略图列表
  - 点击缩略图切换主图
  - 点击主图 → 查看大图（弹窗轮播）
  
- 规格选择：
  - 下拉选择器（鲜花重量、花茶包装、文创尺寸）
  - 规格组合影响价格
  
- 操作按钮：
  - 左下角：沟通商家
  - 底部：联系客服、加入购物车、立即购买
  
- 商品介绍：
  - 富文本格式
  - 支持图文混排
  
- 用户评价区：
  - 展示已购用户评价（文字+图片）
  - 购买后可发布评价
  - 申请退换货（填写表单）

#### 商家商品管理（商家端）
- 图片上传：
  - 选择本地文件（支持多选）
  - 预览图片
  - 确认/取消上传
  - 上传成功后显示缩略图
  
- 商品描述编辑：
  - 富文本编辑器
  - 内容同步至商品详情页

### 3.3 消息中心模块

#### 消息分类
- 物流助手：订单物流进度
- 优惠活动：促销活动通知
- 聊天记录：商家/客服对话

#### 物流助手
- 状态展示：待揽收、运输中、待签收、已签收
- 进度条展示

#### 聊天记录
- 显示所有沟通记录
- 时间戳格式：XX分钟前、XX小时前、具体日期
- 点击条目 → 查看完整历史对话

#### 小红点规则
- 未读消息：图标左上角红点 + 数字
- 点击后红点消失
- 数字为未读消息总数

### 3.4 个人中心模块

#### 基础信息
- 头像、昵称、会员等级
- 信息修改功能

#### 地址管理
- 地址列表展示
- 新增/编辑/删除地址
- 设为默认地址
- 表单校验：必填项提示

#### 订单管理
- 订单状态：待付款、待收货、已完成、售后
- 订单详情查看
- 售后申请入口

#### 其他功能
- 优惠券：查看+使用
- 历史浏览：商品浏览记录
- 评价管理：已购商品评价
- 售后记录：退换货记录

---

## 4. 路由配置

| 路由 | 组件 | 描述 |
|------|------|------|
| / | Home | 首页（价格监测） |
| /price-detail | PriceDetail | 价格详情页 |
| /products | ProductList | 商品列表 |
| /product/:id | ProductDetail | 商品详情页 |
| /messages | MessageCenter | 消息中心 |
| /chat/:id | Chat | 聊天详情 |
| /profile | Profile | 个人中心 |
| /address | AddressManage | 地址管理 |
| /orders | OrderList | 订单列表 |
| /order/:id | OrderDetail | 订单详情 |
| /refund-form/:orderId | RefundForm | 退换货申请 |
| /coupons | CouponList | 优惠券 |
| /history | BrowseHistory | 浏览历史 |
| /reviews | MyReviews | 我的评价 |

---

## 5. 状态管理（Pinia）

### stores/user.js
- 用户信息
- 登录态
- 头像、昵称

### stores/message.js
- 未读消息数
- 消息列表
- 小红点状态

### stores/cart.js
- 购物车商品
- 添加/删除/修改数量

### stores/order.js
- 订单列表
- 订单状态

---

## 6. 模拟数据

### 商品数据
- 茉莉鲜花：多个品种、规格
- 茉莉花茶：多种包装
- 茉莉文创：工艺品、纪念品

### 价格数据
- 30天价格走势
- 分类价格对比
- 历史均价

### 订单数据
- 多种状态订单
- 物流信息

### 消息数据
- 系统通知
- 聊天记录
- 物流动态

---

## 7. 验收标准

### 视觉验收
- [ ] 全局采用拟态UI风格
- [ ] 按钮、卡片、输入框符合规范
- [ ] 响应式布局正常
- [ ] 小红点显示正确

### 功能验收
- [ ] 价格监测模块可查看数据
- [ ] 点击跳转详情页正常
- [ ] 商品图片轮播正常
- [ ] 规格选择功能正常
- [ ] 购物车添加/删除正常
- [ ] 消息中心展示正确
- [ ] 小红点交互正常
- [ ] 个人中心各功能可用
- [ ] 退换货表单校验正常
- [ ] 地址管理CRUD正常

### 交互验收
- [ ] hover效果正常
- [ ] 点击反馈正常
- [ ] 表单验证提示友好
- [ ] 图片上传预览正常
