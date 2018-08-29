package com.github.joostvdg.demo;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class HelloTest {

    @Test
    public void shouldReturnHelloWorld() {
        String expected = "Hello world!";
        Hello hello = new Hello();
        String actual = hello.hello();
        assertEquals(expected, actual);
    }
}
