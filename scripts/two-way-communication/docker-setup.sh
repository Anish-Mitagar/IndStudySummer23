#!/bin/bash

# Set container names as environment variables
CONTAINER_NAME_SERVER_1=server-1
CONTAINER_NAME_SERVER_2=server-2

# Create Docker network
docker network create IndStudyNetwork

# Build Docker image
docker build -t nodeimage .

# Run containers in detached mode with environment variables
docker run -d --network IndStudyNetwork --name $CONTAINER_NAME_SERVER_1 -e CONTAINER_NAME=$CONTAINER_NAME_SERVER_1 -p 50051:50051 nodeimage
docker run -d --network IndStudyNetwork --name $CONTAINER_NAME_SERVER_2 -e CONTAINER_NAME=$CONTAINER_NAME_SERVER_2 -p 50052:50051 nodeimage

echo "Docker setup complete."

