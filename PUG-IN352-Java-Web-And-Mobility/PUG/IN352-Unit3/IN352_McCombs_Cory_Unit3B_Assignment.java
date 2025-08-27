import java.util.*;

/**
 * IN352_McCombs_Cory_Unit3B_Assignment
 * Demonstrates Java Collections (Set, SortedSet, LinkedList)
 * and Binary Tree traversals for Unit 3B Assignment.
 */
public class IN352_McCombs_Cory_Unit3B_Assignment {
    public static void main(String[] args) {
        
        // *********************************************************
        // ****Assignment 3, Part B, Section 1: Sets and SortedSets
        // *********************************************************

        System.out.println();
        System.out.println("*********** Section: B1 ***********");
        System.out.println("Contents of the set are:");
        // Create a set of mammals and print them (unordered)
        String[] mammals = { "Bear", "Gorilla", "Tiger", "Polar Bear", "Lion", "Monkey" };
        Set<String> setMammals = new HashSet<>(Arrays.asList(mammals));
        for (String mammal : setMammals) {
            System.out.println(mammal);
        }
        // Create a sorted set and print mammals in order
        System.out.println("Contents of the sorted set are:");
        Set<String> sortedMammals = new TreeSet<>(setMammals);
        for (String mammal : sortedMammals) {
            System.out.println(mammal);
        }
        // Print first and last items in the sorted set
        System.out.println("The first item in the set is: " + ((TreeSet<String>) sortedMammals).first());
        System.out.println("The last item in the set is: " + ((TreeSet<String>) sortedMammals).last());

        System.out.println();

        // *********************************************************
        // ****Assignment 3, Part B, Section 2: LinkedList Operations
        // *********************************************************

        System.out.println("*********** Section: B2 ***********");
        System.out.println();

        // Create a LinkedList of friends and print the list
        LinkedList<String> myFriends = new LinkedList<>();
        myFriends.add("Fred 602-299-3300");
        myFriends.add("Ann 602-555-4949");
        myFriends.add("Grace 520-544-9898");
        myFriends.add("Sam 602-343-8723");
        myFriends.add("Dorothy 520-689-9745");
        myFriends.add("Susan 520-981-8745");
        myFriends.add("Bill 520-456-9823");
        myFriends.add("Mary 520-788-3457");
        System.out.println("The contents of my friends list:");
        for (String friend : myFriends) {
            System.out.println(friend);
        }
        // Remove specific friends from the list
        myFriends.remove("Bill 520-456-9823"); // Remove by value
        myFriends.removeFirst(); // Remove first element
        myFriends.removeLast(); // Remove last element
        // Safely update Mary's phone number using try-catch to avoid

        // IndexOutOfBoundsException
        try {
            int maryIndex = myFriends.indexOf("Mary 520-788-3457");
            if (maryIndex != -1) {
                myFriends.set(maryIndex, "Mary 520-897-4567");
            }
        } catch (IndexOutOfBoundsException e) {
            System.out.println("Mary is not in the list, so her number could not be updated.");
        }
        System.out.println();
        System.out.println("The updated contents of my friends list:");
        for (String friend : myFriends) {
            System.out.println(friend);
        }
        // Print the number of friends and check if Fred is still in the list
        System.out.println("The number of friends in my list is: " + myFriends.size());
        if (myFriends.contains("Fred 602-299-3300")) {
            System.out.println("Fred is still in the list.");
        } else {
            System.out.println("Fred is no longer in the list.");
        }
        System.out.println();

        // *********************************************************
        // ****Assignment 3, Part B, Section 3: Binary Tree Traversals
        // *********************************************************

        System.out.println("*********** Section: B3 ***********");
        System.out.println();
        // Create a new instance of BinaryTree
        BinaryTree myBinaryTree = new BinaryTree();
        // Insert values into the binary tree
        myBinaryTree.insert(50); // Insert root node
        myBinaryTree.insert(30); // Insert left child
        myBinaryTree.insert(45); // Insert right child of left child
        myBinaryTree.insert(12); // Insert left child of left child
        myBinaryTree.insert(29); // Insert right child of left child of left child
        //
        try {             
            myBinaryTree.traverse();
            System.out.println();
        } catch (UnsupportedOperationException e) {
            System.out.println("A binary tree traversal method is not yet implemented: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("An error occurred while traversing the binary tree: " + e.getMessage());
        }
        System.out.println();

    }

    // BinaryTree class for binary tree operations and traversals
    static class BinaryTree {

        private Node root; // Root node of the binary tree

        // Node class represents each node in the binary tree
        private static class Node {
            int value; // Value stored in the node
            Node left, right; // Left and right child nodes

            Node(int value) {
                this.value = value;
            }
        }

        // Constructor initializes the root to null
        public BinaryTree() {
            root = null;
        }

        // Public method to insert a value into the binary tree
        public void insert(int value) {
            root = insertRec(root, value);
        }

        // Helper method for recursive insertion
        private Node insertRec(Node root, int value) {
            if (root == null) {
                root = new Node(value); // Create a new node if root is null
                return root;
            }

            if (value < root.value) {
                root.left = insertRec(root.left, value); // Insert in left subtree
            } else if (value > root.value) {
                root.right = insertRec(root.right, value); // Insert in right subtree
            }

            return root; // Return the (possibly updated) root node
        }

        // Public method to traverse the tree in order and print values
        public void traverse() {
            System.out.println();
            System.out.println("Traversing the binary tree in order:");
            printInOrder(root);
            System.out.println();
            System.out.println("Traversing the binary tree in pre-order:");
            printPreOrder(root);
            System.out.println();
            System.out.println("Traversing the binary tree in post-order:");
            printPostOrder(root);
            System.out.println();
        }

        // Helper method for in-order traversal (left, root, right)
        public void printInOrder(Node node) {
            if (node != null) {
                printInOrder(node.left);
                System.out.print(node.value + " ");
                printInOrder(node.right);
            }
        }

        // Helper method for pre-order traversal (root, left, right)
        public void printPreOrder(Node node) {
            if (node != null) {
                System.out.print(node.value + " ");
                printPreOrder(node.left);
                printPreOrder(node.right);
            }
        }

        // Helper method for post-order traversal (left, right, root)
        public void printPostOrder(Node node) {
            if (node != null) {
                printPostOrder(node.left);
                printPostOrder(node.right);
                System.out.print(node.value + " ");
            }
        }

        // Getter for the root node
        public Node getRoot() {
            return root;
        }
    }
}
