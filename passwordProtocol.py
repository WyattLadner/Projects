# MA 481 Course Project
#
# Dr. Joanna Furno
#
# Wyatt Ladner
# J00769806
#
# This program will demonstrate password protocol using hash functions.
# The user will input a password and the process of hashing
# the password will be displayed. 

import random
import string
import struct

# SHA-256 Constants
K = [
    0x428a2f98, 0x71374491, 0xb5c0fbcf, 0xe9b5dba5, 0x3956c25b, 0x59f111f1, 0x923f82a4, 0xab1c5ed5,
    0xd807aa98, 0x12835b01, 0x243185be, 0x550c7dc3, 0x72be5d74, 0x80deb1fe, 0x9bdc06a7, 0xc19bf174, 0xe49b69c1,
    0xefbe4786, 0x0fc19dc6, 0x240ca1cc, 0x2de92c6f, 0x4a7484aa, 0x5cb0a9dc, 0x76f988da, 0x983e5152, 0xa831c66d,
    0xb00327c8, 0xbf597fc7, 0xc6e00bf3, 0xd5a79147, 0x06ca6351, 0x14292967, 0x27b70a85, 0x2e1b2138, 0x4d2c6dfc,
    0x53380d13, 0x650a7354, 0x766a0abb, 0x81c2c92e, 0x92722c85, 0xa2bfe8a1, 0xa81a664b, 0xc24b8b70, 0xc76c51a3,
    0xd192e819, 0xd6990624, 0xf40e3585, 0x106aa070, 0x19a4c116, 0x1e376c08, 0x2748774c, 0x34b0bcb5, 0x391c0cb3,
    0x4ed8aa4a, 0x5b9cca4f, 0x682e6ff3, 0x748f82ee, 0x78a5636f, 0x84c87814, 0x8cc70208, 0x90befffa, 0xa4506ceb,
    0xbef9a3f7, 0xc67178f2
]

# Initial Hash Values
H = [
    0x6a09e667, 0xbb67ae85, 0x3c6ef372, 0xa54ff53a, 
    0x510e527f, 0x9b05688c, 0x1f83d9ab, 0x5be0cd19
]


def validatePassword(password):

    """
    Validates that the user's password meets certain requirements:
    - 7+ characters long
    - 1+ number
    - 1+ special character
    - 1+ uppercase letter

    """

    if len(password) < 7:
        return False
    if not any(char.isupper() for char in password):
        return False
    if not any(char.isdigit() for char in password):
        return False
    if not any(char in string.punctuation for char in password):
        return False
    return True



def generateSalt():

    """
    Generates a random string to add onto the password before hashing.
    This is called "salting"

    """

    salt = ''.join(random.choices(string.ascii_letters + string.digits, k=8))
    # randomly selects 8 characters from the list of all lowercase, uppercase, and 9 digits
    # abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789

    return salt




# SHA-256 functions
def right_rotate(value, shift, bits=32):

    """
    Performs circular right rotation of a 32 bit input

    Ensures that bits that are shifted right are wrapped
    to the beginning of the sequence
    
    """

    return (value >> shift) | (value << (bits - shift)) & 0xFFFFFFFF



