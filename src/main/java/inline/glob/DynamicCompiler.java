package inline.glob;
import javax.tools.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.util.*;
import java.util.function.Predicate;

public class DynamicCompiler {

  public Class<?> compile(String className, String code)
      throws ClassNotFoundException, IOException, NoSuchMethodException, IllegalAccessException,
      InvocationTargetException, InstantiationException, IllegalArgumentException, SecurityException {
        // Katalog na skompilowane klasy
        File outputDir = Files.createTempDirectory("_out").toFile();
        outputDir.mkdir();

        // Zapisujemy kod do pliku
        File sourceFile = new File(outputDir, className + ".java");
        try (var writer = new java.io.FileWriter(sourceFile)) {
            writer.write(code);
        }

        // Kompilacja w locie
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, null);
        Iterable<? extends JavaFileObject> compilationUnits = fileManager.getJavaFileObjects(sourceFile);
        boolean success = compiler.getTask(null, fileManager, null, List.of("-d", outputDir.getPath()), null, compilationUnits).call();

        if (!success) {
          System.err.println("Kompilacja nie powiodła się!");
          return null;
        }
        URLClassLoader classLoader = URLClassLoader.newInstance(new URL[]{outputDir.toURI().toURL()});
        Class<?> loadedClass = Class.forName(className, true, classLoader);
        return loadedClass;
    }

}
