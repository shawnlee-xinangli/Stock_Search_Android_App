package com.example.myapplication;

public class NewsItem {
    public String title;
    public String url;
    public String publishedDate;
    public String source;
    public String imageUrl;

    public NewsItem(String title, String url, String publishedDate, String source,String imageUrl) {
        this.title = title;
        this.url = url;
        this.publishedDate = publishedDate;
        this.source = source;
        this.imageUrl = imageUrl;
    }

    @Override
    public String toString() {
        return "NewsItem{" +
                "title='" + title + '\'' +
                ", url='" + url + '\'' +
                ", publishedDate='" + publishedDate + '\'' +
                ", source='" + source + '\'' +
                ", imageUrl='" + imageUrl + '\'' +
                '}';
    }
}
