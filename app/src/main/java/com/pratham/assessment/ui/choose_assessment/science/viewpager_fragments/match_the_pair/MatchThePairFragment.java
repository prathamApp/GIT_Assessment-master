package com.pratham.assessment.ui.choose_assessment.science.viewpager_fragments.match_the_pair;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.Html;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.pratham.assessment.R;
import com.pratham.assessment.custom.gif_viewer.GifView;
import com.pratham.assessment.database.AppDatabase;
import com.pratham.assessment.domain.ScienceQuestion;
import com.pratham.assessment.domain.ScienceQuestionChoice;
import com.pratham.assessment.ui.choose_assessment.science.ItemMoveCallback;
import com.pratham.assessment.ui.choose_assessment.science.ScienceAssessmentActivity;
import com.pratham.assessment.ui.choose_assessment.science.adapters.MatchPairAdapter;
import com.pratham.assessment.ui.choose_assessment.science.adapters.MatchPairDragDropAdapter;
import com.pratham.assessment.ui.choose_assessment.science.interfaces.AssessmentAnswerListener;
import com.pratham.assessment.ui.choose_assessment.science.interfaces.StartDragListener;
import com.pratham.assessment.utilities.Assessment_Utility;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.pratham.assessment.utilities.Assessment_Utility.getFileExtension;
import static com.pratham.assessment.utilities.Assessment_Utility.getFileName;
import static com.pratham.assessment.utilities.Assessment_Utility.setOdiaFont;
import static com.pratham.assessment.utilities.Assessment_Utility.setTamilFont;
import static com.pratham.assessment.utilities.Assessment_Utility.showZoomDialog;

@EFragment(R.layout.layout_match_the_pair_row)
public class MatchThePairFragment extends Fragment implements StartDragListener, MatchThePairContract.MatchThePairView {
    @ViewById(R.id.tv_question)
    TextView question;
    @ViewById(R.id.iv_question_image)
    ImageView questionImage;
    @ViewById(R.id.iv_question_gif)
    GifView questionGif;
    @ViewById(R.id.rl_ans_options1)
    RecyclerView recyclerView1;
    @ViewById(R.id.rl_ans_options2)
    RecyclerView recyclerView2;
    @ViewById(R.id.btn_view_hint)
    Button btn_view_hint;

    @Bean(MatchThePairPresenter.class)
    MatchThePairContract.MatchThePairPresenter presenter;

    private static final String POS = "pos";
    private static final String SCIENCE_QUESTION = "scienceQuestion";

    private int pos;
    private ScienceQuestion scienceQuestion;
    ItemTouchHelper touchHelper;
    MatchPairDragDropAdapter matchPairDragDropAdapter;
    AssessmentAnswerListener assessmentAnswerListener;

    @AfterViews
    public void init() {
        if (getArguments() != null) {
            pos = getArguments().getInt(POS, 0);
            scienceQuestion = (ScienceQuestion) getArguments().getSerializable(SCIENCE_QUESTION);
        }
        if (question != null)
            question.setMovementMethod(new ScrollingMovementMethod());
        assessmentAnswerListener = (ScienceAssessmentActivity) getActivity();

        presenter.setView(MatchThePairFragment.this);
        setMatchPairQuestion();
    }


    public MatchThePairFragment() {
        // Required empty public constructor
    }

    public static MatchThePairFragment newInstance(int pos, ScienceQuestion scienceQuestion) {
        MatchThePairFragment_ matchThePairFragment = new MatchThePairFragment_();
        Bundle args = new Bundle();
        args.putInt("pos", pos);
        args.putSerializable("scienceQuestion", scienceQuestion);
        matchThePairFragment.setArguments(args);
        return matchThePairFragment;
    }

/*    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            pos = getArguments().getInt(POS, 0);
            scienceQuestion = (ScienceQuestion) getArguments().getSerializable(SCIENCE_QUESTION);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.layout_match_the_pair_row, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);
        setMatchPairQuestion();
    }*/

