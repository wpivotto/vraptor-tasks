package br.com.caelum.vraptor.tasks.jobs.simple;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import br.com.caelum.vraptor.tasks.Task;
import br.com.caelum.vraptor.tasks.jobs.TaskExecutionException;

public class ConcurrentJobWrapper implements Job {

	private final Task task;

	public ConcurrentJobWrapper(Task task) {
		this.task = task;
	}

	public void execute(JobExecutionContext context) throws JobExecutionException {
		try { 
			task.execute();
		} catch (Exception e) {
			throw new TaskExecutionException(e);
		}
	}

}
