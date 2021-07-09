#!/usr/bin/env bash

echo "Waiting for cassandra to become available"
sleep 30

echo "Creating keyspace"
cqlsh cassandra -e "CREATE KEYSPACE IF NOT EXISTS test WITH replication = {'class':'SimpleStrategy', 'replication_factor':1};"
