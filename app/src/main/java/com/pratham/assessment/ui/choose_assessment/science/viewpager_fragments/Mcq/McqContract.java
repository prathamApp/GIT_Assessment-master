package com.pratham.assessment.ui.choose_assessment.science.viewpager_fragments.Mcq;

public interface McqContract {
    interface McqView {
    }

    interface McqPresenter {
        void setView(McqContract.McqView mcqView);

    }
}
