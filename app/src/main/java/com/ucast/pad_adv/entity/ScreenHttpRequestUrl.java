package com.ucast.pad_adv.entity;

import java.io.File;

/**
 * Created by pj on 2018/4/28.
 */
public class ScreenHttpRequestUrl {
    public static final String HOST = "http://58.246.122.118:9991";
//    public static final String HOST = "http://192.168.0.56:4422";
    public static final String DOWNLOADFILEURL = HOST + File.separator + "api/SendAdvertisin";
    public static final String DOWNLOADVEDIOURL = HOST + File.separator + "api/SendAdvertisng";
    public static final String TIMEUPDATEURL = HOST + File.separator + "Heart/HeartDetection";
}
