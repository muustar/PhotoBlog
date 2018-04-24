package com.example.feco.photoblog;

public class Users {
    private String image, image_thumb, name, status;

    public  Users(){}

    public Users(String image, String image_thumb, String name, String status) {
        this.image = image;
        this.image_thumb = image_thumb;
        this.name = name;
        this.status = status;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getImage_thumb() {
        return image_thumb;
    }

    public void setImage_thumb(String image_thumb) {
        this.image_thumb = image_thumb;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
