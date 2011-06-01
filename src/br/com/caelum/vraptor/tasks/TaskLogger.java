package br.com.caelum.vraptor.tasks;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TaskLogger implements JobListener {

	private static Logger logger = LoggerFactory.getLogger(TaskLogger.class);

	@Override
	public String getName() {
		return getClass().getName();
	}

	@Override
	public void jobExecutionVetoed(JobExecutionContext context) {
		logger.debug("Task Execution was vetoed");
	}

	@Override
	public void jobToBeExecuted(JobExecutionContext context) {
		logger.debug("Executing Task " + context.getJobDetail().getKey().getName());
	}

	@Override
	public void jobWasExecuted(JobExecutionContext context, JobExecutionException exception) {
		logger.debug("Task " + context.getJobDetail().getKey().getName() + " was executed");
	}

}
