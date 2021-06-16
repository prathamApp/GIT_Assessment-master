package com.pratham.assessment.ui.choose_assessment.science.viewpager_fragments.video;

import com.pratham.assessment.domain.ScienceQuestion;
import com.pratham.assessment.ui.choose_assessment.science.viewpager_fragments.audio.AudioContract;

import java.util.List;

public interface VideoContract {
    interface VideoView {
        void updateAudioList(List videoList);

    }

    interface VideoPresenter {
        void setView(VideoContract.VideoView videoView);

        void setAttemptedList(ScienceQuestion scienceQuestion);
    }
}
