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
  public static void main(String[] args) {
    var dc = new DynamicCompiler();
    try {
      boolean result = dc.compile("");
      System.out.println(result);
    } catch (ClassNotFoundException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (NoSuchMethodException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (IllegalAccessException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (InvocationTargetException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (InstantiationException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (IllegalArgumentException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (SecurityException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  public boolean compile(String args)
      throws ClassNotFoundException, IOException, NoSuchMethodException, IllegalAccessException,
      InvocationTargetException, InstantiationException, IllegalArgumentException, SecurityException {
        String className = "GeneratedPredicate";
        String sourceCode = """
            import java.util.function.Predicate;
            public class GeneratedPredicate implements Predicate<String> {
              @Override
              public boolean test(String input) {
                  return input.length() > 5; // <- Wstawiony kod
              }
            }
            """;

        // Katalog na skompilowane klasy
        File outputDir = Files.createTempDirectory("_out").toFile();
        outputDir.mkdir();

        // Zapisujemy kod do pliku
        File sourceFile = new File(outputDir, className + ".java");
        try (var writer = new java.io.FileWriter(sourceFile)) {
            writer.write(sourceCode);
        }

        // Kompilacja w locie
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, null);
        Iterable<? extends JavaFileObject> compilationUnits = fileManager.getJavaFileObjects(sourceFile);
        boolean success = compiler.getTask(null, fileManager, null, List.of("-d", outputDir.getPath()), null, compilationUnits).call();

        if (!success) {
          System.err.println("Kompilacja nie powiodła się!");
          return success;
        }

        // Dynamiczne ładowanie klasy
        URLClassLoader classLoader = URLClassLoader.newInstance(new URL[]{outputDir.toURI().toURL()});
        Class<?> loadedClass = Class.forName(className, true, classLoader);

        // Tworzenie instancji klasy
        Object instance = loadedClass.getDeclaredConstructor().newInstance();

        // Rzutowanie na Predicate<String>
        Predicate<String> predicate = (Predicate<String>) instance;

        // Testujemy dynamicznie wygenerowany kod
        System.out.println("Czy 'hello' pasuje? " + predicate.test("hello"));
        System.out.println("Czy 'hellooo' pasuje? " + predicate.test("hellooo"));
        return success;
    }

}
