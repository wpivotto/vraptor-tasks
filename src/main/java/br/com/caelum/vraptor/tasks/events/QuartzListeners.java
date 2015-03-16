package br.com.caelum.vraptor.tasks.events;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.quartz.JobListener;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class QuartzListeners {
	
	private @Inject Scheduler quartz;
	private @Inject @Any Instance<JobListener> jobListeners;
	private @Inject @Any Instance<SchedulerListener> schedulerListeners;
	
	private final Logger log = LoggerFactory.getLogger(getClass());

	@PostConstruct
	public void register() {
		try {
			for(JobListener listener : jobListeners) {
				quartz.getListenerManager().addJobListener(listener);
			}
			for(SchedulerListener listener : schedulerListeners) {
				quartz.getListenerManager().addSchedulerListener(listener);
			}
		} catch(SchedulerException e) {
			log.error("Unable to register listener", e);
		}
	}

}
