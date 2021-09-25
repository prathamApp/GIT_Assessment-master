package com.pratham.assessment.ui.choose_assessment.science.adapters;


import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ScaleDrawable;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.pratham.assessment.R;
import com.pratham.assessment.custom.gif_viewer.GifView;
import com.pratham.assessment.domain.ScienceQuestionChoice;
import com.pratham.assessment.ui.choose_assessment.science.ItemMoveCallback;
import com.pratham.assessment.ui.choose_assessment.science.ScienceAssessmentActivity;
import com.pratham.assessment.ui.choose_assessment.science.interfaces.AssessmentAnswerListener;
import com.pratham.assessment.ui.choose_assessment.science.interfaces.StartDragListener;
import com.pratham.assessment.ui.choose_assessment.science.viewpager_fragments.arrange_sequence.ArrangeSequenceFragment;
import com.pratham.assessment.utilities.Assessment_Utility;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.pratham.assessment.utilities.Assessment_Utility.getFileExtension;
import static com.pratham.assessment.utilities.Assessment_Utility.getOptionLocalPath;
import static com.pratham.assessment.utilities.Assessment_Utility.setOdiaFont;
import static com.pratham.assessment.utilities.Assessment_Utility.setTamilFont;

public class ArrangeSeqDragDropAdapter extends RecyclerView.Adapter<ArrangeSeqDragDropAdapter.MyViewHolder> implements ItemMoveCallback.ItemTouchHelperContract {
    List<ScienceQuestionChoice> draggedList = new ArrayList<>();
    private List<ScienceQuestionChoice> data;
    Context context;
    //    DragDropListener dragDropListener;
    //    QuestionTypeListener questionTypeListener;
    StartDragListener startDragListener;
    AssessmentAnswerListener assessmentAnswerListener;
    String qid = "";
    //    String qtId = "";
//    String path;

    public class MyViewHolder extends RecyclerView.ViewHolder {

        private TextView mTitle;
        ImageView iv_choice_image;
        GifView iv_choice_gif;
        View rowView;
        ScrollView sv_arr_seq;
        LinearLayout ll_options;

        public MyViewHolder(View itemView) {
            super(itemView);

            rowView = itemView;
            mTitle = itemView.findViewById(R.id.tv_text);
            iv_choice_image = itemView.findViewById(R.id.iv_choice_image);
            iv_choice_gif = itemView.findViewById(R.id.iv_choice_gif);
            sv_arr_seq = itemView.findViewById(R.id.sv_arr_seq);
            ll_options = itemView.findViewById(R.id.ll_options);
        }
    }


    public ArrangeSeqDragDropAdapter(ArrangeSequenceFragment fragment, List<ScienceQuestionChoice> data, String qtId, Context context) {
        this.data = data;
        this.context = context;
//        questionTypeListener = scienceAdapter;
        startDragListener = fragment;
        assessmentAnswerListener = (ScienceAssessmentActivity) context;
//        this.qtId = qtId;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_simple_text_row_arr_seq, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {
//        holder.mTitle.setText(data.get(position));
//        if(data.get(position))

        setOdiaFont(context, holder.mTitle);
        setTamilFont(context, holder.mTitle);
        holder.setIsRecyclable(false);
        holder.mTitle.setTextColor(Assessment_Utility.selectedColor);
        Drawable drawable = context.getResources().getDrawable(R.drawable.ic_swap_vertical_small);
        drawable.setBounds(0, 0, (int) (drawable.getIntrinsicWidth() * 0.2),
                (int) (drawable.getIntrinsicHeight() * 0.2));
        ScaleDrawable sd = new ScaleDrawable(drawable, 0, 10, 10);
        holder.mTitle.setCompoundDrawablesWithIntrinsicBounds(sd.getDrawable(), null, null, null);

        draggedList.clear();
        if (data.size() > 0) {
            qid = data.get(0).getQid();
            ScienceQuestionChoice scienceQuestionChoice = data.get(position);
            final String localPath = getOptionLocalPath(scienceQuestionChoice,
                    scienceQuestionChoice.getIsQuestionFromSDCard());

            if (scienceQuestionChoice.getChoiceurl() != null && !scienceQuestionChoice.getChoiceurl().equalsIgnoreCase("")) {


                String extension = getFileExtension(localPath);
                if (extension.equalsIgnoreCase("PNG") ||
                        extension.equalsIgnoreCase("gif") ||
                        extension.equalsIgnoreCase("JPEG") ||
                        extension.equalsIgnoreCase("JPG")) {
                    Assessment_Utility.setQuestionImageToImageView(holder.iv_choice_image, holder.iv_choice_gif, localPath, context);
                } else {
                    if (extension.equalsIgnoreCase("mp4") ||
                            extension.equalsIgnoreCase("3gp")) {
                        Assessment_Utility.setThumbnailForVideo(localPath, context, holder.iv_choice_image);
                    }
                    holder.iv_choice_image.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_play_circle));
                    LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(250, 250);
                    param.gravity = Gravity.CENTER;
                    holder.iv_choice_image.setLayoutParams(param);
                }


            } else
                holder.mTitle.setText(Html.fromHtml(scienceQuestionChoice.getChoicename()));

