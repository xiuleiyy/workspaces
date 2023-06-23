package com.example.workspaces;

import software.amazon.awssdk.services.ec2.model.DescribeNetworkInterfacesRequest;
import software.amazon.awssdk.services.ec2.model.DescribeNetworkInterfacesResponse;
import software.amazon.awssdk.services.ec2.model.Filter;
import software.amazon.awssdk.services.workspaces.WorkSpacesClient;
import software.amazon.awssdk.services.workspaces.model.*;

import java.util.Collections;
import software.amazon.awssdk.services.workspaces.model.DescribeWorkspaceBundlesResponse;
import software.amazon.awssdk.services.workspaces.model.CreateWorkspacesResponse;
import software.amazon.awssdk.services.ec2.Ec2Client;

public class App {
    private final WorkSpacesClient workSpacesClient;
    private final Ec2Client ec2Client;

    private static final String EC2_IMAGE_ID = "ami-xxxxxx";
    private static final String DIRECTORY_ID = "d-xxxxxxx";
    private static final String USER_NAME = "helload";

    public App() {
        // Initialize the SDK client outside of the handler method so that it can be reused for subsequent invocations.
        // It is initialized when the class is loaded.
        workSpacesClient = DependencyFactory.workSpacesClient();
        ec2Client = DependencyFactory.ec2Client();
    }

    private ImportWorkspaceImageResponse importWorkspaceImage(
            final String sourceEc2ImageId,
            final String workspaceImageName,
            final String workspaceImageDescription) {
        
        ImportWorkspaceImageRequest importWorkspaceImageRequest = ImportWorkspaceImageRequest.builder()
                .ec2ImageId(sourceEc2ImageId)
                .ingestionProcess(WorkspaceImageIngestionProcess.BYOL_REGULAR_BYOP)
                .imageName(workspaceImageName)
                .imageDescription(workspaceImageDescription)
                .build();
        
        return workSpacesClient.importWorkspaceImage(importWorkspaceImageRequest);
    }

    private DescribeWorkspaceImagesResponse describeWorkspaceImages(
            final String imageId){
        
        DescribeWorkspaceImagesRequest describeWorkspaceImagesRequest = DescribeWorkspaceImagesRequest.builder()
                .imageIds(Collections.singletonList(imageId))
                .build();

        return workSpacesClient.describeWorkspaceImages(describeWorkspaceImagesRequest);
    }

    private CreateWorkspacesResponse createWorkspaces(
            final String bundleId,
            final String directoryId,
            final String userName) {

        WorkspaceRequest workspaceRequest = WorkspaceRequest.builder()
                .bundleId(bundleId)
                .directoryId(directoryId)
                .userName(userName)
                .build();
        CreateWorkspacesRequest createWorkspacesRequest = CreateWorkspacesRequest.builder()
                .workspaces(Collections.singletonList(workspaceRequest))
                .build();

        return workSpacesClient.createWorkspaces(createWorkspacesRequest);
    }

    private DescribeWorkspacesResponse describeWorkspaces(
            final String workspaceId){

        DescribeWorkspacesRequest describeWorkspacesRequest = DescribeWorkspacesRequest.builder()
                .workspaceIds(Collections.singletonList(workspaceId))
                .build();

        return workSpacesClient.describeWorkspaces(describeWorkspacesRequest);
    }

    private CreateWorkspaceBundleResponse createWorkspaceBundle(
        final String imageId,
        final String bundleName,
        final String bundleDescription,
        final Compute computeType,
        final int userStorage,
        final int rootStorage){

        CreateWorkspaceBundleRequest createWorkspaceBundleRequest = CreateWorkspaceBundleRequest.builder()
                .imageId(imageId)
                .bundleName(bundleName)
                .bundleDescription(bundleDescription)
                .computeType(ComputeType.builder().name(computeType).build())
                .userStorage(UserStorage.builder().capacity(String.valueOf(userStorage)).build())
                .rootStorage(RootStorage.builder().capacity(String.valueOf(rootStorage)).build())
                .build();

        return workSpacesClient.createWorkspaceBundle(createWorkspaceBundleRequest);
    }

    private DescribeWorkspaceBundlesResponse describeWorkspaceBundles(
        final String bundleId){

        DescribeWorkspaceBundlesRequest describeWorkspaceBundlesRequest = DescribeWorkspaceBundlesRequest.builder()
                .bundleIds(Collections.singletonList(bundleId))
                .build();

        return workSpacesClient.describeWorkspaceBundles(describeWorkspaceBundlesRequest);
    }

