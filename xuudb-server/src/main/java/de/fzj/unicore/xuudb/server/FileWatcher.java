package de.fzj.unicore.xuudb.server;

import java.io.File;
import java.io.FileNotFoundException;

/**
 * helper to watch a file and invoke a specific action if it was modified.
 * This should be executed periodically, for example using a scheduled executor
 * service.
 * 
 * @author schuller
 */
public class FileWatcher implements Runnable{

	private final File target;
	
	private final Runnable action;
	
	private long lastAccessed;
		
	public FileWatcher(File target, Runnable action)throws FileNotFoundException{
		if(!target.exists() || !target.canRead()){
			throw new FileNotFoundException("File "+target.getAbsolutePath()+
					" does not exist or is not readable.");
		}
		this.target=target;
		this.action=action;
		lastAccessed=target.lastModified();
	}
	
	/**
	 * check if target file has been touched and invoke 
	 * the action if it has
	 */
	public void run(){
		if(target.lastModified()>lastAccessed){
			lastAccessed=target.lastModified();
			action.run();
		}
	}
	
}
