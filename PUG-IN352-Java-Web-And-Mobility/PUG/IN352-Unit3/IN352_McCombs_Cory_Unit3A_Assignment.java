import java.util.*;

/**
 * This class demonstrates the use of Java Collections (Set, SortedSet,
 * LinkedList)
 * and a simple BinaryTree implementation for Unit 3A Assignment.
 */
public class IN352_McCombs_Cory_Unit3A_Assignment {
    public static void main(String[] args) {
        // *********************************************************
        // ****Assignment Unit 3 , Part A, Section 1
        // *********************************************************

        System.out.println();
        System.out.println();

        System.out.println("*********** Section: A1 ***********");

        System.out.println();
        // Create an array of professions
        String[] professions = {
                "Software Engineer",
                "QA Engineer",
                "Data Scientist",
                "Product Manager",
                "UX Designer",
                "DevOps Engineer",
                "Systems Analyst",
                "Cloud Architect"
        };
        // Use a HashSet to store unique professions (unordered)
        Set<String> professionSet = new HashSet<>();
        for (String profession : professions) {
            professionSet.add(profession);
        }

        // Create a TreeSet to sort the professions alphabetically
        SortedSet<String> sortedProfessionSet = new TreeSet<>(professionSet);

        System.out.println("Original List:");
        System.out.println(professionSet); // Print the original set (unordered)

        System.out.println();
        System.out.println();

        System.out.println("Sorted List:");
        System.out.println(sortedProfessionSet); // Print the sorted set

        // *********************************************************
        // ****Assignment Unit 3 , Part A, Section 2
        // *********************************************************

        System.out.println();
        System.out.println();

        System.out.println("*********** Section: A2 ***********");
        System.out.println();

        // Create a LinkedList of book titles
        LinkedList<String> books = new LinkedList<>();
        books.add("A Doll's House");
        books.add("Invisible Man");
        books.add("The Four Agreements");
        books.add("The Lord Of The Flies");
        books.add("Sacred Pathways");
        books.add("48 Laws of Power");
        books.add("Test Automation in the Real World");
        books.add("The Way of the SEAL");
        System.out.println("Original Book List:");
        // Print the original linked list
        System.out.println(books);

        System.out.println();
        // Sort the linked list alphabetically
        Collections.sort(books);

        System.out.println();
        System.out.println("Sorted Book List:");
        // Print the sorted linked list
        System.out.println(books);

        // Remove the second item if the list has more than one item
        if (books.size() > 1) {
            books.remove(1); // Remove the second item
        }
        // Remove the first item if the list is not empty
        if (!books.isEmpty()) {
            books.remove(0); // Remove the first item
        }
        // Remove the last item if the list is not empty
        if (!books.isEmpty()) {
            books.remove(books.size() - 1); // Remove the last item
        }

        System.out.println();
        System.out.println("Book List After Deletions:");
        // Print the list after deletions
        System.out.println(books);

        System.out.println();
        // Print the number of items in the list
        System.out.println("The number of items in my book list is: " + books.size());

        // Check if "Brave New World" exists in the list
        boolean exists = books.contains("Brave New World");
        System.out.println("Does 'Brave New World' exist in the list? " + exists);
        System.out.println();

        // *********************************************************
        // ****Assignment Unit 3 , Part A, Section 3
        // *********************************************************

        System.out.println();
        System.out.println("*********** Section: A3 ***********");

        // Create a new instance of BinaryTree
        BinaryTree myBinaryTree = new BinaryTree();

        // Insert values into the binary tree
        myBinaryTree.insert(50); // Insert root node
        myBinaryTree.insert(30); // Insert left child
        myBinaryTree.insert(45); // Insert right child of left child
        myBinaryTree.insert(12); // Insert left child of left child
        myBinaryTree.insert(29); // Insert right child of left child of left child

        System.out.println();
        System.out.println("The contents of the binary tree are:");

        // Traverse and print the contents of the binary tree in order
        myBinaryTree.traverse(); // prints the tree contents in ascending order

        System.out.println();
        System.out.println();
    }
}

// BinaryTree.java
// This class represents a binary tree with methods to insert and traverse
// nodes.
// It is defined as a top-level class for encapsulation and demonstration
// purposes.
class BinaryTree {

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
        traverseInOrder(root);
    }

    // Helper method for in-order traversal (left, root, right)
    private void traverseInOrder(Node node) {
        if (node != null) {
            traverseInOrder(node.left); // Visit left subtree
            System.out.print(node.value + " "); // Print node value
            traverseInOrder(node.right); // Visit right subtree
        }

    }
}
