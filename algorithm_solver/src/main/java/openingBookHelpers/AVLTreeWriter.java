package openingBookHelpers;

import java.io.FileOutputStream;
import java.io.IOException;

// Credit to Sven Woltmann at:
// https://github.com/SvenWoltmann/binary-tree
public class AVLTreeWriter extends Tree {

    // Necessary because we define the "height" of a null node to be -1
    private int getHeight(TreeNode node) {
        if (node != null) {
            return node.height;
        }
        return -1;
    }

    private int balanceFactor(TreeNode node){
        return getHeight(node.right) - getHeight(node.left);
    }

    // This assumes that the height of the children are currently accurate
    private void updateHeight(TreeNode node) {
        int leftHeight = getHeight(node.left);
        int rightHeight = getHeight(node.right);
        if (rightHeight > leftHeight) {
            node.height = rightHeight + 1;
        }
        else {
            node.height = leftHeight + 1;
        }
    }

    /* Performs a right rotation and updates heights
    Also returns the new node where the old one used to be
    Necessary, because the parent of node will now point to the wrong thing
    This needs to be updated via the return value */
    private TreeNode rotateRight(TreeNode node) {
        TreeNode leftChild = node.left;

        node.left = leftChild.right;
        leftChild.right = node;

        updateHeight(node);
        updateHeight(leftChild);

        return leftChild;
    }

    // Similarly performs a left rotation
    private TreeNode rotateLeft(TreeNode node) {
        TreeNode rightChild = node.right;

        node.right = rightChild.left;
        rightChild.left = node;

        updateHeight(node);
        updateHeight(rightChild);

        return rightChild;
    }

    // Re-balances a tree below a given node
    private TreeNode rebalance(TreeNode node) {
        int balanceFactor = balanceFactor(node);

        // Left-heavy?
        if (balanceFactor < -1) {
            if (balanceFactor(node.left) > 0) {
                node.left = rotateLeft(node.left);
            }
            node = rotateRight(node);
        }

        // Right-heavy?
        if (balanceFactor > 1) {
            if (balanceFactor(node.right) < 0) {
                node.right = rotateRight(node.right);
            }
            node = rotateLeft(node);
        }

        return node;
    }

    // Recursively updates the weight of a node by moving down first
    // Also returns its own weight
    private int updateWeight(TreeNode node) {
        int leftWeight;
        if (node.left == null) {
            leftWeight = 0;
        }
        else {
            leftWeight = updateWeight(node.left);
        }

        int rightWeight;
        if (node.right == null) {
            rightWeight = 0;
        }
        else {
            rightWeight = updateWeight(node.right);
        }

        node.weight = leftWeight + rightWeight + 1;
        return node.weight;
    }

    // Recursively write the entire content of a tree in preorder travel
    // Starts at some starting node
    private void writeContentHelper(FileOutputStream out, TreeNode node) throws IOException {
        if (node != null) {
            node.writeContent(out);
            writeContentHelper(out, node.left);
            writeContentHelper(out, node.right);
        }
    }


    // Insert Node helper
    public TreeNode insertNodeHelper(long key, byte value, TreeNode node) {
        node = super.insertNodeHelper(key, value, node);

        updateHeight(node);

        return rebalance(node);
    }

    // Writes the content of a whole tree in preorder travel
    public void writeContent(FileOutputStream out) throws IOException {
        updateWeight(root);
        writeContentHelper(out, root);
    }
}
