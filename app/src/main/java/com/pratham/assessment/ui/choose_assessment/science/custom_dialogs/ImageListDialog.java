package com.pratham.assessment.ui.choose_assessment.science.custom_dialogs;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaPlayer;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.pratham.assessment.R;
import com.pratham.assessment.constants.Assessment_Constants;
import com.pratham.assessment.ui.choose_assessment.science.ScienceAssessmentActivity;
import com.pratham.assessment.ui.choose_assessment.science.interfaces.AudioPlayerInterface;
import com.pratham.assessment.ui.choose_assessment.science.interfaces.UpdateImageListListener;
import com.pratham.assessment.ui.choose_assessment.science.viewpager_fragments.image_answer.ImageAnswerFragment;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.pratham.assessment.constants.Assessment_Constants.DOWNLOAD_MEDIA_TYPE_ANSWER_AUDIO;
import static com.pratham.assessment.constants.Assessment_Constants.DOWNLOAD_MEDIA_TYPE_ANSWER_IMAGE;
import static com.pratham.assessment.constants.Assessment_Constants.DOWNLOAD_MEDIA_TYPE_ANSWER_MEDIA;
import static com.pratham.assessment.constants.Assessment_Constants.DOWNLOAD_MEDIA_TYPE_ANSWER_VIDEO;
import static com.pratham.assessment.ui.choose_assessment.science.viewpager_fragments.image_answer.ImageAnswerFragment.SHOW_DIALOG;
import static com.pratham.assessment.utilities.Assessment_Utility.formatMilliSeccond;

//import com.pratham.assessment.utilities.AudioUtil;

@EActivity(R.layout.image_list_dialog)
public class ImageListDialog extends AppCompatActivity implements AudioPlayerInterface {

    @ViewById(R.id.btn_ok_img)
    ImageButton btn_ok;
    @ViewById(R.id.btn_delete_img)
    ImageButton btn_delete_img;
    @ViewById(R.id.ib_prev)
    ImageButton ib_prev;
    @ViewById(R.id.ib_next)
    ImageButton ib_next;
    @ViewById(R.id.tv_img_label)
    TextView tv_img_label;
/*    @ViewById(R.id.iv_answer_audio)
    ImageView iv_answer_audio;*/


    @ViewById(R.id.vv_answer_play_video)
    VideoView vv_answer;
    @ViewById(R.id.rl_answer_video)
    RelativeLayout rl_answer_video;
    @ViewById(R.id.iv_answer_image_play_icon)
    ImageView iv_answer_image_play_icon;

    @ViewById(R.id.iv_play_audio)
    ImageView audio_view;
    @ViewById(R.id.iv_fast_forward)
    ImageView iv_fast_forward;
    @ViewById(R.id.iv_rewind)
    ImageView iv_rewind;
    @ViewById(R.id.tv_duration)
    TextView tv_duration;
    @ViewById(R.id.tv_start_time)
    TextView tv_start_time;
    @ViewById(R.id.sb_audio)
    SeekBar sb_audio;

    @ViewById(R.id.sv_image)
    ScrollView sv_image;
    @ViewById(R.id.rl_audio)
    RelativeLayout rl_audio;
    @ViewById(R.id.sv_video)
    ScrollView sv_video;
    boolean isAudioPlaying = false;
    MediaPlayer mediaPlayer;
    int oneTimeOnly = 0;
    int finalTime;
    int startTime;
    Handler myHandler;

    @ViewById(R.id.iv_captured_image)
    ImageView iv_captured_image;
    boolean showDeleteBtn;
    String mediaType = "";
    private List imageList;
    UpdateImageListListener listListener;
    private int currentCnt = 0;
    ImageAnswerFragment fragment;
    /*public ZoomImageDialog(@NonNull Context context, String path, String localPath) {
//        super(context,android.R.style.Theme_NoTitleBar_Fullscreen);
//        super(context, android.R.style.Theme_DeviceDefault_NoActionBar_Fullscreen);
//        super(context,android.R.style.Theme_Holo_NoActionBar_Fullscreen);
        this.context = context;
        this.path = path;
        this.localPath = localPath;
    }*/

    /*public ZoomImageDialog(Context context, String path, String qtid, String localPath) {
        super(context);
        this.context = context;
        this.path = path;
        this.qtid = qtid;
        this.localPath = localPath;
}*/

