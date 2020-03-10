package com.iexec.worker.tee.post.compute.uploader;

import com.iexec.common.result.ResultModel;
import com.iexec.common.result.eip712.Eip712Challenge;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@FeignClient(name = "ResultRepoClient",
        url = "#{publicConfigurationService.resultRepositoryURL}",
        configuration = FeignConfiguration.class)
public interface ResultClient {

    @GetMapping("/results/challenge")
    ResponseEntity<Eip712Challenge> getChallenge(@RequestParam(name = "chainId") Integer chainId) throws FeignException;

    @PostMapping("/results")
    ResponseEntity<String> uploadResult(@RequestHeader("Authorization") String customToken,
                                        @RequestBody ResultModel resultModel);

}