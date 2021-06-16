package com.pratham.assessment.ui.choose_assessment.science.viewpager_fragments.image_answer;

import android.content.Context;
import android.util.Log;

import org.androidannotations.annotations.EBean;

import java.util.ArrayList;
import java.util.Arrays;

import static com.pratham.assessment.constants.Assessment_Constants.STT_REGEX_3;
import static com.pratham.assessment.ui.choose_assessment.science.viewpager_fragments.fib_without_options.FillInTheBlanksWithoutOptionFragment.correctArr;

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