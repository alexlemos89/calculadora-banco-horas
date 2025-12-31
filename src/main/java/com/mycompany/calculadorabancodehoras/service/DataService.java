package com.mycompany.calculadorabancodehoras.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mycompany.calculadorabancodehoras.Funcionario;
import org.springframework.stereotype.Service;
import javax.annotation.PostConstruct;
import java.io.File;
import java.nio.file.Paths;
import java.util.*;

@Service
public class DataService {
    private Map<String, Funcionario> banco = new HashMap<>();
    private final ObjectMapper mapper = new ObjectMapper();
    
    // Define um caminho absoluto para garantir que o arquivo não se perca nas pastas do sistema
    private final String CAMINHO_ARQUIVO = Paths.get("").toAbsolutePath().toString() + File.separator + "dados_calculadora.json";

    @PostConstruct
    public void carregarDadosIniciais() {
        try {
            File arquivo = new File(CAMINHO_ARQUIVO);
            // Log para você verificar no console do NetBeans onde o arquivo está sendo salvo
            System.out.println(">>> SISTEMA BUSCANDO DADOS EM: " + CAMINHO_ARQUIVO);
            
            if (arquivo.exists() && arquivo.length() > 0) {
                banco = mapper.readValue(arquivo, new TypeReference<HashMap<String, Funcionario>>() {});
                System.out.println(">>> SUCESSO: " + banco.size() + " FUNCIONÁRIOS CARREGADOS DO ARQUIVO.");
            } else {
                System.out.println(">>> AVISO: ARQUIVO NÃO ENCONTRADO OU VAZIO. INICIANDO BANCO NOVO.");
            }
        } catch (Exception e) {
            System.err.println(">>> ERRO AO CARREGAR DADOS: " + e.getMessage());
            banco = new HashMap<>();
        }
    }

    private void salvarNoArquivo() {
        try {
            mapper.writeValue(new File(CAMINHO_ARQUIVO), banco);
            System.out.println(">>> DADOS GRAVADOS NO DISCO COM SUCESSO.");
        } catch (Exception e) {
            System.err.println(">>> ERRO AO SALVAR NO DISCO: " + e.getMessage());
        }
    }

    public void salvar(Funcionario f) {
        if (f == null || f.getRegistro() == null || f.getRegistro().trim().isEmpty()) return;

        if (!banco.containsKey(f.getRegistro())) {
            banco.put(f.getRegistro(), f);
        } else {
            Funcionario existente = banco.get(f.getRegistro());
            existente.setNome(f.getNome());
            existente.setSenha(f.getSenha());
            existente.setTipoCarga(f.getTipoCarga());
            existente.setCargaHorariaStr(f.getCargaHorariaStr());
            recalcularSaldo(existente);
        }
        salvarNoArquivo();
    }

    public List<Funcionario> listarTodos() { return new ArrayList<>(banco.values()); }
    
    public Funcionario buscar(String registro) { return banco.get(registro); }

    public void excluir(String registro) { 
        banco.remove(registro); 
        salvarNoArquivo();
    }

    public void alterarSenha(String registro, String novaSenha) {
        Funcionario f = banco.get(registro);
        if (f != null) {
            f.setSenha(novaSenha);
            salvarNoArquivo();
        }
    }

    public void adicionarHoras(String registro, int minutos, String dataRef) {
        Funcionario f = banco.get(registro);
        if (f != null) {
            f.setSaldoMinutos(f.getSaldoMinutos() + minutos);
            String valorTexto = String.format("%02d:%02d", Math.abs(minutos)/60, Math.abs(minutos)%60);
            String operacao = (minutos >= 0) ? "CRÉDITO" : "DÉBITO";
            f.getHistorico().add(dataRef + " - " + valorTexto + " (" + operacao + ")");
            salvarNoArquivo();
        }
    }

    public void substituirLancamento(String registro, int indice, String novaData, int novosMins) {
        Funcionario f = banco.get(registro);
        if (f != null && indice < f.getHistorico().size()) {
            f.getHistorico().remove(indice);
            String valorTexto = String.format("%02d:%02d", Math.abs(novosMins)/60, Math.abs(novosMins)%60);
            String operacao = (novosMins >= 0) ? "CRÉDITO" : "DÉBITO";
            f.getHistorico().add(indice, novaData + " - " + valorTexto + " (" + operacao + ")");
            recalcularSaldo(f);
            salvarNoArquivo();
        }
    }

    private void recalcularSaldo(Funcionario f) {
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
            } catch (Exception e) {}
        }
        f.setSaldoMinutos(total);
    }
}