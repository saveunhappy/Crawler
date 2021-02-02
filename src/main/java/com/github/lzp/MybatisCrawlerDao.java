package com.github.lzp;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

public class MybatisCrawlerDao implements CrawlerDao {
    private SqlSessionFactory sqlSessionFactory;

    public MybatisCrawlerDao() {
        try {
            String resource = "db/mybatis/mybatis-config.xml";
            InputStream inputStream = Resources.getResourceAsStream(resource);
            sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean isProcessedLink(String link) {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            int result = session.selectOne("com.github.lzp.MyMapper.isProcessedLink", link);
            return result != 0;
        }
    }

    @Override
    public void updateProcessedLinksTable(String link) {
        insertTable(link, "LINKS_ALREADY_PROCESSED");
    }

    @Override
    public void updateLinksToBeProcessTable(String link) {
        insertTable(link, "LINKS_TO_BE_PROCESSED");
    }

    private void insertTable(String link, String tableName) {
        HashMap<String, String> sqlParams = new HashMap<>();
        sqlParams.put("tableName", tableName);
        sqlParams.put("link", link);
        try (SqlSession session = sqlSessionFactory.openSession(true)) {
            session.insert("com.github.lzp.MyMapper.insertLinkTable", sqlParams);
        }
    }

    @Override
    public synchronized String getNextUrlThenDelete() {
        String url;
        try (SqlSession session = sqlSessionFactory.openSession(true)) {
            url = session.selectOne("com.github.lzp.MyMapper.selectNextLink", 101);
            session.delete("com.github.lzp.MyMapper.deleteUrl", url);
        }
        return url;
    }

    @Override
    public void insertNewsIntoDatabase(String link, String title, String content) {
        try (SqlSession session = sqlSessionFactory.openSession(true)) {
            session.insert("com.github.lzp.MyMapper.insertNews", new News(link, title, content));
        }
    }
}
