package br.com.caelum.vraptor.tasks.scheduler;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import br.com.caelum.vraptor.ioc.Stereotype;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@Stereotype
public @interface Scheduled {
	String cron() default "";
	int fixedRate() default 0;
	int initialDelay() default 1000;
	boolean concurrent() default true;
	String id() default "";
}