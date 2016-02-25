package com.telenor.connect.id;

import com.telenor.connect.utils.Validator;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.rule.PowerMockRule;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.powermock.api.mockito.PowerMockito.doNothing;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = "src/main/AndroidManifest.xml", sdk = 18)
@PowerMockIgnore({ "org.mockito.*", "org.robolectric.*", "android.*" })
@PrepareForTest({Validator.class})
public class ConnectIdServiceTest {

    @Rule
    public PowerMockRule rule = new PowerMockRule(); // needed to activate PowerMock


    @Test
    public void getInstanceReturnsInstance() {
        mockStatic(Validator.class);
        doNothing().when(Validator.class);

        ConnectIdService instance = ConnectIdService.getInstance();
        assertThat(instance, instanceOf(ConnectIdService.class));
    }
}
