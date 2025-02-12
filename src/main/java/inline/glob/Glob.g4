grammar Glob;
glob: quant+;
quant: ANY | STAR | class;
class: CHAR | LBRACKET NEG? (range | CHAR)+ RBRACKET;
range: CHAR DASH CHAR;

ANY: '?';
STAR: '*';
LBRACKET: '[';
RBRACKET: ']';
NEG: '!';
DASH: '-';
CHAR: .;
