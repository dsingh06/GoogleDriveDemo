package com.thatapp.checklists.ViewClasses

import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.graphics.PorterDuff
import android.os.*
import android.support.annotation.RequiresApi
import android.support.design.widget.Snackbar
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.text.Layout
import android.util.Log
import android.view.View
import android.widget.Toast
import com.thatapp.checklists.R
import kotlinx.android.synthetic.main.display_checklists.*
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.WorkbookFactory
import java.io.File
import android.view.inputmethod.InputMethodManager
import android.widget.DatePicker
import android.widget.EditText
import android.widget.LinearLayout
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.drive.Drive.getDriveResourceClient
import com.google.android.gms.drive.DriveFolder
import com.google.android.gms.drive.MetadataChangeSet
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import com.thatapp.checklists.ModelClasses.*
import com.thatapp.checklists.ViewClasses.MainActivity.Companion.toastSuccessBackground
import java.text.SimpleDateFormat
import java.util.*


class DisplayQuestionsActivity : AppCompatActivity() {

    val questions: ArrayList<QuestionItem> = ArrayList()

    private val TAG = "My_LogMainActivity"
    private lateinit var mDriverServiceHelper: DriveServiceHelper
    private lateinit var prefManager: PrefManager

    lateinit var additionalDetails: EditText
    lateinit var workOrder: EditText
    lateinit var toolbar: Toolbar

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.display_checklists)

        prefManager = PrefManager(this)
        additionalDetails = findViewById(R.id.additionalDetails)
        workOrder = findViewById(R.id.etWorkOrder)

        val intent: Intent = intent
        val filename: String = intent.getStringExtra("fileName")
        Log.e("file name is ", "@   " + filename)

		toolbar = findViewById(R.id.my_toolbar)
		toolbar.setTitle(filename)
		toolbar.setNavigationIcon(R.drawable.ic_back)
		setSupportActionBar(toolbar)


		tvdateTime.setText(SimpleDateFormat("dd/MM/yyyy    HH:mm").format(Date()))
        val newCalendar = Calendar.getInstance();
        val siteDatePickerDialog = DatePickerDialog(this, R.style.MyDatePickerDialogTheme, DatePickerDialog.OnDateSetListener { datePicker: DatePicker, year: Int, monthOfYear: Int, dayOfMonth: Int ->
            val newDate = Calendar.getInstance();
            newDate.set(year, monthOfYear, dayOfMonth);
            tvdateTime.setText(SimpleDateFormat("dd/MM/yyyy    HH:mm", Locale.US).format(newDate.getTime()));

        }, newCalendar.get(Calendar.YEAR), newCalendar.get(Calendar.MONTH), newCalendar.get(Calendar.DAY_OF_MONTH))
        tvdateTime.setOnFocusChangeListener(object : View.OnFocusChangeListener {
            override fun onFocusChange(v: View?, hasFocus: Boolean) {
                if (hasFocus) siteDatePickerDialog.show()
                v!!.clearFocus()
            }
        })

        var uncheckedQuestionArray = ArrayList<String>() // to collect unchecked question's serialnumber
        btnSubmit.setOnClickListener {
            for (question in questions) {
                if (!question.strQuestion.equals("")) {
                    if (question.answer.equals("--")) {
                        uncheckedQuestionArray.add(question.serialNo)
                    }
                }
            }
            if (uncheckedQuestionArray.size >= 1) {
                val stringOfQuestions = uncheckedQuestionArray.toString().replace("[", "").replace("]", "")
                AlertDialog.Builder(this, android.R.style.Theme_Material_Dialog_Alert)
                        .setTitle("Questions skipped")
                        .setMessage("The following questions were not answered: \n" + stringOfQuestions)
                        .setPositiveButton("Go back", { _, _ ->
                            uncheckedQuestionArray.clear()
                        })
                        .setNegativeButton("Skip ALL", { _, _ ->
							showInfo()
                            CreatePdf(questions, filename, additionalDetails.text.toString(), workOrder.text.toString()).execute(this)
                        })
                        .setIcon(R.drawable.ic_alert)
                        .show()
            } else {
				showInfo()
                CreatePdf(questions, filename, additionalDetails.text.toString(),workOrder.text.toString()).execute(this)
            }
        }

        loadQuestionsArray(this, filename)
    }

	private fun showInfo() {
//							val snack  = Snackbar.make(btnSubmit, "Creating PDF...", Snackbar.LENGTH_LONG)
//							val view = snack.view
//							view.getBackground().setColorFilter(toastSuccessBackground, PorterDuff.Mode.SRC_IN)
//							snack.show()
		btnSubmit.isEnabled = false
		btnSubmit.isClickable = false
		btnSubmit.setBackgroundColor(toastSuccessBackground)
		btnSubmit.text = "Creating PDf......please wait"

	}

	@RequiresApi(Build.VERSION_CODES.M)
    private fun loadQuestionsArray(context: Context, fileName: String) {

        try {
            // Creating Input Stream
            val file = File(context.getFilesDir().getAbsolutePath() + File.separator + "downloads",fileName)// + File.separator + prefManager.dirName, fileName)

            // Create a workbook using the File System
            val myWorkBook = WorkbookFactory.create(file)

            // Get the first sheet from workbook
            val mySheet = myWorkBook.getSheetAt(0)
            val rowIter = mySheet.rowIterator()

            var questionsItem: QuestionItem
            var heading = 0
            var question = 0
            while (rowIter.hasNext()) {
                val row: Row = rowIter.next()
                val cellIterator: Iterator<Cell> = row.cellIterator()
                while (cellIterator.hasNext()) {
                    val cell: Cell = cellIterator.next()

                    if (row.rowNum >= 0) { //To filter column headings
                        if (cell.columnIndex == 0) {// To match column index
//                            Log.e("column", "")
//                            Log.e(TAG, "\n column Value: " + cell.toString())
                            heading += 1
                            question = 0
                            questionsItem = QuestionItem(heading.toString(), cell.toString(), "")
                            questions.add(questionsItem)
                        } else {
//                            Log.e("row", "")
//                            Log.e(TAG, "\t\tCell Value: " + cell.toString())
                            question += 1
                            questionsItem = QuestionItem("" + heading + "." + question, "", cell.toString())
                            questions.add(questionsItem)
                        }
                    }
                }
            }

            if (questions.size > 1) {
                val rv_list: RecyclerView = findViewById(R.id.rc)
                rv_list.layoutManager = LinearLayoutManager(this)
                // Access the RecyclerView Adapter and load the data into it
                rv_list.adapter = DisplayQuestionsAdapter(questions, this)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return
    }

    private fun goBackMethod() {
		val toast  = Toast.makeText(this, "PDF Created", Toast.LENGTH_LONG)
		val view = toast.view
		view.getBackground().setColorFilter(toastSuccessBackground, PorterDuff.Mode.SRC_IN)
		toast.show()
		finish()
    }

    fun hideSoftKeyboard() {
        if (currentFocus != null) {
            val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(currentFocus!!.windowToken, 0)
        }
    }

    class CreatePdf(val questions: ArrayList<QuestionItem>, val fileName: String, val additionalDetails: String, val workOrderNum: String) : AsyncTask<Context, Void, Context>() {
        lateinit var pdfCreationObject: CreatePDF
        override fun doInBackground(vararg p0: Context): Context {
            pdfCreationObject = CreatePDF(questions, p0.get(0), fileName, additionalDetails, workOrderNum)
            try {
                pdfCreationObject.startPDFCreation()
            } catch (e: Exception) {
            } // Tried with DriveUploadHelper before
            //val obj = DriveUploader(File(pdfCreationObject.des), p0[0])
            val obj = DriveUploader(File(pdfCreationObject.des), p0[0])
            return p0[0]
        }

        override fun onPostExecute(result: Context) {
            super.onPostExecute(result)
            (result as DisplayQuestionsActivity).goBackMethod()
        }
    }

    fun checkService() {
        val credential = GoogleAccountCredential.usingOAuth2(
                this, setOf(DriveScopes.DRIVE_FILE))
        val googleAccount = GoogleSignIn.getLastSignedInAccount(this)
        if (googleAccount != null) {
            credential.selectedAccount = googleAccount.account
        }

        val googleDriveService = Drive.Builder(
                AndroidHttp.newCompatibleTransport(),
                GsonFactory(),
                credential)
                .setApplicationName("Checklist")
                .build()

        mDriverServiceHelper = DriveServiceHelper(googleDriveService, this, applicationContext)
    }

}
