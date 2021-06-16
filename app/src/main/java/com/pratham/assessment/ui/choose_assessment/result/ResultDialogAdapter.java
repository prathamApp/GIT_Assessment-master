package com.pratham.assessment.ui.choose_assessment.result;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.pratham.assessment.AssessmentApplication;
import com.pratham.assessment.R;
import com.pratham.assessment.constants.Assessment_Constants;
import com.pratham.assessment.custom.gif_viewer.GifView;
import com.pratham.assessment.domain.ScienceQuestionChoice;
import com.pratham.assessment.utilities.Assessment_Utility;

import java.util.List;

import static com.pratham.assessment.utilities.Assessment_Utility.getFileExtension;

public class ResultDialogAdapter extends RecyclerView.Adapter<ResultDialogAdapter.MyViewHolder> {
    Context context;
    List<ScienceQuestionChoice> scienceQuestionChoices;
    String type;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        TextView text;
        ImageView image;
        GifView gifView;
        RelativeLayout rl_img, rl_root;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            text = itemView.findViewById(R.id.tv_text);
            image = itemView.findViewById(R.id.iv_choice_image);
            gifView = itemView.findViewById(R.id.iv_choice_gif);
            rl_img = itemView.findViewById(R.id.rl_img);
            rl_root = itemView.findViewById(R.id.rl_root);
        }
    }


    public ResultDialogAdapter(Context context, List<ScienceQuestionChoice> scienceQuestionChoices, String type) {
        this.context = context;
        this.scienceQuestionChoices = scienceQuestionChoices;
        this.type = type;
    }

    @NonNull
    @Override
    public ResultDialogAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(context).inflate(R.layout.layout_simple_text_row_old, viewGroup, false);
        return new ResultDialogAdapter.MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder myViewHolder, int i) {
        final ScienceQuestionChoice scienceQuestionChoice = scienceQuestionChoices.get(i);
        Assessment_Utility.setOdiaFont(context, myViewHolder.text);
        myViewHolder.text.setTextColor(Color.BLACK);
        if (type.equalsIgnoreCase("que")) {
            if (!scienceQuestionChoice.getChoiceurl().equalsIgnoreCase("")) {
                myViewHolder.rl_img.setVisibility(View.VISIBLE);
                myViewHolder.image.setVisibility(View.VISIBLE);
                myViewHolder.text.setVisibility(View.GONE);

//                final String path = /*Assessment_Constants.loadOnlineImagePath +*/ scienceQuestionChoice.getChoiceurl();

                String fileName = Assessment_Utility.getFileName(scienceQuestionChoice.getQid(), scienceQuestionChoice.getChoiceurl());
                final String localPath = Assessment_Utility.getOptionLocalPath(scienceQuestionChoice, scienceQuestionChoice.getIsQuestionFromSDCard()); /*AssessmentApplication.assessPath + Assessment_Constants.STORE_DOWNLOADED_MEDIA_PATH + "/" + fileName;*/
                setMediaToView(localPath, myViewHolder.image, myViewHolder.gifView);
                /*Glide.with(context).asBitmap().
                        load(localPath).apply(new RequestOptions()
                        .fitCenter()
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .skipMemoryCache(true)
                        .placeholder(Drawable.createFromPath(localPath))
                        .format(DecodeFormat.PREFER_ARGB_8888)
                        .override(Target.SIZE_ORIGINAL))
                        .into(myViewHolder.image);*/

                myViewHolder.image.setOnClickListener(v -> {
                    Log.d("QQQ", "choice clicked....");
                    String path = ""  /*Assessment_Constants.loadOnlineImagePath +*/;
                    String fileName1 = "";
                    String localPath1 = "";

                    if (type.equalsIgnoreCase("que")) {
//                            path = scienceQuestionChoice.getChoiceurl();
                        fileName1 = Assessment_Utility.getFileName(scienceQuestionChoice.getQid(), scienceQuestionChoice.getChoiceurl());
                        if (scienceQuestionChoice.getIsQuestionFromSDCard()) {
                            localPath1 = scienceQuestionChoice.getChoiceurl();
                        } else
                            localPath1 = AssessmentApplication.assessPath + Assessment_Constants.STORE_DOWNLOADED_MEDIA_PATH + "/" + fileName1;
                    } else if (type.equalsIgnoreCase("ans")) {
//                            path = scienceQuestionChoice.getMatchingurl();
                        fileName1 = Assessment_Utility.getFileName(scienceQuestionChoice.getQid(), scienceQuestionChoice.getMatchingurl());
                        if (scienceQuestionChoice.getIsQuestionFromSDCard()) {
                            localPath1 = scienceQuestionChoice.getMatchingurl();
                        } else
                            localPath1 = AssessmentApplication.assessPath + Assessment_Constants.STORE_DOWNLOADED_MEDIA_PATH + "/" + fileName1;
                    }
                    Assessment_Utility.showZoomDialog(context, localPath1, "");
                });

            } else {
                myViewHolder.rl_img.setVisibility(View.GONE);
                myViewHolder.text.setText(Html.fromHtml(scienceQuestionChoice.getChoicename()));
            }
        } else {
            if (type.equalsIgnoreCase("ans")) {
               /* if (scienceQuestionChoice.getMyIscorrect().equalsIgnoreCase("true"))
                    myViewHolder.rl_root.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.green_bg));
                else myViewHolder.rl_root.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.red_bg));*/

                if (!scienceQuestionChoice.getMatchingurl().equalsIgnoreCase("")) {
                    myViewHolder.rl_img.setVisibility(View.VISIBLE);
                    myViewHolder.image.setVisibility(View.VISIBLE);
                    myViewHolder.text.setVisibility(View.GONE);

//                    final String path = /*Assessment_Constants.loadOnlineImagePath +*/ scienceQuestionChoice.getMatchingurl();

                    String fileName = Assessment_Utility.getFileName(scienceQuestionChoice.getQid(), scienceQuestionChoice.getMatchingurl());
                    final String localPath;
                    if (scienceQuestionChoice.getIsQuestionFromSDCard()) {
                        localPath = scienceQuestionChoice.getMatchingurl();
                    } else
                        localPath = AssessmentApplication.assessPath + Assessment_Constants.STORE_DOWNLOADED_MEDIA_PATH + "/" + fileName;


                  /*  Glide.with(context).asBitmap().
                            load(localPath).apply(new RequestOptions()
                            .fitCenter()
                            .diskCacheStrategy(DiskCacheStrategy.NONE)
                            .skipMemoryCache(true)
                            .placeholder(Drawable.createFromPath(localPath))
                            .format(DecodeFormat.PREFER_ARGB_8888)
                            .override(Target.SIZE_ORIGINAL))
                            .into(myViewHolder.image);*/

                    setMediaToView(localPath, myViewHolder.image, myViewHolder.gifView);

                    myViewHolder.image.setOnClickListener(v -> {
                        Log.d("QQQ", "choice clicked....");
//                            String path = ""  /*Assessment_Constants.loadOnlineImagePath +*/;
                        String fileName12 = "";
                        String localPath12 = "";

                        if (type.equalsIgnoreCase("que")) {
//                                path = scienceQuestionChoice.getChoiceurl();
                            fileName12 = Assessment_Utility.getFileName(scienceQuestionChoice.getQid(), scienceQuestionChoice.getChoiceurl());
                            if (scienceQuestionChoice.getIsQuestionFromSDCard()) {
                                localPath12 = scienceQuestionChoice.getChoiceurl();
                            } else
                                localPath12 = AssessmentApplication.assessPath + Assessment_Constants.STORE_DOWNLOADED_MEDIA_PATH + "/" + fileName12;
                        } else if (type.equalsIgnoreCase("ans")) {
//                                path = scienceQuestionChoice.getMatchingurl();
                            fileName12 = Assessment_Utility.getFileName(scienceQuestionChoice.getQid(), scienceQuestionChoice.getMatchingurl());
                            if (scienceQuestionChoice.getIsQuestionFromSDCard()) {
                                localPath12 = scienceQuestionChoice.getMatchingurl();
                            } else
                                localPath12 = AssessmentApplication.assessPath + Assessment_Constants.STORE_DOWNLOADED_MEDIA_PATH + "/" + fileName12;
                        }
                        Assessment_Utility.showZoomDialog(context, localPath12, "");
                    });
                } else {
                    myViewHolder.rl_img.setVisibility(View.GONE);
                    myViewHolder.text.setText(Html.fromHtml(scienceQuestionChoice.getMatchingname()));
                }
            }
        }
    }

    private void setMediaToView(String localPath, ImageView image, GifView gifView) {
        String extension = getFileExtension(localPath);

        if (extension.equalsIgnoreCase("PNG") ||
                extension.equalsIgnoreCase("gif") ||
                extension.equalsIgnoreCase("JPEG") ||
                extension.equalsIgnoreCase("JPG")) {
            Assessment_Utility.setQuestionImageToImageView(image, gifView, localPath, context);
        } else {
            if (extension.equalsIgnoreCase("mp4") ||
                    extension.equalsIgnoreCase("3gp")) {
                Assessment_Utility.setThumbnailForVideo(localPath, context, image);
            }
            image.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_play_circle));
            RelativeLayout.LayoutParams param = new RelativeLayout.LayoutParams(
                    250, 250);
            param.addRule(RelativeLayout.CENTER_IN_PARENT);
            image.setLayoutParams(param);
        }
    }


    @Override
    public int getItemCount() {
        return scienceQuestionChoices.size();
    }
}
