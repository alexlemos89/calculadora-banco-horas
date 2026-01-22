package com.mycompany.calculadorabancodehoras;

import com.fasterxml.jackson.annotation.JsonIgnore;
import javax.persistence.*;
import java.io.Serializable;
import java.util.*;

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
    
    // ADIÇÃO PARA CONTROLE DE ACESSO (ADMIN ou USER)
    private String perfil; 

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "historico_detalhado", joinColumns = @JoinColumn(name = "funcionario_id"))
    private List<String> historico = new ArrayList<>();

    public Funcionario() {}

    // --- CÁLCULOS BÁSICOS ---

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
    public String formatarQualquerSaldo(long minutos) {
        long totalMins = Math.abs(minutos);
        long horas = totalMins / 60;
        long mins = totalMins % 60;
        String sinal = (minutos < 0) ? "-" : "";
        return String.format("%s%02dh%02dmin", sinal, horas, mins);
    }

    // --- MÉTODOS DE FORMATAÇÃO ---

    @JsonIgnore @Transient
    public String getCargaDiariaFormatada() {
        return (this.cargaHorariaStr == null || this.cargaHorariaStr.isEmpty()) ? "00:00" : this.cargaHorariaStr;
    }

    @JsonIgnore @Transient
    public String getCargaSemanalFormatada() {
        long semanal = getMinsDiariosBase() * 5L; 
        return formatarQualquerSaldo(semanal).replace("-", "");
    }

    @JsonIgnore @Transient
    public String getCargaMensalFormatada() {
        long mensal = getMinsDiariosBase() * 22L; 
        return formatarQualquerSaldo(mensal).replace("-", "");
    }

    @JsonIgnore @Transient
    public String getReferenciaDias() {
        int diario = getMinsDiariosBase();
        return diario == 0 ? "0" : String.valueOf(Math.abs(saldoMinutos) / diario);
    }

    // --- MÉTODOS DE HISTÓRICO E SALDO ---

    @JsonIgnore @Transient
    public Map<Integer, Long> getSaldosPorAno() {
        Map<Integer, Long> saldos = new TreeMap<>(Collections.reverseOrder());
        if (historico == null) return saldos;
        for (String h : historico) {
            try {
                String dataStr = h.split(" - ")[0]; 
                int ano;
                if (dataStr.contains("/")) {
                    ano = Integer.parseInt(dataStr.substring(dataStr.lastIndexOf("/") + 1));
                } else {
                    ano = Integer.parseInt(dataStr.substring(0, 4));
                }
                String[] partes = h.split(" - ");
                String horaStr = partes[1].trim().split(" ")[0];
                String[] hm = horaStr.split(":");
                long m = (Long.parseLong(hm[0].trim()) * 60) + Long.parseLong(hm[1].trim());
                if (h.toUpperCase().contains("DÉBITO")) m = -m;
                saldos.put(ano, saldos.getOrDefault(ano, 0L) + m);
            } catch (Exception e) {}
        }
        return saldos;
    }

    @JsonIgnore @Transient
    public String getSaldoFormatado() {
        return formatarQualquerSaldo(this.saldoMinutos);
    }

    @JsonIgnore @Transient
    public boolean isSaldoPositivo() {
        return this.saldoMinutos >= 0;
    }

    @JsonIgnore @Transient
    public String getTextoEquivalenciaCompleta() {
        long totalMins = Math.abs(this.saldoMinutos);
        int cargaDiaria = getMinsDiariosBase();
        if (cargaDiaria <= 0) return "Equivalente a 0 dias";
        long dias = totalMins / cargaDiaria;
        long restoMinutos = totalMins % cargaDiaria;
        long horas = restoMinutos / 60;
        long minutos = restoMinutos % 60;
        return String.format("Equivalente a %d dias, %d horas e %d minutos", dias, horas, minutos);
    }

    // --- GETTERS E SETTERS ---
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
    public void setHistorico(List<String> historico) { this.historico = (historico != null) ? historico : new ArrayList<>(); }
    
    // Getter e Setter para o novo campo Perfil
    public String getPerfil() { return perfil; }
    public void setPerfil(String perfil) { this.perfil = perfil; }
}