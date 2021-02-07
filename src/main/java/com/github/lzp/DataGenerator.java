package com.github.lzp;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public interface DataGenerator {
    static List<News> getNewsListFromMySql(SqlSession sqlSession) {
        List<News> currentNews = sqlSession.selectList("com.github.lzp.MockMapper.selectNews");
        System.out.println(currentNews.size());
        return currentNews;
    }

    static SqlSessionFactory getSqlSessionFactory() {
        SqlSessionFactory sqlSessionFactory;
        try {
            String resource = "db/mybatis/mybatis-config.xml";
            InputStream inputStream = Resources.getResourceAsStream(resource);
            sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return sqlSessionFactory;
    }
}
