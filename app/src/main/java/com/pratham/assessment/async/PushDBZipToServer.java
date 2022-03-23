package com.pratham.assessment.async;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.airbnb.lottie.LottieAnimationView;
import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.pratham.assessment.AssessmentApplication;
import com.pratham.assessment.R;
import com.pratham.assessment.constants.APIs;
import com.pratham.assessment.custom.FastSave;
import com.pratham.assessment.custom.custom_dialogs.PushDataDialog;
import com.pratham.assessment.database.AppDatabase;
import com.pratham.assessment.database.BackupDatabase;
import com.pratham.assessment.domain.Modal_Log;
import com.pratham.assessment.utilities.Assessment_Utility;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.UiThread;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static com.pratham.assessment.AssessmentApplication.isTablet;
import static com.pratham.assessment.constants.Assessment_Constants.PUSH_DATA_FROM_DRAWER;
import static com.pratham.assessment.utilities.Assessment_Utility.checkConnectedToRPI;

// >>>>>>> feature_branch

@EBean
public class PushDBZipToServer {
    Context context;
    boolean autoPush;
    boolean dataPushed = false;

    LottieAnimationView push_lottie;
    TextView txt_push_dialog_msg;
    TextView txt_push_cnt;
    RelativeLayout rl_btn;
    Button ok_btn;
    Modal_Log push_log;

    public PushDBZipToServer(Context context) {
        this.context = context;
    }

    public void setValue(Context context, boolean autoPush) {
        this.context = context;
        this.autoPush = autoPush;
    }

