# MA 481 Course Project
#
# Dr. Joanna Furno
#
# Wyatt Ladner
# J00769806
#
# This program will perform a birthday attack
# on the simple custom hash function defined
# in simpleHash.py

import random
import string
import statistics
from openpyxl import Workbook
from openpyxl.chart import LineChart, Reference
from simpleHash import simpleHashAlgorithm


# TO DO
# Edit program so that different string lengths are tested 100 times.
# - Data is kept track of
# - Spreadsheet is created:
#       - Average tries for each input string length
#       - Fastest and slowest collision speed


# Generates random string of 6 lowercase letters
def randomString(length):
    return ''.join(random.choices(string.ascii_lowercase, k=length))
    # 6 letters are used because it gives the highest probability of finding a collision
    # that is non-trivial
    # using 6 letters, there are 26^6 possible combinations


def birthdayAttack(simpleHashAlgorithm, length):


    maxAttempts=2**20


    seenHashes = {}
    testedInputs = set()
    # Keeps track of inputs, ensuring uniqueness, and the resulting hashes


    counter = 0

    for _ in range(maxAttempts):
        

        # Generates a random string and adds to the set
        while True:
            data = randomString(length)
            if data not in testedInputs:
                testedInputs.add(data)
                break

        
        # Uses custom hash algorithm to find the hash of the generated string
        hash = simpleHashAlgorithm(data)
        #print(f"\n\nInput: {data}\nHash: {hash}")
        counter +=1 


        # Checks to see if this resulting hash is already in the dictionary
        if hash in seenHashes:
            #print(f"\n\nCollision found!\nInput 1: {seenHashes[hash]}\nInput 2: {data}\nHash: {hash}")
            #print(f"Number of inputs needed to find collision: {counter}")
            #return data, seenHashes[hash]
            return counter
        
        seenHashes[hash] = data

    
    #print('No collisions found in max allowed attempts.')
    return None, None

def main():
    results = {}  # key = string length, value = list of 100 attempt counts
    lengths = range(4, 16)

    print("Running birthday attacks...")

    for length in lengths:
        attempts_list = []

        for _ in range(100):
            attempts = birthdayAttack(simpleHashAlgorithm, length)
            attempts_list.append(attempts)

        results[length] = attempts_list
        print(f"Length {length}: complete")

    # Create workbook and worksheet
    wb = Workbook()
    ws = wb.active
    ws.title = "Birthday Attack Results"

    # Write header
    ws.append(["String Length", "Average Attempts", "Min Attempts", "Max Attempts"])

    # Write summary data
    for length in lengths:
        avg = statistics.mean(results[length])
        min_val = min(results[length])
        max_val = max(results[length])
        ws.append([length, avg, min_val, max_val])

    # Create a chart
    chart = LineChart()
    chart.title = "Birthday Attack: Avg Attempts vs String Length"
    chart.x_axis.title = "String Length"
    chart.y_axis.title = "Average Attempts"

    data = Reference(ws, min_col=2, min_row=1, max_row=len(lengths) + 1)  # B1 to B12
    cats = Reference(ws, min_col=1, min_row=2, max_row=len(lengths) + 1)  # A2 to A12

    chart.add_data(data, titles_from_data=True)
    chart.set_categories(cats)

    ws.add_chart(chart, "F2")

    # Save file
    wb.save("birthday_attack_results.xlsx")
    print("Saved results to 'birthday_attack_results.xlsx'")
    
if __name__ == "__main__":
    main()

