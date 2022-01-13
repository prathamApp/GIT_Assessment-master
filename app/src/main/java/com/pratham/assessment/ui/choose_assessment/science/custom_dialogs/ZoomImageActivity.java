package com.pratham.assessment.ui.choose_assessment.science.custom_dialogs;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.VideoView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.pratham.assessment.R;
import com.pratham.assessment.custom.gif_viewer.GifViewZoom;
import com.pratham.assessment.custom.zoom_image.ZoomageView;
import com.pratham.assessment.ui.choose_assessment.science.ScienceAssessmentActivity;
import com.pratham.assessment.ui.choose_assessment.science.interfaces.AudioPlayerInterface;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import static com.pratham.assessment.utilities.Assessment_Utility.formatMilliSeccond;
import static com.pratham.assessment.utilities.Assessment_Utility.getFileExtension;

@EActivity(R.layout.zoom_image_dialog)
public class ZoomImageActivity extends AppCompatActivity implements AudioPlayerInterface {

    @ViewById(R.id.btn_ok_img)
    ImageButton btn_ok;
    @ViewById(R.id.iv_zoom_img)
    ZoomageView zoomImg;
    @ViewById(R.id.iv_img)
    GifViewZoom gifView;
    @ViewById(R.id.vv_video)
    VideoView videoView;
    @ViewById(R.id.iv_play_audio)
    ImageView audio_view;
    @ViewById(R.id.iv_fast_forward)
    ImageView iv_fast_forward;
    @ViewById(R.id.iv_rewind)
    ImageView iv_rewind;

    @ViewById(R.id.rl_audio)
    RelativeLayout rl_audio;
    //    private Context context;
//    private String path;
    @ViewById(R.id.rl_text_view)
    RelativeLayout rl_para;
    @ViewById(R.id.tv_text_view)
    TextView text;
    @ViewById(R.id.sv_para)
    ScrollView sv_para;
    @ViewById(R.id.tv_duration)
    TextView tv_duration;
    @ViewById(R.id.tv_start_time)
    TextView tv_start_time;
    @ViewById(R.id.sb_audio)
    SeekBar sb_audio;
    String para = "";
    private String localPath;
    boolean isAudioPlaying = false;
    int startTime;
    Handler myHandler;

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
    MediaPlayer mediaPlayer;
    int oneTimeOnly = 0;
    int finalTime;

