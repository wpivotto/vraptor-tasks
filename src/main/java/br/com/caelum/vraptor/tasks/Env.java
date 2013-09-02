package br.com.caelum.vraptor.tasks;

import javax.servlet.ServletContext;

import br.com.caelum.vraptor.environment.Environment;
import br.com.caelum.vraptor.ioc.ApplicationScoped;
import br.com.caelum.vraptor.ioc.Component;

@Component
@ApplicationScoped
public class Env {
	
	private final Environment env;
	private final ServletContext context;

	public Env(Environment env, ServletContext context) {
		this.env = env;
		this.context = context;
	}

	public String getHost() {
		return env.get("host", "http://localhost");
	}

	public String getApplicationPath() {
		return getHost() + context.getContextPath();
	}

}
