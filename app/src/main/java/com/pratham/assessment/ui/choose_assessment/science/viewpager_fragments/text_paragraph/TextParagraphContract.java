package com.pratham.assessment.ui.choose_assessment.science.viewpager_fragments.text_paragraph;

import com.pratham.assessment.domain.ScienceQuestion;

import java.util.ArrayList;
import java.util.List;

public class TextParagraphContract {
    public interface TextParagraphView {
        void setResult(String sttRes);

        void setWordsToLayout(List<String> splitWords);
    }

    public interface TextParagraphPresenter {
        void setView(TextParagraphContract.TextParagraphView textParagraphView);

        void sttResultProcess(ArrayList<String> sttResult, List<String> splitWordsPunct, List<String> wordsResIdList, ScienceQuestion scienceQuestion, boolean[] correctArr);

        void createCorrectArr(List<String> splitWords);

    }
}
