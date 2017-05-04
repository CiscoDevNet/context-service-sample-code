package com.cisco.thunderhead;

import java.nio.file.Paths;

/**
 * Global properties
 */
public class SDKTestBase {
    public static String DEFAULT_CONNECTOR_PROPERTIES_PATH = Paths.get("./connector.property").toAbsolutePath().toString();
}
