package br.com.caelum.vraptor.tasks.jobs.request;

import java.net.URL;

import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import br.com.caelum.vraptor.tasks.jobs.TaskExecutionException;

public class RequestScopedJob implements Job {

	private final RequestScopedTask task;
	
	public RequestScopedJob(RequestScopedTask task) {
		this.task = task;
	}

	public void execute(JobExecutionContext context) throws JobExecutionException {

		try {
			JobDataMap map = context.getMergedJobDataMap();
			URL url = new URL((String) map.get("task-uri"));
			task.setup(url);
			task.execute();
		} catch (Exception e) {
			throw new TaskExecutionException(e);
		}
		
	}
	
	

}
