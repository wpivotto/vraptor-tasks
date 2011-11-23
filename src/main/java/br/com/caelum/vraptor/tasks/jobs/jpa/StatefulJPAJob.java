package br.com.caelum.vraptor.tasks.jobs.jpa;

import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.PersistJobDataAfterExecution;

@DisallowConcurrentExecution
@PersistJobDataAfterExecution
public class StatefulJPAJob implements Job {

	private final TaskLogic logic;
	 
	public StatefulJPAJob(TaskLogic logic) {
		this.logic = logic;
	}

	public void execute(JobExecutionContext context) throws JobExecutionException {
		logic.execute();
	}
}
