package com.thatapp.checklist.ModelClasses

import android.content.Context
import android.content.SharedPreferences

public class PrefManager(var context: Context) {

    private val sharedPref: SharedPreferences
    private val editor: SharedPreferences.Editor


    var downloadPath: String?
        get() = sharedPref.getString(KEY_DOWNLOAD_PATH, "")
        set(strPath) {
            editor.putString(KEY_DOWNLOAD_PATH, strPath)
            editor.commit()
        }

    // FOLDER TO STORE THE FILE
    var folderID: String?
        get() = sharedPref.getString(KEY_FOLDER_ID, "")
        set(strFolderID) {
            editor.putString(KEY_FOLDER_ID, strFolderID)
            editor.commit()
        }

    // PARENT FOLDER OF FOLDERID ABOVE - I.E. CHECKLIST APP
    var rootFolderID: String?
        get() = sharedPref.getString(KEY_ROOT_FOLDER_ID, "na")
        set(strFolderID) {
            editor.putString(KEY_ROOT_FOLDER_ID, strFolderID)
            editor.commit()
        }

    var companyName: String?
        get() = sharedPref.getString(KEY_COMPANY_NAME, "")
        set(strPath) {
            editor.putString(KEY_COMPANY_NAME, strPath)
            editor.commit()
        }

    var loginEmail: String?
        get() = sharedPref.getString(KEY_LOGIN_EMAIL, "na")
        set(strEmail) {
            editor.putString(KEY_LOGIN_EMAIL, strEmail)
            editor.commit()
        }

    var userName: String?
        get() = sharedPref.getString(KEY_USER_NAME, "")
        set(userName) {
            editor.putString(KEY_USER_NAME, userName)
            editor.commit()
        }

    var jobTitle: String?
        get() = sharedPref.getString(KEY_JOB_TITLE, "")
        set(userName) {
            editor.putString(KEY_JOB_TITLE, userName)
            editor.commit()
        }

    var systemDate: String?
        get() = sharedPref.getString(KEY_SYSTEM_DATE, "na")
        set(date) {
            editor.putString(KEY_SYSTEM_DATE, date)
            editor.commit()
        }

    var loginStatus: Boolean
        get() = sharedPref.getBoolean(KEY_LOGIN_STATUS, false)
        set(state) {
            editor.putBoolean(KEY_LOGIN_STATUS, state)
            editor.commit()
        }

    var firstRun: Boolean
        get() = sharedPref.getBoolean(KEY_FIRST_RUN, true)
        set(state) {
            editor.putBoolean(KEY_FIRST_RUN, state)
            editor.commit()
        }


    var termAndCondition: Int
        get() = sharedPref.getInt(KEY_TERMS_CONDITION, 0)
        set(date) {
            editor.putInt(KEY_TERMS_CONDITION, date)
            editor.commit()
        }

    var appVersion: Int
        get() = sharedPref.getInt(KEY_VERSION, 0)
        set(date) {
            editor.putInt(KEY_VERSION, date)
            editor.commit()
        }

    var appDownloadUrl: String?
        get() =
            sharedPref.getString(KEY_APP_DOWNLOAD_URL, "https://play.google.com/store/apps/details?id=")
        set(date) {
            editor.putString(KEY_APP_DOWNLOAD_URL, date)
            editor.commit()
        }

    // LOCAL DIRECTORY NAME WHERE GENERATED FILES STAY
    var dirName: String?
        get() = sharedPref.getString(KEY_DIR_NAME,"")
        set(strDir) {
            editor.putString(KEY_DIR_NAME, strDir)
            editor.commit()
        }

    init {
        sharedPref = context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
        editor = sharedPref.edit()
    }

    companion object {

        private val PREF = "Checklist App"
        //---------------Key Value---------
        private val KEY_DOWNLOAD_PATH = "download_path"
        private val KEY_COMPANY_NAME = "company_name"
        private val KEY_LOGIN_EMAIL = "login_email"
        private val KEY_LOGIN_STATUS = "login_status"
        val KEY_FOLDER_ID = "folder_id"
        val KEY_ROOT_FOLDER_ID = "root_folder_id"
        val KEY_USER_NAME = "user_name"
        val KEY_SYSTEM_DATE = "system_date"
        val KEY_UPDATED_ON_DATE = "updated_on_date"
        val KEY_TERMS_CONDITION = "terms_condition"
        val KEY_VERSION = "app_version"
        val KEY_APP_DOWNLOAD_URL = "app_download_url"
        val KEY_REPORT_PATH = "pdf_image_url"
        val KEY_DIR_NAME = "dir_name"
        val KEY_FIRST_RUN = "first_run"
        public var KEY_JOB_TITLE = "job_title"
    }
}
