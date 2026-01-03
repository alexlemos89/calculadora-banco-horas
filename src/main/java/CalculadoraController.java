package com.mycompany.calculadorabancodehoras;

import com.mycompany.calculadorabancodehoras.service.DataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpSession; // Corrigido para a sua versão do Java
import java.util.List;

@Controller
public class CalculadoraController {

    @Autowired
    private DataService dataService;

    @GetMapping("/")
    public String inicial() { return "login"; }

    @PostMapping("/logar")
    public String logar(@RequestParam String usuario, @RequestParam String senha, Model model) {
        if ("administrador".equals(usuario) && "imrealapa".equals(senha)) {
            return "redirect:/dashboard";
        }
        Funcionario f = dataService.buscar(usuario);
        if (f != null && f.getSenha().equals(senha)) {
            model.addAttribute("funcionario", f);
            return "view_funcionario"; 
        }
        return "redirect:/?erro";
    }

    @GetMapping("/dashboard")
    public String dashboard() { return "index"; }

    @GetMapping("/registrar")
    public String registrar() { return "registro"; }

    @PostMapping("/salvar-funcionario")
    public String salvarFuncionario(@RequestParam String nome, @RequestParam String registro,
                                   @RequestParam String tipoCarga, @RequestParam String horasCargaStr,
                                   @RequestParam String senha) {
        Funcionario f = new Funcionario();
        f.setNome(nome);
        f.setRegistro(registro);
        f.setTipoCarga(tipoCarga);
        f.setCargaHorariaStr(horasCargaStr);
        f.setSenha(senha);
        dataService.salvar(f);
        return "redirect:/consultar";
    }

    @GetMapping("/consultar")
    public String consultar(Model model) {
        model.addAttribute("lista", dataService.listarTodos());
        return "consulta";
    }

    @GetMapping("/lancar")
    public String lancar(Model model) {
        model.addAttribute("lista", dataService.listarTodos());
        return "lancamento";
    }

    @PostMapping("/confirmar-lancamento")
    public String confirmarLancamento(@RequestParam String registro, @RequestParam String valor, 
                                     @RequestParam String tipo, @RequestParam String dataRef) {
        try {
            String[] partes = valor.split(":");
            int mins = (Integer.parseInt(partes[0]) * 60) + Integer.parseInt(partes[1]);
            if ("debito".equals(tipo)) mins = -mins;
            dataService.adicionarHoras(registro, mins, dataRef);
        } catch (Exception e) { return "redirect:/dashboard?erro"; }
        return "redirect:/relatorio";
    }

    @GetMapping("/relatorio")
    public String relatorio(Model model) {
        model.addAttribute("lista", dataService.listarTodos());
        return "relatorio";
    }

    @GetMapping("/editar-lancamento")
    public String edLanc(@RequestParam(required = false) String registro, 
                        @RequestParam(required = false) Integer idx, Model model) {
        List<Funcionario> lista = dataService.listarTodos();
        model.addAttribute("lista", lista);
        if (registro != null) {
            Funcionario fSel = dataService.buscar(registro);
            model.addAttribute("fSel", fSel);
            if (idx != null && fSel != null && idx < fSel.getHistorico().size()) {
                String linha = fSel.getHistorico().get(idx);
                model.addAttribute("dataEd", linha.split(" - ")[0]);
                model.addAttribute("horaEd", linha.split(" - ")[1].substring(0, 5));
                model.addAttribute("tipoEd", linha.contains("CRÉDITO") ? "credito" : "debito");
                model.addAttribute("idxEd", idx);
            }
        }
        return "ajustar";
    }

    @PostMapping("/confirmar-edicao")
    public String confirmarEdicao(@RequestParam String registro, @RequestParam int indice,
                                 @RequestParam String valor, @RequestParam String tipo, 
                                 @RequestParam String dataRef) {
        try {
            String[] partes = valor.split(":");
            int mins = (Integer.parseInt(partes[0]) * 60) + Integer.parseInt(partes[1]);
            if ("debito".equals(tipo)) mins = -mins;
            dataService.substituirLancamento(registro, indice, dataRef, mins);
        } catch (Exception e) { return "redirect:/editar-lancamento?erro"; }
        return "redirect:/relatorio";
    }

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
        return "redirect:/consultar";
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

    // NOVA ROTA DE LOGOUT
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        if (session != null) {
            session.invalidate();
        }
        return "redirect:/";
    }
}