/*
Purdue University Global
IN352 - Advanced Software Development Including Web & Mobility Using Java
Unit 1 Assignment / Module 1 Competency Assessment
Topic: Recursion
 */
public class IN352_McCombs_Cory_Unit1_Assignment {
    // Add a static array for memoization (size can be adjusted as needed)
    private static int[] fibMemo = new int[100];

    public static void main(String[] args) {
        // *********************************************************
        // ****Assignment 1, Section 1
        // *********************************************************
        
        /*
            Print a blank line.
            Create a method called Fibonacci which will recursively calculate the Fibonacci sequence for the number passed to it.
            Call the Fibonacci method and pass it the value 10.
         */
        System.out.println(); // Print a blank line
        System.out.println( "**********Section 1**********");
        System.out.println(); // Print a blank line
        int number = 10; // The number to calculate the Fibonacci sequence for
        int result = Fibonacci(number); // Call the Fibonacci method
        System.out.println("The Fibonacci of " + number + " is: " + result);
        System.out.println(); // Print a blank line
        // *********************************************************
        // ****Assignment 1, Section 2
        // *********************************************************
        
        /*Print a blank line.
            Create a method called factorial which will recursively calculate the factorial of an integer value passed to it.
            Create a for loop to use the factorial method to find and print the factorials of every number from 1 to 4.
            When printing the factorial values, print them showing the number and its factorial. For example, the factorial of 4 would be printed as 4! = 24
        */
        
        System.out.println( "**********Section 2**********");
        System.out.println(); // Print a blank line
        for (int i = 1; i <= 4; i++) { // Loop from 1 to 4
            int factorialResult = factorial(i); // Call the factorial method
            System.out.println(i + "! = " + factorialResult); // Print the result in the format "n! = result"
        }

    }

    
    private static int Fibonacci(int number) {
        // Memoization: check if value is already computed
        if (number < 0) {
            throw new IllegalArgumentException("Number must be non-negative");
        }
        if (number == 0) {
            return 0;
        } else if (number == 1) {
            return 1;
        }
        if (fibMemo[number] != 0) {
            return fibMemo[number];
        }
        // Compute and store the result
        fibMemo[number] = Fibonacci(number - 1) + Fibonacci(number - 2);
        return fibMemo[number];
    }

    private static int factorial(int i) {
        // Base case: if i is 0 or 1, return 1 (factorial of 0 and 1 is 1)
        if (i == 0 || i == 1) {
            return 1;
        }
        
        // Recursive case: return i multiplied by the factorial of (i - 1)
        return i * factorial(i - 1);
    }


}
