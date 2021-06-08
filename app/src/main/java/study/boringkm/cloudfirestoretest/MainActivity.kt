package study.boringkm.cloudfirestoretest

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.firebase.storage.FirebaseStorage
import java.io.File


@Suppress("DEPRECATION")
class MainActivity : AppCompatActivity() {

    private lateinit var storage: FirebaseStorage
    private val GET_GALLERY_IMAGE = 200
    private val STORAGE_PERM_CODE = 300
    private lateinit var imageViewToUpload: ImageView
    private lateinit var imageViewToDownload: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        imageViewToUpload = findViewById(R.id.uploadImageView)
        imageViewToDownload = findViewById(R.id.downloadedImageView)

        storage = FirebaseStorage.getInstance()

        findViewById<Button>(R.id.bringGalleryButton).setOnClickListener {
            checkPermission()
        }

        findViewById<Button>(R.id.downloadButton).setOnClickListener {
            loadImageView()
        }

    }

    private fun loadImageView() {
        val storageRef = storage.reference
        val testImageRef = storageRef.child("images/31997")
        val imageFile = File.createTempFile("images", "jpg")
        testImageRef.getFile(imageFile).addOnSuccessListener {
            val bitmap = BitmapFactory.decodeFile(imageFile.path)
            imageViewToDownload.setImageBitmap(bitmap)
            imageViewToDownload.visibility = View.VISIBLE
        }.addOnFailureListener {
            e -> e.printStackTrace()
        }
    }

    private fun checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            when (PackageManager.PERMISSION_GRANTED) {
                ContextCompat.checkSelfPermission(
                    applicationContext,
                    Manifest.permission.READ_EXTERNAL_STORAGE) -> {
                    getGallery()
                }
                else -> {
                    requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), STORAGE_PERM_CODE)
                }
            }
        } else {
            getGallery()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            STORAGE_PERM_CODE -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    Toast.makeText(applicationContext, "권한 생김", Toast.LENGTH_SHORT).show()
                } else {
                    getGallery()
                }
                return
            }
            else -> {
                Toast.makeText(applicationContext, "예상치 못한 권한 요청", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun getGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*")
        startActivityForResult(intent, GET_GALLERY_IMAGE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == GET_GALLERY_IMAGE && resultCode == RESULT_OK && data != null && data.data != null) {
            val selectedImageUri: Uri = data.data!!
            val storageRef = storage.reference
            val imageRef = storageRef.child("images/${selectedImageUri.lastPathSegment}")
            val uploadTask = imageRef.putFile(selectedImageUri)

            uploadTask.addOnFailureListener {
                Toast.makeText(applicationContext, "업로드 실패", Toast.LENGTH_SHORT).show()
            }.addOnSuccessListener {
                Toast.makeText(applicationContext, "업로드 성공", Toast.LENGTH_SHORT).show()
                imageViewToUpload.setImageURI(selectedImageUri)
                imageViewToUpload.visibility = View.VISIBLE
            }
        }
    }
}