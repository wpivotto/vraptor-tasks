package br.com.caelum.vraptor.tasks.scheduler;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.spi.JobFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.com.caelum.vraptor.tasks.TasksMonitor;

@ApplicationScoped
public class SchedulerCreator {

	private static Logger logger = LoggerFactory.getLogger(SchedulerCreator.class);
	private final Scheduler scheduler;

	@Inject
	public SchedulerCreator(JobFactory factory, TasksMonitor monitor) {

		try {
			this.scheduler = new StdSchedulerFactory().getScheduler();
			this.scheduler.setJobFactory(factory);
			this.scheduler.getListenerManager().addJobListener(monitor);
			this.scheduler.getListenerManager().addSchedulerListener(monitor);
			monitor.setScheduler(this.scheduler);
		} catch (SchedulerException e) {
			throw new RuntimeException(e);
		}

	}

	@Produces
	public Scheduler getInstance() {
		return scheduler;
	}

	/**
	 * Calls {#start()} after the indicated number of seconds. (This call does not block). 
	 * This can be useful within applications that have initializers that create the scheduler immediately, 
	 * before the resources needed by the executing jobs have been fully initialized.
	 */
	@PostConstruct
	public void start() {
		try {
			this.scheduler.startDelayed(5);
		} catch (SchedulerException e) {
			logger.error("ERROR", e);
		}
	}

	@PreDestroy
	public void stop() {
		try {
			this.scheduler.shutdown(true);
		} catch (SchedulerException e) {
			logger.error("ERROR", e);
		}

	}

}
