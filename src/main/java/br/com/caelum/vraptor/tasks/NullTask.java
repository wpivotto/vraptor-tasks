package br.com.caelum.vraptor.tasks;

import br.com.caelum.vraptor.ioc.ApplicationScoped;
import br.com.caelum.vraptor.ioc.Component;

@Component
@ApplicationScoped
public class NullTask implements Task {

	public void execute() {}

}
