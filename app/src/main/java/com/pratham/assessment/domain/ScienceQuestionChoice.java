package com.pratham.assessment.domain;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

import java.io.Serializable;

@Entity
public class ScienceQuestionChoice implements Serializable {

    @NonNull
    @PrimaryKey
    private String qcid;
    private String qid;
    private String matchingname;
    private String choicename;
    private String correct;
    private String matchingurl;
    private String choiceurl;
    @Ignore
    private String myIscorrect = "false";
    private boolean IsQuestionFromSDCard;

    private String AppVersionChoice;

    private String localChoiceUrl;
    private String localMatchUrl;

    public String getAppVersionChoice() {
        return AppVersionChoice;
    }

    public void setAppVersionChoice(String appVersionChoice) {
        AppVersionChoice = appVersionChoice;
    }

    public String getMyIscorrect() {
        return myIscorrect;
    }

    public void setMyIscorrect(String myIscorrect) {
        if (myIscorrect == null) {
            this.myIscorrect = "false";
        } else
            this.myIscorrect = myIscorrect;
    }

    public boolean getIsQuestionFromSDCard() {
        return IsQuestionFromSDCard;
    }

    public void setIsQuestionFromSDCard(boolean questionFromSDCard) {
        IsQuestionFromSDCard = questionFromSDCard;
    }

    public String getMatchingurl() {
        return matchingurl;
    }

    public void setMatchingurl(String matchingurl) {
        this.matchingurl = matchingurl;
    }

    public String getChoiceurl() {
        return choiceurl;
    }

    public void setChoiceurl(String choiceurl) {
        this.choiceurl = choiceurl;
    }

    public String getCorrect() {
        return correct;
    }

    public void setCorrect(String correct) {
        this.correct = correct;
    }

    public String getQcid() {
        return qcid;
    }

    public void setQcid(String qcid) {
        this.qcid = qcid;
    }

    public String getChoicename() {
        return choicename;
    }

    public void setChoicename(String choicename) {
        this.choicename = choicename;
    }

    public String getQid() {
        return qid;
    }

    public void setQid(String qid) {
        this.qid = qid;
    }

    public String getMatchingname() {
        return matchingname;
    }

    public void setMatchingname(String matchingname) {
        this.matchingname = matchingname;
    }
    public String getLocalChoiceUrl() {
        return localChoiceUrl;
    }

    public void setLocalChoiceUrl(String localChoiceUrl) {
        this.localChoiceUrl = localChoiceUrl;
    }

    public String getLocalMatchUrl() {
        return localMatchUrl;
    }

    public void setLocalMatchUrl(String localMatchUrl) {
        this.localMatchUrl = localMatchUrl;
    }

}