def sha256_math(password):

    """
    SHA256 converts any input into a fixed size hash of 256 bits, or a 64-hex.
    How it works:
    - Padding
        - Extends the input into a size that is a multiple of 512
    - Expansion
        - 512 bit message is broken into 16 words of 32 bits, which are then all
        expanded into 64 bits.
    - Compression rounds
        - The words are processed through a series of bitwise operations, shifts, 
        and modular additions using eight fixed hash values and constants.
    - Final hashing
        - The results of the rounds update the hash and form the final 256 bit output
        at the end
    
    """



    print("\nOriginal password:", password)
    #print("Generated salt:", salt)



    # Step 1: Append salt to password
    saltedPassword = password #+ salt
    #print("\nSalted password:", saltedPassword)



    # Step 2: Convert to binary representation
    binaryInput = ''.join(format(ord(c), '08b') for c in saltedPassword)
    print("\nBinary representation of input:\n", binaryInput)



    # Step 3: Padding the input to 512-bit block
    originalLength = len(binaryInput)
    binaryInput += '1'  # Append a single 1-bit
    while len(binaryInput) % 512 != 448:
        binaryInput += '0'

    lengthBits = format(originalLength, '064b')  
    binaryInput += lengthBits
    print("\nPadded input (512 bits):\n", binaryInput)



    # Step 4: Break into 16 32-bit words
    words = [int(binaryInput[i:i+32], 2) for i in range(0, 512, 32)]
    print("\nInitial 16 words (in hex):")
    for i, word in enumerate(words):
        print(f"Word {i}: {hex(word)}")



    # Step 5: Extend to 64 words
    for i in range(16, 64):
        s0 = right_rotate(words[i-15], 7) ^ right_rotate(words[i-15], 18) ^ (words[i-15] >> 3)
        s1 = right_rotate(words[i-2], 17) ^ right_rotate(words[i-2], 19) ^ (words[i-2] >> 10)
        newWord = (words[i-16] + s0 + words[i-7] + s1) & 0xFFFFFFFF
        words.append(newWord)

    print("\nExtended 64 words (in hex):")
    for i in range(64):
        print(f"Word {i}: {hex(words[i])}")



    # Step 6: Initialize hash values
    a, b, c, d, e, f, g, h = H



    # Step 7: Main loop (64 rounds)
    for i in range(64):

        # Compute the SHA-256 round functions:
        
        # S1 is the sum of three right rotations on 'e'
        S1 = right_rotate(e, 6) ^ right_rotate(e, 11) ^ right_rotate(e, 25)
        
        # 'ch' (choose function) selects bits from f or g based on e
        ch = (e & f) ^ (~e & g)

        # Compute the temporary value temp1
        temp1 = (h + S1 + ch + K[i] + words[i]) & 0xFFFFFFFF

        # S0 is the sum of three right rotations on 'a'
        S0 = right_rotate(a, 2) ^ right_rotate(a, 13) ^ right_rotate(a, 22)

        # 'maj' (majority function) chooses the majority bit from a, b, and c
        maj = (a & b) ^ (a & c) ^ (b & c)

        # Compute the second temporary value temp2
        temp2 = (S0 + maj) & 0xFFFFFFFF

        # Update the working variables for the next round:
        
        # Shift values downward, h takes the value of g, g takes f, etc.
        h = g
        g = f
        f = e
        
        # 'e' gets updated with d + temp1
        e = (d + temp1) & 0xFFFFFFFF

        d = c
        c = b
        b = a

        # 'a' gets updated with temp1 + temp2
        a = (temp1 + temp2) & 0xFFFFFFFF

        # Print the updated state after this round
        print(f"\nRound {i+1}:")
        print(f"a: {hex(a)}, b: {hex(b)}, c: {hex(c)}, d: {hex(d)}, e: {hex(e)}, f: {hex(f)}, g: {hex(g)}, h: {hex(h)}")




    # Step 8: Compute final hash
    finalHash = [(H[i] + x) & 0xFFFFFFFF for i, x in enumerate([a, b, c, d, e, f, g, h])]
    hashHex = ''.join(format(x, '08x') for x in finalHash)

    return hashHex

    


# Copied without print statements for login
# SHA-256 functions

