#!/bin/bash

echo "Starting hotkey ..."
while (true)
do
    bin/ctl.sh start hotkey
	echo "OneLoop Finished!"
    sleep 1800
done
echo "Finishing insightHotKey ..."
