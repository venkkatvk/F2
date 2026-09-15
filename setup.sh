#!/usr/bin/env bash
# System Environment Provisioning Script for Debian/Ubuntu Systems

set -e

echo "=========================================="
echo " Starting Prerequisites Installation "
echo "=========================================="

# 1. Update Package Repository
echo "[1/4] Updating package index..."
sudo apt-get update -y
sudo apt-get install -y curl wget unzip ca-certificates gnupg software-properties-common

# 2. Install JDK 21
echo "[2/4] Installing OpenJDK 21..."
sudo apt-get install -y openjdk-21-jdk

# 3. Install Apache Maven
echo "[3/4] Installing Apache Maven..."
sudo apt-get install -y maven

# 4. Install Docker & Docker Compose Plugin
echo "[4/4] Installing Docker and Docker Compose..."
if ! command -v docker &> /dev/null; then
    sudo apt-get install -y docker.io docker-compose-v2
    sudo systemctl enable --now docker
    sudo usermod -aG docker $USER
    echo "Docker installed. (Note: Log out and back in if docker group permissions require refresh)."
else
    echo "Docker is already installed."
fi

echo "=========================================="
echo " Verification "
echo "=========================================="
java -version
mvn -version
docker --version
docker compose version

echo "=========================================="
echo " All prerequisites installed successfully!"
echo " You can now run: docker-compose up -d && mvn clean spring-boot:run"
echo "=========================================="