def sha256_math_login(password):

    """
    SHA256 converts any input into a fixed size hash of 256 bits, or a 64-hex.
    How it works:
    - Padding
        - Extends the input into a size that is a multiple of 512
    - Expansion
        - 512 bit message is broken into 16 words of 32 bits, which are then all
        expanded into 64 bits.
    - Compression rounds
        - The words are processed through a series of bitwise operations, shifts, 
        and modular additions using eight fixed hash values and constants.
    - Final hashing
        - The results of the rounds update the hash and form the final 256 bit output
        at the end
    
    """



    #print("\nOriginal password:", password)
    #print("Generated salt:", salt)



    # Step 1: Append salt to password
    saltedPassword = password #+ salt
    #print("\nSalted password:", saltedPassword)



    # Step 2: Convert to binary representation
    binaryInput = ''.join(format(ord(c), '08b') for c in saltedPassword)
    #print("\nBinary representation of input:\n", binaryInput)



    # Step 3: Padding the input to 512-bit block
    originalLength = len(binaryInput)
    binaryInput += '1'  # Append a single 1-bit
    while len(binaryInput) % 512 != 448:
        binaryInput += '0'

    lengthBits = format(originalLength, '064b')  
    binaryInput += lengthBits
    #print("\nPadded input (512 bits):\n", binaryInput)



    # Step 4: Break into 16 32-bit words
    words = [int(binaryInput[i:i+32], 2) for i in range(0, 512, 32)]
    #print("\nInitial 16 words (in hex):")
    #for i, word in enumerate(words):
        #print(f"Word {i}: {hex(word)}")



    # Step 5: Extend to 64 words
    for i in range(16, 64):
        s0 = right_rotate(words[i-15], 7) ^ right_rotate(words[i-15], 18) ^ (words[i-15] >> 3)
        s1 = right_rotate(words[i-2], 17) ^ right_rotate(words[i-2], 19) ^ (words[i-2] >> 10)
        newWord = (words[i-16] + s0 + words[i-7] + s1) & 0xFFFFFFFF
        words.append(newWord)

    #print("\nExtended 64 words (in hex):")
    #or i in range(64):
     #   print(f"Word {i}: {hex(words[i])}")



    # Step 6: Initialize hash values
    a, b, c, d, e, f, g, h = H



    # Step 7: Main loop (64 rounds)
    for i in range(64):

        # Compute the SHA-256 round functions:
        
        # S1 is the sum of three right rotations on 'e'
        S1 = right_rotate(e, 6) ^ right_rotate(e, 11) ^ right_rotate(e, 25)
        
        # 'ch' (choose function) selects bits from f or g based on e
        ch = (e & f) ^ (~e & g)

        # Compute the temporary value temp1
        temp1 = (h + S1 + ch + K[i] + words[i]) & 0xFFFFFFFF

        # S0 is the sum of three right rotations on 'a'
        S0 = right_rotate(a, 2) ^ right_rotate(a, 13) ^ right_rotate(a, 22)

        # 'maj' (majority function) chooses the majority bit from a, b, and c
        maj = (a & b) ^ (a & c) ^ (b & c)

        # Compute the second temporary value temp2
        temp2 = (S0 + maj) & 0xFFFFFFFF

        # Update the working variables for the next round:
        
        # Shift values downward, h takes the value of g, g takes f, etc.
        h = g
        g = f
        f = e
        
        # 'e' gets updated with d + temp1
        e = (d + temp1) & 0xFFFFFFFF

        d = c
        c = b
        b = a

        # 'a' gets updated with temp1 + temp2
        a = (temp1 + temp2) & 0xFFFFFFFF

        # Print the updated state after this round
        #print(f"\nRound {i+1}:")
        #print(f"a: {hex(a)}, b: {hex(b)}, c: {hex(c)}, d: {hex(d)}, e: {hex(e)}, f: {hex(f)}, g: {hex(g)}, h: {hex(h)}")




    # Step 8: Compute final hash
    finalHash = [(H[i] + x) & 0xFFFFFFFF for i, x in enumerate([a, b, c, d, e, f, g, h])]
    hashHex = ''.join(format(x, '08x') for x in finalHash)

    return hashHex




def main():

    passwords = [10]

    run = True
    while run:
        choice = int(input('Choose option:\n1. Sign up\n2. Login\n3. Quit program\nEnter 1, 2, or 3.\n'))
        if choice == 1:
            password = input("\nEnter a password that is: \n\n - 7+ characters\n - 1+ Uppercase letter\n - 1+ Special Character\n - 1+ number\n\n")

            if validatePassword(password):
                #salt = generateSalt()
                hashHex = sha256_math(password)
                print("\nFinal SHA-256 Hash:", hashHex)
                passwords.append(hashHex)


            else:
                print('Invalid password! Try again')
                main()

        elif choice == 2:
            password = input('\nEnter your password: \n')
            hashHex = sha256_math_login(password)

            # Complete "checksum" for login password
            # - refactor sha256 to not print out stuff, or just copy it
            # - output good/bad message for login success/failure

            found = False  # Boolean flag to track if we found a match
            
            for item in passwords:
                if hashHex == item:
                    print('Login Successful!')
                    print('Here is all the money! $999999999')
                    found = True
                    break
            if found == False:
                print('This password is not valid.')
                print('Calling the police...')
        
        elif choice == 3:
            print('Program complete.')
            run = False
        

if __name__ == "__main__":
    main()
