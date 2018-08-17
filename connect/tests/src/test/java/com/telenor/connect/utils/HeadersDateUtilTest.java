package com.telenor.connect.utils;

import org.junit.Test;
import org.robolectric.annotation.Config;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import okhttp3.Headers;

import static java.lang.Math.abs;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@Config(sdk = 18)
public class HeadersDateUtilTest {

    @Test(expected = NullPointerException.class)
    public void getThrowsOnNull() {
        HeadersDateUtil.extractDate(null);
    }

    @Test
    public void getReturnsNullOnEmpty() {
        Date actual = HeadersDateUtil.extractDate(new Headers.Builder().build());

        assertThat(actual, is(nullValue()));
    }

    @Test
    public void getReturnsDateObjectWhenPresent() {
        Headers headers = new Headers.Builder()
                .add("Something-Else", "Foo")
                .add("Not-Date", "Tue, 15 Nov 1264 08:12:31 GMT")
                .add("Date", "Tue, 15 Nov 1994 08:12:31 GMT")
                .build();

        Date actual = HeadersDateUtil.extractDate(headers);

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeZone(TimeZone.getTimeZone("GMT"));
        calendar.set(1994, Calendar.NOVEMBER, 15, 8, 12, 31);

        Date expected = calendar.getTime();

        assertThat(abs(actual.getTime()-expected.getTime()) < 1000 , is(true));
    }

}
