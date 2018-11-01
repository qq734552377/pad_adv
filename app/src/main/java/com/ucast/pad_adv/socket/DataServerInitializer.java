package com.ucast.pad_adv.socket;


import com.ucast.pad_adv.socket.MessageCallback.CallbackHandle;
import com.ucast.pad_adv.socket.MessageCallback.IMsgCallback;
import com.ucast.pad_adv.socket.MessageProtocol.StationPackage;

import java.util.concurrent.TimeUnit;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.handler.timeout.IdleStateHandler;

/**
 * Created by Administrator on 2016/2/3.
 */
public class DataServerInitializer extends ChannelInitializer {

    public IMsgCallback callback;

    public DataServerInitializer() {
        callback = new CallbackHandle();
    }

    public void initChannel(Channel channel) {
        StationPackage stationPackage = new StationPackage(channel);
        stationPackage.callback = callback;
        TcpServerHandle handle = new TcpServerHandle(stationPackage);
        channel.pipeline().addLast("idleStateHandler", new IdleStateHandler(0, 0, 300000, TimeUnit.MILLISECONDS));
        channel.pipeline().addLast("handler", handle);

    }
}
