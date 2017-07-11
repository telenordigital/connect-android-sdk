package com.telenor.connect;


import java.util.HashMap;
import java.util.Map;

public enum ConnectInitializationError {
    OPERATOR_SELECTION_ENDPOINT_DISCOVERY_FAILED,
    NO_OPERATOR_SELECTION_ENDPOINT_RETURNED,
    OPERATOR_DISCOVERY_ERROR,
    NO_MCC_MNC_RETURNED,
    UNSPECIFIED_INITIALIZATION_ERROR;
}
