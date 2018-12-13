package com.thatapp.checklists.ModelClasses;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
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
import java.util.Collections;
import java.util.List;
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

    public DriveServiceHelper(Drive driveService, Activity activity, Context context) {
        this.mDriveService = driveService;
        this.activity = activity;
        this.context = context;
        serviceListener = (ServiceListener) activity;
    }


    /**
     * Creates a text file in the user's My Drive folder and returns its file ID.
     */
    public Task<String> createFile(java.io.File file) {
        return Tasks.call(mExecutor, () -> {
            File metadata = new File()
                    .setParents(Collections.singletonList("root"))
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
            java.io.File filep = new java.io.File(storageDir.getAbsolutePath() + java.io.File.separator + "downloads" + java.io.File.separator + "awasrishabh");

            java.io.File des = new java.io.File(filep.getAbsolutePath() + java.io.File.separator + fileName);

            Boolean t = filep.mkdirs();
            Log.e("dir created", " " + t);


            OutputStream outputStream = new ByteArrayOutputStream();
            mDriveService.files().get(fileId)
                    .executeMediaAndDownloadTo(outputStream);
            Log.e("file in download 121 ", fileName);

            FileOutputStream fOut = new FileOutputStream(des);
            fOut.write(((ByteArrayOutputStream) outputStream).toByteArray());
            Log.e("file ", "12");
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
    public Task<FileList> queryFiles() throws IOException {
        // listing();

        return Tasks.call(mExecutor, () ->

                mDriveService.files().list().setSpaces("drive").setFields("files(id, name)")
                        .execute());
//      mDriveService.files().export("","application/vnd.ms-excel")
    }


    public void queryA() throws IOException, IllegalStateException {

        TeamDrive teamDriveMetadata = new TeamDrive();
        teamDriveMetadata.setName("Project Resources");
        String requestId = UUID.randomUUID().toString();
        TeamDrive teamDriver = mDriveService.teamdrives().create(requestId,
                teamDriveMetadata)
                .execute();
        System.out.println("Team Drive ID: " + teamDriver.getId());


        String pageToken = null;
        Permission newOrganizerPermission = new Permission()
                .setType("user")
                .setRole("organizer")
                .setEmailAddress("user@example.com");

        do {
            TeamDriveList result = mDriveService.teamdrives().list()
                    .setFields("nextPageToken, teamDrives(id, name)")
                    .setUseDomainAdminAccess(true)
                    .setPageToken(pageToken)
                    .execute();
            for (TeamDrive teamDrive : result.getTeamDrives()) {
                System.out.printf("Found Team Drive without organizer: %s (%s)\n",
                        teamDrive.getName(), teamDrive.getId());
                // Note: For improved efficiency, consider batching
                // permission insert requests
                Permission permissionResult = mDriveService.permissions()
                        .create(teamDrive.getId(), newOrganizerPermission)
                        .setUseDomainAdminAccess(true)
                        .setSupportsTeamDrives(true)
                        .setFields("id")
                        .execute();
                Log.e("Added permission: ", "121   " + permissionResult.getId());

            }
            pageToken = result.getNextPageToken();
        } while (pageToken != null);

    }

    public void listing() throws IOException {

        FileList result = mDriveService.files().list()
                .setQ("name='application/vnd.ms-excel'")
                .setSpaces("drive")
                .setFields("files(id, name)")
                .execute();
        for (File file : result.getFiles()) {
            System.out.printf("Found file: %s (%s)\n",
                    file.getName(), file.getId());
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

                    Log.e("name", name);

                    FileList result = mDriveService.files().list().setSpaces("drive").setQ(
                            "mimeType='application/vnd.ms-excel'")
                            .execute();


                    for (File file : result.getFiles()) {
                        Log.e("values ", "file: " + file.getName() + "     " + file.getId());
                        if (file.getName().equalsIgnoreCase(name)) {
                            Log.e("\nmatched ", "file: " + file.getName() + "     " + file.getId());
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

}