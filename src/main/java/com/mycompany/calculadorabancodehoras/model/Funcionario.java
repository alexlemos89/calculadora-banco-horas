package com.mycompany.calculadorabancodehoras;

import com.fasterxml.jackson.annotation.JsonIgnore;
import javax.persistence.*;
import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

@Entity
@Table(name = "funcionarios")
public class Funcionario implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    private String registro;
    private String nome;
    private String tipoCarga; 
    private String cargaHorariaStr;
    private String senha;
    private long saldoMinutos = 0;

    // Mudamos o histórico para aceitar uma estrutura mais completa (ano, valor, data)
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "historico_detalhado", joinColumns = @JoinColumn(name = "funcionario_id"))
    private List<String> historico = new ArrayList<>();

    public Funcionario() {}

    // --- NOVA LÓGICA: SALDO POR ANO ---
    @JsonIgnore
    @Transient
    public Map<Integer, Long> getSaldosPorAno() {
        Map<Integer, Long> saldos = new TreeMap<>(Collections.reverseOrder()); // Anos mais recentes primeiro
        
        for (String h : historico) {
            try {
                // Formato esperado: "2024-05-10 - 02:00 (CRÉDITO)"
                String dataStr = h.split(" - ")[0];
                int ano = Integer.parseInt(dataStr.substring(0, 4));
                
                String[] partes = h.split(" - ");
                String horaStr = partes[1].substring(0, 5);
                String[] hm = horaStr.split(":");
                long m = (Long.parseLong(hm[0].trim()) * 60) + Long.parseLong(hm[1].trim());
                
                if (h.contains("DÉBITO")) m = -m;
                
                saldos.put(ano, saldos.getOrDefault(ano, 0L) + m);
            } catch (Exception e) {
                // Ignora linhas que não estejam no formato de data
            }
        }
        return saldos;
    }

    // Método para formatar qualquer saldo em minutos para "00h00min"
    @JsonIgnore
    @Transient
    public String formatarQualquerSaldo(long minutos) {
        long totalMins = Math.abs(minutos);
        long horas = totalMins / 60;
        long mins = totalMins % 60;
        String sinal = (minutos < 0) ? "-" : "";
        return String.format("%s%02dh%02dmin", sinal, horas, mins);
    }

    // --- MÉTODOS DE FORMATAÇÃO (Mantidos e Ajustados) ---
    @JsonIgnore @Transient
    public int getMinsDiariosBase() {
        try {
            if (cargaHorariaStr == null || !cargaHorariaStr.contains(":")) return 480;
            String[] p = cargaHorariaStr.split(":");
            int total = (Integer.parseInt(p[0].trim()) * 60) + Integer.parseInt(p[1].trim());
            String tipo = (tipoCarga == null) ? "DIÁRIA" : tipoCarga.toUpperCase();
            if (tipo.contains("SEMANAL")) return total / 5;
            if (tipo.contains("MENSAL")) return total / 22;
            return total;
        } catch (Exception e) { return 480; }
    }

    @JsonIgnore @Transient
    public String getResumoAnualFormatado() {
        StringBuilder resumo = new StringBuilder();
        Map<Integer, Long> saldos = getSaldosPorAno();
        int cargaMins = getMinsDiariosBase();

        for (Map.Entry<Integer, Long> entry : saldos.entrySet()) {
            long saldoAno = entry.getValue();
            resumo.append(entry.getKey()).append(": ")
                  .append(formatarQualquerSaldo(saldoAno))
                  .append(saldoAno < 0 ? " - Débito" : " - Crédito");
            
            // Cálculo de equivalência em dias
            if (cargaMins > 0) {
                long dias = Math.abs(saldoAno) / cargaMins;
                if (dias > 0) resumo.append(" (Equivale a ").append(dias).append(dias > 1 ? " dias)" : " dia)");
            }
            resumo.append("\n");
        }
        return resumo.toString();
    }

    // Getters e Setters (Mantidos)
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