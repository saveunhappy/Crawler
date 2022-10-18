//package com.github.lzp;
//
//import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
//
//import java.sql.Connection;
//import java.sql.DriverManager;
//import java.sql.PreparedStatement;
//import java.sql.ResultSet;
//import java.sql.SQLException;
//
//@SuppressFBWarnings("DMI_CONSTANT_DB_PASSWORD")
//public class JdbcCrawlerDao implements CrawlerDao {
//    String jdbcUrl = "jdbc:h2:file:D:\\Project\\Crawler\\news";
//
//    Connection connection = DriverManager.getConnection(jdbcUrl, "root", "root");
//
//    public JdbcCrawlerDao() throws SQLException {
//    }
//
//    @Override
//    public boolean isProcessedLink(String link) throws SQLException {
//        ResultSet resultSet = null;
//        try (PreparedStatement statement =
//                     connection.prepareStatement("select * from LINKS_ALREADY_PROCESSED WHERE LINK = ?")) {
//            statement.setString(1, link);
//            resultSet = statement.executeQuery();
//            if (resultSet.next()) {
//                return true;
//            }
//        } finally {
//            if (resultSet != null) {
//                resultSet.close();
//            }
//        }
//        return false;
//    }
//
//    /**
//     * 将给定链接插入LINKS_TO_BE_PROCESSED表
//     *
//     * @param link 待处理链接
//     * @throws SQLException 数据库操作导致的异常
//     */
//    public void updateProcessedLinksTable(String link)
//            throws SQLException {
//        String sqlStatement = "INSERT INTO LINKS_TO_BE_PROCESSED VALUES (?)";
//        try (PreparedStatement statement = connection.prepareStatement(sqlStatement)) {
//            statement.setString(1, link);
//            statement.executeUpdate();
//        }
//    }
//
//    @Override
//    public void updateLinksToBeProcessTable(String link) throws SQLException {
//        String sqlStatement = "INSERT INTO LINKS_ALREADY_PROCESSED VALUES (?)";
//        try (PreparedStatement statement = connection.prepareStatement(sqlStatement)) {
//            statement.setString(1, link);
//            statement.executeUpdate();
//        }
//    }
//
//    public synchronized String getNextUrlThenDelete() throws SQLException {
//        String nextUrl;
//        try (PreparedStatement statement =
//                     connection.prepareStatement("SELECT * FROM LINKS_TO_BE_PROCESSED");
//             ResultSet resultSet = statement.executeQuery()) {
//            if (resultSet.next()) {
//                nextUrl = resultSet.getString(1);
//                // 从数据库中删除取出的链接
//                deleteLinksToBeProcessed(nextUrl);
//            } else {
//                nextUrl = "";
//            }
//        }
//        return nextUrl;
//    }
//
//    private void deleteLinksToBeProcessed(String nextUrl) throws SQLException {
//        String deleteSqlStatement = "DELETE FROM LINKS_TO_BE_PROCESSED WHERE LINK = ?";
//        try (PreparedStatement statement =
//                     connection.prepareStatement(deleteSqlStatement)) {
//            statement.setString(1, nextUrl);
//            statement.executeUpdate();
//        }
//    }
//
//
//    public void insertNewsIntoDatabase(String link, String title, String content) throws SQLException {
//        String sqlStatement = "INSERT INTO NEWS_RESULTS(title, url, content) VALUES(?, ?, ?)";
//        try (PreparedStatement statement = connection.prepareStatement(sqlStatement)) {
//            statement.setString(1, title);
//            statement.setString(2, link);
//            statement.setString(3, content);
//            statement.executeUpdate();
//        }
//    }
//}
