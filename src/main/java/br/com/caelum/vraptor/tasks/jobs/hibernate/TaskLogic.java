package br.com.caelum.vraptor.tasks.jobs.hibernate;


import org.hibernate.Transaction;
import org.hibernate.classic.Session;

import br.com.caelum.vraptor.tasks.jobs.TaskExecutionException;
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

	public void execute() throws TaskExecutionException {

		Transaction transaction = null;

		try {
			transaction = session.beginTransaction();
			task.setup(session, validator);
			task.execute();
			if (!validator.hasErrors()) {
				transaction.commit();
			}
		} catch (Exception e) {
			throw new TaskExecutionException(e);
		}
		finally {
			if (transaction != null && transaction.isActive()) {
				transaction.rollback();
			}
			session.close();
		}
	}
}
