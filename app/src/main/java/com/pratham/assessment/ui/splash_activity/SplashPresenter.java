package com.pratham.assessment.ui.splash_activity;

import android.app.ActivityManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.pratham.assessment.AssessmentApplication;
import com.pratham.assessment.R;
import com.pratham.assessment.async.GetLatestVersion;
import com.pratham.assessment.constants.Assessment_Constants;
import com.pratham.assessment.custom.FastSave;
import com.pratham.assessment.custom.custom_dialogs.PushDataDialog;
import com.pratham.assessment.database.AppDatabase;
import com.pratham.assessment.database.BackupDatabase;
import com.pratham.assessment.domain.AssessmentLanguages;
import com.pratham.assessment.domain.AssessmentPaperForPush;
import com.pratham.assessment.domain.AssessmentPaperPattern;
import com.pratham.assessment.domain.AssessmentPatternDetails;
import com.pratham.assessment.domain.AssessmentSubjects;
import com.pratham.assessment.domain.AssessmentTest;
import com.pratham.assessment.domain.Attendance;
import com.pratham.assessment.domain.CertificateTopicList;
import com.pratham.assessment.domain.Modal_Log;
import com.pratham.assessment.domain.ScienceQuestion;
import com.pratham.assessment.domain.ScienceQuestionChoice;
import com.pratham.assessment.domain.Score;
import com.pratham.assessment.domain.Session;
import com.pratham.assessment.domain.Status;
import com.pratham.assessment.domain.Student;
import com.pratham.assessment.domain.SupervisorData;
import com.pratham.assessment.services.LocationService;
import com.pratham.assessment.utilities.Assessment_Utility;

import net.lingala.zip4j.core.ZipFile;

import org.androidannotations.annotations.EBean;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import static android.content.Context.ACTIVITY_SERVICE;
import static com.pratham.assessment.constants.Assessment_Constants.SDCARD_OFFLINE_PATH_SAVED;
import static com.pratham.assessment.utilities.Assessment_Utility.getStoragePath;

@EBean
public class SplashPresenter implements SplashContract.SplashPresenter {
    static String fpath, appname;
    Context context;
    SplashContract.SplashView splashView;

    public SplashPresenter(Context context) {
        this.context = context;
        this.splashView = (SplashContract.SplashView) context;
    }

    @Override
    public void checkVersion() {

        try {
            new GetLatestVersion(this, context).execute();
        } catch (Exception e) {
            e.printStackTrace();
            splashView.startApp();

        }
    }

