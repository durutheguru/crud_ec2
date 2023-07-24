package com.julianduru.cdk.stages.test;

/**
 * created by Julian Dumebi Duru on 19/07/2023
 */

import software.amazon.awscdk.CfnOutput;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.services.ec2.*;
import software.constructs.Construct;

import java.util.Arrays;

public class VpcStack extends Stack {


    private final Vpc vpc;


    private final SecurityGroup ec2SecurityGroup;


    public VpcStack(final Construct scope, final String id) {
        super(scope, id);

        // Create VPC
        this.vpc = Vpc.Builder.create(this, "Test_Env_Vpc")
            .vpcName("Test Env VPC")
            .ipAddresses(IpAddresses.cidr("10.0.0.0/16"))
            .maxAzs(2)
            .natGateways(1)
            .subnetConfiguration(
                Arrays.asList(
                    SubnetConfiguration.builder()
                        .name("Public")
                        .subnetType(SubnetType.PUBLIC)
                        .cidrMask(24)
                        .build(),

                    SubnetConfiguration.builder()
                        .name("Private")
                        .subnetType(SubnetType.PRIVATE_ISOLATED)
                        .cidrMask(24)
                        .build()
                )
            )
            .build();

        this.ec2SecurityGroup = this.createSecurityGroup(this.vpc);

        // Output VPC ID
        CfnOutput.Builder.create(this, "Test_Env_Vpc_Id")
            .value(vpc.getVpcId())
            .build();
    }


    public Vpc getVpc() {
        return vpc;
    }


    private SecurityGroup createSecurityGroup(Vpc vpc) {
        SecurityGroup sg = SecurityGroup.Builder.create(this, "TestEc2SecurityGroup")
            .vpc(vpc)
            .build();
        sg.addIngressRule(Peer.anyIpv4(), Port.tcp(22), "Allow SSH access");
        sg.addIngressRule(Peer.anyIpv4(), Port.tcp(8080), "Allow Service Port access");

        return sg;
    }


    public SecurityGroup getEc2SecurityGroup() {
        return ec2SecurityGroup;
    }


}


