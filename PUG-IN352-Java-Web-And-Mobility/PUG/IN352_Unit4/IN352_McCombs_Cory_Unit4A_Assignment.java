
public class IN352_McCombs_Cory_Unit4A_Assignment {
   
    
    public static void main(String[] args) {

        //*********************************************************
        //****Assignment 4, Part A, Section 1
        //*********************************************************

        System.out.println(); // Print a blank line
        System.out.println("********** Section 1 - Quick Sort   **********");
        System.out.println(); // Print a blank line

        int[] myArray = {6501, 9503, 7557, 5535, 5601, 9001, 9888, 8801, 9767, 7815}; // array to sort
        System.out.println("The array unsorted is: " + java.util.Arrays.toString(myArray)); // Print unsorted array

        quickSort(myArray, 0, myArray.length - 1); // Call the quickSort method

        System.out.println(); // Print a blank line
        System.out.println("The array sorted is: " + java.util.Arrays.toString(myArray)); // Print sorted array
        System.out.println(); // Print a blank line

        //*********************************************************
        //****Assignment 4, Part A, Section 2
        //*********************************************************

        System.out.println(); // Print a blank line
        System.out.println("********** Section 2 - Bubble Sort   **********");
        System.out.println(); // Print a blank line
        int[] myBubbleArray = {51, 35, 18, 67, 22, 7, 40, 1}; // array to sort
        System.out.println("The array unsorted is: " + java.util.Arrays.toString(myBubbleArray)); // Print unsorted array
        bubbleSort(myBubbleArray); // Call the bubbleSort method  
        System.out.println(); // Print a blank line
        System.out.println("The array sorted is: " + java.util.Arrays.toString(myBubbleArray)); // Print sorted array
        System.out.println(); // Print a blank line

        //*********************************************************
        //****Assignment 4, Part A, Section 3
        //*********************************************************
       
        System.out.println(); // Print a blank line
        System.out.println("********** Section 3 - Binary Search   **********");
        System.out.println(); // Print a blank line
        //int[] myArray = {6501, 9503, 7557, 5535, 5601, 9001, 9888, 8801, 9767, 7815}; // Presorted array
        int searchValue1 = 8801; // First search value
        int searchValue2 = 7777; // Second search value
        int result1 = binarySearch(myArray, searchValue1); // Call binarySearch for the first value
        System.out.println("Searching for " + searchValue1 + ": " + (result1 != -1 ? "Found at index " + result1 : "Not found in the list")); // Print result
        System.out.println(); // Print a blank line
        int result2 = binarySearch(myArray, searchValue2); // Call binarySearch for the second value
        System.out.println("Searching for " + searchValue2 + ": " + (result2 != -1 ? "Found at index " + result2 : "Not found in the list")); // Print result
        System.out.println(); // Print a blank line
                 
    }


    private static void quickSort(int[] array, int i, int j) {
        // Quick sort implementation
        if (i < j) {
            int pivotIndex = partition(array, i, j); // Partition the array and get the pivot index
            quickSort(array, i, pivotIndex - 1); // Recursively sort the left part
            quickSort(array, pivotIndex + 1, j); // Recursively sort the right part
        }
    }

    private static int partition(int[] array, int i, int j) {
        int pivot = array[j]; // Choose the last element as the pivot
        int k = i - 1; // Index of smaller element

        for (int l = i; l < j; l++) {
            if (array[l] <= pivot) { // If current element is smaller than or equal to pivot
                k++;
                swap(array, k, l); // Swap elements
            }
        }
        swap(array, k + 1, j); // Place the pivot in the correct position
        return k + 1; // Return the index of the pivot  
    }

    private static void swap(int[] array, int k, int l) {
        // Swap elements at indices k and l
        int temp = array[k];
        array[k] = array[l];
        array[l] = temp;

    }

    private static void bubbleSort(int[] bubbleArray) {
        // Bubble sort implementation
        int n = bubbleArray.length; // Get the length of the array
        boolean swapped; // Flag to check if a swap occurred
        do {
            swapped = false; // Reset the flag for each pass
            for (int i = 0; i < n - 1; i++) {
                if (bubbleArray[i] > bubbleArray[i + 1]) { // If the current element is greater than the next
                    // Swap the elements
                    int temp = bubbleArray[i];
                    bubbleArray[i] = bubbleArray[i + 1];
                    bubbleArray[i + 1] = temp;
                    swapped = true; // Set the flag to true indicating a swap occurred
                }
            }
        } while (swapped); // Continue looping until no swaps occur
    }

     private static int binarySearch(int[] searchArray, int target) {
        // Binary search implementation
        int left = 0; // Start index
        int right = searchArray.length - 1; // End index

        while (left <= right) {
            int mid = left + (right - left) / 2; // Calculate the middle index

            if (searchArray[mid] == target) {
                return mid; // Target found, return index
            } else if (searchArray[mid] < target) {
                left = mid + 1; // Search in the right half
            } else {
                right = mid - 1; // Search in the left half
            }
        }
        return -1; // Target not found, return -1  
    }
}
