package com.ucast.pad_adv.avi_activity;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.MediaController;
import android.widget.VideoView;

import com.alibaba.fastjson.JSON;
import com.ucast.pad_adv.R;
import com.ucast.pad_adv.adv_activity.AdvActivity;
import com.ucast.pad_adv.entity.Config;
import com.ucast.pad_adv.entity.ScreenHttpRequestUrl;
import com.ucast.pad_adv.jsonObj.BaseAdvResult;
import com.ucast.pad_adv.jsonObj.BaseHttpResult;
import com.ucast.pad_adv.jsonObj.ImgAdvResult;
import com.ucast.pad_adv.tools.ExceptionApplication;
import com.ucast.pad_adv.tools.MyTools;
import com.ucast.pad_adv.tools.SavePasswd;
import com.ucast.pad_adv.xuitlsEvents.VideoEvent;

import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.x;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;
import de.greenrobot.event.Subscribe;
import de.greenrobot.event.ThreadMode;

public class AviActivity extends AppCompatActivity {

    VideoView vv ;
    public static List<String> paths = new ArrayList<>();
    public static int v_index = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //无title
        requestWindowFeature(android.view.Window.FEATURE_NO_TITLE);
        //全屏
        getWindow().setFlags(WindowManager.LayoutParams. FLAG_FULLSCREEN ,
                WindowManager.LayoutParams. FLAG_FULLSCREEN);
        setContentView(R.layout.activity_avi);


        initPaths();

        vv = findViewById(R.id.vv);

        vv.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                v_index++;
                setVideoPath(paths.get(v_index%paths.size()));
                startAdv();
            }
        });

        vv.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                vv.start();
            }
        });

        vv.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
                paths.remove(v_index%paths.size());
                setVideoPath(paths.get(v_index%paths.size()));
//                showToast("视屏不能播放");
                return true;
            }
        });

        EventBus.getDefault().register(this);
        setVideoPath(paths.get(v_index));
    }

    protected void hideBottomUIMenu() {
        //隐藏虚拟按键，并且全屏
        if (Build.VERSION.SDK_INT > 11 && Build.VERSION.SDK_INT < 19) { // lower api
            View v = this.getWindow().getDecorView();
            v.setSystemUiVisibility(View.GONE);
        } else if (Build.VERSION.SDK_INT >= 19) {
            //for new api versions.
            View decorView = getWindow().getDecorView();
            int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_FULLSCREEN;
            decorView.setSystemUiVisibility(uiOptions);
        }
    }

    private void initPaths() {
        paths.add(Environment.getExternalStorageDirectory().toString() + "/advice2.mp4");
        paths.add(Environment.getExternalStorageDirectory().toString() + "/advice1.mp4");
        paths.add("http://flashmedia.eastday.com/newdate/news/2016-11/shznews1125-19.mp4");
        paths.add("http://www.jmzsjy.com/UploadFile/微课/地方风味小吃——宫廷香酥牛肉饼.mp4");
        paths.add("http://112.253.22.157/17/z/z/y/u/zzyuasjwufnqerzvyxgkuigrkcatxr/hc.yinyuetai.com/D046015255134077DDB3ACA0D7E68D45.flv");
//        paths.add(Environment.getExternalStorageDirectory().toString() + "/Ucast/test.mp4");
        String video_url_base64 = SavePasswd.getInstace().get(SavePasswd.ADVVIDEOURL);
        if (!video_url_base64.equals("")){
            paths.clear();
            String[] video_urls = video_url_base64.split(",");
            for (int i = 0; i < video_urls.length; i++) {
                String one = video_urls[i];
                paths.add(new String(MyTools.decode(one)));
            }
        }

    }

    private void setVideoPath(String path) {
        Uri uri = Uri.parse(path);//将路径转换成uri
        vv.setVideoURI(uri);//为视频播放器设置视频路径
//        vv.setMediaController(new MediaController(AviActivity.this));//显示控制
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (vv != null)
            vv.start();
        getAdvs();
        hideBottomUIMenu();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (vv != null && vv.isPlaying())
            vv.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MainThread, sticky = true)
    public void changePaths(VideoEvent videoEvent){
        if (paths.size() < 0)
            return;
        String path = videoEvent.getPath();
        for (int i = 0; i < paths.size(); i++) {
            if (MyTools.getFileNameByUrl(path).equals(MyTools.getFileNameByUrl(paths.get(i))))
                paths.set(i,path);
        }
    }

    public void getAdvs(){
        RequestParams params = new RequestParams(ScreenHttpRequestUrl.DOWNLOADVEDIOURL);
        params.addBodyParameter("DeviceID", Config.DEVICE_ID);
        params.setConnectTimeout(1000 * 45);
        x.http().post(params, new Callback.CommonCallback<String>() {
            @Override
            public void onSuccess(String result) {
                BaseHttpResult base = JSON.parseObject(result, BaseHttpResult.class);
                if (base.getMsgType().equals(BaseHttpResult.SUCCESS) && base.getData() != null && !base.getData().equals("")){
                    BaseAdvResult baseAdvResult = JSON.parseObject(base.getData(),BaseAdvResult.class);
                    List<ImgAdvResult> imgAdvResults = JSON.parseArray(baseAdvResult.getImg(),ImgAdvResult.class);
                    if (imgAdvResults.size() > 0){
                        paths.clear();
                        StringBuffer vedio_url_sb = new StringBuffer();
                        for (int i = 0; i <imgAdvResults.size() ; i++) {
                            ImgAdvResult one = imgAdvResults.get(i);
                            String url = one.getImgurl();
                            String path = MyTools.isVideoExitInSdcard(url);
                            if (path != null){
                                url = path;
                            }
                            paths.add(url);
                            String img_url_base64 = MyTools.encode(url.getBytes()).replace("\n","");
                            vedio_url_sb.append(img_url_base64);
                            if (i < imgAdvResults.size() -1 ){
                                vedio_url_sb.append(",");
                            }
                        }
                        String save_adv_urls = SavePasswd.getInstace().get(SavePasswd.ADVVIDEOURL);
                        String get_adv_urls = vedio_url_sb.toString();
                        if (save_adv_urls.equals(get_adv_urls)){
                            return;
                        }
                        SavePasswd.getInstace().save(SavePasswd.ADVVIDEOURL,get_adv_urls);
                        v_index = 0;
                        setVideoPath(paths.get(v_index));
                    }
                }

            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
//                showToast("没有获取到服务器广告数据！");
            }

            @Override
            public void onCancelled(CancelledException cex) {

            }

            @Override
            public void onFinished() {

            }
        });
    }
    public void startAdv(){
        Intent start_adv_intent = new Intent(this, AdvActivity.class);
        start_adv_intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);  //注意，必须添加这个标记，否则启动会失败
        this.startActivity(start_adv_intent);
    }
}
