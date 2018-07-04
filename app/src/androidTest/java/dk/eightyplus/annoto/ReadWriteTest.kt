package dk.eightyplus.annoto

import android.support.test.InstrumentationRegistry
import android.support.test.runner.AndroidJUnit4
import android.test.suitebuilder.annotation.LargeTest
import android.util.Log

import org.junit.Test
import org.junit.runner.RunWith

import java.io.DataInputStream
import java.io.IOException
import java.io.InputStream

import dk.eightyplus.annoto.action.State
import dk.eightyplus.annoto.action.Undo
import dk.eightyplus.annoto.component.Component
import dk.eightyplus.annoto.utilities.Storage
import dk.eightyplus.annoto.view.DrawingView

import org.hamcrest.Matchers.equalTo
import org.junit.Assert.assertThat
import org.junit.Assert.fail

@LargeTest
@RunWith(AndroidJUnit4::class)
class ReadWriteTest {

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

        try {
            val inputStream = InstrumentationRegistry.getContext().resources.assets.open(note)
            //InputStream inputStream = this.getClass().getClassLoader().getResource(note).openStream();
            val drawingView = DrawingView(InstrumentationRegistry.getContext(), callback)
            drawingView.load(InstrumentationRegistry.getContext(), DataInputStream(inputStream))

            assertThat("Read components should match size specified in file", drawingView.numComponents, equalTo(14))

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

        try {
            val drawingView = DrawingView(InstrumentationRegistry.getContext(), callback)
            Storage.getStorage(InstrumentationRegistry.getContext()).loadFromFile(drawingView, note, true)
            assertThat("Read components should match size specified in file", drawingView.numComponents, equalTo(5))
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
