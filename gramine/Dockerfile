## CI/CD version

###
### TODO Get JAR from native image instead of rebuilding everything
### Build application jar
###
FROM gradle:6.8.3-jdk11 as gradle-builder
ARG HOME=/home
WORKDIR $HOME
COPY build.gradle gradle.properties settings.gradle  ./
COPY src/ src/
RUN gradle build -i --no-daemon
RUN ls -al /home/build/libs


###
### Build the final graminized image with
### the produced jar.
###
FROM docker-regis.iex.ec/iexec-graphene-base:0.6.0

COPY --from=gradle-builder /home/build/libs/app-all.jar /workplace/app/app.jar

RUN apt update \
    && apt-get install -y openjdk-11-jre-headless \
    && rm -rf /var/lib/apt/lists/*
RUN sed -i "s#MAIN_FUNC=#MAIN_FUNC='-jar app.jar'#" /apploader.sh

RUN mkdir /post-compute-tmp

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
