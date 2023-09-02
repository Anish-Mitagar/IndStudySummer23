#!/bin/bash

# Set container names as environment variables
CONTAINER_NAME_SERVER_1=server-1
CONTAINER_NAME_SERVER_2=server-2
CONTAINER_NAME_SERVER_3=server-3
CONTAINER_NAME_SERVER_4=server-4
CONTAINER_NAME_SERVER_5=server-5
CONTAINER_NAME_SERVER_6=server-6

# Create Docker network
docker network create IndStudyNetwork

# Build Docker image
docker build -t nodeimage .

# Loop through container names and indices
for i in {6..1}
do
  CONTAINER_NAME_VAR="CONTAINER_NAME_SERVER_$i"
  CONTAINER_NAME=${!CONTAINER_NAME_VAR}

  # Build and run container with index as argument
  docker run -d --network IndStudyNetwork --name $CONTAINER_NAME -e CONTAINER_NAME=$CONTAINER_NAME -e clientArgs="$i" -p 5005$i:5005$i nodeimage
done

echo "Docker setup complete."
