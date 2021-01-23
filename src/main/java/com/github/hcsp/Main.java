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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Main {
    public static void main(String[] args) throws IOException {
        // 待处理的链接池，包括新闻链接、博客、广告等等
        List<String> linkPoll = new ArrayList<>();
        // 放入的是，从linkPoll中取出的，已经处理(放入数据库)的链接池, 包括新浪首页和新浪新闻页面
        Set<String> processedLinksPoll = new HashSet<>();

        int articleCount = 0;
        linkPoll.add("http://sina.cn");

        // 没有待处理的链接了
        while (!linkPoll.isEmpty()) {

            String link = linkPoll.remove(linkPoll.size() - 1);

            if (processedLinksPoll.contains(link)) {
                // 已经被处理过的链接
                continue;
            }

            if (isInterestedLink(link)) {
                // 是新闻页面/sina首页/不是新闻页面跳转的登陆页面，那么处理
                System.out.println(link);
                Document document = httpGetAndParseHtmlDoc(link);
                // 把该网页包含的其他链接加入待处理链接池
                extractDocumentLinksToPoll(linkPoll, document);
                // 判断，如果该网页是一个新闻页面就存入数据库，否则什么也不做
                articleCount = storeNewsArticleTitleAndGetCount(articleCount, document);
                // 把当前链接加入已处理链接池
                processedLinksPoll.add(link);
            }
        }
        System.out.println(articleCount);
    }


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

    private static void extractDocumentLinksToPoll(List<String> linkPoll, Document document) {
        ArrayList<Element> links = document.select("a");
        for (Element aTag : links) {
            String refString = aTag.attr("href");
            if (!refString.startsWith("javascript") && !refString.startsWith("javaScript") && !("".equals(refString))) {
                linkPoll.add(refString);
            }
        }
    }

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
