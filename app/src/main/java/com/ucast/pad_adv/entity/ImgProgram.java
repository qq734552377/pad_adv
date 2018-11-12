package com.ucast.pad_adv.entity;

import com.ucast.pad_adv.tools.MyTools;

/**
 * Created by pj on 2018/11/1.
 */
public class ImgProgram {
    String url;
    String msg = "test";
    int Duration = 10000;

    public ImgProgram(String url) {
        this.url = url;
    }

    public ImgProgram(String url, String msg) {
        this.url = url;
        this.msg = msg;
    }

    public ImgProgram(String url, int duration) {
        this.url = url;
        Duration = duration;
    }

    public String getUrl() {
        String path = MyTools.isExitInSdcard(url);
        return path != null ? path : url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public int getDuration() {
        return Duration;
    }

    public void setDuration(int duration) {
        Duration = duration;
    }
}
