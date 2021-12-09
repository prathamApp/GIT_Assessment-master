package com.pratham.assessment.ui.choose_assessment.science.certificate.CertificateSubjects.ExpandableRecyclerView;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.pratham.assessment.R;
import com.pratham.assessment.database.AppDatabase;
import com.pratham.assessment.domain.AssessmentPaperForPush;
import com.pratham.assessment.ui.choose_assessment.science.certificate.CertificateSubjects.CertificateFragment;
import com.pratham.assessment.ui.choose_assessment.science.certificate.CertificateSubjects.CertificateFragment_;
import com.pratham.assessment.ui.choose_assessment.science.certificate.CertificateSubjects.CertificatePDFFragment_;
import com.pratham.assessment.utilities.Assessment_Utility;

import java.io.File;
import java.util.List;

import static com.pratham.assessment.constants.Assessment_Constants.STORE_STUDENT_DIAGNOSTIC_PDF_PATH;

public class SubjectAdapter extends ExpandableRecyclerAdapter<SubjectViewHolder, SubjectAdapter.ExamViewHolder> {

    private Context mContext;
    private LayoutInflater mInflator;

    public SubjectAdapter(Context context, List<? extends ParentListItem> parentItemList) {
        super(parentItemList);
        mInflator = LayoutInflater.from(context);
        this.mContext = context;
    }


    @Override
    public SubjectViewHolder onCreateParentViewHolder(ViewGroup parentViewGroup) {
        View view = mInflator.inflate(R.layout.layout_subject_row, parentViewGroup, false);
        return new SubjectViewHolder(view);
    }

    @Override
    public ExamViewHolder onCreateChildViewHolder(ViewGroup childViewGroup) {
        View view = mInflator.inflate(R.layout.layout_exam_row, childViewGroup, false);
        return new ExamViewHolder(view);
    }

    @Override
    public void onBindParentViewHolder(SubjectViewHolder subjectViewHolder, int position, ParentListItem parentListItem) {
        AssessmentSubjectsExpandable assessmentSubjects = (AssessmentSubjectsExpandable) parentListItem;
        subjectViewHolder.bind(assessmentSubjects);
    }

    @Override
    public void onBindChildViewHolder(ExamViewHolder examViewHolder, int position, Object childListItem) {
        AssessmentPaperForPush paper = (AssessmentPaperForPush) childListItem;
        examViewHolder.bind(paper);
    }


    class ExamViewHolder extends ChildViewHolder {

        private TextView examName;
        private TextView timeStamp;
        private ImageView viewCertificate;

        ExamViewHolder(View itemView) {
            super(itemView);
            examName = itemView.findViewById(R.id.tv_exam);
            timeStamp = itemView.findViewById(R.id.tv_timestamp);
            viewCertificate = itemView.findViewById(R.id.ib_view_certificate);
        }

        public void bind(final AssessmentPaperForPush assessmentPaperForPush) {
            examName.setText(assessmentPaperForPush.getExamName());
            timeStamp.setText(assessmentPaperForPush.getPaperEndTime());
            boolean isDiagnosticTest = AppDatabase.getDatabaseInstance(mContext)
                    .getAssessmentPaperPatternDao().getIsDiagnosticExam(assessmentPaperForPush.getExamId());
            viewCertificate.setOnClickListener(v -> {
                if (isDiagnosticTest) {
                    String subjectName = AppDatabase.getDatabaseInstance(mContext).getAssessmentPaperPatternDao().getSubjectNameById(assessmentPaperForPush.getExamId());
                    String pdfPath = STORE_STUDENT_DIAGNOSTIC_PDF_PATH + "/" +
                            assessmentPaperForPush.getStudentId() + "_" + subjectName + "_" + assessmentPaperForPush.getPaperId() + ".pdf";
                    File pdf = new File(pdfPath);
                    if (pdf.exists()) {
                        Bundle bundle = new Bundle();
                        bundle.putString("pdfPath", pdfPath);
                        Assessment_Utility.showFragment((Activity) mContext, new CertificatePDFFragment_(), R.id.frame_certificate, bundle, CertificatePDFFragment_.class.getSimpleName());
                    } else {
                        Bundle bundle = new Bundle();
                        bundle.putSerializable("assessmentPaperForPush", assessmentPaperForPush);
                        Assessment_Utility.showFragment((Activity) mContext, new CertificateFragment_(), R.id.frame_certificate, bundle, CertificateFragment.class.getSimpleName());
                    }
                } else {
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("assessmentPaperForPush", assessmentPaperForPush);
                    Assessment_Utility.showFragment((Activity) mContext, new CertificateFragment_(), R.id.frame_certificate, bundle, CertificateFragment.class.getSimpleName());
                }
                });

            }

        }

    }