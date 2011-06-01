package br.com.caelum.vraptor.tasks;

import org.hibernate.classic.Session;

public interface TransactionalTask extends Task {

	void setup(Session session);

}
