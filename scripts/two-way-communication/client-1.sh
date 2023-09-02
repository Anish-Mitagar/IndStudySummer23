#!/bin/bash

# Attach to server-1 container and run gradle execute command
docker exec -i server-1 /bin/bash <<EOF

# Run Client.java to connect to server-2
gradle execute -PtargetClass=Client -PclientArgs=server-2

EOF

# Detach from the container
exit
