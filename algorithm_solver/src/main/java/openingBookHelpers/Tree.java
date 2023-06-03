package openingBookHelpers;

public class Tree {
    // Meant to reinvent a binary mapping tree
    // Each node has (key, value) pairs
    // Contains simple, abstract methods inserting a new node at the bottom of tree

    // Public variable
    public TreeNode root;

    // Private Interface
    // Tries to find and return a node from its key, given some starting node
    // Simple and recursive implementation
    private TreeNode searchNodeHelper(long key, TreeNode node) {
        if (node == null) {
            return null;
        }

        if (key == node.key) {
            return node;
        }
        if (key < node.key) {
            return searchNodeHelper(key, node.left);
        }
        return searchNodeHelper(key, node.right);
    }

    // Public Interface
    // Simple getter
    public TreeNode getRoot() {
        return root;
    }

    // Return a whole node based on a key, if that key is in the tree
    // Returns null otherwise
    public TreeNode searchNode(long key) {
        return searchNodeHelper(key, root);
    }

    // Inserts a new node with (key, value), given some starting node
    // If the starting node is null, just return the new node
    // Otherwise, recursively insert call the function below, then use the return value to actually insert
    public TreeNode insertNodeHelper(long key, byte value, TreeNode node) {
        // Null node case
        if (node == null) {
            return new TreeNode(key, value);
        }

        // Can't have duplicate keys
        if (key == node.key) {
            throw new IllegalArgumentException("Tree already contains a node with key " + key);
        }

        // Recursive cases
        if (key < node.key) {
            node.left = insertNodeHelper(key, value, node.left);
        }
        else {
            node.right = insertNodeHelper(key, value, node.right);
        }

        return node;
    }

    // Inserts a node into a tree at a leaf, if the key is not yet in the tree
    // Otherwise, throw an exception
    public void insertNode(long key, byte value) {
        root = insertNodeHelper(key, value, root);
    }
}
