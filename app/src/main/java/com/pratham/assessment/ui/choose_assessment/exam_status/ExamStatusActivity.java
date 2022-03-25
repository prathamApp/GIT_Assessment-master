package com.pratham.assessment.ui.choose_assessment.exam_status;

import android.app.DatePickerDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.pratham.assessment.BaseActivity;
import com.pratham.assessment.R;
import com.pratham.assessment.custom.FastSave;
import com.pratham.assessment.domain.AssessmentPaperForPush;
import com.pratham.assessment.utilities.Assessment_Utility;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

import java.util.Calendar;
import java.util.List;

@EActivity(R.layout.activity_exam_status)
public class ExamStatusActivity extends BaseActivity implements ExamStatusContract.ExamStatusView {
    @Bean(ExamStatusPresenter.class)
    ExamStatusContract.ExamStatusPresenter examStatusPresenter;


    @ViewById(R.id.rv_push_status)
    RecyclerView rv_push_status;
    @ViewById(R.id.tv_status_title)
    TextView tv_status_title;
    @ViewById(R.id.cl_title_)
    LinearLayout cl_title_;

    @ViewById(R.id.tv_date_picker)
    TextView tv_date_picker;
    @ViewById(R.id.btn_date)
    TextView btn_date;
    @ViewById(R.id.tv_student_name_enrollment)
    TextView tv_student;

    @AfterViews
    public void init() {
        tv_date_picker.setVisibility(View.VISIBLE);
        examStatusPresenter.setView(this);
        cl_title_.setVisibility(View.VISIBLE);
        tv_status_title.setText("Exam status");
        tv_student.setVisibility(View.VISIBLE);
        tv_student.setText(FastSave.getInstance().getString("currentStudentName", "")
                + "(" + FastSave.getInstance().getString("currentStudentID", "") + ")");
        String date = Assessment_Utility.getCurrentDateTime().split(" ")[0];
        btn_date.setText(date);
        examStatusPresenter.getAllPapers(date);

    }

    @Click(R.id.btn_date)
    public void selectDate() {
        final Calendar cldr = Calendar.getInstance();
        int day = cldr.get(Calendar.DAY_OF_MONTH);
        int month = cldr.get(Calendar.MONTH);
        int year = cldr.get(Calendar.YEAR);
        // date picker dialog
        DatePickerDialog picker = new DatePickerDialog(ExamStatusActivity.this,
                (view, year1, monthOfYear, dayOfMonth) -> {
//                    btn_date.setText(dayOfMonth + "-" + (monthOfYear + 1) + "-" + year1);
                    btn_date.setText(String.format("%02d-%02d-%02d",
                            dayOfMonth, (monthOfYear + 1), year1));
                    examStatusPresenter.getAllPapers(btn_date.getText().toString());
                }, year, month, day);
        picker.show();
    }


    @Override
    public void setPushStatusToRecycler(List<AssessmentPaperForPush> paper) {
        ExamStatusAdapter adapter = new ExamStatusAdapter(this, paper);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        rv_push_status.setLayoutManager(mLayoutManager);
        rv_push_status.setAdapter(adapter);
    }
}
