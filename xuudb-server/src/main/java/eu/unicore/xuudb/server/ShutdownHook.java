package eu.unicore.xuudb.server;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.apache.logging.log4j.Logger;

import eu.unicore.xuudb.Log;

/**
 * does things on VM shutdown. Client code can register with this class to
 * do controlled shutdown operations.
 */
public class ShutdownHook extends Thread {
	private static final Logger log = Log.getLogger(Log.XUUDB_SERVER, ShutdownHook.class);
	private final Collection<IShutdownable> runners = new ArrayList<>();
	
	public ShutdownHook() {
		Runtime.getRuntime().addShutdownHook(this);
	}

	public void register(IShutdownable obj) {
		if (obj==null)
			throw new IllegalArgumentException("Shutdown hook can't be null");
		runners.add(obj);
	}
	
	public void run() {
		Iterator<IShutdownable> it = runners.iterator();
		while(it.hasNext()) {
			IShutdownable now = it.next();
			log.info("Shutting down <" + now.getNameOfService() + "> ...");
			try {
				now.shutdown();
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				log.info("Done");
			}
		}
		log.info("Bye.");
	}
}
