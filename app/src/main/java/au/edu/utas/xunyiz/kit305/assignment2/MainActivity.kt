package au.edu.utas.xunyiz.kit305.assignment2

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import au.edu.utas.xunyiz.kit305.assignment2.databinding.ActivityMainBinding
import au.edu.utas.xunyiz.kit305.assignment2.databinding.ModeSelectionBinding
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.*

const val database_log = "DATABASE"
const val console_log = "CUSTOM_CHECK"

class MainActivity : AppCompatActivity() {

    private lateinit var ui: ActivityMainBinding
    private lateinit var modeSelection: ModeSelectionBinding

    private lateinit var popupPrescribedBuilder: AlertDialog.Builder
    private lateinit var popupPrescribed: AlertDialog

    var exerciseMode: Boolean = true
    var rounds: Int = -1


    var repetitionSpinnerItems = arrayOf(3, 4, 5, 6, 7, 8, 9, 10)

    @SuppressLint("SimpleDateFormat")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ui = ActivityMainBinding.inflate(layoutInflater)
        modeSelection = ModeSelectionBinding.inflate(layoutInflater)
        setContentView(ui.root)

        // set up the prescribed pop up
        popupPrescribedBuilder = AlertDialog.Builder(this)
        popupPrescribedBuilder.setView(modeSelection.root)
        popupPrescribed = popupPrescribedBuilder.create()

        modeSelection.goalSpinner.adapter = ArrayAdapter<Int> (
            this,
            android.R.layout.simple_spinner_dropdown_item,
            repetitionSpinnerItems
        )

        modeSelection.goalSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedItem = parent!!.getItemAtPosition(position).toString()
                rounds = parent!!.getItemAtPosition(position) as Int
                Log.d(console_log, selectedItem)
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
            }
        }

        modeSelection.goalSpinner.setSelection(2)

//        var buttonList = mutableListOf<Map<String, Int>>()
//
//        ui.button.setOnClickListener {
//            var currentTime = SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(Date())
//            currentTime = SimpleDateFormat("HH:mm:ss").format(Date())
//            Log.d(console_log, currentTime)
//            buttonList.add(mapOf(currentTime to 1))
//            printList(buttonList)
//        }

        modeSelection.goalMode.setOnClickListener {
            modeSelection.goalMode.setBackgroundColor(Color.GREEN)
            exerciseMode = true
        }

        modeSelection.freeMode.setOnClickListener {
            modeSelection.freeMode.setBackgroundColor(Color.GREEN)
            exerciseMode = false
        }
    }

    fun selectModePrescribedGame(view: View) {
        popupPrescribed.show()
    }

    fun closePrescribedPopup(view: View) {
        popupPrescribed.cancel()
    }

    fun goToCustomize(view: View) {
        var customize = Intent(this, Customization::class.java)
        customize.putExtra("ExerciseMode", exerciseMode)
        customize.putExtra("Rounds", rounds)
        startActivity(customize)
    }

    fun viewHistory(view: View) {
        var history = Intent(this, HistoryActivity::class.java)
        startActivity(history)
    }

    private fun printList(list: List<Map<String, Int>>) {
        for (item in list) {
            Log.d(console_log, item.toString())
        }
    }
}