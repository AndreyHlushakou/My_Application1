package com.example.myapplication1

import android.Manifest
import android.content.ContentUris
import android.content.ContentValues
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.documentfile.provider.DocumentFile
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.myapplication1.databinding.ActivityMainBinding
import java.io.File

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navView: BottomNavigationView = binding.navView


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        }

        val textView = findViewById<TextView>(R.id.textView)
        val button = findViewById<Button>(R.id.button)
        button.setOnClickListener {
            val str = "You clicked button =" + Math.random()*100
            textView.text = str
            saveToDownloadText(str)
        }

        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // Permission is granted
        } else {
            // Permission denied
        }
    }

//    private fun saveToDownloadText(text:String) {
//        val destPath = getExternalFilesDir(null)!!.absolutePath
//        val file = File(destPath, "notes.txt")
//        if (!file.exists()){
//            file.createNewFile()
//        } else {
//            file.delete()
//            file.createNewFile()
//        }
//        file.writeText(text)
//
//        val destPathDownload = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
//        val src = FileInputStream(file)
//        val dst = applicationContext.contentResolver.openOutputStream(destPathDownload.toUri())
//        src.copyTo(dst!!)
//        src.close()
//        dst.close()
//        file.delete()
//
//    }

    private fun saveToDownloadText(text: String) {
        try {
            val downloadsDir = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // Для Android 10+ используем MediaStore

                val fileName = "notes.txt"
                val mimeType = "application/octet-stream"
                // 1. Удаляем старый файл, если он есть
                val deleted = contentResolver.query(
                    MediaStore.Downloads.EXTERNAL_CONTENT_URI,
                    arrayOf(MediaStore.MediaColumns._ID),
                    "${MediaStore.MediaColumns.DISPLAY_NAME} = ?",
                    arrayOf(fileName),
                    null
                )?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        ContentUris.withAppendedId(
                            MediaStore.Downloads.EXTERNAL_CONTENT_URI,
                            cursor.getLong(0)
                        )
                    } else null
                }?.let { uri ->
                    contentResolver.delete(
                        uri,
                        null,
                        null)
                    DocumentFile.fromSingleUri(applicationContext, uri)?.delete()
                }


                // 2. Создаём новый файл
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                }

                contentResolver.insert(
                    MediaStore.Downloads.EXTERNAL_CONTENT_URI,
                    contentValues
                )?.let { uri ->
                    contentResolver.openOutputStream(uri)?.use { outputStream ->
                        outputStream.write(text.toByteArray())
                        Toast.makeText(this, "File saved to: ${uri.path}", Toast.LENGTH_LONG).show()
                    }
                }



                return
            } else {
                // Для старых версий Android
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            }

            if (downloadsDir != null && downloadsDir is File) {
                val file = File(downloadsDir, "notes.txt")
                file.writeText(text)
                Toast.makeText(this, "File saved to: ${file.absolutePath}", Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            // Обработка ошибки
            Toast.makeText(this, "Error saving file: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

//    private fun saveToDownloadText(text: String) {
//        try {
//            val file = File(getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "notes.txt")
//            file.writeText(text)
//
//            // Показать пользователю, где сохранен файл
//            Toast.makeText(this, "File saved to: ${file.absolutePath}", Toast.LENGTH_LONG).show()
//        } catch (e: Exception) {
//            e.printStackTrace()
//            Toast.makeText(this, "Error saving file: ${e.message}", Toast.LENGTH_LONG).show()
//        }
//    }

}
