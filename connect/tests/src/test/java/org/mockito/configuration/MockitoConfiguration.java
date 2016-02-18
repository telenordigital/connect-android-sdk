package org.mockito.configuration;

public class MockitoConfiguration extends DefaultMockitoConfiguration {

    /* This file fixes a ClassCastException being thrown when
     running Robolectric tests using PowerMock on multiple files.
    http://stackoverflow.com/q/33008255/2148380 */

    @Override
    public boolean enableClassCache() {
        return false;
    }
}