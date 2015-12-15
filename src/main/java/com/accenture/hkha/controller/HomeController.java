package com.accenture.hkha.controller;


import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.accenture.hkha.constants.UserRoles;
import com.accenture.hkha.model.Assessment2;
import com.accenture.hkha.service.AssessmentService;

@Controller
public class HomeController {

	private final Logger logger = LoggerFactory.getLogger(HomeController.class);

	private AssessmentService assessmentService;
	private String username;
	private Integer assessmentId;


	@Autowired
	public void setWorkService(AssessmentService assessmentService) {
		this.assessmentService = assessmentService;
	}

	@RequestMapping(value="/", method = RequestMethod.GET)
	public String index(Model model){
		logger.info("index()");

		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		this.username = auth.getName();
		logger.info("Welcome " + this.username + "!");

		Collection<SimpleGrantedAuthority> authorities = (Collection<SimpleGrantedAuthority>) SecurityContextHolder.getContext().getAuthentication().getAuthorities();
		for(SimpleGrantedAuthority sga: authorities){
			logger.info("Roles: " + sga.getAuthority());
			if(sga.getAuthority().equals(UserRoles.ROLE_ASSESSOR.toString())){
				return "redirect:/worklist";
			}else if(sga.getAuthority().equals(UserRoles.ROLE_PROF.toString())){
				return "redirect:/prof/worklist";
			}else if(sga.getAuthority().equals(UserRoles.ROLE_CHIEF.toString())){
				return "redirect:/chief/worklist";
			}else if(sga.getAuthority().equals(UserRoles.ROLE_ADMIN.toString())){
				return "redirect:/admin/worklist";
			}
		}

		return "noaccess";
	}

	@RequestMapping(value="/reports", method = RequestMethod.GET)
	public String showReports(Model model){

		logger.info("showReports()");


		return "reports";
	}

	@RequestMapping(value="/worklist", method = RequestMethod.GET)
	public String showWorkList(Model model){

		logger.info("showWorkList()");

		List<Assessment2> list = new ArrayList<Assessment2>();
		list.addAll(assessmentService.findByStatus("FOR ASSESSMENT"));
		list.addAll(assessmentService.findByStatus("RETURNED"));
		list.addAll(assessmentService.findByStatus("SAVE DRAFT"));
		list.addAll(assessmentService.findByStatus("SAVED"));

		model.addAttribute("worklist", list);

		return "worklist";
	}

	@RequestMapping(value="/prof/worklist", method = RequestMethod.GET)
	public String showProfWorklist(Model model){
		logger.info("showProfWorkList()");

		model.addAttribute("reviewList", assessmentService.findByStatusAndAssignment("FOR REVIEW",this.username));

		return "worklist_prof";
	}

	@RequestMapping(value="/prof/worklist/{id}/details", method = RequestMethod.GET)
	public String showReviewDetails(@PathVariable("id") int id, Model model){
		logger.info("showReviewDetails()");
		this.assessmentId = id;
		model.addAttribute("assessmentForm", assessmentService.findById(id));
		model.addAttribute("mode", "PROF_MODE");

		return "assessmentSummaryProf";
	}

	@RequestMapping(value="/approve/prof/{id}", method = RequestMethod.GET)
	public String approveProf(@PathVariable("id") int id, Model model){
		logger.info("approveProf()");

		Assessment2 assessment = assessmentService.findById(id);
		assessment.setApprovedByProf("Y");
		assessment.setStatus("FOR APPROVAL");
		assessment.setAssignedTo("CHIEF");

		assessmentService.saveOrUpdate(assessment);
		logger.info("PROF Approval - Complete!");

		return "redirect:/prof/worklist";
	}

	@RequestMapping(value="/return/prof/{id}", method = RequestMethod.GET)
	public String returnAssessment(@PathVariable("id") int id, Model model){
		logger.info("returnAssessment()");

		Assessment2 assessment = assessmentService.findById(id);
		assessment.setApprovedByProf("N");
		assessment.setStatus("RETURNED");
		assessment.setAssignedTo("ASSESSOR");

		assessmentService.saveOrUpdate(assessment);
		logger.info("Assessment Returned!");

		return "redirect:/prof/worklist";
	}


