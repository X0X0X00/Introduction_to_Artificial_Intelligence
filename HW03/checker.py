#!/usr/bin/python3

import subprocess
import os
import glob
import sys
import glob

'''
# CHECKER script for HW3.

    Usage:
        ./checker <PATH_TO_SUBMISSION_FOLDER>

    [Key addition] Assignment Satisfiability Evaluation:
        * Assumption: Complete assignments must be provided (for all atoms in CNF). 
            ** In case a given atom is not assigned in the assignment, it is not considered in the evaluation of the clause.

    Test cases: 
        * Read from PATH_TEST_CASES path. All test files must be named {idx}, where idx is the test case number.
        * TEST_CASE_ANSWER_KEYS contains the expected output for each test case idx.
            - Update TEST_CASE_ANSWER_KEYS for a different set of test cases.

    Approach:
        1. Searches for executables in all possible extensions (executables or programs with .py/.java/.class files)
        2. Sets commands accordingly (./sat < test_case | .py: python3 sat.py < test_case | java: java sat < test_case)
        3. Iterate over all test cases. Calculate score.
'''

PATH_TEST_CASES = "./inputs/*"
TEST_CASE_ANSWER_KEYS = {1: 'unsatisfiable', 2: 'unsatisfiable', 3: 'unsatisfiable', 4: 'unsatisfiable', 5: 'unsatisfiable', 6: 'unsatisfiable',
                         7: 'satisfiable', 8: 'unsatisfiable', 9: 'unsatisfiable', 10: 'unsatisfiable', 11: 'satisfiable', 12: 'satisfiable',
                         13: 'unsatisfiable', 14: 'satisfiable', 15: 'satisfiable', 16: 'satisfiable', 17: 'unsatisfiable', 18: 'satisfiable',
                         19: 'satisfiable', 20: 'unsatisfiable'}

heuristics = True

def evaluate_assignment(clauses, assignment):
    #print(f"assignment: {assignment}")
    for i, clause in enumerate(clauses):
        clause_satisfied = False
        for literal in clause:
            if literal.startswith('~'):
                # If the literal is a negated literal, the clause is satisfied if corresponding atom is assigned False.
                if assignment.get(literal[1:]) == 'F':
                    clause_satisfied = True
                    break
            else:
                # If literal is non-negated, the clause is satisfied if atom is assigned True.
                if assignment.get(literal) == 'T':
                    clause_satisfied = True
                    break
            # [Per Assumption] If a literal is not in assignment, we skip it, keeping the clause's current satisfaction state.

        if not clause_satisfied:
            print(f"Clause {i} not satisfied by given assignment: {clause}")
            return False
    return True


def read_test_case(test_case_file):
    with open(test_case_file, 'r') as file:
        clauses = [line.strip().split(',') for line in file.readlines()]
    return clauses

def check_sat_solver(folder_path, executable_name, path_test_cases = PATH_TEST_CASES, test_case_answer_keys = TEST_CASE_ANSWER_KEYS):
    test_case_files = [os.path.abspath(file) for file in glob.glob(path_test_cases)]
    os.chdir(folder_path)
    print(f"\nSubmission path: {folder_path}")

    executable = None

    # Print readme if exists
    # for readme in glob.glob('README*'):
    #     if readme.lower().startswith('readme'):
    #         with open(readme, 'r') as file:
    #             print(f"{file.read()}")

    for filename in glob.glob(f"{executable_name}*"):
        if filename.lower() == f"{executable_name}.py" or filename.lower() == f"{executable_name}.java"\
            or filename.lower() == f"{executable_name}.class" or filename.lower() == executable_name:
            executable = filename
            break
    if not executable:
        print(f"No executable found with base_name: '{executable_name}'")
        return
    else:
        #print(f"\tFound executable file: '{executable}'")
        pass
    if executable.endswith('.py'):
        command = ['python3', executable]
    elif executable.endswith('.java'):
        subprocess.run(['javac', executable])
        command = ['java', executable_name]
    elif executable.endswith('.class'):
        command = ['java', executable_name]
    else: # No extension (our expected `script` case)
        os.chmod(executable, 0o755) # equivalent to 'chmod +x executable', as a precaution incase permissions aren't set. 
        command = [f"./{executable}"]

    if not heuristics:
        command += ['--nounit', '--nopure'] # Disable heuristics for consistent testing

    score = 0
    count = 0
    if len(test_case_files) == 0:
        print(f"No test cases found in: {path_test_cases}")
        return
    for test_case_file in test_case_files:
        test_id, test_case = os.path.basename(test_case_file), read_test_case(test_case_file)
        expected_output = test_case_answer_keys.get(int(test_id))
        # if int(test_id) > 18:
        #     continue
        process = subprocess.Popen(command, stdin=open(test_case_file, 'r'), stdout=subprocess.PIPE, stderr=subprocess.PIPE)
        stdout, stderr = process.communicate()
        result = stdout.decode().strip()
        #print(f'{test_case_file}: \nresult: ', result)
        
        if expected_output == 'unsatisfiable':
            if result == 'unsatisfiable':
                score += 1
                print(f'Correct unsatisfiability for test case: {test_id}')
            else:
                print(f"Incorrectly marked as satisfiable: {test_id}")
        
        elif expected_output == 'satisfiable':
            if result.startswith('satisfiable'):
                if result == 'satisfiable':
                    assignment = {}
                    unique_atoms = set()
#        for clause in test_case:
#            for literal in clause:
#                atom = literal.strip('~')
#                unique_atoms.add(atom)
                else:
                    assignment = {literal.split('=')[0]: literal.split('=')[1] for literal in result.replace("satisfiable", "").strip().split(' ')}
                if evaluate_assignment(test_case, assignment):
                    score += 1
                    print(f'Correct satisfiability and correct assignment for test case: {test_id}')
                else:
                    print(f"Correctly marked as satisfiable. But, Incorrect assignment. Test case: {test_id}")
            else:
                print(f"Incorrectly marked as unsatisfiable: {test_id}")
        print('\n')
    
    print(f"\nTest cases passed: {score}/{len(test_case_files)} | Score: {float(score)/float(len(test_case_files))*100.0}%\n")

if __name__ == "__main__":
    print("IN")
    if len(sys.argv) != 2 and len(sys.argv) != 3:
        print("Usage: ./checker <PATH_TO_SUBMISSION_FOLDER> OR ./checker <PATH_TO_SUBMISSION_FOLDER> none")
        sys.exit(1)
        print("IN")
    folder_path = sys.argv[1]
    if len(sys.argv) == 3 and sys.argv[2] == 'none':
        heuristics = False
        print("Heurstics Turned Off\n")


    check_sat_solver(folder_path,'sat')

    # To check with known test cases (6 test cases verified manually), uncomment below:
    #SIMPLE_PATH_TEST_CASES = "simple_inputs/*"
    #SIMPLE_TEST_CASE_ANSWER_KEYS = {1: 'unsatisfiable', 2: 'unsatisfiable', 3: 'unsatisfiable', 4: 'satisfiable', 5: 'satisfiable', 6: 'satisfiable'}
   # check_sat_solver(folder_path, 'sat', SIMPLE_PATH_TEST_CASES, SIMPLE_TEST_CASE_ANSWER_KEYS)
    

