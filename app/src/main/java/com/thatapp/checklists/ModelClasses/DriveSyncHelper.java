package com.thatapp.checklists.ModelClasses;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.support.v4.util.Pair;
import android.util.Log;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.api.client.http.ByteArrayContent;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import static com.thatapp.checklists.ModelClasses.DriveServiceHelper.allDriveFiles;
import static com.thatapp.checklists.ModelClasses.DriveServiceHelper.allLocalFiles;

/**
 * A utility for performing read/write operations on Drive files via the REST API and Syncing Files between Device and Drive Folder
 */
public class DriveSyncHelper {
    private final Executor mExecutor = Executors.newSingleThreadExecutor();
    private Drive mDriveService;

    private Context context;
    ServiceListener serviceListener;
    PrefManager prefManager;

    public DriveSyncHelper(Drive driveService, Context context) {
        this.mDriveService = driveService;
        this.context = context;
        prefManager = new PrefManager(context);
    }

    private void initFolders() {

        java.io.File storageDir = context.getFilesDir();
        prefManager = new PrefManager(context);
        java.io.File fileD = new java.io.File(storageDir.getAbsolutePath() + java.io.File.separator + "downloads");
        Boolean d = fileD.mkdirs();
        java.io.File fileR = new java.io.File(storageDir.getAbsolutePath() + java.io.File.separator + "generated" + java.io.File.separator + prefManager.getDirName());
        Boolean g = fileR.mkdirs();
    }

    public void initDrive() throws IOException {

        FileList result = mDriveService.files().list().setSpaces("Drive")
                .setQ("mimeType='application/vnd.google-apps.folder' and trashed=false and sharedWithMe=true")
                .execute();

        for (File file : result.getFiles()) {

            if (file.getName().equalsIgnoreCase("CheckList App")) {
                prefManager.setRootFolderID(file.getId());
            }
        }


//        Log.e("folder id found", prefManager.getRootFolderID());

        FileList resultF = mDriveService.files().list().setSpaces("Drive")
                .setQ("mimeType = 'application/vnd.google-apps.folder' and '" + prefManager.getRootFolderID() + "' in parents")
                .execute();

        for (File file : resultF.getFiles()) {
//                Log.e("ROOT  Folder list : ", "shared   " + file.name + "   " + file.id)

            if (file.getName().equalsIgnoreCase(prefManager.getDirName())) {
//                Log.e("ROOT Folder  Found: ", "shared   " + file.getName() + "   " + file.getId());
                prefManager.setFolderID(file.getId());
                break;
            }
        }

    }

    public void driveSync() throws IOException {

        if (prefManager.getDirName().length() < 2) {
            initFolders();
        }

        if (prefManager.getFolderID().length() < 2) {
            initDrive();
        }


        java.io.File storageDir = context.getFilesDir();
        prefManager = new PrefManager(context);
        allLocalFiles = new ArrayList<>();
        allDriveFiles= new ArrayList<>();
        java.io.File filep = new java.io.File(storageDir.getAbsolutePath() + java.io.File.separator + "generated" + java.io.File.separator + prefManager.getDirName());
        java.io.File[] testD = filep.listFiles();


        for (int m = 0; m < testD.length; m++) {

            allLocalFiles.add(testD[m].getName());
//            Log.e("file in static", "ok  "+allFiles.size());
        }

        ArrayList<java.io.File> deviceFileList = new ArrayList<>(Arrays.asList(testD));
        FileList request = mDriveService.files().list()
                .setQ("mimeType='application/pdf' and trashed=false and '" + prefManager.getFolderID() + "' in parents")
                .setOrderBy("name")
                .setSpaces("Drive")
                .execute();
        ArrayList<File> driveFileList = new ArrayList<>(request.getFiles());


        for (File file : request.getFiles()) {
//            Log.e("data ", "report files  : " + file.getName() + "  " + file.getId());
            allDriveFiles.add(file.getName());
        }
//        Log.e("length device", " size in drive  " + driveFileList.size() + "      device " + deviceFileList.size());

        Set<String> hSet = new HashSet<>();

        for (java.io.File x : deviceFileList) {
            hSet.add(x.getName());
        }

        for (File x : driveFileList) {
            hSet.add(x.getName());
        }

//        Log.e("set ", "" + hSet.size());

        for (File fName : driveFileList) {
            for (java.io.File fdName : deviceFileList) {
                if (fName.getName().equals(fdName.getName())) {
//                    Log.e("file name ", "matched  " + fName.getName() + "   " + fName.getId() + "     " + fdName);
                    hSet.remove(fName.getName());
                }
            }
        }

        for (java.io.File fName : deviceFileList) {
            for (File fdName : driveFileList) {
                if (fName.getName().equals(fdName.getName())) {
                    hSet.remove(fdName.getName());
                }
            }
        }

        Collections.sort(deviceFileList);

        for (String vhSet : hSet) {
            String fileData = vhSet;
//            Log.e("file to sync", fileData);

            if (deviceFileList.size() > driveFileList.size()) {
                for (java.io.File fileUpload : deviceFileList) {
                    if (vhSet.equalsIgnoreCase(fileUpload.getName())) {
//                        Log.e("inside ", "up " + fileUpload.getName() + "     " + fileData);
                        new DriveUploader(fileUpload, context);
                    } else {
                        //  Log.e("inside ", "up else " + fileUpload.getName() + "     " + fileData);
                    }
                }
            } else if (driveFileList.size() > deviceFileList.size()) {

                for (File fileDownload : driveFileList) {
                    if (vhSet.equalsIgnoreCase(fileDownload.getName())) {
//                        Log.e("inside ", "download " + fileDownload.getName() + "     " + fileData);
//                        downloadPdfFile(fileDownload.getId());
                    } else {
                        //    Log.e("inside ", "up else " + fileDownload.getName() + "     " + fileData);
                    }
                }
            }
        }
        Log.e("files ", "synced");
    }


    public void downloadPdfFile(String fileId) {
        // Retrieve the metadata as a File object.
        try {
            File metadata = mDriveService.files().get(fileId).execute();
            String fileName = metadata.getName();

            java.io.File storageDir = context.getFilesDir();
            prefManager = new PrefManager(context);
            java.io.File filep = new java.io.File(storageDir.getAbsolutePath() + java.io.File.separator + "generated" + java.io.File.separator + prefManager.getDirName());

            java.io.File des = new java.io.File(filep.getAbsolutePath() + java.io.File.separator + fileName);

            Boolean t = filep.mkdirs();
//            Log.e("dir created", " " + t);

            OutputStream outputStream = new ByteArrayOutputStream();
            mDriveService.files().get(fileId)
                    .executeMediaAndDownloadTo(outputStream);

            FileOutputStream fOut = new FileOutputStream(des);
            fOut.write(((ByteArrayOutputStream) outputStream).toByteArray());
            fOut.close();
            Log.e("file download", "success");

        } catch (Exception e) {
            Log.e("file download", e.toString());
            serviceListener.handleError(e);
        }
    }

}