	@RequestMapping(value="/chief/worklist", method = RequestMethod.GET)
	public String showChiefWorklist(Model model){
		logger.info("showChiefWorklist()");

		model.addAttribute("approvalList", assessmentService.findByStatusAndAssignment("FOR APPROVAL", this.username));

		return "worklist_chief";
	}

	@RequestMapping(value="/admin/worklist", method = RequestMethod.GET)
	public String showAdminWorklist(Model model){
		logger.info("showAdminWorklist()");

		model.addAttribute("adminList", assessmentService.findAllAssessment());

		return "worklist_admin";
	}
	@RequestMapping(value="/admin/worklist/{id}/details", method = RequestMethod.GET)
	public String showAssessmentDetails(@PathVariable("id") int id, Model model){
		logger.info("showAssessmentDetails()");
		this.assessmentId = id;
		model.addAttribute("assessmentForm", assessmentService.findById(id));
		model.addAttribute("mode", "ADMIN_MODE");

		return "assessmentSummaryAdmin";
	}

	@RequestMapping(value="/chief/worklist/{id}/details", method = RequestMethod.GET)
	public String showApprovalDetails(@PathVariable("id") int id, Model model){
		logger.info("showApprovalDetails()");
		this.assessmentId = id;
		model.addAttribute("assessmentForm", assessmentService.findById(id));
		model.addAttribute("mode", "CHIEF_MODE");

		return "assessmentSummaryChief";
	}

	@RequestMapping(value="/submitChief", method = RequestMethod.GET)
	public String submitChiefSummary(Model model){

		Assessment2 assessment = assessmentService.findById(this.assessmentId);
		assessment.setStatus("COMPLETED");
		assessment.setAssignedTo("chief");

		assessmentService.saveOrUpdate(assessment);
		logger.info("submit successful!");

		return "redirect:/chief/worklist";
	}

	@RequestMapping(value="/approve/chief/{id}", method = RequestMethod.GET)
	public String approveChief(@PathVariable("id") int id, Model model){

		logger.info("approveChief()");

		Assessment2 assessment = assessmentService.findById(id);
		assessment.setApprovedByChief("Y");
		assessment.setStatus("APPROVED");
		assessment.setAssignedTo("ASSESSOR");

		assessmentService.saveOrUpdate(assessment);
		logger.info("CHIEF Approval - Complete!");

		return "redirect:/chief/worklist";

	}

	@RequestMapping(value="/reject/chief/{id}", method = RequestMethod.GET)
	public String rejectAssessment(@PathVariable("id") int id, Model model){

		logger.info("rejectAssessment()");

		Assessment2 assessment = assessmentService.findById(id);
		assessment.setApprovedByChief("N");
		assessment.setStatus("REJECTED");
		assessment.setAssignedTo("ASSESSOR");

		assessmentService.saveOrUpdate(assessment);
		logger.info("Assessment Rejected!");

		return "redirect:/chief/worklist";

	}

	@RequestMapping(value="/logout", method = RequestMethod.GET)
	public String logout(HttpServletRequest request, HttpServletResponse response){
		logger.info("logout()");
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
	    if (auth != null){
	        new SecurityContextLogoutHandler().logout(request, response, auth);
	    }
	    return "redirect:/login?logout";
	}

	@RequestMapping(value = "/login", method = RequestMethod.GET)
	public ModelAndView login(@RequestParam(value = "error", required = false) String error,
		@RequestParam(value = "logout", required = false) String logout) {

	  ModelAndView model = new ModelAndView();
	  if (error != null) {
		model.addObject("error", "Invalid username and password!");
	  }

	  if (logout != null) {
		model.addObject("msg", "You've been logged out successfully.");
	  }
	  model.setViewName("loginpage");

	  return model;

	}
	//for 403 access denied page
		@RequestMapping(value = "/noaccess", method = RequestMethod.GET)
		public ModelAndView accesssDenied() {

		  ModelAndView model = new ModelAndView();

		  //check if user is login
		  Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		  if (!(auth instanceof AnonymousAuthenticationToken)) {
			String userDetail = (String) auth.getPrincipal();
			model.addObject("username", userDetail);
		  }

		  model.setViewName("noaccess");
		  return model;

		}




