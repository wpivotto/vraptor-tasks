package br.com.caelum.vraptor.tasks.jobs.hibernate;


import org.hibernate.Transaction;
import org.hibernate.classic.Session;

import br.com.caelum.vraptor.tasks.validator.Validator;


public class TaskLogic {

	private final TransactionalTask task;
	private final Validator validator;
	private final Session session;

	public TaskLogic(TransactionalTask task, Validator validator, Session session) {
		this.session = session;
		this.validator = validator;
		this.task = task;
	}

	public void execute() {

		Transaction transaction = null;

		try {
			transaction = session.beginTransaction();
			task.setup(session, validator);
			task.execute();
			if (!validator.hasErrors()) {
				transaction.commit();
			}
		}
		finally {
			if (transaction != null && transaction.isActive()) {
				transaction.rollback();
			}
			session.close();
		}
	}
}
