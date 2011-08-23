package br.com.caelum.vraptor.tasks.jobs.jpa;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import br.com.caelum.vraptor.tasks.validator.Validator;

public class JPAJob implements Job {

	private final TransactionalTask task;
	private final Validator validator;
	private final EntityManager manager;
 
	public JPAJob(TransactionalTask task, Validator validator, EntityManager manager) {
		this.task = task;
		this.validator = validator;
		this.manager = manager;
	}

	public void execute(JobExecutionContext context) throws JobExecutionException {

		EntityTransaction transaction = null;
		
		try {
			transaction = manager.getTransaction();
			transaction.begin();
			task.setup(manager, validator);
			task.execute();
			if (!validator.hasErrors()) {
				transaction.commit();
			}
		}
		finally {
			if (transaction != null && transaction.isActive()) {
				transaction.rollback();
			}
			manager.close();
		}
	}
}
