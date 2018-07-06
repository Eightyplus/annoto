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
    fun save(context: Context, components: List<Component>, dataOutputStream: DataOutputStream) {
        try {
            JSONObject().let { jsonObject ->
                toJson(jsonObject, components)
                Storage.writeData(context, jsonObject.toString().toByteArray(), dataOutputStream)
            }
        } catch (e: JSONException) {
            Log.d(TAG, context.getString(R.string.log_error_exception), e)
        } catch (e: IOException) {
            Log.d(TAG, context.getString(R.string.log_error_exception), e)
        }
    }

    /**
     * Loads components from input stream
     * @param context the context
     * @param dataInputStream input stream to read from
     * @return list of loaded components
     * @throws IOException
     */
    @Throws(IOException::class)
    fun load(context: Context, dataInputStream: DataInputStream): List<Component> {
        try {
            val jsonObject = JSONObject(Storage.readData(dataInputStream))
            return fromJson(context, jsonObject)
        } catch (e: JSONException) {
            Log.d(TAG, context.getString(R.string.log_error_exception), e)
            throw IOException(e)
        } catch (e: IOException) {
            Log.d(TAG, context.getString(R.string.log_error_exception), e)
            throw e
        }
    }

    /**
     * Loads list of component from json object
     * @param context the context
     * @param jsonObject object containing list
     * @return components list to store components to
     * @throws JSONException
     */
    @Throws(JSONException::class)
    fun fromJson(context: Context, jsonObject: JSONObject): List<Component>{
        val jsonArray = jsonObject.getJSONArray(FileId.LIST)
        return List(jsonArray.length()) {
            val obj = jsonArray.get(it) as JSONObject
            val type = obj.getString(FileId.TYPE)

            when (ComponentType.valueOf(type)) {
                ComponentType.CompositeType -> Composite.fromJson(context, obj)
                ComponentType.PictureType -> try {
                    Picture.fromJson(context, obj).initialise()
                } catch (e: IOException) {
                    Log.d(TAG, context.getString(R.string.log_error_exception), e)
                    Text( context.getString(R.string.error_loading_image_user_message))
                }
                ComponentType.PolygonType -> Polygon.fromJson(obj)
                ComponentType.TextType -> Text.fromJson(obj)
            }
        }
    }

    /**
     * Deletes components and their associated files (photos etc.)
     * @param context the context
     * @param jsonString string representing a json object
     * @throws JSONException
     * @throws IOException
     */
    @Throws(JSONException::class, IOException::class)
    fun fromJsonDelete(context: Context, jsonString: String) {
        JSONObject(jsonString).let {
            val list = it.getJSONArray(FileId.LIST)
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
    }

    /**
     * Stores a list of components into a json object
     * @param jsonObject object to save list in
     * @param components list to save
     * @throws JSONException
     */
    @Throws(JSONException::class)
    fun toJson(jsonObject: JSONObject, components: List<Component>) {
        val list = JSONArray().apply {
            for (component in components) {
                put(component.toJson())
            }
        }
        jsonObject.put(FileId.SIZE, components.size)
        jsonObject.put(FileId.LIST, list)
    }
}
