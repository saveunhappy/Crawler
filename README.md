# 基于ElasticSearch的多线程爬虫

## Feature
这是一个简单的爬虫项目,他从
[新浪手机端首页](http://sina.cn)爬取新闻标题,
存储于 mysql 数据库中,
最终导入 ElasticSearch 搜索引擎，来实现关于新闻内容的
全文搜索功能。

## Requirements
- ElasticSearch : 7.10.1(通过docker安装)
- MySql : 5.7.33(通过docker安装)
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
