#!/bin/bash

# Make scripts executable
chmod +x scripts/multi-container-communication/docker-setup.sh
chmod +x multi-container-shutdown.sh

echo "Scripts setup complete."

# Run the docker-setup.sh script
./scripts/multi-container-communication/docker-setup.sh ;

echo "Please run ./multi-container-shutdown.sh to clean up and remove all docker containers/images/networks."

