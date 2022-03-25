package au.edu.utas.xunyiz.kit305.assignment2

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import au.edu.utas.xunyiz.kit305.assignment2.databinding.ActivityHistoryBinding
import au.edu.utas.xunyiz.kit305.assignment2.databinding.ButtonListItemBinding
import au.edu.utas.xunyiz.kit305.assignment2.databinding.HistoryDetailsBinding
import au.edu.utas.xunyiz.kit305.assignment2.databinding.HistoryListItemBinding
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import java.io.File

class HistoryActivity : AppCompatActivity() {
    private lateinit var ui: ActivityHistoryBinding
    private lateinit var detail: HistoryDetailsBinding
    private lateinit var btnList: ButtonListItemBinding

    private lateinit var popupHistoryBuilder: AlertDialog.Builder
    private lateinit var popupHistory: AlertDialog

    var gamesList = mutableListOf<Game>()

    var historyNow = true

    var currentPosition = 0
    var currentID = ""

    var buttonClickList = mutableListOf<Map<String, Int>>()

    var undoGame = Game()
    var undoIndex = 1
    var undoId = ""

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ui = ActivityHistoryBinding.inflate(layoutInflater)
        detail = HistoryDetailsBinding.inflate(layoutInflater)
        btnList = ButtonListItemBinding.inflate(layoutInflater)
        setContentView(ui.root)

        ui.undoHint.visibility = View.INVISIBLE
        ui.undoYes.visibility = View.INVISIBLE
        ui.undoNo.visibility = View.INVISIBLE

        // set up detail pop up
        popupHistoryBuilder = AlertDialog.Builder(this)
        popupHistoryBuilder.setView(detail.root)
        popupHistory = popupHistoryBuilder.create()

        ui.historyBack.setOnClickListener {
            finish()
        }

        ui.historyList.adapter = HistoryAdapter(history = gamesList)
        ui.historyList.layoutManager = LinearLayoutManager(this)

        detail.buttonList.adapter = BtnListAdapter(list = buttonClickList)
        detail.buttonList.layoutManager = LinearLayoutManager(this)
        detail.noImage.visibility = View.INVISIBLE

        getGameFromDB()

