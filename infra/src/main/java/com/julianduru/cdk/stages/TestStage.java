package com.julianduru.cdk.stages;

import com.julianduru.cdk.stages.test.Ec2Stack;
import com.julianduru.cdk.stages.test.RdsStack;
import com.julianduru.cdk.stages.test.VpcStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import software.amazon.awscdk.Stage;
import software.amazon.awscdk.StageProps;
import software.amazon.awscdk.services.ec2.Peer;
import software.amazon.awscdk.services.ec2.Port;
import software.amazon.awscdk.services.ec2.SecurityGroup;
import software.amazon.awscdk.services.ec2.Vpc;
import software.amazon.awscdk.services.s3.Bucket;
import software.constructs.Construct;

/**
 * created by Julian Dumebi Duru on 19/07/2023
 */
public class TestStage extends Stage {


    public TestStage(@NotNull Construct scope, @NotNull String id, @Nullable StageProps props) {
        super(scope, id, props);


        VpcStack vpcStack = new VpcStack(this, "vpcStackId");

        RdsStack rdsStack = new RdsStack(this, "rdsStackId", vpcStack.getVpc(), vpcStack.getEc2SecurityGroup());
        Ec2Stack ec2Stack = new Ec2Stack(
            this, "ec2StackId", vpcStack.getVpc(), vpcStack.getEc2SecurityGroup(), rdsStack.getDatabaseEnvMap()
        );

    }


}


