package inline.glob;

import java.io.File;
import java.io.IOError;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.antlr.runtime.TokenStream;
import org.antlr.v4.runtime.*;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroupDir;
import org.stringtemplate.v4.STGroupFile;

/**
 * Hello world!
 *
 */
public class InlineGlob
{

    public static void main( String[] args ) throws IOException
    {
        var charStream = CharStreams.fromFileName(args[1]);
        var lexer = new GlobLexer(charStream);
        var tokenStream = new CommonTokenStream(lexer);
        var parser = new GlobParser(tokenStream);
        var tree = parser.glob();
        var templates = new STGroupFile("./target/classes/inline/glob/templates/glob.stg");
        ST test = templates.getInstanceOf("test");
        System.out.println(test.render());
    }
}
