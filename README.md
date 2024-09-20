# WebCraw
## Exchange and Stock
> json信息说明
- name => 名称
- price => 价格
- timestamp => 毫秒值
- preClose => 昨日收盘价
- open => 今日开盘价
- high => 最高价
- low => 最低价
- ratio => 涨跌幅
- amount => 成交额
- avgPrice => 均价
- increase => 涨跌额
- close => 今日收盘价
- time => 格式化后的时间

> json配置文件说明
- 配置文件名称固定为config.json，且放置于webCrawler.jar同目录下
- host => sftp ip
- port => sftp 端口
- username => sftp 用户名
- password => sftp密码
- path => sftp存放路径 <mark>需要手动创建该目录<mark/>

> 爬取文件说明
- second_* => 每5秒爬取的实时信息文件
- mitue