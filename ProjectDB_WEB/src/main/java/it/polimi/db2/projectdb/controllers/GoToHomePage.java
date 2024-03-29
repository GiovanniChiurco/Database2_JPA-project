package it.polimi.db2.projectdb.controllers;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;

import javax.ejb.EJB;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;

import it.polimi.db2.projectdb.entities.*;
import it.polimi.db2.projectdb.services.OrderServices;
import it.polimi.db2.projectdb.services.ServicePakcageServices;

@WebServlet("/Home")
public class GoToHomePage extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private TemplateEngine templateEngine;
	@EJB(name = "it.polimi.db2.projectdb.services/ServicePackageServices")
	private ServicePakcageServices sPService;
	@EJB(name = "it.polimi.db2.projectdb.services/OrderServices")
	private OrderServices oService;

	public GoToHomePage() {
		super();
	}

	public void init() throws ServletException {
		ServletContext servletContext = getServletContext();
		ServletContextTemplateResolver templateResolver = new ServletContextTemplateResolver(servletContext);
		templateResolver.setTemplateMode(TemplateMode.HTML);
		this.templateEngine = new TemplateEngine();
		this.templateEngine.setTemplateResolver(templateResolver);
		templateResolver.setSuffix(".html");
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		User user;
		Boolean existUser = false;
		try {
			user = (User) request.getSession().getAttribute("user");
			if(user != null)
				existUser = true;
		}
		catch(Exception e) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Where is my gold?");
			return;
		}
		
		List<ServicePackage> servPackList = null;
		Boolean listEmpty = false;
		try {
			servPackList = sPService.findAllServPackages();
			if (servPackList == null) {
				listEmpty = true;
			}
		} catch (Exception e) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Not possible to get data");
			return;
		}
		
		List<Order> rejectOrdList = null;
		if(existUser == true) {
			try {
				rejectOrdList = oService.rejectedOrdersFromUserID(user.getUsername());
			}
			catch(Exception e) {
				response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Not possible to get order data");
				return;
			}
		}
		String path = "/WEB-INF/Home.html";
		ServletContext servletContext = getServletContext();
		final WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());
		
		ctx.setVariable("servPackList", servPackList);
		ctx.setVariable("listEmpty", listEmpty);
		
		ctx.setVariable("user", user);
		ctx.setVariable("existUser", existUser);
		
		ctx.setVariable("rejectOrdList", rejectOrdList);
		
		templateEngine.process(path, ctx, response.getWriter());
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		doGet(request, response);
	}

	public void destroy() {}

}
