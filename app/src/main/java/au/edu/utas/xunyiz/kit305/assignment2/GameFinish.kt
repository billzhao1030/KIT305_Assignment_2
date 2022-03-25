package au.edu.utas.xunyiz.kit305.assignment2

import android.Manifest
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.core.content.FileProvider
import au.edu.utas.xunyiz.kit305.assignment2.databinding.ActivityGameFinishBinding
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import java.io.File
import java.io.IOException
import java.net.URI
import java.text.SimpleDateFormat
import java.util.*

class GameFinish : AppCompatActivity() {
    private lateinit var ui: ActivityGameFinishBinding
    lateinit var ImageUri: Uri

    private var isTaken = false

    private var id = ""

    var isRound = true

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ui = ActivityGameFinishBinding.inflate(layoutInflater)
        setContentView(ui.root)

        id = intent.getStringExtra("ID").toString()
        isRound = intent.getBooleanExtra("isRound", true)
        Log.d(database_log, id)

        setupText()

        ui.imageView.visibility = View.GONE


        ui.takePic.setOnClickListener {
            if (!isTaken) {
                requestToTakeAPicture()

                isTaken = true

                ui.imageView.visibility = View.VISIBLE
                ui.takePic.text = "Back to Menu"
            } else{
                var menu = Intent(this, MainActivity::class.java)
                startActivity(menu)
            }
        }

        ui.selectImage.setOnClickListener {
            selectImage()
            isTaken = true

            ui.imageView.visibility = View.VISIBLE
            ui.takePic.text = "Back to Menu"
        }
    }

    private fun setupText() {
        val db = Firebase.firestore
        val games = db.collection("games")

        games.document(id)
            .get()
            .addOnSuccessListener { result ->
                if (result != null) {
                    val game = result.toObject<Game>()
                    Log.d(database_log, game.toString())

                    if (game != null) {
                        var s = ""

                        if (game.gameType == true) {
                            if (game.gameMode == true) {
                                s = if (isRound) game.toSummaryRound() else game.toSummaryTime()
                            } else {
                                s = game.toSummary()
                            }
                        } else {
                            s = game.toSummary()
                        }
                        ui.gameSummary.text = s
                    }
                }
            }
    }


    fun selectImage() {
        val gallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
        startActivityForResult(gallery, 100)
    }

    //step 4
    @RequiresApi(Build.VERSION_CODES.M)
    private fun requestToTakeAPicture() {
        requestPermissions(
            arrayOf(Manifest.permission.CAMERA),
            REQUEST_IMAGE_CAPTURE
        )
    }

    //step 5
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            REQUEST_IMAGE_CAPTURE -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    // Permission is granted.
                    takeAPicture()
                } else {
                    Toast.makeText(
                        this,
                        "Cannot access camera, permission denied",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    //step 6
    private fun takeAPicture() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

        //try {
        val photoFile: File = createImageFile()!!
        val photoURI: Uri = FileProvider.getUriForFile(
            this,
            "au.edu.utas.xunyiz.kit305.assignment2",
            photoFile
        )
        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
        startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
        //} catch (e: Exception) {}

    }

    //step 6 part 2
    lateinit var currentPhotoPath: String

    @Throws(IOException::class)
    private fun createImageFile(): File {
        // Create an image file name
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir: File = getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!
        return File.createTempFile(
            "JPEG_${timeStamp}_", /* prefix */
            ".jpg", /* suffix */
            storageDir /* directory */
        ).apply {
            // Save a file: path for use with ACTION_VIEW intents
            currentPhotoPath = absolutePath
        }
    }

    //step 7
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            setPic(ui.imageView)
            Log.d(console_log, currentPhotoPath)
            var file = Uri.fromFile(File(currentPhotoPath))

            val storage = Firebase.storage.reference.child("images/${id}.jpg")
            storage.putFile(file).
            addOnSuccessListener {
                Log.d(database_log, "file stored");
            }.addOnFailureListener {
                Log.d(database_log, "not stored");
            }
        } else if (requestCode == 100 && resultCode == RESULT_OK) {
            ImageUri = data?.data!!
            ui.imageView.setImageURI(ImageUri)

            val storage = Firebase.storage.reference.child("images/${id}.jpg")
            storage.putFile(ImageUri).
            addOnSuccessListener {
                Log.d(database_log, "file stored");
            }.addOnFailureListener {
                Log.d(database_log, "not stored");
            }
        }
    }

    //step 7 pt2
    private fun setPic(imageView: ImageView) {
        // Get the dimensions of the View
        val targetW: Int = imageView.measuredWidth
        val targetH: Int = imageView.measuredHeight

        val bmOptions = BitmapFactory.Options().apply {
            // Get the dimensions of the bitmap
            inJustDecodeBounds = true

            BitmapFactory.decodeFile(currentPhotoPath, this)

            val photoW: Int = outWidth
            val photoH: Int = outHeight

            // Determine how much to scale down the image
            val scaleFactor: Int = Math.max(1, Math.min(photoW / targetW, photoH / targetH))

            // Decode the image file into a Bitmap sized to fill the View
            inJustDecodeBounds = false
            inSampleSize = scaleFactor
        }
        BitmapFactory.decodeFile(currentPhotoPath, bmOptions)?.also { bitmap ->
            imageView.setImageBitmap(bitmap)
        }
    }
}