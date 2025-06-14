package com.example.myapplication1

import android.Manifest
import android.content.ContentUris
import android.content.ContentValues
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.webkit.MimeTypeMap
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.myapplication1.databinding.ActivityMainBinding

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


    private fun saveToDownloadText(text: String) {
        try {
            val fileName = "notes3"
            val resolver = applicationContext.contentResolver

            // 1. Удаляем старый файл, если он есть
            val query = resolver.query(
                MediaStore.Downloads.EXTERNAL_CONTENT_URI,
                arrayOf(MediaStore.Downloads._ID),
                "${MediaStore.Downloads.DISPLAY_NAME} = ?",
                arrayOf(fileName),
                null
            )

            val uriDel = query?.use { cursor ->
//                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Downloads._ID)
//                val listUri = mutableListOf<Uri>()
//                while (cursor.moveToNext()) {
//                    val id = cursor.getLong(idColumn)
//                    val contentUri = ContentUris.withAppendedId(
//                        MediaStore.Downloads.EXTERNAL_CONTENT_URI,
//                        id
//                    )
//                    listUri += contentUri
//                }
//
//                if (!listUri.isEmpty()) {
//                    listUri[0]
//                } else null

                if (cursor.moveToFirst()) {
                    ContentUris.withAppendedId(
                        MediaStore.Downloads.EXTERNAL_CONTENT_URI,
                        cursor.getLong(
                            cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID)
                        )
                    )
                } else null
            }

//            val numRowDel = uriDel?.let { uri ->
//                resolver.delete(
//                    uri,
//                    "${MediaStore.Downloads.DISPLAY_NAME} = ?",
//                    arrayOf(fileName)
//                )
//            }
//
//            if (numRowDel != null) {
//                Toast.makeText(this, "Row ${numRowDel} is deleted", Toast.LENGTH_LONG).show()
//            } else {
//                Toast.makeText(this, "Error deleted", Toast.LENGTH_LONG).show()
//            }

            var uriSave: Uri? = uriDel
            if (uriDel == null) {
                // 2. Создаём новый файл

                val ext = fileName.substringAfterLast('.', "")
                val mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext) ?: "*/*"

                val contentValues = ContentValues().apply {
                    put(MediaStore.Downloads.DISPLAY_NAME, fileName)
                    put(MediaStore.Downloads.MIME_TYPE, mimeType)
                    put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                }

                uriSave = resolver.insert(
                    MediaStore.Downloads.EXTERNAL_CONTENT_URI,
                    contentValues
                )
            }

            uriSave?.let { uri ->
                val outputStreamCr = resolver.openOutputStream(uri)

                outputStreamCr?.use { outputStream ->
                    outputStream.write(text.toByteArray())
                }
                Toast.makeText(this, "File saved to: ${uri.path}", Toast.LENGTH_LONG).show()
            }

        } catch (e: Exception) {
            e.printStackTrace()
            // Обработка ошибки
            Toast.makeText(this, "Error saving file: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

}
