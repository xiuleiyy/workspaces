# Workspaces

Sample code for Amazon Workspaces core SDK APIs.

This project contains an AWS Lambda maven application with [AWS Java SDK 2.x](https://github.com/aws/aws-sdk-java-v2) dependencies.

## Prerequisites
- Java 1.8+
- Apache Maven
- [AWS SAM CLI](https://docs.aws.amazon.com/serverless-application-model/latest/developerguide/serverless-sam-cli-install.html)
- Docker

## Development

The generated function handler class just returns the input. The configured AWS Java SDK client is created in `DependencyFactory` class and you can 
add the code to interact with the SDK client based on your use case.

## WorkSpaces Core APIs

* RegisterWorkSpaceDirectory：将AD注册给WorkSpaces服务，添加至少一个用户（此步骤暂时通过控制台完成）
* ImportWorkspaceImage：将EC2 Image 导入WorkSpaces服务
* CreateWorkspaceBundle：依据导入的Workspace 映像创建Workspace 捆绑包
* CreateWorkspaces：依据Workspace捆绑包创建Workspace

## Connect to the Workspace

You can use RDP client to connect to the created Workspace.

#### Building the project
```
mvn clean install
```

#### Testing it locally
```
sam local invoke
```

#### Adding more SDK clients
To add more service clients, you need to add the specific services modules in `pom.xml` and create the clients in `DependencyFactory` following the same 
pattern as workSpacesClient.

## Deployment

The generated project contains a default [SAM template](https://docs.aws.amazon.com/serverless-application-model/latest/developerguide/sam-resource-function.html) file `template.yaml` where you can 
configure different properties of your lambda function such as memory size and timeout. You might also need to add specific policies to the lambda function
so that it can access other AWS resources.

To deploy the application, you can run the following command:

```
sam deploy --guided
```

See [Deploying Serverless Applications](https://docs.aws.amazon.com/serverless-application-model/latest/developerguide/serverless-deploying.html) for more info.



