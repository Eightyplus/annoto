package dk.eightyplus.annoto.utilities

import android.content.ContentValues
import android.content.Context
import android.content.ContextWrapper
import android.content.CursorLoader
import android.content.Intent
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import dk.eightyplus.annoto.R
import org.json.JSONException

import java.io.ByteArrayOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.FilenameFilter
import java.io.IOException
import java.io.InputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.io.OutputStream
import java.io.StreamCorruptedException
import java.lang.ref.SoftReference
import java.lang.reflect.Field
import java.util.ArrayList
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream

/**
 * User: fries
 * Date: 18/03/14
 * Time: 16.49
 */
class Storage private constructor(
        /** The application context.  */
        protected var context: Context) {

    val notes: Array<String>
        get() {
            val root = getFilename(null)

            return root.list { dir, filename -> filename.endsWith(context.getString(R.string.note_file_format, context.getString(R.string.empty))) }
        }

    @Throws(IOException::class)
    fun writeToFile(save: SaveLoad, fileName: String) {
        val file = getFilename(fileName)

        val outputStream = GZIPOutputStream(FileOutputStream(file))
        val dataOutputStream = DataOutputStream(outputStream)
        save.save(context, dataOutputStream)
        outputStream.flush()
        dataOutputStream.close()
        outputStream.close()
    }

    fun deleteFile(fileName: String): Boolean {
        val file = getFilename(fileName)
        return if (file.exists()) {
            file.delete()
        } else false
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
        val inputStream: GZIPInputStream
        if (isAsset) {
            inputStream = GZIPInputStream(context.resources.assets.open(fileName))
        } else {
            val file = getFilename(fileName)
            inputStream = GZIPInputStream(FileInputStream(file))
        }
        val dataInputStream = DataInputStream(inputStream)
        load.load(context, dataInputStream)
        dataInputStream.close()
        inputStream.close()
    }

    private fun deleteFromFile(file: File) {
        try {
            val inputStream = GZIPInputStream(FileInputStream(file))
            val dataInputStream = DataInputStream(inputStream)

            NoteStorage.fromJsonDelete(context, dataInputStream)
        } catch (e: IOException) {

        } catch (e: JSONException) {

        }

    }

    @Throws(IOException::class)
    @JvmOverloads
    fun writeToFile(bitmap: Bitmap, fileName: String = "image.png", quality: Int = 90): File {
        val file = getFilename(fileName)
        //  = File.createTempFile("image", ".png", context.getCacheDir());
        val out = DataOutputStream(FileOutputStream(file))
        bitmap.compress(Bitmap.CompressFormat.PNG, quality, out)
        out.close()
        return file
    }

    @JvmOverloads
    fun loadFromFile(fileName: String = "image.png"): Bitmap? {
        val file = getFilename(fileName)
        var bitmap: Bitmap? = null
        if (file.exists()) {
            bitmap = BitmapFactory.decodeFile(file.absolutePath)
        }
        return bitmap
    }

    @Throws(IOException::class)
    fun loadBitmapFromIntent(context: ContextWrapper, data: Intent, sampleSize: Int): Bitmap {
        val contentURI = Uri.parse(data.dataString)
        val inputStream = context.contentResolver.openInputStream(contentURI)
        val options = BitmapFactory.Options()
        options.inSampleSize = sampleSize
        return BitmapFactory.decodeStream(inputStream, null, options)
    }

    fun getThumb2Notes(fileName: String): File {
        val prefix = fileName.substring(0, fileName.lastIndexOf('.'))
        return getFilename(String.format("%s.png", prefix))
    }

    fun getFilename(filename: String?): File {
        val applicationPath = context.getExternalFilesDir(null)
        return if (filename == null) {
            File(applicationPath, File.separator)
        } else File(applicationPath, File.separator + filename)
    }

    companion object {
        private val TAG = Storage::class.java.toString()

        private var storageReference: SoftReference<Storage>? = null

        fun getStorage(context: Context): Storage {
            return storageReference?.get() ?: createStorage(context)
        }

        @Synchronized
        private fun createStorage(context: Context): Storage {
            val storage = Storage(context.applicationContext ?: context)
            storageReference = SoftReference(storage)
            return storage
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
            if (inputStream == null) {
                return null
            }
            try {
                val baos = ByteArrayOutputStream()
                var read = true

                while (read) {
                    val byteValue = inputStream.read()
                    read = byteValue != -1
                    if (read) {
                        baos.write(byteValue)
                    }
                }

                return String(baos.toByteArray())
            } finally {
                inputStream.close()
            }
        }
    }
}
