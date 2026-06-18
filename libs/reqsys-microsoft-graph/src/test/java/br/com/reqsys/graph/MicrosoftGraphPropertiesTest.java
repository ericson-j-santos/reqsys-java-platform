package br.com.reqsys.graph;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MicrosoftGraphPropertiesTest {

    @Test
    void deveUsarDefaultsSeguros() {
        var properties = new MicrosoftGraphProperties();

        assertEquals("https://graph.microsoft.com/v1.0", properties.getGraphBaseUrl());
        assertEquals("https://graph.microsoft.com/.default", properties.getScope());
    }
}
