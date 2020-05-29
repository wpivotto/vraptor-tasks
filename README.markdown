Tasks
======

Plug-in library of Quartz job scheduling for vraptor 


*	For vraptor 3 use [this](https://github.com/wpivotto/vraptor-tasks-3) repo

Installation 
--------

*	In a Maven project's pom.xml file:

```xml 
 <repositories>
    <repository>
        <id>sonatype-oss-public</id>
        <url>https://oss.sonatype.org/content/groups/public/</url>
        <releases>
            <enabled>true</enabled>
        </releases>
        <snapshots>
        	<enabled>true</enabled>
	    </snapshots>
    </repository>
</repositories> 

<dependency>
  	<groupId>br.com.prixma</groupId>
  	<artifactId>vraptor-tasks</artifactId>
  	<version>4.3.1</version>
</dependency>
```

*	Or [download](https://oss.sonatype.org/content/groups/public/br/com/prixma/vraptor-tasks/ "Download Link") manually
  
Defining a Task
--------   
```java
@Scheduled(fixedRate = 30000)
public class Spammer implements Task {

	private @Inject Mailer mailer;
	private @Inject MailingList list;
	
	public void execute() {
		for(User user : list) {
			mailer.send(new Spam(user));
		}
	}
	
	public void add(User user) {
		list.add(user);
	}
}
```

Manual Scheduling
--------
1. 	Remove @Scheduled annotation
2. 	Create the following component: 
	
```java	
@ApplicationScoped
public class CustomScheduler {

	public CustomScheduler(TaskScheduler scheduler) {
		scheduler.schedule(mytask.class, customTrigger(), taskId());
	}
}
```

You can schedule a task dynamically with different triggers. For that you should schedule your task with a unique identifier.

Identifiers
--------

The quartz scheduler requires that every task has a unique identifier. You can specify the identifier of the task as follows:

```java
@Scheduled(fixedRate = 5000, id = "...")
public class MyTask implements Task {
```

*If no value has been set, the scheduler will use the `class name` as an identifier.*

	
Tasks and Request Scope
--------

Tasks should only be scheduled using `@ApplicationScoped` or `@Dependent` scopes. 
If your task depends on `@RequestScoped` components, you can annotate a method of your controller that will be invoked automatically.

```java
@RequestScoped
public class Component {

	public void action() {}
	
}

@Controller
public class TaskController {

	private @Inject Component component;

    @Post @Scheduled(fixedRate = 5000)
	public void execute() {
		component.action();
	}
	
}
```

This works because this plugin finds the route to the method and schedule a task that dynamically invokes the associated URL.
This type of task are scheduled **only when the server receives the first request**. Only in this way the plugin can assemble the full path to the method (retrieving the scheme, protocol, port ...). <br>

Stateful Tasks
--------

By default, Quartz jobs are `stateless`, resulting in the possibility of jobs interfering with each other. It might be possible that before the first job has finished, the second one will start. 
To make tasks non-concurrent, set the concurrent flag to `false`. This flag ensures that job execution doesn't overlap.
Example:

```java
@Scheduled(fixedRate = 30000, concurrent = false)
public class BatchImporter implements Task {

	public void execute() {
		...
	}
}
```

Controlling Tasks
--------

Ok, your tasks are scheduled, but sometimes you need control them manually. No problem:

```java	
@Controller
public class TaskController {
	
	private @Inject TaskExecutor executor;
	
	@Path("task/{taskId}/execute")
	void execute(String taskId){
		executor.execute(taskId); //execute it now!
	}

	@Path("task/{taskId}/pause")
	void pause(String taskId){
		executor.pause(taskId); //pause associated trigger, no more executions!
	}
	
	@Path("task/{taskId}/resume")
	void resume(String taskId){
		executor.resume(taskId); //un-pause associated trigger
	}
	
	@Path("tasks/pause")
	void pauseAll(){
		executor.pauseAll(); //pause all triggers (put scheduler in 'remembering' mode)
		//all new tasks will be paused as they are added
	}
	
	@Path("tasks/resume")
	void resumeAll(){
		executor.resumeAll(); //un-pause all triggers
	}
}
```



Monitoring Tasks 
--------
```java
@Controller
public class TasksController {

	@Get("task/{taskId}/stats")
	public void statsFor(String taskId, TaskMonitor monitor) {
		TaskStatistics stats = monitor.getStatisticsFor(taskId);
		log.debug("Fire Time: {}", stats.getFireTime());
		log.debug("Scheduled Fire Time: {}", stats.getScheduledFireTime());
		log.debug("Next Fire Time: {}", stats.getNextFireTime());
		log.debug("Previous Fire Time: {}", stats.getPreviousFireTime());
		log.debug("Execution Time: {}", stats.getExecutionTime());
		log.debug("Max Execution Time: {}", stats.getMaxExecutionTime());
		log.debug("Min Execution Time: {}", stats.getMinExecutionTime());
		log.debug("Execution Count: {}", stats.getExecutionCount());
		log.debug("Refire Count: {}", stats.getRefireCount());
		log.debug("Fail Count: {}", stats.getFailCount());
		log.debug("Last Fault: {}", stats.getLastException());
	}

	public void log(@Observes TaskExecution event) {
		...
	}

}
```	

Creating Custom Tasks 
--------

To create custom tasks: 

1) Create an interface that extends `br.com.caelum.vraptor.tasks.Task`

```java
public interface InterruptableTask extends Task {
	void interrupt(); 	
}
```	

2) Create a class that decorate the execution of its task (must implement `org.quartz.Job`)

```java
public class InterruptableJobWrapper implements InterruptableJob {

	private final InterruptableTask delegate;

	public InterruptableJobWrapper(InterruptableTask delegate) {
		this.delegate = delegate;
	}

	public void execute(JobExecutionContext context) throws JobExecutionException {
		delegate.execute();
	}

	public void interrupt() throws UnableToInterruptJobException {
		delegate.interrupt();
	}
}
```

3) Create a class that provides its task (must implement `br.com.caelum.vraptor.tasks.jobs.JobProvider`)

```java
@ApplicationScoped
public class InterruptableTaskProvider implements JobProvider {

	//Should only instantiate your custom job
	public boolean canProvide(Class<? extends Job> job) {
		return InterruptableJobWrapper.class.equals(job);
	}
	
	//Should only decorate your custom task
	public boolean canDecorate(Class<? extends Task> task) {
		return InterruptableTask.class.isAssignableFrom(task);
	}
	
	//Register your wrapper 
	public Class<? extends Job> getJobWrapper() {
		return InterruptableJobWrapper.class;
	}
	
	//Delegates the execution to your wrapper
	public Job newJob(Task task) {
		return new InterruptableJobWrapper((InterruptableTask) task);
	}

}
```

4) Now we are ready to do some cool task

```java
@ApplicationScoped
@Scheduled(fixedRate = 60000)
public class RuntimeProcessTask implements InterruptableTask {
	
	private final Runtime runtime;
	
	public RuntimeProcessTask(Runtime runtime) {
		this.runtime = runtime;
	}

	public void execute() {
		runtime.exec("ping www.google.com");
	}

	public void interrupt() {
		runtime.kill();
	}
}
```

License
--------
Copyright (c) 2014 William Pivotto
All rights reserved.

Licensed under the Apache License, Version 2.0 (the "License"); 
you may not use this file except in compliance with the License. 
You may obtain a copy of the License at 

http://www.apache.org/licenses/LICENSE-2.0 

Unless required by applicable law or agreed to in writing, software 
distributed under the License is distributed on an "AS IS" BASIS, 
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
See the License for the specific language governing permissions and 
limitations under the License.
