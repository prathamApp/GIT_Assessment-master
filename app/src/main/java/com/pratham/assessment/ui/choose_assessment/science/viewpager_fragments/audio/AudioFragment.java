package com.pratham.assessment.ui.choose_assessment.science.viewpager_fragments.audio;

import android.content.Intent;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.content.CursorLoader;
import android.text.Html;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.pratham.assessment.AssessmentApplication;
import com.pratham.assessment.R;
import com.pratham.assessment.constants.Assessment_Constants;
import com.pratham.assessment.custom.custom_dialogs.ChooseImageDialog;
import com.pratham.assessment.custom.gif_viewer.GifView;
import com.pratham.assessment.database.AppDatabase;
import com.pratham.assessment.domain.DownloadMedia;
import com.pratham.assessment.domain.ScienceQuestion;
import com.pratham.assessment.domain.ScienceQuestionChoice;
import com.pratham.assessment.ui.choose_assessment.science.ScienceAssessmentActivity;
import com.pratham.assessment.ui.choose_assessment.science.custom_dialogs.ImageListDialog_;
import com.pratham.assessment.ui.choose_assessment.science.interfaces.AssessmentAnswerListener;
import com.pratham.assessment.ui.choose_assessment.science.interfaces.AudioPlayerInterface;
import com.pratham.assessment.utilities.Assessment_Utility;
import com.pratham.assessment.utilities.AudioUtil;
import com.pratham.assessment.utilities.PermissionUtils;
import com.pratham.assessment.utilities.RealPathUtil;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static com.pratham.assessment.constants.Assessment_Constants.DOWNLOAD_MEDIA_TYPE_ANSWER_AUDIO;
import static com.pratham.assessment.constants.Assessment_Constants.DOWNLOAD_MEDIA_TYPE_ANSWER_MEDIA;
import static com.pratham.assessment.utilities.Assessment_Utility.formatMilliSeccond;
import static com.pratham.assessment.utilities.Assessment_Utility.getFileExtension;
import static com.pratham.assessment.utilities.Assessment_Utility.getFileName;
import static com.pratham.assessment.utilities.Assessment_Utility.setOdiaFont;
import static com.pratham.assessment.utilities.Assessment_Utility.showZoomDialog;

@EFragment(R.layout.layout_audio_row)
public class AudioFragment extends Fragment implements AudioPlayerInterface, AudioContract.AudioView {
    @ViewById(R.id.tv_question)
    TextView question;
    @ViewById(R.id.tv_duration)
    TextView tv_duration;
    @ViewById(R.id.iv_question_image)
    ImageView questionImage;
    @ViewById(R.id.iv_question_gif)
    GifView questionGif;
    /*    @ViewById(R.id.iv_question_audio)
        ImageView iv_question_audio;
        @ViewById(R.id.rl_question_audio)
        RelativeLayout rl_question_audio;*/
    @ViewById(R.id.iv_record_audio)
    ImageView iv_start_audio;
    @ViewById(R.id.rl_answer_audio)
    RelativeLayout rl_answer_audio;
    @ViewById(R.id.btn_record_audio)
    Button btn_record_audio;
    @ViewById(R.id.btn_show_recorded_audio)
    Button btn_show_recorded_audio;

    @ViewById(R.id.iv_answer_audio)
    ImageView iv_answer_audio;
    @ViewById(R.id.iv_close)
    ImageView iv_close;
    boolean isPlaying;
    boolean isAnsPlaying;
    boolean isAudioRecording;
    @ViewById(R.id.btn_view_hint)
    Button btn_view_hint;

    String fileName;
    String answerPath;
    String localPath;
    public List<String> audioList;

    private static final String POS = "pos";
    private static final String SCIENCE_QUESTION = "scienceQuestion";

    private int pos;
    private ScienceQuestion scienceQuestion;
    AssessmentAnswerListener assessmentAnswerListener;
    public static final int RECORD_AUDIO = 0;
    public static final int PICK_AUDIO_FROM_GALLERY = 1;
    public static final int SHOW_DIALOG = 2;
    int capturedImageCnt = 0;

