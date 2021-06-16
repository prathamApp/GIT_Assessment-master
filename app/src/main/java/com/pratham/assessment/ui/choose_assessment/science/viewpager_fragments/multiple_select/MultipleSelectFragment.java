package com.pratham.assessment.ui.choose_assessment.science.viewpager_fragments.multiple_select;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.text.method.ScrollingMovementMethod;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.pratham.assessment.R;
import com.pratham.assessment.custom.gif_viewer.GifView;
import com.pratham.assessment.database.AppDatabase;
import com.pratham.assessment.domain.ScienceQuestion;
import com.pratham.assessment.domain.ScienceQuestionChoice;
import com.pratham.assessment.ui.choose_assessment.science.ScienceAssessmentActivity;
import com.pratham.assessment.ui.choose_assessment.science.interfaces.AssessmentAnswerListener;
import com.pratham.assessment.utilities.Assessment_Utility;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;

import java.util.List;

import static com.pratham.assessment.utilities.Assessment_Utility.getFileExtension;
import static com.pratham.assessment.utilities.Assessment_Utility.getOptionLocalPath;
import static com.pratham.assessment.utilities.Assessment_Utility.setOdiaFont;
import static com.pratham.assessment.utilities.Assessment_Utility.showZoomDialog;

@EFragment(R.layout.layout_multiple_select_row)
public class MultipleSelectFragment extends Fragment implements MultipleSelectContract.MultipleSelectView {

    @ViewById(R.id.tv_question)
    TextView question;
    @ViewById(R.id.iv_question_image)
    ImageView questionImage;
    @ViewById(R.id.iv_question_gif)
    GifView questionGif;
    @ViewById(R.id.gl_multiselect)
    GridLayout gridLayout;
    @ViewById(R.id.btn_view_hint)
    Button btn_view_hint;

    @Bean(MultipleSelectPresenter.class)
    MultipleSelectContract.MultipleSelectPresenter multipleSelectPresenter;

    private static final String POS = "pos";
    private static final String SCIENCE_QUESTION = "scienceQuestion";

    private int pos;
    private ScienceQuestion scienceQuestion;
    AssessmentAnswerListener assessmentAnswerListener;


    public MultipleSelectFragment() {
        // Required empty public constructor
    }

    public static MultipleSelectFragment newInstance(int pos, ScienceQuestion scienceQuestion) {
        com.pratham.assessment.ui.choose_assessment.science.viewpager_fragments.multiple_select.MultipleSelectFragment_ multipleSelectFragment = new com.pratham.assessment.ui.choose_assessment.science.viewpager_fragments.multiple_select.MultipleSelectFragment_();
        Bundle args = new Bundle();
        args.putInt("pos", pos);
        args.putSerializable("scienceQuestion", scienceQuestion);
        multipleSelectFragment.setArguments(args);
        return multipleSelectFragment;
    }

    @AfterViews
    public void init() {
        if (getArguments() != null) {
            pos = getArguments().getInt(POS, 0);
            scienceQuestion = (ScienceQuestion) getArguments().getSerializable(SCIENCE_QUESTION);
            assessmentAnswerListener = (ScienceAssessmentActivity) getActivity();
        }
        multipleSelectPresenter.setView(MultipleSelectFragment.this);
        if (question != null)
            question.setMovementMethod(new ScrollingMovementMethod());
        setMultipleSelectQuestion();

    }


/*    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            pos = getArguments().getInt(POS, 0);
            scienceQuestion = (ScienceQuestion) getArguments().getSerializable(SCIENCE_QUESTION);
            assessmentAnswerListener = (ScienceAssessmentActivity) getActivity();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.layout_multiple_select_row, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);
        setMultipleSelectQuestion();
    }*/