    private DescribeNetworkInterfacesResponse describeNetworkInterfacesByPrivateIp (
            final String privateIp) {

        DescribeNetworkInterfacesRequest describeNetworkInterfacesRequest = DescribeNetworkInterfacesRequest.builder()
                .filters(Collections.singletonList(Filter.builder().name("private-ip-address").values(privateIp).build()))
                .build();
        
        return ec2Client.describeNetworkInterfaces(describeNetworkInterfacesRequest);
    }

    public static void main(String[] args) throws InterruptedException {
        App app = new App();

        System.out.println("Import EC2 Image to WorkSpaces: " + EC2_IMAGE_ID);

        ImportWorkspaceImageResponse response = app.importWorkspaceImage(EC2_IMAGE_ID, "byop-washua", EC2_IMAGE_ID);
        String imageId = response.imageId();
        // String imageId = "wsi-kwpvn4rvsxxxxx"; //console - create image id -- for testing purpose

        while (true) {
            // DescribeWorkspaceImagesResponse describeWorkspaceImagesResponse = app.describeWorkspaceImages(response.imageId());
            DescribeWorkspaceImagesResponse describeWorkspaceImagesResponse = app.describeWorkspaceImages(imageId);
            System.out.println("describeWorkspaceImagesResponse.images().size() = " + describeWorkspaceImagesResponse.images().size());
            
            WorkspaceImage image = describeWorkspaceImagesResponse.images().get(0);
            if (image.state().equals(WorkspaceImageState.AVAILABLE)) {
                System.out.println("Import Workspace Image is available: " + imageId);
                break;
            } else if (image.state().equals(WorkspaceImageState.ERROR)) {
                System.out.println("Import Workspace Image fails: " + describeWorkspaceImagesResponse.images().get(0).errorMessage());
                System.exit(1);
            }

            Thread.sleep(300000);
        } 

        System.out.println("Create Workspace Bundle from Image: " + imageId);
        CreateWorkspaceBundleResponse createWorkspaceBundleResponse = app.createWorkspaceBundle(
                imageId, "byop-bundle", "test-bundle-description",
                Compute.STANDARD, 50, 80);
        String bundleId = createWorkspaceBundleResponse.workspaceBundle().bundleId();
        while (true) {
            DescribeWorkspaceBundlesResponse describeWorkspaceBundlesResponse = app.describeWorkspaceBundles(bundleId);
            WorkspaceBundle bundle = describeWorkspaceBundlesResponse.bundles().get(0);
            if (bundle.state().equals(WorkspaceBundleState.AVAILABLE)) {
                System.out.println("Create Workspace Bundle is available: " + bundleId);
                break;
            } else if (bundle.state().equals(WorkspaceBundleState.ERROR)) {
                System.out.println("Create Workspace Bundle fails ");
                System.exit(1);
            }

            Thread.sleep(300000);
        }

        System.out.println("Create Workspace from Bundle: " + bundleId);
        CreateWorkspacesResponse createWorkspacesResponse = app.createWorkspaces(bundleId, DIRECTORY_ID, USER_NAME);
        String workspaceId = createWorkspacesResponse.pendingRequests().get(0).workspaceId();
        
        DescribeWorkspacesResponse describeWorkspacesResponse;
        while (true) {
            describeWorkspacesResponse = app.describeWorkspaces(workspaceId);
            Workspace workspace = describeWorkspacesResponse.workspaces().get(0);
            if (workspace.state().equals(WorkspaceState.AVAILABLE)) {
                System.out.println("Create Workspace is available: " + workspaceId);
                break;
            } else if (workspace.state().equals(WorkspaceState.ERROR)) {
                System.out.println("Create Workspace fails ");
                System.exit(1);
            }

            Thread.sleep(3000000);
        }

        String privateIp = describeWorkspacesResponse.workspaces().get(0).ipAddress();
        System.out.println("Workspace ip address: " + privateIp);
        DescribeNetworkInterfacesResponse describeNetworkInterfacesResponse = app.describeNetworkInterfacesByPrivateIp(privateIp);
        String publicIp = describeNetworkInterfacesResponse.networkInterfaces().get(0).association().publicIp();
        System.out.println("Workspace public ip address: " + publicIp);
        System.exit(0);
    }
}
