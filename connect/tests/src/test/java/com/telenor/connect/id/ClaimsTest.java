package com.telenor.connect.id;

import org.junit.Test;
import org.robolectric.annotation.Config;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

@Config(sdk = 18)
public class ClaimsTest {

    @Test
    public void zeroArgumentsClaimsGivesEmptySet() {
        Claims claims = new Claims();

        assertThat(claims.getClaimsAsSet().size(), is(0));
    }

    @Test
    public void duplicatesAreEliminated() {
        Claims claims = new Claims(Claims.PHONE_NUMBER, Claims.PHONE_NUMBER);

        assertThat(claims.getClaimsAsSet().size(), is(1));
    }
}