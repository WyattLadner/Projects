# PA 6
#
# Wyatt Ladner
#
# Ask the user to specify two sequences along with the scoring matrix (match, mismatch, and gap).
# Output the chart of DP calculation. Mark in the chart trace-back paths for all optimal alignment.
# Output all optimal alignments along with their respective alignment scores.



# Initialize the DP matrix with gap penalties
def initialize_matrix(seq1, seq2, gap_penalty):

    rows, cols = len(seq2) + 1, len(seq1) + 1

    # Create a 2D list (list of lists) initialized with zeros
    dp = [[0] * cols for _ in range(rows)]

    # Fill the first row and column with gap penalties
    for i in range(rows):
        dp[i][0] = i * gap_penalty
    for j in range(cols):
        dp[0][j] = j * gap_penalty
    return dp



# Function to fill the DP matrix based on optimal scores
def fill_matrix(dp, seq1, seq2, match_score, mismatch_penalty, gap_penalty):

    for i in range(1, len(seq2) + 1):
        for j in range(1, len(seq1) + 1):

            # Compute match/mismatch, deletion, and insertion scores
            match = dp[i-1][j-1] + (match_score if seq1[j-1] == seq2[i-1] else mismatch_penalty)
            delete = dp[i-1][j] + gap_penalty
            insert = dp[i][j-1] + gap_penalty

            # Assign the maximum value to the current cell
            dp[i][j] = max(match, delete, insert)

    return dp



# Function to trace back through the DP matrix and find all optimal alignments
def traceback(dp, seq1, seq2, match_score, mismatch_penalty, gap_penalty, i, j, alignment1='', alignment2=''):

    # Base case: when both sequences are fully aligned
    if i == 0 and j == 0:
        return [(alignment1[::-1], alignment2[::-1])]
    alignments = []

    # Trace back for gaps (vertical and horizontal moves)
    if i > 0 and dp[i][j] == dp[i-1][j] + gap_penalty:
        alignments.extend(traceback(dp, seq1, seq2, match_score, mismatch_penalty, gap_penalty, i-1, j, alignment1 + '-', alignment2 + seq2[i-1]))
    if j > 0 and dp[i][j] == dp[i][j-1] + gap_penalty:
        alignments.extend(traceback(dp, seq1, seq2, match_score, mismatch_penalty, gap_penalty, i, j-1, alignment1 + seq1[j-1], alignment2 + '-'))
    
    # Trace back for matches/mismatches (diagonal move)
    if i > 0 and j > 0 and dp[i][j] == dp[i-1][j-1] + (match_score if seq1[j-1] == seq2[i-1] else mismatch_penalty):
        alignments.extend(traceback(dp, seq1, seq2, match_score, mismatch_penalty, gap_penalty, i-1, j-1, alignment1 + seq1[j-1], alignment2 + seq2[i-1]))
    
    return alignments



# Main function to get user input, compute DP matrix, and display results
def main():

    # Get sequences and scoring parameters from the user
    seq1 = input("Enter first sequence: ")
    seq2 = input("Enter second sequence: ")
    match_score = int(input("Enter match score: "))
    mismatch_penalty = int(input("Enter mismatch penalty: "))
    gap_penalty = int(input("Enter gap penalty: "))
    
    # Initialize and fill the DP matrix
    dp = initialize_matrix(seq1, seq2, gap_penalty)
    dp = fill_matrix(dp, seq1, seq2, match_score, mismatch_penalty, gap_penalty)
    
    # Display the DP matrix
    print("\nDP Matrix:")
    for row in dp:
        print(row)
    
    # Perform traceback to find all optimal alignments
    alignments = traceback(dp, seq1, seq2, match_score, mismatch_penalty, gap_penalty, len(seq2), len(seq1))
    max_score = dp[-1][-1]
    
    # Display all optimal alignments and their scores
    print("\nOptimal Alignments:")
    for a1, a2 in alignments:
        print(f"{a1}\n{a2}\nScore: {max_score}\n")

main()