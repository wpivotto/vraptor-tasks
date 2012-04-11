package br.com.caelum.vraptor.tasks.jobs.jpa;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import br.com.caelum.vraptor.tasks.jobs.TaskExecutionException;
import br.com.caelum.vraptor.tasks.validator.Validator;

public class TaskLogic {
	
	private final TransactionalTask task;
	private final Validator validator;
	private final EntityManager manager;
 
	public TaskLogic(TransactionalTask task, Validator validator, EntityManager manager) {
		this.task = task;
		this.validator = validator;
		this.manager = manager;
	}

	public void execute() throws TaskExecutionException {

		EntityTransaction transaction = null;
		
		try {
			transaction = manager.getTransaction();
			transaction.begin();
			task.setup(manager, validator);
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
			manager.close();
		}
	}

}
