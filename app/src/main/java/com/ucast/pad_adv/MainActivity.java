package com.ucast.pad_adv;
import com.ucast.pad_adv.R;
import android.app.Activity;
import android.app.KeyguardManager;
import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.ucast.pad_adv.adv_activity.AdvActivity;
import com.ucast.pad_adv.avi_activity.AviActivity;
import com.ucast.pad_adv.tools.ApManager;
import com.ucast.pad_adv.xuitlsEvents.StartAdvPlayEvent;
import com.ucast.pad_adv.xuitlsEvents.UpdateAdvEvent;

import de.greenrobot.event.EventBus;

public class MainActivity extends AppCompatActivity {
//    DevicePolicyManager policyManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent ootStartIntent = new Intent(this, UpdateService.class);
        ootStartIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        this.startService(ootStartIntent);
//        policyManager = (DevicePolicyManager) MainActivity.this.getSystemService(Context.DEVICE_POLICY_SERVICE);
        findViewById(R.id.bt_update).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EventBus.getDefault().postSticky(new UpdateAdvEvent());
//
//                if (policyManager != null)
//                    policyManager.lockNow();

            }
        });
        findViewById(R.id.bt_avi).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EventBus.getDefault().postSticky(new StartAdvPlayEvent(false));
            }
        });


    }
}