    @AfterViews
    public void init() {
        rl_para.setVisibility(View.GONE);
        mediaPlayer = new MediaPlayer();
        myHandler = new Handler();

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
//        path = getIntent().getStringExtra("onlinePath");
        localPath = getIntent().getStringExtra("localPath");
        para = getIntent().getStringExtra("paragraph");
        ScienceAssessmentActivity.dialogOpen = true;

        if (para != null && !para.equalsIgnoreCase("")) {
            rl_para.setVisibility(View.VISIBLE);
            text.setText(Html.fromHtml(para));
            text.setVisibility(View.VISIBLE);
            zoomImg.setVisibility(View.GONE);
            gifView.setVisibility(View.GONE);
            videoView.setVisibility(View.GONE);
            rl_audio.setVisibility(View.GONE);
        }
        String extension = "";
        if (localPath != null && !localPath.trim().equalsIgnoreCase("")) {
            localPath = localPath.trim();
            extension = getFileExtension(localPath);
        }
      /*  else if (!path.equalsIgnoreCase(""))
            extension = getFileExtension(path);*/

        if (extension.equalsIgnoreCase("mp4") ||
                extension.equalsIgnoreCase("3gp")) {
            rl_para.setVisibility(View.GONE);

            zoomImg.setVisibility(View.GONE);
            gifView.setVisibility(View.GONE);
            videoView.setVisibility(View.VISIBLE);

            //Creating MediaController
            final MediaController mediaController = new MediaController(this);
//            mediaController.setAnchorView(videoView);

            //specify the location of media file

            //Setting MediaController and URI, then starting the videoView
           /* videoView.setMediaController(mediaController);
            videoView.setVideoURI(uri);
            videoView.requestFocus();
            videoView.start();*/

            videoView.setVideoPath(localPath);
            videoView.setMediaController(mediaController);
            mediaController.setAnchorView(videoView);
            videoView.setZOrderOnTop(true);
            videoView.setZOrderMediaOverlay(true);
            videoView.start();

        } else if (extension.equalsIgnoreCase("gif")) {
            try {
                InputStream gif = new FileInputStream(localPath);
                zoomImg.setVisibility(View.GONE);
                rl_para.setVisibility(View.GONE);
                gifView.setVisibility(View.VISIBLE);
                gifView.setGifResource(gif);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (extension.equalsIgnoreCase("jpg")
                || extension.equalsIgnoreCase("jpeg")
                || extension.equalsIgnoreCase("png")) {
            zoomImg.setVisibility(View.VISIBLE);
//                    Glide.get(context).clearDiskCache();
            rl_para.setVisibility(View.GONE);

            Glide.with(this)
                    .load(localPath)
//                            .apply(new RequestOptions().diskCacheStrategy(DiskCacheStrategy.NONE))
                    .apply(new RequestOptions()
                            .diskCacheStrategy(DiskCacheStrategy.NONE)
                            .skipMemoryCache(true)
                            .placeholder(Drawable.createFromPath(localPath)))
                    .into(zoomImg);
            gifView.setVisibility(View.GONE);

        } else if (extension.equalsIgnoreCase("mp3")
                || extension.equalsIgnoreCase("wav")
                || extension.equalsIgnoreCase("3gpp")
                || extension.equalsIgnoreCase("m4a")
                || extension.equalsIgnoreCase("amr")) {
            getWindow().setBackgroundDrawable(new ColorDrawable(Color.WHITE));

            rl_para.setVisibility(View.GONE);
            zoomImg.setVisibility(View.GONE);
            gifView.setVisibility(View.GONE);
            rl_audio.setVisibility(View.VISIBLE);


            try {
                mediaPlayer.setDataSource(localPath);
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
                    mp1.setDataSource(localPath);
                    mp1.prepare();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            tv_duration.setText(formatMilliSeccond(finalTime));
          /*  tv_duration.setText(String.format("%d min, %d sec",
                    TimeUnit.MILLISECONDS.toMinutes((long) finalTime),
                    TimeUnit.MILLISECONDS.toSeconds((long) finalTime) -
                            TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes((long)
                                    finalTime)))
            );*/
            tv_start_time.setText(formatMilliSeccond(startTime));
           /* tv_start_time.setText(String.format("%d min, %d sec",
                    TimeUnit.MILLISECONDS.toMinutes((long) startTime),
                    TimeUnit.MILLISECONDS.toSeconds((long) startTime) -
                            TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes((long)
                                    startTime)))
            );*/
            sb_audio.setClickable(false);


            listenAudio();
        }


      /*  sb_audio.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if (mediaPlayer != null && b) {

                    mediaPlayer.seekTo(i * 1000);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
      */
    }



    /*@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.zoom_image_dialog);
        ButterKnife.bind(this);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        path = getIntent().getStringExtra("onlinePath");
        localPath = getIntent().getStringExtra("localPath");
        String extension = "";
        if (!localPath.equalsIgnoreCase(""))
            extension = getFileExtension(localPath);
        else if (!path.equalsIgnoreCase(""))
            extension = getFileExtension(path);

        if (!extension.equalsIgnoreCase("") && extension.equalsIgnoreCase("mp4")) {

            zoomImg.setVisibility(View.GONE);
            gifView.setVisibility(View.GONE);
            videoView.setVisibility(View.VISIBLE);

            //Creating MediaController
            final MediaController mediaController = new MediaController(this);
//            mediaController.setAnchorView(videoView);

            //specify the location of media file

            //Setting MediaController and URI, then starting the videoView
           *//* videoView.setMediaController(mediaController);
            videoView.setVideoURI(uri);
            videoView.requestFocus();
            videoView.start();*//*

            videoView.setVideoPath(path);
            videoView.setMediaController(mediaController);
            mediaController.setAnchorView(videoView);
            videoView.setZOrderOnTop(true);
            videoView.setZOrderMediaOverlay(true);
            videoView.start();

        } else {
            if (path != null) {
                String[] imgPath = path.split("\\.");
                int len;
                if (imgPath.length > 0)
                    len = imgPath.length - 1;
                else len = 0;
                if (imgPath[len].equalsIgnoreCase("gif")) {
                    if (AssessmentApplication.wiseF.isDeviceConnectedToMobileOrWifiNetwork()) {
                        Glide.with(this).asGif()
                                .load(path)
                                .apply(new RequestOptions()
                                        .placeholder(Drawable.createFromPath(localPath)))
                                .into(zoomImg);
                        zoomImg.setVisibility(View.VISIBLE);
                        gifView.setVisibility(View.GONE);
                    } else {
                        try {
                            InputStream gif = new FileInputStream(localPath);
                            zoomImg.setVisibility(View.GONE);
                            gifView.setVisibility(View.VISIBLE);
                            gifView.setGifResource(gif);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                } else if (imgPath[len].equalsIgnoreCase("jpg") || imgPath[len].equalsIgnoreCase("png")) {
                    zoomImg.setVisibility(View.VISIBLE);
//                    Glide.get(context).clearDiskCache();

                    Glide.with(this)
                            .load(path)
//                            .apply(new RequestOptions().diskCacheStrategy(DiskCacheStrategy.NONE))
                            .apply(new RequestOptions()
                                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                                    .skipMemoryCache(true)
                                    .placeholder(Drawable.createFromPath(localPath)))
                            .into(zoomImg);
                    gifView.setVisibility(View.GONE);

                } else if (imgPath[len].equalsIgnoreCase("mp3")) {
                    zoomImg.setVisibility(View.GONE);
                    gifView.setVisibility(View.GONE);
                    rl_audio.setVisibility(View.VISIBLE);

                }
            }
        }
    }
*/

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

    @Override
    protected void onStop() {
        super.onStop();
        ScienceAssessmentActivity.dialogOpen = false;

    }

    @Click(R.id.btn_ok_img)
    public void closeDialog() {
        ScienceAssessmentActivity.dialogOpen = false;

        if (isAudioPlaying) {
            stopPlayer();
//            AudioUtil.stopPlayingAudio();
            mediaPlayer.stop();
        }
//        dismiss();
        finish();
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
    public void stopPlayer() {
        if (isAudioPlaying) {
            isAudioPlaying = false;
//            AudioUtil.stopPlayingAudio();
            mediaPlayer.stop();
            audio_view.setImageResource(R.drawable.ic_play_circle);
        }
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        ScienceAssessmentActivity.dialogOpen = false;
        if (isAudioPlaying) {
            stopPlayer();
//            AudioUtil.stopPlayingAudio();
            mediaPlayer.stop();
        }
    }
}

