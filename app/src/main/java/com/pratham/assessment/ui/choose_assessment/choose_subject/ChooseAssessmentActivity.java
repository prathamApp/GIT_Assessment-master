package com.pratham.assessment.ui.choose_assessment.choose_subject;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.pratham.assessment.AssessmentApplication;
import com.pratham.assessment.BaseActivity;
import com.pratham.assessment.R;
import com.pratham.assessment.async.PushDBZipToServer;
import com.pratham.assessment.async.PushDataToServer;
import com.pratham.assessment.constants.Assessment_Constants;
import com.pratham.assessment.custom.FastSave;
import com.pratham.assessment.custom.GridSpacingItemDecoration;
import com.pratham.assessment.custom.ProcessPhoenix;
import com.pratham.assessment.database.AppDatabase;
import com.pratham.assessment.domain.AssessmentLanguages;
import com.pratham.assessment.domain.AssessmentSubjects;
import com.pratham.assessment.domain.AssessmentTest;
import com.pratham.assessment.domain.Crl;
import com.pratham.assessment.domain.Student;
import com.pratham.assessment.interfaces.DataPushListener;
import com.pratham.assessment.ui.bottom_fragment.BottomStudentsFragment_;
import com.pratham.assessment.ui.choose_assessment.ECELoginDialog;
import com.pratham.assessment.ui.choose_assessment.data_push_status.PushStatusActivity_;
import com.pratham.assessment.ui.choose_assessment.exam_status.ExamStatusActivity_;
import com.pratham.assessment.ui.choose_assessment.fragments.LanguageFragment_;
import com.pratham.assessment.ui.choose_assessment.fragments.TopicFragment;
import com.pratham.assessment.ui.choose_assessment.fragments.TopicFragment_;
import com.pratham.assessment.ui.choose_assessment.science.ScienceAssessmentActivity_;
import com.pratham.assessment.ui.choose_assessment.science.certificate.AssessmentCertificateActivity;
import com.pratham.assessment.utilities.Assessment_Utility;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static com.pratham.assessment.constants.Assessment_Constants.EXAMID;
import static com.pratham.assessment.constants.Assessment_Constants.LANGUAGE;
import static com.pratham.assessment.constants.Assessment_Constants.PUSH_DATA_FROM_DRAWER;
import static com.pratham.assessment.constants.Assessment_Constants.VIDEOMONITORING;
import static com.pratham.assessment.utilities.Assessment_Utility.dpToPx;
import static com.pratham.assessment.utilities.Assessment_Utility.setTamilFont;

