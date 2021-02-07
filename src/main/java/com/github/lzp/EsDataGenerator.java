package com.github.lzp;

import org.apache.http.HttpHost;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;

import java.io.IOException;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class EsDataGenerator implements DataGenerator {

    public static void main(String[] args) throws IOException {
        SqlSessionFactory sqlSessionFactory;
        List<News> newsFromMySql;

        sqlSessionFactory = DataGenerator.getSqlSessionFactory();

        try (SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH)) {
            newsFromMySql = DataGenerator.getNewsListFromMySql(sqlSession);
        }
        for (int i = 0; i < 1000; i++) {
            writeSingThread(newsFromMySql);
        }
    }

    private static void writeSingThread(List<News> newsFromMySql) throws IOException {
        try (RestHighLevelClient client = new RestHighLevelClient(
                RestClient.builder(new HttpHost("localhost", 9200, "http")))
        ) {
            for (int i = 0; i < 100; i++) {
                BulkRequest bulkRequest = new BulkRequest();
                for (News news : newsFromMySql) {
                    IndexRequest indexRequest = setIndexRequest(news);
                    bulkRequest.add(indexRequest);
                }
                BulkResponse bulkResponse = client.bulk(bulkRequest, RequestOptions.DEFAULT);
                System.out.println(bulkResponse.status().getStatus());
            }
        }
    }

    private static IndexRequest setIndexRequest(News news) {
        IndexRequest indexRequest = new IndexRequest("news");
        Map<String, Object> newsToInsert = new HashMap<>();
        newsToInsert.put("content", news.getContent());
        newsToInsert.put("title", news.getTitle());
        newsToInsert.put("url", news.getUrl());

        Random random = new Random();
        Instant instant = news.getCreatedAt().minusSeconds(random.nextInt(3600 * 24 * 3));

        newsToInsert.put("createdAt", instant);
        newsToInsert.put("updatedAt", instant);

        indexRequest.source(newsToInsert, XContentType.JSON);
        return indexRequest;
    }

    private static void updateSingleNewsToEs(RestHighLevelClient client, News news) throws IOException {
        IndexRequest indexRequest = setIndexRequest(news);
        IndexResponse response = client.index(indexRequest, RequestOptions.DEFAULT);
        System.out.println(response.status().getStatus());
    }
}
