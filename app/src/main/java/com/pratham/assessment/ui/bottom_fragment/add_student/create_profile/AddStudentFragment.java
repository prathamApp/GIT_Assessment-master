package com.pratham.assessment.ui.bottom_fragment.add_student.create_profile;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.StringRequestListener;
import com.pratham.assessment.AssessmentApplication;
import com.pratham.assessment.R;
import com.pratham.assessment.constants.APIs;
import com.pratham.assessment.constants.Assessment_Constants;
import com.pratham.assessment.custom.FastSave;
import com.pratham.assessment.database.AppDatabase;
import com.pratham.assessment.database.BackupDatabase;
import com.pratham.assessment.domain.AssessmentLanguages;
import com.pratham.assessment.domain.AvatarModal;
import com.pratham.assessment.domain.ModalProgram;
import com.pratham.assessment.domain.Student;
import com.pratham.assessment.domain.Village;
import com.pratham.assessment.interfaces.OnChildAddedListener;
import com.pratham.assessment.ui.bottom_fragment.add_student.AvatarAdapter;
import com.pratham.assessment.ui.bottom_fragment.add_student.AvatarClickListener;
import com.pratham.assessment.ui.choose_assessment.choose_subject.ChooseAssessmentActivity;
import com.pratham.assessment.ui.splash_activity.SplashActivity;
import com.pratham.assessment.utilities.Assessment_Utility;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static com.pratham.assessment.constants.Assessment_Constants.CHHATTISGARH;
import static com.pratham.assessment.constants.Assessment_Constants.CHHATTISGARH_PROGRAM_ID;
import static com.pratham.assessment.constants.Assessment_Constants.LANGUAGE;
import static com.pratham.assessment.utilities.Assessment_Utility.checkConnectedToRPI;

/*

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
*/

public class AddStudentFragment extends DialogFragment implements AvatarClickListener, AddStudentContract.AddStudentView {

    //    @BindView(R.id.form_root)
    RelativeLayout homeRoot;

    List<AssessmentLanguages> assessmentLanguagesList;
    ProgressDialog progressDialog;

    String selectedProgram = "";
    String selectedVillage = "";
    String selectedState = "";

    //    @BindView(R.id.rv_Avatars)
    RecyclerView recyclerView;

    //    @BindView(R.id.et_studentName)
    EditText et_studentName;
    EditText et_schoolName;

    //    @BindView(R.id.spinner_age)
    Spinner spinner_age;

    //    @BindView(R.id.spinner_app_lang)
    Spinner spinner_app_lang;

    Spinner programSpinner;
    Spinner stateSpinner;
    Spinner blockSpinner;
    Spinner districtSpinner;
    Spinner villageSpinner;


/*    @BindView(R.id.spinner_class)
    Spinner spinner_class;*/

    //    @BindView(R.id.rb_male)
    RadioButton rb_male;

    //    @BindView(R.id.rb_female)
    RadioButton rb_female;
    Button btn_add_new_student;
    String gender = "";
    String selectedLang = "";
    String avatarName;
    ArrayList<Integer> avatars;

    //    @Bean(AddStudentPresenter.class)
    AddStudentContract.AddStudentPresenter addStudentPresenter;

    public AddStudentFragment() {
        // Required empty public constructor
    }

