package au.edu.utas.xunyiz.kit305.assignment2

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipDescription
import android.content.Intent
import android.content.res.Resources
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.util.TypedValue
import android.view.DragEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintSet
import au.edu.utas.xunyiz.kit305.assignment2.databinding.ActivityDesignedGameBinding
import au.edu.utas.xunyiz.kit305.assignment2.databinding.PauseDesignedBinding
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.*

class DesignedGame : AppCompatActivity(), View.OnLongClickListener {
    private lateinit var ui: ActivityDesignedGameBinding
    private lateinit var pause: PauseDesignedBinding

    private lateinit var pauseBuilder: AlertDialog.Builder
    private lateinit var pausePopup: AlertDialog

    var id = ""
    var completed = false

    var round = 0
    var numOfPairs = 1
    var isRandom = true

    var inBound = false
    var pairMade = 0


    var endTime = ""
    var buttonList = mutableListOf<Map<String, Int>>()

    var id_list = Array<Int>(numOfPairs*2, {i -> i})

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ui = ActivityDesignedGameBinding.inflate(layoutInflater)
        pause = PauseDesignedBinding.inflate(layoutInflater)
        setContentView(ui.root)

        // set the pause popup
        pauseBuilder = AlertDialog.Builder(this)
        pauseBuilder.setView(pause.root)
        pausePopup = pauseBuilder.create()
        pausePopup.setCanceledOnTouchOutside(false)

        numOfPairs = intent.getIntExtra("pairs", 2)
        isRandom = intent.getBooleanExtra("random", true)
        id_list = Array<Int>(numOfPairs*2, {i -> i})

        // get database connection
        var db = Firebase.firestore
        var games = db.collection("games")

        //get the id and set
        id = SimpleDateFormat("yyyyMMddHHmmss").format(Date())
        var currentTime = SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(Date())
        var dataMap = hashMapOf(
            "completed" to completed,
            "startTime" to currentTime,
            "endTime" to currentTime,
            "gameMode" to true,
            "gameType" to false,
            "repetition" to 0,
            "buttonList" to buttonList
        )
        games.document(id)
            .set(dataMap)
            .addOnSuccessListener { Log.d(database_log, "design new game") }
            .addOnFailureListener { Log.d(database_log, "design new game fail")}

