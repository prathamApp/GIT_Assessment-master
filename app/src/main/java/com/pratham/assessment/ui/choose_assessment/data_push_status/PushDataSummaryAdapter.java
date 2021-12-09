package com.pratham.assessment.ui.choose_assessment.data_push_status;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.pratham.assessment.R;
import com.pratham.assessment.domain.Modal_Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class PushDataSummaryAdapter extends RecyclerView.Adapter<PushDataSummaryAdapter.MyViewHolder> {
    Context context;
    List<Modal_Log> logs;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        TextView tv_time, tv_push_type, tv_score_count, tv_paper_count, tv_media_count;
        ConstraintLayout cl_push_status, constraintLayout5;
        ImageView iv_img;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            tv_time = itemView.findViewById(R.id.tv_time);
            tv_push_type = itemView.findViewById(R.id.tv_push_type);
            tv_score_count = itemView.findViewById(R.id.tv_score_count);
            tv_paper_count = itemView.findViewById(R.id.tv_paper_count);
            tv_media_count = itemView.findViewById(R.id.tv_media_count);
            cl_push_status = itemView.findViewById(R.id.cl_push_status);
            iv_img = itemView.findViewById(R.id.iv_img);
            constraintLayout5 = itemView.findViewById(R.id.constraintLayout5);
        }
    }

    public PushDataSummaryAdapter(Context context, List<Modal_Log> logs) {
        this.context = context;
        this.logs = logs;
    }

    @NonNull
    @Override
    public PushDataSummaryAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(context).inflate(R.layout.layout_push_summary_item, viewGroup, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PushDataSummaryAdapter.MyViewHolder myViewHolder, int i) {
        Modal_Log log = logs.get(i);
        myViewHolder.tv_time.setText("Date: " + log.getCurrentDateTime());
        if (log.getErrorType().equalsIgnoreCase("DB_PUSH")) {
            myViewHolder.tv_push_type.setText("DB push");
            myViewHolder.tv_score_count.setVisibility(View.GONE);
            myViewHolder.tv_paper_count.setVisibility(View.GONE);
            myViewHolder.tv_media_count.setVisibility(View.GONE);
        } else {
            myViewHolder.tv_push_type.setText(log.getMethodName());
            myViewHolder.tv_score_count.setVisibility(View.VISIBLE);
            myViewHolder.tv_paper_count.setVisibility(View.VISIBLE);
            myViewHolder.tv_media_count.setVisibility(View.VISIBLE);
        }
        if (log.getExceptionMessage() != null && log.getExceptionMessage().contains("successful")) {
            myViewHolder.cl_push_status.setBackground(context.getResources().getDrawable(R.drawable.gradiance_bg_green_light));
            myViewHolder.iv_img.setBackground(context.getResources().getDrawable(R.drawable.ic_check_circle_36dp));
        } else {
            myViewHolder.cl_push_status.setBackground(context.getResources().getDrawable(R.drawable.gradiance_bg_red_light));
            myViewHolder.iv_img.setBackground(context.getResources().getDrawable(R.drawable.ic_cancel_full_24dp));
        }
        try {
            JSONObject object = new JSONObject(log.getLogDetail());
            myViewHolder.tv_score_count.setText("Score: " + object.getString("score_pushed"));
            myViewHolder.tv_paper_count.setText("Paper: " + object.getString("paper_pushed"));
            myViewHolder.tv_media_count.setText("Media: " + object.getString("media_pushed"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return logs.size();
    }


}
