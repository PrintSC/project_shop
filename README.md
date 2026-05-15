# TmallDemo -- 仿天猫电商平台

一个基于 **Spring Boot + Vue 3** 的全栈电商 Web 应用，模拟天猫（Tmall）核心购物流程，包含前台商城和后台管理系统两大模块。

## 目录

- [项目预览](#项目预览)
- [技术栈](#技术栈)
- [项目结构](#项目结构)
- [功能特性](#功能特性)
- [数据库设计](#数据库设计)
- [快速开始](#快速开始)
  - [环境要求](#环境要求)
  - [数据库初始化](#数据库初始化)
  - [后端启动](#后端启动)
  - [前端启动](#前端启动)
- [默认账号](#默认账号)
- [API 文档](#api-文档)
- [核心设计](#核心设计)
- [部署说明](#部署说明)
- [License](#license)

---

## 项目预览

| 前台商城 | 后台管理 |
|---------|---------|
| 首页商品展示 | 数据统计仪表盘 |
| 分类浏览与搜索 | 商品 / 分类 / 属性管理 |
| 商品详情与评价 | 订单管理与发货 |
| 购物车与下单 | 用户与评价管理 |
| 支付宝 / 微信扫码支付 | 商品 Excel 批量导入 |

---

## 技术栈

### 后端

| 技术 | 说明 |
|------|------|
| Java 8 | 编程语言 |
| Spring Boot 2.1.6 | 应用框架 |
| MyBatis-Plus 2.3 | ORM 框架 |
| MySQL 8.0 | 关系型数据库 |
| Redis | 缓存与 Session 存储 |
| Alibaba Druid | 数据库连接池 |
| Swagger 2.9 | API 文档 |
| Lombok | 简化实体类代码 |
| Hutool 5.8 | 工具库（验证码生成等） |
| Apache POI 3.9 | Excel 导入导出 |
| Alipay SDK 4.33 | 支付宝支付 |
| WeChat Pay SDK 4.3 | 微信支付 |
| Log4j2 | 日志框架 |

### 前端

| 技术 | 说明 |
|------|------|
| Vue 3.4 | 渐进式 JavaScript 框架（Composition API） |
| Vite 5.1 | 构建工具 |
| Vue Router 4 | 路由管理 |
| Pinia 2.1 | 状态管理 |
| Element Plus 2.5 | UI 组件库 |
| ECharts 5.5 | 数据可视化图表 |
| Axios 1.6 | HTTP 请求库 |

---

## 项目结构

```
TmallDemo/
├── pom.xml                                    # Maven 配置
├── src/
│   └── main/
│       ├── java/com/xq/tmall/
│       │   ├── TmallApplication.java          # Spring Boot 启动类
│       │   ├── config/                        # 配置类
│       │   │   ├── WebMvcConfig.java          # 静态资源映射
│       │   │   ├── datasource/                # Druid 数据源配置
│       │   │   ├── mybatis/                   # MyBatis-Plus 配置
│       │   │   ├── redis/                     # Redis 配置
│       │   │   └── swagger2/                  # Swagger 配置
│       │   ├── controller/
│       │   │   ├── BaseController.java        # Session 鉴权基类
│       │   │   ├── admin/                     # 后台管理接口 (8个)
│       │   │   └── fore/                      # 前台商城接口 (8个)
│       │   ├── dao/                           # MyBatis Mapper 接口
│       │   ├── entity/                        # 实体类
│       │   ├── service/                       # 业务逻辑接口
│       │   │   └── impl/                      # 业务逻辑实现
│       │   ├── pay/                           # 支付模块（策略模式）
│       │   │   ├── PayFace.java               # 支付接口
│       │   │   ├── impl/alipay/               # 支付宝实现
│       │   │   ├── impl/wx/                   # 微信支付实现
│       │   │   ├── req/                       # 支付请求 DTO
│       │   │   └── resp/                      # 支付响应 DTO
│       │   └── util/                          # 工具类
│       └── resources/
│           ├── application.yml                # 主配置文件
│           ├── application-dev.yml            # 开发环境配置
│           ├── application-prod.yml           # 生产环境配置
│           ├── mapper/                        # MyBatis XML 映射文件
│           └── sql/tmalldemodb-mysql8.0.sql   # 数据库建表及初始数据
│
└── frontend/                                  # Vue 3 前端项目
    ├── package.json
    ├── vite.config.js                         # Vite 配置（含代理）
    ├── index.html
    └── src/
        ├── main.js                            # 入口文件
        ├── App.vue                            # 根组件
        ├── api/
        │   ├── request.js                     # Axios 实例与拦截器
        │   ├── admin.js                       # 后台 API 封装
        │   └── fore.js                        # 前台 API 封装
        ├── layouts/
        │   ├── AdminLayout.vue                # 后台布局（侧边栏 + 顶栏）
        │   └── ForeLayout.vue                 # 前台布局（头部 + 底部）
        ├── router/index.js                    # 路由配置
        ├── stores/
        │   ├── auth.js                        # 用户/管理员登录状态
        │   └── cart.js                        # 购物车状态
        ├── styles/global.css                  # 全局样式
        └── views/
            ├── admin/                         # 后台页面 (11个)
            └── fore/                          # 前台页面 (14个)
```

---

## 功能特性

### 前台商城（用户端）

- **首页展示** -- 商品分类导航、商品列表、特价/推荐商品
- **用户注册** -- AES-CBC 加密存储密码（PBKDF2 密钥派生，每用户独立 Salt + IV）
- **用户登录** -- 图形验证码防护（Hutool LineCaptcha），Session 认证
- **商品浏览** -- 按分类浏览、分页排序
- **商品详情** -- 商品图片（概览图 + 详情图）、属性参数、用户评价、"猜你喜欢"推荐
- **购物车** -- 添加/修改/删除商品、勾选结算
- **订单创建** -- 单件购买或购物车批量下单、省/市/区三级地址选择
- **在线支付** -- 支付宝扫码支付、微信扫码支付（策略模式，可扩展）
- **订单流转** -- 待付款 -> 待发货 -> 待确认 -> 交易成功/关闭
- **商品评价** -- 订单完成后可评价商品
- **个人中心** -- 查看/编辑个人信息、上传头像

### 后台管理（管理端）

- **数据仪表盘** -- 商品数/用户数/订单数统计、近 7 天订单趋势图（ECharts）
- **分类管理** -- 分类的增删改查、图片上传
- **商品管理** -- 商品的增删改查、图片上传、属性绑定、Excel 批量导入/导出
- **属性管理** -- 分类属性的增删改查（如材质、风格、品牌）
- **订单管理** -- 订单查看/搜索/筛选、发货操作、删除订单
- **用户管理** -- 查看/删除用户
- **评价管理** -- 查看/删除商品评价
- **管理员账号** -- 个人信息设置、头像上传
- **登录认证** -- 图形验证码防护

---

## 数据库设计

数据库名：`tmalldemodb`，共 **10 张表**：

| 表名 | 说明 | 备注 |
|------|------|------|
| `address` | 地址表（省/市/区三级） | 自引用层级结构 |
| `admin` | 管理员表 | |
| `category` | 商品分类表 | 支持软删除 |
| `product` | 商品表 | 关联分类 |
| `productImage` | 商品图片表 | 0=概览图, 1=详情图；级联删除 |
| `property` | 分类属性表 | 级联删除 |
| `propertyValue` | 商品属性值表 | 关联商品与属性 |
| `user` | 用户表 | 支持软删除 |
| `productOrder` | 订单表 | 状态: 0待付款/1待发货/2待确认/3交易成功/4交易关闭 |
| `productOrderItem` | 订单明细表 | 级联删除 |
| `review` | 商品评价表 | 关联用户、商品、订单明细 |

ER 关系概览：

```
category 1──n product 1──n productImage
    │                  1──n propertyValue n──1 property
    │
    └──1──n property

user 1──n productOrder 1──n productOrderItem n──1 product
                   │                        └──1 review
                   │
                   └── n──1 address

admin (独立)
```

---

## 快速开始

### 环境要求

| 环境 | 版本要求 |
|------|---------|
| JDK | 1.8+ |
| Maven | 3.6+ |
| MySQL | 8.0+ |
| Redis | 5.0+ |
| Node.js | 16+ |
| npm | 7+ |

### 数据库初始化

1. 启动 MySQL 服务，创建数据库：

```sql
CREATE DATABASE tmalldemodb DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
```

2. 导入建表脚本（含初始数据）：

```bash
mysql -u root -p tmalldemodb < src/main/resources/sql/tmalldemodb-mysql8.0.sql
```

3. 确保 Redis 服务已启动（默认连接 `localhost:6379`，无密码）。

### 后端启动

1. 修改数据库配置（如有需要）：

   编辑 `src/main/resources/application-dev.yml`，修改数据库用户名和密码：

   ```yaml
   spring:
     datasource:
       username: root
       password: 123456    # 修改为你的 MySQL 密码
   ```

2. 构建并运行：

```bash
# 使用 Maven Wrapper
./mvnw clean package
java -jar target/tmall-1.0-SNAPSHOT.jar

# 或直接运行
./mvnw spring-boot:run
```

后端启动后运行在 `http://localhost:8082/tmall`。

### 前端启动

```bash
cd frontend

# 安装依赖
npm install

# 启动开发服务器
npm run dev
```

前端启动后访问 `http://localhost:5173`，API 请求会自动代理到后端 `http://localhost:8082`。

---

## 默认账号

### 管理员账号

| 用户名 | 密码 |
|--------|------|
| MRJIANG | 123456 |
| demilehan | 123456 |
| lihao | 123456 |
| a1209577113 | 123456 |

后台登录地址：`http://localhost:5173/admin/login`

### 用户账号

前台需要自行注册，注册地址：`http://localhost:5173/register`

---

## API 文档

后端集成了 Swagger，启动后可通过以下地址访问 API 文档：

```
http://localhost:8082/tmall/swagger-ui.html
```

### 接口概览

**前台接口 (`/fore/`)**

| 接口 | 说明 |
|------|------|
| `POST /fore/register` | 用户注册 |
| `POST /fore/login` | 用户登录 |
| `GET /fore/home` | 首页数据 |
| `GET /fore/category/{id}` | 分类商品列表 |
| `GET /fore/product/{id}` | 商品详情 |
| `POST /fore/cart` | 购物车操作 |
| `POST /fore/order` | 创建订单 |
| `POST /fore/pay` | 发起支付 |
| `GET /fore/order/{id}` | 订单详情 |

**后台接口 (`/admin/`)**

| 接口 | 说明 |
|------|------|
| `POST /admin/login` | 管理员登录 |
| `GET /admin/dashboard` | 仪表盘数据 |
| `/admin/category/**` | 分类管理 CRUD |
| `/admin/product/**` | 商品管理 CRUD |
| `/admin/property/**` | 属性管理 CRUD |
| `/admin/order/**` | 订单管理 |
| `/admin/user/**` | 用户管理 |
| `/admin/review/**` | 评价管理 |

---

## 核心设计

### 支付模块（策略模式）

支付模块采用策略模式设计，通过 `PayFace` 接口抽象支付行为，支持多种支付方式的灵活扩展：

```java
public interface PayFace {
    PayResponse pay(PayRequest request);
}
```

- `AliScanPayImpl` -- 支付宝扫码支付实现
- `WxScanPayImpl` -- 微信扫码支付实现

通过 `PayTypeEnum` 枚举映射支付类型与 Bean 名称，`ForeOrderController` 注入 `Map<String, PayFace>` 实现动态分发。

### 密码安全

用户密码采用 **AES-CBC-128** 加密，密钥通过 **PBKDF2WithHmacSHA256**（65536 次迭代）从密码派生。每个用户拥有独立的 Salt 和 IV，存储在数据库中，确保即使数据库泄露也无法批量解密。

### 认证机制

前后台均采用 **Session 认证**，`BaseController` 提供 `checkAdmin()` 和 `checkUser()` 方法校验 Session 中的 `adminId` / `userId` 属性，未登录时返回 401 跳转。

### 文件上传

商品图片、分类图片、用户/管理员头像上传至 `./images/res/images/item/` 目录下，通过 Spring MVC 的 `ResourceHandler` 映射 `/res/images/**` 路径提供静态访问。

### Excel 批量导入

商品数据支持通过 Excel 文件批量导入，使用 Apache POI 解析。管理员可下载模板文件，填写后上传即可批量创建商品。

---

## 部署说明

### 生产环境构建

**后端：**

```bash
# 打包（跳过测试加速）
./mvnw clean package -DskipTests

# 运行（使用 prod 配置）
java -jar target/tmall-1.0-SNAPSHOT.jar --spring.profiles.active=prod
```

**前端：**

```bash
cd frontend
npm run build
```

构建产物输出到 `frontend/dist/`，可部署到 Nginx 等 Web 服务器。

### Nginx 配置示例

```nginx
server {
    listen 80;
    server_name your-domain.com;

    # 前端静态资源
    location / {
        root /path/to/frontend/dist;
        try_files $uri $uri/ /index.html;
    }

    # API 反向代理
    location /tmall/ {
        proxy_pass http://localhost:8082/tmall/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    }

    # 图片资源
    location /res/images/ {
        alias /path/to/project/images/res/images/;
    }
}
```

---

## License

本项目仅供学习交流使用。
