package com.pratham.assessment.ui.choose_assessment.science.viewpager_fragments.image_answer;

import android.content.Context;

import org.androidannotations.annotations.EBean;

@EBean
public class ImageAnswerPresenter implements ImageAnswerContract.ImageAnswerPresenter {

    Context context;
    private ImageAnswerContract.ImageAnswerView imageAnswerView;


    public ImageAnswerPresenter(Context context) {
        this.context = context;
    }

    @Override
    public void setView(ImageAnswerContract.ImageAnswerView imageAnswerView) {
        this.imageAnswerView = imageAnswerView;
    }

}