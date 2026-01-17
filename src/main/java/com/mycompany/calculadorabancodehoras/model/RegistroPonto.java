package com.mycompany.calculadorabancodehoras.model;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "funcionarios_horas") // Este é o nome da tabela que você criou no SQL do Neon
public class RegistroPonto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nome_funcionario")
    private String nomeFuncionario;

    @Column(name = "data_registro")
    private LocalDate dataRegistro = LocalDate.now();

    @Column(name = "entrada_1")
    private String entrada1;

    @Column(name = "saida_1")
    private String saida1;

    @Column(name = "entrada_2")
    private String entrada2;

    @Column(name = "saida_2")
    private String saida2;

    @Column(name = "total_horas")
    private String totalHoras;

    // --- CONSTRUTORES ---
    public RegistroPonto() {
    }

    // --- GETTERS E SETTERS (Essenciais para o banco de dados funcionar) ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNomeFuncionario() { return nomeFuncionario; }
    public void setNomeFuncionario(String nomeFuncionario) { this.nomeFuncionario = nomeFuncionario; }

    public LocalDate getDataRegistro() { return dataRegistro; }
    public void setDataRegistro(LocalDate dataRegistro) { this.dataRegistro = dataRegistro; }

    public String getEntrada1() { return entrada1; }
    public void setEntrada1(String entrada1) { this.entrada1 = entrada1; }

    public String getSaida1() { return saida1; }
    public void setSaida1(String saida1) { this.saida1 = saida1; }

    public String getEntrada2() { return entrada2; }
    public void setEntrada2(String entrada2) { this.entrada2 = entrada2; }

    public String getSaida2() { return saida2; }
    public void setSaida2(String saida2) { this.saida2 = saida2; }

    public String getTotalHoras() { return totalHoras; }
    public void setTotalHoras(String totalHoras) { this.totalHoras = totalHoras; }
}