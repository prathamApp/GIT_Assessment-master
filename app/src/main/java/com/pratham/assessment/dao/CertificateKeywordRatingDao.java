package com.pratham.assessment.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import com.pratham.assessment.domain.CertificateKeywordRating;

import java.util.List;

@Dao
public interface CertificateKeywordRatingDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public void insertKeywordRating(CertificateKeywordRating keywordRatingClass);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public void insertAllKeywordRating(List<CertificateKeywordRating> keywordRatingClass);

    @Query("DELETE FROM CertificateKeywordRating")
    public void deleteCertificateTopicList();

    //
    @Query("DELETE FROM CertificateKeywordRating where subjectid=:subjectId and examid=:examId")
    public void deleteQuestionByExamIdSubId(String subjectId, String examId);

    @Query("DELETE FROM CertificateKeywordRating where subjectid=:subjectId and examid=:examId and paperId=:paperId")
    public void deleteQuestionByExamIdSubIdPaperId(String subjectId, String examId, String paperId);

    @Query("select * from CertificateKeywordRating")
    public List<CertificateKeywordRating> getAllCertificateQuestions();

    @Query("select * from CertificateKeywordRating where paperId=:paperId and sentFlag=0")
    public List<CertificateKeywordRating> getAllCertificateQuestionsNew(String paperId);

    @Query("select * from CertificateKeywordRating where examId=:examId and subjectid=:subjectid")
    public List<CertificateKeywordRating> getQuestionsByExamIdBySubId(String examId, String subjectid);

    @Query("select distinct languageId from CertificateKeywordRating where examId=:examId and studentId=:studentId")
    public List<String> getDistinctLangByExamIdStudentId(String studentId, String examId);

    @Query("select distinct languageId from CertificateKeywordRating where examId=:examId")
    public List<String> getDistinctLangByExamId(String examId);

    @Query("select distinct subjectId from CertificateKeywordRating where studentId=:studentId and languageId=:langId")
    public List<String> getDistinctSubjectsByStudentIdLangId(String studentId, String langId);

    @Query("select distinct subjectId from CertificateKeywordRating where languageId=:langId")
    public List<String> getDistinctSubjectsByLangId(String langId);

    @Query("select distinct examId from CertificateKeywordRating where studentId=:studentId")
    public List<String> getQuestionsByExamIdBySubId(String studentId);

    @Query("select distinct examId from CertificateKeywordRating")
    public List<String> getQuestionsByExamId();

    @Query("select * from CertificateKeywordRating where examId=:examId and subjectid=:subjectid and paperId=:paperId and studentId=:studId")
    public List<CertificateKeywordRating> getQuestionsByExamIdSubIdPaperIdStudId(String examId, String subjectid, String paperId, String studId);

    @Query("update CertificateKeywordRating set sentFlag=1 where sentFlag=0")
    public void setSentFlag();

    @Query("select min(questionLevel) from CertificateKeywordRating where isCorrect=0 and paperId=:paperId")
    public int getRecommendedLowestLevel(String paperId);

    @Query("select max(questionLevel) from CertificateKeywordRating where isCorrect=1 and paperId=:paperId")
    public int getRecommendedHighestLevel(String paperId);
}