package com.mycompany.calculadorabancodehoras.service;

import com.mycompany.calculadorabancodehoras.Funcionario;
import com.mycompany.calculadorabancodehoras.repository.RegistroRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DataService {

    @Autowired
    private RegistroRepository repository;

    // 1. SALVAR
    public void salvar(Funcionario f) {
        if (f == null || f.getRegistro() == null) return;
        repository.save(f);
    }

    // 2. LISTAR (Em Ordem Alfabética)
    public List<Funcionario> listarTodos() {
        return repository.findAll().stream()
                .sorted(Comparator.comparing(Funcionario::getNome, String.CASE_INSENSITIVE_ORDER))
                .collect(Collectors.toList());
    }
    
    public Funcionario buscar(String registro) {
        return repository.findById(registro).orElse(null);
    }

    public void excluir(String registro) { 
        repository.deleteById(registro);
    }

    // 3. ALTERAR SENHA (Método que estava faltando)
    public void alterarSenha(String registro, String novaSenha) {
        Funcionario f = buscar(registro);
        if (f != null) {
            f.setSenha(novaSenha);
            repository.save(f);
            System.out.println(">>> SENHA ALTERADA COM SUCESSO NO BANCO.");
        }
    }

    // 4. EXCLUIR LANÇAMENTO ESPECÍFICO
    public void excluirLancamento(String registro, int indice) {
        Funcionario f = buscar(registro);
        if (f != null && indice >= 0 && indice < f.getHistorico().size()) {
            f.getHistorico().remove(indice);
            recalcularSaldo(f);
            repository.save(f);
        }
    }

    public void adicionarHoras(String registro, int minutos, String dataRef) {
        Funcionario f = buscar(registro);
        if (f != null) {
            f.setSaldoMinutos(f.getSaldoMinutos() + minutos);
            String valorTexto = String.format("%02d:%02d", Math.abs(minutos)/60, Math.abs(minutos)%60);
            String operacao = (minutos >= 0) ? "CRÉDITO" : "DÉBITO";
            f.getHistorico().add(dataRef + " - " + valorTexto + " (" + operacao + ")");
            repository.save(f);
        }
    }

    public void substituirLancamento(String registro, int indice, String novaData, int novosMins) {
        Funcionario f = buscar(registro);
        if (f != null && indice >= 0 && indice < f.getHistorico().size()) {
            f.getHistorico().remove(indice);
            String valorTexto = String.format("%02d:%02d", Math.abs(novosMins)/60, Math.abs(novosMins)%60);
            String operacao = (novosMins >= 0) ? "CRÉDITO" : "DÉBITO";
            f.getHistorico().add(indice, novaData + " - " + valorTexto + " (" + operacao + ")");
            recalcularSaldo(f);
            repository.save(f);
        }
    }

    public void recalcularSaldo(Funcionario f) {
        if (f == null || f.getHistorico() == null) return;
        long total = 0;
        for (String h : f.getHistorico()) {
            try {
                String[] partes = h.split(" - ");
                if (partes.length >= 2) {
                    String horaStr = partes[1].substring(0, 5);
                    String[] hm = horaStr.split(":");
                    int m = (Integer.parseInt(hm[0].trim()) * 60) + Integer.parseInt(hm[1].trim());
                    if (h.contains("DÉBITO")) m = -m;
                    total += m;
                }
            } catch (Exception e) {
                // Linha de histórico inválida ignorada
            }
        }
        f.setSaldoMinutos(total);
    }
}