/*********************************************************************************
 * Copyright (c) 2006 Forschungszentrum Juelich GmbH 
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * (1) Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the disclaimer at the end. Redistributions in
 * binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or other
 * materials provided with the distribution.
 * 
 * (2) Neither the name of Forschungszentrum Juelich GmbH nor the names of its 
 * contributors may be used to endorse or promote products derived from this 
 * software without specific prior written permission.
 * 
 * DISCLAIMER
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *********************************************************************************/
 

package de.fzj.unicore.xuudb.server;

import java.util.Collection;
import java.util.Iterator;
import java.util.Vector;

import org.apache.logging.log4j.Logger;

import de.fzj.unicore.xuudb.Log;


/**
 * does things on VM shutdown. Client code can register with this class to
 * do controlled shutdown operations.
 */
public class ShutdownHook extends Thread {
	private static final Logger log = Log.getLogger(Log.XUUDB_SERVER, ShutdownHook.class);
	private Collection<IShutdownable> runners = null;
	
	public ShutdownHook() {
		runners = new Vector<IShutdownable>();
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
