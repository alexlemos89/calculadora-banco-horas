package com.mycompany.calculadorabancodehoras.service;

import com.mycompany.calculadorabancodehoras.Funcionario;

// --- AQUI ESTÁ A CONEXÃO CORRETA ---
import com.mycompany.calculadorabancodehoras.repository.RegistroRepository; 
// -----------------------------------

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import org.springframework.data.domain.Sort;

@Service
public class DataService {

    @Autowired
    private RegistroRepository repository; // Agora usa o seu RegistroRepository

    // 1. SALVAR
    public void salvar(Funcionario f) {
        if (f == null || f.getRegistro() == null) return;
        repository.save(f);
    }

    // 2. LISTAR (Em Ordem Alfabética Direto do Banco)
    public List<Funcionario> listarTodos() {
        return repository.findAll(Sort.by(Sort.Direction.ASC, "nome"));
    }
    
    // 3. BUSCAR
    public Funcionario buscar(String registro) {
        return repository.findById(registro).orElse(null);
    }

    // 4. EXCLUIR FUNCIONÁRIO
    public void excluir(String registro) { 
        repository.deleteById(registro);
    }

    // 5. ALTERAR SENHA
    public void alterarSenha(String registro, String novaSenha) {
        Funcionario f = buscar(registro);
        if (f != null) {
            f.setSenha(novaSenha);
            repository.save(f);
        }
    }

    // 6. ADICIONAR HORAS (LANÇAMENTO)
    public void adicionarHoras(String registro, int minutos, String dataRef) {
        Funcionario f = buscar(registro);
        if (f != null) {
            // Atualiza Saldo
            f.setSaldoMinutos(f.getSaldoMinutos() + minutos);
            
            // Cria Linha de Histórico
            String valorTexto = String.format("%02d:%02d", Math.abs(minutos)/60, Math.abs(minutos)%60);
            String operacao = (minutos >= 0) ? "CRÉDITO" : "DÉBITO";
            f.getHistorico().add(dataRef + " - " + valorTexto + " (" + operacao + ")");
            
            // GRAVA NO BANCO
            repository.save(f);
        }
    }

    // 7. EXCLUIR LANÇAMENTO
    public void excluirLancamento(String registro, int indice) {
        Funcionario f = buscar(registro);
        if (f != null && indice >= 0 && indice < f.getHistorico().size()) {
            f.getHistorico().remove(indice);
            recalcularSaldo(f);
            repository.save(f);
        }
    }

    // 8. SUBSTITUIR LANÇAMENTO
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

    // 9. RECALCULAR SALDO (Segurança)
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