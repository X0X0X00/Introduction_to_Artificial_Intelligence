package hw3;

import java.io.File;
import java.util.*;


public class sat {
	private static boolean useUnitClause = true; // unit clause heuristic control
	private static boolean usePureLiteral = true; // pure literal heuristic
	private static Set<Map<String, Boolean>> visited = new HashSet<>();
	public static void main(String[] args) {
		List<String> expressions = new ArrayList<>();
		// parse command line arguments
		for (String arg : args) {
			if (arg.equals("--nounit")) {
				useUnitClause = false;
			} else if (arg.equals("--nopure")) {
				usePureLiteral = false;
			}
		}

		try {
//			File file = new File("src/hw1/sat-problem2.txt");
//			Scanner scanner = new Scanner(file);
			Scanner scanner = new Scanner(System.in);

			// read the logical expressions line by line
			while (scanner.hasNextLine()) {
				String line = scanner.nextLine();
				expressions.add(line);
			}

			scanner.close();

			boolean result = Solver(expressions);

			if (!result) {
				System.out.println("unsatisfiable");
			}

		} catch (Exception e) {
			System.out.println("IO exception: " + e.getMessage());
		}
	}

	private static List<String> extractVariables(List<String> expressions) {
		// expressions： ["A,B", "~B,C"] -> variables: ["A", "B", "C"]
		Set<String> variables = new HashSet<>();

		for (String expr : expressions) {
			// expr: "~B,C" -> tokens: ["~B", "C"]
			String[] tokens = expr.split(",");
			// tokens-cyc1: ["A", "B"]
			// tokens-cyc2: ["~B", "C"]
			for (String token : tokens) {
				String variable = token.replace("~", "").trim(); // 移除否定符号
				variables.add(variable);
			}
		}

		return new ArrayList<>(variables);
	}

	public static boolean Solver(List<String> expressions) {

		List<List<String>> clauses = new ArrayList<>();
		for (String expression : expressions) {
			String[] literals = expression.split(",");
			List<String> clause = new ArrayList<>(Arrays.asList(literals));
			clauses.add(clause);
		}

		List<String> variables = extractVariables(expressions);
		Map<String, Boolean> tryResult = new HashMap<>();
		// duplicate the original result
		List<List<String>> clausesCopy = new ArrayList<>(clauses);

		// recursively solve the problem
		return dpll(clauses, variables, tryResult, clausesCopy);
	}

	private static boolean dpll(List<List<String>> clauses, List<String> variables, Map<String, Boolean> tryResult, List<List<String>> clausesCopy) {
		// check the recursion out
		if (satisfied(clauses, variables, tryResult)) {
			printSolution(tryResult, variables);
			return true; // Once there is a solution, return true and exit
		}

		// check if there is a blank clause
		for (List<String> clause : clauses) {
			if (clause.isEmpty()) {
				return false; // conflict found, early termination
			}
		}

		// unit clause
		if (useUnitClause) {
			String unitClause = findUnitClause(clauses, tryResult);
			if (unitClause != null) {
				boolean value = !unitClause.startsWith("~");
				String variable = unitClause.replace("~", "").trim();
				tryResult.put(variable, value);
				clauses = simplify(clauses, variable, value);
				return dpll(clauses, variables, tryResult, clausesCopy);
			}
		}

		// pure literal
		if (usePureLiteral) {
			applyPureLiteralHeuristic(clauses, tryResult);
		}

		if (satisfied(clauses, variables, tryResult)) {
			printSolution(tryResult, variables);
			return true;
		}

		// find the first unassigned variable
		String variable = findUVar(variables, tryResult);
		if (variable == null) {
			return false;
		}
		// check if the current tryResult has been visited
		for (Map<String, Boolean> visitedTryResult : visited) {
			if (visitedTryResult.entrySet().containsAll(tryResult.entrySet())) {
				return false;
			}
		}

		// try to assign the variable to true
		tryResult.put(variable, true);
		if (dpll(simplify(clauses, variable, true), variables, tryResult, clausesCopy)) {
			return true;
		}

		// backtracking
		tryResult.put(variable, false);
		if (dpll(simplify(clauses, variable, false), variables, tryResult, clausesCopy)) {
			return true;
		}
		tryResult.remove(variable); // prune
		visited.add(new HashMap<>(tryResult)); // record the visited tryResult
		return false;
	}

