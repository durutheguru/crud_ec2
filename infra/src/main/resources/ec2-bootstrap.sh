#!/bin/bash

echo "Deployment Time: %s"

yum update -y
yum -y install telnet

yum install -y java-17-amazon-corretto.x86_64

aws s3 cp "s3://crud-ec2-00001/crud_ec2-0.0.1-SNAPSHOT.jar" /home/crud_ec2-0.0.1-SNAPSHOT.jar
aws s3 cp "s3://crud-ec2-00001/application.properties" /home/application.properties

DB_ENDPOINT=%s
DB_NAME=%s
SECRET_NAME=%s

USERNAME=$(aws secretsmanager get-secret-value --secret-id ${SECRET_NAME} --query SecretString --output text | jq -r .username)
PASSWORD=$(aws secretsmanager get-secret-value --secret-id ${SECRET_NAME} --query SecretString --output text | jq -r .password)

cat << EOF > /etc/systemd/system/ec2_crud.service

[Unit]
Description=Sample Java Application
After=network.target

[Service]
Type=simple
Restart=always
RestartSec=2
Environment="SPRING_DATASOURCE_URL=jdbc:mysql://${DB_ENDPOINT}:3306/${DB_NAME}?createDatabaseIfNotExist=true"
Environment="SPRING_DATASOURCE_USERNAME=${USERNAME}"
Environment="SPRING_DATASOURCE_PASSWORD=${PASSWORD}"
ExecStart=/usr/bin/java -jar /home/crud_ec2-0.0.1-SNAPSHOT.jar

[Install]
WantedBy=multi-user.target

EOF

systemctl daemon-reload
systemctl enable ec2_crud
systemctl start ec2_crud



