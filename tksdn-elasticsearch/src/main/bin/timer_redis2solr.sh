#!/bin/bash

echo "Starting ..."

bin/ctl.sh start indexfromSolr

while (true)
do
    sleep 1h
    bin/ctl.sh restart indexfromSolr
done

echo "Finishing ..."
