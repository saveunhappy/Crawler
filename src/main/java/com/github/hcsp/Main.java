package com.github.hcsp;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class Main {
    public static void main(String[] args) throws IOException, SQLException {
        int articleCount = 0;
        String jdbcUrl = "jdbc:h2:file:D:\\Project\\Crawler\\news";
        Connection connection = DriverManager.getConnection(jdbcUrl, "root", "root");

        // 需爬虫的主页
        String startString = "http://sina.cn";
        // 将主页插入待处理链接数据库
        try (PreparedStatement statement = connection.prepareStatement(" INSERT INTO LINKS_TO_BE_PROCESSED VALUES (?)")) {
            statement.setString(1, startString);
            statement.executeUpdate();
        }

        // 没有待处理的链接了
        String link;
        while (!("".equals(link = getLinkWaitsForProcessPoll(connection)))) {
            // 从数据库中删除取出的链接
            try (PreparedStatement statement = connection.prepareStatement("DELETE FROM LINKS_TO_BE_PROCESSED WHERE LINK = ?")) {
                statement.setString(1, link);
                statement.executeUpdate();
            }

            // 查询当前连接是否为已经被处理过的链接，是就跳过处理下一条
            try (PreparedStatement statement = connection.prepareStatement("select * from LINKS_ALREADY_PROCESSED WHERE LINK = ?")) {
                statement.setString(1, link);
                ResultSet resultSet = statement.executeQuery();
                boolean existFlag = false;
                if (resultSet.next()) {
                    existFlag = true;
                }
                if (existFlag) {
                    continue;
                }
            }

            if (isInterestedLink(link)) {
                // 是新闻页面/sina首页/不是新闻页面跳转的登陆页面，那么处理
                System.out.println(link);
                Document document = httpGetAndParseHtmlDoc(link);
                // 把该网页包含的其他链接加入待处理链接池
                extractDocumentLinksToPoll(document, connection);
                // 判断，如果该网页是一个新闻页面就操作一下（加入news数据库），否则什么也不做
                articleCount = storeNewsArticleTitleAndGetCount(articleCount, document);
                // 把当前链接加入已处理链接池
                try (PreparedStatement statement = connection.prepareStatement("INSERT INTO LINKS_ALREADY_PROCESSED VALUES (?)")) {
                    statement.setString(1, link);
                    statement.executeUpdate();
                }
            }
        }
        System.out.println(articleCount);
    }

    private static String getLinkWaitsForProcessPoll(Connection connection) throws SQLException {
        String result;
        try (PreparedStatement statement = connection.prepareStatement("SELECT * FROM LINKS_TO_BE_PROCESSED")) {
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                result = resultSet.getString(1);
            } else {
                result = "";
            }
        }
        return result;
    }

    /**
     * 处理某一个选出来的新闻页面，目前是把新闻的文章标题和链接打印出来
     *
     * @param articleCount 目前处理的新闻文章页面数
     * @param document     待处理的新闻页面
     * @return 返回articleCount，目前处理的新闻文章页面数
     */
    private static int storeNewsArticleTitleAndGetCount(int articleCount, Document document) {
        // 有article的页面是新闻文章页
        ArrayList<Element> articleTags = document.select("article");
        if (!articleTags.isEmpty()) {
            articleCount++;
            for (Element articleTag : articleTags) {
                // 选中文章标题
                String title = articleTag.child(0).text();
                System.out.println(title);
            }
        }
        return articleCount;
    }

    /**
     * 将传入网页中包含的所有链接加入待处理链接的数据库
     *
     * @param document 需要抽取链接的网页，Document格式
     */
    private static void extractDocumentLinksToPoll(Document document, Connection connection) throws SQLException {
        ArrayList<Element> links = document.select("a");
        for (Element aTag : links) {
            String refString = aTag.attr("href");
            if (!refString.startsWith("javascript") && !refString.startsWith("javaScript") && !("".equals(refString))) {
                try (PreparedStatement statement = connection.prepareStatement("INSERT INTO LINKS_TO_BE_PROCESSED VALUES (?)")) {
                    statement.setString(1, refString);
                    statement.executeUpdate();
                }
            }
        }
    }

    /**
     * 用jsoup遍历链接，返回一个Ducument对象
     *
     * @param link String格式的网页链接
     * @return Ducument结构，里面包含了遍历后的网页html内容
     * @throws IOException execute()、toString()函数引起的
     */
    private static Document httpGetAndParseHtmlDoc(String link) throws IOException {
        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpGet httpGet = new HttpGet(link);
        httpGet.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:84.0) Gecko/20100101 Firefox/84.0");

        try (CloseableHttpResponse response1 = httpclient.execute(httpGet)) {
            HttpEntity entity1 = response1.getEntity();
            String html = EntityUtils.toString(entity1);
            // 使用jsoup遍历html
            return Jsoup.parse(html);
        }
    }

    private static boolean isInterestedLink(String link) {
        return isNotLoginPage(link) && (isNewsPage(link) || isIndexPage(link));
    }

    private static boolean isIndexPage(String link) {
        String indexPage = "http://sina.cn";
        return link.equals(indexPage);
    }

    private static boolean isNewsPage(String link) {
        return link.contains("news.sina.cn");
    }

    private static boolean isNotLoginPage(String link) {
        return !link.contains("passport.sina.cn");
    }
}
