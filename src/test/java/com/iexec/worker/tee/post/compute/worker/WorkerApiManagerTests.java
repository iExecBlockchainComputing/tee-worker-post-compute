package com.iexec.worker.tee.post.compute.worker;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import uk.org.webcompere.systemstubs.environment.EnvironmentVariables;
import uk.org.webcompere.systemstubs.jupiter.SystemStubsExtension;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

@ExtendWith(SystemStubsExtension.class)
class WorkerApiManagerTests {
    // region getWorkerHost
    @Test
    void shouldGetWorkerHostFromEnvVar(EnvironmentVariables variables) {
        final String workerHost = "host:1234";
        variables.set("WORKER_HOST", workerHost);

        final String result = WorkerApiManager.getWorkerHost();
        assertSame(workerHost, result);
    }

    @Test
    void shouldUseDefaultWorkerHost() {
        final String result = WorkerApiManager.getWorkerHost();
        assertSame("worker:13100", result);
    }
    // endregion

    // region Constructor
    @Test
    void shouldNotCreateNewInstance() throws NoSuchMethodException {
        final Constructor<WorkerApiManager> constructor = WorkerApiManager.class.getDeclaredConstructor();
        constructor.setAccessible(true);

        final InvocationTargetException exception = assertThrows(InvocationTargetException.class, constructor::newInstance);
        assertTrue(exception.getTargetException() instanceof UnsupportedOperationException);
    }
    // endregion

    // region getWorkerApiClient
    @Test
    void shouldGetExistingWorkerApiClient() throws NoSuchFieldException, IllegalAccessException {
        final WorkerApiClient workerApiClient = mock(WorkerApiClient.class);
        final Field workerApiClientField = WorkerApiManager.class.getDeclaredField("workerApiClient");
        workerApiClientField.setAccessible(true);
        workerApiClientField.set(null, workerApiClient);

        assertSame(workerApiClient, WorkerApiManager.getWorkerApiClient());
    }

    @Test
    void shouldCreateWorkerApiClient() throws NoSuchFieldException, IllegalAccessException {
        final Field workerApiClientField = WorkerApiManager.class.getDeclaredField("workerApiClient");
        workerApiClientField.setAccessible(true);
        workerApiClientField.set(null, null);

        assertNull(workerApiClientField.get(null));

        final WorkerApiClient workerApiClient = WorkerApiManager.getWorkerApiClient();
        assertNotNull(workerApiClient);
    }
    // endregion
}