package dk.eightyplus.annoto.utilities

import android.content.Context
import android.util.Log
import dk.eightyplus.annoto.R
import dk.eightyplus.annoto.component.Component
import dk.eightyplus.annoto.component.ComponentType
import dk.eightyplus.annoto.component.Composite
import dk.eightyplus.annoto.component.Picture
import dk.eightyplus.annoto.component.Polygon
import dk.eightyplus.annoto.component.Text
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.IOException

/**
 * NoteStorage is a class to convert components to/from json and make use of this in a serialise context
 */
object NoteStorage {
    private val TAG = NoteStorage::class.java.toString()

    /**
     * Save list of components into output stream
     * @param context the context
     * @param components list of components
     * @param dataOutputStream output stream to write to
     * @return success
     */
    fun save(context: Context, components: List<Component>, dataOutputStream: DataOutputStream): Boolean {
        var result = false
        try {
            val jsonObject = JSONObject()
            toJson(jsonObject, components)
            Storage.writeData(context, jsonObject.toString().toByteArray(), dataOutputStream)
            result = true
        } catch (e: JSONException) {
            Log.d(TAG, context.getString(R.string.log_error_exception), e)
        } catch (e: IOException) {
            Log.d(TAG, context.getString(R.string.log_error_exception), e)
        }

        return result
    }

    /**
     * Loads components from input stream
     * @param context the context
     * @param components list to store components in
     * @param dataInputStream input stream to read from
     * @return success
     */
    fun load(context: Context, components: MutableList<Component>, dataInputStream: DataInputStream): Boolean {

        var result = false
        try {
            val jsonObject = JSONObject(Storage.readData(dataInputStream))
            fromJson(context, jsonObject, components)
            result = true
        } catch (e: JSONException) {
            Log.d(TAG, context.getString(R.string.log_error_exception), e)
        } catch (e: IOException) {
            Log.d(TAG, context.getString(R.string.log_error_exception), e)
        }

        return result
    }

    /**
     * Loads list of component from json object
     * @param context the context
     * @param object object containing list
     * @param components list to store components to
     * @throws JSONException
     */
    @Throws(JSONException::class)
    fun fromJson(context: Context, jsonObject: JSONObject, components: MutableList<Component>){
        val list = jsonObject.getJSONArray(FileId.LIST)
        loop@ for (i in 0 until list.length()) {
            val comp = list.get(i) as JSONObject

            val type = comp.getString(FileId.TYPE)

            val component: Component
            when (ComponentType.valueOf(type)) {
                ComponentType.CompositeType -> component = Composite.fromJson(context, comp)
                ComponentType.PictureType -> try {
                    component = Picture.fromJson(context, comp).initialise()
                } catch (e: IOException) {
                    Log.d(TAG, context.getString(R.string.log_error_exception), e)
                    continue@loop
                }

                ComponentType.PolygonType -> component = Polygon.fromJson(comp)
                ComponentType.TextType -> component = Text.fromJson(comp)
            }

            components.add(component)
        }
    }

    /**
     * Deletes components and their associated files (photos etc.)
     * @param context the context
     * @param dataInputStream input stream to load components from and delete
     * @throws JSONException
     * @throws IOException
     */
    @Throws(JSONException::class, IOException::class)
    fun fromJsonDelete(context: Context, dataInputStream: DataInputStream) {
        val jsonObject = JSONObject(dataInputStream.readUTF())
        val list = jsonObject.getJSONArray(FileId.LIST)
        for (i in 0 until list.length()) {
            val comp = list.get(i) as JSONObject
            val type = comp.getString(FileId.TYPE)

            when (ComponentType.valueOf(type)) {
                ComponentType.CompositeType -> Composite.fromJson(context, comp).delete()
                ComponentType.PictureType -> Picture.fromJson(context, comp).delete()
                ComponentType.PolygonType, ComponentType.TextType -> {}
            }
        }
    }

    /**
     * Stores a list of components into a json object
     * @param object object to save list in
     * @param components list to save
     * @throws JSONException
     */
    @Throws(JSONException::class)
    fun toJson(jsonObject: JSONObject, components: List<Component>) {
        val list = JSONArray()
        for (component in components) {
            list.put(component.toJson())
        }
        jsonObject.put(FileId.SIZE, components.size)
        jsonObject.put(FileId.LIST, list)
    }
}
