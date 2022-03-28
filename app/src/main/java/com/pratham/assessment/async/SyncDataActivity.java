package com.pratham.assessment.async;

import android.support.v7.app.AppCompatActivity;

import com.pratham.assessment.R;
import com.pratham.assessment.interfaces.DataPushListener;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EActivity;


/**
 * this activity is called when data push is called from other apps(PDS, PDL)
 * */
@EActivity(R.layout.activity_sync_data)
public class SyncDataActivity extends AppCompatActivity implements DataPushListener {
    @Bean(PushDataToServer.class)
    PushDataToServer pushDataToServer;

    @AfterViews
    public void init() {
        pushDataToServer.setValue(this, false);
        pushDataToServer.doInBackground();
    }

    @Override
    public void onResponseGet() {
        finish();
    }

}
