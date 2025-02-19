package inline.glob;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface InlineGlob {
    public String glob() default "";
}
