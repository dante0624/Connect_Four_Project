package openingBookHelpers;

import java.io.FileOutputStream;
import java.io.IOException;

// Node in a binary mapping tree, so it has key value pairs
// Can have 0, 1, or 2 children
public class TreeNode {
    public long key;
    public byte value;
    public TreeNode left;
    public TreeNode right;

    // Weight is the total amount of nodes in the subtree (including this node)
    // We only care about this when writing to output for an AVL tree
    public int weight;

    // Height is the distance between this node and the furthest leaf node below
    // We care about this when inserting in an AVL tree
    public int height;

    // Writes the content of a node to a file output stream
    // Only meant to be used by the AVLTreeWriter
    public void writeContent(FileOutputStream out) throws IOException {
        /* The plan for serializing the node
        49 unsigned bits for the key
        6 signed bits for the value
        1 bit for if the right child exists (1 is yes, 0 is no)
        24 unsigned bits for the weight of the left child (0 if it does not exist)
		So 80 bits total, or 10 bytes */
        byte[] data = new byte[10];

        // This is meant to include the key, value, and right child exists
        long firstSevenBytes = key << 7;
        firstSevenBytes += (value << 1) & 0b1111110;
        if (right != null) {
            firstSevenBytes += 1;
        }

        for (int i = 6; i >= 0; i--) {
            data[i] = (byte)(firstSevenBytes & 0xFF);
            firstSevenBytes >>>= 8;
        }

        // Last 3 bytes come from the weight of the left child
        int leftWeight;
        if (left == null) {
            leftWeight = 0;
        }
        else {
            leftWeight = left.weight;
        }

        for (int i = 9; i >= 7; i--) {
            data[i] = (byte)(leftWeight & 0xFF);
            leftWeight >>>= 8;
        }

        out.write(data);
    }

    public TreeNode(long initialKey, byte initialValue) {
        key = initialKey;
        value = initialValue;
        weight = 1;
        height = 0;
    }
}
