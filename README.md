# MovementSync

MovementSync 是一个基于 Java 的 Minecraft 机器人插件，专注于实现精确的移动同步和物理模拟。该插件使机器人能够在 Minecraft 世界中进行真实的物理运动，包括重力、跳跃、移动等功能。

## 功能特性

### 核心功能
- **物理引擎**: 实现了基础的物理系统，包括重力、空气阻力等
- **精确移动**: 支持精确控制机器人的位置和移动
- **实时同步**: 将机器人的位置实时同步到服务器
- **碰撞检测**: 基础的碰撞检测系统，防止穿墙等异常行为

### 移动控制
- **基本移动**: 支持前后左右上下六个方向的基本移动
- **跳跃功能**: 实现了类似玩家的跳跃机制
- **重力开关**: 可以启用或禁用重力效果
- **位置查询**: 可以随时查询机器人当前的精确坐标

### 命令系统
- `/move <方向> [距离]` - 控制机器人向指定方向移动
- `/jump` - 让机器人执行跳跃动作
- `/gravity <on|off>` - 启用或禁用重力
- `/whereami` - 显示机器人当前位置坐标

## 技术架构

### 主要组件

#### MovementController (移动控制器)
负责管理机器人的位置、动量和移动逻辑：
- 位置追踪和更新
- 动量计算和应用
- 与服务器的位置同步

#### Physics (物理引擎)
处理所有的物理计算：
- 重力效果实现
- 空气阻力计算
- 地面检测
- 碰撞检测

#### World (世界系统)
管理游戏世界的区块数据和环境信息。

#### 命令系统
实现了多种控制命令，方便用户操作机器人。

## 安装和使用

### 编译要求
- Java 17 或更高版本
- Maven 构建工具

### 构建步骤
```bash
mvn clean package
```

### 集成方式
该插件需要配合 [mcbot](https://github.com/your-org/mcbot) 框架使用，将编译后的 jar 文件放入插件目录即可。

## API 使用示例

### 移动控制示例
```java
// 获取移动控制器
MovementController controller = MovementSync.Instance.getMovementController();

// 设置位置
Vector3d newPosition = new Vector3d(100, 64, 100);
controller.setPosition(newPosition);

// 执行跳跃
controller.jump();

// 设置动量
controller.setMotion(0.5, 0, 0); // 向X轴正方向移动
```

### 重力控制示例
```java
// 获取移动控制器
MovementController controller = MovementSync.Instance.getMovementController();

// 禁用重力
controller.setGravityEnabled(false);

// 启用重力
controller.setGravityEnabled(true);
```

## 插件配置

目前插件无需特殊配置，所有设置均为代码内默认值。

## 开发计划

- [ ] 更完善的碰撞检测系统
- [ ] 支持更多移动模式（疾跑、潜行等）
- [ ] 实现更复杂的物理效果（水流、岩浆等）
- [ ] 添加路径查找和自动寻路功能
- [ ] 支持更多命令和交互功能

## 贡献指南

欢迎提交 Issue 和 Pull Request 来帮助改进这个项目。

## 许可证

该项目采用 GPL3.0 许可证，详情请查看 [LICENSE](LICENSE) 文件。
