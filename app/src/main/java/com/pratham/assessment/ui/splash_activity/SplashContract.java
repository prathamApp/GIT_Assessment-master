package com.pratham.assessment.ui.splash_activity;


import com.pratham.assessment.database.AppDatabase;

/**
 * Created by Ankita on 23-Nov-17.
 */

public interface SplashContract {

    interface SplashView {
        void startApp();

        void showButton();

        void gotoNextActivity();

        void showProgressDialog();

        void dismissProgressDialog();
    }

    interface SplashPresenter {
        void checkVersion();

        void versionObtained(String latestVersion);



        void copyDataBase();

        void copySDCardDB();


    }

}
