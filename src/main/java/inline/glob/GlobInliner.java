package inline.glob;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.xpath.XPath;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroupFile;
import java.nio.file.*;


public class GlobInliner
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
        private final List<CharClassNode> chars;
        private final CharClassNode nextStar;


        public Star(List<CharClassNode> chars, CharClassNode nextStar) {
            this.chars = chars;
            this.nextStar = nextStar;
        }

        public CharClassNode getNextStar() {
            return nextStar;
        }

        public List<CharClassNode> getChars() {
            return chars;
        }

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

    public static String inline(String glob) {
        final String pattern = GlobInliner.optimizePattern(glob);
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
            final var charClasses = GlobInliner.mapToClasses(quants, parser);
            withoutStar.add("char_classes", charClasses);
            result = withoutStar.render();
        } else {
            final ST withStar = templates.getInstanceOf("glob_with_star");
            final Collection<ParseTree> quants = XPath.findAll(tree, "//quant", parser);
            final var prefixPlusStars = GlobInliner.mapToStars(quants, parser);
            withStar.add("pattern_length", pattern.length());
            withStar.add("prefix_chars", prefixPlusStars.prefixChars);
            withStar.add("first_star", prefixPlusStars.firstStar);
            result = withStar.render();
        }
        return result;
    }


    public static ArrayList<CharClassNode> mapToClasses(Collection<ParseTree> quants, Parser parser) {
        var charClasses = new ArrayList<CharClassNode>();
        for (final var quant : quants) {
            CharClassNode charClass = mapToClass(parser, quant);
            charClasses.add(charClass);
        }
        return charClasses;
    }


    private static CharClassNode mapToClass(Parser parser, final ParseTree quant) {
        final String text = quant.getText();
        final boolean oneChar = XPath.findAll(quant, "//LBRACKET", parser).size() == 0;
        if (oneChar) {
            return switch (text) {
                case "?" -> new AnyChar();
                default -> new Char(text);
            };
        } else {
            final boolean isNegated = XPath.findAll(quant, "//NEG", parser).size() != 0;
            final List<String> chars = XPath.findAll(quant, "//CHAR", parser)
                    .stream().map(ParseTree::toString).toList();
            if (isNegated) {
                return (new NegatedCharacterClass(chars));
            } else {
                return (new CharacterClass(chars));
            }
        }
    }




    final static Predicate<ParseTree> isStar = q -> q.getText().equals("*");
    final static Predicate<ParseTree> isNotStar = isStar.negate();


    public record PrefixCharsWithFirstStar(List<CharClassNode> prefixChars, CharClassNode firstStar) {
    };
    public static PrefixCharsWithFirstStar mapToStars(Collection<ParseTree> quants, Parser parser) {
        List<CharClassNode> prefixChars = quants.stream()
            .takeWhile(isNotStar).map(q->mapToClass(parser, q)).toList();
        final var firstStar = makeStar(prefixChars.size() + 1, quants, parser);
        return new PrefixCharsWithFirstStar(prefixChars, firstStar);
    }

    public static CharClassNode makeStar(int nodeIndex, Collection<ParseTree> quants, Parser parser) {
        if (nodeIndex >= quants.size()) {
            return null;
        }
        List<CharClassNode> chars = quants.stream().skip(nodeIndex)
            .takeWhile(isNotStar).map(q->mapToClass(parser, q)).toList();
        var veryNextStar = makeStar(nodeIndex + chars.size() + 1, quants, parser);
        var nextStar = new Star(chars, veryNextStar);
        return nextStar;
    }




    public static final java.util.regex.Pattern multistar = Pattern.compile("\\*{2,}");
    public static String optimizePattern(String pattern) {
        return multistar.matcher(pattern).replaceAll("*");
    }

    public static void main(String[] args) throws IOException
    {
        final String removedEscapedStars = args[0].replace("\\\\*", "*");
        final String inlinedGlob = GlobInliner.inline(removedEscapedStars);
        @InlineGlob(glob = "*.txt")
        Matcher fm;


        System.out.println(inlinedGlob);
    }

}
