package com.pratham.assessment.ui.choose_assessment.data_push_status;

import android.content.Context;

import com.pratham.assessment.database.AppDatabase;
import com.pratham.assessment.domain.Modal_Log;

import org.androidannotations.annotations.EBean;

import java.util.List;

@EBean
public class PushStatusPresenter implements PushStatusContract.PushStatusPresenter {
    Context context;
    PushStatusContract.PushStatusView pushStatusView;

    public PushStatusPresenter(Context context) {
        this.context = context;
    }

    @Override
    public void setView(PushStatusContract.PushStatusView pushStatusView) {
        this.pushStatusView = pushStatusView;
    }


    @Override
    public void getAllPushLog(String date) {
        List<Modal_Log> log = AppDatabase.getDatabaseInstance(context).getLogsDao().getPushLog("DATA_PUSH", "DB_PUSH", "%" + date + "%");
        pushStatusView.setPushStatusToRecycler(log);

    }

    @Override
    public void getRecentPushed() {
        Modal_Log recentDataPushed = AppDatabase.getDatabaseInstance(context).getLogsDao().getRecentPushed("DATA_PUSH");
        Modal_Log recentDBPushed = AppDatabase.getDatabaseInstance(context).getLogsDao().getRecentPushed("DB_PUSH");
        pushStatusView.setRecentPushed(recentDataPushed,recentDBPushed);


    }


}
