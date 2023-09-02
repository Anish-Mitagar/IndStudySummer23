#!/bin/bash

# List of container names
CONTAINER_NAMES=(
  "server-1"
  "server-2"
  "server-3"
  "server-4"
  "server-5"
  "server-6"
)

# Stop and remove containers
for container_name in "${CONTAINER_NAMES[@]}"
do
  docker stop $container_name
  docker rm $container_name
done

# Remove image
docker rmi nodeimage

# Remove network
docker network rm IndStudyNetwork

echo "Shutdown and cleanup complete."
