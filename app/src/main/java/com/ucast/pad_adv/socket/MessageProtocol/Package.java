package com.ucast.pad_adv.socket.MessageProtocol;


import com.ucast.pad_adv.socket.Message.MessageBase;
import com.ucast.pad_adv.socket.MessageCallback.IMsgCallback;

import io.netty.channel.Channel;

/**
 * Created by Administrator on 2016/2/3.
 */

public abstract  class Package {

    private Channel channel;

    public IMsgCallback callback;

    protected boolean mDispose;

    public Package(Channel _channel) {
        channel = _channel;
    }

    public abstract void Import(byte[] buffer, int offset, int count) throws Exception;

    public abstract MessageBase MessageRead(byte[] data);

    public abstract MessageBase MessageRead(String str) throws Exception;

    protected void OnMessageDataReader(String str) throws Exception {
        MessageBase mesaage = MessageRead(str);
        if (mesaage == null)
            return;
        MessageReceived(mesaage);
    }

    protected void MessageReceived(MessageBase messageBase) {
        synchronized (this) {
            if (callback == null)
                return;
            callback.Receive(channel, messageBase);
        }
    }

    public void Dispose() {
        synchronized (this) {
            if (mDispose)
                return;
            mDispose = true;
            callback = null;
        }
    }
}