package com.icypicks.www.icypicks.java_classes;

import java.util.ArrayList;

public class IceCream {
    private String flavor;
    private String place;
    private String description;
    private ArrayList<String> usersComments;
    private String imageUrl;
    private int uploadNumber;
    private byte[] imageBytes;

    public IceCream(){

    }

    public IceCream(String flavor, String place, String description, ArrayList<String> usersComments, String imageUrl) {
        this.flavor = flavor;
        this.place = place;
        this.description = description;
        this.usersComments = usersComments;
        this.imageUrl = imageUrl;
    }

    public String getFlavor() {
        return flavor;
    }

    public void setFlavor(String flavor) {
        this.flavor = flavor;
    }

    public String getPlace() {
        return place;
    }

    public void setPlace(String place) {
        this.place = place;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ArrayList<String> getUsersComments() {
        return usersComments;
    }

    public void setUsersComments(ArrayList<String> usersComments) {
        this.usersComments = usersComments;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public int getUploadNumber() {
        return uploadNumber;
    }

    public void setUploadNumber(int uploadNumber) {
        this.uploadNumber = uploadNumber;
    }

    public byte[] getImageBytes() {
        return imageBytes;
    }

    public void setImageBytes(byte[] imageBytes) {
        this.imageBytes = imageBytes;
    }
}