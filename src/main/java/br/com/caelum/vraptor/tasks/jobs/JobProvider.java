package br.com.caelum.vraptor.tasks.jobs;

import org.quartz.Job;

import br.com.caelum.vraptor.tasks.Task;

public interface JobProvider {

	Job newJob(Task task);

}
