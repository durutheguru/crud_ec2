package com.julianduru.cdk;

import com.julianduru.cdk.stages.TestStage;
import software.amazon.awscdk.Environment;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.StageProps;
import software.amazon.awscdk.pipelines.CodePipeline;
import software.amazon.awscdk.pipelines.CodePipelineSource;
import software.amazon.awscdk.pipelines.ShellStep;
import software.amazon.awscdk.services.s3.Bucket;
import software.constructs.Construct;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;


public class CodePipelineStack extends Stack {


    public CodePipelineStack(final Construct scope, final String id) {
        this(scope, id, null);
    }


    public CodePipelineStack(final Construct scope, final String id, final StackProps props) {
        super(scope, id, props);

        Bucket bucket = this.createDeploymentBucket();
        createCodePipeline(bucket);
    }


    private Bucket createDeploymentBucket() {
        return Bucket.Builder.create(this, "EC2DeploymentBucket")
            .bucketName("crud-ec2-00001")
            .build();
    }


    private void createCodePipeline(Bucket bucket) {
        CodePipeline pipeline = CodePipeline.Builder.create(this, "pipeline")
            .pipelineName("Pipeline")
            .synth(
                ShellStep.Builder.create("Synth")
                    .input(CodePipelineSource.gitHub("durutheguru/crud_ec2", "main"))
                    .commands(readPipelineCommands())
                    .primaryOutputDirectory("infra/cdk.out")
                    .build()
            )
            .build();

        pipeline.addStage(
            new TestStage(
                this, "testStageId",
                StageProps.builder()
                    .env(
                        Environment.builder()
                            .account("058486276453")
                            .region("us-east-1")
                            .build()
                    )
                    .build()
            )
        );
    }


    private List<String> readPipelineCommands() {
        Path currentWorkingDir = Paths.get(System.getProperty("user.dir"));
        System.out.println("Current Working Directory: " + currentWorkingDir);

        String scriptContent;
        try {
            scriptContent = new String(
                Files.readAllBytes(
                    Paths.get("src/main/resources/code-pipeline-commands.sh")
                ),
                StandardCharsets.UTF_8
            );

            System.out.println("Code Pipeline Command Content:\n" + scriptContent);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read the shell script file.", e);
        }

        return Arrays.asList(scriptContent.split("\\n"));
    }



}

