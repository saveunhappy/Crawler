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
        String jdbcUrl = "jdbc:h2:file:D:\\Project\\Crawler\\news";
        Connection connection = DriverManager.getConnection(jdbcUrl, "root", "root");

        // 需爬虫的主页
        String startPage = "http://sina.cn";
        // 将主页插入待处理链接数据库
        UpdateTableInDatabase(connection, startPage, " INSERT INTO LINKS_TO_BE_PROCESSED VALUES (?)");

        String link;
        while (!("".equals(link = getUrlWaitsForProcessPoll(connection)))) {
            // 从数据库中删除取出的链接
            UpdateTableInDatabase(connection, link, "DELETE FROM LINKS_TO_BE_PROCESSED WHERE LINK = ?");
            // 查询当前连接是否为已经被处理过的链接，是就跳过处理下一条
            if (isProcessedLink(connection, link)) {
                continue;
            }

            if (isInterestedLink(link, startPage)) {
                // 是新闻页面/sina首页/不是新闻页面跳转的登陆页面，那么处理
                Document document = httpGetAndParseHtmlDoc(link);
                // 把该网页包含的其他链接加入待处理链接池
                extractDocumentLinksToDatabase(document, connection);
                // 判断，如果该网页是一个新闻页面就操作一下（打印url和标题），否则什么也不做
                System.out.println(link);
                getTitleAndInsertIntoDatabase(document);
                // 把当前链接加入已处理链接池
                UpdateTableInDatabase(connection, link, "INSERT INTO LINKS_ALREADY_PROCESSED VALUES (?)");
            }
        }
        System.out.println("已完成爬取!!!");
    }

    private static boolean isProcessedLink(Connection connection, String link) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("select * from LINKS_ALREADY_PROCESSED WHERE LINK = ?")) {
            statement.setString(1, link);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return true;
            }
        }
        return false;
    }

    /**
     * 传入一个带一个参数的sql语句
     *
     * @param connection   数据库连接
     * @param link         待处理链接
     * @param sqlStatement 数据库语句
     * @throws SQLException 数据库操作导致的异常
     */
    private static void UpdateTableInDatabase(Connection connection, String link, String sqlStatement) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(sqlStatement)) {
            statement.setString(1, link);
            statement.executeUpdate();
        }
    }

    private static String getUrlWaitsForProcessPoll(Connection connection) throws SQLException {
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
     * @param document 待处理的新闻页面
     * @return 返回articleCount，目前处理的新闻文章页面数
     */
    private static String getTitleAndInsertIntoDatabase(Document document) {
        // 有article的页面是新闻文章页
        String title = null;
        ArrayList<Element> articleTags = document.select("article");
        if (!articleTags.isEmpty()) {
            for (Element articleTag : articleTags) {
                // 选中文章标题
                title = articleTag.child(0).text();
                System.out.println(title);
            }
        }
        return title;
    }

    /**
     * 将传入网页中包含的所有链接加入待处理链接的数据库
     *
     * @param document 需要抽取链接的网页，Document格式
     */
    private static void extractDocumentLinksToDatabase(Document document, Connection connection) throws SQLException {
        ArrayList<Element> links = document.select("a");
        for (Element aTag : links) {
            String refString = aTag.attr("href");
            if (!refString.startsWith("javascript") && !refString.startsWith("javaScript") && !("".equals(refString))) {
                UpdateTableInDatabase(connection, refString, "INSERT INTO LINKS_TO_BE_PROCESSED VALUES (?)");
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
