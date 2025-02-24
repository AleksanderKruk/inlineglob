package inline.glob;
import javax.tools.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;

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
        String className = "HelloWorld";
        String sourceCode = """
            public class HelloWorld {
                public void sayHello() {
                    System.out.println("Hello, dynamically compiled world!");
                }
            }
            """;

        // Katalog na skompilowane klasy
        File outputDir = new File("out");
        outputDir.mkdir();

        // Zapis kodu do pliku
        File sourceFile = new File(outputDir, className + ".java");
        try (FileWriter writer = new FileWriter(sourceFile)) {
            writer.write(sourceCode);
        }

        // Kompilacja pliku
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, null);
        Iterable<? extends JavaFileObject> compilationUnits = fileManager.getJavaFileObjects(sourceFile);
        boolean success = compiler.getTask(null, fileManager, null, List.of("-d", outputDir.getPath()), null, compilationUnits).call();

        if (!success) {
            System.err.println("Kompilacja nie powiodła się!");
            return false;
        }

        // Dynamiczne ładowanie klasy
        URLClassLoader classLoader = URLClassLoader.newInstance(new URL[]{outputDir.toURI().toURL()});
        Class<?> loadedClass = Class.forName(className, true, classLoader);

        // Utworzenie instancji i wywołanie metody
        Object instance = loadedClass.getDeclaredConstructor().newInstance();
        Method method = loadedClass.getMethod("sayHello");
        method.invoke(instance);
        return success;
    }

    // private static class InMemoryJavaFileObject extends SimpleJavaFileObject {
    //     private final String sourceCode;

    //     protected InMemoryJavaFileObject(String className, String sourceCode)
    //     {
    //       super(java.net.URI.create("string:///"
    //             + className.replace('.', '/')
    //             + JavaFileObject.Kind.SOURCE.extension),
    //             Kind.SOURCE);
    //       this.sourceCode = sourceCode;
    //     }

    //     @Override
    //     public CharSequence getCharContent(boolean ignoreEncodingErrors) {
    //         return sourceCode;
    //     }
    // }

}
