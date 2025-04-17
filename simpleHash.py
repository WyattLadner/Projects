# MA 481 Course Project
# 
# Dr. Joanna Furno
# 
# Wyatt Ladner
# J00769806
#
# This program will perform an implementation of a simple,
# custom hash function to show the process of hashing 
# algorithms.


def simpleHashAlgorithm(data: str, mod=2**16, prime = 31):

    # PARAMETERS

    # data: str represents the input string to be hashed

    # mod=2**16 represents the number of bits for the output: 16

    # It limits the output to prevent overflow and to keep the hash values
    # spread out evenly

    # Prime number 31 is chosen because:
    # - It distributes hash values more evenly, reducing collisions.
    # - It is computationally efficient for multiplication.
    # - It is commonly used in text hashing due to its properties.


    hashValue = 0   # base value of 0

    for char in data:   # iterates over each character


        hashValue = (hashValue * prime + ord(char)) % mod

        # Comment this section out when showcaseing BA
        #print(f"Current hash value: {hashValue}" )


        # ALGORITHM

        # ord(char) gets the ASCII value of each character

        # multiplying times prime spaces out the values

        # for the first iteration, the value will be whatever the ASCII is,
        # since hashValue starts at 0

        # % mod keeps the hash value within the fixed range of 2^32

    return hashValue


def main():
    while True:
        plaintext = input("\nEnter string to be hashed (or type 'exit' to quit): ").strip()
        if plaintext.lower() == 'exit':
            print("Exiting program.")
            break

        hash_value = simpleHashAlgorithm(plaintext)
        print(f"\nHash value for '{plaintext}' is: {hash_value}")


if __name__ == "__main__":
    main()
