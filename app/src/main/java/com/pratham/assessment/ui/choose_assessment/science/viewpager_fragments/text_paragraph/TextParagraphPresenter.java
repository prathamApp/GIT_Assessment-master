package com.pratham.assessment.ui.choose_assessment.science.viewpager_fragments.text_paragraph;

import android.content.Context;
import android.util.Log;

import com.pratham.assessment.custom.FastSave;
import com.pratham.assessment.domain.ScienceQuestion;
import com.pratham.assessment.utilities.Assessment_Utility;

import org.androidannotations.annotations.EBean;

import java.util.ArrayList;
import java.util.List;

import static com.pratham.assessment.constants.Assessment_Constants.LANGUAGE;

@EBean
public class TextParagraphPresenter implements TextParagraphContract.TextParagraphPresenter {
    Context context;
    TextParagraphContract.TextParagraphView textParagraphView;
    static boolean[] correctArr;
    List<String> splitWordsPunct = new ArrayList<String>();

    public TextParagraphPresenter(Context context) {
        this.context = context;
    }


    @Override
    public void setView(TextParagraphContract.TextParagraphView textParagraphView) {
        this.textParagraphView = textParagraphView;
    }

    @Override
    public void sttResultProcess(ArrayList<String> sttResult, List<String> splitWordsPunct,
                                 List<String> wordsResIdList, ScienceQuestion scienceQuestion, boolean[] correctArr) {

        String sttRes = "";
        for (int i = 0; i < sttResult.size(); i++) {
            System.out.println("LogTag" + " onResults :  " + sttResult.get(i));

            if (sttResult.get(i).equalsIgnoreCase(scienceQuestion.getAnswer()))
                sttRes = sttResult.get(i);
            else sttRes = sttResult.get(0);
        }
        String[] splitRes = sttRes.split(" ");
       String answer = " ";
//        addSttResultDB(sttResult);

        for (int j = 0; j < splitRes.length; j++) {
            if (FastSave.getInstance().getString(LANGUAGE, "1").equalsIgnoreCase("1"))
                splitRes[j].replaceAll("[^a-zA-Z ]", "");
            else
                splitRes[j] = Assessment_Utility.removeSpecialCharacters(splitRes[j]);
            for (int i = 0; i < splitWordsPunct.size(); i++) {
                if ((splitRes[j].equalsIgnoreCase(splitWordsPunct.get(i))) && !correctArr[i]) {
                    correctArr[i] = true;
                    answer = answer + " " + splitWordsPunct.get(i) /*+ "(" + wordsResIdList.get(i) + "),"*/;
                    break;
                }
            }
        }
//        getPercentage();
//        int correctWordCount = getCorrectCounter();
        String wordTime = Assessment_Utility.getCurrentDateTime();
//        addLearntWords(splitWordsPunct, wordsResIdList);
//        addScore(0, "Words:" + word, correctWordCount, correctArr.length, wordTime, " ");
   /*     this.sttResult.append(" ").append(sttRes);
        assessmentAnswerListener.setAnswerInActivity("" + calculateMarks(), this.sttResult.toString(), scienceQuestion.getQid(), null);
   */     textParagraphView.setResult(sttRes);
    }

    @Override
    public void createCorrectArr(List<String> splitWords) {
        correctArr = new boolean[splitWords.size()];
        for (int i = 0; i < splitWords.size(); i++) {
            correctArr[i] = false;
//            HighlightWords highlightWords = new HighlightWords();
//            highlightWords.setPosition(i);
//            highlightWords.setWord(splitWords.get(i));
//            highlightWords.setHighlighted(false);
//            highlightWordsList.add(highlightWords);
//            splitWordsPunct.add(splitWords.get(i).replaceAll(STT_REGEX_2, ""));
            String myString = Assessment_Utility.removeSpecialCharacters(splitWords.get(i));
            splitWordsPunct.add(myString);
            Log.d("setWords", "setWords: " + myString);
            textParagraphView.setWordsToLayout(splitWords);
        }
    }
}