    ArrayList<AvatarModal> avatarList = new ArrayList<>();
    static OnChildAddedListener onChildAddedListener;
    AvatarAdapter avatarAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addStudentPresenter = new AddStudentPresenter(getActivity());
        addStudentPresenter.setView(AddStudentFragment.this);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.FullScreenDialogStyle);
        getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        avatars = new ArrayList<>();
        for (int i = 0; i < 6; i++)
            avatars.add(Assessment_Utility.getRandomAvatar(getActivity()));

    }

    public void editorListener(final EditText view) {
        view.setOnEditorActionListener(
                new EditText.OnEditorActionListener() {
                    @Override
                    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                        if (actionId == EditorInfo.IME_ACTION_NEXT ||
                                actionId == EditorInfo.IME_ACTION_SEARCH ||
                                actionId == EditorInfo.IME_ACTION_DONE ||
                                event != null &&
                                        event.getAction() == KeyEvent.ACTION_DOWN &&
                                        event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                            view.clearFocus();
                            if (view != null) {
                                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                            }
                            return true;
                        }
                        return false; // pass on to other listeners.
                    }
                }
        );
    }

    public static AddStudentFragment newInstance(OnChildAddedListener splashInter) {
        AddStudentFragment frag = new AddStudentFragment();
        Bundle args = new Bundle();
        args.putString("title", "Create Profile");
        frag.setArguments(args);
        onChildAddedListener = splashInter;
        return frag;
    }

    @Override
    public void onPause() {
        super.onPause();
        SplashActivity.fragmentAddStudentPauseFlg = true;
    }

    @Override
    public void onResume() {
        super.onResume();
        SplashActivity.fragmentAddStudentOpenFlg = true;
        SplashActivity.fragmentAddStudentPauseFlg = false;
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        SplashActivity.fragmentAddStudentOpenFlg = false;
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        progressDialog = new ProgressDialog(getActivity());

        ArrayAdapter<String> ageAdapter = new ArrayAdapter(getActivity(), R.layout.custom_spinner, getResources().getStringArray(R.array.age));
        spinner_age.setAdapter(ageAdapter);

        spinner_age.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                hideKeyboard(v);
                return false;
            }
        });
/*        spinner_age.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideKeyboard(v);
            }
        });*/
        spinner_age.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                hideKeyboard(view);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                if (getActivity() != null) {
                    InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(getActivity().INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }
            }
        });

//        ArrayAdapter<String> classAdapter = new ArrayAdapter(getActivity(), R.layout.custom_spinner, getResources().getStringArray(R.array.student_class));
        //spinner_class.setAdapter(classAdapter);
        addAvatarsInList();
        avatarAdapter = new AvatarAdapter(getActivity(), this, avatarList);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(mLayoutManager);
//        recyclerView.addItemDecoration(new GridSpacingItemDecoration(2, dpToPx(10), true));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(avatarAdapter);
        avatarAdapter.notifyDataSetChanged();

        assessmentLanguagesList = new ArrayList<>();

        if (AssessmentApplication.wiseF.isDeviceConnectedToMobileOrWifiNetwork()) {
            getLanguageData();
        } else {
            assessmentLanguagesList = AppDatabase.getDatabaseInstance(getActivity()).getLanguageDao().getAllLangs();
            if (assessmentLanguagesList.size() <= 0) {
//                Toast.makeText(getActivity(), R.string.connect_to_internet_to_download_languages, Toast.LENGTH_SHORT).show();
                Toast.makeText(getActivity(), "Connect to internet to download languages", Toast.LENGTH_SHORT).show();
                progressDialog.dismiss();
            } else {
                setLanguagesToSpinner();
            }
        }
        addStudentPresenter.loadPrograms();


    }

    private void setLanguagesToSpinner() {
        List<String> languages = new ArrayList<>();

        languages.add("Select Language");
        for (int i = 0; i < assessmentLanguagesList.size(); i++) {
            languages.add(assessmentLanguagesList.get(i).getLanguagename());
        }
        if (getActivity() != null) {
            ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(getActivity(), R.layout.custom_spinner, languages);
            // Drop down layout style - list view with radio button
//        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_item);
            // attaching data adapter to spinner
            spinner_app_lang.setAdapter(dataAdapter);
        }
        spinner_app_lang.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (view != null) {
                    TextView lang = (TextView) view;
                    selectedLang = lang.getText().toString();
                    String langId = AppDatabase.getDatabaseInstance(getActivity()).getLanguageDao().getLangIdByName(selectedLang);
                    if (langId != null) {
                        Assessment_Constants.SELECTED_LANGUAGE = langId;
                        FastSave.getInstance().saveString(LANGUAGE, langId);
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void hideKeyboard(View view) {
        if (view != null)
            if (getActivity() != null) {
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(getActivity().INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
    }

    private void addAvatarsInList() { 
      /*  for (int i = 0; i < 6; i++) {
            AvatarModal avatarModal = new AvatarModal();
            avatarModal.setAvatarName(""+Assessment_Utility.getRandomAvatar(getActivity()));
            avatarModal.setClickFlag(false);
            avatarList.add(avatarModal);
        }*/
        AvatarModal avatarModal = new AvatarModal();
        avatarModal.setAvatarName("g1.png");
//        avatarModal.setClickFlag(false);
        avatarList.add(avatarModal);
        AvatarModal avatarModal1 = new AvatarModal();

        avatarModal1.setAvatarName("b1.png");
//        avatarModal1.setClickFlag(false);
        avatarList.add(avatarModal1);
        AvatarModal avatarModal2 = new AvatarModal();

        avatarModal2.setAvatarName("g2.png");
//        avatarModal2.setClickFlag(false);
        avatarList.add(avatarModal2);
        AvatarModal avatarModal3 = new AvatarModal();

        avatarModal3.setAvatarName("b2.png");
//        avatarModal3.setClickFlag(false);
        avatarList.add(avatarModal3);
        AvatarModal avatarModal4 = new AvatarModal();

        avatarModal4.setAvatarName("g3.png");
//        avatarModal4.setClickFlag(false);
        avatarList.add(avatarModal4);
        AvatarModal avatarModal5 = new AvatarModal();

        avatarModal5.setAvatarName("b3.png");
//        avatarModal5.setClickFlag(false);
        avatarList.add(avatarModal5);

        Collections.shuffle(avatarList);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_student, container, false);
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
//        ButterKnife.bind(this, view);


        recyclerView = view.findViewById(R.id.rv_Avatars);
        et_studentName = view.findViewById(R.id.et_studentName);
        et_schoolName = view.findViewById(R.id.et_school);
        spinner_age = view.findViewById(R.id.spinner_age);
        spinner_app_lang = view.findViewById(R.id.spinner_app_lang);
        programSpinner = view.findViewById(R.id.programSpinner);
        blockSpinner = view.findViewById(R.id.blockSpinner);
        stateSpinner = view.findViewById(R.id.stateSpinner);
        districtSpinner = view.findViewById(R.id.districtSpinner);
        villageSpinner = view.findViewById(R.id.villageSpinner);
        rb_male = view.findViewById(R.id.rb_male);
        rb_female = view.findViewById(R.id.rb_female);
        btn_add_new_student = view.findViewById(R.id.btn_add_new_student);
        rb_male.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gender = "Male";

            }
        });
        rb_female.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gender = "Female";

            }
        });

        btn_add_new_student.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onAddNewClick();
            }
        });

        editorListener(et_studentName);
        return view;
    }

