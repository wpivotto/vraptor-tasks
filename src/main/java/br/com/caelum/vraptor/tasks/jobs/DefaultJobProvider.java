package br.com.caelum.vraptor.tasks.jobs;

import org.quartz.Job;

import br.com.caelum.vraptor.ioc.ApplicationScoped;
import br.com.caelum.vraptor.ioc.Component;
import br.com.caelum.vraptor.tasks.Task;

@Component
@ApplicationScoped
public class DefaultJobProvider implements JobProvider {

	public Job newJob(Task task) {
		return new DefaultJob(task);
	}

}
