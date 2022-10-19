drop table if exists `LINKS_TO_BE_PROCESSED`;
CREATE TABLE `LINKS_TO_BE_PROCESSED`
(
    link varchar(1000)
);
drop table if exists `LINKS_ALREADY_PROCESSED`;

CREATE TABLE `LINKS_ALREADY_PROCESSED`
(
    link varchar(1000)
);
drop table if exists `NEWS_RESULTS`;

CREATE TABLE `NEWS_RESULTS`
(
    id         bigint primary key auto_increment,
    title      text,
    content    text,
    url        varchar(1000),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) DEFAULT CHARSET=utf8mb4;

INSERT INTO LINKS_TO_BE_PROCESSED VALUES ('http://sina.cn');