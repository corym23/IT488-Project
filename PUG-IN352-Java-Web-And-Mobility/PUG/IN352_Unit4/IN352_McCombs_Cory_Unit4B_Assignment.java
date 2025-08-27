


public class IN352_McCombs_Cory_Unit4B_Assignment {
    public static void main(String[] args) {
      
        // *********************************************************
        // ****Assignment 4, Part B, Section 1
        // *********************************************************

        
        System.out.println(); // Print a blank line
        System.out.println("********** Section 1 - Bubble Sort **********");
        System.out.println(); // Print a blank line 
        int[] studentGrades = {65, 95, 75, 55, 56, 90, 98, 88, 97, 78}; // Array of student grades
        System.out.println("The unsorted list of grades is: ");
        printArray(studentGrades); // Print the unsorted array
        System.out.println(); // Print a blank line
        sortArrayDescBS(studentGrades); // Sort the array in descending order
        System.out.println("The grades in descending order are: ");
        printArray(studentGrades); // Print the sorted array in descending order
        System.out.println(); // Print a blank line
        sortArrayAscBS(studentGrades); // Sort the array in ascending order
        System.out.println("The grades in ascending order are: ");
        printArray(studentGrades); // Print the sorted array in ascending order
        System.out.println(); // Print a blank line
        System.out.println(); // Print a blank line

        // *********************************************************
        // ****Assignment 4, Part B, Section 2
        // *********************************************************

        
        System.out.println(); // Print a blank line
        System.out.println("*********** Section: 2 - Quick Sort ***********");
        System.out.println(); // Print a blank line
        int[] gradesArray = {65, 95, 75, 55, 56, 90, 98, 88, 97, 78}; // Original unsorted grades array
        System.out.println("The unsorted list of grades is: ");
        printArray(gradesArray); // Print the unsorted array
        System.out.println(); // Print a blank line
        sortArrayDescQS(gradesArray, 0, gradesArray.length - 1); // Sort the array in descending order using quick sort
        System.out.println("The grades in descending order are: ");
        printArray(gradesArray); // Print the sorted array in descending order
        System.out.println(); // Print a blank line
        sortArrayAscQS(gradesArray, 0, gradesArray.length - 1); // Sort the array in ascending order using quick sort
        System.out.println("The grades in ascending order are: ");
        printArray(gradesArray); // Print the sorted array in ascending order
        System.out.println(); // Print a blank line 

        // *********************************************************
        // ****Assignment 4, Part B, Section 3
        // *********************************************************

       
        System.out.println(); // Print a blank line
        System.out.println("*********** Section: 3 - Sequential Search ***********");
        System.out.println(); // Print a blank line
        int[] sortedGradesArray = {55, 56, 65, 75, 78, 88, 90, 95, 97, 98}; // Sorted grades array
        System.out.println("The contents of the grade array are: ");
        printArray(sortedGradesArray); // Print the sorted array
        System.out.println(); // Print a blank line
        String searchResult1 = seqSearch(sortedGradesArray, 75); // Search for the value 75
        System.out.println(searchResult1); // Print the result of the first search
        System.out.println(); // Print a blank line
        String searchResult2 = seqSearch(sortedGradesArray, 60); // Search for the value 60
        System.out.println(searchResult2); // Print the result of the second search
        System.out.println(); // Print a blank line

        // *********************************************************
        // ****Assignment 4, Part B, Section 4
        // *********************************************************    

        
        System.out.println(); // Print a blank line
        System.out.println("*********** Section: 4 - Binary Search ***********");
        System.out.println(); // Print a blank line
        int[] binarySearchArray = {55, 56, 65, 75, 78, 88, 90, 95, 97, 98}; // Sorted grades array for binary search
        System.out.println("The contents of the grade array are: ");
        printArray(binarySearchArray); // Print the sorted array
        System.out.println(); // Print a blank line
        String binarySearchResult1 = binarySearch(binarySearchArray, 56); // Search for the value 56
        System.out.println(binarySearchResult1); // Print the result of the first binary search
        System.out.println(); // Print a blank line
        String binarySearchResult2 = binarySearch(binarySearchArray, 50); // Search for the value 50
        System.out.println(binarySearchResult2); // Print the result of the second binary search
        System.out.println(); // Print a blank line
        System.out.println(); // Print a blank line

       
    }

