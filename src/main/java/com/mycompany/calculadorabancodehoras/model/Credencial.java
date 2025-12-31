package com.mycompany.calculadorabancodehoras.model;
import java.io.Serializable;

public class Credencial implements Serializable {
    private static final long serialVersionUID = 1L;
    public String registro, senha;

    public Credencial(String registro, String senha) {
        this.registro = registro;
        this.senha = senha;
    }
}