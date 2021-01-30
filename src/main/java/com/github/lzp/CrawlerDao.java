package com.github.lzp;

import java.sql.SQLException;

public interface CrawlerDao {
    boolean isProcessedLink(String link) throws SQLException;

    void updateProcessedLinksTable(String link) throws SQLException;

    void updateLinksToBeProcessTable(String link) throws SQLException;

    String getNextUrlThenDelete() throws SQLException;

    void insertNewsIntoDatabase(String link, String title, String content) throws SQLException;
}
