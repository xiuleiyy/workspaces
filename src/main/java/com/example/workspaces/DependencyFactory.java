package com.example.workspaces;

import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.workspaces.WorkSpacesClient;

/**
 * The module containing all dependencies required by the {@link App}.
 */
public class DependencyFactory {

    private DependencyFactory() {}

    /**
     * @return an instance of WorkSpacesClient
     */
    public static WorkSpacesClient workSpacesClient() {
        return WorkSpacesClient.builder()
                .region(Region.AP_SOUTHEAST_1)
                .httpClientBuilder(UrlConnectionHttpClient.builder())
                .build();
    }

    public static Ec2Client ec2Client() {
        return Ec2Client.builder()
                .region(Region.AP_SOUTHEAST_1)
                .httpClientBuilder(UrlConnectionHttpClient.builder())
                .build();
    }

}
