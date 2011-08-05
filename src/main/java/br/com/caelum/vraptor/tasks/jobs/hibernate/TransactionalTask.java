package br.com.caelum.vraptor.tasks.jobs.hibernate;

import org.hibernate.classic.Session;

import br.com.caelum.vraptor.tasks.Task;
import br.com.caelum.vraptor.tasks.validator.TaskValidator;


public interface TransactionalTask extends Task {

	void setup(Session session, TaskValidator validator);

}
