package br.com.caelum.vraptor.tasks.jobs.hibernate;

import org.hibernate.classic.Session;

import br.com.caelum.vraptor.tasks.Task;


public interface TransactionalTask extends Task {

	void setup(Session session);

}
