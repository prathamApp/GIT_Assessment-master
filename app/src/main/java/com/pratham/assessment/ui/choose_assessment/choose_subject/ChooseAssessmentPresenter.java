package com.pratham.assessment.ui.choose_assessment.choose_subject;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.StringRequestListener;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.pratham.assessment.AssessmentApplication;
import com.pratham.assessment.R;
import com.pratham.assessment.async.GetLatestVersion;
import com.pratham.assessment.constants.APIs;
import com.pratham.assessment.constants.Assessment_Constants;
import com.pratham.assessment.custom.FastSave;
import com.pratham.assessment.custom.custom_dialogs.PushDataDialog;
import com.pratham.assessment.database.AppDatabase;
import com.pratham.assessment.database.BackupDatabase;
import com.pratham.assessment.domain.AssessmentLanguages;
import com.pratham.assessment.domain.AssessmentPaperPattern;
import com.pratham.assessment.domain.AssessmentSubjects;
import com.pratham.assessment.domain.NIOSExam;
import com.pratham.assessment.domain.NIOSExamTopics;
import com.pratham.assessment.domain.Student;
import com.pratham.assessment.ui.choose_assessment.science.ScienceAssessmentActivity;
import com.pratham.assessment.utilities.Assessment_Utility;

import org.androidannotations.annotations.EBean;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

import static com.pratham.assessment.AssessmentApplication.sharedPreferences;
import static com.pratham.assessment.constants.Assessment_Constants.CHHATTISGARH_LANGUAGE_ID;
import static com.pratham.assessment.constants.Assessment_Constants.CHHATTISGARH_PROGRAM_ID;
import static com.pratham.assessment.constants.Assessment_Constants.CHHATTISGARH_SUBJECT_ID;
import static com.pratham.assessment.constants.Assessment_Constants.CHHATTISGARH_SUBJECT_NAME;
import static com.pratham.assessment.constants.Assessment_Constants.CURRENT_VERSION;
import static com.pratham.assessment.constants.Assessment_Constants.LANGUAGE;
import static com.pratham.assessment.utilities.Assessment_Utility.checkConnectedToRPI;

@EBean
public class ChooseAssessmentPresenter implements ChooseAssessmentContract.ChooseAssessmentPresenter {

    Context context;
    ChooseAssessmentContract.ChooseAssessmentView assessView;
    ArrayList<AssessmentSubjects> contentTableList = new ArrayList<>();
    ArrayList<NIOSExam> NIOSSubjectList = new ArrayList<>();
    ArrayList<String> nodeIds;
    ArrayList<AssessmentSubjects> downloadedContentTableList;
    /*List<AssessmentTestModal> assessmentTestModals;
    List<AssessmentTest> assessmentTests = new ArrayList<>();*/

    public ChooseAssessmentPresenter(Context context) {
        this.context = context;
        this.assessView = (ChooseAssessmentContract.ChooseAssessmentView) context;
        nodeIds = new ArrayList<>();
    }


    @Override
    public void copyListData() {
        getListData();

    }


