package com.github.lzp;

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
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.stream.Collectors;

public class Crawler {
    private final CrawlerDao dao = new JdbcCrawlerDao();

    public Crawler() throws SQLException {
    }

    public static void main(String[] args) throws IOException, SQLException {
        new Crawler().run();
    }

    public void run() throws IOException, SQLException {

        // 需爬虫的主页
        String startPage = "http://sina.cn";
        // 将主页插入待处理链接数据库
        dao.updateTableInDatabase(startPage, " INSERT INTO LINKS_TO_BE_PROCESSED VALUES (?)");

        String link;
        while (!("".equals(link = dao.getNextUrlThenDelete()))) {
            // 查询当前连接是否为已经被处理过的链接，是就跳过处理下一条
            if (dao.isProcessedLink(link)) {
                continue;
            }
            // 是新闻页面/sina首页/不是新闻页面跳转的登陆页面，那么处理
            if (isInterestedLink(link, startPage)) {
                // 遍历网页
                Document document = httpGetAndParseHtml(link);
                // 把该网页包含的其他链接加入待处理链接池
                extractOtherLinksInDoc(document);
                // 判断，如果该网页是一个新闻页面就操作一下（打印url和标题），否则什么也不做
                System.out.println(link);
                getTitleAndInsertIntoNewsDatabase(document, link);
                // 把当前链接加入已处理链接池
                dao.updateTableInDatabase(link, "INSERT INTO LINKS_ALREADY_PROCESSED VALUES (?)");
            }
        }
        System.out.println("已完成爬取!!!");
    }


    /**
     * 将传入网页中包含的所有链接加入待处理链接的数据库
     *
     * @param document 需要抽取链接的网页，Document格式
     */
    private void extractOtherLinksInDoc(Document document)
            throws SQLException {
        ArrayList<Element> links = document.select("a");
        for (Element aTag : links) {
            String refString = aTag.attr("href");
            if (refString.startsWith("//")) {
                refString = "https:" + refString;
            }
            if (!refString.startsWith("#") && !refString.startsWith("javascript")
                    && !refString.startsWith("javaScript") && !("".equals(refString))) {
                dao.updateTableInDatabase(refString,
                        "INSERT INTO LINKS_TO_BE_PROCESSED VALUES (?)");
            }
        }
    }

    /**
     * 用jsoup遍历链接，返回一个Document对象
     *
     * @param link String格式的网页链接
     * @return Document结构，里面包含了遍历后的网页html内容
     * @throws IOException execute()、toString()函数引起的
     */
    private static Document httpGetAndParseHtml(String link) throws IOException {
        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpGet httpGet = new HttpGet(link);
        httpGet.addHeader("User-Agent",
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:84.0) Gecko/20100101 Firefox/84.0");

        try (CloseableHttpResponse response1 = httpclient.execute(httpGet)) {
            HttpEntity entity1 = response1.getEntity();
            String html = EntityUtils.toString(entity1);
            // 使用jsoup遍历html
            return Jsoup.parse(html);
        }
    }

    /**
     * 处理某一个选出来的新闻页面，目前是把新闻的文章标题和链接打印出来
     *
     * @param document 待处理的新闻页面
     */
    private void getTitleAndInsertIntoNewsDatabase(Document document, String link)
            throws SQLException {
        // 有article的页面是新闻文章页
        String title;
        String content;
        ArrayList<Element> articleTags = document.select("article");
        if (!articleTags.isEmpty()) {
            for (Element articleTag : articleTags) {
                // 选中文章标题
                title = articleTag.child(0).text();
                System.out.println(title);
                content = articleTag.select("p").stream().map(Element::text).collect(Collectors.joining("\n"));
                dao.insertNewsIntoDatabase(link, title, content);
            }
        }
    }

    private static boolean isInterestedLink(String link, String startPage) {
        return isNotLoginPage(link) && (isNewsPage(link) || isIndexPage(link, startPage));
    }

    private static boolean isIndexPage(String link, String startPage) {
        return link.equals(startPage);
    }

    private static boolean isNewsPage(String link) {
        return link.contains("news.sina.cn");
    }

    private static boolean isNotLoginPage(String link) {
        return !link.contains("passport.sina.cn");
    }
}
