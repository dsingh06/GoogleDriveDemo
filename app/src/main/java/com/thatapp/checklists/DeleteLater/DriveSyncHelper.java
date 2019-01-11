//package com.thatapp.checklists.ModelClasses;
//
//import android.content.Context;
//import android.util.Log;
//import com.google.api.services.drive.Drive;
//import com.google.api.services.drive.model.File;
//import com.google.api.services.drive.model.FileList;
//import java.io.IOException;
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.Collections;
//import java.util.HashSet;
//import java.util.Set;
//
//
//public class DriveSyncHelper {
//
//    private Drive mDriveService;
//    private Context context;
//    private PrefManager prefManager;
//
//    private Boolean checklistFolderExists = false;
//    private Boolean mailIdFolderExists = false;
//
//    private String TAG = "DriveSyncHelperClass:>> ";
//
//    public DriveSyncHelper(Drive driveService, Context context) {
//        this.mDriveService = driveService;
//        this.context = context;
//        prefManager = new PrefManager(context);
//    }
//
//
//    public void driveSync() throws IOException {
//
//        if (prefManager.getDirName().length() < 2) {
//            initFolders();
//        }
//
//        if (prefManager.getFolderID().length() < 2) {
//            initDrive();
//        }
//
//
//        java.io.File storageDir = context.getFilesDir();
//        prefManager = new PrefManager(context);
//        allLocalFiles = new ArrayList<>();
//        allDriveFiles= new ArrayList<>();
//        java.io.File filep = new java.io.File(storageDir.getAbsolutePath() + java.io.File.separator + "generated" + java.io.File.separator + prefManager.getDirName());
//        java.io.File[] testD = filep.listFiles();
//
//
//        for (int m = 0; m < testD.length; m++) {
//            allLocalFiles.add(testD[m].getName());
//        }
//
//        ArrayList<java.io.File> deviceFileList = new ArrayList<>(Arrays.asList(testD));
//
//        if (mailIdFolderExists){
//            FileList folderList = mDriveService.files().list()
//                    .setQ("mimeType='application/pdf' and trashed=false and '" + prefManager.getFolderID() + "' in parents")
//                    .setOrderBy("name")
//                    .setSpaces("Drive")
//                    .execute();
//
//            ArrayList<File> driveFileList = new ArrayList<>(folderList.getFiles());
//
//
//            for (File file : driveFileList) {
//                allDriveFiles.add(file.getName());
//            }
//
//            Set<String> localFilesSet = new HashSet<>();
//            Set<String> driveFilesSet = new HashSet<>();
//
//            localFilesSet.addAll(allLocalFiles);
//            driveFilesSet.addAll(allDriveFiles);
//
//            localFilesSet.removeAll(driveFilesSet);
//
////            for (java.io.File x : deviceFileList) {
////                hSet.add(x.getName());
////            }
////
////            for (File x : driveFileList) {
////                hSet.add(x.getName());
////            }
////
//////        Log.e("set ", "" + hSet.size());
////
////            for (File fName : driveFileList) {
////                for (java.io.File fdName : deviceFileList) {
////                    if (fName.getName().equals(fdName.getName())) {
//////                    Log.e("file name ", "matched  " + fName.getName() + "   " + fName.getId() + "     " + fdName);
////                        hSet.remove(fName.getName());
////                    }
////                }
////            }
////
////            for (java.io.File fName : deviceFileList) {
////                for (File fdName : driveFileList) {
////                    if (fName.getName().equals(fdName.getName())) {
////                        hSet.remove(fdName.getName());
////                    }
////                }
////            }
//
//            Collections.sort(deviceFileList);
//
//  //          for (String vhSet : hSet) {
//  //              String fileData = vhSet;
////            Log.e("file to sync", fileData);
//
//                if (localFilesSet.size()>0) {
//                    for (java.io.File fileUpload : deviceFileList){
//                        if (localFilesSet.contains(fileUpload)) new DriveUploader(fileUpload, context);
//                        Log.e(TAG,"Uploading: "+fileUpload.getName());
//                    }
////                    for (java.io.File fileUpload : deviceFileList) {
////                        if (vhSet.equalsIgnoreCase(fileUpload.getName())) {
////                        Log.e("inside ", "up " + fileUpload.getName() + "     " + fileData);
////                            new DriveUploader(fileUpload, context);
////                        } else {
////                            //  Log.e("inside ", "up else " + fileUpload.getName() + "     " + fileData);
////                        }
////                    }
//                }
////                else if (driveFileList.size() > deviceFileList.size()) {
////
////                    for (File fileDownload : driveFileList) {
////                        if (vhSet.equalsIgnoreCase(fileDownload.getName())) {
//////                        Log.e("inside ", "download " + fileDownload.getName() + "     " + fileData);
//////                        downloadPdfFile(fileDownload.getId());
////                        } else {
////                            //    Log.e("inside ", "up else " + fileDownload.getName() + "     " + fileData);
////                        }
////                    }
////                }
////            }
//            Log.e("files ", "synced");
//
//        }
//    }
//
//
//    private void initFolders() {
//
//        java.io.File storageDir = context.getFilesDir();
//        prefManager = new PrefManager(context);
//        java.io.File fileD = new java.io.File(storageDir.getAbsolutePath() + java.io.File.separator + "downloads");
//        Boolean d = fileD.mkdirs();
//        java.io.File fileR = new java.io.File(storageDir.getAbsolutePath() + java.io.File.separator + "generated" + java.io.File.separator + prefManager.getDirName());
//        Boolean g = fileR.mkdirs();
//    }
//
//
//    private void initDrive() throws IOException {
//
//        FileList driveRootFolders = mDriveService.files().list().setSpaces("Drive")
//                // and sharedWithMe=true") WE DON'T WANT ONLY SHARED
//                .setQ("mimeType='application/vnd.google-apps.folder' and trashed=false")
//                .execute();
//
//        for (File folder : driveRootFolders.getFiles()) {
//            if (folder.getName().equalsIgnoreCase("CheckList App")) {
//                prefManager.setRootFolderID(folder.getId());
//                checklistFolderExists = true;
//                break;
//            }
//        }
//
//        if (checklistFolderExists){
//            FileList foldersInChecklist = mDriveService.files().list().setSpaces("Drive")
//                    .setQ("mimeType = 'application/vnd.google-apps.folder' and '" + prefManager.getRootFolderID() + "' in parents")
//                    .execute();
//
//            for (File folder : foldersInChecklist.getFiles()) {
//                if (folder.getName().equalsIgnoreCase(prefManager.getDirName())) {
//                    prefManager.setFolderID(folder.getId());
//                    mailIdFolderExists = true;
//                    break;
//                }
//            }
//        } else {
//            for (File folder : driveRootFolders.getFiles()) {
//                if (folder.getName().equalsIgnoreCase(prefManager.getDirName())) {
//                    prefManager.setFolderID(folder.getId());
//                    mailIdFolderExists = true;
//                    break;
//                }
//            }
//        }
//    }
//
//    /*
//    public void downloadPdfFile(String fileId) {
//        // Retrieve the metadata as a File object.
//        try {
//            File metadata = mDriveService.files().get(fileId).execute();
//            String fileName = metadata.getName();
//
//            java.io.File storageDir = context.getFilesDir();
//            prefManager = new PrefManager(context);
//            java.io.File filep = new java.io.File(storageDir.getAbsolutePath() + java.io.File.separator + "generated" + java.io.File.separator + prefManager.getDirName());
//
//            java.io.File des = new java.io.File(filep.getAbsolutePath() + java.io.File.separator + fileName);
//
//            Boolean t = filep.mkdirs();
////            Log.e("dir created", " " + t);
//
//            OutputStream outputStream = new ByteArrayOutputStream();
//            mDriveService.files().get(fileId)
//                    .executeMediaAndDownloadTo(outputStream);
//
//            FileOutputStream fOut = new FileOutputStream(des);
//            fOut.write(((ByteArrayOutputStream) outputStream).toByteArray());
//            fOut.close();
//            Log.e("file download", "success");
//
//        } catch (Exception e) {
//            Log.e("file download", e.toString());
//            serviceListener.handleError(e);
//        }
//    }
//    */
//
//}