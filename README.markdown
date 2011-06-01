Tasks
======

Plug-in da biblioteca de agendamento de tarefas Quartz, para Vraptor


Tarefa Simples
--------

	@PrototypeScoped
	@Scheduled("* * 0/12 * * ?")
	public class Backup implements Task {

		private Database database = new Database();

		@Override
		public void execute() {
			database.backup();
		}

	}


Tarefa com controle transacional
--------

	@PrototypeScoped
	@Scheduled(fixedRate = 5000)
	public class DatabaseFiller implements TransactionalTask {

		private Database database;

		@Override
		public void execute() {
			database.add(new RandomRecord());
		}

		@Override
		//setup DAOs, Repositories...
		public void setup(Session session) {
			database = new Database(session);
		}
	}
	
