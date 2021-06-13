# BotPlugin
一个运行于[Mirai Console](https://github.com/mamoe/mirai-console)的插件    
A plugin for [Mirai Console](https://github.com/mamoe/mirai-console) with image sender,pixiv querying and more.
## 功能
* 发送本地图库的圖片(请手动下载赫图,另提供指令sendImage一次发送多张)(发送带有'图'和'来'的消息或者色图来图片(不知道这图我也没办法,你可以用图片ID发出去)) 
* 发送在线的Json源图片
* 发送在线的Pixiv的圖片(允许设置代理,说得好像不设置代理谁进得去呢?)
* 被At时复读,并且把At的对象换成对方(此外还支持颠倒消息顺序,随机旋转消息图片)
* 在群聊中更新某些配置(带有用户控制(废话))
* 设置群聊黑名单(注意settings和darklist指令全局可用,黑名单群settings指令只回復manager)
* 禁言,取消禁言,加群,退群提示(注意:有臭味
* PetPet图片生成(群聊被戳一戳时,请务必为账号使用ANDROID_PHONE协议以便正常使用)
* 插件管理员执行Javascript脚本（可用变量：bot,event,dlg，dlg请查看ScriptMethods.kt获取具体用法）
* 随机复读
* 没了
## 特色
* 单线程撤回发出的本地图片(防止请求过于频繁被服务器拒绝)
* CommandDispatcher(?蜜汁操作,指令派发)    
**在本仓库的Releases中可以直接下载编译好的jar文件! 如果不是需要研究,你可以直接下载**
## 简单使用
- 下载Release里发布的jar.
- [启动Mirai-Console](https://github.com/mamoe/mirai-console/blob/master/docs/Run.md)
- 复制jar到Console工作目录下的plugins里
- 见下方'开始使用'栏目
### 当前项目环境:
* Mirai 2.6.5   
* Kotlin 1.5.10   
* OpenJdk 14   
* Intellij IDEA 2021.1.2   
**注意! 在编译本项目之前,请留意删除gradle.properties中的网络代理设置**   
## 开始使用
建议使用 Java 11或者更高版本的Java    
先带着插件运行一次Mirai Console,然后停止   
在`mirai文件夹/config/RosemoeBotPlugin/PluginConfig.yml`中把   
```yml
managers: []
```
修改为
```yml
managers:
  - 你的QQ
```
当然也可以添加多个Manager,比如   
```yml
managers:
  - 114514
  - 1919810
  - 12345678
```   
然后重新运行Mirai Console
## 群聊指令表
非常建议您先读完下面的指令表再使用
### 设置部分
只有Plugin Manager(不是Bot Manager)才能使用这些指令!
#### 全局设置
```Bash
/settings set recallDelay <时间>
/settings set recallInterval <时间>
/settings set prefix <指令前缀,默认'/'>
/settings set repeatFactor <概率的小数>
/settings reload
/settings reloadBase
/settings get recallDelay
/settings get recallInterval
/settings get prefix
/settings get repeatFactor
/settings enable <功能>
/settings disable <功能>
```
reloadBase只刷新配置不重新建立图片索引,算是轻重载
#### 图片源设置
图片源保存在image_sources.yml中
```Bash
添加一个路径：
/sources path <名称> <路径>

添加一个在线Json图源
/sources json <名称> <网址> <数据路径>
对应的网址要返回一个Json文本，其中通过数据路径可以到达url元素
比如返回下面这段Json：
{"code":1,"msg":"ok","data":"http:\/\/test.xxx.com\/large\/a15b4afegy1fmvjv7pshlj21hc0u0e0s.jpg"}
需要设置的数据路径是 data
对于下面这段Json：
{"code":0,"msg":"","quota":8,"quota_min_ttl":7029,"count":1,"data":[{"pid":61732396,"p":0,"uid":946272,"title":"カンナ","author":"Aile\/エル","url":"https:\/\/i.pixiv.cat\/img-original\/img\/2017\/03\/04\/00\/00\/01\/61732396_p0.png","r18":false,"width":583,"height":650,"tags":["カンナカムイ(小林さんちのメイドラゴン)","康娜卡姆依（小林家的龙女仆）","カンナ","康娜","カンナカムイ","康娜卡姆依","小林さんちのメイドラゴン","小林家的龙女仆","尻神様","尻神样","竜娘","龙娘","マジやばくね","that's wicked","高品質パンツ","高品质内裤","魅惑のふともも","魅惑的大腿"]}]}
需要设置的数据路径是 data\0\url

删除一个源
/sources remove <名称>

刷新源列表
对源进行操作时不会立即生效，使用settings reload会导致设置文件被覆盖
可以使用这个方法来在修改图源列表后刷新图源 
/sources refresh
```
另外，你也可以使用脚本手动完成获取图片的逻辑。至于如何配置使用，请研究ImageSource.kt（懒得写指令了23333）
#### 功能名称表
```Kotlin
val allowedModuleName = listOf(
    "ImageSender",
    "Pixiv",
    "BatchImg",
    "AtReply",
    "Welcome",
    "MuteTip",
    "ReverseAtReply",
    "ReverseAtReplyImage",
    "PetPet",
    "Repeat"
)
```
### Pixiv
```Bash
查看画作信息和预览图
/pixiv illust <画作ID>
查看指定画作的指定P的大图
/pixiv illust <画作ID> <图片索引>
```
R18画作将不会发送图片
### 本地图片
```Bash
/sendImage <图片数目>
```
### Ping
```Bash
/ping 后面和操作系统一样
```
### IP获取
```Bash
/ipList <网址>
/ipList4 <网址>
/ipList6 <网址>
```
### 黑名单
```Bash
/darklist add <群号或this>
/darklist remove <群号或this>
/darklist list <页码,可选,每页15个>
```
## Console指令表
咕了,还没写呢
## 设置Pixiv代理
* `proxyEnabled` 配置是否启用代理
* `proxyType` 配置代理类型,必须是socks或者http其中一种,填写其它默认socks
* `proxyAddress` 配置代理地址,如`127.0.0.1`
* `proxyPort` 配置代理端口,如`1080`   
这些代理设置仅用于Pixiv网路连接,不作它用   
实现上使用了Java的Proxy类
## 示例PluginConfig.yml
```yml
managers: 
  - 1145141919
  - 8101145141
states: 
  ImageSender: true
commandPrefix: '/'
darkListGroups: 
  - 122344646
  - 464544644
imageRecallDelay: 30000
recallMinPeriod: 180
maxImageRequestCount: 16
proxyEnabled: true
proxyType: socks
proxyAddress: 127.0.0.1
proxyPort: 1080
allowR18ImageInPixiv: false
repeatFactor: 0.05
```
