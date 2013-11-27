package br.com.caelum.vraptor.tasks.callback;

import java.util.List;

import org.quartz.JobListener;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.com.caelum.vraptor.ioc.ApplicationScoped;
import br.com.caelum.vraptor.ioc.Component;

@Component
@ApplicationScoped
@SuppressWarnings("unchecked")
public class QuartzListeners {
	
	private final Logger log = LoggerFactory.getLogger(getClass());
	
	public QuartzListeners(Scheduler quartz, List<JobListener> jobListeners, List<SchedulerListener> schedListeners) {
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