    @Override
    public void versionObtained(String latestVersion) {
        // Force Update Code
        String currentVersion = Assessment_Utility.getCurrentVersion(context);
        if (latestVersion != null) {
            if ((!currentVersion.equals(latestVersion))) {
                PushDataDialog pushDialog = new PushDataDialog(context);

                pushDialog.setContentView(R.layout.app_push_data_dialog);
                Objects.requireNonNull(pushDialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                pushDialog.setCancelable(false);
                pushDialog.setCanceledOnTouchOutside(false);
                pushDialog.show();
                TextView txt_push_dialog_msg;
                Button ok_btn, eject_btn;
                txt_push_dialog_msg = pushDialog.findViewById(R.id.txt_push_dialog_msg);
                ok_btn = pushDialog.findViewById(R.id.ok_btn);
                eject_btn = pushDialog.findViewById(R.id.eject_btn);
                ok_btn.setVisibility(View.VISIBLE);
                eject_btn.setVisibility(View.VISIBLE);
                ok_btn.setText(R.string.update);
                eject_btn.setText(R.string.cancel);
                txt_push_dialog_msg.setText(R.string.this_app_version_is_older_please_update_the_app);
                ok_btn.setOnClickListener(view -> {
                    pushDialog.dismiss();
                    splashView.startApp();
                    Assessment_Utility.updateApp(context);
                });
                eject_btn.setOnClickListener(view -> {
                    pushDialog.dismiss();
                    splashView.startApp();
                });
                pushDialog.show();

            } else
                splashView.startApp();

        } else {
            splashView.startApp();
        }
    }

    /**
     * copy db from prathambackups to local db
     */
    @Override
    public void copyDataBase() {

        try {
            new AsyncTask<Void, Integer, Void>() {

                @Override
                protected void onPreExecute() {
                    super.onPreExecute();
                    splashView.showProgressDialog();
                }

                @Override
                protected Void doInBackground(Void... voids) {
                    try {
                        SQLiteDatabase db = SQLiteDatabase.openDatabase(getStoragePath().getAbsolutePath() + "/PrathamBackups" + "/assessment_database", null, SQLiteDatabase.OPEN_READONLY);
                        if (db != null) {
                            try {
                                Cursor content_cursor;
                                content_cursor = db.rawQuery("SELECT * FROM Score Where sentFlag=0", null);
                                List<Score> contents = new ArrayList<>();
                                if (content_cursor.moveToFirst()) {
                                    while (!content_cursor.isAfterLast()) {
                                        Score detail = new Score();
                                        detail.setScoreId(content_cursor.getInt(content_cursor.getColumnIndex("ScoreId")));
                                        detail.setSessionID(content_cursor.getString(content_cursor.getColumnIndex("SessionID")));
                                        detail.setStudentID(content_cursor.getString(content_cursor.getColumnIndex("StudentID")));
                                        detail.setDeviceID(content_cursor.getString(content_cursor.getColumnIndex("DeviceID")));
                                        detail.setResourceID(content_cursor.getString(content_cursor.getColumnIndex("ResourceID")));
                                        detail.setQuestionId(content_cursor.getInt(content_cursor.getColumnIndex("QuestionId")));
                                        detail.setScoredMarks(content_cursor.getInt(content_cursor.getColumnIndex("ScoredMarks")));
                                        detail.setTotalMarks(content_cursor.getInt(content_cursor.getColumnIndex("TotalMarks")));
                                        detail.setStartDateTime(content_cursor.getString(content_cursor.getColumnIndex("StartDateTime")));
                                        detail.setEndDateTime(content_cursor.getString(content_cursor.getColumnIndex("EndDateTime")));
                                        detail.setRevisitedStartDateTime(content_cursor.getString(content_cursor.getColumnIndex("revisitedStartDateTime")));
                                        detail.setRevisitedEndDateTime(content_cursor.getString(content_cursor.getColumnIndex("revisitedEndDateTime")));
                                        detail.setLevel(content_cursor.getInt(content_cursor.getColumnIndex("Level")));
                                        detail.setLabel(content_cursor.getString(content_cursor.getColumnIndex("Label")));
                                        detail.setSentFlag(content_cursor.getInt(content_cursor.getColumnIndex("sentFlag")));
                                        detail.setIsAttempted(content_cursor.getString(content_cursor.getColumnIndex("isAttempted")).equalsIgnoreCase("true") ? true : false);
                                        detail.setIsCorrect(content_cursor.getString(content_cursor.getColumnIndex("isCorrect")).equalsIgnoreCase("true") ? true : false);
                                        detail.setUserAnswer(content_cursor.getString(content_cursor.getColumnIndex("userAnswer")));
                                        detail.setExamId(content_cursor.getString(content_cursor.getColumnIndex("examId")));
                                        detail.setPaperId(content_cursor.getString(content_cursor.getColumnIndex("paperId")));
                                        contents.add(detail);
                                        content_cursor.moveToNext();
                                    }
                                }
                                AppDatabase.getDatabaseInstance(context).getScoreDao().addScoreList(contents);
                                content_cursor.close();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            try {
                                Cursor content_cursor;
                                content_cursor = db.rawQuery("SELECT * FROM Session Where sentFlag=0", null);
                                List<Session> contents = new ArrayList<>();
                                if (content_cursor.moveToFirst()) {
                                    while (!content_cursor.isAfterLast()) {
                                        Session detail = new Session();
                                        detail.setSessionID(content_cursor.getString(content_cursor.getColumnIndex("SessionID")));
                                        detail.setFromDate(content_cursor.getString(content_cursor.getColumnIndex("fromDate")));
                                        detail.setToDate(content_cursor.getString(content_cursor.getColumnIndex("toDate")));
                                        detail.setSentFlag(content_cursor.getInt(content_cursor.getColumnIndex("sentFlag")));
                                        contents.add(detail);
                                        content_cursor.moveToNext();
                                    }
                                }
                                AppDatabase.getDatabaseInstance(context).getSessionDao().addSessionList(contents);
                                content_cursor.close();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            try {
                                Cursor content_cursor;
                                content_cursor = db.rawQuery("SELECT * FROM Attendance Where sentFlag=0", null);
                                List<Attendance> contents = new ArrayList<>();
                                if (content_cursor.moveToFirst()) {
                                    while (!content_cursor.isAfterLast()) {
                                        Attendance detail = new Attendance();
                                        detail.setAttendanceID(content_cursor.getInt(content_cursor.getColumnIndex("attendanceID")));
                                        detail.setSessionID(content_cursor.getString(content_cursor.getColumnIndex("SessionID")));
                                        detail.setDate(content_cursor.getString(content_cursor.getColumnIndex("Date")));
                                        detail.setGroupID(content_cursor.getString(content_cursor.getColumnIndex("GroupID")));
                                        detail.setSentFlag(content_cursor.getInt(content_cursor.getColumnIndex("sentFlag")));
                                        contents.add(detail);
                                        content_cursor.moveToNext();
                                    }
                                }
                                AppDatabase.getDatabaseInstance(context).getAttendanceDao().addAttendanceList(contents);
                                content_cursor.close();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            try {
                                Cursor content_cursor;
                                content_cursor = db.rawQuery("SELECT * FROM AssessmentPaperForPush Where sentFlag=0", null);
                                List<AssessmentPaperForPush> contents = new ArrayList<>();
                                if (content_cursor.moveToFirst()) {
                                    while (!content_cursor.isAfterLast()) {
                                        AssessmentPaperForPush detail = new AssessmentPaperForPush();
                                        detail.setPaperStartTime(content_cursor.getString(content_cursor.getColumnIndex("paperStartTime")));
                                        detail.setPaperEndTime(content_cursor.getString(content_cursor.getColumnIndex("paperEndTime")));
                                        detail.setLanguageId(content_cursor.getString(content_cursor.getColumnIndex("languageId")));
                                        detail.setExamId(content_cursor.getString(content_cursor.getColumnIndex("examId")));
                                        detail.setSubjectId(content_cursor.getString(content_cursor.getColumnIndex("subjectId")));
                                        detail.setOutOfMarks(content_cursor.getString(content_cursor.getColumnIndex("outOfMarks")));
                                        detail.setPaperId(content_cursor.getString(content_cursor.getColumnIndex("paperId")));
                                        detail.setTotalMarks(content_cursor.getString(content_cursor.getColumnIndex("totalMarks")));
                                        detail.setExamTime(content_cursor.getString(content_cursor.getColumnIndex("examTime")));
                                        detail.setCorrectCnt(content_cursor.getInt(content_cursor.getColumnIndex("CorrectCnt")));
                                        detail.setWrongCnt(content_cursor.getInt(content_cursor.getColumnIndex("wrongCnt")));
                                        detail.setSkipCnt(content_cursor.getInt(content_cursor.getColumnIndex("SkipCnt")));
                                        detail.setSessionID(content_cursor.getString(content_cursor.getColumnIndex("SessionID")));
                                        detail.setStudentId(content_cursor.getString(content_cursor.getColumnIndex("studentId")));
                                        detail.setExamName(content_cursor.getString(content_cursor.getColumnIndex("examName")));
                                        detail.setQuestion1Rating(content_cursor.getString(content_cursor.getColumnIndex("question1Rating")));
                                        detail.setQuestion2Rating(content_cursor.getString(content_cursor.getColumnIndex("question2Rating")));
                                        detail.setQuestion3Rating(content_cursor.getString(content_cursor.getColumnIndex("question3Rating")));
                                        detail.setQuestion4Rating(content_cursor.getString(content_cursor.getColumnIndex("question4Rating")));
                                        detail.setQuestion5Rating(content_cursor.getString(content_cursor.getColumnIndex("question5Rating")));
                                        detail.setQuestion6Rating(content_cursor.getString(content_cursor.getColumnIndex("question6Rating")));
                                        detail.setQuestion7Rating(content_cursor.getString(content_cursor.getColumnIndex("question7Rating")));
                                        detail.setQuestion8Rating(content_cursor.getString(content_cursor.getColumnIndex("question8Rating")));
                                        detail.setQuestion9Rating(content_cursor.getString(content_cursor.getColumnIndex("question9Rating")));
                                        detail.setQuestion10Rating(content_cursor.getString(content_cursor.getColumnIndex("question10Rating")));
                                        detail.setFullName(content_cursor.getString(content_cursor.getColumnIndex("FullName")));
                                        detail.setGender(content_cursor.getString(content_cursor.getColumnIndex("Gender")));
                                        detail.setIsniosstudent(content_cursor.getString(content_cursor.getColumnIndex("isniosstudent")));
                                        detail.setAge(content_cursor.getInt(content_cursor.getColumnIndex("Age")));
                                        detail.setSentFlag(content_cursor.getInt(content_cursor.getColumnIndex("sentFlag")));
                                        contents.add(detail);
                                        content_cursor.moveToNext();
                                    }
                                }
                                AppDatabase.getDatabaseInstance(context).getAssessmentPaperForPushDao().insertAllPapersForPush(contents);
                                content_cursor.close();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            try {
                                Cursor content_cursor;
                                content_cursor = db.rawQuery("SELECT * FROM SupervisorData Where sentFlag=0", null);
                                List<SupervisorData> contents = new ArrayList<>();
                                if (content_cursor.moveToFirst()) {
                                    while (!content_cursor.isAfterLast()) {
                                        SupervisorData detail = new SupervisorData();
                                        detail.setsId(content_cursor.getInt(content_cursor.getColumnIndex("sId")));
                                        detail.setAssessmentSessionId(content_cursor.getString(content_cursor.getColumnIndex("assessmentSessionId")));
                                        detail.setSupervisorId(content_cursor.getString(content_cursor.getColumnIndex("supervisorId")));
                                        detail.setSupervisorName(content_cursor.getString(content_cursor.getColumnIndex("supervisorName")));
                                        detail.setSupervisorPhoto(content_cursor.getString(content_cursor.getColumnIndex("supervisorPhoto")));
                                        detail.setSentFlag(content_cursor.getInt(content_cursor.getColumnIndex("sentFlag")));
                                        contents.add(detail);
                                        content_cursor.moveToNext();
                                    }
                                }
                                AppDatabase.getDatabaseInstance(context).getSupervisorDataDao().insertAll(contents);
                                content_cursor.close();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            try {
                                Cursor content_cursor;
                                content_cursor = db.rawQuery("SELECT * FROM Logs Where sentFlag=0", null);
                                List<Modal_Log> contents = new ArrayList<>();
                                if (content_cursor.moveToFirst()) {
                                    while (!content_cursor.isAfterLast()) {
                                        Modal_Log detail = new Modal_Log();
                                        detail.setLogId(content_cursor.getInt(content_cursor.getColumnIndex("logId")));
                                        detail.setDeviceId(content_cursor.getString(content_cursor.getColumnIndex("deviceId")));
                                        detail.setCurrentDateTime(content_cursor.getString(content_cursor.getColumnIndex("currentDateTime")));
                                        detail.setErrorType(content_cursor.getString(content_cursor.getColumnIndex("errorType")));
                                        detail.setExceptionMessage(content_cursor.getString(content_cursor.getColumnIndex("exceptionMessage")));
                                        detail.setExceptionStackTrace(content_cursor.getString(content_cursor.getColumnIndex("exceptionStackTrace")));
                                        detail.setGroupId(content_cursor.getString(content_cursor.getColumnIndex("groupId")));
                                        detail.setLogDetail(content_cursor.getString(content_cursor.getColumnIndex("LogDetail")));
                                        detail.setMethodName(content_cursor.getString(content_cursor.getColumnIndex("methodName")));
                                        contents.add(detail);
                                        content_cursor.moveToNext();
                                    }
                                }
                                AppDatabase.getDatabaseInstance(context).getLogsDao().insertAllLogs(contents);
                                content_cursor.close();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            if (!AssessmentApplication.isTablet) {
                                try {
                                    Cursor content_cursor;
                                    content_cursor = db.rawQuery("SELECT * FROM Student Where newFlag=0", null);
                                    List<Student> contents = new ArrayList<>();
                                    if (content_cursor.moveToFirst()) {
                                        while (!content_cursor.isAfterLast()) {
                                            Student detail = new Student();
                                            detail.setStudentID(content_cursor.getString(content_cursor.getColumnIndex("StudentID")));
                                            detail.setStudentUID(content_cursor.getString(content_cursor.getColumnIndex("StudentUID")));
                                            detail.setFirstName(content_cursor.getString(content_cursor.getColumnIndex("FirstName")));
                                            detail.setMiddleName(content_cursor.getString(content_cursor.getColumnIndex("MiddleName")));
                                            detail.setLastName(content_cursor.getString(content_cursor.getColumnIndex("LastName")));
                                            detail.setFullName(content_cursor.getString(content_cursor.getColumnIndex("FullName")));
                                            detail.setGender(content_cursor.getString(content_cursor.getColumnIndex("Gender")));
                                            detail.setRegDate(content_cursor.getString(content_cursor.getColumnIndex("regDate")));
                                            detail.setAge(content_cursor.getInt(content_cursor.getColumnIndex("Age")));
                                            detail.setVillageName(content_cursor.getString(content_cursor.getColumnIndex("villageName")));
                                            detail.setNewFlag(content_cursor.getInt(content_cursor.getColumnIndex("newFlag")));
                                            detail.setDeviceId(content_cursor.getString(content_cursor.getColumnIndex("DeviceId")));
                                            detail.setProgramId(content_cursor.getString(content_cursor.getColumnIndex("programId")));
                                            detail.setState(content_cursor.getString(content_cursor.getColumnIndex("state")));
                                            detail.setDistrict(content_cursor.getString(content_cursor.getColumnIndex("district")));
                                            detail.setBlock(content_cursor.getString(content_cursor.getColumnIndex("block")));
                                            detail.setSchool(content_cursor.getString(content_cursor.getColumnIndex("school")));
                                            detail.setVillageId(content_cursor.getString(content_cursor.getColumnIndex("villageId")));
                                            detail.setIsniosstudent(content_cursor.getString(content_cursor.getColumnIndex("isniosstudent")));
                                            contents.add(detail);
                                            content_cursor.moveToNext();
                                        }
                                    }
                                    AppDatabase.getDatabaseInstance(context).getStudentDao().insertAll(contents);
                                    content_cursor.close();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                            BackupDatabase.backup(context);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return null;
                }

                @Override
                protected void onProgressUpdate(Integer... values) {
                }

                @Override
                protected void onPostExecute(Void aVoid) {
                    //addStartTime();
                    super.onPostExecute(aVoid);
                    splashView.dismissProgressDialog();
                    /* splashView.showButton();*/
                    BackupDatabase.backup(context);
                }

            }.execute();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * copy OFFLINE db from prathambackups to local db
     */
    @Override
    public void copySDCardDB() {
        try {
            new AsyncTask<Void, Integer, Void>() {
                ProgressDialog progressDialog;
                boolean copySuccessful;
                int tableCopyCount = 0;

                @Override
                protected void onPreExecute() {
                    super.onPreExecute();
                    splashView.showProgressDialog();

                }

                @Override
                protected Void doInBackground(Void... voids) {
                    try {
                        SQLiteDatabase db = SQLiteDatabase.openDatabase(getStoragePath() + "/PrathamBackups/offline_assessment_database.db", null, SQLiteDatabase.OPEN_READONLY);
                        if (db != null) {
                            try {
                                Cursor content_cursor;
                                content_cursor = db.rawQuery("SELECT * FROM AssessmentPaperPattern", null);
                                List<AssessmentPaperPattern> paperPatternList = new ArrayList<>();
                                if (content_cursor.moveToFirst()) {
                                    while (!content_cursor.isAfterLast()) {
                                        AssessmentPaperPattern paperPattern = new AssessmentPaperPattern();
                                        paperPattern.setSubjectname(content_cursor.getString(content_cursor.getColumnIndex("subjectname")));
                                        paperPattern.setExamname(content_cursor.getString(content_cursor.getColumnIndex("examname")));
                                        paperPattern.setExamduration(content_cursor.getString(content_cursor.getColumnIndex("examduration")));
                                        paperPattern.setCertificateQuestion1(content_cursor.getString(content_cursor.getColumnIndex("certificateQuestion1")));
                                        paperPattern.setCertificateQuestion2(content_cursor.getString(content_cursor.getColumnIndex("certificateQuestion2")));
                                        paperPattern.setCertificateQuestion3(content_cursor.getString(content_cursor.getColumnIndex("certificateQuestion3")));
                                        paperPattern.setCertificateQuestion4(content_cursor.getString(content_cursor.getColumnIndex("certificateQuestion4")));
                                        paperPattern.setCertificateQuestion5(content_cursor.getString(content_cursor.getColumnIndex("certificateQuestion5")));
                                        paperPattern.setCertificateQuestion6(content_cursor.getString(content_cursor.getColumnIndex("certificateQuestion6")));
                                        paperPattern.setCertificateQuestion7(content_cursor.getString(content_cursor.getColumnIndex("certificateQuestion7")));
                                        paperPattern.setCertificateQuestion8(content_cursor.getString(content_cursor.getColumnIndex("certificateQuestion8")));
                                        paperPattern.setCertificateQuestion9(content_cursor.getString(content_cursor.getColumnIndex("certificateQuestion9")));
                                        paperPattern.setCertificateQuestion10(content_cursor.getString(content_cursor.getColumnIndex("certificateQuestion10")));
                                        paperPattern.setOutofmarks(content_cursor.getString(content_cursor.getColumnIndex("outofmarks")));
                                        paperPattern.setExamid(content_cursor.getString(content_cursor.getColumnIndex("examid")));
                                        paperPattern.setSubjectid(content_cursor.getString(content_cursor.getColumnIndex("subjectid")));
                                        paperPattern.setIsRandom((content_cursor.getInt(content_cursor.getColumnIndex("IsRandom"))) == 1);
                                        paperPattern.setDiagnosticTest((content_cursor.getInt(content_cursor.getColumnIndex("isDiagnosticTest"))) == 1);
                                        paperPattern.setNoofcertificateq(content_cursor.getString(content_cursor.getColumnIndex("noofcertificateq")));
                                        paperPattern.setExammode(content_cursor.getString(content_cursor.getColumnIndex("exammode")));
                                        paperPatternList.add(paperPattern);
                                        AppDatabase.getDatabaseInstance(context).getAssessmentPaperPatternDao().deletePaperPatternByExamId(paperPattern.getExamid());

                                        content_cursor.moveToNext();
                                    }
                                }
                                AppDatabase.getDatabaseInstance(context).getAssessmentPaperPatternDao().insertAllPapersPatterns(paperPatternList);
                                content_cursor.close();
                                tableCopyCount++;
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            try {
                                Cursor content_cursor;
                                content_cursor = db.rawQuery("SELECT * FROM AssessmentPatternDetails", null);
                                List<AssessmentPatternDetails> assessmentPatternDetailsList = new ArrayList<>();
                                if (content_cursor.moveToFirst()) {
                                    while (!content_cursor.isAfterLast()) {
                                        AssessmentPatternDetails assessmentPatternDetails = new AssessmentPatternDetails();
                                        assessmentPatternDetails.setTotalmarks(content_cursor.getString(content_cursor.getColumnIndex("totalmarks")));
                                        assessmentPatternDetails.setNoofquestion(content_cursor.getString(content_cursor.getColumnIndex("noofquestion")));
                                        assessmentPatternDetails.setQtname(content_cursor.getString(content_cursor.getColumnIndex("qtname")));
                                        assessmentPatternDetails.setMarksperquestion(content_cursor.getString(content_cursor.getColumnIndex("marksperquestion")));
                                        assessmentPatternDetails.setTopicid(content_cursor.getString(content_cursor.getColumnIndex("topicid")));
                                        assessmentPatternDetails.setQlevel(content_cursor.getString(content_cursor.getColumnIndex("qlevel")));
                                        assessmentPatternDetails.setParalevel(content_cursor.getString(content_cursor.getColumnIndex("paralevel")));
                                        assessmentPatternDetails.setQlevelmarks(content_cursor.getString(content_cursor.getColumnIndex("qlevelmarks")));
                                        assessmentPatternDetails.setTopicname(content_cursor.getString(content_cursor.getColumnIndex("topicname")));
                                        assessmentPatternDetails.setExamId(content_cursor.getString(content_cursor.getColumnIndex("examId")));
                                        assessmentPatternDetails.setQtid(content_cursor.getString(content_cursor.getColumnIndex("qtid")));
                                        assessmentPatternDetails.setKeyworddetail(content_cursor.getString(content_cursor.getColumnIndex("keyworddetail")));
                                        assessmentPatternDetailsList.add(assessmentPatternDetails);
                                        AppDatabase.getDatabaseInstance(context).getAssessmentPatternDetailsDao().deletePatternDetailsByExamId(assessmentPatternDetails.getExamId());

                                        content_cursor.moveToNext();
                                    }
                                }
                                AppDatabase.getDatabaseInstance(context).getAssessmentPatternDetailsDao().insertAllPatternDetails(assessmentPatternDetailsList);
                                content_cursor.close();
                                tableCopyCount++;
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            try {
                                Cursor content_cursor;
                                content_cursor = db.rawQuery("SELECT * FROM CertificateTopicList", null);
                                List<CertificateTopicList> certificateTopicLists = new ArrayList<>();
                                if (content_cursor.moveToFirst()) {
                                    while (!content_cursor.isAfterLast()) {
                                        CertificateTopicList topicList = new CertificateTopicList();
                                        topicList.setCertificatequestion(content_cursor.getString(content_cursor.getColumnIndex("certificatequestion")));
                                        topicList.setCertificatekeyword(content_cursor.getString(content_cursor.getColumnIndex("certificatekeyword")));
                                        topicList.setSubjectid(content_cursor.getString(content_cursor.getColumnIndex("subjectid")));
                                        topicList.setExamid(content_cursor.getString(content_cursor.getColumnIndex("examid")));
                                        certificateTopicLists.add(topicList);
                                        AppDatabase.getDatabaseInstance(context).getCertificateTopicListDao()
                                                .deleteQuestionByExamIdSubId(topicList.getSubjectid(), topicList.getExamid());
                                        content_cursor.moveToNext();
                                    }
                                }
                                AppDatabase.getDatabaseInstance(context).getCertificateTopicListDao().insertAllCertificateTopicQuestions(certificateTopicLists);
                                content_cursor.close();
                                tableCopyCount++;
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            try {
                                Cursor content_cursor;
//                                List<DownloadMedia> DownloadMediaList = new ArrayList<>();
                                content_cursor = db.rawQuery("SELECT * FROM ScienceQuestion", null);
                                List<ScienceQuestion> scienceQuestionList = new ArrayList<>();
                                if (content_cursor.moveToFirst()) {
                                    while (!content_cursor.isAfterLast()) {
                                        ScienceQuestion scienceQuestion = new ScienceQuestion();
                                        scienceQuestion.setAnsdesc(content_cursor.getString(content_cursor.getColumnIndex("ansdesc")));
                                        scienceQuestion.setUpdatedby(content_cursor.getString(content_cursor.getColumnIndex("updatedby")));
                                        scienceQuestion.setQlevel(content_cursor.getString(content_cursor.getColumnIndex("qlevel")));
                                        scienceQuestion.setAddedby(content_cursor.getString(content_cursor.getColumnIndex("addedby")));
                                        scienceQuestion.setLanguageid(content_cursor.getString(content_cursor.getColumnIndex("languageid")));
                                        scienceQuestion.setActive(content_cursor.getString(content_cursor.getColumnIndex("active")));
                                        scienceQuestion.setLessonid(content_cursor.getString(content_cursor.getColumnIndex("lessonid")));
                                        scienceQuestion.setQtid(content_cursor.getString(content_cursor.getColumnIndex("qtid")));
                                        scienceQuestion.setQid(content_cursor.getString(content_cursor.getColumnIndex("qid")));
                                        scienceQuestion.setSubjectid(content_cursor.getString(content_cursor.getColumnIndex("subjectid")));
                                        scienceQuestion.setAddedtime(content_cursor.getString(content_cursor.getColumnIndex("addedtime")));
                                        scienceQuestion.setUpdatedtime(content_cursor.getString(content_cursor.getColumnIndex("updatedtime")));
                                        scienceQuestion.setPhotourl(content_cursor.getString(content_cursor.getColumnIndex("photourl")));
                                        scienceQuestion.setExamtime(content_cursor.getString(content_cursor.getColumnIndex("examtime")));
                                        scienceQuestion.setTopicid(content_cursor.getString(content_cursor.getColumnIndex("topicid")));
                                        scienceQuestion.setAnswer(content_cursor.getString(content_cursor.getColumnIndex("answer")));
                                        scienceQuestion.setOutofmarks(content_cursor.getString(content_cursor.getColumnIndex("outofmarks")));
                                        scienceQuestion.setQname(content_cursor.getString(content_cursor.getColumnIndex("qname")));
                                        scienceQuestion.setHint(content_cursor.getString(content_cursor.getColumnIndex("hint")));
                                        scienceQuestion.setExamid(content_cursor.getString(content_cursor.getColumnIndex("examid")));
                                        scienceQuestion.setPdid(content_cursor.getString(content_cursor.getColumnIndex("pdid")));
                                        scienceQuestion.setStartTime(content_cursor.getString(content_cursor.getColumnIndex("startTime")));
                                        scienceQuestion.setEndTime(content_cursor.getString(content_cursor.getColumnIndex("endTime")));
                                        scienceQuestion.setRevisitedStartTime(content_cursor.getString(content_cursor.getColumnIndex("revisitedStartTime")));
                                        scienceQuestion.setRevisitedEndTime(content_cursor.getString(content_cursor.getColumnIndex("revisitedEndTime")));
                                        scienceQuestion.setMarksPerQuestion(content_cursor.getString(content_cursor.getColumnIndex("marksPerQuestion")));
                                        scienceQuestion.setPaperid(content_cursor.getString(content_cursor.getColumnIndex("paperid")));
                                        scienceQuestion.setIsAttempted((content_cursor.getInt(content_cursor.getColumnIndex("isAttempted"))) == 1 ? true : false);
                                        scienceQuestion.setIsCorrect((content_cursor.getInt(content_cursor.getColumnIndex("isCorrect"))) == 1 ? true : false);
                                        scienceQuestion.setIsQuestionFromSDCard(true);
                                        scienceQuestion.setIsParaQuestion(content_cursor.getInt((content_cursor.getColumnIndex("IsParaQuestion"))) == 1);
                                        scienceQuestion.setRefParaID(content_cursor.getString(content_cursor.getColumnIndex("RefParaID")));
                                        scienceQuestion.setAppVersion(content_cursor.getString(content_cursor.getColumnIndex("AppVersion")));
                                        scienceQuestion.setUserAnswerId(content_cursor.getString(content_cursor.getColumnIndex("userAnswerId")));
                                        scienceQuestion.setUserAnswer(content_cursor.getString(content_cursor.getColumnIndex("userAnswer")));

                                        if (scienceQuestion.getPhotourl() != null && !scienceQuestion.getPhotourl().equalsIgnoreCase("")) {
                                            String[] splittedName = scienceQuestion.getPhotourl().split("/");
                                            String FName = splittedName[splittedName.length - 1];
                                            String fileName = Assessment_Utility.getFileName(scienceQuestion.getQid(), scienceQuestion.getPhotourl());
                                            String localSDPath = AssessmentApplication.SDCardPathForOffline + Assessment_Constants.STORE_DOWNLOADED_MEDIA_PATH + "/" + FName;
                                            scienceQuestion.setPhotourl(localSDPath);

                                            /* DownloadMedia downloadMedia = new DownloadMedia();
                                            downloadMedia.setPhoto Url(localSDPath);
                                            downloadMedia.setQtId(content_cursor.getString(content_cursor.getColumnIndex("qtid")));
                                            downloadMedia.setqId(content_cursor.getString(content_cursor.getColumnIndex("qid")));
                                            downloadMedia.setMediaType("questionImage");
                                            DownloadMediaList.add(downloadMedia);
                                            AppDatabase.getDatabaseInstance(context).getDownloadMediaDao().deleteByQIdAndQtid(content_cursor.getString(content_cursor.getColumnIndex("qtid"))
                                                    , content_cursor.getString(content_cursor.getColumnIndex("qid")));*/

                                        }
                                        scienceQuestionList.add(scienceQuestion);
                                        AppDatabase.getDatabaseInstance(context).getScienceQuestionDao().deleteQuestionByQID(scienceQuestion.getQid());

                                        content_cursor.moveToNext();
                                    }
                                }
                                AppDatabase.getDatabaseInstance(context).getScienceQuestionDao().insertAllQuestions(scienceQuestionList);
                                AppDatabase.getDatabaseInstance(context).getScienceQuestionDao().replaceNewLineForQuestions();
                                AppDatabase.getDatabaseInstance(context).getScienceQuestionDao().replaceNewLineForQuestions2();

                                content_cursor.close();
                                tableCopyCount++;
/*  if (DownloadMediaList.size() > 0) {
                                    AppDatabase.getDatabaseInstance(context).getDownloadMediaDao().insertAllMedia(DownloadMediaList);
                                }*/
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            try {
                                Cursor content_cursor;
                                content_cursor = db.rawQuery("SELECT * FROM ScienceQuestionChoice", null);
                                List<ScienceQuestionChoice> scienceQuestionChoiceList = new ArrayList<>();
                                if (content_cursor.moveToFirst()) {
                                    while (!content_cursor.isAfterLast()) {
                                        ScienceQuestionChoice scienceQuestionChoice = new ScienceQuestionChoice();
                                        scienceQuestionChoice.setQcid(content_cursor.getString(content_cursor.getColumnIndex("qcid")));
                                        scienceQuestionChoice.setQid(content_cursor.getString(content_cursor.getColumnIndex("qid")));
                                        scienceQuestionChoice.setMatchingname(content_cursor.getString(content_cursor.getColumnIndex("matchingname")));
                                        scienceQuestionChoice.setChoicename(content_cursor.getString(content_cursor.getColumnIndex("choicename")));
                                        scienceQuestionChoice.setCorrect(content_cursor.getString(content_cursor.getColumnIndex("correct")));
                                        scienceQuestionChoice.setMatchingurl(content_cursor.getString(content_cursor.getColumnIndex("matchingurl")));
                                        scienceQuestionChoice.setChoiceurl(content_cursor.getString(content_cursor.getColumnIndex("choiceurl")));
                                        scienceQuestionChoice.setAppVersionChoice(content_cursor.getString(content_cursor.getColumnIndex("AppVersionChoice")));

                                        AppDatabase.getDatabaseInstance(context).getScienceQuestionChoicesDao().deleteQuestionChoicesByQID(content_cursor.getString(content_cursor.getColumnIndex("qid")));
                                        if (content_cursor.getString(content_cursor.getColumnIndex("choiceurl")) != null && !content_cursor.getString(content_cursor.getColumnIndex("choiceurl")).equalsIgnoreCase("")) {
                                            String fileName = Assessment_Utility.getFileName(content_cursor.getString(content_cursor.getColumnIndex("qid")), content_cursor.getString(content_cursor.getColumnIndex("choiceurl")));
                                            String localSDPath = AssessmentApplication.SDCardPathForOffline + Assessment_Constants.STORE_DOWNLOADED_MEDIA_PATH + "/" + fileName;
                                            scienceQuestionChoice.setChoiceurl(localSDPath);
                                             /* DownloadMedia downloadMedia = new DownloadMedia();
                                            downloadMedia.setPhotoUrl(localSDPath);
                                            downloadMedia.setqId(content_cursor.getString(content_cursor.getColumnIndex("qid")));
                                            downloadMedia.setMediaType("optionImage");
                                            DownloadMediaList.add(downloadMedia);*/
                                        }
                                        if (content_cursor.getString(content_cursor.getColumnIndex("matchingurl")) != null && !content_cursor.getString(content_cursor.getColumnIndex("matchingurl")).equalsIgnoreCase("")) {
                                            String fileName = Assessment_Utility.getFileName(content_cursor.getString(content_cursor.getColumnIndex("qid")), content_cursor.getString(content_cursor.getColumnIndex("matchingurl")));
                                            final String localSDPath = AssessmentApplication.SDCardPathForOffline + Assessment_Constants.STORE_DOWNLOADED_MEDIA_PATH + "/" + fileName;
                                            scienceQuestionChoice.setMatchingurl(localSDPath);
                                           /* DownloadMedia downloadMedia = new DownloadMedia();
                                            downloadMedia.setPhotoUrl(localSDPath);
                                            downloadMedia.setqId(content_cursor.getString(content_cursor.getColumnIndex("qid")));
                                            downloadMedia.setMediaType("optionImage");
                                            DownloadMediaList.add(downloadMedia);*/
                                        }
                                        scienceQuestionChoice.setIsQuestionFromSDCard(true);

                                        scienceQuestionChoiceList.add(scienceQuestionChoice);
                                        content_cursor.moveToNext();
                                    }
                                }
                               /* if (DownloadMediaList.size() > 0) {
                                    AppDatabase.getDatabaseInstance(context).getDownloadMediaDao().insertAllMedia(DownloadMediaList);
                                }*/
                                AppDatabase.getDatabaseInstance(context).getScienceQuestionChoicesDao().insertAllQuestionChoices(scienceQuestionChoiceList);
                                content_cursor.close();
//                                FastSave.getInstance().saveBoolean(Assessment_Constants.SDCARD_OFFLINE_PATH_SAVED, true);
                                copySuccessful = true;
                                tableCopyCount++;
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            try {
                                Cursor content_cursor;
                                content_cursor = db.rawQuery("SELECT * FROM AssessmentLanguages", null);
                                List<AssessmentLanguages> languages = new ArrayList<>();
                                if (content_cursor.moveToFirst()) {
                                    while (!content_cursor.isAfterLast()) {
                                        AssessmentLanguages language = new AssessmentLanguages();
                                        language.setLanguageid(content_cursor.getString(content_cursor.getColumnIndex("languageid")));
                                        language.setLanguagename(content_cursor.getString(content_cursor.getColumnIndex("languagename")));
                                        languages.add(language);
                                        AppDatabase.getDatabaseInstance(context).getLanguageDao().deleteByLangId(language.getLanguageid());

                                        content_cursor.moveToNext();
                                    }
                                }
                                AppDatabase.getDatabaseInstance(context).getLanguageDao().insertAllLanguages(languages);
                                content_cursor.close();
                                tableCopyCount++;
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            try {
                                Cursor content_cursor;
                                content_cursor = db.rawQuery("SELECT * FROM AssessmentSubjects", null);
                                List<AssessmentSubjects> subjects = new ArrayList<>();
                                if (content_cursor.moveToFirst()) {
                                    while (!content_cursor.isAfterLast()) {
                                        AssessmentSubjects assessmentSubject = new AssessmentSubjects();
                                        assessmentSubject.setSubjectid(content_cursor.getString(content_cursor.getColumnIndex("subjectid")));
                                        assessmentSubject.setSubjectname(content_cursor.getString(content_cursor.getColumnIndex("subjectname")));
                                        assessmentSubject.setLanguageid(content_cursor.getString(content_cursor.getColumnIndex("languageid")));
                                        subjects.add(assessmentSubject);
                                        AppDatabase.getDatabaseInstance(context).getSubjectDao().deleteSubjectsByLangIdSubId(assessmentSubject.getSubjectid(), assessmentSubject.getLanguageid());

                                        content_cursor.moveToNext();
                                    }
                                }
                                AppDatabase.getDatabaseInstance(context).getSubjectDao().insertAllSubjects(subjects);
                                content_cursor.close();
                                tableCopyCount++;
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            try {
                                Cursor content_cursor;
                                content_cursor = db.rawQuery("SELECT * FROM AssessmentTest", null);
                                List<AssessmentTest> assessmentTests = new ArrayList<>();
                                if (content_cursor.moveToFirst()) {
                                    while (!content_cursor.isAfterLast()) {
                                        AssessmentTest assessmentTest = new AssessmentTest();
                                        assessmentTest.setExamname(content_cursor.getString(content_cursor.getColumnIndex("examname")));
                                        assessmentTest.setLanguageId(content_cursor.getString(content_cursor.getColumnIndex("languageId")));
                                        assessmentTest.setExamid(content_cursor.getString(content_cursor.getColumnIndex("examid")));
                                        assessmentTest.setSubjectid(content_cursor.getString(content_cursor.getColumnIndex("subjectid")));
                                        assessmentTest.setSubjectname(content_cursor.getString(content_cursor.getColumnIndex("subjectname")));
                                        assessmentTest.setExamtype(content_cursor.getString(content_cursor.getColumnIndex("examtype")));
                                        assessmentTests.add(assessmentTest);
                                        AppDatabase.getDatabaseInstance(context).getTestDao().deleteTestsByLangIdAndSubId(assessmentTest.getSubjectid(), assessmentTest.getLanguageId());

                                        content_cursor.moveToNext();
                                    }
                                }
                                AppDatabase.getDatabaseInstance(context).getTestDao().insertAllTest(assessmentTests);
                                content_cursor.close();
                                tableCopyCount++;

                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            BackupDatabase.backup(context);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return null;
                }

                @Override
                protected void onProgressUpdate(Integer... values) {
                }

                @Override
                protected void onPostExecute(Void aVoid) {
                    //addStartTime();
                    super.onPostExecute(aVoid);
                    splashView.dismissProgressDialog();
                    BackupDatabase.backup(context);
                    if (tableCopyCount > 7)
                        FastSave.getInstance().saveBoolean(SDCARD_OFFLINE_PATH_SAVED, true);
                    splashView.showButton();
                }

            }.execute();
        } catch (
                Exception e) {
            e.printStackTrace();
        }

    }


    /**
     * Make initial metadata entries in status table
     */
    public static void doInitialEntries(Context context) {
        try {
            Status status;
            status = new Status();

            String key = "DeviceId",
                    value = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
            setStatusTableEntries(status, key, value, context);

            key = "CRLID";
            value = "default";
            setStatusTableEntries(status, key, value, context);

            key = "DeviceName";
            value = Assessment_Utility.getDeviceName();
            setStatusTableEntries(status, key, value, context);

            key = "gpsFixDuration";
            value = "";
            setStatusTableEntries(status, key, value, context);

            key = "prathamCode";
            value = "";
            setStatusTableEntries(status, key, value, context);

            key = "apkType";
            if (AssessmentApplication.isTablet)
                value = "Tablet";
            else value = "Smart phone";
            setStatusTableEntries(status, key, value, context);

            key = "Latitude";
            value = "";
            setStatusTableEntries(status, key, value, context);

            key = "Longitude";
            value = "";
            setStatusTableEntries(status, key, value, context);

            key = "GPSDateTime";
            value = "";
            setStatusTableEntries(status, key, value, context);

            key = "CurrentSession";
            value = "NA";
            setStatusTableEntries(status, key, value, context);


            key = "SdCardPath";
            value = "NA";
            setStatusTableEntries(status, key, value, context);

            key = "AppLang";
            value = "NA";
            setStatusTableEntries(status, key, value, context);

            key = "AppStartDateTime";
            value = "NA";
            setStatusTableEntries(status, key, value, context);

            key = "ActivatedForGroups";
            value = "NA";
            setStatusTableEntries(status, key, value, context);

            key = "programId";
            value = "1";
            setStatusTableEntries(status, key, value, context);

            key = "group1";
            value = "NA";
            setStatusTableEntries(status, key, value, context);

            key = "group2";
            value = "NA";
            setStatusTableEntries(status, key, value, context);

            key = "group3";
            value = "NA";
            setStatusTableEntries(status, key, value, context);


            key = "group4";
            value = "NA";
            setStatusTableEntries(status, key, value, context);

            key = "group5";
            value = "NA";
            setStatusTableEntries(status, key, value, context);

            key = "village";
            value = "NA";
            setStatusTableEntries(status, key, value, context);

            key = "ActivatedDate";
            value = "NA";
            setStatusTableEntries(status, key, value, context);

            key = "AssessmentSession";
            value = "NA";
            setStatusTableEntries(status, key, value, context);

            key = "AndroidID";
            value = "NA";
            setStatusTableEntries(status, key, value, context);

            key = "DBVersion";
            value = "NA";
            setStatusTableEntries(status, key, value, context);

            key = "SerialID";
            value = Assessment_Utility.getDeviceSerialID();
            setStatusTableEntries(status, key, value, context);

            key = "OsVersionName";
            value = Assessment_Utility.getOSVersion();
            setStatusTableEntries(status, key, value, context);

            key = "OsVersionNum";
            value = Assessment_Utility.getOSVersionNo();
            setStatusTableEntries(status, key, value, context);

            key = "AvailableStorage";
            value = Assessment_Utility.getAvailableStorage();
            setStatusTableEntries(status, key, value, context);

            key = "ScreenResolution";
            value = Assessment_Utility.getScreenResolution((AppCompatActivity) context);
            setStatusTableEntries(status, key, value, context);

            key = "Manufacturer";
            value = Assessment_Utility.getManufacturer();
            setStatusTableEntries(status, key, value, context);

            key = "Model";
            value = Assessment_Utility.getModel();
            setStatusTableEntries(status, key, value, context);

            key = "ApiLevel";
            value = Assessment_Utility.getApiLevel() + "";
            setStatusTableEntries(status, key, value, context);

            key = "InternalStorageSize";
            value = Assessment_Utility.getInternalStorageSize();
            setStatusTableEntries(status, key, value, context);


            key = "AppBuildDate";
            value = Assessment_Constants.APP_BUILD_DATE;
            setStatusTableEntries(status, key, value, context);

            WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            WifiInfo wInfo = wifiManager.getConnectionInfo();
            String macAddress = wInfo.getMacAddress();
            key = "wifiMAC";
            value = macAddress;
            setStatusTableEntries(status, key, value, context);

            setAppName(status, context);
            setAppVersion(status, context);

//            addStartTime(status,context);
            String appStartTime = AssessmentApplication.getCurrentDateTime();
            setStatusTableEntries(status, "AppStartDateTime", appStartTime, context);

//            getSdCardPath();
            requestLocation(context);

            FastSave.getInstance().saveBoolean(Assessment_Constants.INITIAL_ENTRIES, true);
            BackupDatabase.backup(context);


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void setStatusTableEntries(Status status, String key, String value, Context
            context) {

        if (AppDatabase.getDatabaseInstance(context).getStatusDao().getKey(key) != null) {
            AppDatabase.getDatabaseInstance(context).getStatusDao().updateValue(key, value);
        } else {
            status.setStatusKey(key);
            status.setValue(value);
            AppDatabase.getDatabaseInstance(context).getStatusDao().insert(status);
        }

    }


    private static void requestLocation(Context context) {
        new LocationService(context).checkLocation();
    }

    private static void setAppVersion(Status status, Context context) {
        PackageInfo pInfo = null;
        String verCode = "";
        try {
            pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            verCode = pInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        setStatusTableEntries(status, "apkVersion", verCode, context);

    }

    private static void setAppName(Status status, Context context) {
        String appname = "";
        CharSequence c;
        ActivityManager am = (ActivityManager) context.getSystemService(ACTIVITY_SERVICE);
        List l = am.getRunningAppProcesses();
        Iterator i = l.iterator();
        PackageManager pm = context.getPackageManager();
        ActivityManager.RunningAppProcessInfo info = (ActivityManager.RunningAppProcessInfo) (i.next());
        try {
            c = pm.getApplicationLabel(pm.getApplicationInfo(info.processName, PackageManager.GET_META_DATA));
            appname = c.toString();
            Log.w("LABEL", c.toString());
        } catch (Exception e) {
        }


        String key = "appName";
        String value = appname;
        setStatusTableEntries(status, key, value, context);


    }


}