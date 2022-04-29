package com.pratham.assessment.ui.choose_assessment.fragments;

import android.app.ProgressDialog;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.StringRequestListener;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.pratham.assessment.AssessmentApplication;
import com.pratham.assessment.R;
import com.pratham.assessment.constants.APIs;
import com.pratham.assessment.constants.Assessment_Constants;
import com.pratham.assessment.custom.FastSave;
import com.pratham.assessment.database.AppDatabase;
import com.pratham.assessment.domain.AssessmentTest;
import com.pratham.assessment.domain.AssessmentTestModal;
import com.pratham.assessment.domain.NIOSExam;
import com.pratham.assessment.domain.NIOSExamTopics;
import com.pratham.assessment.ui.choose_assessment.choose_subject.ChooseAssessmentActivity;
import com.pratham.assessment.utilities.Assessment_Utility;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.pratham.assessment.constants.Assessment_Constants.LANGUAGE;
import static com.pratham.assessment.utilities.Assessment_Utility.checkConnectedToRPI;

/*import butterknife.BindView;
import butterknife.ButterKnife;*/


/**
 * fragment is used to display the all the exams of selected subjects
 */

@EFragment(R.layout.fragment_topic)
public class TopicFragment extends Fragment {
    List<AssessmentTestModal> assessmentTestModals;
    List<AssessmentTest> assessmentTests = new ArrayList<>();
    @ViewById(R.id.rv_topics)
    RecyclerView rv_topics;
    @ViewById(R.id.rl_no_exams)
    RelativeLayout rl_no_exams;
    @ViewById(R.id.tv_no_exams)
    TextView tv_no_exams;
    String subjectId, langId;
    ProgressDialog progressDialog;


    public TopicFragment() {
        // Required empty public constructor
    }

    @AfterViews
    public void init() {
        subjectId = FastSave.getInstance().getString("SELECTED_SUBJECT_ID", "1");
        langId = FastSave.getInstance().getString(LANGUAGE, "1");
        tv_no_exams.setText(R.string.no_exams);
        if (AssessmentApplication.wiseF.isDeviceConnectedToMobileOrWifiNetwork()) {
            if (FastSave.getInstance().getBoolean("enrollmentNoLogin", false))
                getNIOSExams();
            else
                getExamData();
        } else {
            if (FastSave.getInstance().getBoolean("enrollmentNoLogin", false))
                getNIOSExams();
            else
                getOfflineTests();
        }
    }


