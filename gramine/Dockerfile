FROM docker-regis.iex.ec/iexec-graphene-base:0.7.0

COPY build/libs/app-all.jar /workplace/app/app.jar

RUN apt update \
    && apt-get install -y openjdk-11-jre-headless \
    && rm -rf /var/lib/apt/lists/*
RUN sed -i "s#MAIN_FUNC=#MAIN_FUNC='-jar app.jar'#" /apploader.sh

RUN mkdir /post-compute-tmp

WORKDIR /workplace/app

# finalize manifest
COPY gramine/entrypoint.manifest /

# Finalize app (finalize manifest and sign app)
RUN /finalize-app.sh
