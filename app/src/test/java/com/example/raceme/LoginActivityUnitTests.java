package com.example.raceme;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class LoginActivityUnitTests
{
    @Test
    public void CheckCredentials_isCorrect()
    {
        // Run the method and store the result.
        String actualResult = LoginActivity.CheckCredentials("d.nuz@google.com","12356737");

        // Store the expected result.
        String expectedResult = "Passed";

        // Determine whether the result is a success.
        assertEquals(actualResult,expectedResult);
    }
}