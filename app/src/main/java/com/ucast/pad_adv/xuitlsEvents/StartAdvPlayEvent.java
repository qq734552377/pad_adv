package com.ucast.pad_adv.xuitlsEvents;

/**
 * Created by pj on 2018/11/2.
 */
public class StartAdvPlayEvent {
    boolean isRestart = false;

    public StartAdvPlayEvent() {
    }

    public StartAdvPlayEvent(boolean isRestart) {
        this.isRestart = isRestart;
    }

    public boolean isRestart() {
        return isRestart;
    }

    public void setRestart(boolean restart) {
        isRestart = restart;
    }
}