    private void getListData() {
        getLanguageData();

        String currentStudentId = FastSave.getInstance().getString("currentStudentID", "");
        Student student = AppDatabase.getDatabaseInstance(context).getStudentDao().getStudent(currentStudentId);

        if (AssessmentApplication.wiseF.isDeviceConnectedToMobileOrWifiNetwork()) {
            if (student != null)
                if (student.getProgramId() != null && student.getState() != null) {
                    assessView.clearContentList();
                    contentTableList.clear();
                    //this code is only for CHHATTISGARH program.
                    // if program is CHHATTISGARH show only specific exams saved in constant. no other exams to be displayed.
                    if (student.getProgramId().equalsIgnoreCase(CHHATTISGARH_PROGRAM_ID)
                            && student.getState().trim().equalsIgnoreCase(Assessment_Constants.CHHATTISGARH)) {
                        AssessmentSubjects subjects = new AssessmentSubjects();
                        subjects.setLanguageid(CHHATTISGARH_LANGUAGE_ID);
                        subjects.setSubjectid(CHHATTISGARH_SUBJECT_ID);
                        subjects.setSubjectname(CHHATTISGARH_SUBJECT_NAME);
                        contentTableList.add(subjects);

                        AppDatabase.getDatabaseInstance(context).getSubjectDao().deleteSubjectsByLangId(Assessment_Constants.SELECTED_LANGUAGE);
                        AppDatabase.getDatabaseInstance(context).getSubjectDao().insertAllSubjects(contentTableList);
                        assessView.addContentToViewList(contentTableList);
                        assessView.notifyAdapter();
                    } else {
                        if (FastSave.getInstance().getBoolean("enrollmentNoLogin", false)) {
                            boolean isRPI = checkConnectedToRPI();
                            if (isRPI)
                                getSubjectData();
                            else
                                getNIOSSubjects();
                        }
                        else
                            getSubjectData();
                    }
                } else {
                    if (FastSave.getInstance().getBoolean("enrollmentNoLogin", false)) {
                        boolean isRPI = checkConnectedToRPI();
                        if (isRPI)
                            getSubjectData();
                        else
                            getNIOSSubjects();
                    }
                    else
                        getSubjectData();
                }
        } else {
            if (student != null)
                if (student.getProgramId() != null && student.getState() != null) {
                    if (student.getProgramId().equalsIgnoreCase(CHHATTISGARH_PROGRAM_ID)
                            && student.getState().trim().equalsIgnoreCase(Assessment_Constants.CHHATTISGARH)) {
                        downloadedContentTableList = (ArrayList<AssessmentSubjects>) AppDatabase.getDatabaseInstance(context).getSubjectDao().getChhattisgarhSubject(CHHATTISGARH_SUBJECT_ID);
                    } else {
                        if (FastSave.getInstance().getBoolean("enrollmentNoLogin", false))
                            getOfflineNIOSSubjects();
                        else
                            downloadedContentTableList = (ArrayList<AssessmentSubjects>) AppDatabase.getDatabaseInstance(context).getSubjectDao().getAllSubjectsByLangId(Assessment_Constants.SELECTED_LANGUAGE);
                    }
                } else if (FastSave.getInstance().getBoolean("enrollmentNoLogin", false))
                    getOfflineNIOSSubjects();
                else
                    downloadedContentTableList = (ArrayList<AssessmentSubjects>) AppDatabase.getDatabaseInstance(context).getSubjectDao().getAllSubjectsByLangId(Assessment_Constants.SELECTED_LANGUAGE);
            assessView.clearContentList();
     /*   }
        if (downloadedContentTableList.size() <= 0) {
        }else {*/
            BackupDatabase.backup(context);
            contentTableList.clear();
            contentTableList.addAll(downloadedContentTableList);
            assessView.addContentToViewList(contentTableList);
            assessView.notifyAdapter();
        }

       /* new AsyncTask<Object, Void, Object>() {
            @Override
            protected Object doInBackground(Object[] objects) {
                try {

                    try {
                        *//*for (int j = 0; j < downloadedContentTableList.size(); j++) {
                            ContentTable contentTable = new ContentTable();
                            contentTable.setNodeId("" + downloadedContentTableList.get(j).getNodeId());
                            contentTable.setNodeType("" + downloadedContentTableList.get(j).getNodeType());
                            contentTable.setNodeTitle("" + downloadedContentTableList.get(j).getNodeTitle());
                            contentTable.setNodeKeywords("" + downloadedContentTableList.get(j).getNodeKeywords());
                            contentTable.setNodeAge("" + downloadedContentTableList.get(j).getNodeAge());
                            contentTable.setNodeDesc("" + downloadedContentTableList.get(j).getNodeDesc());
                            contentTable.setNodeServerImage("" + downloadedContentTableList.get(j).getNodeServerImage());
                            contentTable.setNodeImage("" + downloadedContentTableList.get(j).getNodeImage());
                            contentTable.setResourceId("" + downloadedContentTableList.get(j).getResourceId());
                            contentTable.setResourceType("" + downloadedContentTableList.get(j).getNodeType());
                            contentTable.setResourcePath("" + downloadedContentTableList.get(j).getResourcePath());
                            contentTable.setParentId("" + downloadedContentTableList.get(j).getParentId());
                            contentTable.setLevel("" + downloadedContentTableList.get(j).getLevel());
                            contentTable.setContentType(downloadedContentTableList.get(j).getContentType());
                            contentTable.setIsDownloaded("true");
                            contentTable.setOnSDCard(true);

                        }*//*


                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return null;
                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
            }

            @Override
            protected void onPostExecute(Object o) {

            }
        }.execute();*/
    }

