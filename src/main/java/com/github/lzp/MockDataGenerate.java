package com.github.lzp;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.util.List;
import java.util.Random;

public class MockDataGenerate {
    private static final int TARGET_ROW_COUNT = 500_000;

    public static void main(String[] args) {
        SqlSessionFactory sqlSessionFactory;
        List<News> currentNews;

        try {
            String resource = "db/mybatis/mybatis-config.xml";
            InputStream inputStream = Resources.getResourceAsStream(resource);
            sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        try (SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH)) {
            currentNews = getNewsList(sqlSession);
            int count = TARGET_ROW_COUNT - currentNews.size();
            mockNewsData(currentNews, sqlSession, count);
        }
    }

    private static void mockNewsData(List<News> currentNews, SqlSession sqlSession, int count) {
        Random random = new Random();
        try {
            while (count-- > 0) {
                News newsToBeInserted = getMockNewsToBeInserted(currentNews, random);
                sqlSession.insert("com.github.lzp.MockMapper.insertNews", newsToBeInserted);
                System.out.println("Left:" + count);
            }
            if (count % 2000 == 0) {
                sqlSession.flushStatements();
            }
            sqlSession.commit();
        } catch (Exception e) {
            sqlSession.rollback();
            throw new RuntimeException(e);
        }
    }

    private static News getMockNewsToBeInserted(List<News> currentNews, Random random) {
        int index = random.nextInt(currentNews.size());
        News newsToBeInserted = currentNews.get(index);

        Instant instant = newsToBeInserted.getCreatedAt().minusSeconds(random.nextInt(3600 * 24 * 3));
        newsToBeInserted.setCreatedAt(instant);
        newsToBeInserted.setUpdatedAt(instant);
        return newsToBeInserted;
    }

    private static List<News> getNewsList(SqlSession sqlSession) {
        List<News> currentNews = sqlSession.selectList("com.github.lzp.MockMapper.selectNews");
        System.out.println(currentNews.size());
        return currentNews;
    }
}
