package br.com.reqsys.common.lgpd;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

class PiiMaskerTest {
    @Test
    void deveMascararCpf() {
        assertEquals("***.***.***-09", PiiMasker.cpf("12345678909"));
    }

    @Test
    void deveMascararEmail() {
        assertEquals("er***@exemplo.com", PiiMasker.email("ericson@exemplo.com"));
    }
}
