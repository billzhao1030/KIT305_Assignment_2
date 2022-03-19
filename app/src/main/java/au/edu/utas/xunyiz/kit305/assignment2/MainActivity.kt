package au.edu.utas.xunyiz.kit305.assignment2

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import au.edu.utas.xunyiz.kit305.assignment2.databinding.ActivityMainBinding
import au.edu.utas.xunyiz.kit305.assignment2.databinding.ModeSelectionBinding
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


const val database_log = "DATABASE"
const val console_log = "CUSTOM_CHECK"
const val REQUEST_IMAGE_CAPTURE = 1
const val PREFERENCE_FILE = "NameFile"
const val SCREEN_X = 768
const val SCREEN_Y = 826

class MainActivity : AppCompatActivity() {

    private lateinit var ui: ActivityMainBinding
    private lateinit var modeSelection: ModeSelectionBinding

    private lateinit var popupPrescribedBuilder: AlertDialog.Builder
    private lateinit var popupPrescribed: AlertDialog

    // variables sent between screens
    var exerciseMode: Boolean = true
    var goal: Boolean = true
    var rounds: Int = -1
    var time: Int = -1

    private val repetitionSpinnerItems = arrayOf(3, 4, 5, 6, 7, 8)
    private val timeSpinnerItems =
        arrayOf("30 seconds", "1 min", "1 min 30 seconds", "2 min", "2 min 30 seconds", "3 min")

    @SuppressLint("SimpleDateFormat")
    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ui = ActivityMainBinding.inflate(layoutInflater)
        modeSelection = ModeSelectionBinding.inflate(layoutInflater)
        setContentView(ui.root)

        // username setting and store
        var settings = getSharedPreferences(PREFERENCE_FILE, Context.MODE_PRIVATE)
        ui.username.text = settings.getString("username", "Your name")?.toEditable()

        ui.username.addTextChangedListener(object : TextWatcher {

            override fun afterTextChanged(s: Editable) {}

            override fun beforeTextChanged(s: CharSequence, start: Int,
                                           count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence, start: Int,
                                       before: Int, count: Int) {
                var editor = settings.edit()
                editor.putString("username", ui.username.text.toString())
                editor.apply()
            }
        })


        // set up the prescribed pop up
        popupPrescribedBuilder = AlertDialog.Builder(this)
        popupPrescribedBuilder.setView(modeSelection.root)
        popupPrescribed = popupPrescribedBuilder.create()

        modeSelection.goalMode.performClick()
        modeSelection.repetitions.performClick()

        // Round/time selection
        modeSelection.goalSpinner.adapter = ArrayAdapter<Int>(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            repetitionSpinnerItems
        )

        modeSelection.goalSpinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    if (goal) {
                        rounds = parent!!.getItemAtPosition(position) as Int
                    } else {
                        time = (position + 1) * 30
                    }
                }

                override fun onNothingSelected(p0: AdapterView<*>?) {
                }
            }

        modeSelection.goalSpinner.setSelection(2)

//        ui.camera.setOnClickListener {
//            requestToTakeAPicture()
//        }
    }

    fun String.toEditable(): Editable =  Editable.Factory.getInstance().newEditable(this)

    // prescribed game popup
    fun selectModePrescribedGame(view: View) {
        popupPrescribed.show()
    }

    fun closePrescribedPopup(view: View) {
        popupPrescribed.cancel()
    }


    // Mode selection
    fun goalModeOnClick(view: View) {
        modeSelection.goalMode.setBackgroundColor(Color.GREEN)
        modeSelection.freeMode.setBackgroundColor(Color.GRAY)
        exerciseMode = true

        buttonState(true)
    }

    fun freeModeOnClick(view: View) {
        modeSelection.freeMode.setBackgroundColor(Color.GREEN)
        modeSelection.goalMode.setBackgroundColor(Color.GRAY)
        exerciseMode = false

        buttonState(false)

        rounds = -1
        time = -1
    }

    fun buttonState(state: Boolean) {
        modeSelection.repetitions.isEnabled = state
        modeSelection.minutes.isEnabled = state
        modeSelection.goalSpinner.isEnabled = state
    }


    // goal selection
    fun goalRound(view: View) {
        goal = true
        time = -1

        modeSelection.repetitions.setBackgroundColor(Color.RED)
        modeSelection.minutes.setBackgroundColor(Color.GRAY)

        modeSelection.goalSpinner.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            repetitionSpinnerItems
        )
        modeSelection.goalSpinner.setSelection(2)
    }

    fun goalTime(view: View) {
        goal = false
        rounds = -1

        modeSelection.minutes.setBackgroundColor(Color.RED)
        modeSelection.repetitions.setBackgroundColor(Color.GRAY)

        modeSelection.goalSpinner.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            timeSpinnerItems
        )
        modeSelection.goalSpinner.setSelection(2)
    }


    // popup menu
    fun goToCustomize(view: View) {
        var customize = Intent(this, Customization::class.java)
        customize.putExtra("ExerciseMode", exerciseMode)
        customize.putExtra("Goal", goal)
        customize.putExtra("Rounds", rounds)
        customize.putExtra("Time", time)

        startActivity(customize)
    }

    fun viewHistory(view: View) {
        var history = Intent(this, HistoryActivity::class.java)
        startActivity(history)
    }

    fun shareCSV(view: View) {
        var sentCSV = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, "Text to share...")
            type = "text/plain"
        }
        startActivity(Intent.createChooser(sentCSV, "Share via..."))
    }
}