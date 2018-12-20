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
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;


/**
 * A utility for performing read/write operations on Drive files via the REST API and opening a
 * file picker UI via Storage Access Framework.
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


    /**
     * Creates a text file in the user's My Drive folder and returns its file ID.
     */
    public Task<String> uploadFile(java.io.File file) {
        return Tasks.call(mExecutor, () -> {
            File metadata = new File()
                    .setParents(Collections.singletonList(prefManager.getFolderID()))
                    .setMimeType("application/pdf")
                    .setName(file.getName());

            File googleFile = mDriveService.files().create(metadata).execute();
            if (googleFile == null) {
                throw new IOException("Null result when requesting file creation.");
            }

            Log.e("fid", "" + googleFile.getId());
            return googleFile.getId();
        });
    }

    /**
     * Opens the file identified by {@code fileId} and returns a {@link Pair} of its name and
     * contents.
     */
    public Task<Pair<String, String>> readFile(String fileId) {
        return Tasks.call(mExecutor, () -> {
            // Retrieve the metadata as a File object.
            File metadata = mDriveService.files().get(fileId).execute();
            String name = metadata.getName();

            // Stream the file contents to a String.
            try (InputStream is = mDriveService.files().get(fileId).executeMediaAsInputStream();
                 BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
                StringBuilder stringBuilder = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    stringBuilder.append(line);
                }
                String contents = stringBuilder.toString();

                return Pair.create(name, contents);
            }
        });
    }

    /**
     * Updates the file identified by {@code fileId} with the given {@code name} and {@code
     * content}.
     */
    public Task<Void> saveFile(String fileId, String name, String content) {
        return Tasks.call(mExecutor, () -> {
            // Create a File containing any metadata changes.


            File fileMetadata = new File();
            fileMetadata.setName("checklists");
            fileMetadata.setMimeType("application/vnd.google-apps.folder");

            File file = mDriveService.files().create(fileMetadata)
                    .setFields(fileId)
                    .execute();
            System.out.println("Folder ID: " + file.getId());

            File metadata = new File().setName(name);

            // Convert content to an AbstractInputStreamContent instance.
            ByteArrayContent contentStream = ByteArrayContent.fromString("text/plain", content);

            // Update the metadata and contents.
            mDriveService.files().update(fileId, metadata, contentStream).execute();
            return null;
        });
    }


    public void downloadFile(String fileId) {
        // Retrieve the metadata as a File object.
        try {

            File metadata = mDriveService.files().get(fileId).execute();
            String fileName = metadata.getName();

            java.io.File storageDir = context.getFilesDir();
            prefManager = new PrefManager(context);
            java.io.File filep = new java.io.File(storageDir.getAbsolutePath() + java.io.File.separator + "downloads" + java.io.File.separator + prefManager.getDirName());

            java.io.File des = new java.io.File(filep.getAbsolutePath() + java.io.File.separator + fileName);

            Boolean t = filep.mkdirs();
            Log.e("dir created", " " + t);


            OutputStream outputStream = new ByteArrayOutputStream();
            mDriveService.files().get(fileId)
                    .executeMediaAndDownloadTo(outputStream);

            FileOutputStream fOut = new FileOutputStream(des);
            fOut.write(((ByteArrayOutputStream) outputStream).toByteArray());
            fOut.close();
            Log.e("file download", "success");

            serviceListener.fileDownloaded(des, "abcd");
        } catch (Exception e) {
            Log.e("file download", e.toString());
            serviceListener.handleError(e);
        }
    }

    /**
     * Returns a {@link FileList} containing all the visible files in the user's My Drive.
     *
     * <p>The returned list will only contain files visible to this app, i.e. those which were
     * created by this app. To perform operations on files not created by the app, the project must
     * request Drive Full Scope in the <a href="https://play.google.com/apps/publish">Google
     * Developer's Console</a> and be submitted to Google for verification.</p>
     */
    public boolean driveCheck() throws IOException {

        boolean result = false;

        initFolders();
        if (prefManager.getFirstRun()) {
            prefManager.setFirstRun(false);
            Log.e("Login", "status is: " + prefManager.getFirstRun());
            initFolders();
        }

        if (prefManager.getDirName().length() > 3) {
            driveSync();
        }

        if (prefManager.getJobTitle().length() < 5) {
            return false;
        } else if (prefManager.getCompanyName().length() < 5) {
            return false;
        } else if (prefManager.getUserName().length() < 5) {
            return false;
        } else {

            return true;
        }


        // return result;
    }

    private void initFolders() {

        java.io.File storageDir = context.getFilesDir();
        prefManager = new PrefManager(context);
        java.io.File fileD = new java.io.File(storageDir.getAbsolutePath() + java.io.File.separator + "downloads" + java.io.File.separator + prefManager.getDirName());
        Boolean d = fileD.mkdirs();
        java.io.File fileR = new java.io.File(storageDir.getAbsolutePath() + java.io.File.separator + "generated" + java.io.File.separator + prefManager.getDirName());
        Boolean g = fileR.mkdirs();

//        Log.e("dir created", " " + d + "    " + g);
    }

    public void driveSync() throws IOException {

        boolean result = false;
        ArrayList<File> driveFileList = new ArrayList();
        ArrayList<java.io.File> deviceFileList = new ArrayList();
        java.io.File storageDir = context.getFilesDir();
        prefManager = new PrefManager(context);
        java.io.File filep = new java.io.File(storageDir.getAbsolutePath() + java.io.File.separator + "generated" + java.io.File.separator + prefManager.getDirName());
        java.io.File[] testD = filep.listFiles();
        for (int i = 0; i < testD.length; i++) {
            deviceFileList.add(testD[i]);
        }

//        Log.e("length device", " size in device  " + testD.length + "     " + deviceFileList.size());
//
        FileList request = mDriveService.files().list().setQ("mimeType='application/pdf' and trashed=false and '" + prefManager.getFolderID() + "' in parents").setOrderBy("name").setSpaces("Drive").execute();
        for (File file : request.getFiles()) {
//            Log.e("data ", "report files  : " + file.getName() + "  " + file.getId());
            driveFileList.add(file);
        }
        Log.e("length device", " size in drive  " + driveFileList.size() + "      device " + deviceFileList.size());

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
            Log.e("file to sync", fileData);

            if (deviceFileList.size() > driveFileList.size()) {
                for (java.io.File fileUpload : deviceFileList) {
                    if (vhSet.equalsIgnoreCase(fileUpload.getName())) {
                        Log.e("inside ", "up " + fileUpload.getName() + "     " + fileData);
                        new DriveUploader(fileUpload, context);
                    } else {
                      //  Log.e("inside ", "up else " + fileUpload.getName() + "     " + fileData);
                    }
                }
            } else if (driveFileList.size() > deviceFileList.size()) {

                for (File fileDownload : driveFileList) {
                    if (vhSet.equalsIgnoreCase(fileDownload.getName())) {
                        Log.e("inside ", "download " + fileDownload.getName() + "     " + fileData);
                        downloadPdfFile(fileDownload.getId());
                    } else {
                    //    Log.e("inside ", "up else " + fileDownload.getName() + "     " + fileData);
                    }
                }
            }
        }
        Log.e("files ","synced");
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