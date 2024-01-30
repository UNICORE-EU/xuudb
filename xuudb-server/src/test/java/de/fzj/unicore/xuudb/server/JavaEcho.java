package de.fzj.unicore.xuudb.server;

public class JavaEcho
{
	/**
	 * To be used as standalone echo program (as we are not sure if /bin/echo is available ;-)
	 * @param args
	 */
	public static void main(String[] args)
	{
		for (String a: args)
			System.out.print(a + ' ');
		System.out.flush();
	}

}