    @Bean(AudioPresenter.class)
    AudioContract.AudioPresenter presenter;

    public AudioFragment() {
        // Required empty public constructor
    }


    public static AudioFragment newInstance(int pos, ScienceQuestion scienceQuestion) {
        AudioFragment_ audioFragment = new AudioFragment_();
        Bundle args = new Bundle();
        args.putInt("pos", pos);
        args.putSerializable("scienceQuestion", scienceQuestion);
        audioFragment.setArguments(args);
        return audioFragment;
    }

    @AfterViews
    public void init() {
        if (getArguments() != null) {
            pos = getArguments().getInt(POS, 0);
            scienceQuestion = (ScienceQuestion) getArguments().getSerializable(SCIENCE_QUESTION);
            assessmentAnswerListener = (ScienceAssessmentActivity) getActivity();

        }
        if (question != null)
            question.setMovementMethod(new ScrollingMovementMethod());
        setAudioQuestion();

    }


    /* @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         if (getArguments() != null) {
             pos = getArguments().getInt(POS, 0);
             scienceQuestion = (ScienceQuestion) getArguments().getSerializable(SCIENCE_QUESTION);
             assessmentAnswerListener = (ScienceAssessmentActivity) getActivity();

         }
         setAudioQuestion();

     }
 */
  /*  @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.layout_audio_row, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);
        setAudioQuestion();

    }
*/
    public void setAudioQuestion() {
        presenter.setView(this);

        audioList = new ArrayList();

        setOdiaFont(getActivity(), question);
        if (scienceQuestion.getIsAttempted() && scienceQuestion.getMatchingNameList() != null) {
            presenter.setAttemptedList(scienceQuestion);

            if (scienceQuestion.getMatchingNameList().size() > 0) {
                for (int i = 0; i < scienceQuestion.getMatchingNameList().size(); i++) {
                    audioList.add(scienceQuestion.getMatchingNameList().get(i).getQcid());

                }
            }
            btn_show_recorded_audio.setVisibility(View.VISIBLE);
        }


        iv_start_audio.setVisibility(View.GONE);
        btn_record_audio.setVisibility(View.VISIBLE);

        question.setText(Html.fromHtml(scienceQuestion.getQname()));
        if (scienceQuestion.getPhotourl() != null && !scienceQuestion.getPhotourl().equalsIgnoreCase("")) {
            questionImage.setVisibility(View.VISIBLE);

            fileName = getFileName(scienceQuestion.getQid(), scienceQuestion.getPhotourl());
//            path = Environment.getExternalStorageDirectory().toString() + "/.Assessment/Content/Downloaded" + "/" + fileName;
            final String localPath = Assessment_Utility.getQuestionLocalPath(scienceQuestion);
         /*   if (scienceQuestion.getIsQuestionFromSDCard())
                localPath = scienceQuestion.getPhotourl();
            else
                localPath = AssessmentApplication.assessPath + Assessment_Constants.STORE_DOWNLOADED_MEDIA_PATH + "/" + fileName;
*/
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
      /*      String path = scienceQuestion.getPhotourl();
            String[] imgPath = path.split("\\.");
            int len;
            if (imgPath.length > 0)
                len = imgPath.length - 1;
            else len = 0;
            if (imgPath[len].equalsIgnoreCase("gif")) {
                try {
                    InputStream gif;
*//*                    if (AssessmentApplication.wiseF.isDeviceConnectedToMobileOrWifiNetwork()) {
                        Glide.with(getActivity()).asGif()
                                .load(path)
                                .apply(new RequestOptions()
                                        .placeholder(Drawable.createFromPath(localPath)))
                                .into(questionImage);
                    } else {*//*
                    gif = new FileInputStream(localPath);
                    questionImage.setVisibility(View.GONE);
                    questionGif.setVisibility(View.VISIBLE);
                    questionGif.setGifResource(gif);
                    //}
                } catch (Exception e) {
                    e.printStackTrace();
                }

           *//*     Glide.with(getActivity()).asGif()
                        .load(path)
                        .apply(new RequestOptions()
                                .placeholder(Drawable.createFromPath(localPath)))
                        .into(questionImage);*//*
//                    zoomImg.setVisibility(View.VISIBLE);
            } else {
                Glide.with(getActivity())
                        .load(localPath)
                        .apply(new RequestOptions()
                                .diskCacheStrategy(DiskCacheStrategy.NONE)
                                .skipMemoryCache(true)
                                .placeholder(Drawable.createFromPath(localPath)))
                        .into(questionImage);
            }*/

            questionImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showZoomDialog(getActivity(), localPath, "");
                }
            });
            questionGif.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showZoomDialog(getActivity(), localPath, "");
                }
            });


        } else questionImage.setVisibility(View.GONE);


        try {

//            File direct = new File(AssessmentApplication.assessPath + Assessment_Constants.STORE_DOWNLOADED_MEDIA_PATH);
            File direct = new File(AssessmentApplication.assessPath);
            if (!direct.exists()) direct.mkdir();
//            direct = new File(Environment.getExternalStorageDirectory().toString() + "/.Assessment/Science/Content/Answers");
            direct = new File(AssessmentApplication.assessPath + Assessment_Constants.STORE_ANSWER_MEDIA_PATH);
            if (!direct.exists()) direct.mkdir();
    /*        iv_question_audio.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String fileName = scienceQuestion.getPaperid() + "_" + scienceQuestion.getQid() + ".mp3";

                    String path = Environment.getExternalStorageDirectory().toString() + "/.Assessment/Content/Answers/" + fileName;
                    if (isAudioRecording) {
                        isAudioRecording = false;
                        iv_start_audio.setImageResource(R.drawable.ic_play_circle);

                    } else {
                        isAudioRec1ording = true;
                        iv_start_audio.setImageResource(R.drawable.ic_pause);

                    }
                    assessmentAnswerListener.setAudio(path, isAudioRecording);
                }
            });*/
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


   /* @Click(R.id.iv_question_audio)
    public void playQuestionAudio() {
//        String fileName =scienceQuestion.getQid() + "_" + scienceQuestion.getPaperid()  + ".mp3";
        try {
//            String path = Environment.getExternalStorageDirectory().toString() + "/.Assessment/Content/Downloaded/" + fileName;
            if (isPlaying) {
                isPlaying = false;
//                iv_question_audio.setImageResource(R.drawable.ic_play_circle);
                AudioUtil.stopPlayingAudio();

            } else {
                isPlaying = true;
//                iv_question_audio.setImageResource(R.drawable.ic_pause);
                AudioUtil.playRecording(localPath, this);


            }
//            assessmentAnswerListener.setAudio(path, isAudioRecording);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }*/

    @Click(R.id.btn_record_audio)
    public void recAudio() {
        ChooseImageDialog chooseImageDialog = new ChooseImageDialog(getActivity());
        chooseImageDialog.btn_take_photo.setText(R.string.record_audio);
        chooseImageDialog.btn_take_photo.setOnClickListener(v -> {
            chooseImageDialog.dismiss();
            iv_start_audio.setVisibility(View.VISIBLE);
            btn_record_audio.setVisibility(View.GONE);
            btn_show_recorded_audio.setVisibility(View.GONE);
            startAudio();
        });

        chooseImageDialog.btn_choose_from_gallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseImageDialog.dismiss();
                if ((Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)) {
                    String[] permissionArray = new String[]{PermissionUtils.Manifest_WRITE_EXTERNAL_STORAGE};

                    if (!((ScienceAssessmentActivity) getActivity()).isPermissionsGranted(getActivity(), permissionArray)) {
                        Toast.makeText(getActivity(), R.string.give_storage_permissions, Toast.LENGTH_SHORT).show();
                    } else {
                        Intent intent_upload = new Intent();
                        intent_upload.setType("audio/*");
                        intent_upload.setAction(Intent.ACTION_GET_CONTENT);
                        startActivityForResult(intent_upload, PICK_AUDIO_FROM_GALLERY);
                    }
                } else {
                    Intent intent_upload = new Intent();
                    intent_upload.setType("audio/*");
                    intent_upload.setAction(Intent.ACTION_GET_CONTENT);
                    startActivityForResult(intent_upload, PICK_AUDIO_FROM_GALLERY);
                }
            }
        });
        chooseImageDialog.show();

    }

    @Click(R.id.btn_show_recorded_audio)
    public void onViewCaptured() {
        if (audioList.size() > 0) {
            showImageThumbnailDialog(audioList, false);
        } else {
            Toast.makeText(getActivity(), "Nothing to show.", Toast.LENGTH_SHORT).show();
        }
    }

    private void showImageThumbnailDialog(List imageList, boolean showButton) {
//        ImageListDialog imageListDialog = new ImageListDialog(getActivity(), imageList);
//        imageListDialog.show();
        Intent intent = new Intent(getActivity(), ImageListDialog_.class);
        intent.putParcelableArrayListExtra("imageList", (ArrayList<? extends Parcelable>) imageList);
        intent.putExtra("showDeleteButton", showButton);
        intent.putExtra(DOWNLOAD_MEDIA_TYPE_ANSWER_MEDIA, DOWNLOAD_MEDIA_TYPE_ANSWER_AUDIO);
        startActivityForResult(intent, SHOW_DIALOG);
    }


    @Click(R.id.iv_record_audio)
    public void startAudio() {
        try {
            if (isAnsPlaying) {
                isAnsPlaying = false;
                iv_answer_audio.setImageResource(R.drawable.ic_play_circle);
                btn_record_audio.setVisibility(View.GONE);
                AudioUtil.stopPlayingAudio();

            }

            String fileName = scienceQuestion.getQid() + "_" + scienceQuestion.getPaperid() + "_" + Assessment_Constants.DOWNLOAD_MEDIA_TYPE_ANSWER_AUDIO + "_" + capturedImageCnt + ".mp3";

//            String path = Environment.getExternalStorageDirectory().toString() + "/.Assessment/Content/Answers/" + fileName;
            answerPath = AssessmentApplication.assessPath + Assessment_Constants.STORE_ANSWER_MEDIA_PATH + "/" + fileName;

            if (isAudioRecording) {
                isAudioRecording = false;
                iv_start_audio.setImageResource(R.drawable.ic_mic_24dp);
                iv_start_audio.setElevation(5);
                AudioUtil.stopRecording();
                scienceQuestion.setUserAnswer(answerPath);
                audioList.add(answerPath);
                showImageThumbnailDialog(audioList, true);
                rl_answer_audio.setVisibility(View.GONE);
                iv_start_audio.setVisibility(View.GONE);
                btn_record_audio.setVisibility(View.VISIBLE);

//                assessmentAnswerListener.setAnswerInActivity("", answerPath, scienceQuestion.getQid(), null);


            } else {
                isAudioRecording = true;
                iv_start_audio.setImageResource(R.drawable.ic_stop_black_24dp);
                AudioUtil.startRecording(answerPath);
                iv_start_audio.setElevation(5);
                rl_answer_audio.setVisibility(View.GONE);


            }
//            assessmentAnswerListener.setAudio(path, isAudioRecording);
            MediaPlayer mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(answerPath);
            mediaPlayer.prepare();
            int finalTime = mediaPlayer.getDuration();
            String dur = formatMilliSeccond(finalTime);
            Log.d("finalTime", "onAnswerPlayClick: " + dur);
//            assessmentAnswerListener.setAudio(path, isAudioRecording);
            tv_duration.setText(dur);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Click(R.id.iv_answer_audio)
    public void onAnswerPlayClick() {
        try {
            // String fileName = scienceQuestion.getQid() + "_" + scienceQuestion.getPaperid() + "_" + Assessment_Constants.DOWNLOAD_MEDIA_TYPE_ANSWER_AUDIO + ".mp3";
            // String path = AssessmentApplication.assessPath + Assessment_Constants.STORE_ANSWER_MEDIA_PATH + "/" + fileName;
            String path = scienceQuestion.getUserAnswer();
            DownloadMedia media = AppDatabase.getDatabaseInstance(getActivity()).getDownloadMediaDao().getMediaByQidAndPaperId(scienceQuestion.getQid(), scienceQuestion.getPaperid(), DOWNLOAD_MEDIA_TYPE_ANSWER_AUDIO);
            if (media != null) path = media.getPhotoUrl();
            if (path != null && !path.equalsIgnoreCase("")) {
                if (isAnsPlaying) {
                    isAnsPlaying = false;
                    iv_answer_audio.setImageResource(R.drawable.ic_play_circle);
                    AudioUtil.stopPlayingAudio();

                } else {
                    isAnsPlaying = true;
                    iv_answer_audio.setImageResource(R.drawable.ic_pause);
                    AudioUtil.playRecording(path, this);


                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public void stopPlayer() {
        if (isAnsPlaying) {
            isAnsPlaying = false;
            if (iv_answer_audio != null)
                iv_answer_audio.setImageResource(R.drawable.ic_play_circle);
        }
        if (isPlaying) {
           /* if (iv_question_audio != null)
                iv_question_audio.setImageResource(R.drawable.ic_play_circle);*/
            isPlaying = false;
        }
    }

    @Override
    public void onPause() {
        super.onPause();
//        if(audioPlayerInterface!=null)
        String fileName = scienceQuestion.getQid() + "_" + scienceQuestion.getPaperid() + "_" + Assessment_Constants.DOWNLOAD_MEDIA_TYPE_ANSWER_AUDIO + ".mp3";

//            String path = Environment.getExternalStorageDirectory().toString() + "/.Assessment/Content/Answers/" + fileName;
        String path = AssessmentApplication.assessPath + Assessment_Constants.STORE_ANSWER_MEDIA_PATH + "/" + fileName;

        if (isAudioRecording) {
            isAudioRecording = false;
            iv_start_audio.setImageResource(R.drawable.ic_mic_24dp);
            iv_start_audio.setElevation(5);
            AudioUtil.stopRecording();
            scienceQuestion.setUserAnswer(path);
            rl_answer_audio.setVisibility(View.GONE);
            btn_record_audio.setVisibility(View.VISIBLE);
            iv_start_audio.setVisibility(View.GONE);
            if (audioList.size() > 0)
                btn_show_recorded_audio.setVisibility(View.VISIBLE);
            else btn_show_recorded_audio.setVisibility(View.GONE);

//            assessmentAnswerListener.setAnswerInActivity("", path, scienceQuestion.getQid(), null);


        }

        if (isPlaying || isAnsPlaying)
            AudioUtil.stopPlayingAudio();
        stopPlayer();
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


    @Click(R.id.iv_close)
    public void deleteAudio() {
        if (isAnsPlaying) {
            isAnsPlaying = false;
            AudioUtil.stopPlayingAudio();
        }
/*        if (new File(scienceQuestion.getUserAnswer()).exists())
            new File(scienceQuestion.getUserAnswer()).delete();*/
//        assessmentAnswerListener.setAnswerInActivity("", "", scienceQuestion.getQid(), null);
        rl_answer_audio.setVisibility(View.GONE);
        iv_start_audio.setVisibility(View.GONE);
        btn_record_audio.setVisibility(View.VISIBLE);
    }


    private String getRealPathFromURI(Uri contentUri) {
        String[] proj = {MediaStore.Images.Media.DATA};
        CursorLoader loader = new CursorLoader(getActivity(), contentUri, proj, null, null, null);
        Cursor cursor = loader.loadInBackground();
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        String result = cursor.getString(column_index);
        cursor.close();
        return result;
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
        try {
            if (resultCode == -1 && requestCode == PICK_AUDIO_FROM_GALLERY) {
                Uri selectedImage = data.getData();
                String path;
                path = RealPathUtil.getUriRealPathAboveKitkat(getActivity(), selectedImage);
                if (path.equalsIgnoreCase("")) {
                    path = RealPathUtil.getRealPathFromURI_API19New(getActivity(), selectedImage);
                }

//                path = getRealPathFromURI_API19(getActivity(), selectedImage);
                audioList.add(path);
                showImageThumbnailDialog(audioList, true);

                scienceQuestion.setUserAnswer(path);
//                assessmentAnswerListener.setAnswerInActivity("", path, scienceQuestion.getQid(), null);

                if (audioList.size() > 0) {
                    btn_show_recorded_audio.setVisibility(View.VISIBLE);
                } else btn_show_recorded_audio.setVisibility(View.GONE);

                MediaPlayer mediaPlayer = new MediaPlayer();
                mediaPlayer.setDataSource(path);
                mediaPlayer.prepare();
                int finalTime = mediaPlayer.getDuration();
                String dur = formatMilliSeccond(finalTime);
                Log.d("finalTime", "onAnswerPlayClick: " + dur);
//            assessmentAnswerListener.setAudipath, isAudioRecording);
                tv_duration.setText(dur);
                rl_answer_audio.setVisibility(View.GONE);
            } else if (resultCode == -1 && requestCode == RECORD_AUDIO) {
//                Bitmap photo = (Bitmap) data.getExtras().get("data");
//                Uri selectedImage = data.getData();
              /*  fileName = scienceQuestion.getQid() + "_" + scienceQuestion.getPaperid() + "_" + Assessment_Constants.DOWNLOAD_MEDIA_TYPE_ANSWER_IMAGE + "_" + capturedImageCnt + ".jpg";

                String file = answerPath + "/" + fileName;*/
                audioList.add(answerPath);
                showImageThumbnailDialog(audioList, true);
//                setImage(capturedImageUri);
//                if (currentFragment instanceof ImageAnswerFragment)
//                    ((ImageAnswerFragment) currentFragment).setImage(photo);
//                selectedImage.setImageBitmap(photo);
                // String selectedImagePath = getPath(photo);
//                setImage();
//                createDirectoryAndSaveFile(photo, fileName);
                if (audioList.size() > 0) {
                    btn_show_recorded_audio.setVisibility(View.VISIBLE);
                } else btn_show_recorded_audio.setVisibility(View.GONE);
                capturedImageCnt++;
            } else if (resultCode == 2 && requestCode == SHOW_DIALOG) {
                try {
                    List<ScienceQuestionChoice> answers = new ArrayList<>();

                    List audioList = data.getParcelableArrayListExtra("imageList");
//                    for (int j = 0; j < imageList.size(); j++) {
//                        for (int k = 0; k < newImageList.size(); k++) {
                 /*   Iterator<List> iterator = imageList.iterator();
                    while (iterator.hasNext()) {
//                        ScienceQuestion scienceQuestion = (ScienceQuestion) iterator.next();
                        if (!newImageList.contains(iterator.next()))
                            iterator.remove();
                    }*/


//                        }
//                    }
                    if (audioList.size() > 0) {
                        for (int i = 0; i < audioList.size(); i++) {
                            ScienceQuestionChoice answer = new ScienceQuestionChoice();
                            if (audioList.get(i) instanceof String) {
//                                fileName = scienceQuestion.getQid() + "_" + scienceQuestion.getPaperid() + "_" + Assessment_Constants.DOWNLOAD_MEDIA_TYPE_ANSWER_IMAGE + "_" + capturedImageCnt + ".jpg";
                                answer.setQcid((String) audioList.get(i));
                            }
//                            if (imageList.get(i) instanceof Uri) {
                            else {
                                String path;
                                path = RealPathUtil.getUriRealPathAboveKitkat(getActivity(), (Uri) audioList.get(i));
                                if (path.equalsIgnoreCase("")) {
                                    path = RealPathUtil.getRealPathFromURI_API19New(getActivity(), (Uri) audioList.get(i));
                                }
                                answer.setQcid(path);
//                                answer.setQcid(path);
                            }

//                            }
                            answers.add(answer);

                        }

                        Log.d("TAG", "onActivityResult: " + audioList.size());

                        assessmentAnswerListener.setAnswerInActivity("", "", scienceQuestion.getQid(), answers);

                    }
                    this.audioList = audioList;
                    if (audioList.size() > 0) {
                        btn_show_recorded_audio.setVisibility(View.VISIBLE);
                    } else btn_show_recorded_audio.setVisibility(View.GONE);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        } catch (
                Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void updateAudioList(List audioList) {
        this.audioList = audioList;
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


}
