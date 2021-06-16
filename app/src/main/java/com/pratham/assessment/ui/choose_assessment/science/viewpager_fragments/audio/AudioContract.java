package com.pratham.assessment.ui.choose_assessment.science.viewpager_fragments.audio;

import com.pratham.assessment.domain.ScienceQuestion;

import java.util.List;

public interface AudioContract {
    interface AudioView {
        void updateAudioList(List audioList);
    }

    interface AudioPresenter {
        void setView(AudioContract.AudioView audioView);

        void setAttemptedList(ScienceQuestion scienceQuestion);
    }
}
