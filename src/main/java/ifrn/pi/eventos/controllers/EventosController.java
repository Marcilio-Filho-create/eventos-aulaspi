package ifrn.pi.eventos.controllers;

import java.util.List;
import java.util.Optional;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import ifrn.pi.eventos.models.Convidado;
import ifrn.pi.eventos.models.Evento;
import ifrn.pi.eventos.repositories.ConvidadoRepository;
import ifrn.pi.eventos.repositories.EventoRepository;

@Controller
@RequestMapping("/eventos")
public class EventosController {
	
	@Autowired
	private EventoRepository er;
	@Autowired
	private ConvidadoRepository cr;
	
	@GetMapping("/form")
	public String form(Evento evento) {
		return "eventos/formEvento";
	}
	
	@PostMapping
	public String salvar(@Valid Evento evento, BindingResult result, RedirectAttributes attributes) {
		
		if(result.hasErrors()) {
			return form(evento);
		}
		
		er.save(evento);
		attributes.addFlashAttribute("mensagem", "Evento adicionado com sucesso!");
		
		return "redirect:/eventos";
	}
	
	@GetMapping
	public ModelAndView listar() {
		List<Evento> eventos = er.findAll();
		ModelAndView mv = new ModelAndView("eventos/lista");
		mv.addObject("eventos", eventos);
		return mv;
	}
	
	@GetMapping("/{idEvento}")
	public ModelAndView detalhar(@PathVariable Long idEvento, Convidado convidado) {
		ModelAndView md = new ModelAndView();
		Optional<Evento> opt = er.findById(idEvento);
		
		if(opt.isEmpty()) {
			md.setViewName("redirect:/eventos");
			return md;
		}
		
		md.setViewName("eventos/detalhes");
		Evento evento = opt.get();
		md.addObject("ev", evento);
		
		List<Convidado> convidados = cr.findByEvento(evento);
		md.addObject("convidados", convidados);
		
		return md;
	}
	
	@PostMapping("/{idEvento}")
	public ModelAndView salvarConvidado(@PathVariable Long idEvento, @Valid Convidado convidado, BindingResult result, RedirectAttributes attributes) {
		ModelAndView md = new ModelAndView();
		
		Optional<Evento> opt = er.findById(idEvento);
		if(opt.isEmpty()) {
			md.setViewName("redirect:/eventos");
			return md;
		}
		
		Evento evento = opt.get();
		convidado.setEvento(evento);
		if(result.hasErrors()) {
			return detalhar(evento.getId(), convidado);
		}
		
		cr.save(convidado);
		md.setViewName("redirect:/eventos/{idEvento}");
		md.addObject("ev", evento);
		attributes.addFlashAttribute("mensagem", "Convidado adicionado com sucesso!");
		
		return md;
	}
	
	@GetMapping("/{id}/selecionar")
	public ModelAndView selecionarEvento(@PathVariable Long id) {
		ModelAndView md = new ModelAndView();
		Optional<Evento> opt = er.findById(id);
		
		if(opt.isEmpty()) {
			md.setViewName("redirect:/eventos");
			return md;
		}
		
		Evento evento = opt.get();
		md.setViewName("eventos/formEvento");
		md.addObject("evento", evento);
		
		return md;
	}
	
	@GetMapping("/{idEvento}/convidados/{idConvidado}/selecionar")
	public ModelAndView selecionarConvidado(@PathVariable Long idEvento, @PathVariable Long idConvidado) {
		ModelAndView md = new ModelAndView();
		Optional<Evento> optEvento = er.findById(idEvento);
		Optional<Convidado> optConvidado = cr.findById(idConvidado);
		
		if(optEvento.isEmpty() || optConvidado.isEmpty()) {
			md.setViewName("redirect:/eventos");
			return md;
		}
		
		Evento evento = optEvento.get();
		Convidado convidado = optConvidado.get();
		
		if(evento.getId() != convidado.getEvento().getId()) {
			md.setViewName("redirect:/eventos");
			return md;
		}
		
		md.setViewName("eventos/detalhes");
		md.addObject("convidado", convidado);
		md.addObject("ev", evento);
		md.addObject("convidados", cr.findByEvento(evento));
		
		return md;
	}
	
	@GetMapping("/{id}/deletar")
	public String apagarEvento(@PathVariable Long id, RedirectAttributes attributes) {
		Optional<Evento> opt = er.findById(id);
		
		if(!opt.isEmpty()) {
			Evento evento = opt.get();
			List<Convidado> conv = cr.findByEvento(evento);
			
			cr.deleteAll(conv);
			er.delete(evento);
			attributes.addFlashAttribute("mensagem", "Evento removido com sucesso!");
		}
		
		return "redirect:/eventos";
	}
	
	@GetMapping("/{idE}/deletar/{idC}")
	public String apagarConvidado(@PathVariable Long idE, @PathVariable Long idC, RedirectAttributes attributes) {
		Optional<Convidado> optC = cr.findById(idC);
		Optional<Evento> optE = er.findById(idE);
		
		if(!optC.isEmpty() && !optE.isEmpty()) {
			Convidado con = optC.get();
			Evento eve = optE.get();
			
			if(con.getEvento().getId() == eve.getId()) {
				cr.delete(con);
				attributes.addFlashAttribute("mensagem", "Convidado removido com sucesso!");
			}
			else {
				return "redirect:/eventos";
			}
		}
		
		return "redirect:/eventos/{idE}";
	}
}