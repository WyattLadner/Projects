# CSC 322
# Programming Assignment 3
#
# Wyatt Ladner
#
# This program will implement two algorithms
# using recursion and dynamic programming
# to return a desired fibonacci number to the user
#
# Fibonacci sequence:
# F(n) = F(n-1) + F(n-2)
# F(0), F(1) = 1
import time


def main():
    choice = int(input('Enter 1 for DP, enter 2 for recursive: '))
    n = int(input("Enter a fibonacci index: "))

    startTime = time.time()

    if choice == 1:
        fib = dp(n)
        print('DP')

    elif choice == 2:
        fib = recursive(n)
        print('Recursive')
    
    endTime = time.time()

    print('Fibonacci number requested is',fib)

    executionTime = (endTime - startTime) * 1000
    print(f"Execution Time: {executionTime:.3f} ms\n")
    return 0


def dp(n):

    #ensures valid input, first two fibonacci numbers are 1
    if n <= 0:
        return "Must be a positive number"
    elif n == 1 or n == 2:
        return 1
    

    #Dynamic algorithm
    prev = 1
    curr = 1
    
    for _ in range(3, n+1):
        temp = curr                 # temporary value saves current value
        curr = prev + temp          # current fibonaccie value is calculated: sum of previous two numbers
        prev = temp                 # previous is updated


    return curr



def recursive(n):

    #Ensures valid input
    if n <= 0:
        return "Input must be a positive integer"
    
    #Base case
    elif n == 1 or n == 2:
        return 1
    
    #Recursively calls with different n values
    return recursive(n - 1) + recursive(n - 2)



main()