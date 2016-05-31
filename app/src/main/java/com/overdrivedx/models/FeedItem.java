package com.overdrivedx.models;

/**
 * Created by babatundedennis on 1/28/15.
 */
public class FeedItem {
    private int id;
    private String body, images;

    public FeedItem() {
    }

    public FeedItem(int id,String body,
                     String images) {
        super();
        this.id = id;
        this.images = images;
        this.body = body;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }


    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }


    public String getImages() {
        return images;
    }

    public void setImages(String images) {
        this.images = images;
    }
}
