Tasks
======

Plug-in da biblioteca de agendamento de tarefas Quartz, para Vraptor

Intalacao
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

Para usar as validaçõs basta adicionar no seu classpath qualquer implementação do Bean Validation.
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
	
	Remova a anotacao @Scheduled das Tasks
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
