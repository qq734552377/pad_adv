package com.ucast.pad_adv.socket;

import android.util.Base64;

import com.ucast.pad_adv.socket.Memory.NettyChannelMap;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;

/**
 * Created by Administrator on 2016/2/16.
 */
public class Common {
    public static String encode(byte[] bstr) {
        return Base64.encodeToString(bstr, Base64.DEFAULT);
    }
    /**
     * 解码
     *
     * @param str
     * @return string
     */
    public static byte[] decode(String str) {
        byte[] bt = new byte[100];
        try {

            bt = Base64.decode(str, Base64.DEFAULT);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return bt;
    }
    public static boolean ServicesAllSend(byte[] Data) {
        Set set = NettyChannelMap.ToList();
        boolean isSendOk=false;
        for (Iterator iter = set.iterator(); iter.hasNext(); ) {
            Map.Entry entry = (Map.Entry) iter.next();
            Channel value = (Channel) entry.getValue();
            if (value == null)
                return false;
            ByteBuf resp = Unpooled.copiedBuffer(Data);
            value.writeAndFlush(resp);
           isSendOk=true;
        }
        return isSendOk;
    }






}
