package com.pratham.assessment.ui.choose_assessment.science.certificate.CertificateSubjects;import android.app.ProgressDialog;import android.content.Intent;import android.graphics.Bitmap;import android.graphics.pdf.PdfDocument;import android.net.Uri;import android.os.Bundle;import android.os.Handler;import android.support.constraint.ConstraintLayout;import android.support.v4.app.Fragment;import android.support.v4.content.FileProvider;import android.support.v7.widget.LinearLayoutManager;import android.support.v7.widget.RecyclerView;import android.text.Html;import android.util.Log;import android.view.View;import android.widget.RelativeLayout;import android.widget.ScrollView;import android.widget.TextView;import android.widget.Toast;import com.androidnetworking.AndroidNetworking;import com.androidnetworking.error.ANError;import com.androidnetworking.interfaces.StringRequestListener;import com.google.gson.Gson;import com.pratham.assessment.AssessmentApplication;import com.pratham.assessment.R;import com.pratham.assessment.constants.APIs;import com.pratham.assessment.constants.Assessment_Constants;import com.pratham.assessment.custom.FastSave;import com.pratham.assessment.custom.customFont.SansTextView;import com.pratham.assessment.custom.screenshot.BigScreenshot;import com.pratham.assessment.database.AppDatabase;import com.pratham.assessment.domain.AssessmentPaperForPush;import com.pratham.assessment.domain.AssessmentPaperPattern;import com.pratham.assessment.domain.CertificateKeywordRating;import com.pratham.assessment.domain.SupervisorData;import com.pratham.assessment.utilities.Assessment_Utility;import org.androidannotations.annotations.AfterViews;import org.androidannotations.annotations.Bean;import org.androidannotations.annotations.Click;import org.androidannotations.annotations.EFragment;import org.androidannotations.annotations.ViewById;import java.io.File;import java.io.FileInputStream;import java.io.FileOutputStream;import java.io.IOException;import java.nio.channels.FileChannel;import java.text.SimpleDateFormat;import java.util.Date;import java.util.List;import static com.pratham.assessment.constants.Assessment_Constants.STORE_STUDENT_DIAGNOSTIC_PDF_PATH;import static com.pratham.assessment.constants.Assessment_Constants.SUPERVISED;//import com.ezydev.bigscreenshot.BigScreenshot;@EFragment(R.layout.fragment_certificate)public class CertificateFragment extends Fragment implements CertificateContract.CertificateView, BigScreenshot.ProcessScreenshot {    @ViewById(R.id.tv_name)    TextView tv_studName;    @ViewById(R.id.tv_name_details)    TextView tv_name_details;    @ViewById(R.id.tv_correct_cnt)    TextView tv_correct_cnt;    @ViewById(R.id.tv_wrong_cnt)    TextView tv_wrong_cnt;    @ViewById(R.id.tv_skip_cnt)    TextView tv_skip_cnt;    @ViewById(R.id.tv_total_time)    TextView tv_total_time;    @ViewById(R.id.tv_time_taken)    TextView tv_time_taken;    @ViewById(R.id.tv_total_marks)    TextView tv_total_marks;    @ViewById(R.id.tv_student_marks)    TextView tv_student_marks;    @ViewById(R.id.tv_total_cnt)    TextView tv_total_cnt;    @ViewById(R.id.tv_recommended_level)    TextView tv_recommended_level;    @ViewById(R.id.tv_yes_no)    TextView tv_yes_no;    @ViewById(R.id.rv_diagnostic)    RecyclerView rv_diagnostic;    @ViewById(R.id.sv_diagnostic_certificate)    ScrollView sv_diagnostic_certificate;    @ViewById(R.id.cl_diagnostic)    ConstraintLayout cl_diagnostic;    @ViewById(R.id.cl_diagnostic_root)    ConstraintLayout cl_diagnostic_root;    @ViewById(R.id.tv_studentName)    TextView tv_studentName;    @ViewById(R.id.tv_studentEnrollment)    TextView tv_studentEnrollment;    @ViewById(R.id.tv_exam_name)    TextView tv_exam_name;    @ViewById(R.id.tv_exam_date)    TextView tv_exam_date;    @ViewById(R.id.tv_supervisor_name)    TextView tv_supervisor_name;    @ViewById(R.id.tv_supervisor_id)    TextView tv_supervisor_id;    /* @ViewById(R.id.frame_fragment_certificate)     FrameLayout frame_fragment_certificate;*//*    @ViewById(R.id.rl_cer)    RelativeLayout card_certificate;*/    @ViewById(R.id.rl_cer)    RelativeLayout rl_cer;    @ViewById(R.id.sv_star_certificate)    ScrollView sv_star_certificate;    @ViewById(R.id.tv_pdf)    SansTextView tv_pdf;    @ViewById(R.id.tv_share_pdf)    SansTextView tv_share_pdf;    boolean pdfPressed = false;    BigScreenshot longScreenshot;    String certificatePDFPath = "";    @Bean(CertificatePresenterImpl.class)    CertificateContract.CertificatePresenter presenter;    AssessmentPaperForPush assessmentPaperForPush;    @ViewById(R.id.rv_rating)    RecyclerView rv_rating;    boolean isDiagnosticTest;    List<CertificateKeywordRating> certificateKeywordRatingList;    public CertificateFragment() {    }    @AfterViews    public void init() {        if (getArguments() != null) {            Bundle bundle = getArguments();            assessmentPaperForPush = (AssessmentPaperForPush) bundle.getSerializable("assessmentPaperForPush");        }        presenter.setView(this);        presenter.getIsDiagnosticTest(assessmentPaperForPush.getExamId());    }    @Click(R.id.tv_share_pdf)    public void shareClicked() {        File myPDFFile = new File(certificatePDFPath);        if (myPDFFile.exists()) {            //use file provider or higher versions            //            Uri uri = Uri.fromFile(myPDFFile);            Uri uri = FileProvider.getUriForFile(getActivity(),                    getActivity().getApplicationContext().getPackageName() + ".provider", myPDFFile);            Intent share = new Intent();            share.setAction(Intent.ACTION_SEND);            share.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);            share.setType("application/pdf");            share.putExtra(Intent.EXTRA_STREAM, uri);            startActivity(Intent.createChooser(share, "Share app via"));        }    }    private void generateCertificate() {        presenter.getStudentName();        AssessmentPaperPattern assessmentPaperPattern;        if (assessmentPaperForPush != null) {            assessmentPaperPattern = AppDatabase.getDatabaseInstance(getActivity())                    .getAssessmentPaperPatternDao().getAllAssessmentPaperPatternsBySubIdAndExamId(assessmentPaperForPush.getSubjectId(), assessmentPaperForPush.getExamId());            if (assessmentPaperPattern != null) {                if (assessmentPaperPattern.isDiagnosticTest()) {                    if (!assessmentPaperPattern.getExammode().equalsIgnoreCase(SUPERVISED))                        getActivity().finish();                    else                        createQuestionList(assessmentPaperPattern);                } else createQuestionList(assessmentPaperPattern);            } else {                if (AssessmentApplication.wiseF.isDeviceConnectedToMobileOrWifiNetwork())                    downloadPaperPattern(assessmentPaperForPush.getExamId());                else {                }            }            tv_correct_cnt.setText("" + assessmentPaperForPush.getCorrectCnt());            int totalWrong = assessmentPaperForPush.getWrongCnt() + assessmentPaperForPush.getSkipCnt();            tv_wrong_cnt.setText("" + totalWrong);            tv_skip_cnt.setText("" + assessmentPaperForPush.getSkipCnt());            tv_total_time.setText(assessmentPaperForPush.getExamTime() + " mins.");            calculateTime(assessmentPaperForPush.getPaperStartTime(), assessmentPaperForPush.getPaperEndTime());            tv_student_marks.setText(assessmentPaperForPush.getTotalMarks());            tv_total_marks.setText(assessmentPaperForPush.getOutOfMarks());            int totalCnt = assessmentPaperForPush.getCorrectCnt() + assessmentPaperForPush.getWrongCnt() + assessmentPaperForPush.getSkipCnt();            tv_total_cnt.setText(totalCnt + "");        } else {            getActivity().finish();        }//        presenter.getPaper(assessmentPaperForPush.getExamId(), assessmentPaperForPush.getSubjectId());    }    private void createQuestionList(AssessmentPaperPattern assessmentPaperPattern) {        String paperId = assessmentPaperForPush.getPaperId();        String examId = assessmentPaperForPush.getExamId();        String subId = assessmentPaperForPush.getSubjectId();        String studId = assessmentPaperForPush.getStudentId();//        List<CertificateRatingModalClass> questionList = new ArrayList<>();        certificateKeywordRatingList = AppDatabase.getDatabaseInstance(getActivity())                .getCertificateKeywordRatingDao().getQuestionsByExamIdSubIdPaperIdStudId(examId, subId, paperId, studId);        SharePDFClicked();  /*      setValuesInList(questionList, assessmentPaperPattern.getCertificateQuestion1(), assessmentPaperForPush.getQuestion1Rating(), "1", paperId);        setValuesInList(questionList, assessmentPaperPattern.getCertificateQuestion2(), assessmentPaperForPush.getQuestion2Rating(), "2", paperId);        setValuesInList(questionList, assessmentPaperPattern.getCertificateQuestion3(), assessmentPaperForPush.getQuestion3Rating(), "3", paperId);        setValuesInList(questionList, assessmentPaperPattern.getCertificateQuestion4(), assessmentPaperForPush.getQuestion4Rating(), "4", paperId);        setValuesInList(questionList, assessmentPaperPattern.getCertificateQuestion5(), assessmentPaperForPush.getQuestion5Rating(), "5", paperId);        setValuesInList(questionList, assessmentPaperPattern.getCertificateQuestion6(), assessmentPaperForPush.getQuestion6Rating(), "6", paperId);        setValuesInList(questionList, assessmentPaperPattern.getCertificateQuestion7(), assessmentPaperForPush.getQuestion7Rating(), "7", paperId);        setValuesInList(questionList, assessmentPaperPattern.getCertificateQuestion8(), assessmentPaperForPush.getQuestion8Rating(), "8", paperId);        setValuesInList(questionList, assessmentPaperPattern.getCertificateQuestion9(), assessmentPaperForPush.getQuestion9Rating(), "9", paperId);        setValuesInList(questionList, assessmentPaperPattern.getCertificateQuestion10(), assessmentPaperForPush.getQuestion10Rating(), "10", paperId);*///        CertificateRatingAdapter adapter = new CertificateRatingAdapter(getActivity(), questionList);        if (!isDiagnosticTest) {            CertificateKeywordRatingAdapter adapter = new CertificateKeywordRatingAdapter(getActivity(), certificateKeywordRatingList);            RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);            rv_rating.setLayoutManager(mLayoutManager);            rv_rating.setAdapter(adapter);        } else {            DiagnosticCertificateAdapter adapter = new DiagnosticCertificateAdapter(getActivity(), certificateKeywordRatingList);            RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);            rv_diagnostic.setLayoutManager(mLayoutManager);            rv_diagnostic.setAdapter(adapter);        }    }    /* private void setValuesInList(List<CertificateRatingModalClass> questionList, String question, String questionRating, String level, String paperId) {         float rating;         if (question != null) {             if (!question.equalsIgnoreCase("")) {                 CertificateRatingModalClass ratingModalClass = new CertificateRatingModalClass();                 ratingModalClass.setCertificateQuestion(question + "");                 if (questionRating != null                         && !questionRating.trim().equalsIgnoreCase("") && !questionRating.trim().equalsIgnoreCase("null")) {                     rating = Float.parseFloat(questionRating);                 } else {                     rating = presenter.getRating(level, paperId);                 }                 ratingModalClass.setRating(rating);                 questionList.add(ratingModalClass);             }         }     } */    private void downloadPaperPattern(String examId) {        final ProgressDialog progressDialog = new ProgressDialog(getActivity());        progressDialog.show();        progressDialog.setMessage(getString(R.string.downloading_paper_pattern));        progressDialog.setCancelable(false);        progressDialog.setCanceledOnTouchOutside(false);//        progressDialog.show();        AndroidNetworking.get(APIs.AssessmentPaperPatternAPI + examId)                .build()                .getAsString(new StringRequestListener() {                    @Override                    public void onResponse(String response) {                        Gson gson = new Gson();                        AssessmentPaperPattern assessmentPaperPattern = gson.fromJson(response, AssessmentPaperPattern.class);                        if (assessmentPaperPattern != null) {                            AppDatabase.getDatabaseInstance(getActivity()).getAssessmentPaperPatternDao().insertPaperPattern(assessmentPaperPattern);                            Log.d(";;;;", assessmentPaperPattern.getCertificateQuestion1() + "");                            if (assessmentPaperPattern.getExammode().equalsIgnoreCase(SUPERVISED))                                if (assessmentPaperPattern.isDiagnosticTest())                                    getActivity().finish();                                else                                    createQuestionList(assessmentPaperPattern);                        }                        progressDialog.dismiss();                    }                    @Override                    public void onError(ANError anError) {                        progressDialog.dismiss();                        Toast.makeText(getActivity(), "Error downloading paper pattern..", Toast.LENGTH_SHORT).show();                    }                });    }    @Override    public void setStudentName(String name) {        String timeStamp;        String time = "";        String dateStamp = Assessment_Utility.getCurrentDateTime();        if (assessmentPaperForPush != null) {            if (assessmentPaperForPush.getPaperEndTime() != null) {                timeStamp = assessmentPaperForPush.getPaperEndTime();                dateStamp = timeStamp.split(" ")[0];                time = timeStamp.split(" ")[1];            }            if (!isDiagnosticTest) {                String nameNo;                if (FastSave.getInstance().getBoolean("enrollmentNoLogin", false))                    nameNo = name + "( " + assessmentPaperForPush.getStudentId() + " )";                else nameNo = name;                tv_studName.setText(Html.fromHtml("<b><i><font color=\"#07992F\">"                        + nameNo + "</font></i></b> has completed<br><i><b>" + assessmentPaperForPush.getExamName() + "</b></i> successfully"));                tv_name_details.setText(Html.fromHtml("on " + dateStamp + " using<br>'" + getResources().getString(R.string.app_name) + "' app<br>"                        + "by <i>Pratham Education Foundation.</i>"));            } else {                SupervisorData supervisor = AppDatabase.getDatabaseInstance(getActivity()).getSupervisorDataDao().getSupervisorByPhoto(assessmentPaperForPush.getPaperId() + "%");                tv_studentEnrollment.setText(getResources().getString(R.string.enrollment_no) + " " + assessmentPaperForPush.getStudentId());                tv_studentName.setText(getResources().getString(R.string.student_name) + " " + name);                tv_exam_name.setText(getResources().getString(R.string.exam_name) + " " + assessmentPaperForPush.getExamName());                tv_exam_date.setText(getResources().getString(R.string.date) + " " + dateStamp + " " + time);                if (supervisor != null) {                    tv_supervisor_name.setVisibility(View.VISIBLE);                    tv_supervisor_id.setVisibility(View.VISIBLE);                    tv_supervisor_name.setText(getResources().getString(R.string.supervisor_name) + " " + supervisor.getSupervisorName());                    tv_supervisor_id.setText("Supervisor id:" + " " + supervisor.getSupervisorId());                }            }        }    }    @Override    public void setIsDiagnosticTest(boolean isDiagnosticTest) {        this.isDiagnosticTest = isDiagnosticTest;        if (this.isDiagnosticTest) {            sv_diagnostic_certificate.setVisibility(View.VISIBLE);            sv_star_certificate.setVisibility(View.GONE);            presenter.getRecommendedLevel(assessmentPaperForPush.getPaperId());        } else {            sv_diagnostic_certificate.setVisibility(View.GONE);            sv_star_certificate.setVisibility(View.VISIBLE);            sv_star_certificate.setBackground(getResources().getDrawable(Assessment_Utility.getRandomCertificateBackground(getActivity())));        }        generateCertificate();    }    @Override    public void setRecommendedLevel(int level) {        AppDatabase.getDatabaseInstance(getActivity())                .getAssessmentPaperForPushDao().setRecommendedLevel(assessmentPaperForPush.getPaperId(), level);        AppDatabase.getDatabaseInstance(getActivity())                .getAssessmentPaperForPushDao().setDiagnostic(assessmentPaperForPush.getPaperId(), true);        tv_recommended_level.setText(getResources().getString(R.string.recommended_level) + " " + level);    }    private void calculateTime(String paperStartTime, String paperEndTime) {        try {            Date date1 = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").parse(paperStartTime);            Date date2 = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").parse(paperEndTime);            long diff = date2.getTime() - date1.getTime();            long diffSeconds = diff / 1000 % 60;            long diffMinutes = diff / (60 * 1000) % 60;            long diffHours = diff / (60 * 60 * 1000);            String unit = "min.";            String time = "";            if (diffHours == 0 && diffMinutes == 0 && diffSeconds != 0) {                unit = " secs.";                time = "00 : 00 : " + diffSeconds + "";            } else if (diffHours == 0 && diffMinutes != 0) {                unit = " mins.";                time = "00 : " + diffMinutes + " : " + diffSeconds;            } else if (diffHours != 0) {                unit = " hrs.";                time = diffHours + " : " + diffMinutes + "  : " + diffSeconds;            }            tv_time_taken.setText(time + unit);        } catch (Exception e) {            e.printStackTrace();        }    }    @Click(R.id.tv_pdf)    public void SharePDFClicked() {        ProgressDialog dialog = new ProgressDialog(getActivity());        dialog.setMessage("loading");        dialog.setCancelable(false);        dialog.setCanceledOnTouchOutside(false);        if (!pdfPressed) {//        showLoader();            pdfPressed = true;/*            certificatePDFPath = Environment.getExternalStorageDirectory().toString() + "/.FCAInternal/StudentPDFs/"                    + FastSave.getInstance().getString(CURRENT_STUDENT_ID, "") + "/";*/            if (!isDiagnosticTest)                certificatePDFPath = AssessmentApplication.assessPath + Assessment_Constants.STORE_STUDENT_PDF_PATH + "/";            else {                certificatePDFPath = STORE_STUDENT_DIAGNOSTIC_PDF_PATH + "/";            }            if (!new File(certificatePDFPath).exists())                new File(certificatePDFPath).mkdir();            tv_pdf.setText("STOP");            dialog.show();            // Main container which screenshot is to be taken - main_certi_layout            // recyclerView for scrolling screenshot.            if (isDiagnosticTest)                longScreenshot = new BigScreenshot(this, rv_diagnostic, cl_diagnostic_root);            else longScreenshot = new BigScreenshot(this, rv_rating, rl_cer);            longScreenshot.startScreenshot();            new Handler().postDelayed(() -> {                dialog.dismiss();                tv_pdf.performClick();            }, 700);        } else {            pdfPressed = false;            longScreenshot.stopScreenshot();            tv_pdf.setText("PDF");        }    }    public static boolean copyFile(File sourceFile, File destFile) throws IOException {        if (!destFile.exists()) {            destFile.createNewFile();        }        FileChannel source = null;        FileChannel destination = null;        try {            source = new FileInputStream(sourceFile).getChannel();            destination = new FileOutputStream(destFile).getChannel();            // previous code: destination.transferFrom(source, 0, source.size());            // to avoid infinite loops, should be:            long count = 0;            long size = source.size();            while ((count += destination.transferFrom(source, count, size - count)) < size) ;        } finally {            if (source != null) {                source.close();            }            if (destination != null) {                destination.close();            }        }        return sourceFile.delete();    }    @Override    public void getScreenshot(Bitmap bitmap) {        Log.d("SharePDF", "getScreenshot: ");        try {            PdfDocument pdfDocument = new PdfDocument();            PdfDocument.PageInfo myPageInfo = new PdfDocument.PageInfo.Builder(bitmap.getWidth(), bitmap.getHeight(), 1).create();            PdfDocument.Page page = pdfDocument.startPage(myPageInfo);            page.getCanvas().drawBitmap(bitmap, 0, 0, null);            pdfDocument.finishPage(page);            String pdfName;            if (!isDiagnosticTest)                pdfName = FastSave.getInstance().getString("currentStudentID", "") + "_" + assessmentPaperForPush.getPaperId() + ".pdf";            else {                String subjectName = AppDatabase.getDatabaseInstance(getActivity()).getAssessmentPaperPatternDao().getSubjectNameById(assessmentPaperForPush.getExamId());                pdfName = FastSave.getInstance().getString("currentStudentID", "")                        + "_" + subjectName + "_" + assessmentPaperForPush.getPaperId() + ".pdf";                File folder = new File(STORE_STUDENT_DIAGNOSTIC_PDF_PATH);                for (final File fileEntry : folder.listFiles()) {                    if (fileEntry.getName().contains(subjectName)                            && fileEntry.getName().contains(FastSave.getInstance()                            .getString("currentStudentID", ""))) {//                        File old = new File(STORE_STUDENT_DIAGNOSTIC_PDF_OLD_PATH);                        File old = new File(AssessmentApplication.assessPath + Assessment_Constants.STORE_STUDENT_PDF_PATH);                        if (!old.exists()) old.mkdir();                        File newDest = new File(old + "/" + fileEntry.getName());                        boolean isDeleted = copyFile(fileEntry, newDest);                        Log.d("getScreenshot", "getScreenshot: " + isDeleted);                    }                }            }            certificatePDFPath = certificatePDFPath + "" + pdfName;            File myPDFFile = new File(certificatePDFPath);            try {                pdfDocument.writeTo(new FileOutputStream(myPDFFile));            } catch (IOException e) {                e.printStackTrace();            }            pdfDocument.close();           /* if (myPDFFile.exists()) {                //use file provider or higher versions                //            Uri uri = Uri.fromFile(myPDFFile);                Uri uri = FileProvider.getUriForFile(getActivity(),                        getActivity().getApplicationContext().getPackageName() + ".provider", myPDFFile);                Intent share = new Intent();                share.setAction(Intent.ACTION_SEND);                share.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);                share.setType("application/pdf");                share.putExtra(Intent.EXTRA_STREAM, uri);                startActivity(Intent.createChooser(share, "Share app via"));            }*/        } catch (Exception e) {            e.printStackTrace();        }    }    /*public void takeScreenshot() {        View u = ((Activity) getActivity()).findViewById(R.id.sv_diagnostic_certificate);        ScrollView z = (ScrollView) ((Activity) getActivity()).findViewById(R.id.sv_diagnostic_certificate);        int totalHeight = z.getChildAt(0).getHeight();        int totalWidth = z.getChildAt(0).getWidth();        Bitmap b = getBitmapFromView(u, totalHeight, totalWidth);        //Save bitmap        String extr = STORE_STUDENT_DIAGNOSTIC_PDF_PATH + "/";        String fileName = "report.jpg";        File myPath = new File(extr, fileName);        FileOutputStream fos = null;        try {            fos = new FileOutputStream(myPath);            b.compress(Bitmap.CompressFormat.JPEG, 100, fos);            fos.flush();            fos.close();            MediaStore.Images.Media.insertImage(getActivity().getContentResolver(), b, "Screen", "screen");        } catch (FileNotFoundException e) {            // TODO Auto-generated catch block            e.printStackTrace();        } catch (Exception e) {            // TODO Auto-generated catch block            e.printStackTrace();        }    }    public Bitmap getBitmapFromView(View view, int totalHeight, int totalWidth) {        Bitmap returnedBitmap = Bitmap.createBitmap(totalWidth, totalHeight, Bitmap.Config.ARGB_8888);        Canvas canvas = new Canvas(returnedBitmap);        Drawable bgDrawable = view.getBackground();        if (bgDrawable != null)            bgDrawable.draw(canvas);        else            canvas.drawColor(Color.WHITE);        view.draw(canvas);        return returnedBitmap;    }    public Bitmap getScreenBitmap() {       *//* View v = sv_diagnostic_certificate;        v.setDrawingCacheEnabled(true);        v.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));        v.layout(0, 0, v.getMeasuredWidth(), v.getMeasuredHeight());        v.buildDrawingCache(true);        Bitmap b = Bitmap.createBitmap(v.getDrawingCache());        v.setDrawingCacheEnabled(false); // clear drawing cache        return b;*//*        int height = sv_diagnostic_certificate.getMeasuredHeight();        int width = sv_diagnostic_certificate.getMeasuredWidth();        Bitmap b = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);        Canvas c = new Canvas(b);        sv_diagnostic_certificate.draw(c);        return b;    }*/}