package com.pratham.assessment.database;

import android.content.Context;
import android.os.Environment;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;

import static com.pratham.assessment.utilities.Assessment_Utility.getStoragePath;

public class BackupDatabase {

    public static void backup(Context mContext) {

        try {
//            File sd = Environment.getStoragePath();
            deletePreviousDbs();
            File sd = new File(getStoragePath() + "/PrathamBackups");
            if (!sd.exists())
                sd.mkdirs();
            if (sd.canWrite()) {
                File currentDB = mContext.getDatabasePath(AppDatabase.DB_NAME);
                if (currentDB != null) {
                    File parentPath = currentDB.getParentFile();
                    for (File f : parentPath.listFiles()) {
                        File temp = new File(sd, f.getName());
                        if (!temp.exists()) temp.createNewFile();
                        FileChannel src = new FileInputStream(f).getChannel();
                        FileChannel dst = new FileOutputStream(temp).getChannel();
                        dst.transferFrom(src, 0, src.size());
                        src.close();
                        dst.close();
                    }
                }
            } /*else {
                //EventBus.getDefault().post(PermissionUtils.WRITE_PERMISSION);
            }*/
//            DeleteSensitiveTablesFromBackupDB.deleteTables();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*try {
        File sd = Environment.getExternalStorageDirectory();

        if (sd.canWrite()) {
            File file = mContext.getDir("databases", Context.MODE_PRIVATE);

            String currentDBPath = file.getAbsolutePath().replace("app_databases","databases")+"/"+ DB_NAME;
            String backupDBPath = DB_NAME+".db";
            File currentDB = new File(currentDBPath);
            File backupDB = new File(sd, backupDBPath);

            if (currentDB.exists()) {
                FileChannel src = new FileInputStream(currentDB).getChannel();
                FileChannel dst = new FileOutputStream(backupDB).getChannel();
                dst.transferFrom(src, 0, src.size());
                src.close();
                dst.close();
            }
        }
    } catch (Exception e) {
        e.printStackTrace();
    }
}*/
    private static void deletePreviousDbs() {
        File sd = getStoragePath();
        for (File f : sd.listFiles()) {
            if (f.getName().contains(AppDatabase.DB_NAME))
                f.delete();
        }
    }

}