    @AfterViews
    public void init() {
        mediaPlayer = new MediaPlayer();
        myHandler = new Handler();
        this.imageList = getIntent().getParcelableArrayListExtra("imageList");
        showDeleteBtn = getIntent().getBooleanExtra("showDeleteButton", false);
        mediaType = getIntent().getStringExtra(DOWNLOAD_MEDIA_TYPE_ANSWER_MEDIA);
        if (mediaType.equalsIgnoreCase(DOWNLOAD_MEDIA_TYPE_ANSWER_AUDIO))
            tv_img_label.setText(getString(R.string.audio) + " : " + imageList.size() + "/" + imageList.size());
        else if (mediaType.equalsIgnoreCase(DOWNLOAD_MEDIA_TYPE_ANSWER_IMAGE))
            tv_img_label.setText(getString(R.string.image) + imageList.size() + "/" + imageList.size());
        else if (mediaType.equalsIgnoreCase(DOWNLOAD_MEDIA_TYPE_ANSWER_VIDEO))
            tv_img_label.setText(getString(R.string.video) + " : " + imageList.size() + "/" + imageList.size());

        showImages();
    }

   /* public ImageListDialog(Context context, List<String> imageList) {
        super(context, android.R.style.Theme_Holo_Light_Dialog_NoActionBar_MinWidth);

    }*/

    private void showImages() {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        ScienceAssessmentActivity.dialogOpen = true;
        if (!showDeleteBtn) {

            ib_next.setVisibility(View.INVISIBLE);
            if (showDeleteBtn) {
                btn_delete_img.setVisibility(View.VISIBLE);
                ib_prev.setVisibility(View.INVISIBLE);
                ib_next.setVisibility(View.INVISIBLE);
            } else {
                btn_delete_img.setVisibility(View.INVISIBLE);
                ib_prev.setVisibility(View.VISIBLE);
                ib_next.setVisibility(View.VISIBLE);
            }
        } else {
            ib_prev.setVisibility(View.INVISIBLE);
            ib_next.setVisibility(View.INVISIBLE);
        }
        setInitImage();

    }

    private void setInitImage() {
        if (imageList.size() > 0) {
            if (!showDeleteBtn) {
                if (imageList.size() == 1) ib_prev.setVisibility(View.INVISIBLE);

            }
            currentCnt = imageList.size() - 1;
            setUri(imageList.get(currentCnt));
            showPrevNext();
        } else {
            updateList();
           /* ImageAnswerFragment.updateList(imageList);
            finish();*/
        }
    }

