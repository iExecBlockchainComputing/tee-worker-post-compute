# tee-worker-post-compute

What is it?

The TEE Worker Post-Compute component starts right after the execution of an iExec developer's TEE application.
It replaces a sensible part of the standard iExec Worker which needs to be trusted and confidential.

This component is an application running inside a trusted enclave which will:

1. Encrypt the result

2. Upload the result
  a. For cloud computing: it uploads the result of the task to IPFS or Dropbox
  b. For off-chain computing: it prepares a valid callback for the worker's on-chain contribution

3. Sign the result of the computation for the worker's contribution