    private void getOfflineNIOSSubjects() {
        String currentStudentID = FastSave.getInstance().getString("currentStudentID", "");
        downloadedContentTableList = new ArrayList<>();
        List<NIOSExam> AllExams = AppDatabase.getDatabaseInstance(context).getNiosExamDao().getAllSubjectsByStudId(currentStudentID);
        if (AllExams != null && AllExams.size() > 0) {
            for (int i = 0; i < AllExams.size(); i++) {
                List<NIOSExamTopics> NIOSTopics = AppDatabase.getDatabaseInstance(context).getNiosExamTopicDao().getTopicIdByExamId(AllExams.get(i).getExamid());
                if (NIOSTopics != null && NIOSTopics.size() > 0)
                    for (int j = 0; j < NIOSTopics.size(); j++) {
                        AssessmentSubjects subjects = new AssessmentSubjects();
                        subjects.setLanguageid(NIOSTopics.get(j).getLanguageid());
                        subjects.setSubjectid(NIOSTopics.get(j).getSubjectid());
                        subjects.setSubjectname(NIOSTopics.get(j).getSubjectname());
                        if (!containsId(downloadedContentTableList, subjects.getSubjectid()))
                            downloadedContentTableList.add(subjects);
                    }
            }
        } else {
//            Toast.makeText(context, R.string.no_exams, Toast.LENGTH_SHORT).show();
//            Assessment_Utility.showDialog(context,context.getString(R.string.no_exams));
            assessView.showNoExamLayout(true);
        }
    }

    private void getOfflineNIOSSubjectsRPI() {
        String currentStudentID = FastSave.getInstance().getString("currentStudentID", "");
        downloadedContentTableList = new ArrayList<>();
        List<NIOSExam> AllExams = AppDatabase.getDatabaseInstance(context).getNiosExamDao().getAllSubjectsByStudId(currentStudentID);
        if (AllExams != null && AllExams.size() > 0) {
            for (int k = 0; k < AllExams.size(); k++) {
                List<NIOSExamTopics> NIOSTopics = AppDatabase.getDatabaseInstance(context).getNiosExamTopicDao().getTopicIdByExamId(AllExams.get(k).getExamid());
                if (NIOSTopics != null && NIOSTopics.size() > 0)
                    for (int l = 0; l < NIOSTopics.size(); l++) {
                        AssessmentSubjects subjects = new AssessmentSubjects();
                        subjects.setLanguageid(NIOSTopics.get(l).getLanguageid());
                        subjects.setSubjectid(NIOSTopics.get(l).getSubjectid());
                        subjects.setSubjectname(NIOSTopics.get(l).getSubjectname());
                        if (!containsId(downloadedContentTableList, subjects.getSubjectid()))
                        {

                            List<NIOSExam> AllExams_ = AppDatabase.getDatabaseInstance(context).getNiosExamDao().getAllSubjectsByStudIdSubId(currentStudentID, subjects.getSubjectid());

                            if (AllExams_ != null && AllExams_.size() > 0)
                                for (int i = 0; i < AllExams_.size(); i++) {
                                    List<NIOSExamTopics> allTopics = AppDatabase.getDatabaseInstance(context).getNiosExamTopicDao().getTopicIdByExamId(AllExams_.get(i).getExamid());
                                    if (allTopics != null && allTopics.size() > 0)
                                        for (int j = 0; j < allTopics.size(); j++) {
                                            if (allTopics.get(j).getLanguageid().equalsIgnoreCase(FastSave.getInstance().getString(LANGUAGE, "1"))) {
                                                if (checkIfStudentExamExist(AllExams_, allTopics.get(j).getExamid())) {
                                                    //                            if(if(AllExams.get(i).getExamid())allTopics.get(j).getExamid())

                                                    AssessmentPaperPattern assessmentPaperPatterns =
                                                            AppDatabase.getDatabaseInstance(context).getAssessmentPaperPatternDao()
                                                                    .getAssessmentPaperPatternsByExamId(allTopics.get(j).getExamid());
                                                    if (assessmentPaperPatterns != null) {
                                                        //generatePaperPattern();
                                                        downloadedContentTableList.add(subjects);
                                                    } else {
                                                        //finish();
                                                        //Toast.makeText(this, context.getString(R.string.connect_to_internet_to_download_paper_format), Toast.LENGTH_SHORT).show();
                                                    }
                                                }
                                            }
                                        }
                                }

                        }
                    }
            }
        }  else {
//            Toast.makeText(context, R.string.no_exams, Toast.LENGTH_SHORT).show();
//            Assessment_Utility.showDialog(context,context.getString(R.string.no_exams));
            assessView.showNoExamLayout(true);
        }
    }

