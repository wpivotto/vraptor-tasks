package br.com.caelum.vraptor.tasks.scheduler;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Disposes;
import javax.enterprise.inject.Produces;

import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.spi.JobFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.com.caelum.vraptor.tasks.TasksMonitor;

public class SchedulerCreator {

	private static Logger logger = LoggerFactory.getLogger(SchedulerCreator.class);
	private Scheduler scheduler;

	@Produces @ApplicationScoped
	public Scheduler create(JobFactory factory, TasksMonitor monitor) {
		if (scheduler == null) {
			try {
				scheduler = new StdSchedulerFactory().getScheduler();
				scheduler.setJobFactory(factory);
				scheduler.getListenerManager().addJobListener(monitor);
				scheduler.getListenerManager().addSchedulerListener(monitor);
				monitor.setScheduler(scheduler);
				this.scheduler.startDelayed(5);
			} catch (SchedulerException e) {
				throw new RuntimeException(e);
			}
		}
		return scheduler;
	}

	public void shutdownScheduler(@Disposes Scheduler scheduler) {
		try {
			scheduler.shutdown(true);
		} catch (SchedulerException e) {
			logger.error("ERROR", e);
		}
	}

}
