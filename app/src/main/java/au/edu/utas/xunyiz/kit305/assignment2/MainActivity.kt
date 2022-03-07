package au.edu.utas.xunyiz.kit305.assignment2

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import au.edu.utas.xunyiz.kit305.assignment2.databinding.ActivityMainBinding
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.*

const val database_log = "DATABASE"
const val console_log = "CUSTOM_CHECK"

class MainActivity : AppCompatActivity() {

    private lateinit var ui: ActivityMainBinding

    @SuppressLint("SimpleDateFormat")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ui = ActivityMainBinding.inflate(layoutInflater)
        setContentView(ui.root)

        // Firebase connection
        val db = Firebase.firestore
        var games = db.collection("games")

        games
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    Log.d(database_log, document.id)
                }
            }

        var buttonList = mutableListOf<Map<String, Int>>()

        ui.button.setOnClickListener {
            var currentTime = SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(Date())
            currentTime = SimpleDateFormat("HH:mm:ss").format(Date())
            Log.d(console_log, currentTime)
            buttonList.add(mapOf(currentTime to 1))
            printList(buttonList)
        }
    }

    private fun printList(list: List<Map<String, Int>>) {
        for (item in list) {
            Log.d(console_log, item.toString())
        }
    }
}