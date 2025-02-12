package inline.glob;

import java.io.File;
import java.io.IOError;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroupDir;
import org.stringtemplate.v4.STGroupFile;

/**
 * Hello world!
 *
 */
public class App
{
    public static void main( String[] args ) throws IOException
    {
        // var f = Files.find(Path.of("."), 10, (p, attrs) -> Files.isDirectory(p));
        var templates = new STGroupFile("./target/classes/inline/glob/templates/glob.stg");
        ST test = templates.getInstanceOf("test");
        test.add("name", "HELLO");
        System.out.println(test.render());
        // f.forEach(p -> {
        //     System.out.println(p.toString());
        // });
    }
}
