/*
 * Copyright (c) 2012 ICM Uniwersytet Warszawski All rights reserved.
 * See LICENCE.txt file for licensing information.
 */
package de.fzj.unicore.xuudb.server.db;

public interface IStorage {
	public IClassicStorage getClassicStorage();
	public IRESTClassicStorage getRESTClassicStorage();
	public IPoolStorage getPoolStorage();
	public void shutdown();
}
