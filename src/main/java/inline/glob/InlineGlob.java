package inline.glob;

import java.io.File;
import java.io.IOError;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;

import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.xpath.XPath;
import org.antlr.v4.runtime.tree.pattern.ParseTreePatternMatcher;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroupDir;
import org.stringtemplate.v4.STGroupFile;

/**
 * Hello world!
 *
 */
public class InlineGlob
{
    interface CharClassNode {
        public String getType();
    }

    public static class AnyChar implements CharClassNode {
        public String getType() {
            return "any_char";
        }
    }

    public static class Star implements CharClassNode {
        public String getType() {
            return "star";
        }
    }

    public static class NegatedCharacterClass implements CharClassNode {
        private final List<String> chars;
        public String getType() {
            return "negated_character_class";
        }
        public NegatedCharacterClass(List<String> chars_) {
            chars = chars_;
        }
        public List<String> getChars() {
            return chars;
        }
    }

    public static class CharacterClass implements CharClassNode {
        private final List<String> chars;
        public CharacterClass(List<String> chars_) {
            chars = chars_;
        }
        public String getType() {
            return "character_class";
        }
        public List<String> getChars() {
            return chars;
        }
    }

    public static class Char implements CharClassNode {
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


    public static ArrayList<CharClassNode> mapToClasses(Collection<ParseTree> quants, Parser parser) {
        var charClasses = new ArrayList<CharClassNode>();
        for (final var quant : quants) {
            final String text = quant.getText();
            final boolean oneChar = XPath.findAll(quant, "//LBRACKET", parser).size() == 0;
            if (oneChar) {
                switch (text) {
                    case "?" -> charClasses.add(new AnyChar());
                    case "*" -> charClasses.add(new Star());
                    default -> charClasses.add(new Char(text));
                }
            } else {
                System.out.println();
                final boolean isNegated = XPath.findAll(quant, "//NEG", parser).size() != 0;
                final List<String> chars = XPath.findAll(quant, "//CHAR", parser)
                        .stream().map(ParseTree::toString).toList();
                if (isNegated) {
                    charClasses.add(new NegatedCharacterClass(chars));
                } else {
                    charClasses.add(new CharacterClass(chars));
                }
            }
        }
        return charClasses;
    }


    public static final java.util.regex.Pattern multistar = Pattern.compile("\\*{2,}");
    public static String optimizePattern(String pattern) {
        return multistar.matcher(pattern).replaceAll("*");
    }

    public static void main(String[] args) throws IOException
    {
        final String pattern = InlineGlob.optimizePattern(args[0]);
        final var charStream = CharStreams.fromString(pattern);
        final var lexer = new GlobLexer(charStream);
        final var tokenStream = new CommonTokenStream(lexer);
        final var parser = new GlobParser(tokenStream);
        final var tree = parser.glob();


        final long star_node_count = XPath.findAll(tree, "//STAR", parser).size();
        final long any_node_count = XPath.findAll(tree, "//ANY", parser).size();
        final long bracketed_class_count = XPath.findAll(tree, "//LBRACKET", parser).size();
        final var templates = new STGroupFile("./target/classes/inline/glob/templates/glob.stg");
        String result = null;
        if (star_node_count == 0
                && any_node_count == 0
                && bracketed_class_count == 0)
        {
            final ST trivial = templates.getInstanceOf("glob_trivial");
            trivial.add("string", pattern);
            result = trivial.render();
        }
        else if (star_node_count == 0) {
            final ST withoutStar = templates.getInstanceOf("glob_without_star");
            final Collection<ParseTree> quants = XPath.findAll(tree, "//quant", parser);
            final long length_contraint = pattern.length() - star_node_count;
            withoutStar.add("pattern_length", length_contraint);
            final var charClasses = InlineGlob.mapToClasses(quants, parser);
            withoutStar.add("char_classes", charClasses);
            result = withoutStar.render();
        }  else {
            final ST withStar = templates.getInstanceOf("glob_with_star");
            final Collection<ParseTree> quants = XPath.findAll(tree, "//quant", parser);
            withStar.add("pattern_length", pattern.length());
            final var charClasses = InlineGlob.mapToClasses(quants, parser);
            withStar.add("char_classes", charClasses);
            result = withStar.render();
        }
        System.out.println(result);
    }

}
