#!/bin/bash

cd $(dirname $0)

SCONE_IMG_NAME=scone-debug/iexec-sconify-image-unlocked
if [ -z "SCONE_IMG_VERSION" ] ; then
  SCONE_IMG_VERSION=5.7.6
fi

if [ -z "$IMG_TO" ] ; then
  IMG_TO=$IMG_FROM
fi
IMG_TO+=-sconify-${SCONE_IMG_VERSION}-debug

ARGS=$(sed -e "s'\${IMG_FROM}'${IMG_FROM}'" -e "s'\${IMG_TO}'${IMG_TO}'" sconify.args)
echo $ARGS

SCONE_IMAGE="registry.scontain.com/${SCONE_IMG_NAME}:${SCONE_IMG_VERSION}"

docker run --rm \
    -v /var/run/docker.sock:/var/run/docker.sock \
    ${SCONE_IMAGE} \
        sconify_iexec \
            --cli=${SCONE_IMAGE} \
            --crosscompiler=${SCONE_IMAGE} \
            $ARGS

echo
docker run --rm -e SCONE_HASH=1 $IMG_TO
