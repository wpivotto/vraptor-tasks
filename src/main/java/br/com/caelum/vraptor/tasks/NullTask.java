package br.com.caelum.vraptor.tasks;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class NullTask implements Task {

	public void execute() {}

}
