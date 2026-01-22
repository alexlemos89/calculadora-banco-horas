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

    // MÉTODO AUXILIAR PARA VERIFICAR SE O USUÁRIO NÃO É ADMIN
    private boolean isNotAdmin(HttpSession session) {
        Object admin = session.getAttribute("admin");
        return admin == null || !(boolean) admin;
    }

    @GetMapping("/")
    public String inicial() { 
        return "login"; 
    }

    @PostMapping("/logar")
    public String logar(@RequestParam String usuario, @RequestParam String senha, HttpSession session, Model model) {
        // 1. LOGIN DO ADMINISTRADOR MESTRE (FIXO)
        if ("administrador".equals(usuario) && "imrealapa".equals(senha)) {
            session.setAttribute("admin", true);
            session.setAttribute("perfil", "ADMIN");
            session.setAttribute("nomeUsuario", "Administrador");
            return "redirect:/dashboard";
        }

        // 2. LOGIN DE FUNCIONÁRIO OU ADM CADASTRADO NO BANCO
        Funcionario f = dataService.buscar(usuario);
        if (f != null && f.getSenha().equals(senha)) {
            session.setAttribute("usuarioLogado", f.getRegistro());
            session.setAttribute("nomeUsuario", f.getNome()); 
            
            String perfil = (f.getPerfil() != null) ? f.getPerfil().toUpperCase() : "USER";
            session.setAttribute("perfil", perfil);
            session.setAttribute("admin", "ADMIN".equals(perfil));

            model.addAttribute("f", f);
            return "redirect:/dashboard"; 
        }
        return "redirect:/?erro";
    }

    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) { 
        model.addAttribute("nomeExibicao", session.getAttribute("nomeUsuario"));
        return "index"; 
    }

    @GetMapping("/consultar")
    public String consultar(HttpSession session, Model model) {
        try {
            String perfil = (String) session.getAttribute("perfil");
            String registro = (String) session.getAttribute("usuarioLogado");

            List<Funcionario> lista;
            if ("USER".equals(perfil)) {
                lista = new ArrayList<>();
                Funcionario eu = dataService.buscar(registro);
                if (eu != null) lista.add(eu);
            } else {
                lista = dataService.listarTodos();
            }
            
            if (lista == null) lista = new ArrayList<>();
            model.addAttribute("lista", lista);
            model.addAttribute("nomeExibicao", session.getAttribute("nomeUsuario"));
        } catch (Exception e) {
            model.addAttribute("lista", new ArrayList<Funcionario>());
            model.addAttribute("erro", "Erro ao acessar o banco de dados.");
        }
        return "consulta";
    }

    @PostMapping("/buscar-dados")
    public String buscarDados(@RequestParam("registro") String registro, Model model, HttpSession session) {
        // Se for funcionário, ele só pode buscar a si mesmo
        String perfil = (String) session.getAttribute("perfil");
        String usuarioLogado = (String) session.getAttribute("usuarioLogado");

        if ("USER".equals(perfil) && !registro.equals(usuarioLogado)) {
            return "redirect:/consultar?erro_acesso";
        }

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
    public String relatorioGeral(HttpSession session, Model model) {
        String perfil = (String) session.getAttribute("perfil");
        String registro = (String) session.getAttribute("usuarioLogado");

        if ("USER".equals(perfil)) {
            List<Funcionario> listaApenasEu = new ArrayList<>();
            Funcionario eu = dataService.buscar(registro);
            if (eu != null) listaApenasEu.add(eu);
            model.addAttribute("lista", listaApenasEu);
        } else {
            model.addAttribute("lista", dataService.listarTodos());
        }
        
        model.addAttribute("nomeExibicao", session.getAttribute("nomeUsuario"));
        return "relatorio";
    }

    // --- MÉTODOS PROTEGIDOS: SÓ ADMINISTRADOR ---

    @GetMapping("/registrar")
    public String registrar(HttpSession session, Model model) { 
        if (isNotAdmin(session)) return "redirect:/dashboard";
        model.addAttribute("nomeExibicao", session.getAttribute("nomeUsuario"));
        return "registro"; 
    }

    @PostMapping("/salvar-funcionario")
    public String salvar(Funcionario f, HttpSession session) {
        if (isNotAdmin(session)) return "redirect:/dashboard";
        
        // Garante que se o perfil não for enviado, salva como USER
        if (f.getPerfil() == null || f.getPerfil().isEmpty()) {
            f.setPerfil("USER");
        }
        
        dataService.salvar(f);
        return "redirect:/registrar?sucesso";
    }

    @GetMapping("/lancar")
    public String lancar(HttpSession session, Model model) {
        if (isNotAdmin(session)) return "redirect:/dashboard";
        model.addAttribute("lista", dataService.listarTodos());
        model.addAttribute("nomeExibicao", session.getAttribute("nomeUsuario"));
        return "lancamento";
    }

    @GetMapping("/editar-lancamento")
    public String edLanc(@RequestParam(required = false) String registro, 
                         @RequestParam(required = false) Integer idx, Model model, HttpSession session) {
        if (isNotAdmin(session)) return "redirect:/dashboard";
        
        model.addAttribute("lista", dataService.listarTodos());
        model.addAttribute("nomeExibicao", session.getAttribute("nomeUsuario"));
        
        if (registro != null) {
            Funcionario fSel = dataService.buscar(registro);
            model.addAttribute("fSel", fSel);
            
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

    @PostMapping("/confirmar-lancamento")
    public String confirmarLancamento(@RequestParam String registro, 
                                      @RequestParam String valor, 
                                      @RequestParam String tipo, 
                                      @RequestParam String dataRef,
                                      HttpSession session) {
        if (isNotAdmin(session)) return "redirect:/dashboard";
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

    @GetMapping("/editar-carga")
    public String edCarga(HttpSession session, Model model) {
        if (isNotAdmin(session)) return "redirect:/dashboard";
        model.addAttribute("lista", dataService.listarTodos());
        model.addAttribute("nomeExibicao", session.getAttribute("nomeUsuario"));
        return "ajuste_carga"; 
    }

    @GetMapping("/excluir")
    public String excluir(HttpSession session, Model model) {
        if (isNotAdmin(session)) return "redirect:/dashboard";
        model.addAttribute("lista", dataService.listarTodos());
        model.addAttribute("nomeExibicao", session.getAttribute("nomeUsuario"));
        return "excluir";
    }

    @GetMapping("/senhas")
    public String senhas(HttpSession session, Model model) {
        if (isNotAdmin(session)) return "redirect:/dashboard";
        model.addAttribute("lista", dataService.listarTodos());
        model.addAttribute("nomeExibicao", session.getAttribute("nomeUsuario"));
        return "senhas";
    }

    // --- MÉTODOS DE AÇÃO PROTEGIDOS ---

    @PostMapping("/confirmar-exclusao")
    public String confirmarExclusao(@RequestParam String registro, HttpSession session) {
        if (isNotAdmin(session)) return "redirect:/dashboard";
        dataService.excluir(registro);
        return "redirect:/consultar";
    }

    @PostMapping("/alterar-senha")
    public String alterarSenha(@RequestParam String registro, @RequestParam String novaSenha, HttpSession session) {
        if (isNotAdmin(session)) return "redirect:/dashboard";
        dataService.alterarSenha(registro, novaSenha);
        return "redirect:/dashboard";
    }

    @GetMapping("/excluir-lancamento")
    public String excluirLanc(@RequestParam String registro, @RequestParam int idx, HttpSession session) {
        if (isNotAdmin(session)) return "redirect:/dashboard";
        dataService.excluirLancamento(registro, idx);
        return "redirect:/editar-lancamento?registro=" + registro;
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        if(session != null) session.invalidate();
        return "redirect:/";
    }
}