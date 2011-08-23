package br.com.caelum.vraptor.tasks.validator;

import java.util.Collection;
import java.util.List;

import br.com.caelum.vraptor.validator.Message;
import br.com.caelum.vraptor.validator.Validations;

public interface Validator {
	
	public void checking(Validations validations);

	public void validate(Object object);

	public void addAll(Collection<? extends Message> messages);

	public void add(Message message);

	public boolean hasErrors();

	public List<Message> getErrors();

}
