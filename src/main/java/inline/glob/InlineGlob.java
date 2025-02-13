package inline.glob;

import java.io.File;
import java.io.IOError;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;

import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
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
    interface CharClass {
        public String getType();
    }

    public static class AnyChar implements CharClass {
        public String getType() {
            return "any_char";
        }
    }

    public static class Char implements CharClass {
        final String char_;
        @Override
        public String getType() {
            return "char";
        }
        public String getChar() {
            return char_;
        }
        Char(final String char__) {
            char_ = char__;
        }
    }

    // public static class Char implements CharClass {
    //     final String type = "char";
    //     final String char_;
    //     Char(final String char__) {
    //         char_ = char__;
    //     }
    // }

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
            final ST withoutStar = templates.getInstanceOf("glob_without_star");
            final Collection<ParseTree> quants = XPath.findAll(tree, "//quant", parser);
            withoutStar.add("pattern_length", pattern.length());
            // withoutStar.inspect();
            var charClasses = new ArrayList<CharClass>();
            for (final var quant : quants) {
                final String text = quant.getText();
                switch (text) {
                    case "?" -> charClasses.add(new AnyChar());
                    default -> charClasses.add(new Char(text));
                }
            }
            withoutStar.add("char_classes", charClasses);
            withoutStar.inspect();
            result = withoutStar.render();
            // withStar.add()
        }  else {

        }
        System.out.println(result);

        // final ST trivial = templates.getInstanceOf("glob_trivial");


        // System.out.println(test.render());
    }

}
