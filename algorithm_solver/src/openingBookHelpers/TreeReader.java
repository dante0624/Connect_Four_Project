package openingBookHelpers;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Iterator;

public class TreeReader implements Iterable<Long> {
    // Constants about directory structure
    public String filePath;

    // Constant error message
    private final String readErrorMessage = "Could not read 10 bytes at once";


    // Helper Methods related to the stream
    // Raise RuntimeException instead of IOException because of how iterators work
    private FileInputStream openStream() {
        // Read the correct file
        try {
            return new FileInputStream(filePath);
        }
        catch (IOException e) {
            throw new RuntimeException("Error opening file at " + filePath);
        }
    }
    private void closeStream(FileInputStream bookIn) {
        try {
            bookIn.close();
        }
        catch (IOException ex) {
            throw new RuntimeException("Failed to close file " + filePath);
        }
    }

    // These raise exceptions, but make sure to close the a stream first
    // We only throw IO Exceptions in get, so we can close directly
    private void throwIOSafely(String errorMessage, FileInputStream bookIn) throws IOException {
        bookIn.close();
        throw new IOException(errorMessage);
    }
    // We only throw IllegalArgumentExceptions in get, when we can't find a key
    private void noKeyFoundError(
            FileInputStream bookIn,
            long searchKey
    ) throws IllegalArgumentException, IOException {
        bookIn.close();
        throw new IllegalArgumentException("No key of " + searchKey + " found");
    }

    // We throw runtime errors in the iterable, so we use the closeStream method
    private void throwRuntimeSafely(String errorMessage, FileInputStream bookIn) {
        closeStream(bookIn);
        throw new RuntimeException(errorMessage);
    }

    // Helper method, reconstructs a key from bytes of data
    private long reconstructKey(byte[] currEntry) {
        long key = (long) currEntry[0] & 0xFF;
        for (int i = 1; i <= 5; i++) {
            key = (key << 8) + ((long) currEntry[i] & 0xFF);
        }

        // Finally, add the MSB from currEntry[6]
        key <<= 1;
        if (currEntry[6] < 0) {
            key += 1;
        }
        return key;
    }

    // Returns an eval when given a key and a depth
    // Throws an illegal argument exception if the key is not found
    public int get(long searchKey) throws IllegalArgumentException, IOException {
        FileInputStream bookIn = openStream();

        // These are collectively make up each entry
        byte[] currEntry = new byte[10];
        long key;
        int value, leftWeight;
        boolean rightChildExists;

        // Iterate through the tree, looking for our key
        while (bookIn.available() >= 10) {
            // Read the current entry
            if (bookIn.read(currEntry, 0, 10) != 10) {
                throwIOSafely(readErrorMessage, bookIn);
            }

            // Reconstruct the key
            key = reconstructKey(currEntry);

            // Reconstruct the value
            // Shift to fill up the MSB of 32 integer bits, then shift back
            // Ensures that negatives values are negative, and positives values are positive
            value = (currEntry[6] << 25) >> 26;

            // Reconstruct the boolean
            rightChildExists = (currEntry[6] & 1) == 1;

            // Reconstruct the left child weight
            leftWeight = (int) currEntry[7] & 0xFF;
            for (int i = 8; i < 10; i++) {
                leftWeight = (leftWeight << 8) + ((int) currEntry[i] & 0xFF);
            }

            // found the key, return the value
            if (searchKey == key) {
                bookIn.close();
                return value;
            }

            // Check the left child
            if (searchKey < key) {
                if (leftWeight == 0) {
                    noKeyFoundError(bookIn, searchKey);
                }
                continue;
            }

            // check the right child
            if (!rightChildExists) {
                noKeyFoundError(bookIn, searchKey);
            }

            if (bookIn.skip(leftWeight * 10) != leftWeight * 10) {
                throwIOSafely("Error skipping bytes from file", bookIn);
            }
        }

        // This is if we break out of the loop because the file runs out of 10 byte blocks
        noKeyFoundError(bookIn, searchKey);

        // This will never happen because of the error above, but JVM wants it
        return 0;
    }

    // Lets us iterate over all keys in a file
    @Override
    public Iterator<Long> iterator() {
        return new Iterator<>() {
            private final FileInputStream bookIn = openStream();

            @Override
            public boolean hasNext() {
                // Assume it does not, unless we find that it does
                boolean hasData = false;
                try {
                    hasData = bookIn.available() >= 10;
                    if (!hasData) {
                        closeStream(bookIn);
                    }
                } catch (IOException e) {
                    throwRuntimeSafely("Error checking availability from file", bookIn);
                }
                return hasData;
            }

            @Override
            public Long next() {
                byte[] currEntry = new byte[10];

                // Read the current entry
                try {
                    if (bookIn.read(currEntry, 0, 10) != 10) {
                        throwRuntimeSafely(readErrorMessage, bookIn);
                    }
                } catch (IOException e) {
                    throwRuntimeSafely(readErrorMessage, bookIn);
                }

                // Reconstruct the current key
                return reconstructKey(currEntry);
            }
        };
    }

    // Simple constructor
    public TreeReader(String initialFileName) {
        filePath = initialFileName;
    }
}
