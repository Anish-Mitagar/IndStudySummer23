#!/bin/bash

# Make scripts executable
chmod +x scripts/two-way-communication/docker-setup.sh
chmod +x scripts/two-way-communication/client-1.sh
chmod +x scripts/two-way-communication/client-2.sh
chmod +x two-way-shutdown.sh

echo "Scripts setup complete."

# Run the docker-setup.sh script
./scripts/two-way-communication/docker-setup.sh ; \

echo "Giving 1 minutes for the servers to start up" ; \

# Countdown for 1 minutes
for ((i = 60; i >= 1; i--)); do
    echo -ne "Running client-1 in $i seconds...\r"
    sleep 1
done
echo ; \

# Run the client scripts after docker setup
./scripts/two-way-communication/client-1.sh ; \

# Countdown for 10 seconds
for ((i = 10; i >= 1; i--)); do
    echo -ne "Running client-2 in $i seconds...\r"
    sleep 1
done
echo ; \

./scripts/two-way-communication/client-2.sh

echo "Please run ./two-way-shutdown.sh to clean up and remove all docker containers/images/networks."

