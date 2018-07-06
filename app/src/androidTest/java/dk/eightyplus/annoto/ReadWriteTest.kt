package dk.eightyplus.annoto

import android.Manifest
import android.support.test.InstrumentationRegistry
import android.support.test.rule.GrantPermissionRule
import android.support.test.runner.AndroidJUnit4
import android.util.Log
import dk.eightyplus.annoto.action.State
import dk.eightyplus.annoto.action.Undo
import dk.eightyplus.annoto.component.Component
import dk.eightyplus.annoto.utilities.Storage
import dk.eightyplus.annoto.view.DrawingView
import org.hamcrest.Matchers.equalTo
import org.junit.Assert.assertThat
import org.junit.Assert.fail
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.*
import java.util.zip.GZIPInputStream

@RunWith(AndroidJUnit4::class)
class ReadWriteTest {

    @Rule @JvmField
    val mRuntimePermissionRule: GrantPermissionRule =
            GrantPermissionRule.grant(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            )

    private val callback = object : Callback {
        override fun hexColorForResourceId(color: Int): Int {
            return InstrumentationRegistry.getContext().getColor(color)
        }

        override var state: State = State.DrawPath

        override fun textEditDone() {

        }

        override fun move(component: Component, dx: Float, dy: Float, scale: Float) {

        }

        override fun setStrokeWidth(width: Int) {

        }

        override fun add(undo: Undo) {

        }

        override fun load(fileName: String) {

        }

        override fun colorChanged(color: Int) {

        }
    }

    @Test
    fun readJsonFile() {

        val note = "file-2018-07-03_09-25-38.note"
        val testNote = "file-test-out-put.note"

        try {
            val context = InstrumentationRegistry.getContext()
            val inputStream = context.resources.assets.open(note)
            val drawingView = DrawingView(context, callback)
            drawingView.load(context, DataInputStream(inputStream))

            assertThat("Read components should match size specified in file", drawingView.numComponents, equalTo(14))

            val jsonString = Storage.readData(context.resources.assets.open(note))
            val file = File(context.getExternalFilesDir(null), File.separator + testNote)
            drawingView.save(context, DataOutputStream(FileOutputStream(file)))

            FileInputStream(file).use {
                val result = Storage.readData(it) ?: throw IOException("not not written during test")
                assertThat("read should equal written json", result, equalTo(jsonString))
            }

        } catch (e: IOException) {
            Log.e("ERROR", "IOException", e)
            fail("IOException should not be thrown")
        } catch (e: ClassNotFoundException) {
            Log.e("ERROR", "ClassNotFoundException", e)
            e.printStackTrace()
            fail("ClassNotFoundException should not be thrown")
        }

    }

    @Test
    fun readGzippedJsonFile() {
        val note = "file-2018-07-03_09-22-01.note"
        val testNote = "file-test-out-put.gzip"

        try {
            val context = InstrumentationRegistry.getContext()
            val drawingView = DrawingView(context, callback)
            Storage.getStorage(context).loadFromFile(drawingView, note, true)
            assertThat("Read components should match size specified in file", drawingView.numComponents, equalTo(5))

            val jsonString = Storage.readData(GZIPInputStream(context.resources.assets.open(note)))
            Storage.getStorage(context).writeToFile(drawingView, testNote)

            val file = File(context.getExternalFilesDir(null), File.separator + testNote)
            FileInputStream(file).use {
                GZIPInputStream(it).use {
                    val result = Storage.readData(it) ?: throw IOException("not not written during test")
                    assertThat("read should equal written json", result, equalTo(jsonString))
                }
            }
        } catch (e: IOException) {
            Log.e("ERROR", "IOException", e)
            fail("IOException should not be thrown")
        } catch (e: ClassNotFoundException) {
            Log.e("ERROR", "ClassNotFoundException", e)
            e.printStackTrace()
            fail("ClassNotFoundException should not be thrown")
        }

    }

}
