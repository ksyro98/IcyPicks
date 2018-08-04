package com.icypicks.www.icypicks.java_classes;


import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

/**
 * This class represents a user.
 * Each user has a name, a favorite ice cream flavor, an email address, some info
 * and an ArrayList containing his uploads (the uploadNumber of the ice cream class)
 * Each user also has a profile image, but the profile image is saved in the firebase storage.
 * (instead of the firebase database), so it the User class has no field for it.
 * Each user also has a user id, given to him from the firebase authentication.
 */
public class User implements Parcelable {
    private String name;
    private String favoriteFlavor;
    private String email;
    private String info;
    private ArrayList<Integer> uploads;

    public User(){

    }

    public User(String name, String favoriteFlavor, String email, String info, ArrayList<Integer> uploads) {
        this.name = name;
        this.favoriteFlavor = favoriteFlavor;
        this.email = email;
        this.info = info;
        this.uploads = uploads;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFavoriteFlavor() {
        return favoriteFlavor;
    }

    public void setFavoriteFlavor(String favoriteFlavor) {
        this.favoriteFlavor = favoriteFlavor;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public ArrayList<Integer> getUploads() {
        return uploads;
    }

    public void setUploads(ArrayList<Integer> uploads) {
        this.uploads = uploads;
    }

    public void addUpload(int numberOfUpload){
        if(uploads == null){
            this.uploads = new ArrayList<>();
        }
        this.uploads.add(numberOfUpload);
    }

    public String getStringOfUploads(){
        StringBuilder stringBuilder = new StringBuilder();
        if(uploads == null){
            return null;
        }
        for(Integer uploadNumber : uploads){
            stringBuilder.append(String.valueOf(uploadNumber)).append("_");
        }
        return stringBuilder.toString();
    }

    protected User(Parcel in) {
        name = in.readString();
        favoriteFlavor = in.readString();
        email = in.readString();
        info = in.readString();
        if (in.readByte() == 0x01) {
            uploads = new ArrayList<Integer>();
            in.readList(uploads, Integer.class.getClassLoader());
        } else {
            uploads = null;
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(favoriteFlavor);
        dest.writeString(email);
        dest.writeString(info);
        if (uploads == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeList(uploads);
        }
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<User> CREATOR = new Parcelable.Creator<User>() {
        @Override
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };
}