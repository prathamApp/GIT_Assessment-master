package com.pratham.assessment.ui.choose_assessment.science.interfaces;

import com.pratham.assessment.domain.ScienceQuestionChoice;

import java.util.List;

public interface AssessmentAnswerListener {

    void setAnswerInActivity(String answer, String qid, List<ScienceQuestionChoice> list, int marks);

    void removeSupervisorFragment();

    void reDownloadExam();

//    void setParagraph(String para, boolean isParaQuestion);
}
