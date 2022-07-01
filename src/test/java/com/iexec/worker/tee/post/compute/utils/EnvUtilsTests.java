package com.iexec.worker.tee.post.compute.utils;

import com.iexec.worker.tee.post.compute.PostComputeException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import uk.org.webcompere.systemstubs.environment.EnvironmentVariables;
import uk.org.webcompere.systemstubs.jupiter.SystemStubsExtension;

import static com.iexec.common.replicate.ReplicateStatusCause.POST_COMPUTE_TASK_ID_MISSING;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SystemStubsExtension.class)
class EnvUtilsTests {
    private static final String ENVIRONMENT_VAR = "envVar";
    private static final String ENVIRONMENT_VAR_VALUE = "envVarValue";

    // region getEnvVar
    @Test
    void shouldGetEnvVar(EnvironmentVariables environment) {
        environment.set(ENVIRONMENT_VAR, ENVIRONMENT_VAR_VALUE);
        assertEquals(ENVIRONMENT_VAR_VALUE, EnvUtils.getEnvVar(ENVIRONMENT_VAR));
    }

    @Test
    void shouldNotGetEnvVarSinceEmptyVar(EnvironmentVariables environment) {
        environment.set(ENVIRONMENT_VAR, "");
        assertEquals("", EnvUtils.getEnvVar(ENVIRONMENT_VAR));
    }

    @Test
    void shouldNotGetEnvVarSinceUnknownVar() {
        assertEquals("", EnvUtils.getEnvVar(ENVIRONMENT_VAR));
    }
    // endregion

    // region getEnvVarOrThrow
    @Test
    void shouldGetEnvVarOrThrow(EnvironmentVariables environment) {
        environment.set(ENVIRONMENT_VAR, ENVIRONMENT_VAR_VALUE);
        final String actualValue = assertDoesNotThrow(() -> EnvUtils.getEnvVarOrThrow(ENVIRONMENT_VAR, POST_COMPUTE_TASK_ID_MISSING));
        assertEquals(ENVIRONMENT_VAR_VALUE, actualValue);
    }

    @Test
    void shouldNotGetEnvVarOrThrowSinceEmptyVar(EnvironmentVariables environment) {
        environment.set(ENVIRONMENT_VAR, "");
        final PostComputeException exception = assertThrows(PostComputeException.class, () -> EnvUtils.getEnvVarOrThrow(ENVIRONMENT_VAR, POST_COMPUTE_TASK_ID_MISSING));
        assertEquals(POST_COMPUTE_TASK_ID_MISSING, exception.getStatusCause());
        assertEquals("Required env var is empty [name:" + ENVIRONMENT_VAR + "]", exception.getMessage());
    }

    @Test
    void shouldNotGetEnvVarOrThrowSinceUnknownVar() {
        final PostComputeException exception = assertThrows(PostComputeException.class, () -> EnvUtils.getEnvVarOrThrow(ENVIRONMENT_VAR, POST_COMPUTE_TASK_ID_MISSING));
        assertEquals(POST_COMPUTE_TASK_ID_MISSING, exception.getStatusCause());
        assertEquals("Required env var is empty [name:" + ENVIRONMENT_VAR + "]", exception.getMessage());
    }
    // endregion
}