package com.github.lzp;

public class News {
    String link;
    String title;
    String content;

    public News(String link, String title, String content) {
        this.link = link;
        this.title = title;
        this.content = content;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
