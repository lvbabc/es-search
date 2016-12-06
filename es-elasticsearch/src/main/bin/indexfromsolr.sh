#!/bin/bash

echo "Starting indexfromSolrNow ..."
while (true)
do
    bin/ctl.sh start indexfromSolrNow
	echo "OneLoop Finished!"
    sleep 3600
done
echo "Finishing indexfromSolr ..."
    
