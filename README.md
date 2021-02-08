# 多线程爬虫和ElasticSearch搜索引擎
[![CircleCI](https://circleci.com/gh/pccmast/Crawler.svg?style=shield) ](https://circleci.com/gh/pccmast/Crawler)
![license](https://img.shields.io/github/license/pccmast/Crawler?style=flat-square)

这是一个简单的爬虫项目,
他从 [新浪手机端首页](http://sina.cn) 爬取新闻标题,
存储于 MySql 数据库中,
最终导入 ElasticSearch 搜索引擎, 来实现关于爬取新闻内容的
全文搜索功能。

## Requirements
- Docker : 20.10.2
- ElasticSearch : 7.10.1 (通过docker安装)
- MySql : 5.7.33 (通过docker安装)
- Maven : 3.6.3

## 部署步骤
1. 下载本仓库代码
2. 通过 docker 部署 mysql 和 elasticsearch
```bash
docker run -d -v resource/esdata:/usr/share/elasticsearch/data --name elasticsearch -p 9200:9200 -p 9300:9300 -e "discovery.type=single-node" elasticsearch:7.10.1

docker run --name mysql-for-crawler -e MYSQL_ROOT_PASSWORD=* -p 3306:3306 -v resources/db/data:/var/lib/mysql -d mysql:5.7.33
```

## 目录结构描述
```markdown
│ .gitignore
├─.circleci
└─src
   ├─main
   │  ├─java
   │  │  └─com
   │  │      └─github
   │  │          └─lzp
   │  │                  Crawler.java
   │  │                  CrawlerDao.java
   │  │                  DataGenerator.java
   │  │                  DataSearchEngine.java
   │  │                  EsDataGenerator.java
   │  │                  JdbcCrawlerDao.java
   │  │                  Main.java
   │  │                  MockDataGenerator.java
   │  │                  MybatisCrawlerDao.java
   │  │                  News.java
   │  └─resources
   │      ├─db
   │      │  ├─migration  // 存放flyway管理数据库的sql脚本
   │      │  │      V1__Create_tables.sql
   │      │  │      V2__Create_index.sql
   │      │  └─mybatis    // 存放mybatis用的映射文件
   │      │          MockMapper.xml
   │      │          mybatis-config.xml
   │      │          MyMapper.xml
   │      └─esdata        // elasticsearch的数据
   │      └─data          // mysql的数据
   └─test
       └─java
           └─com
               └─github
                   └─lzp
                        SmokeTest.java
```

其中，位于 com.github.lzp 的文件：
```markdown
CrawlerDao.java         持久层接口，负责与数据库联络的方法
JdbcCrawlerDao.java     使用了JDBC的持久层接口实现类
MybatisCrawlerDao.java  使用了mybatis的持久层接口实现类

DataGenerator.java      数据生成接口,其中有两个实现类的公有方法
MockDataGenerator.java  根据爬取的新闻数据生成假数据, 并插入mysql数据库
EsDataGenerator.java    根据爬取的新闻数据生成假数据, 并插入elasticsearch搜索引擎

Main.java               多线程爬取网络链接的主文件
Crawler.java            爬虫线程的实现类
DataSearchEngine.java   在控制台输入, 搜索elasticsearch上的项目并返回结果
News.java               爬取项目存储的对象--新闻类
```

## 使用
1. 运行`Main.java`爬取网络数据, 默认会把数据存入 mysql 数据库中(使用 JDBC 存入 h2 数据库) 

2. 运行`MockDataGenerator.java`, 从mysql数据库中读取数据, 增加假数据以后把取得的数据放回 mysql 数据库。**(基于1)**

3. 运行`EsDataGenerator.java`, 从mysql数据库中读取数据, 增加假数据以后把取得的数据放入 elasticsearch 搜索引擎中。**(基于1)**

4. 运行`DataSearchEngine.java`, 在输入框输入想查询的文本, 返回得到查询结果。**(基于3)**

5. 在浏览器输入`http://localhost:9200/`, 查看elasticsearch相关信息。比如, `http://localhost:9200/_count` 、`http://localhost:9200/_search?q=title:美国` **(基于3)**
