package br.com.caelum.vraptor.tasks.jobs.simple;

import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.PersistJobDataAfterExecution;

import br.com.caelum.vraptor.tasks.Task;
import br.com.caelum.vraptor.tasks.TaskContext;
import br.com.caelum.vraptor.tasks.jobs.TaskExecutionException;

@DisallowConcurrentExecution
@PersistJobDataAfterExecution
public class StatefulJobWrapper implements Job {

	private final Task task;

	public StatefulJobWrapper(Task task) {
		this.task = task;
	}

	public void execute(JobExecutionContext context) throws JobExecutionException {
		try { 
			task.execute(new TaskContext(context));
		} catch (Exception e) {
			throw new TaskExecutionException(e);
		}
	}

}
