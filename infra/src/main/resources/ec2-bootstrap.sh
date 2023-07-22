#!/bin/bash


yum update -y
yum -y install telnet

yum install -y java-17-openjdk

aws s3 cp "s3://crud_ec2_00001/crud_ec2-0.0.1-SNAPSHOT.jar" /home/crud_ec2-0.0.1-SNAPSHOT.jar

cat << EOF > /etc/systemd/system/ec2_crud.service

[Unit]
Description=Sample Java Application
After=network.target

[Service]
Type=simple
Restart=always
RestartSec=2
ExecStart=/usr/bin/java -jar /home/crud_ec2-0.0.1-SNAPSHOT.jar

[Install]
WantedBy=multi-user.target

EOF

systemctl daemon-reload
systemctl enable ec2_crud
systemctl start ec2_crud



