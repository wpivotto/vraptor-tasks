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
  	<version>1.0.1</version>
</dependency>
```
  
Simple Task 
--------   
```java
@PrototypeScoped
@Scheduled(fixedRate = 30000)
public class ReportsDelivery implements Task {

	private Mailer mailer;
	private ScheduledReports reports;
		
	ReportsDelivery(Mailer mailer, ScheduledReports reports){
		this.mailer = mailer;
		this.reports = reports;
	}
	
	public void execute() {
		for(Email email : reports.toDeliver()){
			mailer.send(email);
		}
	}
}
```

Transactional Task (Hibernate)
--------
```java
import br.com.caelum.vraptor.tasks.jobs.hibernate.TransactionalTask;
	
@PrototypeScoped
@Scheduled(cron = "* * 0/12 * * ?")
public class DatabaseDumper implements TransactionalTask {

	private Database database;

	public void execute() {
		database.backup();
	}

	public void setup(Session session, Validator validator) {
		database = new Database(session);
	}
}
```

Transactional Task (JPA)
--------
```java
import br.com.caelum.vraptor.tasks.jobs.jpa.TransactionalTask;
	
@PrototypeScoped
@Scheduled(cron = "* * 0/12 * * ?")
public class DatabaseDumper implements TransactionalTask {

	private Database database;

	public void execute() {
		database.backup();
	}

	public void setup(EntityManager manager, Validator validator) {
		database = new Database(manager);
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
public class client {
	
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
@ApplicationScoped
public class CustomScheduler {

	public CustomScheduler(TaskScheduler scheduler, List<Task> tasks){
		for(Task task : tasks){
			scheduler.schedule(task, customTrigger());
		}
	}
}
```
	
Tasks and Request Scope
--------

If you need to access components with request scope, the easiest way is to build your logic into a method of a controller and call it from a task.
There´s a helper class (TaskRequest) that does it for you. This class finds the path for your method dynamically and performs a get request.

```java
@ApplicationScoped
@Scheduled(fixedRate = 5000)
public class RequestScopeTask implements Task {

	private final TaskRequest request;
	
	public RequestScopeTask(TaskRequest request) {
		this.request = request;
	}

	public void execute() {
		request.access(Controller.class).method();
		if(request.sucess())
			log something...
	}
}
```

```java	
@Resource
public class Controller {
	
	private RequestComponent component;
		
	Controller(RequestComponent component){
		this.component = component;
	}
	
	@Get("task/execute")
	public void method(){
		//put task logic here
	}
}
```
	
If you want to block requests from outside the server, there´s a solution here: <https://gist.github.com/1312993>

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
	
	@Path("task/execute")
	void execute(){
		executor.execute(MyTask.class); //execute it now!
	}

	@Path("task/pause")
	void pause(){
		executor.pause(MyTask.class); //pause associated trigger, no more executions!
	}
	
	@Path("...")
	void resume(Task task){
		executor.resume(task); //un-pause associated trigger
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

	public TasksController(TasksMonitor monitor) {
		TaskStatistics stats = monitor.getStatisticsFor(MyTask.class);
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

	private Session session;
	
	...

	public void executed(Class task, TaskStatistics stats) {
		session.persist(...);
	}
	
	public void scheduled(Task task){ ... }
	
	public void unscheduled(Task task){ ... }
	
	public void failed(Class task, TaskStatistics stats, Exception error){ ... }
	
	public void paused(Class task){ ... }
	
	public void resumed(Class task){ ... }
	
	public void beforeExecute(Class task){ ... }
	
	public void executionVetoed(Class task){ ... }
	
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
