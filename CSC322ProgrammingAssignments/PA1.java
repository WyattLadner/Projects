/*
Programming Assignment 1
CSC322
Wyatt Ladner
Savanna Trovinger
Blake White

This program will generate 1000 pairs of integers,
run the brute force algorithm to find the GCD of each,
 */


import java.util.Random;


public class PA1 {

    public static void main(String[] args) {
        assignmentData();
    }

    public static void assignmentData() {
        Random random = new Random();

        //creates 2D array; 2 columns of 1000 rows
        int rows = 1000;
        int cols = 2;
        int[][] nums = new int[rows][cols];

        //populate array with random ints
        for (int i = 0; i < rows; i++) {
            nums[i][0] = random.nextInt(1000) + 1;
            nums[i][1] = random.nextInt(1000) + 1;
        }

        //array for computation times
        double[] times = new double[1000];

        //iterates through 2D array and finds GCD and records time into array
        for (int i = 0; i < rows; i++) {
            int num1 = nums[i][0];
            int num2 = nums[i][1];

            //tracks time in nanoseconds it takes for gcd to be found
            double starttime = System.nanoTime();

            //uncomment whichever method you want to use

//            int gcd = eav1GCD(num1, num2);
  //          int gcd = eav2gcd(num1, num2);
//          int gcd = bfV1(num1, num2);
            int gcd = bfV2(num1, num2);

            double endtime = System.nanoTime();
            double total = endtime - starttime;

            System.out.println(gcd);

            //adds time for gcd calculation into an array
            times[i] = total;
        }
        //prints calc times all together; makes for easier data
        //intput into excel sheets
        for (int i = 0; i < times.length; i++) {
            System.out.println(times[i]);
        }

        //prints integer pairs
        for (int i = 0; i < nums.length; i++) {             // Loop through rows
            for (int j = 0; j < nums[i].length; j++) {      // Loop through columns
                System.out.print(nums[i][j] + "\t");
            }
            System.out.println();
        }
    }

    //version 1 of EA
    public static int eav1GCD(int a, int b) {
        while (b != 0) {
            int temp = b;
            b = a % b;
            a = temp;
        }
        return a;
    }

        //version 2 of EA
    public static int eav2gcd(int a, int b) {
        // Ensure a >= b > 0
        if (a < b) {
            int temp = a;
            a = b;
            b = temp;
        }

        int remainder;
        while (b != 0) {
            remainder = a - b;

            // Handling cases where quotient > 1
            while (remainder > b) {
                remainder -= b;
            }

            a = b;
            b = remainder;
        }

        return a; // GCD is stored in 'a'
    }

    //Brute force version 1
    public static int bfV1(int a, int b) {
        int smaller = Math.min(a, b);
        int gcd = 1; // Default GCD

        for (int i = 1; i <= smaller; i++) {
            if (a % i == 0 && b % i == 0) {
                gcd = i;
            }
        }

        return gcd;
    }

    // Brute force version 2 V2:
    public static int bfV2(int a, int b) {
        int smaller = Math.min(a, b);

        for (int i = smaller; i > 0; i--) {
            if (a % i == 0 && b % i == 0) {
                return i;
            }
        }

        return 1;
    }
}

