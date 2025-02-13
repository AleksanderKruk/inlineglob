package inline.glob;

import java.io.File;
import java.io.IOError;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.antlr.runtime.TokenStream;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.xpath.XPath;
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
        final String pattern = args[0];
        final var charStream = CharStreams.fromString(pattern);
        final var lexer = new GlobLexer(charStream);
        final var tokenStream = new CommonTokenStream(lexer);
        final var parser = new GlobParser(tokenStream);
        final var tree = parser.glob();
        final long star_node_count = XPath.findAll(tree, "//STAR", parser).size();
        final long any_node_count = XPath.findAll(tree, "//ANY", parser).size();
        final var templates = new STGroupFile("./target/classes/inline/glob/templates/glob.stg");
        String result = null;
        if (star_node_count == 0 && any_node_count == 0) {
            final ST trivial = templates.getInstanceOf("glob_trivial");
            trivial.add("string", pattern);
            result = trivial.render();
        }
        else if (star_node_count == 0) {
            final ST withStar = templates.getInstanceOf("with_star");
            result = withStar.render();
            // withStar.add()
        }  else {

        }
        System.out.println(result);

        // final ST trivial = templates.getInstanceOf("glob_trivial");


        // System.out.println(test.render());
    }

}
