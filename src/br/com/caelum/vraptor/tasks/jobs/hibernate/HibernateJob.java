package br.com.caelum.vraptor.tasks.jobs.hibernate;


import org.hibernate.Transaction;
import org.hibernate.classic.Session;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;


public class HibernateJob implements Job {

	private final TransactionalTask task;
	private final Session session;

	public HibernateJob(TransactionalTask task, Session session) {
		this.session = session;
		this.task = task;
	}

	public void execute(JobExecutionContext context) throws JobExecutionException {

		Transaction transaction = null;

		try {
			transaction = session.beginTransaction();
			task.setup(session);
			task.execute();
			transaction.commit();
		}
		finally {
			if (transaction != null && transaction.isActive()) {
				transaction.rollback();
			}
			session.close();
		}
	}
}
