FROM sconecuratedimages/iexec:openjdk11-alpine3.10-test
COPY --from=sconecuratedimages/iexec:cli-alpine /opt/scone/scone-cli /opt/scone/scone-cli
COPY --from=sconecuratedimages/iexec:cli-alpine /usr/local/bin/scone /usr/local/bin/scone
COPY --from=sconecuratedimages/iexec:cli-alpine /opt/scone/bin /opt/scone/bin
RUN apk add bash --no-cache

RUN echo "http://dl-cdn.alpinelinux.org/alpine/v3.5/community" >> /etc/apk/repositories \
    && apk update

COPY build/libs/dropbox-uploader-@projectversion@.jar /app/dropbox-uploader.jar
COPY build/resources/main/entrypoint.sh /app/entrypoint.sh
RUN chmod +x /app/entrypoint.sh

RUN which java

RUN SCONE_MODE=sim SCONE_HEAP=128M		    \
	&& mkdir conf							    \
	&& scone fspf create fspf.pb 					    \
	&& scone fspf addr fspf.pb /  --not-protected --kernel /            \
	&& scone fspf addr fspf.pb /usr --authenticated --kernel /usr       \
	&& scone fspf addf fspf.pb /usr /usr 			            \
	&& scone fspf addr fspf.pb /bin --authenticated --kernel /bin       \
	&& scone fspf addf fspf.pb /bin /bin 			            \
	&& scone fspf addr fspf.pb /lib --authenticated --kernel /lib       \
	&& scone fspf addf fspf.pb /lib /lib 			            \
	&& scone fspf addr fspf.pb /etc/ssl --authenticated --kernel /etc/ssl       \
	&& scone fspf addf fspf.pb /etc/ssl /etc/ssl 			            \
	&& scone fspf addr fspf.pb /sbin --authenticated --kernel /sbin     \
	&& scone fspf addf fspf.pb /sbin /sbin 			            \	    \
	&& scone fspf addr fspf.pb /app --authenticated --kernel /app 	    \
	&& scone fspf addf fspf.pb /app /app 				    \
	&& scone fspf encrypt ./fspf.pb > /conf/keytag 			    \
        && MRENCLAVE="$(SCONE_HASH=1 SCONE_HEAP=3G java)"			            \
	&& FSPF_TAG=$(cat conf/keytag | awk '{print $9}') 	            \
	&& FSPF_KEY=$(cat conf/keytag | awk '{print $11}')		    \
	&& FINGERPRINT="$FSPF_KEY|$FSPF_TAG|$MRENCLAVE"			    \
	&& echo $FINGERPRINT > conf/fingerprint.txt			    \
	&& printf "\n########################################################\nMREnclave: \n$FINGERPRINT\n########################################################\n\n"

ENV LOCAL_FILE_PATH /upload

ENTRYPOINT ["/app/entrypoint.sh"]


# gradle buildImage
# or
# docker image build -f Dockerfile-untrusted -t nexus.iex.ec/dropbox-uploader:1.0.0 . --no-cache