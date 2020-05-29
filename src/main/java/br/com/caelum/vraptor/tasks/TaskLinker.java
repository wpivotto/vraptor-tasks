package br.com.caelum.vraptor.tasks;

import java.lang.reflect.Method;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import br.com.caelum.vraptor.config.Configuration;
import br.com.caelum.vraptor.http.route.Router;

@Dependent
public class TaskLinker {

	@Inject private Router router;
	@Inject private Configuration cfg;  
	
	public String linkTo(Class<?> controller, Method method) {
		return linkTo(controller, method, new Object[method.getParameterTypes().length]);
	}
	
	public String linkTo(Class<?> controller, Method method, Object[] args) {
		String URI = router.urlFor(controller, method, args);
		if (URI.startsWith("/")) {
			return cfg.getApplicationPath() + URI;
		}
		return URI;
	}
	
}
