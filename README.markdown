Tasks
======

Plug-in library of Quartz job scheduling for vraptor 

Installation 
--------

1. 	In a Maven project's pom.xml file:

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
  	<version>1.0.3</version>
</dependency>
```
  
Defining a Task
--------   
```java
@PrototypeScoped
@Scheduled(fixedRate = 30000)
public class Spammer implements Task {

	private Mailer mailer;
	private MailingList list;
		
	//Automatically inject constructor arguments	
	Spammer(Mailer mailer, MailingList list) {
		this.mailer = mailer;
		this.list = list;
	}
	
	public void execute() {
		for(User user : list) {
			mailer.send(new Spam(user));
		}
	}
}
```

How to put a task in a database transaction block
--------

Solution: providing a one transaction per task invocation.
Creates a session and begins a transaction on the beginning of the execution, and closes (and commits or rollbacks) them on the end of the execution.

Hibernate example:

```java
import br.com.caelum.vraptor.tasks.jobs.hibernate.TransactionalTask;
	
@PrototypeScoped
@Scheduled(cron = "* * 0/12 * * ?")
public class DatabasePurger implements TransactionalTask {

	private Database database;

	public void execute() {
		database.purge();
	}

	public void setup(Session session, Validator validator) {
		database = new Database(session);
	}
}
```

JPA example:

```java
import br.com.caelum.vraptor.tasks.jobs.jpa.TransactionalTask;
	
@PrototypeScoped
@Scheduled(cron = "* * 0/12 * * ?")
public class DatabasePurger implements TransactionalTask {

	public void setup(EntityManager manager, Validator validator) {
		database = new Database(manager);
	}
}
```

Passing Parameters
--------
	
```java	
public includeParam(TaskScheduler scheduler) {
	scheduler.include("path", "/usr/share/logs/", "LogCleaner").include("maxAge", 60 * 24 * 7, "LogCleaner");
}

@Component
@Scheduled(fixedRate = 60000)
public class LogCleaner implements Task {

	@Param
	private String path;
	
	@Param
	private Integer maxAge = 60 * 24; //default value
	
	public void execute() {
	}
	
}
```



Bean Validation (JSR303)	
--------

To use these features you only need to put any implementation of Bean Validation jars in your classpath.
If validation fails the transaction will not be effective. 

```java
import br.com.caelum.vraptor.tasks.jobs.jpa.TransactionalTask;
import br.com.caelum.vraptor.tasks.validator.Validator;
	
@PrototypeScoped
@Scheduled(fixedRate = 60000)
public class CsvImporter implements TransactionalTask {

	private ClientRepository repository;
	private Validator validator;
	private CsvFile file = ...

	public void execute() {
		if(file.exists()){
			while(file.hasNext()){
				Client client = (Client) file.next();
				validator.validate(client);
				repository.add(client);
			}
		}
	}

	public void setup(EntityManager manager, Validator validator) {
		this.repository = new ClientRepository(manager);
		this.validator = validator;
	}
}
```
```java

@Entity
public class Client {
	
	@Id
	@GeneratedValue
	private Long id;

	@CreditCardNumber
	private String creditCard;
	...

```

Manual Scheduling
--------
1. 	Remove @Scheduled annotation
2. 	Create the following component: 
	
```java	
@Component
public class CustomScheduler {

	public CustomScheduler(TaskScheduler scheduler){
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

Tasks should only be scheduled using `@ApplicationScoped` or `@PrototypeScoped` scopes. 
If your task depends on `@RequestScoped` components, you can annotate a method of your controller that will be invoked automatically.

```java
@Resource
public class TaskController {

	private final Result result;
	
	public TaskController(Result result) {
		this.result = result;
	}

    @Scheduled(fixedRate = 1500)
	public void taskCode1() {
		//put your task logic here
		result.nothing();
	}
	
	@Scheduled(fixedRate = 1500)
	public void taskCode2(String param1, Integer param2) {
		//put your task logic here
		result.nothing();
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
@Resource
public class TaskController {
	
	private TaskExecutor executor;

	TaskController(TaskExecutor executor){
		this.executor = executor;
	}
	
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
@Resource
public class TasksController {

	private final TasksMonitor monitor;
	
	@Path("task/{taskId}/stats")
	public void statsFor(String taskId) {
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
}
```	

More information?

```java
@Component
public class TaskEventLogger implements TaskCallback {

	public void executed(String taskId, TaskStatistics stats){ ... }
	
	public void scheduled(String taskId, Trigger trigger){ ... }
	
	public void unscheduled(String taskId){ ... }
	
	public void failed(String taskId, TaskStatistics stats, Exception error){ ... }
	
	public void paused(String taskId){ ... }
	
	public void resumed(String taskId){ ... }
	
	public void beforeExecute(String taskId){ ... }
	
	public void executionVetoed(String taskId){ ... }
	
	...
}
```	

Creating Custom Tasks 
--------

To create custom tasks: 

1.	Create an interface that extends `br.com.caelum.vraptor.tasks.Task`

```java
public interface InterruptableTask extends Task {
	void interrupt(); 	
}
```	

2.	Create a class that decorate the execution of its task (must implement `org.quartz.Job`)

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

3.	Create a class that provides its task (must implement `br.com.caelum.vraptor.tasks.jobs.JobProvider`)

```java
@Component
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

4.	Now we are ready to do some cool task

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
Copyright (c) 2011 William Pivotto
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
