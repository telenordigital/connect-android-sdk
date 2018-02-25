package com.telenor.connect.utils;

import org.junit.Test;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import retrofit.client.Header;

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
    public void getReturnsNullOnEmptyList() {
        Date actual = HeadersDateUtil.extractDate(new ArrayList<Header>());

        assertThat(actual, is(nullValue()));
    }

    @Test
    public void getReturnsDateObjectWhenPresentInList() {
        List<Header> headers = new ArrayList<>();
        headers.add(new Header("Something-Else", "Foo"));
        headers.add(new Header("Not-Date", "Tue, 15 Nov 1264 08:12:31 GMT"));
        headers.add(new Header("Date", "Tue, 15 Nov 1994 08:12:31 GMT"));

        Date actual = HeadersDateUtil.extractDate(headers);

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeZone(TimeZone.getTimeZone("GMT"));
        calendar.set(1994, Calendar.NOVEMBER, 15, 8, 12, 31);

        Date expected = calendar.getTime();

        assertThat(abs(actual.getTime()-expected.getTime()) < 1000 , is(true));
    }

}
