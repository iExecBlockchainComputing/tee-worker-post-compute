#!/bin/sh

#JAVA_OPTIONS="-DACCESS_TOKEN=$DROPBOX_ACCESS_TOKEN -DLOCAL_FILE_PATH=$LOCAL_FILE_PATH -DREMOTE_FILENAME=$REMOTE_FILENAME"
#java $JAVA_OPTIONS -jar /app/tee-worker-post-compute.jar

java -jar /app/tee-worker-post-compute.jar
