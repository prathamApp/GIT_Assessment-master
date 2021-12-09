package com.pratham.assessment.ui.choose_assessment.exam_status;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.pratham.assessment.R;
import com.pratham.assessment.domain.AssessmentPaperForPush;

import java.util.List;

public class ExamStatusAdapter extends RecyclerView.Adapter<ExamStatusAdapter.MyViewHolder> {
    Context context;
    List<AssessmentPaperForPush> paperForPushList;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        TextView tv_time, tv_exam_name, tv_marks;
        ConstraintLayout cl_push_status;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            tv_time = itemView.findViewById(R.id.tv_time);
            tv_exam_name = itemView.findViewById(R.id.tv_exam_name);
            tv_marks = itemView.findViewById(R.id.tv_marks);
            cl_push_status = itemView.findViewById(R.id.cl_push_status);

        }
    }

    public ExamStatusAdapter(Context context, List<AssessmentPaperForPush> paper) {
        this.context = context;
        this.paperForPushList = paper;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(context).inflate(R.layout.layout_exam_status_item, viewGroup, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder myViewHolder, int i) {
        AssessmentPaperForPush paper = paperForPushList.get(i);
        String date = paper.getPaperStartTime().split(" ")[0];
        String time = paper.getPaperStartTime().split(" ")[1];

        myViewHolder.tv_time.setText(Html.fromHtml(date + "<br/>" + time));
        myViewHolder.tv_exam_name.setText(paper.getExamName());
        myViewHolder.tv_marks.setText(paper.getTotalMarks() + "/" + paper.getOutOfMarks());
        if (i % 2 == 0)
            myViewHolder.cl_push_status.setBackgroundColor(context.getResources().getColor(R.color.light_green));
        else
            myViewHolder.cl_push_status.setBackgroundColor(context.getResources().getColor(R.color.colorGreenLight));

    }

    @Override
    public int getItemCount() {
        return paperForPushList.size();
    }


}
