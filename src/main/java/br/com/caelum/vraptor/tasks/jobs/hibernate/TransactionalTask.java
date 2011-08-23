package br.com.caelum.vraptor.tasks.jobs.hibernate;

import org.hibernate.classic.Session;

import br.com.caelum.vraptor.tasks.Task;
import br.com.caelum.vraptor.tasks.validator.Validator;


public interface TransactionalTask extends Task {

	void setup(Session session, Validator validator);

}
