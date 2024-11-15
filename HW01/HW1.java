
/*
A,B,C
~B,D
C,~A,F,E
~F
This means (A ∨ B ∨ C) ∧ ( ¬B ∨ D) ∧ (C ∨ ¬A ∨ F ∨ E) ∧ (¬F).
∧ is exactly 'and' in this context. ∨ means 'or'.

% cat /u/cs242/hw1/sat-problem1.txt
A,B
~B,C
% ./sat < /u/cs242/hw1/sat-problem1.txt
satisfiable A=T B=F C=T
% cat /u/cs242/hw1/sat-problem2.txt
A
~A
% ./sat < /u/cs242/hw1/sat-problem2.txt
unsatisfiable
*/

import java.util.*;

public class HW1 {

    private static boolean Solver(List<String> expressions) {
        // 储存所有布尔变量
        List<String> variables = extractVariables(expressions);
        // variables ["A", "B", "C"]
        int n = variables.size();
        int totalCombinations = (int) Math.pow(2, n); // 2^n 种组合
        boolean isSatif = false;

        // 逐个尝试每种可能的布尔组合
        // 0 = False, 1 = True
        // 2^2 = 4 种组合
        // ["A", "B"] totalCombinations = 4
        // i: 0 1 2 3
        // i: 00 01 10 11
        // ["A", "B", "C"] totalCombinations = 8
        // i: 000 001 010 011 100 101 110 111

        for (int i = 0; i < totalCombinations; i++) {
            Map<String, Boolean> Possible_Result = new HashMap<>();

            // 为每个布尔变量赋值
            for (int j = 0; j < n; j++) {
                // n 是元素的个数
                // j 是元素的编号
                // i 是 2^n 种组合的编号

                // i: 101
                // j = 0 i = True
                // j = 1 i = False
                // j = 2 i = True
                // 101 and 001 = 001
                // 101 and 010 = 000
                // 101 and 100 = 100
                Possible_Result.put(variables.get(j), (i & (1 << j)) != 0);
                //Possible_Result: {"A": True, "B": False, "C": True}
            }

            // 检查当前赋值是否满足所有表达式
            if (check_Satisfiable(Possible_Result, expressions)) {
                System.out.print("satisfiable ");
                for (Map.Entry<String, Boolean> E : Possible_Result.entrySet()) {
                    System.out.print(E.getKey() + "=" + (E.getValue() ? "T" : "F") + " ");
                }
                System.out.println();  // 换行
                isSatif = true;
//                return true; // 存在可满足的赋值
            }
        }

        return isSatif; // 无法找到可满足的赋值
    }

    // 提取所有布尔变量
    private static List<String> extractVariables(List<String> expressions) {
//        expressions  ["A,B" "~B,C"] -> variables ["A", "B", "C"]
        Set<String> variables = new HashSet<>();

        for (int i = 0; i < expressions.size(); i++) {
            // expr: "A,B" -> tokens: ["A", "B"]
            String expr = expressions.get(i);
            String[] tokens = expr.split(",");
            // tokens: ["A", "B"]
            // tokens: ["~B", "C"]

            for (int j = 0; j < tokens.length; j++) {
                String token = tokens[j];
                String variable = token.replace("~", "").trim(); // 移除否定符号
                variables.add(variable);
            }
        }

        return new ArrayList<>(variables); // 元素需要有编号
    }

    // 检查给定的布尔变量赋值是否满足表达式
    private static boolean check_Satisfiable(Map<String, Boolean> Possible_Result, List<String> expressions) {
        // expressions ["A,B" "~B,C"]
        // Possible_Result: {"A": True, "B": False, "C": True}
        for (int i = 0; i < expressions.size(); i++) {
            String expr = expressions.get(i);
            String[] t = expr.split(",");
            boolean result = false;

            // 检查每个子句
            // (A ∨ B ∨ C) ∧ ( ¬B ∨ D) ∧ (C ∨ ¬A ∨ F ∨ E) ∧ (¬F)
            // 只要(A ∨ B ∨ C)一个是True就满足

            for (int j = 0; j < t.length; j++) {
                String tk = t[j];
                boolean isNot = tk.contains("~");
                String var = tk.replace("~", "").trim();
                boolean val = Possible_Result.get(var);

                // 内层循环
                if (isNot) {
                    val = !val; // 取反
                }

                if (val) {
                    result = true;
                    break;
                }
            }

            // 外层循环
            // 任意子句不满足则整个表达式不满足
            if (!result) {
                return false;
            }
        }

        return true; // 所有子句都满足
    }


//    public static void main(String[] args) {
//        // 示例输入逻辑表达式
//        List<String> expressions = new ArrayList<>();
//        expressions.add("A,B");
//        expressions.add("~B,C");
//
//        // 调用求解函数
//        boolean result = Solver(expressions);
//        if (!result) {
//            System.out.println("unsatisfiable");
//        }


    public static void main(String[] args) {

        // 创建一个列表存储逻辑表达式
        List<String> expressions = new ArrayList<>();

        try {
            // 从标准输入读取（也可以直接读取文件）
            Scanner scanner = new Scanner(System.in);

            // 逐行读取输入的逻辑表达式
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine(); // 按行切分
                expressions.add(line);
            }

            // 关闭 Scanner
            scanner.close();

            // 调用求解函数
            boolean result = Solver(expressions);

            if (result) {
//                System.out.println("satisfiable");
            } else {
                System.out.println("unsatisfiable");
            }

        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}




