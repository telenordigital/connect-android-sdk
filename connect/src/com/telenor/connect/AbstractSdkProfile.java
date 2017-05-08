package com.telenor.connect;

import android.content.Context;

import com.telenor.connect.id.ConnectIdService;
import com.telenor.connect.utils.RestHelper;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public abstract class AbstractSdkProfile implements SdkProfile {
    protected static ExecutorService sExecutor = Executors.newSingleThreadExecutor();

    private ConnectIdService connectIdService;
    private WellKnownAPI.WellKnownConfig wellKnownResult;

    protected Context context;
    protected boolean useStaging;
    protected boolean confidentialClient;

    public AbstractSdkProfile(
            Context context,
            boolean useStaging,
            boolean confidentialClient) {
        this.context = context;
        this.useStaging = useStaging;
        this.confidentialClient = confidentialClient;
    }

    @Override
    public Context getContext() {
        return context;
    }

    public WellKnownAPI.WellKnownConfig getWellKnownConfig() {
        if (wellKnownResult == null) {
            Future<WellKnownAPI.WellKnownConfig> wellKnownResultFuture =
                    sExecutor.submit(new Callable<WellKnownAPI.WellKnownConfig>() {
                        @Override
                        public WellKnownAPI.WellKnownConfig call() throws Exception {
                            return RestHelper.
                                    getWellKnownApi(getWellKnownEndpoint()).
                                    getWellKnownConfig();
                        }
                    });
            try {
                wellKnownResult = wellKnownResultFuture.get();
            } catch (InterruptedException | ExecutionException ignored) {
            }
        }
        return wellKnownResult;
    }

    @Override
    public boolean isConfidentialClient() {
        return confidentialClient;
    }

    @Override
    public ConnectIdService getConnectIdService() {
        return connectIdService;
    }

    public void setConnectIdService(ConnectIdService connectIdService) {
        this.connectIdService = connectIdService;
    }

    protected void deInitialize() {
        wellKnownResult = null;
    }

    protected abstract String getWellKnownEndpoint();
}
