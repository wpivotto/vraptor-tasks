package br.com.caelum.vraptor.tasks;

import java.lang.reflect.Method;

import br.com.caelum.vraptor.config.Configuration;
import br.com.caelum.vraptor.http.route.Router;
import br.com.caelum.vraptor.ioc.Component;
import br.com.caelum.vraptor.ioc.RequestScoped;

@Component
@RequestScoped
public class TaskLinker {

	private final Router router;
	private final Configuration cfg;
	
	public TaskLinker(Router router, Configuration cfg) {
		this.router = router;
		this.cfg = cfg;
	}

	public String linkTo(Class<?> controller, Method method) {
		String URI = router.urlFor(controller, method, new Object[method.getParameterTypes().length]);
		if (URI.startsWith("/")) {
			return cfg.getApplicationPath() + URI;
		}
		return URI;
	}
	
}
