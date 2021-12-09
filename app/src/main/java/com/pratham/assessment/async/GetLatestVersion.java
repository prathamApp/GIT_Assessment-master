package com.pratham.assessment.async;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.pratham.assessment.R;
import com.pratham.assessment.ui.choose_assessment.choose_subject.ChooseAssessmentContract;
import com.pratham.assessment.ui.splash_activity.SplashContract;

import org.jsoup.Jsoup;


public class GetLatestVersion extends AsyncTask<String, String, String> {

    String latestVersion;
    ChooseAssessmentContract.ChooseAssessmentPresenter chooseAssessmentPresenter;
    SplashContract.SplashPresenter splashPresenter;
    ProgressDialog dialog;
    Context context;

    public GetLatestVersion(ChooseAssessmentContract.ChooseAssessmentPresenter chooseAssessmentPresenter, Context context) {
        this.chooseAssessmentPresenter = chooseAssessmentPresenter;
        this.context = context;
    }

    public GetLatestVersion(SplashContract.SplashPresenter splashPresenter, Context context) {
        this.splashPresenter = splashPresenter;
        this.context = context;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        dialog = new ProgressDialog(context);
        dialog.setMessage(context.getResources().getString(R.string.loading_please_wait));
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
//        COS_Utility.showDialogInApiCalling(dialog, SplashActivity.this, "Checking if new version is available!");
    }


    @Override
    protected String doInBackground(String... params) {
        try {
            latestVersion = Jsoup.connect("https://play.google.com/store/apps/details?id=com.pratham.assessment&hl=en")
                    .timeout(30000)
                    .userAgent("Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6")
                    .referrer("http://www.google.com")
                    .get()
                    .select("div.hAyfc:nth-child(4) > span:nth-child(2) > div:nth-child(1) > span:nth-child(1)")
                    .first()
                    .ownText();
            Log.d("latest::", latestVersion);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return latestVersion;
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        dialog.dismiss();
        if (chooseAssessmentPresenter != null)
            chooseAssessmentPresenter.versionObtained(latestVersion);
        if (splashPresenter != null)
            splashPresenter.versionObtained(latestVersion);
    }
}
