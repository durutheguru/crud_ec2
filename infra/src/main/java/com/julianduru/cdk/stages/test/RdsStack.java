package com.julianduru.cdk.stages.test;

/**
 * created by Julian Dumebi Duru on 19/07/2023
 */

import com.julianduru.cdk.util.JSONUtil;
import software.amazon.awscdk.CfnOutput;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.services.ec2.*;
import software.amazon.awscdk.services.rds.*;
import software.amazon.awscdk.services.secretsmanager.Secret;
import software.amazon.awscdk.services.secretsmanager.SecretStringGenerator;
import software.constructs.Construct;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;


public class RdsStack extends Stack {

    private final DatabaseInstance database;

    private final Secret secret;

    private final Map<String, String> databaseEnvMap;


    public RdsStack(final Construct scope, final String id, final Vpc vpc, final SecurityGroup ec2SecurityGroup) {
        super(scope, id);

        this.secret = createDatabaseSecret();
        this.database = createDatabaseInstance(vpc, this.secret, ec2SecurityGroup);

        this.databaseEnvMap = prepareDbEnvMap();

        // Output RDS Endpoint
        CfnOutput.Builder.create(this, "DatabaseEndpoint")
            .value(database.getDbInstanceEndpointAddress())
            .build();
    }


    private Secret createDatabaseSecret() {
        Map<String, String> secretsMap = new HashMap<>();
        secretsMap.put("username", "duru");

        return Secret.Builder.create(this, "TemplatedSecret")
            .generateSecretString(
                SecretStringGenerator.builder()
                    .secretStringTemplate(JSONUtil.asJsonString(secretsMap, ""))
                    .generateStringKey("password")
                    .excludeCharacters("/@\"")
                    .build()
            )
            .build();
    }


    private DatabaseInstance createDatabaseInstance(Vpc vpc, Secret secret, SecurityGroup ec2SecurityGroup) {
        SecurityGroup rdsSecurityGroup = SecurityGroup.Builder.create(this, "TestRDSSecurityGroup")
            .vpc(vpc)
            .build();
        rdsSecurityGroup.addIngressRule(ec2SecurityGroup, Port.tcp(3306), "Allow EC2 access");
        rdsSecurityGroup.addIngressRule(Peer.anyIpv4(), Port.tcp(3306), "Allow Public access");


        return DatabaseInstance.Builder.create(this, "Database")
            .engine(
                DatabaseInstanceEngine.mysql(
                    MySqlInstanceEngineProps.builder()
                        .version(MysqlEngineVersion.VER_8_0)
                        .build()
                )
            )
            .instanceIdentifier("my-rds-database")
            .credentials(Credentials.fromSecret(secret))
            .vpc(vpc)
            .vpcSubnets(
                SubnetSelection.builder()
                    .subnetType(SubnetType.PUBLIC)
                    .build()
            )
            .securityGroups(Collections.singletonList(rdsSecurityGroup))
            .build();
    }


    public DatabaseInstance getDatabase() {
        return database;
    }


    public Secret getSecret() {
        return secret;
    }


    private Map<String, String> prepareDbEnvMap() {
        Map<String, String> map = new HashMap<>();

        map.put("DB_ENDPOINT", database.getDbInstanceEndpointAddress());
        map.put("DB_NAME", "ec2_db");
        map.put("SECRET_NAME", this.secret.getSecretName());
        map.put("SECRET_ARN", this.secret.getSecretArn());

        System.out.println("DBMap: \n" + map);

        return map;
    }


    public Map<String, String> getDatabaseEnvMap() {
        return databaseEnvMap;
    }


}