    private void setUri(Object obj) {

        if (mediaType.equalsIgnoreCase(DOWNLOAD_MEDIA_TYPE_ANSWER_AUDIO)) {
            try {
                rl_audio.setVisibility(View.VISIBLE);
                sv_video.setVisibility(View.GONE);
                sv_image.setVisibility(View.GONE);
//                MediaPlayer mediaPlayer = new MediaPlayer();


          /*      mediaPlayer.prepare();
                int finalTime = mediaPlayer.getDuration();
                String dur = formatMilliSeccond(finalTime);
                Log.d("finalTime", "onAnswerPlayClick: " + dur);
//            assessmentAnswerListener.setAudio(path, isAudioRecording);
                tv_duration.setText(dur);
//                rl_answer_audio.setVisibility(View.VISIBLE);*/


                try {
                    if (obj instanceof Uri)
                        mediaPlayer.setDataSource(this, (Uri) obj);
                    else mediaPlayer.setDataSource(obj.toString());
                    mediaPlayer.prepare();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                finalTime = mediaPlayer.getDuration();

                startTime = mediaPlayer.getCurrentPosition();
                if (oneTimeOnly == 0) {
                    sb_audio.setMax(finalTime);
                    oneTimeOnly = 1;
                }

                mediaPlayer.setOnCompletionListener(mp1 -> {
                    audio_view.setImageResource(R.drawable.ic_play_circle);
                    isAudioPlaying = false;
                    try {
                        if (mp1.isPlaying())
                            mp1.stop();
                        myHandler = new Handler();
                        if (obj instanceof Uri)
                            mp1.setDataSource(this, (Uri) obj);
                        else mp1.setDataSource(obj.toString());
                        mp1.prepare();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
                tv_duration.setText(formatMilliSeccond(finalTime));


                tv_start_time.setText(formatMilliSeccond(startTime));

                sb_audio.setClickable(false);


            } catch (Exception e) {
                Toast.makeText(this, "Can't play this audio.", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }

        } else if (mediaType.equalsIgnoreCase(DOWNLOAD_MEDIA_TYPE_ANSWER_IMAGE)) {

            try {
                rl_audio.setVisibility(View.GONE);
                sv_video.setVisibility(View.GONE);
                sv_image.setVisibility(View.VISIBLE);
                Glide.with(this)
                        .load(obj)
                        .apply(new RequestOptions()
                                .diskCacheStrategy(DiskCacheStrategy.NONE)
                                .skipMemoryCache(true))
                        .into(iv_captured_image);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (mediaType.equalsIgnoreCase(DOWNLOAD_MEDIA_TYPE_ANSWER_VIDEO)) {
            rl_audio.setVisibility(View.GONE);
            sv_video.setVisibility(View.VISIBLE);
            sv_image.setVisibility(View.GONE);
            try {

                rl_answer_video.setVisibility(View.VISIBLE);
//        answerPath = Environment.getExternalStorageDirectory().toString() + Assessment_Constants.STORE_ANSWER_MEDIA_PATH + "/" +scienceQuestion.getUserAnswer();
//            answerPath = AssessmentApplication.assessPath + Assessment_Constants.STORE_ANSWER_MEDIA_PATH + "/" + scienceQuestion.getUserAnswer();
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inSampleSize = 1;
                Bitmap thumb = ThumbnailUtils.createVideoThumbnail(imageList.get(currentCnt).toString(), MediaStore.Images.Thumbnails.MICRO_KIND);
                BitmapDrawable ob = new BitmapDrawable(getResources(), thumb);
                iv_answer_image_play_icon.setBackgroundDrawable(ob);
                iv_answer_image_play_icon.setVisibility(View.VISIBLE);
//                VideoCaptured = true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Click(R.id.ib_next)
    public void nextImg() {
        currentCnt++;


        vv_answer.stopPlayback();
        if (isAudioPlaying) {
            isAudioPlaying = false;
//            iv_answer_audio.setImageResource(R.drawable.ic_play_circle);
//            AudioUtil.stopPlayingAudio();
            mediaPlayer.stop();
            mediaPlayer.seekTo(0);
        }
        if (currentCnt < imageList.size()) {
            setUri(imageList.get(currentCnt));
        } else {
            currentCnt = imageList.size();
        }
        showPrevNext();

    }


    /* @Click(R.id.iv_answer_audio)
     public void playAnsAudio() {
         Object path = imageList.get(currentCnt);
         if (path != null) {
             if (isAnsPlaying) {
                 isAnsPlaying = false;
                 iv_answer_audio.setImageResource(R.drawable.ic_play_circle);
                 AudioUtil.stopPlayingAudio();

             } else {
                 isAnsPlaying = true;
                 iv_answer_audio.setImageResource(R.drawable.ic_pause);
                 if (path instanceof Uri)
                     AudioUtil.playRecording((Uri) path, ImageListDialog.this, this);
                 else AudioUtil.playRecording(path.toString(), ImageListDialog.this);


             }
         }
     }
 */
    @Click({R.id.iv_answer_image_play_icon, R.id.vv_answer_play_video})
    public void onAnswerVideoClicked() {
        MediaController mediaController = new MediaController(this);
        iv_answer_image_play_icon.setVisibility(View.GONE);
        vv_answer.setVisibility(View.VISIBLE);
        if (imageList.get(currentCnt) instanceof Uri)
            vv_answer.setVideoURI((Uri) imageList.get(currentCnt));
        else
            vv_answer.setVideoPath(imageList.get(currentCnt).toString());
        vv_answer.setMediaController(mediaController);
        mediaController.setAnchorView(vv_answer);
        vv_answer.setZOrderOnTop(true);
        vv_answer.setZOrderMediaOverlay(true);
        vv_answer.start();
    }

    @Click(R.id.ib_prev)
    public void prevImg() {
        currentCnt--;

        vv_answer.stopPlayback();
        if (isAudioPlaying) {
            isAudioPlaying = false;
//            iv_answer_audio.setImageResource(R.drawable.ic_play_circle);
//            AudioUtil.stopPlayingAudio();
            mediaPlayer.stop();
            mediaPlayer.seekTo(0);

        }
        if (currentCnt > -1) {
            setUri(imageList.get(currentCnt));
        } else currentCnt = 0;


        showPrevNext();

    }

    private void showPrevNext() {
        if (!showDeleteBtn)
            tv_img_label.setText(getString(R.string.image) + (currentCnt + 1) + "/" + imageList.size());

        if (!showDeleteBtn)
            if (currentCnt > -1 && currentCnt < imageList.size() - 1)
                ib_next.setVisibility(View.VISIBLE);
            else ib_next.setVisibility(View.INVISIBLE);

        if (!showDeleteBtn)
            if (currentCnt > 0)
                ib_prev.setVisibility(View.VISIBLE);
            else ib_prev.setVisibility(View.INVISIBLE);
    }


    @Click(R.id.btn_ok_img)
    public void closeDialog() {
        ScienceAssessmentActivity.dialogOpen = false;
        vv_answer.stopPlayback();
        if (isAudioPlaying) {
            isAudioPlaying = false;
//            iv_answer_audio.setImageResource(R.drawable.ic_play_circle);
//            AudioUtil.stopPlayingAudio();
            mediaPlayer.stop();
        }

        if (showDeleteBtn)
            updateList();
        else finish();

//        ImageAnswerFragment.updateList(imageList);
    }

    @Click(R.id.iv_fast_forward)
    public void onFastForwardClicked() {
        sb_audio.setClickable(false);

        if (isAudioPlaying) {
            int temp = (int) startTime;
            if ((temp + 1000) <= finalTime) {
                startTime = startTime + 1000;
                mediaPlayer.seekTo((int) startTime);
                sb_audio.setProgress((int) startTime);
                tv_start_time.setText(formatMilliSeccond(startTime));

            }
        }
    }

    @Click(R.id.iv_rewind)
    public void onRewindClicked() {
        sb_audio.setClickable(false);

        if (isAudioPlaying) {
            int temp = (int) startTime;
            if ((temp - 1000) > 0) {
                startTime = startTime - 1000;
                mediaPlayer.seekTo((int) startTime);
                sb_audio.setProgress((int) startTime);
                tv_start_time.setText(formatMilliSeccond(startTime));
            }
        }
    }

    private void updateList() {
        vv_answer.stopPlayback();
        if (isAudioPlaying) {
            isAudioPlaying = false;
//            iv_answer_audio.setImageResource(R.drawable.ic_play_circle);
//            AudioUtil.stopPlayingAudio();
            mediaPlayer.stop();

        }

        Intent intent = new Intent();
        intent.putStringArrayListExtra("imageList", (ArrayList<String>) imageList);
        intent.putExtra(DOWNLOAD_MEDIA_TYPE_ANSWER_MEDIA, Assessment_Constants.DOWNLOAD_MEDIA_TYPE_ANSWER_IMAGE);
        setResult(SHOW_DIALOG, intent);
        finish();
    }

    @Click(R.id.btn_delete_img)
    public void deleteImage() {
        imageList.remove(currentCnt);
//        ImageAnswerFragment.updateList(imageList);
        updateList();
//        setInitImage();
//        finish();
    }
  /* @Click(R.id.btn_confirm)
    public void confirm() {
        imageList.remove(currentCnt);
//        ImageAnswerFragment.updateList(imageList);
        updateList();
//        setInitImage();
    }*/

    @Override
    protected void onStop() {
        super.onStop();
        ScienceAssessmentActivity.dialogOpen = false;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_BACK) {
            super.onKeyDown(keyCode, event);
            return true;
        }
        return false;
    }

    @Click(R.id.iv_play_audio)
    public void listenAudio() {
        sb_audio.setClickable(false);

        if (isAudioPlaying) {
            isAudioPlaying = false;
            audio_view.setImageResource(R.drawable.ic_play_circle);
//            AudioUtil.stopPlayingAudio();
            mediaPlayer.stop();
            mediaPlayer.seekTo(0);
//            stopPlayer();

        } else {
            isAudioPlaying = true;
            audio_view.setImageResource(R.drawable.ic_pause);
//            AudioUtil.playRecording(localPath, this);
            mediaPlayer.start();
            myHandler.postDelayed(UpdateSongTime, 100);
        }


 /*           final int duration = mediaPlayer.getDuration();
            final int amountToUpdate = duration / 100;
            Timer mTimer = new Timer();
            mTimer.schedule(new TimerTask() {

                @Override
                public void run() {
                    runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            if (!(amountToUpdate * sb_audio.getProgress() >= duration)) {
                                int p = sb_audio.getProgress();
                                p += 1;
                                sb_audio.setProgress(p);
                            }
                        }
                    });
                }

                ;
            }, amountToUpdate);*/

//        final SeekBar mSeelBar = new SeekBar(this);

    }

    private Runnable UpdateSongTime = new Runnable() {
        @SuppressLint("DefaultLocale")
        @UiThread
        public void run() {
            try {
                startTime = mediaPlayer.getCurrentPosition();
       /*         tv_start_time.setText(String.format("%d min, %d sec",
                        TimeUnit.MILLISECONDS.toMinutes((long) startTime),
                        TimeUnit.MILLISECONDS.toSeconds((long) startTime) -
                                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.
                                        toMinutes((long) startTime)))
                );*/
                tv_start_time.setText(formatMilliSeccond(startTime));
                Log.d("AUDIO ACT", "run: " + startTime);
                sb_audio.setProgress((int) startTime);
                myHandler.postDelayed(this, 100);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };


    @Override
    public void onBackPressed() {
        if (isAudioPlaying) {
            stopPlayer();
//            AudioUtil.stopPlayingAudio();
            mediaPlayer.stop();
        }
        /*super.onBackPressed();
        ScienceAssessmentActivity.dialogOpen = false;*/
//        ImageAnswerFragment.updateList(imageList);
//        updateList();
    }

    @Override
    public void stopPlayer() {
        if (isAudioPlaying) {
            isAudioPlaying = false;
       /*     if (iv_answer_audio != null)
                iv_answer_audio.setImageResource(R.drawable.ic_play_circle);*/
        }
    }


}



