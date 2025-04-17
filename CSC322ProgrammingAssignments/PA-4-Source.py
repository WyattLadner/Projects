# PA4.py
#
# Programming Assignment 4
# Wyatt Ladner
#
# This program will represent the 
# staircase problem for dynamic programming.
#
# User inputs number of stairs, and the program
# will use recursive and non-recursive functions
# to find the number of routes, tracking calculation time

import time

def main():
    
    go = True
    while go:

        #Get number of steps and verify
        n = int(input('Enter number of levels in staircase: '))
        steps = ensureValidInput(n)

        #Recursive solution
        startTime = time.time()
        recursiveWays, recursivePaths = recursiveStaircase(n, steps)
        recursiveTime = (time.time()-startTime) * 1000
        print(f"\n\tRecursive Data")
        print(f"Time elapsed: {recursiveTime:.3f}ms")
        print(f"Number of paths: {recursiveWays}")

        #Dynamic solution
        startTime = time.time()
        iterativeWays, iterativePaths = nonRecursiveStaircase(n, steps)
        iterativeTime = (time.time()-startTime) * 1000
        print(f"\n\tIterative Data")
        print(f"Time elapsed: {iterativeTime:.3f}ms")
        print(f"Number of ways: {iterativeWays}\n")

        for i, path in enumerate(iterativePaths, 1):
            print(f"Way {i}: {' -> '.join(map(str, path))}")

        again = input('Run again? Y/N ')
        if again != 'Y' and again != 'y':
            go = False

    return 0

def ensureValidInput(n):

    while True:
        try:
            #Separates users steps at the comma
            stepSizes = set(map(int, input(f"Enter allowed steps as comma-separated values: ").split(',')))

            #Ensures the user enters 2 or more unique step sizes
            if len(stepSizes) < 2 or any(step < 1 or step > n for step in stepSizes):
                raise ValueError
            return sorted(stepSizes)
        except ValueError:
            print(f"Invalid input")


#Recursively calculates the number of paths possible
def recursiveStaircase(n, steps, path=[]):

    if n == 0:
        return 1, [path]
    
    if n < 0:
        return 0, []
    
    totalWays = 0
    allPaths = []

    for step in steps:
        ways, paths = recursiveStaircase(n - step, steps, path + [step])
        totalWays += ways
        allPaths.extend(paths)

    return totalWays, allPaths



def nonRecursiveStaircase(n, steps):

    iterative = [0] * (n+1)
    iterative[0] = 1

    for i in range (1, n+1):
        for step in steps:
            if i - step >= 0:
                iterative[i] += iterative[i - step]

    paths = [[] for _ in range(n+1)]
    paths[0] = [[]]

    for i in range(1, n + 1):
        for step in steps:
            if i - step >= 0:
                for path in paths[i - step]:
                    paths[i].append(path + [step])


    return iterative[n], paths[n]

main()