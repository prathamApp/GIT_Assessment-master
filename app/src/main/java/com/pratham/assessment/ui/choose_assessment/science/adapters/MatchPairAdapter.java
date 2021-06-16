package com.pratham.assessment.ui.choose_assessment.science.adapters;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.pratham.assessment.R;
import com.pratham.assessment.custom.gif_viewer.GifView;
import com.pratham.assessment.domain.ScienceQuestionChoice;
import com.pratham.assessment.utilities.Assessment_Utility;

import java.util.List;

import static com.pratham.assessment.utilities.Assessment_Utility.getFileExtension;
import static com.pratham.assessment.utilities.Assessment_Utility.getOptionLocalPath;

public class MatchPairAdapter extends RecyclerView.Adapter<MatchPairAdapter.MyViewHolder> {
    List<ScienceQuestionChoice> pairList;
    Context context;

    public MatchPairAdapter(List<ScienceQuestionChoice> pairList, Context context) {
        this.pairList = pairList;
        this.context = context;
    }


    public class MyViewHolder extends RecyclerView.ViewHolder {
        TextView text;
        ImageView imageView;
        GifView gifView;
        RelativeLayout rl_img;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            text = itemView.findViewById(R.id.tv_text);
            imageView = itemView.findViewById(R.id.iv_choice_image);
            gifView = itemView.findViewById(R.id.iv_choice_gif);
            rl_img = itemView.findViewById(R.id.rl_img);
        }
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(context).inflate(R.layout.layout_simple_text_row_old, viewGroup, false);
        return new MyViewHolder(view);
    }

    /*@Override
    public void onBindViewHolder(@NonNull final MyViewHolder myViewHolder, int i) {
        ScienceQuestionChoice scienceQuestionChoice = pairList.get(i);
        setOdiaFont(context, myViewHolder.text);

        if (!scienceQuestionChoice.getChoiceurl().equalsIgnoreCase("")) {
            myViewHolder.imageView.setVisibility(View.VISIBLE);
            myViewHolder.text.setVisibility(View.GONE);

            Glide.with(context).asBitmap().
                    load(*//*Assessment_Constants.loadOnlineImagePath + *//*scienceQuestionChoice.getChoiceurl()).apply(new RequestOptions()
                    .fitCenter()
                    .format(DecodeFormat.PREFER_ARGB_8888)
                    .override(Target.SIZE_ORIGINAL))
                    .into(myViewHolder.imageView);
        } else myViewHolder.text.setText(scienceQuestionChoice.getChoicename());

    }*/

    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder holder, int i) {
        final ScienceQuestionChoice scienceQuestionChoice = pairList.get(i);
        if (scienceQuestionChoice.getChoiceurl() != null
                && !scienceQuestionChoice.getChoiceurl().equalsIgnoreCase("")) {
//            final String path = scienceQuestionChoice.getChoiceurl();

            String fileName = Assessment_Utility.getFileName(scienceQuestionChoice.getQid(), scienceQuestionChoice.getChoiceurl());
            final String localPath = getOptionLocalPath(scienceQuestionChoice, scienceQuestionChoice.getIsQuestionFromSDCard());/*AssessmentApplication.assessPath + Assessment_Constants.STORE_DOWNLOADED_MEDIA_PATH + "/" + fileName;
             */
//                holder.iv_choice_image.setVisibility(View.VISIBLE);
            holder.rl_img.setVisibility(View.VISIBLE);
            holder.text.setVisibility(View.GONE);
            holder.text.setTextColor(Color.WHITE);
           /* Glide.with(context).asBitmap().
                    load(path).apply(new RequestOptions()
                    .fitCenter()
                    .format(DecodeFormat.PREFER_ARGB_8888)
                    .override(Target.SIZE_ORIGINAL))
                    .into(holder.imageView);*/

            String extension = getFileExtension(localPath);

            if (extension.equalsIgnoreCase("PNG") ||
                    extension.equalsIgnoreCase("gif") ||
                    extension.equalsIgnoreCase("JPEG") ||
                    extension.equalsIgnoreCase("JPG")) {
                Assessment_Utility.setQuestionImageToImageView(holder.imageView, holder.gifView, localPath, context);
            } else {
                if (extension.equalsIgnoreCase("mp4") ||
                        extension.equalsIgnoreCase("3gp")) {
                    Assessment_Utility.setThumbnailForVideo(localPath, context, holder.imageView);
                }
                holder.imageView.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_play_circle));
                RelativeLayout.LayoutParams param = new RelativeLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, 190);
                param.addRule(RelativeLayout.CENTER_IN_PARENT);
                holder.imageView.setPadding(5,5,5,5);
                holder.imageView.setLayoutParams(param);
            }
          /*  Glide.with(context)
                    .load(path)
//                            .apply(new RequestOptions().diskCacheStrategy(DiskCacheStrategy.NONE))
                    .apply(new RequestOptions()
                            .diskCacheStrategy(DiskCacheStrategy.NONE)
                            .skipMemoryCache(true)
                            .placeholder(Drawable.createFromPath(localPath)))
                    .into(holder.imageView);*/

//                holder.iv_choice_image.setOnClickListener(new View.OnClickListener() {
            holder.imageView.setOnClickListener(v -> Assessment_Utility.showZoomDialog(context, localPath, ""));
            holder.gifView.setOnClickListener(v -> Assessment_Utility.showZoomDialog(context, localPath, ""));
        } else {
            holder.text.setMovementMethod(new ScrollingMovementMethod());
            holder.rl_img.setVisibility(View.GONE);
            if (scienceQuestionChoice.getChoicename() != null && !scienceQuestionChoice.getChoicename().equalsIgnoreCase(""))
                holder.text.setText(Html.fromHtml(scienceQuestionChoice.getChoicename()));
        }

    }

    @Override
    public int getItemCount() {
        return pairList.size();
    }
}
