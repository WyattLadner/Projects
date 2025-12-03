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
    return ''.join(random.choices(string.ascii_letters + string.digits, k=8))

def right_rotate(value, shift, bits=32):
    return (value >> shift) | (value << (bits - shift)) & 0xFFFFFFFF

def sha256_math(salted_input, verbose=True):
    binaryInput = ''.join(format(ord(c), '08b') for c in salted_input)
    if verbose:
        print("\nBinary representation of input:\n", binaryInput)

    originalLength = len(binaryInput)
    binaryInput += '1'
    while len(binaryInput) % 512 != 448:
        binaryInput += '0'
    binaryInput += format(originalLength, '064b')

    if verbose:
        print("\nPadded input (512 bits):\n", binaryInput)

    words = [int(binaryInput[i:i+32], 2) for i in range(0, 512, 32)]
    for i in range(16, 64):
        s0 = right_rotate(words[i-15], 7) ^ right_rotate(words[i-15], 18) ^ (words[i-15] >> 3)
        s1 = right_rotate(words[i-2], 17) ^ right_rotate(words[i-2], 19) ^ (words[i-2] >> 10)
        newWord = (words[i-16] + s0 + words[i-7] + s1) & 0xFFFFFFFF
        words.append(newWord)

    a, b, c, d, e, f, g, h = H
    for i in range(64):
        S1 = right_rotate(e, 6) ^ right_rotate(e, 11) ^ right_rotate(e, 25)
        ch = (e & f) ^ (~e & g)
        temp1 = (h + S1 + ch + K[i] + words[i]) & 0xFFFFFFFF
        S0 = right_rotate(a, 2) ^ right_rotate(a, 13) ^ right_rotate(a, 22)
        maj = (a & b) ^ (a & c) ^ (b & c)
        temp2 = (S0 + maj) & 0xFFFFFFFF

        h, g, f, e, d, c, b, a = g, f, e, (d + temp1) & 0xFFFFFFFF, c, b, a, (temp1 + temp2) & 0xFFFFFFFF

    finalHash = [(H[i] + x) & 0xFFFFFFFF for i, x in enumerate([a, b, c, d, e, f, g, h])]
    return ''.join(format(x, '08x') for x in finalHash)

def main():
    user_data = {}  # username -> (salt, hashed_password)

    while True:
        choice = input('\nChoose option:\n1. Sign up\n2. Login\n3. Quit program\nEnter 1, 2, or 3: ')
        if choice == '1':
            username = input("\nEnter your username: ")
            password = input("Enter a password (7+ chars, 1+ upper, number, special): ")
            if validatePassword(password):
                salt = generateSalt()
                combined = username + salt + password
                hashed = sha256_math(combined, verbose=True)
                user_data[username] = (salt, hashed)
                print("\nFinal SHA-256 Hash:", hashed)
            else:
                print("Invalid password! Try again.")

        elif choice == '2':
            username = input("\nEnter your username: ")
            password = input("Enter your password: ")
            if username in user_data:
                salt, stored_hash = user_data[username]
                combined = username + salt + password
                input_hash = sha256_math(combined, verbose=False)
                if input_hash == stored_hash:
                    print("Login successful! Here's all the money! $999999999")
                else:
                    print("Incorrect password. Calling the police...")
            else:
                print("Username not found.")

        elif choice == '3':
            print("Program complete.")
            break

if __name__ == "__main__":
    main()