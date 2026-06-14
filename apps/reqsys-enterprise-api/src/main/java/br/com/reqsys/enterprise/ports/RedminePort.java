package br.com.reqsys.enterprise.ports;

import br.com.reqsys.enterprise.domain.Requisito;

public interface RedminePort {
    int publicarRequisito(Requisito requisito);
}
