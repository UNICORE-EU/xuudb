package eu.unicore.xuudb.server.dynamic;

import java.util.ArrayList;
import java.util.List;

/**
 * Performs lexing (tokenization) of the input string with the following rules:
 * <ol>
 * <li> string is break on whitespaces
 * <li> parts of the string with spaces are returned as one token when surrounded with " 
 * <li> it is possible to cite any character by prefixing it with \. So to use literal
 * '\' write '\\'.
 * </ol>
 * Copied from XNJS ;-((((
 * @author golbi
 *
 */
public class SimplifiedCmdLineLexer
{
	public static String[] tokenizeString(String origStr)
	{
		List<String> tokens = new ArrayList<>();
		boolean inCitation = false;
		StringBuilder token = new StringBuilder();
		
		char[] orig = origStr.toCharArray();
		for (int i=0; i<orig.length; i++)
		{
			switch (orig[i])
			{
			case '\\':
				if (i < orig.length-1)
				{
					token.append(orig[i+1]);
					i++;
				} else
					token.append(orig[i]);
				
				break;
			case ' ':
				if (!inCitation)
				{
					if (token.length() > 0)
						tokens.add(token.toString());
					token = new StringBuilder();
					while (i<orig.length && orig[i] == ' ')
						i++;
					i--;
				} else
				{
					token.append(orig[i]);
				}
				break;
			case '"':
				inCitation = !inCitation;
				break;
			default:
				token.append(orig[i]);
			}
		}
		if (token.length() > 0)
			tokens.add(token.toString());
		return tokens.toArray(new String[tokens.size()]);
	}
}
