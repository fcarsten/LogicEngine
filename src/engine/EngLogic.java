/**
 * Copyright 2003-2016 Carsten Friedrich (Carsten.Friedrich@gmail.com) and Colin Murray
 *
 * License: GNU GENERAL PUBLIC LICENSE 3.0 (https://www.gnu.org/copyleft/gpl.html)
 *
 */
package engine;

import java.util.Iterator;
import java.util.Vector;

public class EngLogic {
	private Vector<Vector<String>> formula;
	public Vector<boolean[]> topConfiguration;
	public Vector<boolean[]> bottomConfiguration;
	public Vector<String> variables;

	private void addVariables() {
		for (final Vector<String> clause : this.formula) {
			for (String var : clause) {
				if (var.substring(0, 1).equals(new String("~"))) {
					var = var.substring(1);
				}
				if (!this.variables.contains(var)) {
					this.variables.add(var);
				}
			}
		}
	}

	private void configure(final Vector<Integer> values) {
		this.topConfiguration.clear();
		this.bottomConfiguration.clear();
		int current = 0;
		for (final String temp : this.variables) {
			String var;
			String var_dash;
			if (values.get(current) == 1) {
				var = temp;
				var_dash = new String("~" + temp);
			} else {
				var = new String("~" + temp);
				var_dash = temp;
			}
			final boolean[] col = new boolean[this.formula.size()];
			final boolean[] col_dash = new boolean[this.formula.size()];
			final Iterator<Vector<String>> i = this.formula.iterator();
			int counter = 0;
			while (i.hasNext()) {
				final Vector<String> clause = i.next();
				if (!clause.contains(var)) {
					col[counter] = true;
				} else {
					col[counter] = false;
				}
				if (!clause.contains(var_dash)) {
					col_dash[counter] = true;
				} else {
					col_dash[counter] = false;
				}
				++counter;
			}
			++current;
			this.topConfiguration.add(col);
			this.bottomConfiguration.add(col_dash);
		}
	}

	public EngLogic(final Vector<Vector<String>> f) {
		this.formula = f;
		this.topConfiguration = new Vector<>();
		this.bottomConfiguration = new Vector<>();
		this.variables = new Vector<>();
		this.addVariables();
		final Vector<Integer> values = new Vector<>(this.variables.size());
		for (int i = 0; i != this.variables.size(); ++i) {
			values.add(new Integer(0));
		}
		this.configure(values);
	}
}
