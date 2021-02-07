package com.github.lzp;

import org.apache.http.HttpHost;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.MultiMatchQueryBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class DataSearchEngine {
    public static void main(String[] args) throws IOException {

        System.out.println("请输入你的关键词:");
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
        String keywords = bufferedReader.readLine();
        searchInESDatabase(keywords);
    }

    private static void searchInESDatabase(String keywords) throws IOException {
        try (RestHighLevelClient client = new RestHighLevelClient(
                RestClient.builder(new HttpHost("localhost", 9200, "http")))
        ) {
            SearchRequest searchRequest = new SearchRequest("news");
            searchRequest.source(new SearchSourceBuilder().query(new MultiMatchQueryBuilder(keywords, "title", "content")));
            SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
            searchResponse.getHits().forEach(hit -> System.out.println(hit.getSourceAsString()));
        }
    }
}
