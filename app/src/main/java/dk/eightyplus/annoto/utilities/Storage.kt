package dk.eightyplus.annoto.utilities

import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import dk.eightyplus.annoto.R
import org.json.JSONException
import java.io.*
import java.lang.ref.SoftReference
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream

/**
 * User: fries
 * Date: 18/03/14
 * Time: 16.49
 */
class Storage private constructor(context: Context) {

    private var context: Context = context.applicationContext ?: context

    val notes: Array<String>
        get() {
            return getFilename(null).list { _, filename ->
                filename.endsWith(context.getString(R.string.note_file_format, context.getString(R.string.empty)))
            }
        }

    @Throws(IOException::class)
    fun writeToFile(save: SaveLoad, fileName: String) {
        val file = getFilename(fileName)

        GZIPOutputStream(FileOutputStream(file)).use { gos ->
            DataOutputStream(gos).use {  dos ->
                save.save(context, dos)
            }
        }
    }

    fun deleteFile(fileName: String): Boolean {
        return getFilename(fileName).run {
            try {
                delete()
            } catch (e: IOException) {
                Log.d(TAG, context.getString(R.string.log_error_exception), e)
                false
            }
        }
    }

    fun deleteNoteAndThumb(fileName: String) {
        val file = getFilename(fileName)
        if (file.exists()) {
            deleteFromFile(file)
            file.delete()
        }
        val thumb = getThumb2Notes(fileName)
        if (thumb.exists()) {
            thumb.delete()
        }
    }

    @Throws(IOException::class, ClassNotFoundException::class)
    @JvmOverloads
    fun loadFromFile(load: SaveLoad, fileName: String, isAsset: Boolean = false) {
        val inputStream = if (isAsset)
            context.resources.assets.open(fileName)
        else
            FileInputStream(getFilename(fileName))

        inputStream.use { iStream ->
            GZIPInputStream(iStream).use { gis ->
                DataInputStream(gis).use { dis ->
                    load.load(context, dis)
                }
            }
        }
    }

    private fun deleteFromFile(file: File) {
        try {
            GZIPInputStream(FileInputStream(file)).use { gis ->
                DataInputStream(gis).use { dis ->
                    readData(dis)?.let {
                        NoteStorage.fromJsonDelete(context, it)
                    }
                }
            }
        } catch (e: Exception) {
            Log.d(TAG, context.getString(R.string.log_error_exception), e)
        }
    }

    @Throws(IOException::class)
    @JvmOverloads
    fun writeToFile(bitmap: Bitmap, fileName: String = "image.png", quality: Int = 90): File {
        return getFilename(fileName).apply {
            DataOutputStream(FileOutputStream(this)).use { dos ->
                bitmap.compress(Bitmap.CompressFormat.PNG, quality, dos)
            }
        }
    }

    @JvmOverloads
    fun loadFromFile(fileName: String = "image.png"): Bitmap? {
        val file = getFilename(fileName)
        return if (file.exists()) {
            BitmapFactory.decodeFile(file.absolutePath)
        } else null
    }

    @Throws(IOException::class)
    fun loadBitmapFromIntent(context: ContextWrapper, data: Intent, sampleSize: Int): Bitmap {
        Uri.parse(data.dataString).let { contentURI ->
            context.contentResolver.openInputStream(contentURI).use { inputStream ->
                val options = BitmapFactory.Options().apply {
                    inSampleSize = sampleSize
                }
                return BitmapFactory.decodeStream(inputStream, null, options)
            }
        }
    }

    fun getThumb2Notes(fileName: String): File {
        val prefix = fileName.substring(0, fileName.lastIndexOf('.'))
        return getFilename(String.format("%s.png", prefix))
    }

    fun getFilename(filename: String? = ""): File {
        val applicationPath = context.getExternalFilesDir(null)
        return File(applicationPath, File.separator + (filename ?: ""))
    }

    companion object {
        private val TAG = Storage::class.java.toString()

        private var storageReference: SoftReference<Storage>? = null

        fun getStorage(context: Context): Storage {
            return storageReference?.get() ?: createStorage(context)
        }

        @Synchronized
        private fun createStorage(context: Context): Storage {
            return Storage(context).apply {
                storageReference = SoftReference(this)
            }
        }

        @Throws(IOException::class)
        fun writeData(context: Context, data: ByteArray, out: OutputStream) {
            val buffer = 8192
            try {
                var offset = 0
                while (true) {
                    var length = data.size - offset

                    if (length <= 0) {
                        break
                    } else if (length > buffer) {
                        length = buffer
                    }

                    out.write(data, offset, length)
                    offset += length
                }
            } catch (e: IOException) {
                Log.d(TAG, context.getString(R.string.log_error_exception), e)
                throw e
            }

        }

        @Throws(IOException::class)
        fun readData(inputStream: InputStream?): String? {
            return inputStream?.use { iStream ->
                ByteArrayOutputStream().use { baos ->
                    iStream.copyTo(baos)
                    String(baos.toByteArray())
                }
            }
        }
    }
}
