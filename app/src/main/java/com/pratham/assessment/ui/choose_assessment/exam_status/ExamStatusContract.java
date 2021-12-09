package com.pratham.assessment.ui.choose_assessment.exam_status;

import com.pratham.assessment.domain.AssessmentPaperForPush;

import java.util.List;

public interface ExamStatusContract {
    interface ExamStatusView {
        void setPushStatusToRecycler(List<AssessmentPaperForPush> paper);
    }

    interface ExamStatusPresenter {
        void setView(ExamStatusView examStatusView);

        void getAllPapers(String date);

    }
}
