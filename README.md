# Inline Glob

This project tries to provide compile-time inlining of `glob` patterns in Java.

# Goals

The library usage I am aiming for is an annotation.
```java
@InlineGlob(glob = "*.txt") PathMatcher pathMatcher;
@InlineGlob(glob = "*.txt") Predicate<String> stringPredicate;
@InlineGlob(glob = "*.txt") Predicate<Path> pathPredicate;
```
The tool itself should be runnable via jar to generate and print the inlined code for the given glob.
```
java -jar inlineglob.jar <glob-to-be-inlined>
```
The generated code should be ideally call-free and as optimal as is feasible.

# Progress

## Glob

Pattern         |  Implemented?    
----------------|---------------
`a`             | ➕             
`[abc]`         | ➕
`[!abc]`        | ➕
`[a-z]`         |
`[!a-z]`         |
`?`             | ➕
`*`             | ➕
`*.{java,py}`   |  
`**`             | 

## Library

Feature         |  Implemented?    
----------------|---------------
`@InlineGlob(glob = "*.txt") PathMatcher pathMatcher;`            |              
`@InlineGlob(glob = "*.txt") Predicate<String> stringPredicate;`  | 
`@InlineGlob(glob = "*.txt") Predicate<Path> pathPredicate;`      | 


# Rationale

This repository is mostly educational. It's main goal is to test the StringTemplate templating engine.
Additionally it touches the ways of classifing unicode characters. 
Finally I plan on running tests to see whether or not inlining speeds up the matching process.



