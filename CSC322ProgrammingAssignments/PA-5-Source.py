import time
import itertools

class Task:
    def __init__(self, task_id, value, start, end):
        self.task_id = task_id
        self.value = value
        self.start = start
        self.end = end

    def __repr__(self):
        return f"Task_{self.task_id}(Value: {self.value}, Start: {self.start}, End: {self.end})"


# 1. Get User input
n = int(input("Enter the total number of paid tasks: "))

tasks = []
for i in range(n):
    value = int(input(f"Enter the value for Task_{i + 1}: "))
    start = int(input(f"Enter the start time for Task_{i + 1}: "))
    end = int(input(f"Enter the end time for Task_{i + 1}: "))
    tasks.append(Task(i + 1, value, start, end))


# 2. Output tasks sorted by end time
tasks.sort(key=lambda x: x.end)
print("\n\nSorted Tasks by End Time:")
for task in tasks:
    print(task)


# 3. Brute-Force Algorithm
def brute_force(tasks):
    best_sets = []
    max_value = 0
    start_time = time.time()


    # 
    for i in range(1, len(tasks) + 1):

        for combination in itertools.combinations(tasks, i):
            valid = True
            total_value = sum(task.value for task in combination)

            for i in range(len(combination)):

                for j in range(i + 1, len(combination)):
                    
                    # Checks for overlap
                    if combination[i].end > combination[j].start:
                        valid = False
                        break

                if not valid:
                    break

            # Updates value if new best path is found
            if valid and total_value > max_value:
                max_value = total_value
                best_sets = [combination]

            # Adds to the best set list if different paths get same high value
            elif valid and total_value == max_value:
                best_sets.append(combination)

    end_time = time.time()
    print(f"\n\nBrute-Force Algorithm time elapsed: {end_time - start_time:.6f} seconds")

    print(f"Max Earnings: {max_value}")
    for task_set in best_sets:
        print(" -> ".join(str(task) for task in task_set), f"with a total earning of {max_value}")
        

# 4. Recursive DP Algorithm
def recursive_dp(tasks):

    def helper(i):

        if i == 0:
            return tasks[0].value, [tasks[0]]
        
        else:
            incl_value, incl_tasks = tasks[i].value, [tasks[i]]

            for j in range(i - 1, -1, -1):

                if tasks[j].end <= tasks[i].start:
                    incl_value, incl_tasks = max(incl_value + helper(j)[0], helper(i - 1)[0]), incl_tasks + helper(j)[1]
                    break

            return incl_value, incl_tasks
        
    start_time = time.time()
    max_value, task_set = helper(len(tasks) - 1)
    end_time = time.time()
    
    print(f"\n\nRecursive DP Algorithm time elapsed: {end_time - start_time:.6f} seconds")
    print(f"Max Earnings: {max_value}")
    print(" -> ".join(str(task) for task in task_set), f"with a total earning of {max_value}")
    

# 5. Non-Recursive DP Algorithm
def non_recursive_dp(tasks):

    dp = [0] * len(tasks)
    prev = [-1] * len(tasks)

    for i in range(len(tasks)):
        dp[i] = tasks[i].value

        for j in range(i):
            # Checks for overlap
            if tasks[j].end <= tasks[i].start:
                dp[i] = max(dp[i], tasks[i].value + dp[j])
                prev[i] = j

    max_value = max(dp)
    max_index = dp.index(max_value)

    task_set = []
    while max_index != -1:
        task_set.append(tasks[max_index])
        max_index = prev[max_index]
    
    start_time = time.time()
    end_time = time.time()

    print(f"\n\nNon-Recursive DP Algorithm time elapsed: {end_time - start_time:.6f} seconds")
    print(f"Max Earnings: {max_value}")
    print(" -> ".join(str(task) for task in reversed(task_set)), f"with a total earning of {max_value}")


# 6. Max Legitimate Sets of Tasks Algorithm
def max_legitimate_sets(tasks):
    task_sets = []
    for i in range(1, len(tasks) + 1):
        for combination in itertools.combinations(tasks, i):
            valid = True
            total_value = sum(task.value for task in combination)
            for i in range(len(combination)):
                for j in range(i + 1, len(combination)):
                    if combination[i].end > combination[j].start:
                        valid = False
                        break
                if not valid:
                    break
            if valid:
                task_sets.append((combination, total_value))
    
    task_sets.sort(key=lambda x: x[1], reverse=True)

    print(f"\n\nThere are {len(task_sets)} options to select different sets of tasks.")
    for idx, (task_set, value) in enumerate(task_sets):
        print(f"Option {idx + 1}: " + " -> ".join(str(task) for task in task_set), f"with a total earning of {value}")

# 7. Run all algorithms
brute_force(tasks)
recursive_dp(tasks)
non_recursive_dp(tasks)
max_legitimate_sets(tasks)
