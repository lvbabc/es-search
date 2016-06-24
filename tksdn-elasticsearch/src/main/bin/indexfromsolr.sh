#!/bin/bash

echo "Starting ..."

while (true)
do
    bin/ctl.sh start indexfromSolr
    sleep 1h
done

echo "Finishing ..."
