package com.telenor.connect.id;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class ClaimsTest {

    @Test
    public void zeroArgumentsClaimsGivesEmptySet() {
        Claims claims = new Claims();

        assertThat(claims.getClaims().size(), is(0));
    }

    @Test
    public void duplicatesAreEliminated() {
        Claims claims = new Claims(Claims.PHONE_NUMBER, Claims.PHONE_NUMBER);

        assertThat(claims.getClaims().size(), is(1));
    }
}