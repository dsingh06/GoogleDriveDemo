package com.thatapp.checklists.ModelClasses;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.StrictMode;
import android.provider.OpenableColumns;
import android.support.v4.util.Pair;
import android.util.Log;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.api.client.http.ByteArrayContent;
import com.google.api.client.http.FileContent;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.google.api.services.drive.model.Permission;
import com.google.api.services.drive.model.StartPageToken;
import com.google.api.services.drive.model.TeamDrive;
import com.google.api.services.drive.model.TeamDriveList;
import com.thatapp.checklists.ViewClasses.MainActivity;

import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import okio.Okio;


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

        if(prefManager.getDirName().length()>3){
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
            Log.e("data ", "report files  : " + file.getName() + "  " + file.getId());
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

        Log.e("set ", "" + hSet.size());

        for (File fName : driveFileList) {
            for (java.io.File fdName : deviceFileList) {
                if (fName.getName().equals(fdName.getName())) {
                    Log.e("file name ", "matched  " + fName.getName() + "   " + fName.getId() + "     " + fdName);
                    hSet.remove(fName.getName());
                }
            }
        }

//        Log.e("@@@ length device", " size in drive  " + driveFileList.size() + "      device " + deviceFileList.size());
//        Log.e("set ", "" + hSet.size());

        for (java.io.File fName : deviceFileList) {
            for (File fdName : driveFileList) {
                if (fName.getName().equals(fdName.getName())) {
                    Log.e("file name ", "matched  " + fName.getName() + "     " + fdName);
                    hSet.remove(fdName.getName());
                }
            }
        }

//        Log.e("## length device", " size in drive  " + driveFileList.size() + "      device " + deviceFileList.size());
        Log.e("set ", "" + hSet.size());


        Set<String> driveSet = new HashSet<>();
        Set<String> deviceSet = new HashSet<>();

        for (String vhSet : hSet) {
            String fileData = vhSet;

            Log.e("file to sync", fileData);
            Collections.sort(deviceFileList);
//            Collections.sort(driveFileList);

            if (deviceFileList.size() > driveFileList.size()) {
                for (int i = 0; i < deviceFileList.size(); i++) {
//                    Log.e("file ", "1   " + deviceFileList.get(i).getName());

                    if (fileData.equalsIgnoreCase(deviceFileList.get(i).getName())) {

//                        Log.e("inside ", "upload  matched ");
//                        java.io.File fileUpload = deviceFileList.get(i);
//                        new DriveUploader(deviceFileList.get(i), context);
                    } else {
                        Log.e("inside ", "upload");
                        driveSet.add(deviceFileList.get(i).getName());
                        //                      java.io.File fileUpload = deviceFileList.get(i);
//                        new DriveUploader(deviceFileList.get(i), context);
                    }
                }
            } else if (driveFileList.size() > deviceFileList.size()) {

                for (int i = 0; i < driveFileList.size(); i++) {
//                    Log.e("file ", "2  " + driveFileList.get(i).getName());
                    if (fileData.equalsIgnoreCase(driveFileList.get(i).getName())) {

//                        Log.e("inside ", "download  matched");
//                        File fileDownload = driveFileList.get(i);
//                        downloadPdfFile(fileDownload.getId());
                    } else {

                        Log.e("inside ", "download");
                        deviceSet.add(driveFileList.get(i).getName());
                        //  File fileDownload = driveFileList.get(i);
                        // downloadPdfFile(fileDownload.getId());
                    }
                }
            }
        }

        for (int j = 0; j < driveSet.size(); j++) {
            Log.e("inside ", "upload");
            java.io.File fileUpload = deviceFileList.get(j);
            new DriveUploader(fileUpload, context);
        }


        for (int k = 0; k < deviceSet.size(); k++) {
            Log.e("inside ", "download");
            File fileDownload = driveFileList.get(k);
            downloadPdfFile(fileDownload.getId());
        }






      /*  for (int i = 0; i < driveFileList.size(); i++) {
            for (int m = 0; m < deviceFileList.size(); m++) {
                String fName = driveFileList.get(i).toString();
                String fdName = deviceFileList.get(m).toString();
                Log.e("name is ", "1  " + fName);
                Log.e("name is ", "2  " + fdName);
            }
        }
        Log.e("length device", " size in drive  " + driveFileList.size() + "      device " + deviceFileList.size());
*/

//        return result;
    }


    public void queryA() throws IOException, IllegalStateException {

        FileList result = mDriveService.files().list().setCorpus("user").setSpaces("Drive")
                .setQ("mimeType='application/vnd.google-apps.folder' and trashed=false and sharedWithMe=true and title='CheckList App'")
                .execute();
        for (File file : result.getFiles()) {
            Log.e("values ", file.getName() + "   " + file.getId());
        }

    }

    public void listing() throws IOException {

        FileList result = mDriveService.files().list().setCorpus("user")
                .setQ("sharedWithMe=true and mimeType='application/vnd.google-apps.folder'")
                .execute();
        Log.e("results", "  " + result.size());
        Log.e("results", "  " + result.getKind());
        Log.e("results", "  " + result.getFiles().toString());
        List files = result.getFiles();
        for (int i = 0; i < files.size(); i++) {
            Log.e("Found file: ", "wewewe   " + files.get(i).toString() + "   ");
        }

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

//                    Log.e("name", name);

                    FileList result = mDriveService.files().list().setSpaces("drive")
                            .setQ("mimeType='application/vnd.ms-excel'")
                            .execute();

//                    Log.e("size", " 111 " + result.getFiles().size());

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

            serviceListener.fileDownloaded(des, "abcd");
        } catch (Exception e) {
            Log.e("file download", e.toString());
            serviceListener.handleError(e);
        }
    }

}