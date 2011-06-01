package br.com.caelum.vraptor.tasks.scheduler;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.com.caelum.vraptor.ioc.ApplicationScoped;
import br.com.caelum.vraptor.ioc.Component;
import br.com.caelum.vraptor.ioc.ComponentFactory;
import br.com.caelum.vraptor.tasks.TaskLogger;
import br.com.caelum.vraptor.tasks.jobs.DefaultJobFactory;

@Component
@ApplicationScoped
public class SchedulerCreator implements ComponentFactory<Scheduler> {

	private static Logger logger = LoggerFactory.getLogger(SchedulerCreator.class);
	private final Scheduler scheduler;

	public SchedulerCreator(DefaultJobFactory factory) {

		try {
			this.scheduler = new StdSchedulerFactory().getScheduler();
			this.scheduler.setJobFactory(factory);
			this.scheduler.getListenerManager().addJobListener(new TaskLogger());
		} catch (SchedulerException e) {
			throw new RuntimeException(e);
		}

	}

	@Override
	public Scheduler getInstance() {
		return scheduler;
	}

	@PostConstruct   
	public void start() {
		try {
			this.scheduler.start();
		} catch (SchedulerException e) {
			logger.debug("ERROR", e);
		}
	}

	@PreDestroy
	public void stop() {
		try {
			this.scheduler.shutdown(true);
		} catch (SchedulerException e) {
			logger.debug("ERROR", e);
		}
		
	}

}