	@RequestMapping(value="/worklist/{id}/form", method = RequestMethod.GET)
	public String showWorkDetails(@PathVariable("id") int id, Model model){
		logger.info("showWorkDetails() id: {}", id);

		Assessment2 assessment = assessmentService.findById(id);
		model.addAttribute("assessmentForm", assessment);

		logger.info(assessment.toString());


		return "assessmentForm";
	}

	@RequestMapping(value="/worklist/{id}/form/confirm", method = RequestMethod.POST)
	public String submitAssessment(@ModelAttribute("assessmentForm") Assessment2 assessment, Model model, @PathVariable int id){

		logger.info("submitAssessment()");
		logger.info("id: " + id);
		this.assessmentId = id;

		model.addAttribute("assessmentForm", assessment);

		logger.info(assessment.toString());

		return "assessmentSummary";
	}

	@RequestMapping(value="/worklist/{id}/form/save", method = RequestMethod.POST)
	public String saveAssessment(@ModelAttribute("assessmentForm") Assessment2 assessment, Model model, @PathVariable int id){

		logger.info("saveAssessment()");
		logger.info("id: " + id);
		this.assessmentId = id;

		assessment.setStatus("SAVE DRAFT");

		assessmentService.saveOrUpdate(assessment);
		model.addAttribute("assessmentForm", assessment);

		logger.info(assessment.toString());

		return "redirect:/worklist/{id}/form";
	}

	@RequestMapping(value="worklist/{id}/form/summary", method = RequestMethod.GET)
	public String showSummary(@ModelAttribute("assessmentForm") Assessment2 assessment, Model model){

		//Assessment2 assessment = assessmentService.findById(this.assessmentId);
		/*model.addAttribute("assessment", assessment);
		model.addAttribute("mode", "ASSESS_MODE");

		assessment.setStatus("FOR APPROVAL");
		assessment.setAssignedTo("prof");

		assessmentService.saveOrUpdate(assessment);*/
		model.addAttribute("assessmentForm", assessment);

		return "assessmentSummary";
	}

	@RequestMapping(value="/worklist/{id}/form/submit", method = RequestMethod.POST)
	public String submitSummary(@ModelAttribute("assessmentForm") Assessment2 assessment, Model model){

		Assessment2 assessmentOrig = assessmentService.findById(this.assessmentId);
		assessment.setStatus("FOR REVIEW");
		assessment.setAssignedTo("prof");
		assessment.setContract(assessmentOrig.getContract());
		assessment.setAssessmentDate(assessmentOrig.getAssessmentDate());
		assessment.setCreatedBy(assessmentOrig.getCreatedBy());
		assessment.setCreatedDate(assessmentOrig.getCreatedDate());

		assessmentService.saveOrUpdate(assessment);
		logger.info("submit successful!");

		model.addAttribute("assessmentForm", assessment);
		return "redirect:/worklist";//"assessmentSummaryAck";//
	}

	@RequestMapping(value="/submitProf", method = RequestMethod.GET)
	public String submitProfSummary(Model model){

		Assessment2 assessment = assessmentService.findById(this.assessmentId);
		assessment.setStatus("FOR APPROVAL");
		assessment.setAssignedTo("chief");

		assessmentService.saveOrUpdate(assessment);
		logger.info("submit successful!");

		return "redirect:/prof/worklist";
	}


	public Integer getAssessmentId() {
		return assessmentId;
	}

	public void setAssessmentId(Integer assessmentId) {
		this.assessmentId = assessmentId;
	}


}
