package serializationHelpers;

import java.nio.file.Path;
import java.nio.file.Paths;

public class Utils {
	final public static String projectFolder = "algorithm_solver";
	final public static String bookResources = "src/main/resources/openingBook";
	final public static String tableResources = "src/main/resources/transTableSerialized";
    final public static String testResources = "src/test/resources";
	final public static String frontEndResources = "src/main/resources/frontEnd";

	public static String getProjectRoot() {
		Path path = Paths.get(System.getProperty("user.dir"));

		while (path != null && !path.toString().endsWith(projectFolder)) {
			path = path.getParent();
		}
		if (path == null) {
			throw new RuntimeException("Need to run this script somewhere within " + projectFolder);
		}
		return path.toString();
	}
}
