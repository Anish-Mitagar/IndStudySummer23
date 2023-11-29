#!/bin/bash

# List of container names
CONTAINER_NAMES=(
  "server-1"
  "server-2"
  "server-3"
  "server-4"
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

# Connect to CockroachDB SQL shell, drop tables, and exit
cockroach sql --insecure --host=localhost:26257 <<EOF
USE socialnetwork;
DROP TABLE IF EXISTS temp_message;
DROP TABLE IF EXISTS temp_forum;
DROP TABLE IF EXISTS temp_person;
\q
EOF

echo "Shutdown and cleanup complete."
