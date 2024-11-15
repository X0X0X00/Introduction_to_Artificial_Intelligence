import java.util.*;

public class puzzle {

	// store possible directions
	static class Direction {
		int x, y;
		String name;
		Direction(int x, int y, String name) {
			this.x = x;
			this.y = y;
			this.name = name; // UP, DOWN, LEFT, RIGHT
		}
	}


	// store the board state
	static class Board {
		Direction[] directions = {
				new Direction(-1, 0, "UP"),
				new Direction(1, 0, "DOWN"),
				new Direction(0, -1, "LEFT"),
				new Direction(0, 1, "RIGHT")
		};
		int[][] board; // store the puzzle
		int n; // size of the puzzle
		int blank_x, blank_y; // store the blank tile
		List<Direction> path; // store the path

		Board(int[][] array) {
			// assign the array to the board
			n = array.length; // get the size of the array
			board = new int[n][n];
			for (int i = 0; i < n; i++) {
				System.arraycopy(array[i], 0, board[i], 0, n);
			}
			// find the blank tile
			for (int i = 0; i < n; i++) {
				for (int j = 0; j < n; j++) {
					if (board[i][j] == 0) {
						blank_x = i;
						blank_y = j;
						break;
					}
				}
			}
			path = new ArrayList<>(); // initialize the path
		}

		// ctor with array and path
		Board(int[][] array, List<Direction> newPath) {
			this(array);
			// assign path
			path.addAll(newPath);
		}

		boolean isGoal() {
			// final state: from 0 to n^2-1
			// 0 1 2
			// 3 4 5
			// 6 7 8
			for (int i = 0; i < n; i++) {
				for (int j = 0; j < n; j++) {
					if (i * n + j != board[i][j]) {
						return false;
					}
				}
			}
			return true;
		}

		// check the available directions in the current state
		List<Board> availDirs() {
			List<Board> nextStates = new ArrayList<>();
			for (Direction dir : directions) {
				int new_x = blank_x + dir.x;
				int new_y = blank_y + dir.y;
				if (new_x >= 0 && new_x < n && new_y >= 0 && new_y < n) {
					int[][] newBoard = deepCopy(board);
					// swap new_x, new_y with blank_x, blank_y
					newBoard[blank_x][blank_y] = newBoard[new_x][new_y];
					newBoard[new_x][new_y] = 0;

					// assign path
					// create new path
					List<Direction> newPath = new ArrayList<>(path);
					// add the direction
					newPath.add(dir);

					// create new board state
					Board newState = new Board(newBoard, newPath);
					nextStates.add(newState);
				}
			}
			return nextStates;
		}


		// copy the board
		int[][] deepCopy(int[][] board) {
			int[][] newBoard = new int[n][n];
			for (int i = 0; i < n; i++) {
				System.arraycopy(board[i], 0, newBoard[i], 0, n);
			}
			return newBoard;
		}


		// override equals
		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			Board other = (Board) o;
			return Arrays.deepEquals(board, other.board);
		}
		// override for hashCode
		@Override
		public int hashCode() {
			return Arrays.deepHashCode(board);
		}

	}

	// BFS Search Algorithm
	public static List<Direction> bfs(Board start) {
//		System.out.println("BFS");
		// store the visited states
		Set<Board> visited = new HashSet<>();
		// use the queue to store the states, fringe
		Queue<Board> queue = new LinkedList<>();
		queue.add(start);
		visited.add(start);

		int expanded = 0;
		while (!queue.isEmpty()) {
			Board current = queue.poll(); // get the first element in the queue
			expanded++;
			if (current.isGoal()) {
				System.out.println("Expanded in bfs: " + expanded);
				return current.path;
			}

			for (Board nextState : current.availDirs()) {
				if (!visited.contains(nextState)) {
					visited.add(nextState);
					queue.add(nextState);
				}
			}
		}
		return new ArrayList<>();
	}

	// A* Search Algorithm
	public static List<Direction> astar(Board start) {
		// use priority queue to store the states, fringe
		PriorityQueue<Board> open = new PriorityQueue<>(Comparator.comparingInt(puzzle::f_n));
		// close table: store the visited states and their minimum g_n, this is an advanced version of visited set
		Map<Board, Integer> close = new HashMap<>();
		open.add(start);
		close.put(start, 0);
		int expanded = 0;

		while (!open.isEmpty()) {
			Board current = open.poll();
			expanded++;
			if (current.isGoal()) {
				System.out.println("Expanded in astar: " + expanded);
				return current.path;
			}

			for (Board nextState : current.availDirs()) {
				// calculate the g_n for the next state
				int g_n = current.path.size() + 1;
				// if the next state is not in the close table or the new g_n is smaller than the previous g_n
				if (!close.containsKey(nextState) || g_n < close.get(nextState)) {
					close.put(nextState, g_n);
					open.add(nextState);
				}
			}
		}

		return new ArrayList<>();
	}


	public static int f_n(Board b) {
		// g_n is the cost to reach the current state
		int g_n = b.path.size();
		// use Manhattan distance to calculate the heuristic
		// h_n is the heuristic
		int h_n = 0;
		for (int i = 0; i < b.n; i++) {
			for (int j = 0; j < b.n; j++) {
				int value = b.board[i][j];
				if (value != 0) {
					// for example, value = 5, n = 3 => target_x = 1, target_y = 2
					int target_x = value / b.n; // 1
					int target_y = value % b.n; // 2
					// calculate the Manhattan distance
					h_n += Math.abs(i - target_x) + Math.abs(j - target_y);
				}
			}
		}
		// calculate f_n = g_n + h_n
		return g_n + h_n;
	}

	public static void main(String[] args) {
		Scanner scanner = new Scanner(System.in);
		int n = scanner.nextInt();
		int[][] array = new int[n][n];
		// read in the array matrix
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < n; j++) {
				array[i][j] = scanner.nextInt();// read in the int
			}
		}
		Board b = new Board(array);
		// get the command line argument
		String command = "";
		if (args.length > 0) {
			command = args[0];
		}
		else {
			// by default, use BFS
//			command = "--bfs";
			command = "--astar";
		}
		// the result is stored in the result list with the directions
		List<Direction> result = new ArrayList<>();
		if (command.equals("--bfs")) {
			result = bfs(b);
		} else if (command.equals("--astar")) {
			result = astar(b);
		}
		// print the result
		if (result.isEmpty()) {
			System.out.println("No solution");
		} else {
			for (Direction dir : result) {
				System.out.println(dir.name);
			}
		}

	}
}
