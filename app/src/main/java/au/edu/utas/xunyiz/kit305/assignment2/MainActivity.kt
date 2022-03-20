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
import au.edu.utas.xunyiz.kit305.assignment2.databinding.ModeSelectionDesignedBinding
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
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
    private lateinit var designedSelection: ModeSelectionDesignedBinding

    private lateinit var popupPrescribedBuilder: AlertDialog.Builder
    private lateinit var popupPrescribed: AlertDialog

    private lateinit var popupDesignedBuilder: AlertDialog.Builder
    private lateinit var popupDesigned: AlertDialog

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
        designedSelection = ModeSelectionDesignedBinding.inflate(layoutInflater)
        setContentView(ui.root)

        ui.repetitionSummary.text = "Loading..."

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

        popupDesignedBuilder = AlertDialog.Builder(this)
        popupDesignedBuilder.setView(designedSelection.root)
        popupDesigned = popupDesignedBuilder.create()

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

        getHistorySummary()
    }

    private fun getHistorySummary() {
        val db = Firebase.firestore
        val games = db.collection("games")

        var prescribedTotal: Int = 0
        var designedToal: Int = 0

        games
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    val game = document.toObject<Game>()

                    if (game.gameType == true) {
                        prescribedTotal += game.repetition!!

                    } else {
                        designedToal += game.repetition!!
                    }
                }

                var str = "You have completed\n${prescribedTotal} repetitions in Number in order\n" +
                        "${designedToal} repetitions in Matching numbers"

                ui.repetitionSummary.text = str
            }
    }

    fun String.toEditable(): Editable =  Editable.Factory.getInstance().newEditable(this)

    // prescribed game popup
    fun selectModePrescribedGame(view: View) {
        popupPrescribed.show()
    }

    fun closePrescribedPopup(view: View) {
        popupPrescribed.cancel()
    }

    // designed game popup
    fun selectModeDesignedGame(view: View) {
        popupDesigned.show()
    }

    fun closeDesignedPopup(view: View) {
        popupDesigned.cancel()
    }

    fun startDesignedGame(view: View) {
        var design = Intent(this, DesignedGame::class.java)


        val radioButtonID: Int = designedSelection.numOfPairsGroup.checkedRadioButtonId
        val selectionButton: View = designedSelection.numOfPairsGroup.findViewById(radioButtonID)
        var pairs = designedSelection.numOfPairsGroup.indexOfChild(selectionButton) + 1

        design.putExtra("pairs", pairs)
        design.putExtra("random", designedSelection.switchRandom.isChecked)

        startActivity(design)
    }


    // Mode selection
    fun goalModeOnClick(view: View) {
        modeSelection.goalMode.setBackgroundResource(R.drawable.button_default)
        modeSelection.freeMode.setBackgroundResource(R.drawable.button_default_highlight)
        exerciseMode = true

        buttonState(true)
    }

    fun freeModeOnClick(view: View) {
        modeSelection.freeMode.setBackgroundResource(R.drawable.button_default)
        modeSelection.goalMode.setBackgroundResource(R.drawable.button_default_highlight)
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

        modeSelection.repetitions.setBackgroundResource(R.drawable.button_default)
        modeSelection.minutes.setBackgroundResource(R.drawable.button_default_highlight)

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

        modeSelection.minutes.setBackgroundResource(R.drawable.button_default)
        modeSelection.repetitions.setBackgroundResource(R.drawable.button_default_highlight)

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
}