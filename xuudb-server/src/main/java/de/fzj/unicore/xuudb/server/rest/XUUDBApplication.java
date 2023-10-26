package de.fzj.unicore.xuudb.server.rest;

import java.util.HashSet;
import java.util.Set;

import jakarta.ws.rs.core.Application;

public class XUUDBApplication extends Application {

	@Override
	public Set<Class<?>> getClasses() {
		Set<Class<?>>classes = new HashSet<>();
		classes.add(RestXUUDB.class);
		return classes;
	}

}