        startDesignedGame()
    }

    @SuppressLint("SetTextI18n")
    private fun startDesignedGame() {

        for (button in 1..numOfPairs * 2) {
            var btn: Button = Button(this)
            btn.id = (button + 1) / 2 * 10 + 2 - button % 2
            Log.d(database_log, btn.id.toString())
            id_list[button-1] = btn.id


            Log.d(database_log, btn.text.toString())
            btn.setBackgroundResource(R.drawable.round_button)
            btn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24f)

            val metrics = Resources.getSystem().displayMetrics
            var scale:Int = (100 * (metrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)).toInt()
            btn.layoutParams = ViewGroup.LayoutParams(scale, scale)

            ui.designed.addView(btn)
        }

        var x_point = setLocationX()
        var y_point = setLocationY()

        for (button in 1..numOfPairs*2) {
            var setting = ConstraintSet()
            var mainWindow = ui.designed
            setting.clone(mainWindow)

            var marginStart: Int = getDP(x_point[button - 1])
            var marginBottom: Int = getDP(y_point[button - 1])

            setting.connect(id_list[button-1], ConstraintSet.START, ui.root.id, ConstraintSet.START, marginStart)
            setting.connect(id_list[button-1], ConstraintSet.BOTTOM, ui.root.id, ConstraintSet.BOTTOM, marginBottom)

            setting.applyTo(mainWindow)

            var btn: Button = findViewById<Button>(id_list[button-1])
            btn.setText("${(button+1)/2}")

            btn.setOnLongClickListener(this)
            btn.setOnDragListener(dragListener)
        }

        ui.progress.text = "Round ${round+1}"
    }

    private fun reposition(id_list: Array<Int>) {
        var x_point = setLocationX()
        var y_point = setLocationY()

        for (button in 1..numOfPairs*2) {
            if (isRandom) {
                var setting = ConstraintSet()
                var mainWindow = ui.designed
                setting.clone(mainWindow)

                var marginStart: Int = getDP(x_point[button - 1])
                var marginBottom: Int = getDP(y_point[button - 1])

                setting.connect(id_list[button-1], ConstraintSet.START, ui.root.id, ConstraintSet.START, marginStart)
                setting.connect(id_list[button-1], ConstraintSet.BOTTOM, ui.root.id, ConstraintSet.BOTTOM, marginBottom)

                setting.applyTo(mainWindow)
            }

            var btn: Button = findViewById<Button>(id_list[button-1])
            btn.setText("${(button+1)/2}")


            //btn.setOnLongClickListener(this)
            //btn.setOnDragListener(dragListener)
        }
    }

    private fun setLocationX(): Array<Int> {
        var listX = Array<Int>(numOfPairs*2, {i -> i * 1})
        val totalX = SCREEN_X
        var randomX: Int

        var numList = mutableListOf<Int>()

        for (i in 1..numOfPairs*2) {
            var random = (0..5).random() // shuffle
            while (numList.contains(random)) {
                random = (0..5).random()
            }
            numList.add(random)
            randomX = random * 110 + 8
            listX[i - 1] = randomX
        }

        return listX
    }
    private fun setLocationY(): Array<Int> {
        var listY = Array<Int>(numOfPairs*2, {i -> i * 1})
        val totalY = SCREEN_Y
        var randomY: Int

        var numList = mutableListOf<Int>()

        for (i in 1..numOfPairs*2) {
            var random = (0..5).random()
            while (numList.contains(random)) {
                random = (0..5).random()
            }
            numList.add(random)
            randomY = random * 110 + 32
            listY[i - 1] = randomY
        }

        return listY
    }

    @SuppressLint("SetTextI18n")
    val dragListener = View.OnDragListener { view, event ->
        val v = event.localState as View
        when(event.action) {
            DragEvent.ACTION_DRAG_STARTED -> {
                event.clipDescription.hasMimeType((ClipDescription.MIMETYPE_TEXT_PLAIN))
            }
            DragEvent.ACTION_DRAG_ENTERED -> {
                if (view.id / 10  != v.id /10) {
                    v.visibility = View.VISIBLE
                    inBound = false
                } else {
                    inBound = true
                }
                view.invalidate()
                true
            }
            DragEvent.ACTION_DRAG_LOCATION -> true
            DragEvent.ACTION_DRAG_EXITED -> {
                if (view.id / 10  != v.id /10) {
                    v.visibility = View.INVISIBLE
                    inBound = false
                }
                view.invalidate()

                true
            }
            DragEvent.ACTION_DROP -> {
                val item = event.clipData.getItemAt(0)
                val dragData = item.text
                //Toast.makeText(this, dragData, Toast.LENGTH_SHORT).show()

                view.invalidate()
                if (inBound == true) {
                    pairMade++
                }

                if (pairMade == numOfPairs) {
                    pairMade = 0
                    round++
                    ui.progress.text = "Round ${round+1}"

                    for (button in 1..numOfPairs*2) {
                        var btn: Button = findViewById<Button>(id_list[button-1])
                        btn.visibility = View.VISIBLE
                        uploadRound()
                    }
                    reposition(id_list)

                    completed = true
                    pause.goToMenuD.text = "Finish Exercise"
                }

                //v.visibility = View.VISIBLE
                true
            }
            DragEvent.ACTION_DRAG_ENDED -> {

                true
            }
            else -> false
        }
    }

    fun complete_game(view: View) {
        var db = Firebase.firestore
        var games = db.collection("games")

        var currentTime = SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(Date())

        games.document(id)
            .update("endTime", currentTime)
            .addOnSuccessListener { Log.d(database_log, "endtime update") }
            .addOnFailureListener { Log.d(database_log, "endtime not update")}
        games.document(id)
            .update("completed", completed)
            .addOnSuccessListener { Log.d(database_log, "endtime update") }
            .addOnFailureListener { Log.d(database_log, "endtime not update")}

        if (completed) {
            var finishGame = Intent(this, GameFinish::class.java)

            finishGame.putExtra("ID", id)
            finishGame.putExtra("completed", completed)

            startActivity(finishGame)
        } else {
            var menu = Intent(this, MainActivity::class.java)
            startActivity(menu)
        }
    }

    fun designPauseShow(view: View) {
        pausePopup.show()
    }

    fun designPauseClose(view: View) {
        pausePopup.cancel()
    }

    fun getDP(dp: Int) : Int {
        val metrics = Resources.getSystem().displayMetrics
        return (dp * (metrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)).toInt()
    }

    fun uploadRound() {
        var db = Firebase.firestore
        var games = db.collection("games")

        games.document(id)
            .update("repetition", round)
    }

    override fun onLongClick(view: View?): Boolean {
        val clipText = "Clip data"
        val item = ClipData.Item(clipText)
        val mimeTypes = arrayOf(ClipDescription.MIMETYPE_TEXT_PLAIN)
        val data = ClipData(clipText, mimeTypes, item)

        val dragShadowBuilder = View.DragShadowBuilder(view)
        if (view != null) {
            view.startDragAndDrop(data, dragShadowBuilder, view, 0)
        }

        if (view != null) {
            view.visibility = View.INVISIBLE
        }

        Log.d(database_log, view!!.id.toString())


        return true
    }
}