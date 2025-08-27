import java.util.Scanner;
import java.util.Stack;

public class IN352_McCombs_Cory_Unit2_Assignment {

    public static void main(String[] args) {

        // *********************************************************

        // ****Assignment 2, Part A, Section 1

        // *********************************************************

        System.out.println("**********Section 1**********");
        System.out.println(); // Print a blank line
        System.out.println("Please enter any word and then hit the enter key: ");
        Scanner scanner = new Scanner(System.in); // Create a Scanner object to read input

        String userWord = scanner.nextLine(); // Read user input
        System.out.println("You word in reverse is: " + revString(userWord)); // Print the input back to the in reverse
                                                                              // order
        System.out.println(); // Print a blank line

        // *********************************************************

        // ****Assignment 2, Part A, Section 2

        // *********************************************************

        System.out.println("**********Section 2**********");
        System.out.println(); // Print a blank line

        // Create a queue structure using Stack
        Stack<String> shoppers = new Stack<>();
        shoppers.push("Jane");
        shoppers.push("Bob");
        shoppers.push("Liza");
        shoppers.push("Tom");
        shoppers.push("Mary");

        // Print the number of shoppers
        System.out.println("The number of shoppers at the grocery store is: " + shoppers.size());

        // Print the first shopper in line
        System.out.println("The first shopper in line is: " + shoppers.firstElement());

        // Add more shoppers to the queue
        shoppers.push("Stephen");
        shoppers.push("Ellen");

        // Remove three shoppers from the queue
        shoppers.pop(); // Remove Ellen
        shoppers.pop(); // Remove Stephen
        shoppers.pop(); // Remove Mary

        // Print the current number of shoppers
        System.out.println("The number of shoppers now in line is: " + shoppers.size());

        // Print the current first shopper in line
        System.out.println("The shopper currently first in line is: " + shoppers.firstElement());
        System.out.println(); // Print a blank line

        // *********************************************************

        // ****Assignment 2, Part B, Section 1

        // *********************************************************

        System.out.println("**********Section 1**********");
        System.out.println(); // Print a blank line

        Stack<String> bankQueue = new Stack<>();// Create a stack to represent the bank queue
        // Add customers to the queue
        bankQueue.push("Jim");
        bankQueue.push("Bob");
        bankQueue.push("Susan");
        bankQueue.push("Liz");
        bankQueue.push("Alex");

        // Print the number of people in line
        System.out.println("The number of people in line at the bank is: " + bankQueue.size());

        // Print the names of those in line
        System.out.println("The names of those in line at the bank are: " + bankQueue);

        // Print the first customer in line and remove them from the queue
        System.out.println("The first customer in line is: " + bankQueue.firstElement());
        bankQueue.pop();

        // Add more customers to the queue
        bankQueue.push("Andy");
        bankQueue.push("Rhonda");

        // Remove three more customers from the queue
        if (!bankQueue.isEmpty()) {
            bankQueue.pop(); // Remove Rhonda
        }
        if (!bankQueue.isEmpty()) {
            bankQueue.pop(); // Remove Andy
        }
        if (!bankQueue.isEmpty()) {
            bankQueue.pop(); // Remove Alex
        }

        // Print the current number of customers in line
        System.out.println("The number of customers in line now is: " + bankQueue.size());
        System.out.println(); // Print a blank line

        // *********************************************************

        // ****Assignment 2, Part B, Section 2

        // *********************************************************

        System.out.println("**********Section 2**********");
        System.out.println(); // Print a blank line

        System.out.println("Please enter a sentence and then hit the enter key: ");
        Scanner sentenceScanner = new Scanner(System.in); // Create a Scanner object to read input
        String userSentence = sentenceScanner.nextLine(); // Read user input
        String[] words = userSentence.split(" "); // Split the sentence into words
        Stack<String> wordStack = new Stack<>(); // Create a stack to hold words
        for (String word : words) { // Push each word onto the stack
            wordStack.push(word);
        }
        System.out.println("The sentence in reverse is: ");
        while (!wordStack.isEmpty()) { // Pop words from the stack until it's empty
            System.out.println(wordStack.pop()); // Print each word on a new line
        }
        System.out.println(); // Print a blank line
        // Close the scanners
        sentenceScanner.close();
        scanner.close();
    }

    // Method to reverse a string using a stack
    private static String revString(String str) {
        Stack<Character> stack = new Stack<>(); // Create a stack to hold characters
        for (char c : str.toCharArray()) { // Convert string to character array and push each character onto the stack
            stack.push(c);
        }

        StringBuilder reversed = new StringBuilder(); // Create a StringBuilder to build the reversed string
        while (!stack.isEmpty()) { // Pop characters from the stack until it's empty
            reversed.append(stack.pop());
        }
        return reversed.toString(); // Return the reversed string
    }
}
