package br.com.caelum.vraptor.tasks;

import java.lang.reflect.Method;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import br.com.caelum.vraptor.config.Configuration;
import br.com.caelum.vraptor.http.route.Router;

@Dependent
public class TaskLinker {

	private final Router router;
	private final Configuration cfg;
	
	@Inject
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
