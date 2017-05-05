package uk.co.joshcorne.cardashboard;

import android.content.Context;
import android.support.design.widget.TabLayout;

import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;

import uk.co.joshcorne.cardashboard.models.Journey;
import uk.co.joshcorne.cardashboard.models.Ping;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

/**
 * To work on unit tests, switch the Test Artifact in the Build Variants view.
 */
public class JourneyUnitTests {

    @Mock
    Journey journey;

    @Test
    public void getAvgSpeed_ReturnsCorrectly() throws Exception {
        //Setup
        MockitoAnnotations.initMocks(this);

        double avgSpeed = 123.45;
        when(journey.getAvgSpeed()).thenReturn(avgSpeed);

        //Return
        assertTrue(journey.getAvgSpeed() == avgSpeed);

    }
}