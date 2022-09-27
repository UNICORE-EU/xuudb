package de.fzj.unicore.xuudb.client;

import java.io.File;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import de.fzj.unicore.xuudb.client.actions.AbstractAction;
import de.fzj.unicore.xuudb.client.actions.AddAction;
import de.fzj.unicore.xuudb.client.actions.AddDNAction;
import de.fzj.unicore.xuudb.client.actions.CheckCertAction;
import de.fzj.unicore.xuudb.client.actions.CheckDNAction;
import de.fzj.unicore.xuudb.client.actions.ConnectionManager;
import de.fzj.unicore.xuudb.client.actions.DAPFindMappingAction;
import de.fzj.unicore.xuudb.client.actions.DAPFindReverseMappingAction;
import de.fzj.unicore.xuudb.client.actions.DAPFreezeMappingAction;
import de.fzj.unicore.xuudb.client.actions.DAPGetAttributes;
import de.fzj.unicore.xuudb.client.actions.DAPListMappingAction;
import de.fzj.unicore.xuudb.client.actions.DAPListPoolsAction;
import de.fzj.unicore.xuudb.client.actions.DAPRemoveMappingAction;
import de.fzj.unicore.xuudb.client.actions.DAPRemovePoolAction;
import de.fzj.unicore.xuudb.client.actions.DAPSimulateGetAttributes;
import de.fzj.unicore.xuudb.client.actions.ExportAction;
import de.fzj.unicore.xuudb.client.actions.ImportAction;
import de.fzj.unicore.xuudb.client.actions.ListAction;
import de.fzj.unicore.xuudb.client.actions.RemoveAction;
import de.fzj.unicore.xuudb.client.actions.UpdateAction;

public class CLCExecutor {
	private ConnectionManager connManager;
	private ClientConfiguration conf;
	private String configFile = null;
	private Map<String, AbstractAction> actions;
	private String[] parsedLine;

	public CLCExecutor() {
		actions = new HashMap<>();
	}

	public CLCExecutor(ConnectionManager m) {
		this();
		this.connManager = m;
	}

	public CLCExecutor(ConnectionManager m, String config) {
		this();
		this.connManager = m;
		this.configFile = config;
	}

	public CLCExecutor(String config) {
		this();
		this.configFile = config;
	}

	public String[] getParsedLine() {
		return parsedLine;
	}

	public void init() throws Exception {

		ServiceFactory serviceFac = new ServiceFactory(conf);
		connManager = new ConnectionManager(serviceFac.getAdminAPI(),
				serviceFac.getPublicAPI(), serviceFac.getDAPAdminAPI(),
				serviceFac.getDAPPublicAPI());

	}

	public void registerHelpActions() {
		AbstractAction aaObj;
		aaObj = new HelpAction(connManager);
		actions.put(aaObj.getName(), aaObj);
		aaObj = new HelpFullAction(connManager);
		actions.put(aaObj.getName(), aaObj);

	}

	private void registerAction(Class<? extends AbstractAction> aa) {
		AbstractAction aaObj;
		try {
			Constructor<? extends AbstractAction> constructor = aa
					.getDeclaredConstructor(ConnectionManager.class);
			aaObj = constructor.newInstance(connManager);
		} catch (Exception e) {
			System.err.println("Buggy action: can't register action " + aa);
			e.printStackTrace();
			return;
		}
		actions.put(aaObj.getName(), aaObj);
		String[] aliases = aaObj.getAliases();
		for (String alias : aliases)
			actions.put(alias, aaObj);
	}

	public void registerAllActions() {
		registerAction(AddDNAction.class);
		registerAction(AddAction.class);
		registerAction(ListAction.class);
		registerAction(RemoveAction.class);
		registerAction(UpdateAction.class);
		registerAction(ExportAction.class);
		registerAction(ImportAction.class);

		registerAction(CheckCertAction.class);
		registerAction(CheckDNAction.class);

		registerAction(DAPListMappingAction.class);
		registerAction(DAPFindMappingAction.class);
		registerAction(DAPFindReverseMappingAction.class);
		registerAction(DAPFreezeMappingAction.class);
		registerAction(DAPRemoveMappingAction.class);
		registerAction(DAPRemovePoolAction.class);
		registerAction(DAPListPoolsAction.class);
		registerAction(DAPSimulateGetAttributes.class);
		registerAction(DAPGetAttributes.class);
	}

