package com.example.myapplication1

import android.Manifest
import android.content.ContentUris
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.util.Log
import android.webkit.MimeTypeMap
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.myapplication1.databinding.ActivityMainBinding
import java.io.File
import java.io.FileNotFoundException
import java.util.Date

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navView: BottomNavigationView = binding.navView


        if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }

        val textView = findViewById<TextView>(R.id.textView)
        val button = findViewById<Button>(R.id.button)
        button.setOnClickListener {
            val str = "You clicked button =" + Math.random()*100
            textView.text = str
            saveToDownloadText(str)
        }

        val textView2 = findViewById<TextView>(R.id.textView2)
        val button2 = findViewById<Button>(R.id.button2)
        button2.setOnClickListener {
            val str = "You clicked button2 =" + Math.random()*100
            textView2.text = str
            checkMap()
            debugMediaStore()
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
            val fileName = "SKB378_31.pdf"
            val resolver = contentResolver

            // 1. Ищем старый файл, если он есть
            val query = resolver.query(
                MediaStore.Downloads.EXTERNAL_CONTENT_URI,
                arrayOf(MediaStore.Downloads._ID),
                "${MediaStore.Downloads.DISPLAY_NAME} = ?",
                arrayOf(fileName),
                null
            )

            val uriDel = query?.use { cursor ->
                if (cursor.moveToFirst()) {
                    ContentUris.withAppendedId(
                        MediaStore.Downloads.EXTERNAL_CONTENT_URI,
                        cursor.getLong(
                            cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID)
                        )
                    )
                } else null
            }

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


    private fun checkMap() {

        try {
            val resolver = applicationContext.contentResolver
            val msUri = MediaStore.Downloads.EXTERNAL_CONTENT_URI
            val msId = MediaStore.Downloads._ID
            val msDn = MediaStore.Downloads.DISPLAY_NAME

            val query = resolver.query(
                msUri,
                arrayOf(
                    msId,
                    msDn
                ),
                null,
                null,
                null
            )

            val listUri = mutableListOf<Uri>()
            val listNames = mutableListOf<String>()

            query?.use { cursor ->
                val idColumn = cursor.getColumnIndexOrThrow(msId)
                val displayName = cursor.getColumnIndexOrThrow(msDn)

                while (cursor.moveToNext()) {
                    val contentUri = ContentUris.withAppendedId(
                        msUri,
                        cursor.getLong(idColumn)
                    )
                    val name = cursor.getString(displayName)

                    listUri += contentUri
                    listNames += name
                }

            }

            if (listUri.isEmpty()) {}

        } catch (e: Exception) {
            e.printStackTrace()
            // Обработка ошибки
            Toast.makeText(this, "Error saving file: ${e.message}", Toast.LENGTH_LONG).show()
        }

    }

    private fun debugMediaStore() {
        try {
            val resolver = applicationContext.contentResolver

            // Проверяем несколько разных типов медиа
            val collections = mapOf(
                "Downloads" to MediaStore.Downloads.EXTERNAL_CONTENT_URI,
                "Images" to MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                "Documents" to MediaStore.Files.getContentUri("external")
            )

            collections.forEach { (name, uri) ->
                Log.d("MediaStoreDebug", "=== Scanning $name ===")

                val projection = arrayOf(
                    MediaStore.MediaColumns._ID,
                    MediaStore.MediaColumns.DISPLAY_NAME,
                    MediaStore.MediaColumns.RELATIVE_PATH,
                    MediaStore.MediaColumns.DATE_MODIFIED,
                    MediaStore.MediaColumns.SIZE
                )

                val query = resolver.query(
                    uri,
                    projection,
                    null,
                    null,
//                    "${MediaStore.MediaColumns.DATE_MODIFIED} DESC LIMIT 100" // Последние 100 файлов
                    null
                )

                query?.use { cursor ->
                    val idCol = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID)
                    val nameCol = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME)
                    val pathCol = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.RELATIVE_PATH)
                    val dateCol = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATE_MODIFIED)
                    val sizeCol = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.SIZE)

                    while (cursor.moveToNext()) {
                        val fileUri = ContentUris.withAppendedId(uri, cursor.getLong(idCol))
                        val filePath = cursor.getString(pathCol) ?: ""
                        val fileName = cursor.getString(nameCol)
                        val fileDate = Date(cursor.getLong(dateCol) * 1000)
                        val fileSize = cursor.getLong(sizeCol)

                        // Проверяем, существует ли реальный файл
                        val exists = try {
                            resolver.openInputStream(fileUri) != null
                        } catch (e: Exception) {
                            false
                        }

                        Log.d("MediaStoreDebug",
                            """
                        URI: $fileUri
                        Path: $filePath$fileName
                        Date: $fileDate
                        Size: $fileSize
                        Exists: $exists
                        ----------------------
                        """.trimIndent())
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("MediaStoreDebug", "Error scanning MediaStore", e)
        }
    }

}
