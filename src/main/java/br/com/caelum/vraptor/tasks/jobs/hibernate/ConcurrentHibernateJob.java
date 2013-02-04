package br.com.caelum.vraptor.tasks.jobs.hibernate;


import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public class ConcurrentHibernateJob implements Job {

	private final TaskLogic logic;

	public ConcurrentHibernateJob(TaskLogic logic) {
		this.logic = logic;
	}

	public void execute(JobExecutionContext context) throws JobExecutionException {
		logic.execute();
	}
}
