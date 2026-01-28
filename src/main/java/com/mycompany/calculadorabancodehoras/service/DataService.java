package com.mycompany.calculadorabancodehoras.service;

import com.mycompany.calculadorabancodehoras.model.Funcionario; 
import com.mycompany.calculadorabancodehoras.repository.RegistroRepository; 
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.ArrayList;
import org.springframework.data.domain.Sort;

@Service
public class DataService {

    @Autowired
    private RegistroRepository repository;

    public void salvar(Funcionario f) {
        if (f == null || f.getRegistro() == null) return;
        
        Funcionario existente = buscar(f.getRegistro());
        if (existente != null) {
            // AJUSTE PARA O BOTÃO 6: 
            // Só atualizamos o Nome, Senha e Perfil se eles vierem preenchidos da tela.
            // Se vierem nulos (como no caso do botão 6), mantemos o que já está no banco.
            
            if (f.getNome() != null && !f.getNome().isEmpty()) {
                existente.setNome(f.getNome());
            }
            if (f.getSenha() != null && !f.getSenha().isEmpty()) {
                existente.setSenha(f.getSenha());
            }
            if (f.getPerfil() != null && !f.getPerfil().isEmpty()) {
                existente.setPerfil(f.getPerfil());
            }
            
            // Campos de Carga Horária (sempre atualizados pelo Botão 6)
            existente.setCargaHorariaStr(f.getCargaHorariaStr()); 
            existente.setTipoCarga(f.getTipoCarga());
            
            // Garante que o histórico não seja perdido
            if (existente.getHistorico() == null) {
                existente.setHistorico(new ArrayList<>());
            }
            
            repository.save(existente);
        } else {
            // Cadastro de novo funcionário
            if (f.getHistorico() == null) f.setHistorico(new ArrayList<>());
            repository.save(f);
        }
    }

    public List<Funcionario> listarTodos() {
        return repository.findAll(Sort.by(Sort.Direction.ASC, "nome"));
    }

    public Funcionario buscar(String registro) {
        return repository.findById(registro).orElse(null);
    }

    public void excluir(String registro) { 
        repository.deleteById(registro);
    }

    public void alterarSenha(String registro, String novaSenha) {
        Funcionario f = buscar(registro);
        if (f != null) {
            f.setSenha(novaSenha);
            repository.save(f);
        }
    }

    public void adicionarHoras(String registro, int minutos, String dataRef) {
        Funcionario f = buscar(registro);
        if (f != null) {
            f.setSaldoMinutos(f.getSaldoMinutos() + minutos);
            String valorTexto = String.format("%02d:%02d", Math.abs(minutos)/60, Math.abs(minutos)%60);
            String operacao = (minutos >= 0) ? "CRÉDITO" : "DÉBITO";
            if (f.getHistorico() == null) f.setHistorico(new ArrayList<>());
            f.getHistorico().add(dataRef + " - " + valorTexto + " (" + operacao + ")");
            repository.save(f);
        }
    }

    public void excluirLancamento(String registro, int indice) {
        Funcionario f = buscar(registro);
        if (f != null && f.getHistorico() != null && indice >= 0 && indice < f.getHistorico().size()) {
            f.getHistorico().remove(indice);
            recalcularSaldo(f);
            repository.save(f);
        }
    }

    public void substituirLancamento(String registro, int indice, String novaData, int novosMins) {
        Funcionario f = buscar(registro);
        if (f != null && f.getHistorico() != null && indice >= 0 && indice < f.getHistorico().size()) {
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
                    String parteHoraETipo = partes[1];
                    String horaStr = parteHoraETipo.substring(0, 5);
                    String[] hm = horaStr.split(":");
                    int m = (Integer.parseInt(hm[0].trim()) * 60) + Integer.parseInt(hm[1].trim());
                    if (h.toUpperCase().contains("DÉBITO")) m = -m;
                    total += m;
                }
            } catch (Exception e) {}
        }
        f.setSaldoMinutos(total);
    }
}