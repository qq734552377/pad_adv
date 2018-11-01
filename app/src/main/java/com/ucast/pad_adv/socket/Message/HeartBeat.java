package com.ucast.pad_adv.socket.Message;

/**
 * Created by Administrator on 2016/3/31.
 */
public class HeartBeat extends MessageBase {
    public String state;
    public void Load(String[] str) {
        super.Load(str);
        Cmd = str[0];
        state =str[1];

    }
}
