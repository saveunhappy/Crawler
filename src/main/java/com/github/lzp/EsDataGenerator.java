package com.github.lzp;

import org.apache.http.HttpHost;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EsDataGenerator implements DataGenerator {

    public static void main(String[] args) throws IOException {
        SqlSessionFactory sqlSessionFactory;
        List<News> newsFromMySql;

        sqlSessionFactory = DataGenerator.getSqlSessionFactory();

        try (SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH)) {
            newsFromMySql = DataGenerator.getNewsListFromMySql(sqlSession);
        }

        try (RestHighLevelClient client = new RestHighLevelClient(
                RestClient.builder(new HttpHost("localhost", 9200, "http")))
        ) {

            for (News news : newsFromMySql) {
                updateNewsToEs(client, news);
            }
        }
    }

    private static void updateNewsToEs(RestHighLevelClient client, News news) throws IOException {
        IndexRequest request = new IndexRequest("news");
        Map<String, Object> newsToInsert = new HashMap<>();
        newsToInsert.put("content", news.getContent());
        newsToInsert.put("title", news.getTitle());
        newsToInsert.put("url", news.getUrl());
        newsToInsert.put("createdAt", news.getCreatedAt());
        newsToInsert.put("updatedAt", news.getUpdatedAt());


        request.source(newsToInsert, XContentType.JSON);

        IndexResponse response = client.index(request, RequestOptions.DEFAULT);
        System.out.println(response.status().getStatus());
    }
}
