# CloudMist CS Plugin
注意：这个项目暂时还处于开发阶段，并不成熟，遇到的一切BUG都是正常现象
如果你无法接受，请不要使用！
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

- 游戏管理
  - 多个游戏房间同时运行
  - 灵活的队伍分配
  - 自动平衡队伍人数
  - 完整的计分板显示

- 玩家系统
  - 击杀统计
  - 死亡统计
  - 经济系统
  - 武器购买系统

## 配置

配置文件位于 `plugins/CloudMist_CS/config.yml`

## 命令

## 命令

### 管理员命令
- `/cs create <名称>` - 创建新游戏
- `/cs remove <名称>` - 删除游戏
- `/cs setlobby <名称>` - 设置游戏大厅
- `/cs setspawn <名称> <队伍>` - 设置队伍出生点
- `/cs forcestart <名称>` - 强制开始游戏
- `/cs forcestop <名称>` - 强制结束游戏

### 玩家命令
- `/cs join <名称>` - 加入游戏
- `/cs leave` - 离开游戏
- `/cs list` - 查看可用游戏列表
- `/cs team <队伍>` - 选择队伍

## API

插件提供了完整的 API 接口供其他插件调用：

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

- Bukkit/Spigot 1.12.2+
- Java 8+

## 开源协议

本项目采用依据Github开源协议开源
详细请看开源协议

## 贡献

欢迎提交 Issue 和 Pull Request！

## 联系方式

如有问题或建议，请通过以下方式联系：

- GitHub Issues
- Email: 3406814785@qq.com