	private static boolean satisfied(List<List<String>> clauses, List<String> variables, Map<String, Boolean> tryResult) {
		if (tryResult.size() != variables.size()) {
			// if the tryResult is not complete, return false
			return false;
		}
		for (List<String> clause : clauses) {
			// for each clause, check if it is satisfied
			boolean clauseSatisfied = false;
			for (String literal : clause) {
				boolean isNegated = literal.startsWith("~");
				String variable = literal.replace("~", "").trim();
				boolean value = tryResult.getOrDefault(variable, false); // there should be a value for each variable
				if (isNegated) {
					value = !value;
				}
				if (value) {
					// inner loop, once the clause is satisfied, break
					clauseSatisfied = true;
					break;
				}
			}
			if (!clauseSatisfied) {
				// if the clause is not satisfied, return false
				return false;
			}
		}
		// all clauses are satisfied
		return true;
	}

	private static String findUnitClause(List<List<String>> clauses, Map<String, Boolean> tryResult) {
		// iterate
		for (List<String> clause : clauses) {
			// ensure that it is a unit clause
			if (clause.size() == 1) {
				String variable = clause.get(0);
				// check whether the
				if (!tryResult.containsKey(variable) && !tryResult.containsKey(variable.replace("~", ""))) {
					return variable; // return the selected var
				}
			}
		}
		return null; // if not found, return null
	}


	private static void applyPureLiteralHeuristic(List<List<String>> clauses, Map<String, Boolean> tryResult) {
		Map<String, Integer> literalCount = new HashMap<>();
		// count the appearance of each literal
		for (List<String> clause : clauses) {
			for (String literal : clause) {
				literalCount.put(literal, literalCount.getOrDefault(literal, 0) + 1);
			}
		}

		for (String literal : literalCount.keySet()) {
			String variable = literal.replace("~", "").trim();
			if (!tryResult.containsKey(variable)) {
				// find the pure literal, which means ~X and X cannot appear at the same time, so the product is 0
				if (literalCount.getOrDefault("~" + variable, 0) * literalCount.getOrDefault(variable, 0) == 0) {
					boolean value = !literal.startsWith("~"); // to make sure the pure literal is true
					tryResult.put(variable, value);
				}
			}
		}
	}

	private static String findUVar(List<String> variables, Map<String, Boolean> tryResult) {
		// find the first unassigned variable in the variables
		for (String variable : variables) {
			if (!tryResult.containsKey(variable)) {
				// if the variable is not assigned, return it
				return variable;
			}
		}
		return null;
	}

	private static String findUVar_old(List<List<String>> clauses, Map<String, Boolean> tryResult) {
		// find the first unassigned variable in the clauses
		for (List<String> clause : clauses) {
			for (String literal : clause) {
				String variable = literal.replace("~", "").trim();
				if (!tryResult.containsKey(variable)) {
					// if the variable is not assigned, return it
					return variable;
				}
			}
		}
		return null;
	}

	private static List<List<String>> simplify(List<List<String>> clauses, String variable, boolean value) {
		// value is the temporary assignment for the variable
		List<List<String>> simplified = new ArrayList<>();

		for (List<String> clause : clauses) {
			List<String> newClause = new ArrayList<>(); // the simplified clause for the current clause
			boolean satisfied = false;

			for (String literal : clause) {
				String var = literal.replace("~", "").trim();
				boolean isNegated = literal.startsWith("~");

				if (var.equals(variable)) {
					if ((isNegated && !value) || (!isNegated && value)) {
						// if the variable causes the clause to be true, skip the clause
						satisfied = true;
						break;
					}
				} else {
					// if there is nothing to do with the clause, add it to the new clause
					newClause.add(literal);
				}
			}
			if (!satisfied) {
				// if the clause is not satisfied, add it to the simplified clauses for the next recursion
				simplified.add(newClause);
			}
		}
		return simplified;
	}

	private static void printSolution(Map<String, Boolean> tryResult, List<String> variables) {
		// print the solution in one line
		System.out.print("satisfiable");
		// print the variable tryResults one by one
		for (String variable : variables) {
			System.out.printf(" %s=%s", variable, tryResult.get(variable) ? "T" : "F");
		}
	}
}
