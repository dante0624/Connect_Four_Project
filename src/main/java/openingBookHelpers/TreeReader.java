package openingBookHelpers;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.Arrays;

import miscHelpers.Utils;

/* Class takes in a serialized tree as a File object in its constructor.
   Class supports get and iterate methods.
   Both of these open a stream at the start, then close it before returning
   or raising errors. As such this class can be reused once initialized. */
public class TreeReader implements Iterable<Long> {
    private File bookFile;
	private FileInputStream bookIn;
    private final String readErrorMessage = "Could not read 10 bytes at once";

    private void openStream() {
        try {
            bookIn = new FileInputStream(bookFile);
        }
        catch (IOException e) {
            throw new RuntimeException("Error opening file at " + bookFile.toString());
        }
    }
    private void closeStream() {
        try {
            bookIn.close();
        }
        catch (IOException ex) {
            throw new RuntimeException("Failed to close file " + bookFile.toString());
        }
    }

    // These raise exceptions, but make sure to close the a stream first
    // We only throw IO Exceptions in get, so we can close directly
    private void throwIOSafely(String errorMessage) throws IOException {
        bookIn.close();
        throw new IOException(errorMessage);
    }
    // We only throw IllegalArgumentExceptions in get, when we can't find a key
    private void noKeyFoundError(long searchKey) throws IllegalArgumentException, IOException {
        bookIn.close();
        throw new IllegalArgumentException("No key of " + searchKey + " found");
    }

    // We throw runtime errors in the iterable, so we use the closeStream method
    private void throwRuntimeSafely(String errorMessage) {
        closeStream();
        throw new RuntimeException(errorMessage);
    }

    // Helper method, reconstructs a key from bytes of data
    private long reconstructKey(byte[] currEntry) {
        long key = (long) currEntry[0] & 0xFF;
        for (int i = 1; i <= 5; i++) {
            key = (key << 8) + ((long) currEntry[i] & 0xFF);
        }

        // Add the MSB from currEntry[6]
        key <<= 1;
        if (currEntry[6] < 0) {
            key += 1;
        }
        return key;
    }

    // Returns an eval when given a key and a depth
    // Throws an illegal argument exception if the key is not found
    public int get(long searchKey) throws IllegalArgumentException, IOException {
		openStream();

        byte[] currEntry = new byte[10];
        long key;
        int value, leftWeight;
        boolean rightChildExists;

        while (bookIn.available() >= 10) {
            if (bookIn.read(currEntry, 0, 10) != 10) {
                throwIOSafely(readErrorMessage);
            }

            key = reconstructKey(currEntry);

            // Shift to fill up the MSB of 32 integer bits, then shift back
            // Ensures that negatives values are negative, and positives values are positive
            value = (currEntry[6] << 25) >> 26;

            if (searchKey == key) {
                bookIn.close();
                return value;
            }

            leftWeight = (int) currEntry[7] & 0xFF;
            for (int i = 8; i < 10; i++) {
                leftWeight = (leftWeight << 8) + ((int) currEntry[i] & 0xFF);
            }


			// Checking left subtree just involves going to the next node
            if (searchKey < key) {
                if (leftWeight == 0) {
                    noKeyFoundError(searchKey);
                }
                continue;
            }

            rightChildExists = (currEntry[6] & 1) == 1;

			// Checking right subtree involves skipping the entire left subtree
            if (!rightChildExists) {
                noKeyFoundError(searchKey);
            }

            if (bookIn.skip(leftWeight * 10) != leftWeight * 10) {
                throwIOSafely("Error skipping bytes from file");
            }
        }

        noKeyFoundError(searchKey);

        // This will never happen because of the error above, but my LSP wants it
        return 0;
    }

    // Lets us iterate over all keys in a file
    @Override
    public Iterator<Long> iterator() {
		openStream();

        return new Iterator<>() {
            @Override
            public boolean hasNext() {
                boolean hasData = false;

                try {
                    hasData = bookIn.available() >= 10;
                    if (!hasData) {
                        closeStream();
                    }
                } catch (IOException e) {
                    throwRuntimeSafely("Error checking availability from file");
                }
                return hasData;
            }

            @Override
            public Long next() {
                byte[] currEntry = new byte[10];

                try {
                    if (bookIn.read(currEntry, 0, 10) != 10) {
                        throwRuntimeSafely(readErrorMessage);
                    }
                } catch (IOException e) {
                    throwRuntimeSafely(readErrorMessage);
                }

                return reconstructKey(currEntry);
            }
        };
    }

	// Returns the maximum depth of book that has been solved, and all depths below have also been solved
	// So a return value of 11 indicates that all books (0-11) have been solved, but 12 has not
	// Returns -1 if depth 0 has not been solved
	public static int getMaxBookDepth() {
        File books = Paths.get(Utils.getProjectRoot(), Utils.bookResources).toFile();
		String[] fileNames = books.list();
		int[] depths = new int[fileNames.length];
		for (int i = 0; i < fileNames.length; i++) {
			depths[i] = Integer.parseInt(fileNames[i].replaceAll("[^0-9]", ""));
		}
		Arrays.sort(depths);

		int maxDepth = -1;
		for (int depth: depths) {
			if (depth == maxDepth + 1) {
				maxDepth = depth;
			}
			else {
				break;
			}
		}
		return maxDepth;
	}

    public TreeReader(File initialFile) {
		bookFile = initialFile;
    }
}