            holder.ll_options.setOnTouchListener((v, event) -> {
                if (event.getAction() ==
                        MotionEvent.ACTION_DOWN) {
                    startDragListener.requestDrag(holder);
                }
                return false;
            });

            holder.iv_choice_image.setOnTouchListener((v, event) -> {
                if (event.getAction() ==
                        MotionEvent.ACTION_DOWN) {
                    startDragListener.requestDrag(holder);
                   /* String fileName = Assessment_Utility.getFileName(scienceQuestionChoice.getQid(), scienceQuestionChoice.getChoiceurl());
                    final String localPath = AssessmentApplication.assessPath + Assessment_Constants.STORE_DOWNLOADED_MEDIA_PATH + "/" + fileName;
*/
                    Assessment_Utility.showZoomDialog(context, localPath, "");
                }
                return false;
            });

            //todo add bubbleshowcase
       /* if (!Assessment_Constants.isShowcaseDisplayed)
            if (position == 0) {
                Assessment_Constants.isShowcaseDisplayed = true;
                new BubbleShowCaseBuilder((Activity) context)
                        .title("Note: ")
                        .description("swap to match the answer on the right to the word on the left")
                        .backgroundColor(ContextCompat.getColor(context, R.color.colorAccentDark))
                        .closeActionImage(ContextCompat.getDrawable(context, R.drawable.ic_close))
                        .targetView(holder.itemView).show();
            }*/
        }

    }


    @Override
    public int getItemCount() {
        return data.size();
    }


    @Override
    public void onRowMoved(int fromPosition, int toPosition) {
        try {

/*            if (fromPosition < toPosition) {
                for (int i = fromPosition; i < toPosition; i++) {
                    Collections.swap(data, i, i + 1);
                }
            } else {
                for (int i = fromPosition; i > toPosition; i--) {
                    Collections.swap(data, i, i - 1);
                }
            }
            notifyItemMoved(fromPosition, toPosition);
            draggedList = data;
            Log.d("sss", draggedList.toString());*/




         /*   Log.d("QQQ", fromPosition + " " + toPosition);
            if (fromPosition < toPosition && fromPosition > 0 ) {
                for (int i = fromPosition; i < toPosition; i++) {
                    Log.d("QQQfor1", fromPosition + " " + toPosition);
                    Collections.swap(data, i, i + 1);
                }
            } else {
                for (int i = fromPosition; i > toPosition; i--) {
                   Log.d("QQQfor2", fromPosition + " " + toPosition);
                    if (i > 0)
                        Collections.swap(data, i, i - 1);
                }
            }
            notifyItemMoved(fromPosition, toPosition);
            draggedList = data;
            startDragListener.onItemDragged(draggedList);

//            Log.d("sss", draggedList.toString());ba
//        dragDropListener.setList(draggedList, data.get(0).getQid());
//        questionTypeListener.setAnswer("", "", data.get(0).getQid(), draggedList);
            assessmentAnswerListener.setAnswerInActivity("", "", data.get(0).getQid(), draggedList);*/


            if (fromPosition < toPosition) {
                for (int i = fromPosition; i < toPosition; i++) {
                    Collections.swap(data, i, i + 1);
                }
            } else {
                for (int i = fromPosition; i > toPosition; i--) {
                    Collections.swap(data, i, i - 1);
                }
            }
            notifyItemMoved(fromPosition, toPosition);
            /* draggedList = data;*/
            assessmentAnswerListener.setAnswerInActivity("", "", qid, data);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onRowSelected(MatchPairDragDropAdapter.MyViewHolder myViewHolder) {

    }

    @Override
    public void onRowSelected(ArrangeSeqDragDropAdapter.MyViewHolder myViewHolder) {
        myViewHolder.rowView.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.gradient_selector));
        myViewHolder.mTitle.setTextColor(Assessment_Utility.selectedColor);
    }

    @Override
    public void onRowClear(MatchPairDragDropAdapter.MyViewHolder myViewHolder) {

    }

    @Override
    public void onRowClear(ArrangeSeqDragDropAdapter.MyViewHolder myViewHolder) {

    }

  /*  @Override
    public void onRowSelected(MyViewHolder myViewHolder) {
//       myViewHolder.rowView.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.ripple_rectangle));
        *//*  myViewHolder.mTitle.setTextColor(Assessment_Utility.selectedColor);
     *//*
        myViewHolder.rowView.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.gradient_selector));
        myViewHolder.mTitle.setTextColor(Assessment_Utility.selectedColor);
    }

    @Override
    public void onRowClear(MyViewHolder myViewHolder) {
//        myViewHolder.rowView.setBackgroundColor(Color.WHITE);
       *//* myViewHolder.rowView.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.gradient_selector));
        myViewHolder.mTitle.setTextColor(Assessment_Utility.selectedColor);*//*

    }*/
}


