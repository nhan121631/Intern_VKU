package com.vku.job.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class DemoTest {
    @Mock
    private List<String> list;

    @Test
    void testMock() {
        Mockito.when(list.size()).thenReturn(5);
        assertEquals(5, list.size());
    }
}
