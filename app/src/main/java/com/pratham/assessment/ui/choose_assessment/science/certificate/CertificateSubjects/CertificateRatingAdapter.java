package com.pratham.assessment.ui.choose_assessment.science.certificate.CertificateSubjects;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;

import com.pratham.assessment.R;
import com.pratham.assessment.domain.CertificateModelClass;
import com.pratham.assessment.domain.CertificateRatingModalClass;
import com.pratham.assessment.domain.CertificateTopicList;
import com.pratham.assessment.utilities.Assessment_Utility;

import java.util.List;

public class CertificateRatingAdapter extends RecyclerView.Adapter<CertificateRatingAdapter.MyViewHolder> {
    private Context context;
    private List<CertificateRatingModalClass> questionList;

    public CertificateRatingAdapter(Context context, List<CertificateRatingModalClass> questionList) {
        this.context = context;
        this.questionList = questionList;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        TextView question;
        RatingBar ratingBar;
        LinearLayout ll_rating;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            question = itemView.findViewById(R.id.tv_question);
            ratingBar = itemView.findViewById(R.id.rb_ratingStars);
            ll_rating = itemView.findViewById(R.id.ll_rating);
        }
    }


    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.layout_certificate_rating_item, viewGroup, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder myViewHolder, int i) {
        CertificateRatingModalClass topicList = questionList.get(i);

        Assessment_Utility.setOdiaFont(context, myViewHolder.question);
        Assessment_Utility.setTamilFont(context, myViewHolder.question);
        myViewHolder.question.setText(Html.fromHtml(topicList.getCertificateQuestion()));
        myViewHolder.ratingBar.setRating(topicList.getRating());

    }

    @Override
    public int getItemCount() {
        return questionList.size();
    }


}
