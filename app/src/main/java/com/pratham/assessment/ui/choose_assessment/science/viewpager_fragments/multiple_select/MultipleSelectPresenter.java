package com.pratham.assessment.ui.choose_assessment.science.viewpager_fragments.multiple_select;

import android.content.Context;
import android.widget.CompoundButton;

import com.pratham.assessment.domain.ScienceQuestionChoice;

import org.androidannotations.annotations.EBean;

import java.util.List;

@EBean
public class MultipleSelectPresenter implements MultipleSelectContract.MultipleSelectPresenter {
    Context context;
    MultipleSelectContract.MultipleSelectView multipleSelectView;

    public MultipleSelectPresenter(Context context) {
        this.context = context;
    }

    @Override
    public void setView(MultipleSelectContract.MultipleSelectView view) {
        this.multipleSelectView = view;
    }

    @Override
    public void setCheckedAnswer(CompoundButton buttonView, boolean isChecked, List<ScienceQuestionChoice> choices) {
        String mQcID = buttonView.getTag().toString();
        ScienceQuestionChoice mScienceQuestionChoice = null;
        for (ScienceQuestionChoice scienceQuestionChoice : choices) {
            if (scienceQuestionChoice.getQcid().equals(mQcID)) {
                mScienceQuestionChoice = scienceQuestionChoice;
                break;
            }
        }
        if (isChecked) {
            if (mScienceQuestionChoice != null)
                mScienceQuestionChoice.setMyIscorrect("true");
        } else {
            if (mScienceQuestionChoice != null)
                mScienceQuestionChoice.setMyIscorrect("false");
        }
        multipleSelectView.setAnswer(choices);
    }
}
