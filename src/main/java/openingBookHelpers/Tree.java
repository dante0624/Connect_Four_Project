package openingBookHelpers;

// Binary mapping tree with key value pairs
public class Tree {
    public TreeNode root;

    // Tries to find and return a node from its key, given some starting node
	// Returns null if the key is not found
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

    // Return a whole node based on a key, if that key is in the tree
    // Returns null otherwise
    public TreeNode searchNode(long key) {
        return searchNodeHelper(key, root);
    }

	/* Inserts key and value below a starting node
	If the starting node is null we return the new (key, value) as a new node
	Otherwise we return back the original starting node */
    public TreeNode insertNodeHelper(long key, byte value, TreeNode node) {
        if (node == null) {
            return new TreeNode(key, value);
        }

        // Can't have duplicate keys
        if (key == node.key) {
            throw new IllegalArgumentException("Tree already contains a node with key " + key);
        }

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