    /**
     * for enrollment login, exams are already stored in db when subjects are stored.
     * so here exams are fetched from db by checking the whether the exam is assigned to that student.
     */
    private void getNIOSExams() {
        String currentStudentID = FastSave.getInstance().getString("currentStudentID", "");
        langId = FastSave.getInstance().getString(LANGUAGE, "1");

        List<NIOSExam> AllExams = AppDatabase.getDatabaseInstance(getActivity()).getNiosExamDao().getAllSubjectsByStudIdSubId(currentStudentID, subjectId);

        if (AllExams != null && AllExams.size() > 0)
            for (int i = 0; i < AllExams.size(); i++) {
                List<NIOSExamTopics> allTopics = AppDatabase.getDatabaseInstance(getActivity()).getNiosExamTopicDao().getTopicIdByExamId(AllExams.get(i).getExamid());
                if (allTopics != null && allTopics.size() > 0)
                    for (int j = 0; j < allTopics.size(); j++) {
                        if (allTopics.get(j).getLanguageid().equalsIgnoreCase(langId)) {
                            if (checkIfStudentExamExist(AllExams, allTopics.get(j).getExamid())) {
//                            if(if(AllExams.get(i).getExamid())allTopics.get(j).getExamid())
                                AssessmentTest test = new AssessmentTest();
                                test.setLanguageId(allTopics.get(j).getLanguageid());
                                test.setSubjectname(allTopics.get(j).getSubjectname());
                                test.setSubjectid(allTopics.get(j).getSubjectid());
                                test.setExamname(allTopics.get(j).getExamname());
                                test.setExamid(allTopics.get(j).getExamid());
                                assessmentTests.add(test);
                            }
                        }
                    }
            }
        if (assessmentTests.size() > 0) {
            rl_no_exams.setVisibility(View.GONE);
            rv_topics.setVisibility(View.VISIBLE);
            setTopicsToRecyclerView(assessmentTests);
        } else {
            ((ChooseAssessmentActivity) getActivity()).frameLayout.setVisibility(View.GONE);
            ((ChooseAssessmentActivity) getActivity()).rlSubject.setVisibility(View.VISIBLE);
            getActivity().getSupportFragmentManager().popBackStack();
//            Toast.makeText(getActivity(), R.string.no_exams, Toast.LENGTH_SHORT).show();
            Assessment_Utility.showDialog(getActivity(), getActivity().getString(R.string.no_exams));
          /*  rl_no_exams.setVisibility(View.VISIBLE);
            rv_topics.setVisibility(View.GONE);*/

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


    /****
     * for create profile login there is different api for loading exams.
     */
    private void getExamData() {
        String url = "";
        boolean isRPI = checkConnectedToRPI();
        if (isRPI)
            url = APIs.AssessmentExamAPIRPI + subjectId + "&languageid=" + Assessment_Constants.SELECTED_LANGUAGE;
        else
            url = APIs.AssessmentExamAPI + subjectId + "&languageid=" + Assessment_Constants.SELECTED_LANGUAGE;

        Log.i("url12345_1",url);
        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setMessage(getString(R.string.loading_exams));
        progressDialog.setCancelable(false);
        progressDialog.show();
        AndroidNetworking.get(url)
                .build()
                .getAsString(new StringRequestListener() {
                    @Override
                    public void onResponse(String response) {

                        JSONArray jsonArray;

                        if (!isRPI) {
                            Gson gson = new Gson();
                            Type listType = new TypeToken<List<AssessmentTestModal>>() {
                            }.getType();
                            assessmentTestModals = gson.fromJson(response, listType);
                            assessmentTests.clear();
                        } else {
                            JSONObject jsonObject = null;
                            try {
                                jsonObject = new JSONObject(response);
                                jsonArray = jsonObject.getJSONArray("results");
                                Gson gson = new Gson();
                                Type listType = new TypeToken<List<AssessmentTestModal>>() {
                                }.getType();
                                assessmentTestModals = gson.fromJson(String.valueOf(jsonArray), listType);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                        for (int i = 0; i < assessmentTestModals.size(); i++) {
                            assessmentTests.addAll(assessmentTestModals.get(i).getLstsubjectexam());
                            for (int j = 0; j < assessmentTests.size(); j++) {
                                assessmentTests.get(j).setSubjectid(assessmentTestModals.get(i).getSubjectid());
                                assessmentTests.get(j).setSubjectname(assessmentTestModals.get(i).getSubjectname());
                                assessmentTests.get(j).setLanguageId(Assessment_Constants.SELECTED_LANGUAGE);
                            }
                        }
                        List<AssessmentTest> assessmentPublicTests = new ArrayList<>();

                        if (assessmentTests.size() > 0) {
                            AppDatabase.getDatabaseInstance(getActivity()).getTestDao().deleteTestsByLangIdAndSubId(subjectId, Assessment_Constants.SELECTED_LANGUAGE);
                            AppDatabase.getDatabaseInstance(getActivity()).getTestDao().insertAllTest(assessmentTests);
                            if (progressDialog != null && progressDialog.isShowing() && isVisible()) {
                                progressDialog.dismiss();
                            }
                            for (int i = 0; i < assessmentTests.size(); i++) {
                                if (assessmentTests.get(i).getExamtype() != null && assessmentTests.get(i).getExamtype().equalsIgnoreCase("public"))
                                    assessmentPublicTests.add(assessmentTests.get(i));
                            }
                        }

                        if (assessmentPublicTests.size() > 0) {
                            setTopicsToRecyclerView(assessmentPublicTests);
                           /* else
                                Toast.makeText(getActivity(), "No exams", Toast.LENGTH_SHORT).show();*/
              /* flowLayout.removeAllViews();
                            setTopicsToCheckBox(assessmentTests);*/
                        } else {
                            if (progressDialog != null && progressDialog.isShowing() && isVisible()) {
                                progressDialog.dismiss();
                            }
                            if (getActivity() != null) {
                                ((ChooseAssessmentActivity) Objects.requireNonNull(getActivity())).frameLayout.setVisibility(View.GONE);
                                ((ChooseAssessmentActivity) getActivity()).rlSubject.setVisibility(View.VISIBLE);
                                getActivity().getSupportFragmentManager().popBackStack();
                            }
//                            Toast.makeText(getActivity(), R.string.no_exams, Toast.LENGTH_SHORT).show();
                            Assessment_Utility.showDialog(getActivity(), getActivity().getString(R.string.no_exams));


                            AppDatabase.getDatabaseInstance(getActivity()).getTestDao().deleteTestsByLangIdAndSubId(subjectId, Assessment_Constants.SELECTED_LANGUAGE);

                        }
                    }

                    @Override
                    public void onError(ANError anError) {
                        if (progressDialog != null && progressDialog.isShowing() && isVisible()) {
                            progressDialog.dismiss();
                        }
                        ((ChooseAssessmentActivity) Objects.requireNonNull(getActivity())).frameLayout.setVisibility(View.GONE);
                        ((ChooseAssessmentActivity) getActivity()).rlSubject.setVisibility(View.VISIBLE);
                        getActivity().getSupportFragmentManager().popBackStack();

//                        Toast.makeText(getActivity(), "" + anError, Toast.LENGTH_SHORT).show();
                    }
                });

    }

    private void setTopicsToRecyclerView(List<AssessmentTest> assessmentPublicTests) {
        try {
            TopicAdapter topicAdapter = new TopicAdapter(getActivity(), assessmentPublicTests);
            if (getActivity() != null) {
                LinearLayoutManager linearLayoutManager = new GridLayoutManager(getActivity(), 2);
                rv_topics.setLayoutManager(linearLayoutManager);
                rv_topics.setAdapter(topicAdapter);
                topicAdapter.notifyDataSetChanged();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * get list of exams from db for selected subject.
     */
    private void getOfflineTests() {
        assessmentTests = AppDatabase.getDatabaseInstance(getContext()).getTestDao().getTopicBySubIdAndLangId(subjectId, Assessment_Constants.SELECTED_LANGUAGE);
        List<AssessmentTest> assessmentPublicTests = new ArrayList<>();
        if (assessmentTests.size() > 0) {
            for (int i = 0; i < assessmentTests.size(); i++) {
                if (assessmentTests.get(i).getExamtype() != null && assessmentTests.get(i).getExamtype().equalsIgnoreCase("public"))
                    assessmentPublicTests.add(assessmentTests.get(i));
            }
        }
        if (assessmentPublicTests.size() > 0) {
            setTopicsToRecyclerView(assessmentPublicTests);

        } else {
            if (progressDialog != null && progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
            ((ChooseAssessmentActivity) getActivity()).frameLayout.setVisibility(View.GONE);
            ((ChooseAssessmentActivity) getActivity()).rlSubject.setVisibility(View.VISIBLE);
            getActivity().getSupportFragmentManager().popBackStack();
            Toast.makeText(getActivity(), R.string.no_internet_connection, Toast.LENGTH_SHORT).show();
        }
    }
}
