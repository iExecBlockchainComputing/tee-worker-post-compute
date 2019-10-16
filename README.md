# dropbox-uploader
dropbox-uploader component enabling trusted push on worker side

`gradle clean build && java -jar  \
             -DLOCAL_FILE_PATH="/home/james/bla/0x1.zip" \
             -DREMOTE_FILENAME="0x1.zip" \
             -DACCESS_TOKEN="xxx" \
             build/libs/dropbox-uploader.jar`
