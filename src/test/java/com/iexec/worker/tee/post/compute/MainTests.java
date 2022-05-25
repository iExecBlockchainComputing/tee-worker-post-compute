package com.iexec.worker.tee.post.compute;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.org.webcompere.systemstubs.SystemStubs.catchSystemExit;

class MainTests {

    @BeforeEach
    void init() {
        Main.postComputeAppRunner = null;
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9})
    void shouldExitWithCode(int expectedExitCode) throws Exception {
        final PostComputeAppRunner postComputeAppRunner = mock(PostComputeAppRunner.class);
        when(postComputeAppRunner.start()).thenReturn(expectedExitCode);
        Main.postComputeAppRunner = postComputeAppRunner;

        final int actualExitCode = catchSystemExit(() -> Main.main(null));
        assertEquals(expectedExitCode, actualExitCode);
    }

    @Test
    void shouldGetExistingPostComputeAppRunner() {
        final PostComputeAppRunner postComputeAppRunner = new PostComputeAppRunner();
        Main.postComputeAppRunner = postComputeAppRunner;

        assertSame(postComputeAppRunner, Main.getPostComputeAppRunner());
    }

    @Test
    void shouldCreatePostComputeAppRunner() {
        assertNotNull(Main.getPostComputeAppRunner());
    }
}