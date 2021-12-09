package com.pratham.assessment.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import com.pratham.assessment.domain.TempScienceQuestion;

import java.util.List;

@Dao
public interface TempScienceQuestionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public void insertAllQuestions(List<TempScienceQuestion> questionList);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public void insert(TempScienceQuestion question);

    @Query("SELECT * FROM TempScienceQuestion where StudentID=:currentStudentID and subjectid=:subjectId and examid=:selectedExamId and languageid=:selectedLang")
    public List<TempScienceQuestion> getAlreadyAttemptedPaper(String currentStudentID, String subjectId, String selectedExamId, String selectedLang);

    @Query("DELETE FROM TempScienceQuestion WHERE paperId=:paperId and qid=:qid")
    public void deleteQuestionByPaperIdQid(String paperId, String qid);

    @Query("UPDATE TempScienceQuestion SET  outofmarks=:outOMarks , startTime=:startTme" +
            " , endTime=:endTime , marksPerQuestion=:marksPerQ , userAnswer=:userAns" +
            " , userAnswerId=:userAnsId , isAttempted=:isAttempted , isCorrect=:isCorrect" +
            " , IsParaQuestion=:isParaQ , RefParaID=:refParaId , SessionID=:sessionId " +
            ", DeviceID=:DeviceID , ScoredMarks=:ScoredMarks , paperTotalMarks=:paperTotalMarks " +
            ", paperStartDateTime=:paperStartDateTime , paperEndDateTime=:paperEndDateTime " +
            ", Label=:label WHERE paperId=:paperId and qid=:qid")
    public int updateQuestionByPaperIdQid(String paperId, String qid, String outOMarks, String startTme,
                                          String endTime, String marksPerQ, String userAns, String userAnsId,
                                          boolean isAttempted, boolean isCorrect, boolean isParaQ, String refParaId,
                                          String sessionId, String DeviceID, String ScoredMarks, String paperTotalMarks,
                                          String paperStartDateTime, String paperEndDateTime, String label);

    @Query("UPDATE TempScienceQuestion SET languageid=:langId, subjectid=:subId,topicid=:topicId," +
            " lessonid=:lessonId,qtid=:qtId,qname=:qName,answer=:answer,ansdesc=:ansDesc,qlevel=:qLevel," +
            "hint=:hint, addedby=:addedBy,addedtime=:addedTime, updatedby=:updatedBy ,updatedtime=:updatedTime," +
            " IsParaQuestion=:isParaQ , RefParaID=:refParaId , photourl=:photoUrl,AppVersion=:appVersion where qid=:qid")
    public int updateQuestionForVersion(String langId, String subId, String topicId, String lessonId,
                                        String qtId, String qName, String answer, String ansDesc, String qLevel, String hint, String addedBy,
                                        String addedTime, String updatedBy, String updatedTime, String appVersion, String photoUrl,
                                        boolean isParaQ, String refParaId, String qid
    );


    @Query("DELETE FROM TempScienceQuestion WHERE examid=:examId and languageid=:langId and subjectid=:subId and paperId=:paperId and StudentID=:studId")
    public int deleteByLangIdSubIdTopicIdPaperIdStudId(String examId, String langId, String subId, String paperId, String studId);

    @Query("DELETE FROM TempScienceQuestion WHERE examid=:examId and languageid=:langId and subjectid=:subId  and StudentID=:studId")
    public int deleteByLangIdSubIdTopicIdStudId(String examId, String langId, String subId, String studId);

    @Query("UPDATE TempScienceQuestion SET qname = replace( qname, '\n', '<br/>' ) WHERE qname LIKE '%\n%'")
    public void replaceNewLineForQuestions();

    @Query("UPDATE TempScienceQuestion SET qname = replace( qname, '/n', '<br/>' ) WHERE qname LIKE '%/n%'")
    public void replaceNewLineForQuestions2();

}