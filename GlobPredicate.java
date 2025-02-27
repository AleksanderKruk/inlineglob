import java.util.function.Predicate;
public class GlobPredicate implements Predicate<String> {
    @Override
    public boolean test(String inputString) {
        final int input_length = inputString.length();
        int i = 0;
        if (input_length != 5) {
            return false;
        }
        if (inputString.charAt(i) != 'a'
             && inputString.charAt(i) != 'b'
             && inputString.charAt(i) != 'c') {
            return false;
        }
        i++;
        return true;
    }
}