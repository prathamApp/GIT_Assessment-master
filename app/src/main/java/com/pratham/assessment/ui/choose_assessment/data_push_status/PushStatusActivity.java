package com.pratham.assessment.ui.choose_assessment.data_push_status;

import android.app.DatePickerDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.widget.TextView;

import com.pratham.assessment.BaseActivity;
import com.pratham.assessment.R;
import com.pratham.assessment.domain.Modal_Log;
import com.pratham.assessment.utilities.Assessment_Utility;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

import java.util.Calendar;
import java.util.List;

@EActivity(R.layout.activity_push_status)
public class PushStatusActivity extends BaseActivity implements PushStatusContract.PushStatusView {
    @Bean(PushStatusPresenter.class)
    PushStatusContract.PushStatusPresenter pushStatusPresenter;
    @ViewById(R.id.tv_date_picker)
    TextView tv_date_picker;
    @ViewById(R.id.btn_date)
    TextView btn_date;

    @ViewById(R.id.tv_recent_db_pushed)
    TextView tv_recent_db_pushed;

    @ViewById(R.id.tv_recent_data_pushed)
    TextView tv_recent_data_pushed;


    @ViewById(R.id.rv_push_status)
    RecyclerView rv_push_status;

    @AfterViews
    public void init() {
        pushStatusPresenter.setView(this);
        String date = Assessment_Utility.getCurrentDateTime().split(" ")[0];
        btn_date.setText(date);
        pushStatusPresenter.getRecentPushed();
        pushStatusPresenter.getAllPushLog(date);
    }

    @Click(R.id.btn_date)
    public void selectDate() {
        final Calendar cldr = Calendar.getInstance();
        int day = cldr.get(Calendar.DAY_OF_MONTH);
        int month = cldr.get(Calendar.MONTH);
        int year = cldr.get(Calendar.YEAR);
        // date picker dialog
        DatePickerDialog picker = new DatePickerDialog(PushStatusActivity.this,
                (view, year1, monthOfYear, dayOfMonth) -> {
//                    btn_date.setText(dayOfMonth + "-" + (monthOfYear + 1) + "-" + year1);
                    btn_date.setText(String.format("%02d-%02d-%02d",
                            dayOfMonth, (monthOfYear + 1), year1));
                    pushStatusPresenter.getAllPushLog(btn_date.getText().toString());

                }, year, month, day);
        picker.show();
    }

    @Override
    public void setPushStatusToRecycler(List<Modal_Log> log) {
        PushDataSummaryAdapter adapter = new PushDataSummaryAdapter(this, log);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        rv_push_status.setLayoutManager(mLayoutManager);
        rv_push_status.setAdapter(adapter);
    }

    @Override
    public void setRecentPushed(Modal_Log recentDataPushed, Modal_Log recentDBPushed) {
        if (recentDataPushed != null)
            tv_recent_data_pushed.setText(Html.fromHtml("<b>Recent data pushed</b>" + "<br/>" + recentDataPushed.getCurrentDateTime() + "<br/>" + recentDataPushed.getMethodName()));
        if (recentDBPushed != null)
            tv_recent_db_pushed.setText(Html.fromHtml("<b>Recent db pushed</b>" + "<br/>" + recentDBPushed.getCurrentDateTime() + "<br/>" + recentDBPushed.getMethodName()));
    }
}
