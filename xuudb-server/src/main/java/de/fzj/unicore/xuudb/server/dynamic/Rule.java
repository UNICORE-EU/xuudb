package de.fzj.unicore.xuudb.server.dynamic;

import java.util.Collections;
import java.util.List;

import org.springframework.expression.Expression;

/**
 * Generic rule which can route a request to its Mapping. Immutable.
 *
 * @author golbi
 */
public class Rule
{
	private final Expression condition;
	private final boolean overwrite;
	private final List<Mapping> actions;

	public Rule(Expression condition, boolean overwrite, List<? extends Mapping> mappings)
	{
		this.condition = condition;
		this.overwrite = overwrite;
		this.actions = Collections.unmodifiableList(mappings);
	}

	public Expression getCondition()
	{
		return condition;
	}

	public boolean isOverwrite()
	{
		return overwrite;
	}

	public List<Mapping> getActions()
	{
		return actions;
	}
}
