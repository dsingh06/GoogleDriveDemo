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
public class DriveServiceHelper {
    private final Executor mExecutor = Executors.newSingleThreadExecutor();
    private Drive mDriveService;
    private Activity activity;
    private Context context;
    ServiceListener serviceListener;
    PrefManager prefManager;

    public DriveServiceHelper(Drive driveService, Activity activity, Context context) {
        this.mDriveService = driveService;
        this.activity = activity;
        this.context = context;
        serviceListener = (ServiceListener) activity;
        prefManager = new PrefManager(activity);
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

        initFolders();
        initDrive();

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
    }

    private void initFolders() {

        java.io.File storageDir = context.getFilesDir();
        prefManager = new PrefManager(context);
        java.io.File fileD = new java.io.File(storageDir.getAbsolutePath() + java.io.File.separator + "downloads" + java.io.File.separator + prefManager.getDirName());
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
                Log.e("ROOT Folder  Found: ", "shared   " + file.getName() + "   " + file.getId());
                prefManager.setFolderID(file.getId());
                break;
            }


        }

    }


    public void driveSync() throws IOException {

        ArrayList<File> driveFileList = new ArrayList();
        ArrayList<java.io.File> deviceFileList = new ArrayList();
        java.io.File storageDir = context.getFilesDir();
        prefManager = new PrefManager(context);
        java.io.File filep = new java.io.File(storageDir.getAbsolutePath() + java.io.File.separator + "generated" + java.io.File.separator + prefManager.getDirName());
        java.io.File[] testD = filep.listFiles();
        for (int i = 0; i < testD.length; i++) {
            deviceFileList.add(testD[i]);
        }

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
//                    Log.e("file name ", "matched  " + fName.getName() + "     " + fdName);
                    hSet.remove(fdName.getName());
                }
            }
        }

//        Log.e("## length device", " size in drive  " + driveFileList.size() + "      device " + deviceFileList.size());
        Log.e("set ", "" + hSet.size());

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
        Log.e("files ", "synced");
    }

    /**
     * Returns an {@link Intent} for opening the Storage Access Framework file picker.
     */
    public Intent createFilePickerIntent() {

        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/vnd.ms-excel");
        return intent;
    }

    /**
     * Opens the file at the {@code uri} returned by a Storage Access Framework {@link Intent}
     * created by {@link #createFilePickerIntent()} using the given {@code contentResolver}.
     */
    public Task<Pair<String, String>> openFileUsingStorageAccessFramework(
            ContentResolver contentResolver, Uri uri) {
        return Tasks.call(mExecutor, () -> {
            // Retrieve the document's display name from its metadata.
            String name = "";

            try (Cursor cursor = contentResolver.query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    name = cursor.getString(nameIndex);

                    FileList result = mDriveService.files().list().setSpaces("drive")
                            .setQ("mimeType='application/vnd.ms-excel'")
                            .execute();


                    for (File file : result.getFiles()) {
//                        Log.e("values ", "file: " + file.getName() + "     " + file.getId());
                        if (file.getName().equalsIgnoreCase(name)) {
//                            Log.e("file id found ", "file: " + file.getName() + "     " + file.getId());
                            downloadFile(file.getId());
                        }
                    }
                } else {
                    throw new IOException("Empty cursor returned for file.");
                }
            }

            // Read the document's contents as a String.
            String content;
            try (InputStream is = contentResolver.openInputStream(uri);
                 BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
                StringBuilder stringBuilder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    stringBuilder.append(line);
                }
                content = stringBuilder.toString();
                Log.e("con", content);
            }
            return Pair.create(name, content);
        });
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

            // serviceListener.fileDownloaded(des, "abcd");
        } catch (Exception e) {
            Log.e("file download", e.toString());
            serviceListener.handleError(e);
        }
    }
}