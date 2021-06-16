package com.pratham.assessment.ui.choose_assessment.science.viewpager_fragments.Mcq;

import android.content.Context;

import org.androidannotations.annotations.EBean;

@EBean
public class McqPresenter implements McqContract.McqPresenter {
    Context context;
    McqContract.McqView mcqView;

    public McqPresenter(Context context) {
        this.context = context;
    }


    @Override
    public void setView(McqContract.McqView mcqView) {
        this.mcqView = mcqView;
    }
}
