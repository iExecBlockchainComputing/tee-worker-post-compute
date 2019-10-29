# tee-worker-post-compute
tee-worker-post-compute component enabling trusted: 

* 1 - result encryption
* 2 - result upload
* 3 - execution sign


`gradle clean build && java -jar  \
             -DLOCAL_FILE_PATH="/home/james/bla/0x1.zip" \
             -DREMOTE_FILENAME="0x1.zip" \
             -DACCESS_TOKEN="xxx" \
             build/libs/tee-worker-post-compute.jar`
