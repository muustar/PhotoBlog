package com.example.feco.photoblog;

import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

public class BlogPost extends BlogPostId {
    private String title, desc, image, thumb, uid;
    @ServerTimestamp
    private Date timestamp;


    public BlogPost(String title, String desc, String image, String thumb, String uid, Date timestamp) {
        this.title = title;
        this.desc = desc;
        this.image = image;
        this.thumb = thumb;
        this.uid = uid;
        this.timestamp = timestamp;
    }


    public BlogPost() {

    }

    public String getThumb() {
        return thumb;
    }

    public void setThumb(String thumb) {
        this.thumb = thumb;
    }


    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }
}
