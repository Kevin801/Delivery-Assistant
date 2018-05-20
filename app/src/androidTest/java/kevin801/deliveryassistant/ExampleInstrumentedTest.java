package kevin801.deliveryassistant;

import android.content.Context;
import android.graphics.Path;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.google.android.gms.common.api.GoogleApi;
import com.google.android.gms.location.places.Place;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;

import kevin801.deliveryassistant.maps.DirectionsJSONParser;
import kevin801.deliveryassistant.maps.list.DeliveriesListAdapter;
import kevin801.deliveryassistant.maps.list.Delivery;

import static org.junit.Assert.*;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {
    
    @Test
    public void useAppContext() {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getTargetContext();
        assertEquals("kevin801.deliveryhelper", appContext.getPackageName());
    }
}
