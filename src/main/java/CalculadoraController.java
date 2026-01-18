package com.mycompany.calculadorabancodehoras;

import com.mycompany.calculadorabancodehoras.service.DataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.ArrayList;

@Controller
@SessionAttributes("f")
public class CalculadoraController {

    @Autowired
    private DataService dataService;

    @GetMapping("/")
    public String inicial() { 
        return "login"; 
    }

    @PostMapping("/logar")
    public String logar(@RequestParam String usuario, @RequestParam String senha, HttpSession session, Model model) {
        if ("administrador".equals(usuario) && "imrealapa".equals(senha)) {
            session.setAttribute("admin", true);
            return "redirect:/dashboard";
        }
        Funcionario f = dataService.buscar(usuario);
        if (f != null && f.getSenha().equals(senha)) {
            session.setAttribute("usuarioLogado", f.getRegistro());
            model.addAttribute("f", f);
            return "redirect:/dashboard"; 
        }
        return "redirect:/?erro";
    }

    @GetMapping("/dashboard")
    public String dashboard() { 
        return "index"; 
    }

    @GetMapping("/consultar")
    public String consultar(Model model) {
        try {
            List<Funcionario> lista = dataService.listarTodos();
            if (lista == null) lista = new ArrayList<>();
            model.addAttribute("lista", lista);
        } catch (Exception e) {
            model.addAttribute("lista", new ArrayList<Funcionario>());
            model.addAttribute("erro", "Erro ao acessar o banco de dados.");
        }
        return "consulta";
    }

    @PostMapping("/buscar-dados")
    public String buscarDados(@RequestParam("registro") String registro, Model model) {
        Funcionario f = dataService.buscar(registro);
        if (f != null) {
            model.addAttribute("f", f);
        } else {
            model.addAttribute("erro", "Registro não encontrado!");
        }
        model.addAttribute("lista", dataService.listarTodos());
        return "consulta";
    }

    @GetMapping("/relatorio")
    public String relatorioGeral(Model model) {
        model.addAttribute("lista", dataService.listarTodos());
        return "relatorio";
    }

    @GetMapping("/registrar")
    public String registrar() { 
        return "registro"; 
    }

    @PostMapping("/salvar-funcionario")
    public String salvar(Funcionario f) {
        dataService.salvar(f);
        return "redirect:/registrar?sucesso";
    }

    @GetMapping("/lancar")
    public String lancar(Model model) {
        model.addAttribute("lista", dataService.listarTodos());
        return "lancamento";
    }

    @PostMapping("/confirmar-lancamento")
    public String confirmarLancamento(@RequestParam String registro, 
                                      @RequestParam String valor, 
                                      @RequestParam String tipo, 
                                      @RequestParam String dataRef) {
        try {
            String[] partes = valor.split(":");
            int mins = (Integer.parseInt(partes[0].trim()) * 60) + Integer.parseInt(partes[1].trim());
            if ("debito".equals(tipo)) mins = -mins;
            dataService.adicionarHoras(registro, mins, dataRef);
        } catch (Exception e) {
            return "redirect:/lancar?erro";
        }
        return "redirect:/lancar?sucesso";
    }

    // --- GERENCIAR LANÇAMENTOS E DADOS DO FUNCIONÁRIO ---
    @GetMapping("/editar-lancamento")
    public String edLanc(@RequestParam(required = false) String registro, 
                         @RequestParam(required = false) Integer idx, Model model) {
        model.addAttribute("lista", dataService.listarTodos());
        
        if (registro != null) {
            Funcionario fSel = dataService.buscar(registro);
            model.addAttribute("fSel", fSel);
            
            // Se clicou em editar um lançamento da lista
            if (idx != null && fSel != null && idx < fSel.getHistorico().size()) {
                String linha = fSel.getHistorico().get(idx);
                try {
                    model.addAttribute("idxEd", idx);
                    model.addAttribute("dataEd", linha.split(" - ")[0]);
                    String info = linha.split(" - ")[1];
                    model.addAttribute("horaEd", info.split(" ")[0]);
                    model.addAttribute("tipoEd", linha.toUpperCase().contains("DÉBITO") ? "debito" : "credito");
                } catch (Exception e) {}
            }
        }
        return "ajustar";
    }

    // NOVA FUNÇÃO: Atualiza Nome, Senha e Carga do Funcionário
    @PostMapping("/atualizar-cadastro-geral")
    public String atualizarCadastroGeral(@RequestParam String registro, 
                                         @RequestParam String novoNome,
                                         @RequestParam String novaSenha,
                                         @RequestParam String novoTipo,
                                         @RequestParam String novaCarga) {
        Funcionario f = dataService.buscar(registro);
        if (f != null) {
            f.setNome(novoNome);
            f.setSenha(novaSenha);
            f.setTipoCarga(novoTipo);
            f.setCargaHorariaStr(novaCarga);
            dataService.salvar(f);
        }
        return "redirect:/editar-lancamento?registro=" + registro + "&sucesso_dados";
    }

    @PostMapping("/confirmar-edicao")
    public String confirmarEdicao(String registro, int indice, String valor, String tipo, String dataRef) {
        try {
            String[] partes = valor.split(":");
            int mins = (Integer.parseInt(partes[0].trim()) * 60) + Integer.parseInt(partes[1].trim());
            if ("debito".equals(tipo)) mins = -mins;
            dataService.substituirLancamento(registro, indice, dataRef, mins);
        } catch (Exception e) {}
        return "redirect:/editar-lancamento?registro=" + registro;
    }

    @GetMapping("/excluir-lancamento")
    public String excluirLanc(@RequestParam String registro, @RequestParam int idx) {
        dataService.excluirLancamento(registro, idx);
        return "redirect:/editar-lancamento?registro=" + registro;
    }

    // Mantendo para compatibilidade caso use a outra tela
    @GetMapping("/editar-carga")
    public String edCarga(Model model) {
        model.addAttribute("lista", dataService.listarTodos());
        return "ajuste_carga"; 
    }

    @PostMapping("/atualizar-so-carga")
    public String atualizarSoCarga(@RequestParam String registro, 
                                   @RequestParam String novoTipo, 
                                   @RequestParam String novaHora) {
        Funcionario f = dataService.buscar(registro);
        if (f != null) {
            f.setTipoCarga(novoTipo);
            f.setCargaHorariaStr(novaHora);
            dataService.salvar(f);
        }
        return "redirect:/editar-carga?sucesso";
    }

    @GetMapping("/excluir")
    public String excluir(Model model) {
        model.addAttribute("lista", dataService.listarTodos());
        return "excluir";
    }

    @PostMapping("/confirmar-exclusao")
    public String confirmarExclusao(@RequestParam String registro) {
        dataService.excluir(registro);
        return "redirect:/consultar";
    }

    @GetMapping("/senhas")
    public String senhas(Model model) {
        model.addAttribute("lista", dataService.listarTodos());
        return "senhas";
    }

    @PostMapping("/alterar-senha")
    public String alterarSenha(@RequestParam String registro, @RequestParam String novaSenha) {
        dataService.alterarSenha(registro, novaSenha);
        return "redirect:/dashboard";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        if(session != null) session.invalidate();
        return "redirect:/";
    }
}