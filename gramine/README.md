## How to run demo with Gramine

1. Start a SPS container:
```shell
docker run -d -v /opt/multiple/sessions:/graphene/workplace/sessions -v /opt/secret-prov/certs/:/graphene/workplace/certs -p 8080:8080 -p 4433:4433 -e SPS_USERNAME=admin -e SPS_PASSWORD=admin --name iexec-sps iexechub/iexec-sps:bf3bb00-dev
```


2. Build your app:
```shell
docker build -t gramine-tee-worker-post-compute:latest -f gramine/Dockerfile  .
```
Please note the `measurement` value.


3. To add a session to the SPS, run the following after having filled both env var:
```shell
SESSION_ID=<define your custom session id>
MEASUREMENT=<set previous retrieved measurement>
SPS_IP=<set SPS IP>

TASK_ID=<set task ID>
RESULT_STORAGE_CALLBACK=<set whether you use callback>
RESULT_SIGN_WORKER_ADDRESS=<set worker address>
RESULT_SIGN_TEE_CHALLENGE_PRIVATE_KEY=<set TEE challenge private key>
RESULT_STORAGE_TOKEN=<set result storage token>
RESULT_STORAGE_PROXY=<set result storage proxy>

curl --location --request POST 'localhost:8080/api/session/' \
--header 'Authorization: Basic YWRtaW46YWRtaW4=' \
--header 'Content-Type: application/json' \
--data-raw '{
  "session": "'${SESSION_ID}'",
  "enclaves": [
    {
      "name": "Post Compute",
      "mrenclave": "'${MEASUREMENT}'",
      "command": "/apploader.sh",
      "environment": {
        "RESULT_TASK_ID": "'${TASK_ID}'",
        "RESULT_STORAGE_CALLBACK": "'${RESULT_STORAGE_CALLBACK}'",
        "RESULT_SIGN_WORKER_ADDRESS": "'${RESULT_SIGN_WORKER_ADDRESS}'",
        "RESULT_STORAGE_TOKEN": "'${RESULT_STORAGE_TOKEN}'",
        "RESULT_STORAGE_PROXY": "'${RESULT_STORAGE_PROXY}'",
        "RESULT_SIGN_TEE_CHALLENGE_PRIVATE_KEY": "'${RESULT_SIGN_TEE_CHALLENGE_PRIVATE_KEY}'"
      },
      "volumes": [
        "/iexec_out"
      ]
    }
  ]
}'
```


4. Run the app:
```shell
docker run --device=/dev/sgx/enclave -v "/iexec_out/${TASK_ID}:/iexec_out" -v /var/run/aesmd/aesm.socket:/var/run/aesmd/aesm.socket -v $PWD/encryptedData:/workplace/encryptedData -v /opt/secret-prov/certs/:/graphene/attestation/certs/ -e session=${SESSION_ID} -e sps=${SPS_IP}  gramine-tee-worker-post-compute:latest
```


### Troubleshooting:

#### "Get keys failed"
When the app can't communicate with the SPS, you can encounter some numeric error codes, in the following format:
```
[error] connect to kms failed, kms_endpoint is iexec-sps:4433, cert_path is /graphene/attestation/certs/test-ca-sha256.crt
[error] get keys failed, return -[ERROR_CODE] 
```

Depending on the error code, the issue is the following:

| Error code |       Error       |                                                         Solution                                                          |
|:----------:|:-----------------:|:-------------------------------------------------------------------------------------------------------------------------:|
|    111     |  Can't reach SPS  |                                       Check SPS IP is correct in app configuration.                                       |
|    9984    | Certificate error | Check both app & SPS share a valid certificate. Regenerate it if needed, providing SPS IP as `Common Name` when prompted. |
