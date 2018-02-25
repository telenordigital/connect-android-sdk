package com.telenor.connect.utils;

import com.telenor.connect.id.Claims;

import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 18)
public class ClaimsParameterFormatterTest {

    @Test
    public void asJsonReturnsOpenIdSpec() throws Exception {
        // http://openid.net/specs/openid-connect-core-1_0.html#ClaimsParameter
        String claimsJson = ClaimsParameterFormatter.asJson(new Claims(Claims.EMAIL));

        final String expectedJson = "{\n" +
                "\t\"userinfo\":\n" +
                "\t{\n" +
                "\t\t\"email\": {\"essential\": true}\n" +
                "\t}\n" +
                "}";

        assertThat(claimsJson, is(new JSONObject(expectedJson).toString()));
    }

    @Test
    public void asJsonAcceptsEmptyClaims() throws Exception {
        String claimsJson = ClaimsParameterFormatter.asJson(new Claims());

        final String expectedJson = "{\"userinfo\": {}}";

        assertThat(claimsJson, is(new JSONObject(expectedJson).toString()));
    }

    @Test
    public void multipleClaimsIsAccepted() throws Exception {
        String claimsJson = ClaimsParameterFormatter.asJson(new Claims(Claims.EMAIL, Claims.PHONE_NUMBER));

        final String expectedJson = "{\n" +
                "\t\"userinfo\":\n" +
                "\t{\n" +
                "\t\t\"email\": {\"essential\": true},\n" +
                "\t\t\"phone_number\": {\"essential\": true}\n" +
                "\t}\n" +
                "}";

        assertThat(claimsJson, is(new JSONObject(expectedJson).toString()));
    }
}