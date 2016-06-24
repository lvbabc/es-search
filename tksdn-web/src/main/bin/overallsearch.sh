#!/bin/bash

echo "Starting ..."

bin/ctl.sh start queryApiServer

while (true)
do
    sleep 1h
    bin/ctl.sh restart queryApiServer
done

echo "Finishing ..."
