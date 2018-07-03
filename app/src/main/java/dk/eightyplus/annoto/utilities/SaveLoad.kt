package dk.eightyplus.annoto.utilities

import android.content.Context

import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.IOException

/**
 * User: fries
 * Date: 18/03/14
 * Time: 16.59
 */
interface SaveLoad {
    @Throws(IOException::class)
    fun save(context: Context, outputStream: DataOutputStream)

    @Throws(IOException::class, ClassNotFoundException::class)
    fun load(context: Context, inputStream: DataInputStream)
}
