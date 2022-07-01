## CI/CD version

ARG JAR_PATH=build/libs/app-all.jar

FROM iexechub/iexec-graphene-base:dev
RUN apt update &&  apt-get install -y openjdk-11-jdk git

COPY $JAR_PATH /workplace/app/app.jar
RUN ls -al /workplace/app
RUN ls -al /workplace/app/app.jar
RUN ls -al /workplace/app/app.jar/build
RUN ls -al /workplace/app/app.jar/build/libs

# set the jar name for java, no need for binnary app
RUN sed -i "s#MAIN_FUNC=#MAIN_FUNC='-jar app.jar'#" /apploader.sh

WORKDIR /workplace/app

# finalize manifest
COPY gramine/entrypoint.manifest /
RUN python3 -B /var/tmp/finalize_manifest.py

# sign image
COPY gramine/signer-key.pem /signer-key.pem
RUN /graphene/python/graphene-sgx-sign \
        -libpal /graphene/Runtime/libpal-Linux-SGX.so \
        -key /signer-key.pem \
        -manifest /entrypoint.manifest \
        -output /entrypoint.manifest.sgx

RUN rm -rf /var/tmp/* \
    && rm -rf /graphene/python/graphene-sgx-sign /graphene/python/graphenelibos/sgx_sign.py \
    && rm -rf /signer-key.pem