    // Methods for sorting and searching    
        private static void printArray(int[] studentGrades) {
        // Print the contents of the array
        for (int grade : studentGrades) {
            System.out.print(grade + " "); // Print each grade followed by a space
        }
        System.out.println(); // Print a new line after printing all grades
    }

    private static void sortArrayDescBS(int[] studentGrades) {
        // Bubble sort algorithm to sort the array in descending order
        int n = studentGrades.length; // Get the length of the array
        for (int i = 0; i < n - 1; i++) { // Outer loop for passes
            for (int j = 0; j < n - i - 1; j++) { // Inner loop for comparisons
                if (studentGrades[j] < studentGrades[j + 1]) { // Compare adjacent elements
                    // Swap if they are in the wrong order
                    int temp = studentGrades[j];
                    studentGrades[j] = studentGrades[j + 1];
                    studentGrades[j + 1] = temp;
                }
            }
        }
    }

        private static void sortArrayDescQS(int[] gradesArray, int i, int j) {
        // Quick sort algorithm to sort the array in descending order
        if (i < j) {
            int pivotIndex = partitionDesc(gradesArray, i, j); // Partition the array
            sortArrayDescQS(gradesArray, i, pivotIndex - 1); // Recursively sort the left part
            sortArrayDescQS(gradesArray, pivotIndex + 1, j); // Recursively sort the right part
        }
    }

        // Quick sort partition method for descending order
        private static int partitionDesc(int[] gradesArray, int i, int j) {
        int pivot = gradesArray[j]; // Choose the last element as the pivot
        int k = i - 1; // Index of the smaller element
        for (int l = i; l < j; l++) {
            if (gradesArray[l] >= pivot) { // If current element is greater than or equal to pivot
                k++;
                // Swap elements
                int temp = gradesArray[k];
                gradesArray[k] = gradesArray[l];
                gradesArray[l] = temp;
            }
        }
        // Place the pivot in the correct position
        int temp = gradesArray[k + 1];
        gradesArray[k + 1] = gradesArray[j];
        gradesArray[j] = temp;
        return k + 1;
    }

    // Sequential search method
    private static void sortArrayAscBS(int[] studentGrades) {
        // Bubble sort algorithm to sort the array in ascending order
        int n = studentGrades.length;
        for (int i = 0; i < n - 1; i++) {
            for (int j = 0; j < n - i - 1; j++) {
                if (studentGrades[j] > studentGrades[j + 1]) {
                    int temp = studentGrades[j];
                    studentGrades[j] = studentGrades[j + 1];
                    studentGrades[j + 1] = temp;
                }
            }
        }
    }


    private static void sortArrayAscQS(int[] gradesArray, int i, int j) {
        // Quick sort algorithm to sort the array in ascending order
        if (i < j) {
            int pivotIndex = partitionAsc(gradesArray, i, j);
            sortArrayAscQS(gradesArray, i, pivotIndex - 1);
            sortArrayAscQS(gradesArray, pivotIndex + 1, j);
        }
    }

    private static int partitionAsc(int[] gradesArray, int i, int j) {
        int pivot = gradesArray[j];
        int k = i - 1;
        for (int l = i; l < j; l++) {
            if (gradesArray[l] <= pivot) {
                k++;
                int temp = gradesArray[k];
                gradesArray[k] = gradesArray[l];
                gradesArray[l] = temp;
            }
        }
        int temp = gradesArray[k + 1];
        gradesArray[k + 1] = gradesArray[j];
        gradesArray[j] = temp;
        return k + 1;
    }

    private static String seqSearch(int[] arr, int value) {
        for (int i = 0; i < arr.length; i++) {
            if (arr[i] == value) {
                return "Value " + value + " found at index " + i + ".";
            }
        }
        return "Value " + value + " was not found in the array.";
    }

    private static String binarySearch(int[] arr, int value) {
        int left = 0;
        int right = arr.length - 1;
        while (left <= right) {
            int mid = left + (right - left) / 2;
            if (arr[mid] == value) {
                return "Value " + value + " found at index " + mid + ".";
            } else if (arr[mid] < value) {
                left = mid + 1;
            } else {
                right = mid - 1;
            }
        }
        return "Value " + value + " was not found in the array.";
    }
}
