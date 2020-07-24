/*
 * Copyright (c) 2011-2012 ICM Uniwersytet Warszawski All rights reserved.
 * See LICENCE.txt file for licensing information.
 */
package de.fzj.unicore.xuudb.server.dynamic;


/**
 * Base class for all mapping types. Extensions perform an actual mapping,
 * i.e. add/set attributes.
 * 
 * @author K. Benedyczak
 */
public abstract class Mapping
{
	protected String configuration;
	protected MappingType type;
	
	
	public Mapping(String configuration, MappingType maps)
	{
		this.configuration = configuration.trim();
		type = maps;
	}
	
	public abstract void applyAttributes(EvaluationContext context, boolean overwrite, boolean dryRun);

	/**
	 * @return the configuration
	 */
	public String getConfiguration()
	{
		return configuration;
	}

	/**
	 * @return the type
	 */
	public MappingType getType()
	{
		return type;
	}	
}
