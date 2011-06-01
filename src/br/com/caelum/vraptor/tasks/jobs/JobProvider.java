package br.com.caelum.vraptor.tasks.jobs;

import org.quartz.Job;
import org.quartz.JobDetail;

public interface JobProvider {

	boolean canProvide(Class<? extends Job> job);

	Job newJob(JobDetail jobDetail);

}
