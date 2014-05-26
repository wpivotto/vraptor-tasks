package br.com.caelum.vraptor.tasks.events;

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
@SuppressWarnings("unchecked")
public class QuartzListeners {
	
	private final Logger log = LoggerFactory.getLogger(getClass());
	
	@Deprecated // CDI eyes only
	public QuartzListeners() {}
	
	@Inject
	public QuartzListeners(Scheduler quartz, @Any Instance<JobListener> jobListeners, @Any Instance<SchedulerListener> schedListeners) {
		try {
			for(JobListener listener : jobListeners) {
				quartz.getListenerManager().addJobListener(listener);
			}
			for(SchedulerListener listener : schedListeners) {
				quartz.getListenerManager().addSchedulerListener(listener);
			}
		} catch(SchedulerException e) {
			log.error("Unable to register all listeners", e);
		}
	}

}
