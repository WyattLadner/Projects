"""
CSC 332 PA2
Wyatt Ladner


This python program will randomly populate 9 arrays, and then perform
merge sort algorithms. User will be able to choose from which array
output is desired, and all data will be printed into generated
excel spreadsheets.
"""

import random                   # For generating random integers
import time                     # For measuring execution time
import openpyxl                 # For saving Excel files
import math                     # For calculating logrithms

def main():
    print("Program started")
    arrays = {                  #Creates array of arrays for easy
        "array1": [0] * 1000,       #population and sorting
        "array2": [0] * 2000,
        "array3": [0] * 3000,
        "array4": [0] * 4000,
        "array5": [0] * 5000,
        "array6": [0] * 6000,
        "array7": [0] * 7000,
        "array8": [0] * 8000,
        "array9": [0] * 9000
    }

    sortedArrays = []                            #Array to store sorted arrays
    results = []                                 #Array to store spreadsheet data in

    for key in arrays:                           #Sorts arrays, tracks time, inserts into spreadsheet
        populateArray(arrays[key])               #Populates arrays with random integers
        n =len(arrays[key])
        startTime = time.perf_counter_ns()
        sortedArray = mergeSort(arrays[key])
        endTime = time.perf_counter_ns()
        sortedArrays.append(sortedArray)

        timeElapsed = endTime - startTime
        data = compute_metrics(n, timeElapsed)
        results.append((data))

    createSpreadsheet(results)

    while True:
        choice = input("Enter the array number (1-9) you want to view, or any other key to quit: ")

        if choice.isdigit():  # Check if it's a valid number
            choice = int(choice)
            if 1 <= choice <= 9:
                key = f"array{choice}"
                print(f"Unsorted array: {arrays[key]}")
                print(f"Sorted array: {sortedArrays[choice-1]}")
            else:
                print("Invalid number. Please enter a number between 1 and 9.")
        else:
            print("Program complete.")
            break


    return 1


#Computes n log(n) and (n log(n)) / time, formatting the result in scientific notation
def compute_metrics(n, time_ns):

    n_log_n = n * math.log2(n)                       # Calculate n log(n)
    ratio = n_log_n / time_ns if time_ns > 0 else 0  # Avoid division by zero

    sci_notation = "{:.2E}".format(ratio)               # Convert to scientific notation
                                                        # (rounded integer x * 10^y)
    return [n, n_log_n, time_ns, sci_notation]



#Populates arrays randomly
def populateArray(array):
    size = len(array)
    for i in range(size):
        array[i] = random.randint(1, size)



#Merge sort functions

#Splits arrays in half down to single integers,
#then reconstructs into halves
def mergeSort(array):
    if len(array) <= 1:
        return array

    mid = len(array) // 2                 #Finds midpoint of array
    leftHalf = mergeSort(array[:mid])     #Recursively sorts left half
    rightHalf = mergeSort(array[mid:])    #Sorts right half

    return merge(leftHalf, rightHalf)

#Takes sorted halves and merges them together
def merge(left, right):
    sortedArray = []
    i = j = 0

    #Merge sorted halves
    while i < len(left) and j < len(right):
        if left[i] < right[j]:
            sortedArray.append(left[i])
            i+=1
        else:
            sortedArray.append(right[j])
            j+=1

    #Add any leftover elements
    sortedArray.extend(left[i:])
    sortedArray.extend(right[j:])

    return sortedArray



#Creates spreadsheet and inputs data from array
def createSpreadsheet(data, filename = "Mergesort_Time.xlsx"):

    #Create spreadsheet
    workbook = openpyxl.Workbook()
    sheet = workbook.active
    sheet.title = "Merge Sort Results"

    #Add headers
    headers = ["Input Size (n)", "n log(n)", "Time (ns)", "(n log(n)) / Time"]
    sheet.append(headers)

    #Add data to rows
    for row in data:
        sheet.append(row)

    workbook.save(filename)
    print(f"Excel file '{filename}' generated successfully!")

main()