@EActivity(R.layout.activity_choose_assessment)
public class ChooseAssessmentActivity extends BaseActivity implements
        ChoseAssessmentClicked, ChooseAssessmentContract.ChooseAssessmentView, DataPushListener {
    @Bean(ChooseAssessmentPresenter.class)
    ChooseAssessmentContract.ChooseAssessmentPresenter presenter;
    @Bean(PushDBZipToServer.class)
    PushDBZipToServer pushDataBaseZipToServer;
    @ViewById(R.id.rl_Profile)
    RelativeLayout rl_Profile;
    @ViewById(R.id.btn_Profile)
    ImageButton btn_Profile;
    @ViewById(R.id.spinner_choose_lang)
    Spinner spinner_choose_lang;
    @ViewById(R.id.drawer_layout)
    DrawerLayout drawerLayout;
    @ViewById(R.id.navigation)
    NavigationView navigation;
    @ViewById(R.id.rl_choose_sub)
    public RelativeLayout rlSubject;
    @ViewById(R.id.nav_frame_layout)
    public FrameLayout frameLayout;
    @ViewById(R.id.tv_choose_assessment)
    TextView tv_choose_assessment;
    @ViewById(R.id.menu_icon)
    ImageButton menu_icon;
    @ViewById(R.id.rl_no_exams)
    RelativeLayout rl_no_exams;
    @ViewById(R.id.swipe_to_refresh)
    SwipeRefreshLayout swipe_to_refresh;
    /* @ViewById(R.id.toggle_btn)
     public SwipeableButton toggle_btn;*/
    Context context;
    private RecyclerView recyclerView;
    List<AssessmentSubjects> contentTableList;
    ChooseAssessmentAdapter chooseAssessAdapter;
    ECELoginDialog eceLoginDialog;
    Crl loggedCrl;
    boolean videoMonitoring = false, isExit = false;
    @Bean(PushDataToServer.class)
    PushDataToServer pushDataToServer;
    @ViewById(R.id.tv_no_exams)
    TextView tv_no_exams;

    @AfterViews
    public void init() {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        context = this;


        String currentStudentID = FastSave.getInstance().getString("currentStudentID", "");
        Student student = AppDatabase.getDatabaseInstance(this).getStudentDao().getStudent(currentStudentID);
        String studentEnrollmentId = student.getLastName();//enrollment id saved in lastName
        String studentName = student.getFullName();
        String avatar = student.getAvatarName();

        View nav = navigation.getHeaderView(0);
        TextView name = nav.findViewById(R.id.userName);
        name.setText(Html.fromHtml(studentName));

        TextView enrollmentId = nav.findViewById(R.id.tv_enrollment_id);
        if (FastSave.getInstance().getBoolean("enrollmentNoLogin", false)) {
            enrollmentId.setVisibility(View.VISIBLE);
            if (studentEnrollmentId != null)
                enrollmentId.setText(Html.fromHtml(studentEnrollmentId));
            else enrollmentId.setText(Html.fromHtml(currentStudentID));
        } else enrollmentId.setVisibility(View.GONE);

        ImageView imageView = nav.findViewById(R.id.iv_avatar);
        if (avatar != null && !avatar.equalsIgnoreCase(""))
            imageView.setImageResource(/*getResources().getDrawable(*/Assessment_Utility.getAvatarDrawable(avatar)/*)*/);
        else imageView.setImageResource(R.drawable.g1);

        LinearLayout linearLayout = nav.findViewById(R.id.ll_user_details_header);
        linearLayout.setOnClickListener(v -> showExitDialog(true));

        Assessment_Constants.SELECTED_LANGUAGE = FastSave.getInstance().getString(LANGUAGE, "1");
        swipe_to_refresh.setColorSchemeColors(getResources().getColor(R.color.catcho_primary));
        swipe_to_refresh.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        menu_icon.setImageDrawable(getDrawable(R.drawable.ic_menu));
                        rlSubject.setVisibility(View.VISIBLE);
                        recyclerView.setVisibility(View.VISIBLE);
                        rl_no_exams.setVisibility(View.GONE);
                        frameLayout.setVisibility(View.GONE);
                        clearContentList();
                        presenter.copyListData();
//                        swipe_to_refresh.setRefreshing(false);
                    }
                }
        );

        Menu menu = navigation.getMenu();
//        MenuItem nav_video = menu.findItem(R.id.menu_video_monitoring);
        MenuItem nav_push = menu.findItem(R.id.menu_push_data);
        if (!AssessmentApplication.isTablet) {
//            nav_video.setVisible(true);
            nav_push.setVisible(true);
        } else {
//            nav_video.setVisible(false);
            nav_push.setVisible(false);
        }


        eceLoginDialog = new ECELoginDialog(this);


//        presenter = new ChooseAssessmentPresenter(ChooseAssessmentActivity.this);
        contentTableList = new ArrayList<>();


        recyclerView = findViewById(R.id.choose_subject_recycler);
        chooseAssessAdapter = new ChooseAssessmentAdapter(this, contentTableList, this);
