package com.github.lzp;

import java.sql.SQLException;

public interface CrawlerDao {
    boolean isProcessedLink(String link) throws SQLException;

    void UpdateTableInDatabase(String link, String sqlStatement) throws SQLException;

    String getNextUrlThenDelete() throws SQLException;

    void insertNewsIntoDatabase(String link, String title, String content) throws SQLException;
}
