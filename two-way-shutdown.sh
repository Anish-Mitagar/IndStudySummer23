#!/bin/bash

# Stop containers
docker stop server-1 ; \
docker stop server-2 ; \

#remove containers
docker rm server-1 ; \
docker rm server-2 ; \

# Remove image
docker rmi nodeimage ; \

# Shutdown and remove network (you can remove the container instances first if they are still running)
docker network rm IndStudyNetwork

echo "Shutdown and cleanup complete."