/*    public static void hideSoftKeyboard(Activity activity) {
        InputMethodManager inputMethodManager = (InputMethodManager)  activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
       if(activity.getCurrentFocus()!=null && activity.getCurrentFocus().getWindowToken() != null)
           inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
    }*/

    /*public void setupUI(View view) {

        //Set up touch listener for non-text box views to hide keyboard.
        if(!(view instanceof EditText)) {

            view.setOnTouchListener(new View.OnTouchListener() {

                public boolean onTouch(View v, MotionEvent event) {
                    hideSoftKeyboard(getActivity());
                    return false;
                }

            });
        }

        //If a layout container, iterate over children and seed recursion.
        if (view instanceof ViewGroup) {

            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {

                View innerView = ((ViewGroup) view).getChildAt(i);

                setupUI(innerView);
            }
        }
    }*/

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            int width = ViewGroup.LayoutParams.MATCH_PARENT;
            int height = ViewGroup.LayoutParams.MATCH_PARENT;
            dialog.getWindow().setLayout(width, height);
        }
    }

    /* @OnClick(R.id.rb_male)
     public void maleGenderClicked() {
         //ButtonClickSound.start();
         // rb_male.setBackground(getResources().getDrawable(R.drawable.correct_bg));
         // rb_female.setBackground(getResources().getDrawable(R.drawable.ripple_rectangle));
         gender = "Male";
     }

     @OnClick(R.id.rb_female)
     public void femaleGenderClicked() {
         //ButtonClickSound.start();
         //rb_female.setBackground(getResources().getDrawable(R.drawable.correct_bg));
         //rb_male.setBackground(getResources().getDrawable(R.drawable.ripple_rectangle));
         gender = "Female";
     }
 */
