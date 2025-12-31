package com.mycompany.calculadorabancodehoras;

import com.fasterxml.jackson.annotation.JsonIgnore; // IMPORTANTE: Adicione este import
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Funcionario implements Serializable {
    private static final long serialVersionUID = 1L;

    private String nome;
    private String registro;
    private String tipoCarga; // DIÁRIA, SEMANAL ou MENSAL
    private String cargaHorariaStr;
    private String senha;
    private long saldoMinutos = 0;
    private List<String> historico = new ArrayList<>();

    public Funcionario() {}

    // --- LÓGICA DE CONVERSÃO AUTOMÁTICA ---

    @JsonIgnore // Adicione isso para o Jackson não tentar salvar este cálculo
    public int getMinsDiariosBase() {
        try {
            if (cargaHorariaStr == null || !cargaHorariaStr.contains(":")) return 480;
            String[] p = cargaHorariaStr.split(":");
            int totalDigitado = (Integer.parseInt(p[0].trim()) * 60) + Integer.parseInt(p[1].trim());
            if (totalDigitado <= 0) return 480;

            String tipo = (tipoCarga == null) ? "DIÁRIA" : tipoCarga.toUpperCase();
            if (tipo.contains("SEMANAL")) return totalDigitado / 5;
            if (tipo.contains("MENSAL")) return totalDigitado / 22;
            return totalDigitado;
        } catch (Exception e) {
            return 480;
        }
    }

    @JsonIgnore // Ignorar no salvamento JSON
    public String getCargaDiariaFormatada() {
        int m = getMinsDiariosBase();
        return String.format("%02d:%02d", m / 60, m % 60);
    }

    @JsonIgnore // Ignorar no salvamento JSON
    public String getCargaSemanalFormatada() {
        int m = getMinsDiariosBase() * 5;
        return String.format("%02d:%02d", m / 60, m % 60);
    }

    @JsonIgnore // Ignorar no salvamento JSON
    public String getCargaMensalFormatada() {
        int m = getMinsDiariosBase() * 22;
        return String.format("%02d:%02d", m / 60, m % 60);
    }

    @JsonIgnore // Ignorar no salvamento JSON
    public String getReferenciaDias() {
        try {
            int cargaMins = getMinsDiariosBase();
            if (saldoMinutos == 0) return "";
            long totalSaldo = Math.abs(saldoMinutos);
            long dias = totalSaldo / cargaMins;
            long sobra = totalSaldo % cargaMins;
            long horas = sobra / 60;
            long mins = sobra % 60;

            StringBuilder frase = new StringBuilder();
            frase.append(" (Equivale a: ");
            if (dias > 0) frase.append(dias).append(dias > 1 ? " Dias" : " Dia");
            if (horas > 0 || mins > 0) {
                if (dias > 0) frase.append(" e ");
                frase.append(String.format("%02dh%02d", horas, mins));
            }
            frase.append(")");
            return frase.toString();
        } catch (Exception e) {
            return "";
        }
    }

    @JsonIgnore // Ignorar no salvamento JSON
    public String getSaldoFormatado() {
        long totalMins = Math.abs(saldoMinutos);
        long horas = totalMins / 60;
        long minutos = totalMins % 60;
        String sinal = (saldoMinutos < 0) ? "-" : "";
        return String.format("%s%02d:%02d", sinal, horas, minutos);
    }

    // --- GETTERS E SETTERS PADRÃO (Estes o Jackson DEVE salvar) ---
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getRegistro() { return registro; }
    public void setRegistro(String registro) { this.registro = registro; }
    public String getTipoCarga() { return tipoCarga; }
    public void setTipoCarga(String tipoCarga) { this.tipoCarga = tipoCarga; }
    public String getCargaHorariaStr() { return cargaHorariaStr; }
    public void setCargaHorariaStr(String cargaHorariaStr) { this.cargaHorariaStr = cargaHorariaStr; }
    public String getSenha() { return senha; }
    public void setSenha(String senha) { this.senha = senha; }
    public long getSaldoMinutos() { return saldoMinutos; }
    public void setSaldoMinutos(long saldoMinutos) { this.saldoMinutos = saldoMinutos; }
    public List<String> getHistorico() { return historico; }
    public void setHistorico(List<String> historico) { this.historico = historico; }
}