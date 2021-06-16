package com.pratham.assessment.ui.choose_assessment.science.viewpager_fragments.audio;

import android.content.Context;

import com.pratham.assessment.domain.ScienceQuestion;

import org.androidannotations.annotations.EBean;

import java.util.ArrayList;
import java.util.List;

@EBean
public class AudioPresenter implements AudioContract.AudioPresenter {
    Context context;
    AudioContract.AudioView audioView;


    public AudioPresenter(Context context) {
        this.context = context;
    }

    @Override
    public void setView(AudioContract.AudioView audioView) {
        this.audioView = audioView;
    }

    @Override
    public void setAttemptedList(ScienceQuestion scienceQuestion) {
        List audioList = new ArrayList();
        if (scienceQuestion.getMatchingNameList().size() > 0) {
            for (int i = 0; i < scienceQuestion.getMatchingNameList().size(); i++) {
                audioList.add(scienceQuestion.getMatchingNameList().get(i).getQcid());
            }
        }
        audioView.updateAudioList(audioList);
    }

}
