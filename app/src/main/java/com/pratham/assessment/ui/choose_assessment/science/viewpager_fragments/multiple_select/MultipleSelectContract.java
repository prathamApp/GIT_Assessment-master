package com.pratham.assessment.ui.choose_assessment.science.viewpager_fragments.multiple_select;

import android.widget.CompoundButton;

import com.pratham.assessment.domain.ScienceQuestionChoice;

import java.util.List;

public interface MultipleSelectContract {
    interface MultipleSelectView {
        void setAnswer(List<ScienceQuestionChoice> choices);
    }

    interface MultipleSelectPresenter {
        void setView(MultipleSelectContract.MultipleSelectView view);

        void setCheckedAnswer(CompoundButton buttonView, boolean isChecked, List<ScienceQuestionChoice> choices);
    }

}
