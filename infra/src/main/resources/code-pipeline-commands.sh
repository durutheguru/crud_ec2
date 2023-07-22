echo "Version of Linux running:"
cat /etc/os-release
npm install -g aws-cdk
cd service
mvn clean package -DskipTests
aws s3 ls s3://crud-ec2-00001 || aws s3 mb s3://crud-ec2-00001
aws s3 cp target/crud_ec2-0.0.1-SNAPSHOT.jar s3://crud-ec2-00001/
cd ../infra
cdk deploy --all --verbose --require-approval never
