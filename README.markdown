Tasks
======

Plug-in da biblioteca de agendamento de tarefas Quartz, para Vraptor

Instalação
--------
Adicione ao seu web.xml

	<context-param>
        	<param-name>br.com.caelum.vraptor.packages</param-name>
	        <param-value>br.com.caelum.vraptor.tasks</param-value>
    </context-param>
    
Tarefa Simples
--------   

	@PrototypeScoped
	@Scheduled(cron = "* * 0/12 * * ?")
	public class Backup implements Task {

		private Database database = new Database();

		@Override
		public void execute() {
			database.backup();
		}

	}


Tarefa com controle transacional (Hibernate)
--------

	import br.com.caelum.vraptor.tasks.jobs.hibernate.TransactionalTask;
	
	@PrototypeScoped
	@Scheduled(fixedRate = 5000, initialDelay = 3000)
	public class DatabaseFiller implements TransactionalTask {

		private Database database;

		@Override
		public void execute() {
			database.add(new RandomRecord());
		}

		@Override
		//setup DAOs, Repositories...
		public void setup(Session session, TaskValidator validator) {
			database = new HibernateDatabase(session);
		}
	}
	
Tarefa com controle transacional (JPA)
--------

	import br.com.caelum.vraptor.tasks.jobs.jpa.TransactionalTask;
	
	@PrototypeScoped
	@Scheduled(fixedRate = 5000, initialDelay = 3000)
	public class DatabaseFiller implements TransactionalTask {

		private Database database;

		@Override
		public void execute() {
			database.add(new RandomRecord());
		}

		@Override
		//setup DAOs, Repositories...
		public void setup(EntityManager manager, TaskValidator validator) {
			database = new JPADatabase(manager);
		}
	}

Bean Validation (JSR303)	
--------

Para usar as validações basta adicionar no seu classpath qualquer implementação do Bean Validation.
Se a validação falhar a transação não será efetivada.

	import br.com.caelum.vraptor.tasks.jobs.jpa.TransactionalTask;
	
	@PrototypeScoped
	@Scheduled(fixedRate = 60000)
	public class CsvImporter implements TransactionalTask {

		private Database database;
		private TaskValidator validator;
		private CsvFile file = ...

		public void execute() {
			if(file.exists()){
				while(file.hasNext()){
					Client client = (Client) file.next();
					validator.validate(client);
					database.add(client);
				}
			
			}
		}

		public void setup(EntityManager manager, TaskValidator validator) {
			this.database = new JPADatabase(manager);
			this.validator = validator;
		}
	}

--------

	@Entity
	public class client {
	
	@Id
	@GeneratedValue
	private Long id;
	
	@CreditCardNumber
	private String creditCard;
	
	...
	
	
Agendamento manual
--------
	
	Remova a anotação @Scheduled das Tasks
	Crie o seguinte componente:
	
	@Component
	@ApplicationScoped
	public class CustomScheduler {

		public CustomScheduler(TaskScheduler scheduler, List<Task> tasks){
			for(Task task : tasks){
				scheduler.schedule(task, custom trigger);
			}
		}
	}
	
Monitorando Tasks
--------
	
	@Resource
	public class TasksController {

		public TasksController(TasksMonitor monitor){
			TaskStatistics stats = monitor.getStatisticsFor(MyTask.class);
			log.info("Next Fire Time " + stats.getNextFireTime());
			...
		}
	}
	
Criando Tasks Personalizadas
--------

Para criar tasks personalizadas:

1.	Crie uma interface que extenda `br.com.caelum.vraptor.tasks.Task`

		public interface CustomTask extends Task {
		
			void myCustomBehaviour(MyCustomDependency dep); 
			
		}
	
2.	Crie uma classe que decore a execução de sua Task (deve implementar `org.quartz.Job`)

		public class CustomJobWrapper implements Job {
	
			private final CustomTask task;
			private final MyCustomDependency dep;
		
			public CustomJobWrapper(CustomTask task, MyCustomDependency dep) {
				this.task = task;
				this.dep = dep;
			}
		
			public void execute(JobExecutionContext context) throws JobExecutionException {
				task.myCustomBehaviour(dep);
				task.execute();
			}
	
		}

3.	Crie uma classe que implemente `br.com.caelum.vraptor.tasks.jobs.JobProvider`

		@Component
		@ApplicationScoped
		public class CustomTaskProvider implements JobProvider {
	
			private MyCustomDependency dep;
			
			//receba as dependências da sua Task via construtor
			public CustomTaskProvider(MyCustomDependency dep){
				this.dep = dep;
			}
			
			//Deverá decorar somente as Tasks personalizadas
			public boolean canDecorate(Class<? extends Task> task) {
				return CustomTask.class.isAssignableFrom(task);
			}
			
			//Registra o seu decorator
			public Class<? extends Job> getJobWrapper() {
				return CustomJobWrapper.class;
			}
			
			//Deverá instanciar somente as Tasks personalizadas
			public boolean canProvide(Class<? extends Job> job) {
				return CustomJobWrapper.class.equals(job);
			}
	
			//Delega a execução para o seu decorator
			public Job newJob(Task task) {
				return new CustomJobWrapper((CustomTask) task, dep);
			}
	
		}

Licença
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
