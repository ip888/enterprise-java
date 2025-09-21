#!/bin/bash

echo "🚀 Setting up Enterprise Java Portfolio Development Environment..."

# Install SDKMAN for Java version management
curl -s "https://get.sdkman.io" | bash
source "$HOME/.sdkman/bin/sdkman-init.sh"

# Install Java 21 (GraalVM for native compilation)
sdk install java 21.0.1-graal

# Install Maven 3.9+
sdk install maven 3.9.5

# Install Gradle 8+ (alternative build tool)
sdk install gradle 8.4

# Install Apache Spark
wget https://archive.apache.org/dist/spark/spark-3.5.0/spark-3.5.0-bin-hadoop3.tgz
tar -xzf spark-3.5.0-bin-hadoop3.tgz
sudo mv spark-3.5.0-bin-hadoop3 /opt/spark
rm spark-3.5.0-bin-hadoop3.tgz

# Install Apache Flink
wget https://archive.apache.org/dist/flink/flink-1.18.0/flink-1.18.0-bin-scala_2.12.tgz
tar -xzf flink-1.18.0-bin-scala_2.12.tgz
sudo mv flink-1.18.0 /opt/flink
rm flink-1.18.0-bin-scala_2.12.tgz

# Set environment variables
echo 'export SPARK_HOME=/opt/spark' >> ~/.bashrc
echo 'export FLINK_HOME=/opt/flink' >> ~/.bashrc
echo 'export PATH=$SPARK_HOME/bin:$FLINK_HOME/bin:$PATH' >> ~/.bashrc

# Install Docker Compose
sudo curl -L "https://github.com/docker/compose/releases/latest/download/docker-compose-linux-x86_64" -o /usr/local/bin/docker-compose
sudo chmod +x /usr/local/bin/docker-compose

# Install JMeter for performance testing
wget https://archive.apache.org/dist/jmeter/binaries/apache-jmeter-5.6.2.tgz
tar -xzf apache-jmeter-5.6.2.tgz
sudo mv apache-jmeter-5.6.2 /opt/jmeter
rm apache-jmeter-5.6.2.tgz
echo 'export JMETER_HOME=/opt/jmeter' >> ~/.bashrc
echo 'export PATH=$JMETER_HOME/bin:$PATH' >> ~/.bashrc

# Install Terraform for Infrastructure as Code
wget https://releases.hashicorp.com/terraform/1.6.0/terraform_1.6.0_linux_amd64.zip
unzip terraform_1.6.0_linux_amd64.zip
sudo mv terraform /usr/local/bin/
rm terraform_1.6.0_linux_amd64.zip

# Setup Git configuration
git config --global init.defaultBranch main
git config --global pull.rebase false

# Create project directories
mkdir -p {microservices,streaming-analytics,infrastructure,docs,scripts}

# Install additional development tools
npm install -g @apidevtools/swagger-cli
npm install -g newman  # Postman CLI for API testing

# Setup pre-commit hooks directory
mkdir -p .git/hooks

echo "✅ Development environment setup complete!"
echo "🔧 Available tools:"
echo "   - Java 21 (GraalVM)"
echo "   - Maven 3.9.5 & Gradle 8.4"
echo "   - Apache Spark 3.5.0"
echo "   - Apache Flink 1.18.0"
echo "   - Docker & Docker Compose"
echo "   - Terraform"
echo "   - JMeter"
echo ""
echo "🚀 Ready to build enterprise-grade applications!"

# Source the environment
source ~/.bashrc