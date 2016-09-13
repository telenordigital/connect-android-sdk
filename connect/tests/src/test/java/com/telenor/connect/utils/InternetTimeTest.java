package com.telenor.connect.utils;

import org.junit.Test;

import java.util.Calendar;
import java.util.Date;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class InternetTimeTest {

    @Test
    public void internetTimeIsWithinBoundaryOfLocalTime() throws Exception {
        final Date internetDate = InternetTime.getInternetDate();
        final int boundarySeconds = 5;

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.SECOND, boundarySeconds);
        Date upperLimit = calendar.getTime();

        calendar.setTime(new Date());
        calendar.add(Calendar.SECOND, -boundarySeconds);
        Date lowerLimit = calendar.getTime();

        boolean withinLimit = internetDate.before(upperLimit) && internetDate.after(lowerLimit);
        assertThat(withinLimit, is(true));
    }
}
