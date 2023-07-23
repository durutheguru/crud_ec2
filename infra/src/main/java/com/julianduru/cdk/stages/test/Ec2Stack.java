package com.julianduru.cdk.stages.test;

/**
 * created by Julian Dumebi Duru on 19/07/2023
 */

import software.amazon.awscdk.CfnOutput;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.services.ec2.*;
import software.amazon.awscdk.services.iam.*;
import software.constructs.Construct;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;


public class Ec2Stack extends Stack {

    private final Instance instance;

    private final SecurityGroup securityGroup;


    public Ec2Stack(final Construct scope, final String id, final Vpc vpc) {
        super(scope, id);

        this.securityGroup = createSecurityGroup(vpc);
        this.instance = createInstance(this.securityGroup, vpc);

        // Output EC2 Instance ID
        CfnOutput.Builder.create(this, "Test_Ec2_Instance_Id")
            .value(instance.getInstanceId())
            .build();
    }


    public SecurityGroup getSecurityGroup() {
        return securityGroup;
    }


    private SecurityGroup createSecurityGroup(Vpc vpc) {
        SecurityGroup sg = SecurityGroup.Builder.create(this, "TestEc2SecurityGroup")
            .vpc(vpc)
            .build();
        sg.addIngressRule(Peer.anyIpv4(), Port.tcp(22), "Allow SSH access");

        return sg;
    }


    private Instance createInstance(SecurityGroup securityGroup, Vpc vpc) {
        IRole role = createEC2RoleForBucketAccess();
        String scriptContent = readBootStrapScript();

        return Instance.Builder.create(this, "Test_Ec2_Instance")
            .vpc(vpc)
            .vpcSubnets(
                SubnetSelection.builder()
                    .subnetType(SubnetType.PUBLIC)
                    .build()
            )
            .instanceType(InstanceType.of(InstanceClass.T2, InstanceSize.MICRO))
            .machineImage(MachineImage.latestAmazonLinux2023())
            .securityGroup(securityGroup)
            .userData(UserData.custom(scriptContent))
            .role(role)
            .build();
    }


    private IRole createEC2RoleForBucketAccess() {
        Role role = Role.Builder.create(this, "TestStageEC2Role")
            .assumedBy(new ServicePrincipal("ec2.amazonaws.com"))
            .build();

        PolicyStatement s3ReadPolicy = PolicyStatement.Builder.create()
            .effect(Effect.ALLOW)
            .actions(Collections.singletonList("s3:GetObject"))
            .resources(Collections.singletonList("arn:aws:s3:::crud-ec2-00001/*"))
            .build();

        ManagedPolicy managedPolicy = new ManagedPolicy(this, "TestStageEC2S3ReadPolicy", ManagedPolicyProps.builder()
            .statements(Collections.singletonList(s3ReadPolicy))
            .build());

        role.addManagedPolicy(managedPolicy);

        return role;
    }


    private String readBootStrapScript() {
        Path currentWorkingDir = Paths.get(System.getProperty("user.dir"));
        System.out.println("Current Working Directory: " + currentWorkingDir);

        String scriptContent;
        try {
            scriptContent = new String(
                Files.readAllBytes(
                    Paths.get("src/main/resources/ec2-bootstrap.sh")
                ),
                StandardCharsets.UTF_8
            );

            System.out.println("EC2 Bootstrap Script Content:\n" + scriptContent);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read the shell script file.", e);
        }

        return scriptContent;
    }


}