    private boolean checkIfStudentExamExist(List<NIOSExam> allExams, String examid) {
        boolean exists = false;
        for (int i = 0; i < allExams.size(); i++) {
            if (allExams.get(i).getExamid().equalsIgnoreCase(examid)) {
                exists = true;
                break;
            }

        }

        return exists;
    }

    /**
     * for enrollment login, exams and subjects are assigned to enrollment ids from server side.
     *  subjects and exams are saved in NIOSExam and NIOSExamTopics tables.
     * **** when app is updated on play store, this api is updated on server with new playstore version.***
     */
    private void getNIOSSubjects() {
        NIOSSubjectList.clear();
        contentTableList.clear();
        String url = APIs.AssessmentEnrollmentNoExamAPI + FastSave.getInstance()
                .getString("currentStudentID", "") + "&appversion=" + /*"1.2.0"*/Assessment_Utility.getCurrentVersion(context);
        Log.i("url12345_getNIOSSubjects",url);
        final ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setMessage(context.getString(R.string.loading_subjects));
        progressDialog.show();
        AndroidNetworking.get(url)
                .build()
                .getAsString(new StringRequestListener() {
                    @RequiresApi(api = Build.VERSION_CODES.N)
                    @Override
                    public void onResponse(String response) {
                        Gson gson = new Gson();
                        Type listType = new TypeToken<List<NIOSExam>>() {
                        }.getType();
                        NIOSSubjectList = gson.fromJson(response, listType);
//                            assessmentTests.clear();
//                        contentTableList = new ArrayList<>();
                        JSONArray jsonArray = null;
                        try {
                            if (NIOSSubjectList.size() > 0) {
                                AppDatabase.getDatabaseInstance(context).getNiosExamDao().deleteByStudId(Assessment_Constants.currentStudentID);
                                List<NIOSExamTopics> NIOSTopics;
                                for (int i = 0; i < NIOSSubjectList.size(); i++) {
                                    NIOSTopics = NIOSSubjectList.get(i).getLststudentexamtopic();
                                    if (NIOSTopics.size() > 0) {
                                        for (int j = 0; j < NIOSTopics.size(); j++) {
                                            NIOSSubjectList.get(i).setSubjectid(NIOSTopics.get(j).getSubjectid());
                                            AssessmentSubjects subjects = new AssessmentSubjects();
                                            subjects.setLanguageid(NIOSTopics.get(j).getLanguageid());
                                            subjects.setSubjectid(NIOSTopics.get(j).getSubjectid());
                                            subjects.setSubjectname(NIOSTopics.get(j).getSubjectname());
                                            if (!containsId(contentTableList, subjects.getSubjectid()))
                                                contentTableList.add(subjects);
                                            AppDatabase.getDatabaseInstance(context).getNiosExamTopicDao().insertAllTopics(NIOSTopics);
                                        }
                                    }
                                    AppDatabase.getDatabaseInstance(context).getNiosExamDao().insertAllExams(NIOSSubjectList);
                                }

                                if (progressDialog != null && progressDialog.isShowing()) {
                                    progressDialog.dismiss();
                                }
                                BackupDatabase.backup(context);
//                            contentTableList.addAll(downloadedContentTableList);
                                assessView.addContentToViewList(contentTableList);
                                assessView.notifyAdapter();
                                //getTopicData();
                            } else {
//                                Toast.makeText(context, R.string.no_exams, Toast.LENGTH_SHORT).show();
//                                Assessment_Utility.showDialog(context, context.getString(R.string.no_exams));
                                if (progressDialog != null && progressDialog.isShowing()) {
                                    progressDialog.dismiss();
                                }
                                assessView.showNoExamLayout(true);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(ANError anError) {
                        Toast.makeText(context, "Error in loading..Check internet connection", Toast.LENGTH_SHORT).show();
//                        AppDatabase.getDatabaseInstance(context).getAssessmentPaperPatternDao().deletePaperPatterns();
                        if (progressDialog != null && progressDialog.isShowing()) {
                            progressDialog.dismiss();
                        }
                        checkVersion();
                    }
                });


    }


   /* private void gotoPlayStore() {
        String currentVersion = Assessment_Utility.getCurrentVersion(context);
        String updatedVersion = sharedPreferences.getString(CURRENT_VERSION, "-1");
        if (updatedVersion != null) {
            if (updatedVersion.equalsIgnoreCase("-1")) {
                if (Assessment_Utility.isDataConnectionAvailable(context)) {
                    try {
                        new GetLatestVersion().execute().get();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else splashView.startApp();
            } else {
                if (updatedVersion != null && currentVersion != null && isCurrentVersionEqualsPlayStoreVersion(currentVersion, updatedVersion)) {
                    splashView.showUpdateDialog();
                } else
                    splashView.startApp();
            }
        }
    }
*/

    public boolean containsId(final ArrayList<AssessmentSubjects> list, final String id) {
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getSubjectid().equalsIgnoreCase(id))
                return true;
        }
        return false;
    }


    /**
     * if create profile login, Subjects will be loaded from server.
     */
    private void getSubjectData() {
        String url = "";
        boolean isRPI = checkConnectedToRPI();
        if (isRPI)
            url = APIs.AssessmentSubjectAPIRPI + Assessment_Constants.SELECTED_LANGUAGE;
        else url = APIs.AssessmentSubjectAPI + Assessment_Constants.SELECTED_LANGUAGE;

        Log.i("url12345",url);
        contentTableList.clear();
        final ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setMessage(context.getString(R.string.loading_subjects));
        AndroidNetworking.get(url)
                .build()
                .getAsString(new StringRequestListener() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONArray jsonArray;

                            if (!isRPI) {
                                jsonArray = new JSONArray(response);
                            } else {
                                JSONObject jsonObject = new JSONObject(response);
                                jsonArray = jsonObject.getJSONArray("results");
                            }

                            for (int i = 0; i < jsonArray.length(); i++) {
                                AssessmentSubjects assessmentSubjects = new AssessmentSubjects();
                                assessmentSubjects.setSubjectid(jsonArray.getJSONObject(i).getString("subjectid"));
                                assessmentSubjects.setSubjectname(jsonArray.getJSONObject(i).getString("subjectname"));
                                assessmentSubjects.setLanguageid(Assessment_Constants.SELECTED_LANGUAGE);
                                contentTableList.add(assessmentSubjects);
                            }
                            if (contentTableList.size() > 0) {
                                AppDatabase.getDatabaseInstance(context).getSubjectDao().deleteSubjectsByLangId(Assessment_Constants.SELECTED_LANGUAGE);
                                AppDatabase.getDatabaseInstance(context).getSubjectDao().insertAllSubjects(contentTableList);
                                if (progressDialog != null && progressDialog.isShowing())
                                    progressDialog.dismiss();
                                BackupDatabase.backup(context);
//                            contentTableList.addAll(downloadedContentTableList);
                                assessView.addContentToViewList(contentTableList);
                                assessView.notifyAdapter();
                                //getTopicData();
                            } else
//                                    Toast.makeText(context, R.string.no_subjects, Toast.LENGTH_SHORT).show();
//                        Assessment_Utility.showDialog(context,context.getString(R.string.no_subjects));
                                assessView.showNoExamLayout(true);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(ANError anError) {
                        Toast.makeText(context, R.string.error_in_loading_check_internet_connection, Toast.LENGTH_SHORT).show();
//                        AppDatabase.getDatabaseInstance(context).getAssessmentPaperPatternDao().deletePaperPatterns();
                        AppDatabase.getDatabaseInstance(context).getSubjectDao().deleteSubjectsByLangId(Assessment_Constants.SELECTED_LANGUAGE);
                        if (progressDialog != null && progressDialog.isShowing())
                            progressDialog.dismiss();
                    }
                });

    }
    private void getSubjectDataRPI() {
        String currentStudentID = FastSave.getInstance().getString("currentStudentID", "");
        downloadedContentTableList = new ArrayList<>();
        List<NIOSExam> AllExams = AppDatabase.getDatabaseInstance(context).getNiosExamDao().getAllSubjectsByStudId(currentStudentID);
        if (AllExams != null && AllExams.size() > 0) {
            for (int k = 0; k < AllExams.size(); k++) {
                List<NIOSExamTopics> NIOSTopics = AppDatabase.getDatabaseInstance(context).getNiosExamTopicDao().getTopicIdByExamId(AllExams.get(k).getExamid());
                if (NIOSTopics != null && NIOSTopics.size() > 0)
                    for (int l = 0; l < NIOSTopics.size(); l++) {
                        AssessmentSubjects subjects = new AssessmentSubjects();
                        subjects.setLanguageid(NIOSTopics.get(l).getLanguageid());
                        subjects.setSubjectid(NIOSTopics.get(l).getSubjectid());
                        subjects.setSubjectname(NIOSTopics.get(l).getSubjectname());
                        if (!containsId(downloadedContentTableList, subjects.getSubjectid()))
                        {

                            List<NIOSExam> AllExams_ = AppDatabase.getDatabaseInstance(context).getNiosExamDao().getAllSubjectsByStudIdSubId(currentStudentID, subjects.getSubjectid());

                            if (AllExams_ != null && AllExams_.size() > 0)
                                for (int i = 0; i < AllExams_.size(); i++) {
                                    List<NIOSExamTopics> allTopics = AppDatabase.getDatabaseInstance(context).getNiosExamTopicDao().getTopicIdByExamId(AllExams_.get(i).getExamid());
                                    if (allTopics != null && allTopics.size() > 0)
                                        for (int j = 0; j < allTopics.size(); j++) {
                                            if (allTopics.get(j).getLanguageid().equalsIgnoreCase(FastSave.getInstance().getString(LANGUAGE, "1"))) {
                                                if (checkIfStudentExamExist(AllExams_, allTopics.get(j).getExamid())) {
                                                    //                            if(if(AllExams.get(i).getExamid())allTopics.get(j).getExamid())

                                                    AssessmentPaperPattern assessmentPaperPatterns =
                                                            AppDatabase.getDatabaseInstance(context).getAssessmentPaperPatternDao()
                                                                    .getAssessmentPaperPatternsByExamId(allTopics.get(j).getExamid());
                                                    if (assessmentPaperPatterns != null) {
                                                        //generatePaperPattern();
                                                        downloadedContentTableList.add(subjects);
                                                    } else {
                                                        //finish();
                                                        //Toast.makeText(this, context.getString(R.string.connect_to_internet_to_download_paper_format), Toast.LENGTH_SHORT).show();
                                                    }
                                                }
                                            }
                                        }
                                }

                        }
                    }
            }
        }


        String url = "";
        boolean isRPI = checkConnectedToRPI();
        if (isRPI)
            url = APIs.AssessmentSubjectAPIRPI + Assessment_Constants.SELECTED_LANGUAGE;
        else url = APIs.AssessmentSubjectAPI + Assessment_Constants.SELECTED_LANGUAGE;

        Log.i("url12345",url);
        contentTableList.clear();
        final ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setMessage(context.getString(R.string.loading_subjects));
        AndroidNetworking.get(url)
                .build()
                .getAsString(new StringRequestListener() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONArray jsonArray;

                            if (!isRPI) {
                                jsonArray = new JSONArray(response);
                            } else {
                                JSONObject jsonObject = new JSONObject(response);
                                jsonArray = jsonObject.getJSONArray("results");
                            }

                            for (int i = 0; i < jsonArray.length(); i++) {
                                AssessmentSubjects assessmentSubjects = new AssessmentSubjects();
                                assessmentSubjects.setSubjectid(jsonArray.getJSONObject(i).getString("subjectid"));
                                assessmentSubjects.setSubjectname(jsonArray.getJSONObject(i).getString("subjectname"));
                                assessmentSubjects.setLanguageid(Assessment_Constants.SELECTED_LANGUAGE);
                                contentTableList.add(assessmentSubjects);
                            }
                            if (contentTableList.size() > 0) {
                                AppDatabase.getDatabaseInstance(context).getSubjectDao().deleteSubjectsByLangId(Assessment_Constants.SELECTED_LANGUAGE);
                                AppDatabase.getDatabaseInstance(context).getSubjectDao().insertAllSubjects(contentTableList);
                                if (progressDialog != null && progressDialog.isShowing())
                                    progressDialog.dismiss();
                                BackupDatabase.backup(context);
//                            contentTableList.addAll(downloadedContentTableList);
                                assessView.addContentToViewList(contentTableList);
                                assessView.notifyAdapter();
                                //getTopicData();
                            } else
//                                    Toast.makeText(context, R.string.no_subjects, Toast.LENGTH_SHORT).show();
//                        Assessment_Utility.showDialog(context,context.getString(R.string.no_subjects));
                                assessView.showNoExamLayout(true);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(ANError anError) {
                        Toast.makeText(context, R.string.error_in_loading_check_internet_connection, Toast.LENGTH_SHORT).show();
//                        AppDatabase.getDatabaseInstance(context).getAssessmentPaperPatternDao().deletePaperPatterns();
                        AppDatabase.getDatabaseInstance(context).getSubjectDao().deleteSubjectsByLangId(Assessment_Constants.SELECTED_LANGUAGE);
                        if (progressDialog != null && progressDialog.isShowing())
                            progressDialog.dismiss();
                    }
                });

    }

    /*private void getListData() {
        new AsyncTask<Object, Void, Object>() {
            @Override
            protected Object doInBackground(Object[] objects) {
                try {
                    downloadedContentTableList = AppDatabase.getDatabaseInstance(context).getContentTableDao().getContentData(nodeIds.get(nodeIds.size() - 1));
                    BackupDatabase.backup(context);

                    assessView.clearContentList();

                    try {
                        for (int j = 0; j < downloadedContentTableList.size(); j++) {
                            ContentTable contentTable = new ContentTable();
                            contentTable.setNodeId("" + downloadedContentTableList.get(j).getNodeId());
                            contentTable.setNodeType("" + downloadedContentTableList.get(j).getNodeType());
                            contentTable.setNodeTitle("" + downloadedContentTableList.get(j).getNodeTitle());
                            contentTable.setNodeKeywords("" + downloadedContentTableList.get(j).getNodeKeywords());
                            contentTable.setNodeAge("" + downloadedContentTableList.get(j).getNodeAge());
                            contentTable.setNodeDesc("" + downloadedContentTableList.get(j).getNodeDesc());
                            contentTable.setNodeServerImage("" + downloadedContentTableList.get(j).getNodeServerImage());
                            contentTable.setNodeImage("" + downloadedContentTableList.get(j).getNodeImage());
                            contentTable.setResourceId("" + downloadedContentTableList.get(j).getResourceId());
                            contentTable.setResourceType("" + downloadedContentTableList.get(j).getNodeType());
                            contentTable.setResourcePath("" + downloadedContentTableList.get(j).getResourcePath());
                            contentTable.setParentId("" + downloadedContentTableList.get(j).getParentId());
                            contentTable.setLevel("" + downloadedContentTableList.get(j).getLevel());
                            contentTable.setContentType(downloadedContentTableList.get(j).getContentType());
                            contentTable.setIsDownloaded("true");
                            contentTable.setOnSDCard(true);

                            assessView.addContentToViewList(contentTable);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return null;
                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
            }

            @Override
            protected void onPostExecute(Object o) {
                super.onPostExecute(o);
                assessView.notifyAdapter();
            }
        }.execute();
    }*/


    @Override
    public void versionObtained(String latestVersion) {
        if (latestVersion != null) {
            sharedPreferences.edit().putString(CURRENT_VERSION, latestVersion).apply();
//            checkVersion();
        /*    if (!latestVersion.equalsIgnoreCase(Assessment_Utility.getCurrentVersion(context)))
                updateApp();*/
        } else {
//            splashView.startApp();
            Toast.makeText(context, R.string.updated, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void startActivity(String activityName) {
    }


    private void getLanguageData() {
        String url = "";
        boolean isRPI = checkConnectedToRPI();
        if (isRPI)
            url = APIs.AssessmentLanguageAPIRPI;
        else url = APIs.AssessmentLanguageAPI;

        Log.i("url12345_getLanguageData",url);
        final ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setMessage(context.getString(R.string.loading));
        progressDialog.setCancelable(false);
        progressDialog.show();
        AndroidNetworking.get(url)
                .build()
                .getAsString(new StringRequestListener() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONArray jsonArray;

                            if (!isRPI) {
                                jsonArray = new JSONArray(response);
                            } else {
                                JSONObject jsonObject = new JSONObject(response);
                                jsonArray = jsonObject.getJSONArray("results");
                            }

                            if (progressDialog != null && progressDialog.isShowing())
                                progressDialog.dismiss();
                            List<AssessmentLanguages> assessmentLanguagesList = new ArrayList<>();

                            for (int i = 0; i < jsonArray.length(); i++) {
                                AssessmentLanguages assessmentLanguages = new AssessmentLanguages();
                                assessmentLanguages.setLanguageid(jsonArray.getJSONObject(i).getString("languageid"));
                                assessmentLanguages.setLanguagename(jsonArray.getJSONObject(i).getString("languagename"));
                                assessmentLanguagesList.add(assessmentLanguages);
                            }
                            if (assessmentLanguagesList.size() > 0)
                                AppDatabase.getDatabaseInstance(context).getLanguageDao().insertAllLanguages(assessmentLanguagesList);


                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(ANError anError) {
                        Toast.makeText(context, R.string.error_in_loading_check_internet_connection, Toast.LENGTH_SHORT).show();
//                        AppDatabase.getDatabaseInstance(getActivity()).getAssessmentPaperPatternDao().deletePaperPatterns();
  /*                      ((ChooseAssessmentActivity) getActivity()).frameLayout.setVisibility(View.GONE);
                        ((ChooseAssessmentActivity) getActivity()).rlSubject.setVisibility(View.VISIBLE);
                        ((ChooseAssessmentActivity) getActivity()).toggle_btn.setVisibility(View.VISIBLE);
                        getActivity().getSupportFragmentManager().popBackStackImmediate();*/
                        if (progressDialog != null && progressDialog.isShowing())
                            progressDialog.dismiss();
//                        selectTopicDialog.show();
                    }
                });

    }


    /***
     * check latest version if error in loading api
     */

    private void checkVersion() {
        String latestVersion = "";
        String currentVersion = Assessment_Utility.getCurrentVersion(context);
        Log.d("version::", "Current version = " + currentVersion);
        try {
            latestVersion = new GetLatestVersion(this, context).execute().get();
            Log.d("version::", "Latest version = " + latestVersion);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        // Force Update Code
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
                    Assessment_Utility.updateApp(context);
                });
                eject_btn.setOnClickListener(view -> {
                    pushDialog.dismiss();
                    Toast.makeText(context, R.string.update, Toast.LENGTH_SHORT).show();
                });
                pushDialog.show();

            } else
                Toast.makeText(context, R.string.error_in_loading_check_internet_connection, Toast.LENGTH_SHORT).show();

        } else {
            Toast.makeText(context, R.string.error_in_loading_check_internet_connection, Toast.LENGTH_SHORT).show();
        }
    }

}

