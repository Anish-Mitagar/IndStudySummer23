#!/bin/bash

# Set container names as environment variables
CONTAINER_NAME_SERVER_1=server-1
CONTAINER_NAME_SERVER_2=server-2
CONTAINER_NAME_SERVER_3=server-3
CONTAINER_NAME_SERVER_4=server-4

# Create Docker network
docker network create IndStudyNetwork

# Build Docker image
docker build -t nodeimage .

# Loop through container names and indices - Docker Experiment
for i in {4..1}
do
  CONTAINER_NAME_VAR="CONTAINER_NAME_SERVER_$i"
  CONTAINER_NAME=${!CONTAINER_NAME_VAR}

  # Build and run container with index as argument
  docker run -d --network IndStudyNetwork --name $CONTAINER_NAME -e CONTAINER_NAME=$CONTAINER_NAME -e clientArgs="$i" -e queryArgs=6 -e messageArgs=545 -p 5005$i:5005$i nodeimage
done

#Single Docker Container for testing
#i=1
#CONTAINER_NAME_VAR="CONTAINER_NAME_SERVER_$i"
#CONTAINER_NAME=${!CONTAINER_NAME_VAR}

#docker run -d --network IndStudyNetwork --name $CONTAINER_NAME -e CONTAINER_NAME=$CONTAINER_NAME -e clientArgs="$i" -e queryArgs=5 -e messageArgs=545 -p 50051:50051 nodeimage

echo "Docker setup complete."
