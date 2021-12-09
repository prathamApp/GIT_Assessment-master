package com.pratham.assessment.ui.choose_assessment.exam_status;

import android.content.Context;

import com.pratham.assessment.custom.FastSave;
import com.pratham.assessment.database.AppDatabase;
import com.pratham.assessment.domain.AssessmentPaperForPush;

import org.androidannotations.annotations.EBean;

import java.util.List;

@EBean
public class ExamStatusPresenter implements ExamStatusContract.ExamStatusPresenter {
    Context context;
    ExamStatusContract.ExamStatusView examStatusView;

    public ExamStatusPresenter(Context context) {
        this.context = context;
    }

    @Override
    public void setView(ExamStatusContract.ExamStatusView examStatusView) {
        this.examStatusView = examStatusView;
    }


    @Override
    public void getAllPapers(String date) {
        String currentStudentID = FastSave.getInstance().getString("currentStudentID", "");
        List<AssessmentPaperForPush> paper = AppDatabase.getDatabaseInstance(context)
                .getAssessmentPaperForPushDao().getAssessmentPaperByStudentIdPaperDate(currentStudentID, "%" + date + "%");
        examStatusView.setPushStatusToRecycler(paper);

    }


}
