/*
 * Copyright (c) 2012 ICM Uniwersytet Warszawski All rights reserved.
 * See LICENCE.txt file for licensing information.
 */
package de.fzj.unicore.xuudb.server.dynamic;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Arrays;

import org.apache.logging.log4j.Logger;

import de.fzj.unicore.xuudb.Log;


/**
 * Helper class used to invoke external applications.
 * @author K. Benedyczak
 */
public class ProcessInvoker
{
	private static final Logger log = Log.getLogger(Log.XUUDB_SERVER, ProcessInvoker.class);
	
	public static final int MAX_TIMEOUT = 20000;
	
	private int maxTimeout = MAX_TIMEOUT;
	
	public ProcessInvoker()
	{
		this(MAX_TIMEOUT);
	}

	public ProcessInvoker(int maxTimeout)
	{
		this.maxTimeout = maxTimeout;
	}


	/**
	 * Invokes an application with provided arguments.
	 * @param toInvoke executable to invoke
	 * @param args arguments to be added
	 * @return true only if application finished successfully and exited with 0 status
	 */
	public boolean invokeWithChecking(String toInvoke, String[] args)
	{
		String[] cmdLine = new String[args.length + 1];
		cmdLine[0] = toInvoke;
		for (int i=0; i<args.length; i++)
			cmdLine[i+1] = args[i];
		try
		{
			TimeLimitedThread tlt = invokeAndWait(cmdLine);
			int exitStatus = tlt.getP().exitValue();
			if (exitStatus != 0)
			{
				log.error("Execution of command line: " + Arrays.toString(cmdLine) + 
						" finished with a non-zero exit status: " + exitStatus + 
						" StdErr is: " + tlt.getStderr());
				return false;
			}
		} catch (Exception e)
		{
			Log.logException("Execution of command line: " + Arrays.toString(cmdLine) + 
					" finished with an error: " + e.toString(), e, log);
			return false;
		}
		return true;
	}
	
	public Process invoke(String[] cmdLine) throws Exception
	{
		Runtime runtime = Runtime.getRuntime();
		Process process = runtime.exec(cmdLine);
		return process;
	}
	
	public TimeLimitedThread invokeAndWait(String[] cmdLine) throws Exception
	{
		Process process = invoke(cmdLine);
		TimeLimitedThread tlthread = new TimeLimitedThread(process);
		Thread thread = new Thread(tlthread);
		thread.start();
		waitFor(process, thread, tlthread, maxTimeout);
		return tlthread;
	}
	
	
	protected void waitFor(Process p, Thread thread, TimeLimitedThread tlthread, long timeout) throws Exception
	{
		try
		{
			thread.join(timeout);
			if (thread.isAlive()) {
				throw new InterruptedException();
			}
		} catch (InterruptedException e)
		{
			p.destroy();
			thread.interrupt();
			throw new Exception("Killed the invoked script as it hang for at least " +
					timeout + "ms");
		}
		
		if (tlthread.exc != null)
			throw tlthread.exc;
	}
	
	public static String readStdErrOut(InputStream is)
	{
		byte[] buf = new byte[1024];
		int i=0;
		int r=0;
		String msg;
		try
		{
			while ((r=is.read(buf, i, buf.length-i)) > 0)
				i += r;
			msg = new String(buf, 0, i, Charset.defaultCharset());
		} catch (IOException e)
		{
			msg = "Error reading the command's output.";
		}
		return msg;
	}
	
	protected boolean finished(Process p)
	{
		try
		{
			p.exitValue();
		} catch (IllegalThreadStateException ie)
		{
			return false;
		}
		return true;
	}
	
	protected static class TimeLimitedThread implements Runnable
	{
		protected Process p;
		protected String stderr, stdout;
		protected Exception exc = null;
		
		public TimeLimitedThread(Process p)
		{
			this.p = p;
		}
		
		public void unsafeRun() throws Exception
		{
			stdout = readStdErrOut(new BufferedInputStream(p.getInputStream()));
			stderr = readStdErrOut(new BufferedInputStream(p.getErrorStream()));
			p.waitFor();
		}

		public Process getP()
		{
			return p;
		}

		public String getStderr()
		{
			return stderr;
		}

		public String getStdout()
		{
			return stdout;
		}

		public Exception getExc()
		{
			return exc;
		}

		@Override
		public void run()
		{
			try
			{
				unsafeRun();
			} catch (Exception e)
			{
				exc = e;
			}
		}
	}
}
