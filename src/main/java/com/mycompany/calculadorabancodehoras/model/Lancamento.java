package com.mycompany.calculadorabancodehoras.model;

import java.io.Serializable;

public class Lancamento implements Serializable {
    private static final long serialVersionUID = 1L;
    public String mesAnoLancamento;
    public String tipo;
    public int valorMinutos;
    public String inputStr;

    public Lancamento(String mesAnoLancamento, String tipo, int valorMinutos, String inputStr) {
        this.mesAnoLancamento = mesAnoLancamento;
        this.tipo = tipo;
        this.valorMinutos = valorMinutos;
        this.inputStr = inputStr;
    }

    @Override
    public String toString() {
        return String.format("%s (%s | Mês/Ano: %s)", inputStr, tipo, mesAnoLancamento);
    }
}