    public void setMatchPairQuestion() {
        setOdiaFont(getActivity(), question);
        setTamilFont(getActivity(), question);
        question.setText(Html.fromHtml(scienceQuestion.getQname()));
        final String fileName = getFileName(scienceQuestion.getQid(), scienceQuestion.getPhotourl());
//                String localPath = Environment.getExternalStorageDirectory() + Assessment_Constants.STORE_DOWNLOADED_MEDIA_PATH + "/" + fileName;
        final String localPath = Assessment_Utility.getQuestionLocalPath(scienceQuestion);
     /*   if (scienceQuestion.getIsQuestionFromSDCard())
            localPath = scienceQuestion.getPhotourl();
        else
            localPath = AssessmentApplication.assessPath + Assessment_Constants.STORE_DOWNLOADED_MEDIA_PATH + "/" + fileName;
*/
        if (scienceQuestion.getPhotourl() != null && !scienceQuestion.getPhotourl().equalsIgnoreCase("")) {
            if (new File(localPath).exists()) {

                questionImage.setVisibility(View.VISIBLE);
//            if (AssessmentApplication.wiseF.isDeviceConnectedToMobileOrWifiNetwork()) {
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

          /*  String path = scienceQuestion.getPhotourl();
            String[] imgPath = path.split("\\.");
            int len;
            if (imgPath.length > 0)
                len = imgPath.length - 1;
            else len = 0;
            if (imgPath[len].equalsIgnoreCase("gif")) {
                try {
                    InputStream gif;
                   *//* if (AssessmentApplication.wiseF.isDeviceConnectedToMobileOrWifiNetwork()) {
                        Glide.with(getActivity()).asGif()
                                .load(path)
                                .apply(new RequestOptions()
                                        .placeholder(Drawable.createFromPath(localPath)))
                                .into(questionImage);
//                    zoomImg.setVisibility(View.VISIBLE);
                    } else {*//*
                    gif = new FileInputStream(localPath);
                    questionImage.setVisibility(View.GONE);
                    questionGif.setVisibility(View.VISIBLE);
                    questionGif.setGifResource(gif);
//                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                Glide.with(getActivity())
                        .load(path)
                        .apply(new RequestOptions()
                                .placeholder(Drawable.createFromPath(localPath))
                                .diskCacheStrategy(DiskCacheStrategy.NONE)
                                .skipMemoryCache(true))
                        .into(questionImage);
            }*/
         /*   } else {
                String fileName = getFileName(scienceQuestion.getQid(), scienceQuestion.getPhotourl());
//                String localPath = Environment.getExternalStorageDirectory() + Assessment_Constants.STORE_DOWNLOADED_MEDIA_PATH + "/" + fileName;
                String localPath = AssessmentApplication.assessPath + Assessment_Constants.STORE_DOWNLOADED_MEDIA_PATH + "/" + fileName;
                Bitmap bitmap = BitmapFactory.decodeFile(localPath);
                questionImage.setImageBitmap(bitmap);
            }*/
            } else assessmentAnswerListener.reDownloadExam();
        } else questionImage.setVisibility(View.GONE);


        questionImage.setOnClickListener(v -> showZoomDialog(getActivity(), localPath, ""));
        questionGif.setOnClickListener(v -> showZoomDialog(getActivity(), localPath, ""));

        List<ScienceQuestionChoice> AnswerList = new ArrayList<>();

        if (!scienceQuestion.getUserAnswer().equalsIgnoreCase("")) {
            String[] ansIds = scienceQuestion.getUserAnswer().split(",");
            if (ansIds.length > 0)
                for (int i = 0; i < ansIds.length; i++) {
                    if (ansIds[i].equalsIgnoreCase(scienceQuestion.getMatchingNameList().get(i).getQcid()))
                        AnswerList.add(scienceQuestion.getMatchingNameList().get(i));
                }

        }

        List<ScienceQuestionChoice> pairList = new ArrayList<>();
        List<ScienceQuestionChoice> shuffledList = new ArrayList<>();

        pairList.clear();
//        pairList = AppDatabase.getDatabaseInstance(context).getScienceQuestionChoicesDao().getQuestionChoicesByQID(scienceQuestion.getQid());
        pairList = scienceQuestion.getLstquestionchoice();
        Log.d("wwwwwwwwwww", pairList.size() + "");
        if (pairList != null && pairList.size() > 0) {
            recyclerView1.setVisibility(View.VISIBLE);
        } else assessmentAnswerListener.reDownloadExam();


        if (!pairList.isEmpty()) {
          /*  for (int p = 0; p < pairList.size(); p++) {
                list1.add(pairList.get(p).getChoicename());
                list2.add(pairList.get(p).getMatchingname());
            }*/
            MatchPairAdapter matchPairAdapter = new MatchPairAdapter(pairList, getActivity());
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity().getApplicationContext());
            recyclerView1.setLayoutManager(linearLayoutManager);
            recyclerView1.setAdapter(matchPairAdapter);
            if (scienceQuestion.getMatchingNameList() == null) {
                shuffledList.clear();

                shuffledList.addAll(pairList);
                if (shuffledList.size() > 1)
                    while (shuffledList.equals(pairList)) {
                        Collections.shuffle(shuffledList);
                    }
            } else {
                if (AnswerList.size() > 0)
                    shuffledList.addAll(AnswerList);
                else {
                    shuffledList = scienceQuestion.getMatchingNameList();
                    Collections.shuffle(shuffledList);
                }

            }

            if (shuffledList != null && shuffledList.size() > 0) {
                recyclerView2.setVisibility(View.VISIBLE);
            } else recyclerView2.setVisibility(View.GONE);


            matchPairDragDropAdapter = new MatchPairDragDropAdapter(this, shuffledList, getActivity());
            ItemTouchHelper.Callback callback =
                    new ItemMoveCallback(matchPairDragDropAdapter);
            touchHelper = new ItemTouchHelper(callback);
            touchHelper.attachToRecyclerView(null);
            touchHelper.attachToRecyclerView(recyclerView2);

            LinearLayoutManager linearLayoutManager1 = new LinearLayoutManager(getActivity().getApplicationContext());
            recyclerView2.setLayoutManager(linearLayoutManager1);
            recyclerView2.setAdapter(matchPairDragDropAdapter);
            Log.d("wwwwwwwwwww", pairList.size() + "");
        } else
            assessmentAnswerListener.reDownloadExam();

    }


    @Override
    public void requestDrag(RecyclerView.ViewHolder viewHolder) {
        touchHelper.startDrag(viewHolder);
    }

    @Override
    public void onItemDragged(List<ScienceQuestionChoice> draggedList) {
        matchPairDragDropAdapter.notifyDataSetChanged();
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

//            assessmentAnswerListener.setParagraph(para, scienceQuestion.isParaQuestion());

        }


    }

    @Click(R.id.btn_view_hint)
    public void showPara() {
        if (scienceQuestion != null) {
            ScienceQuestion scienceQuestion = AppDatabase.getDatabaseInstance(getActivity()).getScienceQuestionDao().getQuestionByQID(this.scienceQuestion.getQid());
            if (scienceQuestion.isParaQuestion()) {
                String para = AppDatabase.getDatabaseInstance(getActivity()).getScienceQuestionDao().getParabyRefId(scienceQuestion.getRefParaID());
                showZoomDialog(getActivity(), "", para);
            }
        }
    }
}
