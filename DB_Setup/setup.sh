echo Creating Single Cockroach Node...

#localhost command
#cockroach start --insecure --store=node1 --listen-addr=localhost:26257 --http-addr=localhost:8080 --join=localhost:26257,localhost:26258,localhost:26259 --background

#Docker change
cockroach start --insecure --store=node1 --listen-addr=0.0.0.0:26257 --http-addr=localhost:8080 --join=localhost:26257,localhost:26258,localhost:26259 --background

echo Initializing Single Cockroach Node Cluster...

cockroach init --insecure --host=localhost:26257

grep 'node starting' node1/logs/cockroach.log -A 11

echo Running sql script...

cockroach sql --insecure --host=localhost:26257 --file DB_Setup/setup.sql

echo Finished Setting Up Database, now connecting to SQL shell...

cockroach sql --insecure --host=localhost:26257