    @UiThread
    protected void onPreExecute() {

        PushDataDialog pushDialog = new PushDataDialog(context);
//            pushDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        pushDialog.setContentView(R.layout.app_push_data_dialog);
        Objects.requireNonNull(pushDialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        pushDialog.setCancelable(false);
        pushDialog.setCanceledOnTouchOutside(false);
        if (!autoPush)
            pushDialog.show();

        push_lottie = pushDialog.findViewById(R.id.push_lottie);
        txt_push_dialog_msg = pushDialog.findViewById(R.id.txt_push_dialog_msg);
        txt_push_cnt = pushDialog.findViewById(R.id.txt_push_cnt);
        rl_btn = pushDialog.findViewById(R.id.rl_btn);
        ok_btn = pushDialog.findViewById(R.id.ok_btn);
        ok_btn.setOnClickListener(view -> pushDialog.dismiss());
    }

    @Background
    public void doInBackground() {
        onPreExecute();
        push_log = new Modal_Log();
        push_log.setCurrentDateTime(Assessment_Utility.getCurrentDateTime());
        push_log.setDeviceId(Assessment_Utility.getDeviceId(context));
        push_log.setLogDetail("Apk version : " + Assessment_Utility.getCurrentVersion(context));
        push_log.setErrorType("DB_PUSH");

        if (Assessment_Utility.checkConnectedToRPI())
            pushZipToServer(context, APIs.UploadDBZipURLRPI);
        else
            pushZipToServer(context, APIs.pushDbZip);

    }

    public void pushZipToServer(Context context, String... url) {
        try {
//            String newdata = compress(String.valueOf(data));
            BackupDatabase.backup(context);
            File dir = new File(Assessment_Utility.getStoragePath() + "/PrathamBackups/");
            File[] db_files = dir.listFiles();


            if (db_files != null) {
                List<String> fileNameListStrings = new ArrayList<>();

                for (int i = 0; i < db_files.length; i++)
                    if (db_files[i].exists() && db_files[i].isFile() && db_files[i].getName().contains("assessment"))
                        fileNameListStrings.add(db_files[i].getAbsolutePath());

                String filePathStr = Assessment_Utility.getStoragePath()
                        + "/PrathamBackups/" + AppDatabase.DB_NAME; // file path to save

                String fileName = Assessment_Utility.getUUID() + "_" +
                        Assessment_Utility.getDeviceId(context) + "_" + FastSave.getInstance().getString("currentStudentID", "");

                zip(fileNameListStrings, filePathStr + ".zip", new File(filePathStr));

                String multipartKey = "";
                if (checkConnectedToRPI())
                    multipartKey = "uploaded_file";
                else multipartKey = fileName;



      /*      File dir = new File(Environment.getStoragePath().toString() + "/PrathamBackups/");
            File[] db_files = dir.listFiles();
            int num = 0;
           if (db_files != null) {
                for (int i = 0; i < db_files.length; i++)
                    if (db_files[i].exists() && db_files[i].isFile() && db_files[i].getName().contains("assessment"))
                        fileNameListStrings.add(db_files[i].getAbsolutePath());


                String filePathStr = Environment.getStoragePath().toString()
                        + "/PrathamBackups/" + AppDatabase.DB_NAME; // file path to save

                zip(s, filePathStr + ".zip");


                String fielName = "" + Assessment_Utility.getUUID();

 */
                AndroidNetworking.upload(url[0])
                        .addHeaders("Content-Type", "file/zip")
                        .addMultipartFile(multipartKey, new File(filePathStr + ".zip"))
                        .setPriority(Priority.HIGH)
                        .build()
                        .getAsJSONObject(new JSONObjectRequestListener() {
                            @Override
                            public void onResponse(JSONObject response) {
                                try {
//                                    if (response.getString("success").equalsIgnoreCase("true")) {
                                    if (new File(filePathStr + ".zip").exists())
                                        new File(filePathStr + ".zip").delete();

                                    dataPushed = true;
                                    onPostExecute();
//                                    }

                                } catch (Exception e) {
                                    e.printStackTrace();

                                }
                            }

                            @Override
                            public void onError(ANError anError) {
                                dataPushed = false;
                                if (new File(filePathStr + ".zip").exists())
                                    new File(filePathStr + ".zip").delete();
                                onPostExecute();
                            }
                        });
            }
                    /*.getAsString(new StringRequestListener() {
                        @Override
                        public void onResponse(String response) {
                            Log.d("PushData", "DB ZIP PUSH " + response);
                            Gson gson = new Gson();
                            FilePushResponse pushResponse = gson.fromJson(response, FilePushResponse.class);

                            new File(filePathStr + ".zip").delete();
                            if (pushResponse.isSuccess()*//*equalsIgnoreCase("success")*//*) {
                                Log.d("PushData", "DB ZIP PUSH SUCCESS");
                                pushSuccessfull = true;
                                setDataPushSuccessfull();
                            } else {
                                Log.d("PushData", "Failed DB ZIP PUSH");
                                pushSuccessfull = false;
                                setDataPushFailed();
                            }
                        }

                        @Override
                        public void onError(ANError anError) {
                            //Fail - Show dialog with failure message.
                            Log.d("PushData", "Data push FAIL");
                            Log.d("PushData", "ERROR  " + anError);
                            pushSuccessfull = false;
                            setDataPushFailed();
                        }
                    });*/
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void zip(List<String> _files, String zipFileName, File filepath) {
        try {
            int BUFFER = 10000;

            BufferedInputStream origin = null;
            FileOutputStream dest = new FileOutputStream(zipFileName);
            ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(dest));

            byte[] data = new byte[BUFFER];
            for (int i = 0; i < _files.size(); i++) {
                Log.v("Compress", "Adding: " + _files.get(i));
                FileInputStream fi = new FileInputStream(_files.get(i));
                origin = new BufferedInputStream(fi, BUFFER);
                ZipEntry entry = new ZipEntry(_files.get(i).substring(_files.get(i).lastIndexOf("/") + 1));
                out.putNextEntry(entry);
                int count;
                while ((count = origin.read(data, 0, BUFFER)) != -1) {
                    out.write(data, 0, count);
                }
                origin.close();
            }

            out.close();
//            new File(zipFileName).delete();
        } catch (Exception e) {
            e.printStackTrace();
            dataPushed = false;
            onPostExecute();

        }
    }


    public void zipOld(String[] _files, String zipFileName) {
        try {
            BufferedInputStream origin;
            FileOutputStream dest = new FileOutputStream(zipFileName);
            ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(dest));

            int BUFFER = 10000;
            byte[] data = new byte[BUFFER];
            for (String file : _files) {
                Log.v("Compress", "Adding: " + file);
                FileInputStream fi = new FileInputStream(file);
                origin = new BufferedInputStream(fi, BUFFER);
                ZipEntry entry = new ZipEntry(file.substring(file.lastIndexOf("/") + 1));
                out.putNextEntry(entry);
                int count;
                while ((count = origin.read(data, 0, BUFFER)) != -1) {
                    out.write(data, 0, count);
                }
                origin.close();
            }

            out.close();
//            new File(zipFileName).delete();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @UiThread
    protected void onPostExecute() {
        // super.onPostExecute(o);
        try {
            if (autoPush)
                push_log.setMethodName("auto push");
            else push_log.setMethodName("manual push");
            ok_btn.setVisibility(View.VISIBLE);
            if (!AssessmentApplication.wiseF.isDeviceConnectedToMobileOrWifiNetwork()) {
                txt_push_dialog_msg.setText(R.string.no_internet_connection);
                push_log.setExceptionMessage("push failed no internet");

            } else {
                if (!dataPushed) {
                    push_lottie.setAnimation("error_cross.json");
                    push_lottie.playAnimation();
                    txt_push_dialog_msg.setText(R.string.db_push_failed);
                    push_log.setExceptionMessage("push failed");

                } else if (isTablet || !autoPush) {
                    push_lottie.setAnimation("success.json");
                    push_lottie.playAnimation();
                    txt_push_dialog_msg.setText(R.string.db_pushed_successfully);
                    push_log.setExceptionMessage("push successful");

                }
            }
            PUSH_DATA_FROM_DRAWER = false;
            AppDatabase.getDatabaseInstance(context).getLogsDao().insertLog(push_log);
            BackupDatabase.backup(context);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


}
