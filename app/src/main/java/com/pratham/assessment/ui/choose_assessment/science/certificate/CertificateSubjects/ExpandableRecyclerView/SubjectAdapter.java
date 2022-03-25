package com.pratham.assessment.ui.choose_assessment.science.certificate.CertificateSubjects.ExpandableRecyclerView;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.pratham.assessment.R;
import com.pratham.assessment.database.AppDatabase;
import com.pratham.assessment.domain.AssessmentPaperForPush;
import com.pratham.assessment.domain.Student;
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
    private String appName;
    String schoolAppName = "PraDigi for School";

    public SubjectAdapter(Context context, List<? extends ParentListItem> parentItemList, String appName) {
        super(parentItemList);
        mInflator = LayoutInflater.from(context);
        this.mContext = context;
        this.appName = appName;
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

        private TextView examName, timeStamp, tv_studentName, tv_id;
        private ImageView viewCertificate;
        private LinearLayout ll_certificate_row;

        ExamViewHolder(View itemView) {
            super(itemView);
            examName = itemView.findViewById(R.id.tv_exam);
            timeStamp = itemView.findViewById(R.id.tv_timestamp);
//            viewCertificate = itemView.findViewById(R.id.ib_view_certificate);
            ll_certificate_row = itemView.findViewById(R.id.ll_certificate_row);
            tv_studentName = itemView.findViewById(R.id.tv_studentName);
            tv_id = itemView.findViewById(R.id.tv_id);
        }

        public void bind(final AssessmentPaperForPush assessmentPaperForPush) {
            if (appName.equalsIgnoreCase(schoolAppName)) {
                tv_studentName.setVisibility(View.VISIBLE);
                tv_id.setVisibility(View.VISIBLE);
                String studentId = assessmentPaperForPush.getStudentId();
                Student student = AppDatabase.getDatabaseInstance(mContext).getStudentDao().getStudent(studentId);
                if (student != null) {
                    if (student.getLastName() != null && !student.getLastName().equalsIgnoreCase(""))
                        tv_id.setText("Id : " + student.getLastName());
                    else
                        tv_id.setText("Id : " + studentId);

                    tv_studentName.setText(mContext.getResources().getString(R.string.student_name) + " " + student.getFullName());
                }
            } else {
                tv_studentName.setVisibility(View.GONE);
                tv_id.setVisibility(View.GONE);
            }
            examName.setText(mContext.getResources().getString(R.string.exam_name) + " " + assessmentPaperForPush.getExamName());
            timeStamp.setText(mContext.getResources().getString(R.string.time) + " " + assessmentPaperForPush.getPaperEndTime());
            boolean isDiagnosticTest = AppDatabase.getDatabaseInstance(mContext)
                    .getAssessmentPaperPatternDao().getIsDiagnosticExam(assessmentPaperForPush.getExamId());
            ll_certificate_row.setOnClickListener(v -> {
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