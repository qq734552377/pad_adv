package com.ucast.pad_adv.jsonObj;

/**
 * Created by pj on 2018/8/24.
 */
public class BaseAdvResult {
    private String Url;
    private String IsVideo;
    //单位为秒
    private int Duration;

    public String getUrl() {
        return Url;
    }

    public void setUrl(String url) {
        Url = url;
    }


    public boolean getIsVideo() {
        if (IsVideo != null && IsVideo.equals("true"))
            return true;
        return false;
    }

    public void setIsVideo(String isVideo) {
        IsVideo = isVideo;
    }


    public int getDuration() {
        if(Duration <= 0)
            return 5000;
        return Duration * 1000;
    }

    public void setDuration(int duration) {
        Duration = duration;
    }
}
