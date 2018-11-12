package com.ucast.pad_adv.entity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by pj on 2018/11/2.
 */
public class AdvPlayObj {
    boolean isVideo = false;
    String videoUrl;
    List<String> imgList;
    int duration = 10000;

    public AdvPlayObj() {
    }

    public AdvPlayObj(boolean isVideo, String videoUrl) {
        this.isVideo = isVideo;
        this.videoUrl = videoUrl;
    }

    public boolean isVideo() {
        return isVideo;
    }

    public void setVideo(boolean video) {
        isVideo = video;
        if (!video)
            imgList = new ArrayList<>();
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }

    public List<String> getImgList() {
        return imgList;
    }

    public void setImgList(List<String> imgList) {
        this.imgList = imgList;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }
}
