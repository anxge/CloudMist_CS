# CloudMist CS Plugin

一个基于 Bukkit/Spigot 的我的世界 CS:GO 游戏模式插件。
- 由雾里云端开发团队开发
- 插件作者：Jens_Hon
- 参与的开发者：Jens_Hon，Kenty，ANshiwang

## 功能特点

- 完整的 CS:GO 游戏机制
- 支持多个游戏场地同时运行
- 武器购买系统
- 经济系统
- 队伍系统（T/CT）
- 炸弹安装/拆除机制
- 计分板实时显示
- 自定义游戏设置

## 命令

### 玩家命令 (`/cmcs`, `/cs`, `/csgo`)

- `/cmcs join <游戏>` - 加入游戏
- `/cmcs leave` - 离开当前游戏
- `/cmcs list` - 查看可用游戏
- `/cmcs buy` - 打开购买菜单
- `/cmcs money` - 查看当前金钱
- `/cmcs stats` - 查看个人数据
- `/cmcs top` - 查看排行榜

### 管理员命令 (`/csadmin`)

- `/csadmin create <名称>` - 创建新游戏
- `/csadmin delete <游戏>` - 删除游戏
- `/csadmin start <游戏>` - 强制开始游戏
- `/csadmin stop <游戏>` - 强制停止游戏
- `/csadmin setlobby <游戏>` - 设置大厅
- `/csadmin setspawn <游戏> <t/ct>` - 设置出生点
- `/csadmin setbombsite <游戏> <A/B>` - 设置炸弹点
- `/csadmin setbuyzone <游戏>` - 设置购买区
- `/csadmin reload` - 重载配置
- `/csadmin settings` - 查看设置

## 权限

- `cloudmist.cs.use` - 允许使用玩家命令（默认：true）
- `cloudmist.cs.admin` - 允许使用管理命令（默认：op）
- `cloudmist.admin` - 允许使用 CS 游戏管理命令（默认：op）

## 配置

游戏设置可通过以下方式配置：

- `/csadmin settings` 命令
- 配置文件修改
- 预设模板使用

可配置项包括：
- 回合时间
- 初始金钱
- 每队最少/最多玩家数
- 胜利所需分数
- 击杀奖励
- 回合胜利/失败奖励
- 冻结时间
- 友军伤害开关

## 安装

1. 下载最新版本的插件 JAR 文件
2. 将文件放入服务器的 `plugins` 文件夹
3. 重启服务器或重载插件
4. 使用管理员命令设置游戏场地

## 依赖

- Bukkit/Spigot 1.12+ 
- Java 8+

## 开发者

WLYD Team

## 许可证

[MIT License](LICENSE)
