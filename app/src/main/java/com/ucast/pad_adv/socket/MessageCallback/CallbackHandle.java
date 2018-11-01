package com.ucast.pad_adv.socket.MessageCallback;

import android.os.Environment;
import android.util.Log;

import com.ucast.pad_adv.socket.Common;
import com.ucast.pad_adv.socket.Message.HeartBeat;
import com.ucast.pad_adv.socket.Message.MessageBase;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;

/**
 * Created by Administrator on 2016/2/4.
 */
public class CallbackHandle implements IMsgCallback {
    public static final String LOG_PATH = Environment.getExternalStorageDirectory().toString() + "/Ucast";

    public void Receive(Channel _channel, Object obj) {
        if (obj == null)
            return;
        if (!(obj instanceof MessageBase))
            return;
        MessageBase msgbase = (MessageBase) obj;
        switch (msgbase.Cmd) {
            case "1105":
                SendHeartBeat((HeartBeat) obj, _channel);
                break;
            default:
                break;
        }
    }
    private static final String TAG = "CallbackHandle";

    private void SendHeartBeat(HeartBeat msg, Channel channel) {
        String heart = "@1005," + "1223334444" + "$";
        ByteBuf resp = Unpooled.copiedBuffer(heart.getBytes());
        channel.writeAndFlush(resp);
    }

}
