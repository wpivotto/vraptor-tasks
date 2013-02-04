package br.com.caelum.vraptor.tasks.jobs.request;

import java.net.URL;

import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.PersistJobDataAfterExecution;

import br.com.caelum.vraptor.tasks.jobs.TaskExecutionException;

@DisallowConcurrentExecution
@PersistJobDataAfterExecution
public class RequestScopedJob implements Job {

	private final RequestScopedTask task;
	
	public RequestScopedJob(RequestScopedTask task) {
		this.task = task;
	}

	public void execute(JobExecutionContext context) throws JobExecutionException {
		try {
			URL url = new URL(urlString(context));
			task.setup(url, context.getMergedJobDataMap());
			task.execute();
		} catch (Exception e) {
			throw new TaskExecutionException(e);
		}
	}
	
	private String urlString(JobExecutionContext context) {
		JobDataMap map = context.getMergedJobDataMap();
		String path = map.getString("task-uri");
		return path;
	}

}
