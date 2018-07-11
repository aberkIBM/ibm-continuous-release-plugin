package com.ibm.devops.connect.Endpoints;

import jenkins.model.Jenkins;
import com.ibm.devops.connect.DevOpsGlobalConfiguration;

import java.net.URL;
import java.net.MalformedURLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.mvc.UrlFilenameViewController;

public class EndpointsVelocity implements IEndpoints {
    private String logPrefix = "[EndpointsVelocity]";
	private static final Logger log = LoggerFactory.getLogger(EndpointsVelocity.class);
    // private static final String SYNC_API_ENPOINT = "https://velocity.us-south.containers.mybluemix.net/sync/";
    // private static final String SYNC_API_ENPOINT = "https://192.168.1.6:8090/reporting-sync-api/";
    // private static final String SYNC_API_ENPOINT = "http://192.168.1.6:3499/";
    private static final String SYNC_API_ENPOINT = "http://192.168.1.6:8071/";
    private static final String AMQP_ENPOINT = "https://velocity.us-south.containers.mybluemix.net/sync/";
    // private static final String SYNC_API_ENPOINT = "http://192.168.1.35:6002/";
    private static final String SYNC_STORE_ENPOINT = "https://bogus/";
    private static final String CONNECT_ENPOINT = "https://bogus";
    private static final String REPORTING_SYNC_PATH = "/reporting-sync-api/";

    public String getSyncApiEndpoint() {
        return getBaseUrl() + REPORTING_SYNC_PATH;
    }

    public String getSyncApiEndpoint(String baseUrl) {
        baseUrl = removeTrailingSlash(baseUrl);
        return baseUrl + REPORTING_SYNC_PATH;
    }

    public String getSyncStoreEndpoint() {
        return SYNC_STORE_ENPOINT;
    }

    public String getConnectEndpoint() {
        return CONNECT_ENPOINT;
    }

    public String getVelocityHostname() {
        try {
            String url = getBaseUrl();
            URL urlObj = new URL(url);
            return urlObj.getHost();
        } catch (MalformedURLException e) {
            log.error(logPrefix + "No valid URL was provided for Velocity: ", e);
        }
        return "";
    }

    private String getBaseUrl() {
        return removeTrailingSlash(Jenkins.getInstance().getDescriptorByType(DevOpsGlobalConfiguration.class).getBaseUrl());
    }

    private String removeTrailingSlash(String url) {
        if (url.endsWith("/")) {
            url = url.substring(0, url.length() - 1);
        }
        return url;
    }
}