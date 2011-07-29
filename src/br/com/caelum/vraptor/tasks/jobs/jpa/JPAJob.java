package br.com.caelum.vraptor.tasks.jobs.jpa;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public class JPAJob implements Job {

	private final TransactionalTask task;
	private final EntityManager manager;
 
	public JPAJob(TransactionalTask task, EntityManager manager) {
		this.task = task;
		this.manager = manager;
	}

	public void execute(JobExecutionContext context) throws JobExecutionException {

		EntityTransaction transaction = null;
		
		try {
			transaction = manager.getTransaction();
			transaction.begin();
			task.setup(manager);
			task.execute();
			transaction.commit();
		}
		finally {
			if (transaction != null && transaction.isActive()) {
				transaction.rollback();
			}
			manager.close();
		}
	}
}
