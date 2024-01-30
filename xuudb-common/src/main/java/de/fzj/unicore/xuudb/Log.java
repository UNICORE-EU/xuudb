package de.fzj.unicore.xuudb;

/**
 * @author K. Benedyczak
 */
public class Log extends eu.unicore.util.Log
{
	public static final String XUUDB_PFX = UNICORE + ".xuudb";
	
	/**
	 * logger prefix for XUUDB server
	 */
	public static final String XUUDB_SERVER = XUUDB_PFX + ".server";

	/**
	 * logger prefix for XUUDB server DB subsystem.
	 */
	public static final String XUUDB_DB = XUUDB_SERVER + ".db";

	/**
	 * logger prefix for XUUDB client
	 */
	public static final String XUUDB_CLIENT = XUUDB_PFX + ".client";
}
