package com.ibm.devops.connect.Endpoints;

public interface IEndpoints {

    public String getSyncApiEndpoint();

    public String getSyncApiEndpoint(String baseUrl);

    public String getSyncStoreEndpoint();

    public String getConnectEndpoint();

    public String getVelocityHostname();

}