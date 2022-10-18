package com.github.lzp;

import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;

import java.time.Instant;
import java.util.List;
import java.util.Random;

public class MockDataGenerator implements DataGenerator {
    private static final int TARGET_ROW_COUNT = 500_0;

    public static void main(String[] args) {
        SqlSessionFactory sqlSessionFactory;
        List<News> currentNews;

        sqlSessionFactory = DataGenerator.getSqlSessionFactory();

        try (SqlSession sqlSession = sqlSessionFactory.openSession(true)) {
//            currentNews = DataGenerator.getNewsListFromMySql(sqlSession, 2000);
            currentNews = DataGenerator.getNewsListFromMySql(sqlSession);
            int count = TARGET_ROW_COUNT - currentNews.size();
            mockNewsDataAndInsertIntoDB(currentNews, sqlSession, count);
        }
    }

    private static void mockNewsDataAndInsertIntoDB(List<News> currentNews, SqlSession sqlSession, int count) {
        Random random = new Random();
        try {
            while (count-- > 0) {
                News newsToBeInserted = getMockNewsToBeInserted(currentNews, random);
                sqlSession.insert("com.github.lzp.MockMapper.insertNews", newsToBeInserted);
                System.out.println("Left:" + count);
                if (count % 2000 == 0) {
                    sqlSession.flushStatements();
                }
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
}
