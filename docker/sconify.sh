#!/bin/bash

<< 'USAGE'
IMG_FROM=docker.io/iexechub/tee-worker-post-compute:dev \
IMG_TO=docker.io/iexechub/tee-worker-post-compute:dev-debug \
SCONIFIER_VERSION=x.y.z \
./sconify.sh
USAGE

cd $(dirname $0)

ARGS=$(sed -e "s'\${IMG_FROM}'${IMG_FROM}'" -e "s'\${IMG_TO}'${IMG_TO}'" sconify.args)
echo $ARGS

SCONIFIER_IMAGE="registry.scontain.com:5050/scone-production/iexec-sconify-image:${SCONIFIER_VERSION}"

/bin/bash -c "docker run \
    --rm \
    -v /var/run/docker.sock:/var/run/docker.sock \
    ${SCONIFIER_IMAGE} \
    sconify_iexec \
    --cli=${SCONIFIER_IMAGE} \
    --crosscompiler=${SCONIFIER_IMAGE} \
    $ARGS"
