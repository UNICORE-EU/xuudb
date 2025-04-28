package eu.unicore.xuudb.client.actions;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.text.DateFormat;

import org.apache.logging.log4j.Logger;

import eu.unicore.xuudb.xbeans.LoginDataType;
import eu.unicore.xuudb.xbeans.MappingDataType;
import eu.unicore.xuudb.Log;
import eu.unicore.xuudb.X509Utils;

public abstract class AbstractAction implements Comparable<AbstractAction> {
	
	protected static final Logger logger = Log.getLogger(Log.XUUDB_CLIENT);

	protected enum Category {classic, dynamic, other}; 
	private static final DateFormat SHORT = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.MEDIUM);
	protected final ConnectionManager cm;
	protected final String cmd;
	protected final String help;
	protected String category;
	protected String[] aliases;
	protected int minArgs;
	protected int maxArgs;
	
	protected AbstractAction(ConnectionManager cm, String cmd, String help) {
		this.cm = cm;
		this.cmd = cmd;
		this.help = help;
		minArgs = 0;
		maxArgs = 100;
		aliases = new String[0];
		this.category = Category.classic.toString();
	}

	protected AbstractAction(ConnectionManager cm, String cmd, String help,
			int minArgs) {
		this(cm, cmd, help);
		this.minArgs = minArgs;
	}

	protected AbstractAction(ConnectionManager cm, String cmd, String help,
			int minArgs, int maxArgs) {
		this(cm, cmd, help);
		this.minArgs = minArgs;
		this.maxArgs = maxArgs;
	}

	protected AbstractAction(ConnectionManager cm, String cmd, String help,
			int minArgs, int maxArgs, String alias) {
		this(cm, cmd, help, minArgs, maxArgs);
		aliases = new String[] { alias };
	}

	public String getName() {
		return cmd;
	}

	public String getHelp() {
		return help;
	}

	public int getMinArgsNumber() {
		return minArgs;
	}

	public int getMaxArgsNumber() {
		return maxArgs;
	}

	public int compareTo(AbstractAction a) {
		int c = a.getCategory().compareTo(getCategory());
		if (c == 0)
			return a.getName().compareTo(cmd);
		return c;
	}

	public String[] getAliases() {
		return aliases;
	}

	public abstract boolean invoke(String[] args, boolean isBatch)
			throws Exception;

	protected void logArguments(String[] args) {
		logger.debug("Command: {}", getName());
		for (int i = 0; i < args.length; i++) {
			logger.debug("Parameter {}: {}", i, args[i]);
		}
	}

	protected LoginDataType parseArgsToUlogin(String[] args) throws Exception {
		LoginDataType ret = LoginDataType.Factory.newInstance();
		boolean alreadyHaveToken = false;
		for (int i = 0; i < args.length; ++i) {
			if (args[i].toLowerCase().startsWith("gcid="))
				ret.setGcID(args[i].substring(5));
			else if (args[i].toLowerCase().startsWith("xlogin="))
				ret.setXlogin(args[i].substring(7));
			else if (args[i].toLowerCase().startsWith("pemfile=")) {
				String pem = args[i].substring(8);
				ret.setToken(X509Utils.getStringFromPEMFile(pem));
				alreadyHaveToken = true;
			} else if (args[i].toLowerCase().startsWith("dn=")) {
				if (alreadyHaveToken) {
					throw new Exception(
							"You can not use pemfile= and dn= at a time.");
				}
				ret.setToken(args[i].substring(3));
			} else if (args[i].toLowerCase().startsWith("role="))
				ret.setRole(args[i].substring(5));
			else if (args[i].toLowerCase().startsWith("project="))
				ret.setProjects(args[i].substring(8));
			else {
				throw new Exception("Unknown parameter: " + args[i]);
			}
		}
		return ret;
	}

	protected boolean confirm(String message) {
		System.out.println(message + "\nAre you sure? [yes, no]");
		String ans = "no";
		try (BufferedReader br = new BufferedReader(new InputStreamReader(System.in))){
			ans = br.readLine();
		} catch (Exception e) {
		}

		return "yes".equalsIgnoreCase(ans);
	}

	protected void printMapping(MappingDataType[] resp) {
		if (resp == null)
			System.out.println("The database query result is empty.");
		if (resp.length == 0)
			System.out.println("The database query result is empty.");
		else {
			System.out.println("The database query result contains   "
					+ resp.length + "   entries:");
			System.out.printf("%-5s|%-25s|%-10s|%-20s|%-30s|%-18s|%-18s|\n",
					"  Id", "         Key", " Key type", "       Value", "          Pool name",
					"    Freeze time", "   Last Access");
			System.out.println("-------------------------------------------------------------------------------------------------------------------------------------");

			for (int i = 0; i < resp.length; i++) {
				String freeze = (resp[i].getFreezeTime() != null) ? 
						SHORT.format(resp[i].getFreezeTime().getTime()) : "";
				String last = (resp[i].getLastAccess() != null) ? 
						SHORT.format(resp[i].getLastAccess().getTime()) : "";
				
				System.out.printf("%5s|%25s|%10s|%20s|%30s|%18s|%18s|\n",
						resp[i].getId(), resp[i].getKey(),
						resp[i].getKeyType(), resp[i].getValue(), resp[i]
								.getPoolName(), freeze, last);
			}

		}
	}
	
	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}
}