	public boolean parseLine(String[] tokens) throws Exception {
		if (tokens.length == 0 || tokens[0].equals(""))
			return false;

		String cmd = tokens[0];
		AbstractAction aa = actions.get(cmd);
		if (aa == null) {
			System.out.println("Command '" + cmd + "' not found."
					+ " Type 'help' to get started.");
			return false;
		}
		if (tokens.length - 1 < aa.getMinArgsNumber()) {
			System.out.println("There must be at least "
					+ aa.getMinArgsNumber() + " arguments to the '"
					+ aa.getName() + "' command.");
			return false;
		}

		if (tokens.length - 1 > aa.getMaxArgsNumber()) {
			System.out.println("There must be no more than "
					+ aa.getMaxArgsNumber() + " arguments to the '"
					+ aa.getName() + "' command.");
			return false;
		}

		String[] shiftedTokens = new String[tokens.length - 1];
		System.arraycopy(tokens, 1, shiftedTokens, 0, tokens.length - 1);

		return aa.invoke(shiftedTokens, conf.isBatch());

	}

	public boolean processCommandLineArgs(String[] args) {
		Boolean hasRealArgs = true;

		if (args.length == 0) {
			printXUUDBUsage();
			return false;
		}

		if (args[0].equalsIgnoreCase("--config") && args.length > 1) {
			configFile = args[1];
			if (args.length > 2) {
				String[] shiftedArgs = new String[args.length - 2];
				System.arraycopy(args, 2, shiftedArgs, 0, args.length - 2);
				args = shiftedArgs;

			} else {
				hasRealArgs = false;
			}
		}

		if (hasRealArgs == true) {
			parsedLine = args;
			return true;
		} else {
			printXUUDBUsage();
			return false;
		}
	}

	public void printXUUDBUsage() {
		System.out.println("This is a command line client which operates on a "
				+ "UNICORE XUUDB system.\nThe basic syntax is:\n"
				+ "   admin.??? [--config <configFile>] <COMMAND ...>\n\n"

				+ "Options are:\n" + "     -e   Print errors\n"
				+ "Without options this help message is printed. "
				+ "Options order must be preserved.\n\n"

				+ "Use 'help' command to see the list or 'help <CMD>' "
				+ "to get help on every command.\n\n");
	}

	public void readConfig() {
		try {
			if (configFile == null) {
				configFile = System.getProperty("xuudb.client.conf", new File(
						"conf", "xuudb_client.conf").getAbsolutePath());
			}
			conf = new ClientConfiguration(new File(configFile));
		} catch (Exception e1) {
			System.out.println("Can't read configuration: " + e1);

			System.exit(1);
		}

	}

	private class HelpAction extends AbstractAction {
		HelpAction(ConnectionManager cm) {
			super(cm, "help",
					"Provides help. Use without arguments to get generic help"
							+ " or with command name as parameter to get help"
							+ " on the specified command usage.", 0, 1);
			setCategory(AbstractAction.Category.other.toString());
		}

		@Override
		public boolean invoke(String[] args, boolean isBatch) {
			if (args.length >= 1) {
				AbstractAction a = actions.get(args[0]);
				if (a == null)
					System.out.println("Command " + args[0] + " is not known");
				else
					System.out.println("Help for command: " + args[0] + " (category: " + a.getCategory() + ")\n"
							+ a.getHelp());
			} else {
				System.out.println("This is command line client which operates on "
								+ "UNICORE XUUDB system. Type 'help <CMD>' to get help on "
								+ "every command. Available commands are:");
				Iterator<AbstractAction> it = actions.values().iterator();
				List<AbstractAction> list = new ArrayList<>();
				while (it.hasNext()) {
					AbstractAction aa = it.next();
					if (!list.contains(aa))
						list.add(aa);
				}
				Collections.sort(list);
				Collections.reverse(list);
				String cat = "";
				for (AbstractAction a : list) {
					if (!cat.equals(a.getCategory())) {
						System.out.println("\n[" + a.getCategory() + "]");
						cat = a.getCategory();
					}
					System.out.print("  " + a.getName());
					for (String alias : a.getAliases())
						System.out.print(" (" + alias + ")");
					System.out.println();
				}
			}
			return true;
		}
	}

	private class HelpFullAction extends AbstractAction {
		HelpFullAction(ConnectionManager cm) {
			super(cm, "helpAll", "Provides full help for all commands.", 0);
			setCategory(AbstractAction.Category.other.toString());
		}

		@Override
		public boolean invoke(String[] args, boolean isBatch) {
			System.out.println("This is a command line client which operates on the "
					+ "UNICORE XUUDB. Type 'help <CMD>' to get help on "
					+ "a command. Available commands are:");
			Iterator<AbstractAction> it = actions.values().iterator();
			List<AbstractAction> list = new ArrayList<>();
			while (it.hasNext()) {
				AbstractAction aa = it.next();
				if (!list.contains(aa))
					list.add(aa);
			}
			Collections.sort(list);
			Collections.reverse(list);
			for (AbstractAction a : list) {
				System.out.println("\n------------------------------------\n"
						+ "Command: " + a.getName() + "\n");
				System.out.println(a.getHelp());
			}
			return true;
		}
	}

	public Map<String, AbstractAction> getActions() {
		return actions;
	}

}
