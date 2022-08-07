package sg.edu.nus.comp.cs4218.testutils;

import sg.edu.nus.comp.cs4218.Environment;

import java.lang.reflect.Field;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SuppressWarnings("PMD") // Not required to check for given test from the prof
public class TestEnvironmentUtil {

    private static final String CURRENT_DIR_FIELD = "currentDirectory";
    private static Class<?> environmentClass;

    private static String retrievePackageNameForClassName(String className) {
        try (Stream<Path> filesWalk = Files.walk(Paths.get("src"))) {

            List<String> result = filesWalk.map(Path::toString)
                    .filter(s -> s.contains(className))
                    .collect(Collectors.toList());

            Path path = Paths.get(result.get(0));
            Optional<String> packageDeclarationLine = Files.lines(path).findFirst();

            if (packageDeclarationLine.isPresent()) {
                return packageDeclarationLine.get().replaceAll("package |;", "");
            }

            System.err.println("Package declaration not present in " + className);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return "";
    }

    // Adapted from https://stackoverflow.com/questions/28678026/how-can-i-get-all-class-files-in-a-specific-package-in-java
    private static List<Class<?>> getClassesInPackage(String packageName) {
        String path = packageName.replaceAll("\\.", File.separator);
        List<Class<?>> classes = new ArrayList<>();
        String[] classPathEntries = System.getProperty("java.class.path").split(
                System.getProperty("path.separator")
        );
        String name;
        for (String classpathEntry : classPathEntries) {
            if (classpathEntry.endsWith(".jar")) {
                continue;
            }
            try {
                File base = new File(classpathEntry + File.separatorChar + path);
                for (File file : base.listFiles()) {
                    name = file.getName();
                    if (name.endsWith(".class")) {
                        name = name.substring(0, name.length() - 6);
                        classes.add(Class.forName(packageName + "." + name));
                    }
                }
            } catch (Exception ex) {
                // Silence is gold
            }
        }

        return classes;
    }

    private static void getEnvironmentClass() {
        String packageName = retrievePackageNameForClassName("Environment");
        List<Class<?>> classes = getClassesInPackage(packageName);

        for (Class<?> packageClass : classes) {
            if (packageClass.getName().contains("Environment")) {
                environmentClass = packageClass;
                break;
            }
        }
    }

    public static String getCurrentDirectory() {
        return Environment.currentDirectory;
    }

    public static void setCurrentDirectory(String directory) {
        Environment.setCurrentDirectory(directory);
    }

    public static Class<?> getApplicationClass(String className) throws NoClassDefFoundError {
        String packageName = retrievePackageNameForClassName(className);
        List<Class<?>> classes = getClassesInPackage(packageName);

        if (classes.isEmpty()) {
            throw new NoClassDefFoundError("Cannot find " + className);
        }

        for (Class<?> packageClass : classes) {
            if (packageClass.getName().equals(packageName + "." + className)) {
                return packageClass;
            }
        }

        throw new NoClassDefFoundError("Cannot find " + className);
    }
}
