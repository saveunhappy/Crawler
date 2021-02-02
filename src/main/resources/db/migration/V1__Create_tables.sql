CREATE
DATABASE IF NOT EXISTS news;

CREATE TABLE news.LINKS_TO_BE_PROCESSED
(
    link varchar(1000)
);

CREATE TABLE news.LINKS_ALREADY_PROCESSED
(
    link varchar(1000)
);

CREATE TABLE news.NEWS_RESULTS
(
    id         bigint primary key auto_increment,
    title      text,
    content    text,
    url        varchar(1000),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);