package com.pratham.assessment.ui.choose_assessment.science.certificate.CertificateSubjects;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.pratham.assessment.R;
import com.pratham.assessment.domain.CertificateKeywordRating;
import com.pratham.assessment.utilities.Assessment_Utility;

import java.util.List;

public class DiagnosticCertificateAdapter extends RecyclerView.Adapter<DiagnosticCertificateAdapter.MyViewHolder> {
    private Context context;
    private List<CertificateKeywordRating> certificateKeywordRatingList;

    public DiagnosticCertificateAdapter(Context context, List<CertificateKeywordRating> certificateKeywordRatingList) {
        this.context = context;
        this.certificateKeywordRatingList = certificateKeywordRatingList;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        TextView tv_level, tv_competency_question, tv_yes_no;
        LinearLayout ll_diagnostic_item;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            tv_level = itemView.findViewById(R.id.tv_level);
            tv_competency_question = itemView.findViewById(R.id.tv_competency_question);
            tv_yes_no = itemView.findViewById(R.id.tv_yes_no);
            ll_diagnostic_item = itemView.findViewById(R.id.ll_diagnostic_item);
        }
    }


    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.layout_diagnostic_certificate_item, viewGroup, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder myViewHolder, int i) {
        CertificateKeywordRating keywordRating = certificateKeywordRatingList.get(i);

        Assessment_Utility.setOdiaFont(context, myViewHolder.tv_level);
        Assessment_Utility.setTamilFont(context, myViewHolder.tv_level);
        Assessment_Utility.setOdiaFont(context, myViewHolder.tv_competency_question);
        Assessment_Utility.setTamilFont(context, myViewHolder.tv_competency_question);
        Assessment_Utility.setOdiaFont(context, myViewHolder.tv_yes_no);
        Assessment_Utility.setTamilFont(context, myViewHolder.tv_yes_no);
        myViewHolder.tv_level.setText(keywordRating.getQuestionLevel() + "");
        myViewHolder.tv_competency_question.setText(keywordRating.getCertificatequestion());
        if (keywordRating.isCorrect())
            myViewHolder.tv_yes_no.setText(R.string.yes);
        else myViewHolder.tv_yes_no.setText(R.string.no);

        if (keywordRating.isCorrect())
            myViewHolder.ll_diagnostic_item.setBackgroundColor(context.getResources().getColor(R.color.colorGreenLight));
        else
            myViewHolder.ll_diagnostic_item.setBackgroundColor(context.getResources().getColor(R.color.colorRedLightnew));

    }

    @Override
    public int getItemCount() {
        return certificateKeywordRatingList.size();
    }


}
