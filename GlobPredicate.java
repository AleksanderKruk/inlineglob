import java.util.function.Predicate;
public class GlobPredicate implements Predicate<String> {
    @Override
    public boolean test(String inputString) {
        return inputString.equals("abcd");
    }
}