        popupHistory.setOnCancelListener {
            detail.selfie.setImageResource(android.R.color.transparent)
            detail.noImage.visibility = View.INVISIBLE
            detail.selfie.visibility = View.VISIBLE
        }
    }

    inner class HistoryHolder(var ui: HistoryListItemBinding): RecyclerView.ViewHolder(ui.root) {}
    inner class HistoryAdapter(private val history: MutableList<Game>) : RecyclerView.Adapter<HistoryHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryHolder {
            val ui = HistoryListItemBinding.inflate(layoutInflater, parent, false)
            return HistoryHolder(ui)
        }

        @SuppressLint("SetTextI18n", "NotifyDataSetChanged")
        override fun onBindViewHolder(holder: HistoryHolder, position: Int) {
            val game = history[position]
            var str = ""

            if (historyNow) {
                str = if (game.gameMode==true) "Goal Mode" else "Free-play Mode"
            } else {
                str = "Free-play"
            }

            holder.ui.gameMode.text = "${position+1}. ${str}"
            holder.ui.startTime.text = "From: ${game.startTime}"
            holder.ui.endTime.text = "To: ${game.endTime}"
            holder.ui.repetitionHistory.text = "Repetition: ${game.repetition}"
            holder.ui.completeSituation.text = if (game.completed==true) "Completed" else "Not completed"

            holder.ui.root.setOnClickListener {
                currentPosition = position
                currentID = history[position].id.toString()

                buttonClickList = game.buttonList!!
                (detail.buttonList.adapter as BtnListAdapter).notifyDataSetChanged()

                if (undoId != "") {
                    undoNo(ui.undoNo)
                }

                showHistoryDetail()
            }
        }

        override fun getItemCount(): Int {
            return history.size
        }
    }

    inner class BtnListHolder(var ui: ButtonListItemBinding): RecyclerView.ViewHolder(ui.root) {}
    inner class BtnListAdapter(private val list: MutableList<Map<String, Int>>): RecyclerView.Adapter<BtnListHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BtnListHolder {
            val ui = ButtonListItemBinding.inflate(layoutInflater, parent, false)
            return BtnListHolder(ui)
        }

        @SuppressLint("SetTextI18n")
        override fun onBindViewHolder(holder: BtnListHolder, position: Int) {
            var buttonClick = buttonClickList[position]

            if (buttonClick.containsValue(10)|| buttonClick.containsValue(20) || buttonClick.containsValue(30) || buttonClick.containsValue(40) || buttonClick.containsValue(50)) {
                var str = "${buttonClick.keys} : Button ${buttonClick.values}"
                str = "${str.substring(0, str.length - 2)}] Incorrect"
                holder.ui.singleBtnClick.text = str
                holder.ui.singleBtnClick.setTextColor(Color.RED)
            } else {
                holder.ui.singleBtnClick.text = "${buttonClick.keys} : Button ${buttonClick.values}"
            }
        }

        override fun getItemCount(): Int {
            return list.size
        }

    }

    fun showHistoryDetail() {
        detail.buttonList.adapter = BtnListAdapter(list = buttonClickList)
        detail.buttonList.layoutManager = LinearLayoutManager(this)


        if (historyNow) {
            detail.clickSummary.visibility = View.VISIBLE
            detail.buttonList.visibility = View.VISIBLE
            detail.textView.visibility = View.VISIBLE
            var summaryClick = "Total button presses: ${gamesList[currentPosition].totalClick}\n" +
                    "Correct button presses: ${gamesList[currentPosition].rightClick}"
            detail.clickSummary.text = summaryClick
        } else {
            detail.clickSummary.visibility = View.GONE
            detail.buttonList.visibility = View.GONE
            detail.textView.visibility = View.GONE
        }

        if (gamesList[currentPosition].completed == true) {
            val storageRef = FirebaseStorage.getInstance().reference.child("images/${currentID}.jpg")
            val localFile = File.createTempFile( "tempFile", "jpg")

            storageRef.getFile(localFile).addOnSuccessListener {
                val targetW: Int = detail.selfie.measuredWidth
                val targetH: Int = detail.selfie.measuredHeight

                val bmOptions = BitmapFactory.Options().apply {
                    inJustDecodeBounds = true

                    BitmapFactory.decodeFile(localFile.absolutePath, this)

                    val photoW: Int = outWidth
                    val photoH: Int = outHeight

                    val scaleFactor: Int = Math.max(1, Math.min(photoW / targetW, photoH / targetH))

                    inJustDecodeBounds = false
                    inSampleSize = scaleFactor
                }

                BitmapFactory.decodeFile(localFile.absolutePath, bmOptions)?.also { bitmap ->
                    detail.selfie.setImageBitmap(bitmap)
                }
            }.addOnFailureListener {

            }
        } else {
            Log.d(database_log, " no image")
            detail.selfie.visibility = View.INVISIBLE
            detail.noImage.visibility = View.VISIBLE
        }

        popupHistory.show()
    }


    fun history1(view: View) {
        if (!historyNow) {
            historyNow = true
            getGameFromDB()
            ui.history1.setBackgroundResource(R.drawable.button_default)
            ui.history2.setBackgroundResource(R.drawable.button_default_highlight)
        }
    }

    fun history2(view: View) {
        if (historyNow) {
            historyNow = false
            getGameFromDB()
            ui.history2.setBackgroundResource(R.drawable.button_default)
            ui.history1.setBackgroundResource(R.drawable.button_default_highlight)
        }
    }


    private fun getGameFromDB() {
        val db = Firebase.firestore
        val games = db.collection("games")

        ui.loading.text = "Loading..."
        games
            .get()
            .addOnSuccessListener {  result ->
                gamesList.clear()
                (ui.historyList.adapter as HistoryAdapter).notifyDataSetChanged()
                Log.d(database_log, "----")
                for (document in result) {
                    var totalClick = 0
                    var rightClick = 0

                    val game = document.toObject<Game>()
                    game.id = document.id

                    if (game.gameType == historyNow) {
                        for (buttonClick in game.buttonList!!) {
                            totalClick++
                            if (!(buttonClick.containsValue(10) || buttonClick.containsValue(20) || buttonClick.containsValue(30) || buttonClick.containsValue(40) || buttonClick.containsValue(50))) {
                                rightClick++
                            }
                        }
                        game.rightClick = rightClick
                        game.totalClick = totalClick
                        gamesList.add(game)
                    }
                }

                (ui.historyList.adapter as HistoryAdapter).notifyDataSetChanged()
                ui.loading.text = "${gamesList.size} Exercise(s)"
            }
    }

    private fun closeHistoryDetail() {
        detail.selfie.setImageResource(android.R.color.transparent)
        popupHistory.cancel()
    }

    fun shareAll(view: View) {
        var games = ""

        for (game in gamesList) {
            games += "${game.toTable()}\n"
        }

        var sentCSV = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, games)
            type = "text/plain"
        }
        startActivity(Intent.createChooser(sentCSV, "Share via..."))
    }

    fun shareThis(view: View) {
        var game = gamesList[currentPosition].toTable()

        var sentCSV = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, game)
            type = "text/plain"
        }
        startActivity(Intent.createChooser(sentCSV, "Share via..."))
    }

    @SuppressLint("NotifyDataSetChanged")
    fun deleteThis(view: View) {
        val builder = AlertDialog.Builder(this)
        builder.setMessage("Are you sure you want to Delete?")
            .setCancelable(false)
            .setPositiveButton(
                "Yes",
                DialogInterface.OnClickListener { dialog, id ->
                    val db = Firebase.firestore
                    val games = db.collection("games")

                    games.document(currentID)
                        .delete()
                        .addOnSuccessListener {
                            Log.d(database_log, "document deleted")

                        }
                        .addOnFailureListener { Log.d(database_log, "document not deleted")}

                    undoGame = gamesList[currentPosition]
                    undoIndex = currentPosition
                    undoId = currentID
                    gamesList.removeAt(currentPosition)

                    closeHistoryDetail()

                    (ui.historyList.adapter as HistoryAdapter).notifyDataSetChanged()
                    ui.loading.text = "${gamesList.size} Exercise(s)"

                    ui.undoHint.visibility = View.VISIBLE
                    ui.undoYes.visibility = View.VISIBLE
                    ui.undoNo.visibility = View.VISIBLE
                })
            .setNegativeButton("No", DialogInterface.OnClickListener { dialog, id -> dialog.cancel() })
        val alert = builder.create()
        alert.show()


    }

    @SuppressLint("SetTextI18n")
    fun undoYes(view: View) {
        var game = undoGame

        var dataMap = hashMapOf(
            "completed" to game.completed,
            "startTime" to game.startTime,
            "endTime" to game.endTime,
            "gameMode" to game.gameMode,
            "gameType" to game.gameType,
            "repetition" to game.repetition,
            "buttonList" to game.buttonList
        )

        gamesList.add(undoIndex, undoGame)

        val db = Firebase.firestore
        val games = db.collection("games")
        games.document(game.id!!)
            .set(dataMap)
            .addOnSuccessListener { Log.d(database_log, "prescribe new game") }
            .addOnFailureListener { Log.d(database_log, "prescribe new game fail")}
        (ui.historyList.adapter as HistoryAdapter).notifyDataSetChanged()
        ui.loading.text = "${gamesList.size} Exercise(s)"

        ui.undoHint.visibility = View.INVISIBLE
        ui.undoYes.visibility = View.INVISIBLE
        ui.undoNo.visibility = View.INVISIBLE
        undoId = ""
    }

    fun undoNo(view: View) {
        ui.undoHint.visibility = View.INVISIBLE
        ui.undoYes.visibility = View.INVISIBLE
        ui.undoNo.visibility = View.INVISIBLE

        val storageRef = FirebaseStorage.getInstance().reference.child("images/${undoId}.jpg")
        storageRef.delete()

        undoId = ""
    }
}