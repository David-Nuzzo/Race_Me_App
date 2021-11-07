package com.example.raceme;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
@SuppressWarnings("deprecation")
public class AllRoutesActivityTests
{
    @Test
    public void FormatThenSplit_isCorrect()
    {
        String startStr = "{Points=[lat/lng: (52.57907274847152,-0.2596871182322502), lat/lng: (52.57852693662698,-0.26039622724056244)]}";

        // Run the method and store the result.
        String[] actualResult = AllRoutesActivity.FormatThenSplit(startStr);

        // Store the expected result.
        String[] expectedResult = {"52.57907274847152","-0.2596871182322502","52.57852693662698","-0.26039622724056244"};

        // Determine whether the result is a success.
        assertEquals(expectedResult,actualResult);
    }
}