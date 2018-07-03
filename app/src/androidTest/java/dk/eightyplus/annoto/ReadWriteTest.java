package dk.eightyplus.annoto;

import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;
import android.util.Log;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

import dk.eightyplus.annoto.action.State;
import dk.eightyplus.annoto.action.Undo;
import dk.eightyplus.annoto.component.Component;
import dk.eightyplus.annoto.view.DrawingView;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class ReadWriteTest {

    @Test
    public void readJsonFile() {

        String note = "file-2018-07-03_09-25-38.note";

        Callback callback = new Callback() {

            @Override
            public void textEditDone() {

            }

            @Override
            public void move(Component component, float dx, float dy, float scale) {

            }

            @Override
            public State getState() {
                return null;
            }

            @Override
            public void setState(State state) {

            }

            @Override
            public void setStrokeWidth(int width) {

            }

            @Override
            public void add(Undo undo) {

            }

            @Override
            public void load(String fileName) {

            }

            @Override
            public void colorChanged(int color) {

            }
        };

        try {
            InputStream inputStream = this.getClass().getClassLoader().getResource(note).openStream();
            DrawingView drawingView = new DrawingView(InstrumentationRegistry.getContext(), callback);
            drawingView.load(InstrumentationRegistry.getContext(), new DataInputStream(inputStream));

            assertThat("Read components should match size specified in file", drawingView.getNumComponents(), equalTo(14));

        } catch (IOException e) {
            Log.e("ERROR", "IOException", e);
            fail("IOException should not be thrown");
        } catch (ClassNotFoundException e) {
            Log.e("ERROR", "ClassNotFoundException", e);
            e.printStackTrace();
            fail("ClassNotFoundException should not be thrown");
        }

    }

}
