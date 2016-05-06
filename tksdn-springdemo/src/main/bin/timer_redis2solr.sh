#!/bin/bash

echo "Starting ..."

bin/ctl.sh start importRedisToES

while (true)
do
    sleep 1h
    bin/ctl.sh restart importRedisToES
done

echo "Finishing ..."
