package br.com.caelum.vraptor.tasks.jobs.jpa;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public class ConcurrentJPAJob implements Job {

	private final TaskLogic logic;
 
	public ConcurrentJPAJob(TaskLogic logic) {
		this.logic = logic;
	}

	public void execute(JobExecutionContext context) throws JobExecutionException {
		logic.execute();
	}
}
