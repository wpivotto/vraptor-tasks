package br.com.caelum.vraptor.tasks;

import java.lang.reflect.Method;

import br.com.caelum.vraptor.http.route.Router;
import br.com.caelum.vraptor.ioc.ApplicationScoped;
import br.com.caelum.vraptor.ioc.Component;

@Component
@ApplicationScoped
public class TaskLinker {

	private final Router router;
	private final Env env;
	
	public TaskLinker(Router router, Env env) {
		this.router = router;
		this.env = env;
	}

	public String linkTo(Class<?> controller, Method method) {
		String URI = router.urlFor(controller, method, new Object[method.getParameterTypes().length]);
		if (URI.startsWith("/")) {
			return env.getApplicationPath() + URI;
		}
		return URI;
	}
	
}
