package com.pratham.assessment.ui.choose_assessment.science.viewpager_fragments.video;

import android.content.Context;

import com.pratham.assessment.domain.ScienceQuestion;

import org.androidannotations.annotations.EBean;

@EBean
public class VideoPresenter implements VideoContract.VideoPresenter {
    Context context;

    public VideoPresenter(Context context) {
        this.context = context;
    }

    @Override
    public void setView(VideoContract.VideoView videoView) {

    }

    @Override
    public void setAttemptedList(ScienceQuestion scienceQuestion) {

    }
}