//    @OnClick(R.id.btn_add_new_student)
    public void onAddNewClick() {
        //ButtonClickSound.start();
        if (selectedProgram.equalsIgnoreCase(""))
            addStudentPresenter.loadPrograms();

        if (assessmentLanguagesList.size() <= 0) {
            if (!AssessmentApplication.wiseF.isDeviceConnectedToMobileOrWifiNetwork())
//                Toast.makeText(getActivity(), R.string.connect_to_internet_to_download_languages, Toast.LENGTH_SHORT).show();
                Toast.makeText(getActivity(), "Connect to internet to download languages", Toast.LENGTH_SHORT).show();
            else {
                getLanguageData();

//                Toast.makeText(getActivity(), R.string.select_language, Toast.LENGTH_SHORT).show();
                Toast.makeText(getActivity(), "Select language", Toast.LENGTH_SHORT).show();

            }
        } else if (et_studentName.getText().toString().equalsIgnoreCase("") ||
                /*spinner_class.getSelectedItem().toString().equalsIgnoreCase("select class") ||*/
                spinner_age.getSelectedItem().toString().equalsIgnoreCase("select age") ||
                gender.equalsIgnoreCase("") || avatarName == null ||
                selectedLang.equalsIgnoreCase("Select Language") || selectedLang.equalsIgnoreCase("")
                || selectedProgram.equalsIgnoreCase(CHHATTISGARH_PROGRAM_ID) || selectedState.equalsIgnoreCase(CHHATTISGARH)) {
//            Toast.makeText(getActivity(), R.string.please_fil_all_the_details, Toast.LENGTH_SHORT).show();
            if (et_studentName.getText().toString().equalsIgnoreCase(""))
                Toast.makeText(getActivity(), "Please enter name..", Toast.LENGTH_SHORT).show();
            else if (spinner_age.getSelectedItem().toString().equalsIgnoreCase("select age"))
                Toast.makeText(getActivity(), "Please select age..", Toast.LENGTH_SHORT).show();
            else if (gender.equalsIgnoreCase(""))
                Toast.makeText(getActivity(), "Please select gender..", Toast.LENGTH_SHORT).show();
            else if (selectedLang.equalsIgnoreCase("Select Language") || selectedLang.equalsIgnoreCase(""))
                Toast.makeText(getActivity(), "Please select language..", Toast.LENGTH_SHORT).show();
            else if (avatarName == null)
                Toast.makeText(getActivity(), "Please select avatar..", Toast.LENGTH_SHORT).show();
            else if (selectedProgram.equalsIgnoreCase(CHHATTISGARH_PROGRAM_ID)
                    && selectedState.equalsIgnoreCase(CHHATTISGARH)) {
                if (et_schoolName.getText().toString().equalsIgnoreCase(""))
                    Toast.makeText(getActivity(), "Please enter school name..", Toast.LENGTH_SHORT).show();
                else if (districtSpinner.getSelectedItemPosition() < 1)
                    Toast.makeText(getActivity(), "Please select district..", Toast.LENGTH_SHORT).show();
                else if (blockSpinner.getSelectedItemPosition() < 1)
                    Toast.makeText(getActivity(), "Please select block..", Toast.LENGTH_SHORT).show();
                else if (villageSpinner.getSelectedItemPosition() < 1)
                    Toast.makeText(getActivity(), "Please select village..", Toast.LENGTH_SHORT).show();
                else addStudentToDB();
            } else addStudentToDB();

        } else {
            addStudentToDB();

        }
    }

    private void addStudentToDB() {
        Student student = new Student();
        student.setStudentID(AssessmentApplication.getUniqueID().toString());
        student.setFullName(et_studentName.getText().toString().trim());
        student.setAge(Integer.parseInt(spinner_age.getSelectedItem().toString()));
        student.setStud_Class(/*spinner_class.getSelectedItem().toString()*/"");
        student.setGender(gender);
        student.setAvatarName(avatarName);
        student.setGroupId("PS");
        student.setIsniosstudent("0");
//            if (programSpinner.getSelectedItemPosition() > 0)
        student.setProgramId(selectedProgram);
        student.setVillageId(selectedVillage);
        if (stateSpinner.getSelectedItemPosition() > 0)
            student.setState(stateSpinner.getSelectedItem().toString());
        if (districtSpinner.getSelectedItemPosition() > 0)
            student.setDistrict(districtSpinner.getSelectedItem().toString());
        if (blockSpinner.getSelectedItemPosition() > 0)
            student.setBlock(blockSpinner.getSelectedItem().toString());
        if (villageSpinner.getSelectedItemPosition() > 0)
            student.setVillageName(villageSpinner.getSelectedItem().toString());
        if (!et_schoolName.getText().toString().equalsIgnoreCase(""))
            student.setSchool(et_schoolName.getText().toString().trim());
//            student.setStudentUID("PS");
            /*if (gender.equalsIgnoreCase("male"))
                student.setAvatarName("b1");
            else
                student.setAvatarName("g3");
*/
        student.setDeviceId(Assessment_Utility.getDeviceId(getActivity()));
        AppDatabase.getDatabaseInstance(getActivity()).getStudentDao().insert(student);
        BackupDatabase.backup(getActivity());
        Toast.makeText(getActivity(), "Profile created Successfully..", Toast.LENGTH_SHORT).show();
        onChildAddedListener.onChildAdded();
        dismiss();


    }


    @Override
    public void onAvatarClick(int position, String StudentName) {
        avatarName = StudentName;

       /* for (int i = 0; i < avatarList.size(); i++)
            avatarList.get(i).setClickFlag(false);
        avatarList.get(position).setClickFlag(true);*/
        avatarAdapter.notifyDataSetChanged();
//        avatarName = AssessmentApplication.pradigiPath + "/.LLA/English/LLA_Thumbs/" + StudentName;

    }


    private void getLanguageData() {
//        progressDialog.setMessage(getString(R.string.loading));
        String url = "";
        boolean isRPI = checkConnectedToRPI();
        if (isRPI)
            url = APIs.AssessmentLanguageAPIRPI;
        else url = APIs.AssessmentLanguageAPI;
        progressDialog.setMessage("Loading..");
        progressDialog.setCancelable(false);
        progressDialog.show();
        AndroidNetworking.get(url)
                .build()
                .getAsString(new StringRequestListener() {
                    @Override
                    public void onResponse(String response) {
                        try {

                            if (getActivity() != null && progressDialog != null && progressDialog.isShowing())
                                progressDialog.dismiss();

                            JSONArray jsonArray;

                            if (!isRPI) {
                                jsonArray = new JSONArray(response);
                            } else {
                                JSONObject jsonObject = new JSONObject(response);
                                jsonArray = jsonObject.getJSONArray("results");
                            }

                            for (int i = 0; i < jsonArray.length(); i++) {
                                AssessmentLanguages assessmentLanguages = new AssessmentLanguages();
                                assessmentLanguages.setLanguageid(jsonArray.getJSONObject(i).getString("languageid"));
                                assessmentLanguages.setLanguagename(jsonArray.getJSONObject(i).getString("languagename"));
                                assessmentLanguagesList.add(assessmentLanguages);
                            }
                            if (getActivity() != null)
                                AppDatabase.getDatabaseInstance(getActivity()).getLanguageDao().insertAllLanguages(assessmentLanguagesList);
                            setLanguagesToSpinner();


                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(ANError anError) {
//                        Toast.makeText(getActivity(), R.string.error_in_loading_check_internet_connection, Toast.LENGTH_SHORT).show();
                        Toast.makeText(getActivity(), "Error in loading..Check internet connection", Toast.LENGTH_SHORT).show();
//                        AppDatabase.getDatabaseInstance(getActivity()).getAssessmentPaperPatternDao().deletePaperPatterns();
                        ((ChooseAssessmentActivity) getActivity()).frameLayout.setVisibility(View.GONE);
                        ((ChooseAssessmentActivity) getActivity()).rlSubject.setVisibility(View.VISIBLE);
//                        ((ChooseAssessmentActivity) getActivity()).toggle_btn.setVisibility(View.VISIBLE);
                        getActivity().getSupportFragmentManager().popBackStackImmediate();

                        progressDialog.dismiss();
//                        selectTopicDialog.show();
                    }
                });

    }

    @Override
    public void showProgram(final List<ModalProgram> prgrmList) {
        try {
//            this.prgrmList = prgrmList;
            List<String> prgrms = new ArrayList<>();
            for (ModalProgram mp : prgrmList) {
                prgrms.add(mp.getProgramName());
            }
            ArrayAdapter arrayStateAdapter = new ArrayAdapter(Objects.requireNonNull(Objects.requireNonNull(getActivity())), R.layout.custom_spinner, prgrms);
//            arrayStateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            programSpinner.setAdapter(arrayStateAdapter);
            programSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//                    disableSaveButton();
                    if (position <= 0) {
                        stateSpinner.setSelection(0);
                        stateSpinner.setEnabled(false);
                        blockSpinner.setSelection(0);
                        blockSpinner.setEnabled(false);
                        districtSpinner.setSelection(0);
                        districtSpinner.setEnabled(false);
                        villageSpinner.setSelection(0);
                        villageSpinner.setEnabled(false);
                    } else {
                        selectedProgram = prgrmList.get(position).getProgramId();
                        addStudentPresenter.pullStates(selectedProgram);
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void showStatesSpinner(ArrayList states) {
        stateSpinner.setEnabled(true);
        if (states.size() <= 1) {
            states.clear();
            states.add("No states");
        }
        ArrayAdapter arrayStateAdapter = new ArrayAdapter(getActivity(), R.layout.custom_spinner, states);
//        arrayStateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        stateSpinner.setAdapter(arrayStateAdapter);
        stateSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long l) {
//                disableSaveButton();
                if (pos <= 0) {
                    districtSpinner.setSelection(0);
                    districtSpinner.setEnabled(false);
                    blockSpinner.setSelection(0);
                    blockSpinner.setEnabled(false);
                    villageSpinner.setSelection(0);
                    villageSpinner.setEnabled(false);

                } else {
                    selectedState = states.get(pos).toString().trim();
                    addStudentPresenter.loadDistrictSpinner(pos, selectedProgram);

                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    @Override
    public void showDistrictSpinner(List districts) {
        districtSpinner.setEnabled(true);
        if (districts.size() <= 1) {
            districts.clear();
            districts.add("No districts");
        }
        ArrayAdapter arrayBlockAdapter = new ArrayAdapter(getActivity(), R.layout.custom_spinner, districts);
//        arrayBlockAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        districtSpinner.setAdapter(arrayBlockAdapter);
        districtSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long l) {
//                disableSaveButton();
                if (pos <= 0) {
                    blockSpinner.setSelection(0);
                    blockSpinner.setEnabled(false);
                    villageSpinner.setSelection(0);
                    villageSpinner.setEnabled(false);
                } else {
                    //open Village Dialog
                    String district = adapterView.getSelectedItem().toString();
                    addStudentPresenter.loadBlocks(district);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    @Override
    public void showBlockSpinner(List<String> blockList) {
        blockSpinner.setEnabled(true);
        if (blockList.size() <= 1) {
            blockList.clear();
            blockList.add("No blocks");
        }
        ArrayAdapter arrayStateAdapter = new ArrayAdapter(getActivity(), R.layout.custom_spinner, blockList);
//        arrayStateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        blockSpinner.setAdapter(arrayStateAdapter);
        blockSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long l) {
//                disableSaveButton();
                if (pos <= 0) {
                    villageSpinner.setSelection(0);
                    villageSpinner.setEnabled(false);

                }
                String block = adapterView.getSelectedItem().toString();
                addStudentPresenter.loadVillages(block);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
    }

    @Override
    public void showVillageSpinner(ArrayList<Village> villageList) {
        villageSpinner.setEnabled(true);
        List<String> villageNames = new ArrayList<>();

        for (Village v : villageList) {
            villageNames.add(v.getVillageName());
        }
        if (villageNames.size() <= 1) {
            villageNames.clear();
            villageNames.add("No villages");
        }
        ArrayAdapter arrayStateAdapter = new ArrayAdapter(getActivity(), R.layout.custom_spinner, villageNames);
//        arrayStateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        villageSpinner.setAdapter(arrayStateAdapter);
        villageSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long l) {
//                disableSaveButton();
                selectedVillage = String.valueOf(villageList.get(pos).getVillageId());
//                addStudentPresenter.loadVillages(block);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
    }


    @Override
    public void showProgressDialog(String msg) {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(getActivity());
        }
        progressDialog.setCancelable(false);
        progressDialog.setMessage(msg);
        progressDialog.show();
    }

    @Override
    public void closeProgressDialog() {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }

}