    public void setMultipleSelectQuestion() {

        setOdiaFont(getActivity(), question);

        question.setText(Html.fromHtml(scienceQuestion.getQname()));
        final String localPath = Assessment_Utility.getQuestionLocalPath(scienceQuestion);

        if (scienceQuestion.getPhotourl() != null && !scienceQuestion.getPhotourl().equalsIgnoreCase("")) {
            questionImage.setVisibility(View.VISIBLE);
            String extension = getFileExtension(localPath);

            if (extension.equalsIgnoreCase("PNG") ||
                    extension.equalsIgnoreCase("gif") ||
                    extension.equalsIgnoreCase("JPEG") ||
                    extension.equalsIgnoreCase("JPG")) {
                Assessment_Utility.setQuestionImageToImageView(questionImage, questionGif, localPath, getActivity());
            } else {
                if (extension.equalsIgnoreCase("mp4") ||
                        extension.equalsIgnoreCase("3gp")) {
                    Assessment_Utility.setThumbnailForVideo(localPath, getActivity(), questionImage);
                }
                questionImage.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.ic_play_circle));
                LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(250, 250);
                param.gravity = Gravity.CENTER;
                questionImage.setLayoutParams(param);
            }
        } else questionImage.setVisibility(View.GONE);

        questionImage.setOnClickListener(v -> showZoomDialog(getActivity(), localPath, ""));
        questionGif.setOnClickListener(v -> showZoomDialog(getActivity(), localPath, ""));

        final List<ScienceQuestionChoice> choices = scienceQuestion.getLstquestionchoice();

        gridLayout.setColumnCount(1);
        gridLayout.removeAllViews();
        for (int j = 0; j < choices.size(); j++) {
            View view = LayoutInflater.from(getActivity()).inflate(R.layout.layout_multiple_select_item, gridLayout, false);
            final CheckBox checkBox = (CheckBox) view;
            checkBox.setButtonTintList(Assessment_Utility.colorStateList);
            checkBox.setTextColor(getActivity().getResources().getColor(R.color.white));
            setOdiaFont(getActivity(), checkBox);
            if (!choices.get(j).getChoicename().equalsIgnoreCase(""))
                checkBox.setText(Html.fromHtml(choices.get(j).getChoicename()));

            if (!choices.get(j).getChoiceurl().equalsIgnoreCase("")) {
//                final String path = choices.get(j).getChoiceurl();

                String fileNameChoice = Assessment_Utility.getFileName(scienceQuestion.getQid(), choices.get(j).getChoiceurl());
                final String localPathChoice = getOptionLocalPath(choices.get(j), scienceQuestion.isParaQuestion()); /*AssessmentApplication.assessPath + Assessment_Constants.STORE_DOWNLOADED_MEDIA_PATH + "/" + fileNameChoice;
                 */

                checkBox.setOnClickListener(v -> Assessment_Utility.showZoomDialog(getActivity(), localPathChoice, ""));

                if (choices.get(j).getChoicename().equalsIgnoreCase(""))
                    checkBox.setText(getString(R.string.view_option) + " " + (j + 1));
            }
            checkBox.setTag(choices.get(j).getQcid());

            if (scienceQuestion.getIsAttempted()) {
                if (choices.get(j).getMyIscorrect().equalsIgnoreCase("TRUE")) {
                    checkBox.setChecked(true);
                    checkBox.setTextColor(Assessment_Utility.selectedColor);
                } else {
                    checkBox.setChecked(false);
                    checkBox.setTextColor(getActivity().getResources().getColor(R.color.white));

                }
//                }
            }
            checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {

                multipleSelectPresenter.setCheckedAnswer(buttonView, isChecked, choices);
                for (int i = 0; i < gridLayout.getRowCount(); i++) {
                    if (((CheckBox) gridLayout.getChildAt(i)).isChecked()) {
                        ((CheckBox) gridLayout.getChildAt(i)).setTextColor(Assessment_Utility.selectedColor);
                    } else {
                        ((CheckBox) gridLayout.getChildAt(i)).setTextColor(Color.WHITE);
                    }
                }
            });
            GridLayout.LayoutParams paramGrid = new GridLayout.LayoutParams();
            paramGrid.width = GridLayout.LayoutParams.WRAP_CONTENT;
            paramGrid.setGravity(Gravity.FILL_HORIZONTAL);
            paramGrid.setMargins(10, 10, 10, 10);
            checkBox.setLayoutParams(paramGrid);
            gridLayout.addView(checkBox);
        }
    }

    @Override
    public void setUserVisibleHint(boolean visible) {
        super.setUserVisibleHint(visible);
        if (visible && isResumed()) {
            //Only manually call onResume if fragment is already visible
            //Otherwise allow natural fragment lifecycle to call onResume
            onResume();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!getUserVisibleHint()) {
            return;
        }

        //INSERT CUSTOM CODE HERE
        String para = "";
        if (scienceQuestion != null) {
            ScienceQuestion scienceQuestion = AppDatabase.getDatabaseInstance(getActivity()).getScienceQuestionDao().getQuestionByQID(this.scienceQuestion.getQid());
            if (scienceQuestion.isParaQuestion()) {
                btn_view_hint.setVisibility(View.VISIBLE);
//                para = AppDatabase.getDatabaseInstance(getActivity()).getScienceQuestionDao().getParabyRefId(scienceQuestion.getRefParaID());
            } else btn_view_hint.setVisibility(View.GONE);
//            assessmentAnswerListener.setParagraph(para, scienceQuestion.isParaQuestion());

        } else {
            btn_view_hint.setVisibility(View.GONE);
        }


    }

    @Click(R.id.btn_view_hint)
    public void showPara() {
        if (scienceQuestion != null) {
            if (scienceQuestion.isParaQuestion()) {
                String para = AppDatabase.getDatabaseInstance(getActivity()).getScienceQuestionDao().getParabyRefId(scienceQuestion.getRefParaID());
                showZoomDialog(getActivity(), "", para);
            }
        }
    }

    @Override
    public void setAnswer(List<ScienceQuestionChoice> choices) {
        assessmentAnswerListener.setAnswerInActivity("", "", scienceQuestion.getQid(), choices);
    }
}