//        RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(this, 2);
        RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(this, 2);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.addItemDecoration(new GridSpacingItemDecoration(2, dpToPx(10, this), true));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(chooseAssessAdapter);

        presenter.copyListData();


        navigation.setNavigationItemSelectedListener(menuItem -> {
            switch (menuItem.getItemId()) {
                case R.id.menu_Subject:
                    menu_icon.setImageDrawable(getDrawable(R.drawable.ic_menu));
                    rlSubject.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.VISIBLE);
                    rl_no_exams.setVisibility(View.GONE);
                    frameLayout.setVisibility(View.GONE);
                    tv_choose_assessment.setText("Choose subject");
//                        toggle_btn.setVisibility(View.VISIBLE);
                    clearContentList();
                    presenter.copyListData();

                    break;

                case R.id.menu_certificate:
                    startActivity(new Intent(ChooseAssessmentActivity.this, AssessmentCertificateActivity.class));
                    break;

                case R.id.menu_language:
//                        toggle_btn.setVisibility(View.GONE);
                    menu_icon.setImageDrawable(getResources().getDrawable(R.drawable.ic_arrow_back));

                    rlSubject.setVisibility(View.GONE);
                    frameLayout.setVisibility(View.VISIBLE);
                    Assessment_Utility.showFragment(ChooseAssessmentActivity.this, new LanguageFragment_(), R.id.nav_frame_layout,
                            null, LanguageFragment_.class.getSimpleName());
                    break;
                case R.id.menu_download_offline_language:
                    Intent intent = new Intent(Intent.ACTION_MAIN);
                    intent.setComponent(new ComponentName("com.google.android.googlequicksearchbox",
                            "com.google.android.voicesearch.greco3.languagepack.InstallActivity"));
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    break;
/*                    case R.id.menu_video_monitoring:
                    Menu menu = navigation.getMenu();
                    MenuItem nav_video = menu.findItem(R.id.menu_video_monitoring);
                    if (!videoMonitoring) {
                        videoMonitoring = true;
                        Assessment_Constants.VIDEOMONITORING = true;
                        nav_video.setTitle("Video monitoring(ON)");
                        Toast.makeText(ChooseAssessmentActivity.this, "Video monitoring : ON", Toast.LENGTH_SHORT).show();
                    } else {
                        videoMonitoring = false;
                        Assessment_Constants.VIDEOMONITORING = false;
                        nav_video.setTitle("Video monitoring(OFF)");
                        Toast.makeText(ChooseAssessmentActivity.this, "Video monitoring : OFF", Toast.LENGTH_SHORT).show();

                    }
                    break;*/
                case R.id.menu_sync_status:
                    startActivity(new Intent(ChooseAssessmentActivity.this, PushStatusActivity_.class));
                    break;
                case R.id.menu_exam_status:
                    startActivity(new Intent(ChooseAssessmentActivity.this, ExamStatusActivity_.class));
                    break;
                case R.id.menu_push_data:
                    PUSH_DATA_FROM_DRAWER = true;
                    pushDataToServer.setValue(ChooseAssessmentActivity.this, false);
                    pushDataToServer.doInBackground();
                    break;
                case R.id.menu_push_db:
                    pushDataBaseZipToServer.setValue(context, false);
                    pushDataBaseZipToServer.doInBackground();
                    break;
            }
            drawerLayout.closeDrawer(GravityCompat.START);

            return false;
        });
    }


    @Click(R.id.menu_icon)
    public void openMenu() {
     /*   if (toggle_btn.getVisibility() != View.VISIBLE)
            menu_icon.setImageDrawable(getResources().getDrawable(R.drawable.ic_arrow_back));
        else menu_icon.setImageDrawable(getResources().getDrawable(R.drawable.ic_menu));
*/
        String langId = FastSave.getInstance().getString(LANGUAGE, "1");
        Assessment_Utility.setLocaleByLanguageId(this, langId);

        getSupportFragmentManager().popBackStack();
        int fragments = getSupportFragmentManager().getBackStackEntryCount();
        if (fragments == 0) {
            menu_icon.setImageDrawable(getResources().getDrawable(R.drawable.ic_menu));
            if (drawerLayout.isDrawerOpen(Gravity.START))
                drawerLayout.closeDrawer(Gravity.START);
            else
                drawerLayout.openDrawer(Gravity.START);
        } else {
            resetActivity();
        }

    }

    @Override
    public void clearContentList() {
        contentTableList.clear();
    }

    @Override
    public void addContentToViewList(List<AssessmentSubjects> contentTable) {

        contentTableList.addAll(contentTable);
        if (contentTableList.size() > 0) {

            Collections.sort(contentTableList, new Comparator<AssessmentSubjects>() {
                @Override
                public int compare(AssessmentSubjects o1, AssessmentSubjects o2) {
                    return o1.getSubjectid().compareTo(o2.getSubjectid());
                }
            });
            Log.d("sorted", contentTableList.toString());
            showNoExamLayout(false);

        } else {
            showNoExamLayout(true);
        }
    }

    @Override
    public void showNoExamLayout(boolean show) {
        swipe_to_refresh.setRefreshing(false);
        String langId = FastSave.getInstance().getString(LANGUAGE, "1");
        Assessment_Utility.setLocaleByLanguageId(this, langId);
        tv_no_exams.setText(R.string.no_exams);

        if (show) {
            recyclerView.setVisibility(View.GONE);
            rl_no_exams.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            rl_no_exams.setVisibility(View.GONE);
        }

    }

    @Override
    public void notifyAdapter() {
        chooseAssessAdapter.notifyDataSetChanged();
        swipe_to_refresh.setRefreshing(false);
    }


    @Override
    public void onBackPressed() {
        int fragments = getSupportFragmentManager().getBackStackEntryCount();
        if (fragments >= 1) {
            getSupportFragmentManager().popBackStack();
            resetActivity();
        } else {
//            startActivity(new Intent(this, MenuActivity.class));
            showExitDialog(false);
        }
    }

    public void showExitDialog(boolean isLogout) {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setContentView(R.layout.exit_dialog);
        dialog.setCanceledOnTouchOutside(false);
        TextView title = dialog.findViewById(R.id.dia_title);
        Button exit_btn = dialog.findViewById(R.id.dia_btn_exit);
        Button restart_btn = dialog.findViewById(R.id.dia_btn_restart);
        Button cancel_btn = dialog.findViewById(R.id.dia_btn_cancel);
        setTamilFont(context, title);
        setTamilFont(context, restart_btn);
        setTamilFont(context, exit_btn);
        setTamilFont(context, cancel_btn);
        cancel_btn.setVisibility(View.VISIBLE);
        if (isLogout) {
            title.setText("Do you want to logout?");
            cancel_btn.setVisibility(View.GONE);
        } else {
            cancel_btn.setVisibility(View.VISIBLE);
            title.setText(R.string.do_you_want_to_exit);
        }
        restart_btn.setText(R.string.no);
        exit_btn.setText(R.string.yes);
        cancel_btn.setText(R.string.restart);
        dialog.show();

        exit_btn.setOnClickListener(v -> {
            AssessmentApplication.endTestSession(context);
            if (!isLogout) {
                dialog.dismiss();
                VIDEOMONITORING = false;
                finishAffinity();
            } else {
                isExit = true;
                dialog.dismiss();
                drawerLayout.closeDrawer(GravityCompat.START);
                BottomStudentsFragment_ bottomStudentsFragment = new BottomStudentsFragment_();
                if (!bottomStudentsFragment.isVisible() && !bottomStudentsFragment.isAdded()) {
                    bottomStudentsFragment.show(getSupportFragmentManager(), BottomStudentsFragment_.class.getSimpleName());
                }
            }
        });

        cancel_btn.setOnClickListener(v -> {
            VIDEOMONITORING = false;
            finishAffinity();
            try {
                ProcessPhoenix.triggerRebirth(ChooseAssessmentActivity.this);
            } catch (Exception e) {
                e.printStackTrace();
            }
//                startActivity(new Intent(ChooseAssessmentActivity.this, SplashActivity_.class));
        });
        restart_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                if (isExit) {
                    drawerLayout.closeDrawer(GravityCompat.START);
                    BottomStudentsFragment_ bottomStudentsFragment = new BottomStudentsFragment_();
                    if (!bottomStudentsFragment.isVisible() && !bottomStudentsFragment.isAdded()) {
                        bottomStudentsFragment.show(getSupportFragmentManager(), BottomStudentsFragment_.class.getSimpleName());
                    }
                }
            }
        });
    }

    @Override
    public void subjectClicked(final int position, final AssessmentSubjects sub) {
        Assessment_Constants.SELECTED_SUBJECT = sub.getSubjectname();
        Assessment_Constants.SELECTED_SUBJECT_ID = sub.getSubjectid();
        FastSave.getInstance().saveString("SELECTED_SUBJECT_ID", sub.getSubjectid());
        loggedCrl = null;
        String crlId = "";
        tv_choose_assessment.setText("Choose topic");
        menu_icon.setImageDrawable(getResources().getDrawable(R.drawable.ic_arrow_back));

        rlSubject.setVisibility(View.GONE);
//        toggle_btn.setVisibility(View.GONE);
        frameLayout.setVisibility(View.VISIBLE);

        if (sub.getSubjectname().equalsIgnoreCase("ece")) {
            if (Assessment_Constants.ASSESSMENT_TYPE.equalsIgnoreCase("") || Assessment_Constants.ASSESSMENT_TYPE.equalsIgnoreCase(Assessment_Constants.PRACTICE)) {
                rlSubject.setVisibility(View.VISIBLE);
//                toggle_btn.setVisibility(View.VISIBLE);
                frameLayout.setVisibility(View.GONE);
                Toast.makeText(this, "Switch on supervision mode", Toast.LENGTH_SHORT).show();
            } else {
               /* Intent intent = new Intent(ChooseAssessmentActivity.this, SupervisedAssessmentActivity_.class);
                intent.putExtra("crlId", "");
//                    intent.putExtra("subId", sub);
                startActivity(intent);*/
               /* Intent intent = new Intent(ChooseAssessmentActivity.this, ECEActivity.class);
                intent.putExtra("resId", "9962");
                intent.putExtra("crlId", "");
                startActivity(intent);*/
            }
        } else {
            Assessment_Utility.showFragment(ChooseAssessmentActivity.this, new TopicFragment_(), R.id.nav_frame_layout,
                    null, TopicFragment.class.getSimpleName());
        }


    }

    @Override
    public void languageClicked(int pos, AssessmentLanguages languages) {
        Assessment_Constants.SELECTED_LANGUAGE = languages.getLanguageid();
        FastSave.getInstance().saveString(LANGUAGE, languages.getLanguageid());
        Assessment_Utility.setLocaleByLanguageId(this, languages.getLanguageid());
        setLanguageInNav();
        clearContentList();
        presenter.copyListData();
        drawerLayout.closeDrawer(GravityCompat.START);
        Toast.makeText(this, "Language " + languages.getLanguagename(), Toast.LENGTH_SHORT).show();
        getSupportFragmentManager().popBackStackImmediate();
        frameLayout.setVisibility(View.GONE);
        rlSubject.setVisibility(View.VISIBLE);
//        toggle_btn.setVisibility(View.VISIBLE);
        /*if (toggle_btn.getVisibility() != View.VISIBLE)
            menu_icon.setImageDrawable(getResources().getDrawable(R.drawable.ic_arrow_back));
        else*/
        menu_icon.setImageDrawable(getResources().getDrawable(R.drawable.ic_menu));


    }

    @Override
    public void topicClicked(int pos, AssessmentTest test) {
//        Toast.makeText(this, "" + test.getExamname(), Toast.LENGTH_SHORT).show();
        Assessment_Constants.SELECTED_EXAM_ID = test.getExamid();
        FastSave.getInstance().saveString(EXAMID, test.getExamid());

        Intent intent = new Intent(ChooseAssessmentActivity.this, ScienceAssessmentActivity_.class);
        startActivity(intent);
//        }
    }




    @Override
    protected void onResume() {
        super.onResume();
        setLanguageInNav();
        int fragments = getSupportFragmentManager().getBackStackEntryCount();
        if (fragments >= 1) {
        } else {
//            startActivity(new Intent(this, MenuActivity.class));
            resetActivity();
        }

    }

    private void setLanguageInNav() {
        Menu menu = navigation.getMenu();
        MenuItem nav_lang = menu.findItem(R.id.menu_language);
        MenuItem nav_sub = menu.findItem(R.id.menu_Subject);
        MenuItem nav_certi = menu.findItem(R.id.menu_certificate);
        MenuItem nav_download_offline_lang = menu.findItem(R.id.menu_download_offline_language);
        MenuItem nav_push_data = menu.findItem(R.id.menu_push_data);
        String lang = AppDatabase.getDatabaseInstance(this).getLanguageDao().getLangNameById(Assessment_Constants.SELECTED_LANGUAGE);
        String languageMenu = "";
        if (lang != null) {
            if (!lang.equals("")) {
                languageMenu = "Language : " + lang;
            }
        } else languageMenu = "Language : " + " English";

        nav_lang.setTitle(languageMenu);
        nav_certi.setTitle(getString(R.string.certificate));
        nav_download_offline_lang.setTitle(R.string.download_offline_languages);
        nav_sub.setTitle(R.string.subjects);
        nav_push_data.setTitle(R.string.push_data);

    }

    public void resetActivity() {
//        toggle_btn.setVisibility(View.VISIBLE);

        /*  if (toggle_btn.getVisibility() == View.VISIBLE)*/
        menu_icon.setImageDrawable(getResources().getDrawable(R.drawable.ic_menu));

        getSupportFragmentManager().popBackStack();


        rlSubject.setVisibility(View.VISIBLE);
        frameLayout.setVisibility(View.GONE);
        tv_choose_assessment.setText("Choose subject");
    }

    @Override
    public void onResponseGet() {

    }

}