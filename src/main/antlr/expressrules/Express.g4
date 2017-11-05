grammar Express;

@header {
    package expressrules;
}

tokens {
	/* id types */
        CONSTANT_IDENT;
        ENTITY_IDENT;
        FUNCTION_IDENT;
        PROCEDURE_IDENT;
        PARAMETER_IDENT;
        SCHEMA_IDENT;
        TYPE_IDENT;
        VARIABLE_IDENT;
        ENUMERATION_IDENT;
        ATTRIBUTE_IDENT;
	ENTITY_ATTR_IDENT;
	TYPE_ATTR_IDENT;
	ENTITY_VAR_IDENT;
	TYPE_VAR_IDENT;
	ENTITY_PARAM_IDENT;
	TYPE_PARAM_IDENT;
	/* id types Express V2 */
	SUBTYPE_CONSTRAINT_ID;

	/* nodes types */
	ACTUAL_PARAMETER_LIST; ADD_LIKE_OP; AGGREGATE_INITIALIZER; 
	AGGREGATE_SOURCE; AGGREGATE_TYPE; AGGREGATION_TYPES; ALGORITHM_HEAD; 
	ALIAS_STMT; ARRAY_TYPE; ASSIGNMENT_STMT; BAG_TYPE; BASE_TYPE; 
	BINARY_TYPE; BOOLEAN_TYPE; BOUND_1; BOUND_2; BOUND_SPEC; 
	BUILT_IN_CONSTANT; BUILT_IN_FUNCTION; BUILT_IN_PROCEDURE; CASE_ACTION;
 	CASE_LABEL; CASE_STMT; COMPOUND_STMT; CONSTANT_BODY; CONSTANT_DECL; 
	CONSTANT_FACTOR; CONSTANT_ID; DECLARATION; DOMAIN_RULE; ELEMENT; 
	ENTITY_HEAD; ENTITY_DECL; ENTITY_BODY; SUBSUPER; SUPERTYPE_CONSTRAINT;
 	ABSTRACT_SUPERTYPE_DECLARATION; SUBTYPE_DECLARATION; EXPLICIT_ATTR; 
	ATTRIBUTE_DECL; ATTRIBUTE_ID; QUALIFIED_ATTRIBUTE; DERIVE_CLAUSE; 
	DERIVED_ATTR; INVERSE_CLAUSE; INVERSE_ATTR; UNIQUE_CLAUSE; UNIQUE_RULE;
 	REFERENCED_ATTRIBUTE; ENTITY_CONSTRUCTOR; ENTITY_ID; 
	ENUMERATION_REFERENCE; ESCAPE_STMT; EXPRESSION; FACTOR; 
	FORMAL_PARAMETER; ATTRIBUTE_QUALIFIER; FUNCTION_CALL; FUNCTION_DECL; 
	FUNCTION_HEAD; FUNCTION_ID; GENERALIZED_TYPES; 
	GENERAL_AGGREGATION_TYPES; GENERAL_ARRAY_TYPE; GENERAL_BAG_TYPE; 
	GENERAL_LIST_TYPE; GENERAL_REF; GENERAL_SET_TYPE; GENERIC_TYPE; 
	GROUP_QUALIFIER; IF_STMT; INCREMENT; INCREMENT_CONTROL; INDEX; INDEX_1;
 	INDEX_2; INDEX_QUALIFIER; INTEGER_TYPE; INTERVAL; INTERVAL_HIGH; 
	INTERVAL_ITEM; INTERVAL_LOW; INTERVAL_OP; LABEL; LIST_TYPE; LITERAL;
	REAL_LITERAL; INTEGER_LITERAL; STRING_LITERAL; 
	LOCAL_DECL; LOCAL_VARIABLE; LOGICAL_EXPRESSION; LOGICAL_LITERAL; 
	LOGICAL_TYPE; MULTIPLICATION_LIKE_OP; NAMED_TYPES; NULL_STMT; 
	NUMBER_TYPE; NUMERIC_EXPRESSION; ONE_OF; PARAMETER; PARAMETER_ID; 
	PARAMETER_TYPE; POPULATION; PRECISION_SPEC; PRIMARY; 
	PROCEDURE_CALL_STMT; PROCEDURE_DECL; PROCEDURE_HEAD; PROCEDURE_ID; 
	QUALIFIABLE_FACTOR; QUALIFIER; QUERY_EXPRESSION; REAL_TYPE; 
	REFERENCE_CLAUSE; REL_OP; REL_OP_EXTENDED; REPEAT_CONTROL; REPEAT_STMT;
	REPETITION; RESOURCE_OR_RENAME; RESOURCE_REF; RETURN_STMT; RULE_DECL;
	RULE_HEAD; RULE_ID; SCHEMA_ID; SCHEMA_BODY; SCHEMA_DECL; 
	INTERFACE_SPECIFICATION; USE_CLAUSE; NAMED_TYPE_OR_RENAME; SELECTOR; 
	SET_TYPE; SIMPLE_EXPRESSION; SIMPLE_FACTOR; SIMPLE_TYPES; SKIP_STMT; 
	STMT; STRING_TYPE; SUBTYPE_CONSTRAINT; SUPERTYPE_EXPRESSION; 
	SUPERTYPE_FACTOR; SUPERTYPE_RULE; SUPERTYPE_TERM; SYNTAX; TERM; 
	TYPE_DECL; UNDERLYING_TYPE; CONSTRUCTED_TYPES; ENUMERATION_TYPE; 
	ENUMERATION_ID; SELECT_TYPE; TYPE_ID; TYPE_LABEL; TYPE_LABEL_ID; 
	UNARY_OP; UNTIL_CONTROL; VARIABLE_ID; WHERE_CLAUSE; WHILE_CONTROL; 
	WIDTH; WIDTH_SPEC; ENTITY_REF; TYPE_REF; ENUMERATION_REF; 
	ATTRIBUTE_REF; CONSTANT_REF; FUNCTION_REF; PARAMETER_REF; VARIABLE_REF;
 	SCHEMA_REF; TYPE_LABEL_REF; PROCEDURE_REF; SIMPLE_ID; ELSE_CLAUSE;
	RENAME_ID;
	/* Express amendment nodes */
	ENUMERATION_ITEMS; ENUMERATION_EXTENSION;
	SELECT_LIST; SELECT_EXTENSION;
	REDECLARED_ATTRIBUTE;
	SUBTYPE_CONSTRAINT_DECL; SUBTYPE_CONSTRAINT_HEAD; SUBTYPE_CONSTRAINT_BODY;
	ABSTRACT_SUPERTYPE; TOTAL_OVER;	
	CONCRETE_TYPES;
	GENERIC_ENTITY_TYPE;
	SCHEMA_VERSION_ID;
	LANGUAGE_VERSION_ID;
}


schema_decl : 'schema' IDENT SEMI schema_body 'end_schema' SEMI ;
schema_body : ( interface_specification )* ( constant_decl )? ( declaration | rule_decl )* ;
// schema_body : declaration* ;
interface_specification : reference_clause | use_clause ; // reference or use other schema, not used in IFC
reference_clause : 'reference from'  IDENT ( LPAREN  resource_or_rename  (  COMMA  resource_or_rename  )*  RPAREN  )?  SEMI;
resource_or_rename : IDENT ( 'as' IDENT )? ;
constant_decl : 'constant'  constant_body ( constant_body )*  'end_constant'  SEMI ;
constant_body : IDENT COLON base_type COLEQ expression SEMI;
declaration : entity_decl |  type_decl |  subtype_constraint_decl |  function_decl |  procedure_decl ; /* TODO: remove subtype_constraint_del bla?, newer express than 1994? */
entity_decl : entity_head entity_body 'end_entity' SEMI ;
entity_head : 'entity' IDENT subsuper? SEMI;

subsuper : supertype_constraint? subtype_declaration? ;
supertype_constraint :  abstract_supertype_declaration |  supertype_rule ;
abstract_supertype_declaration : 'abstract' 'supertype' subtype_constraint?;
supertype_rule :  'supertype' subtype_constraint ;
subtype_constraint :  'of'  LPAREN  supertype_expression  RPAREN ;
supertype_expression :  supertype_factor  ( 'andor' supertype_factor )* ;
supertype_factor : supertype_term  ( 'and' supertype_term )* ;
supertype_term : one_of |  LPAREN  supertype_expression RPAREN | entity_ref ;
one_of : 'oneof' LPAREN  supertype_expression ( COMMA supertype_expression )*  RPAREN ;
subtype_declaration : 'subtype' 'of' LPAREN entity_ref ( COMMA entity_ref )* RPAREN ;

entity_ref : IDENT ;
entity_body : explicit_attr* derive_clause? inverse_clause? unique_clause? where_clause? ;
explicit_attr :  attribute_decl (COMMA attribute_decl)* COLON ('optional')? base_type SEMI;
type_decl: 'type' IDENT ASSIGN underlying_type SEMI (where_clause)? 'end_type' SEMI;
attribute_decl : attribute_id | redeclared_attribute ; 

underlying_type : constructed_types | aggregation_types | simple_types | type_ref ;
constructed_types : enumeration_type | select_type ;
enumeration_type : 'extensible'? 'enumeration' ('of' enumeration_items | enumeration_extension )?;
enumeration_items : LPAREN enumeration_id (COMMA enumeration_id)* RPAREN;
enumeration_extension : 'based_on' type_ref ('with' enumeration_items)? ;
enumeration_id : IDENT ;
select_type : 'extensible'? 'generic_entity'? 'select' (select_list | select_extension )? ;
select_list : LPAREN named_types (COMMA named_types)* RPAREN ;
select_extension : 'based_on' type_ref ('with' select_list)? ;


base_type : concrete_types | generalized_types;
concrete_types : aggregation_types | simple_types | named_types ;
generalized_types : aggregate_type | general_aggregation_types | generic_type | generic_entity_type ;
aggregation_types : array_type | bag_type | list_type | set_type ;
simple_types : binary_type | boolean_type | integer_type | logical_type | number_type | real_type | string_type ;
aggregate_type : 'aggregate' ( COLON type_label )?  'of' parameter_type ;

general_aggregation_types : general_array_type | general_bag_type | general_list_type | general_set_type;
generic_type : 'generic'  ( COLON type_label )? ;
generic_entity_type : 'generic_entity' ;
array_type : 'array' bound_spec 'of' 'optional'? 'unique'? base_type ;
bag_type : 'bag' bound_spec? 'of' base_type ;
list_type : 'list'  bound_spec?  'of' 'unique'?  base_type ;
set_type : 'set' bound_spec? 'of' base_type ;
binary_type : 'binary' width_spec? ;
boolean_type : 'boolean';
integer_type : 'integer' ;
logical_type : 'logical' ;
number_type : 'number' ;
real_type : 'real'  (LPAREN precision_spec RPAREN)? ;
string_type : 'string' width_spec? ;
type_label : IDENT;
parameter_type : generalized_types | named_types | simple_types;
bound_spec : LBRACK  bound_1  COLON  bound_2  RBRACK;
bound_1 : numeric_expression;
bound_2: numeric_expression;
use_clause: 'use' 'from' schema_ref (LPAREN named_type_or_rename (COMMA named_type_or_rename)* RPAREN )? SEMI;

schema_ref : IDENT;
procedure_decl : procedure_head algorithm_head? stmt* 'end_procedure'  SEMI;
procedure_head : 'procedure'  procedure_id (LPAREN 'var'? formal_parameter (SEMI 'var'? formal_parameter)*  RPAREN )?  SEMI;
algorithm_head : declaration* constant_decl? local_decl?;
procedure_id : IDENT;
parameter_id : IDENT;
local_decl : 'local' local_variable local_variable* 'end_local' SEMI;
function_decl : function_head algorithm_head?  stmt stmt* 'end_function' SEMI ;
numeric_expression: simple_expression;
function_head : 'function' function_id (LPAREN formal_parameter (SEMI formal_parameter)*  RPAREN )?  COLON parameter_type SEMI;


subtype_constraint_decl : subtype_constraint_head subtype_constraint_body 'end_subtype_constraint' SEMI ;
rule_decl : rule_head algorithm_head? stmt*  where_clause 'end_rule'  SEMI;
precision_spec : numeric_expression;
width_spec : LPAREN width  RPAREN 'fixed'?;
width : numeric_expression;
named_type_or_rename : named_types ('as' (entity_id | type_id ) )? ;
named_types : entity_ref | type_ref ;
function_id : IDENT;
formal_parameter : parameter_id (COMMA parameter_id)* COLON parameter_type ;
attribute_id : IDENT;
redeclared_attribute : qualified_attribute ( 'renamed' attribute_id )?;
type_ref : IDENT;
general_array_type : 'array' bound_spec? 'of' 'optional'? 'unique'? parameter_type ;
general_bag_type : 'bag' bound_spec? 'of' parameter_type;
general_list_type : 'list' bound_spec? 'of' 'unique'? parameter_type ;
general_set_type : 'set' bound_spec? 'of' parameter_type;
local_variable : variable_id (COMMA variable_id)* COLON parameter_type (COLEQ expression)? SEMI;
variable_id : IDENT ;
rule_head : 'rule' rule_id 'for' LPAREN entity_ref (COMMA entity_ref)* RPAREN SEMI;
rule_id : IDENT ;
where_clause : 'where' domain_rule SEMI (domain_rule SEMI)*;
entity_id : IDENT ;
type_id : IDENT ;

derive_clause : 'derive' derived_attr (derived_attr)* ;
derived_attr : attribute_decl COLON base_type COLEQ expression SEMI ;
inverse_clause : 'inverse' inverse_attr (inverse_attr)* ;
inverse_attr : attribute_decl COLON (('set' | 'bag') bound_spec? 'of')? entity_ref 'for' /* global_ident */ attribute_ref SEMI; 
unique_clause : 'unique' unique_rule SEMI (unique_rule SEMI)*;
unique_rule : (label COLON)? referenced_attribute (COMMA referenced_attribute)*;
label: IDENT;
referenced_attribute : attribute_ref | qualified_attribute ;

simple_expression : term (add_like_op term)*;
term : factor (multiplication_like_op factor)*;
add_like_op : PLUS | MINUS | 'or' | 'xor' ;
multiplication_like_op : STAR | DIVSIGN | 'div' | 'mod' | 'and' | DOUBLEBAR ;
factor : simple_factor (DOUBLESTAR simple_factor)? ;



simple_factor : aggregate_initializer | interval | query_expression | (unary_op? ((LPAREN expression RPAREN) | primary)) | entity_constructor | enumeration_reference ;

unary_op : PLUS | MINUS | 'not';
entity_constructor : entity_ref LPAREN (expression (COMMA expression)* )?  RPAREN ; /* Lookahead needed? */
enumeration_reference : (type_ref  DOT)?  enumeration_ref;
aggregate_initializer : LBRACK (element (COMMA element)* )? RBRACK ;
interval : LCURLY interval_low interval_op interval_item interval_op interval_high RCURLY;
interval_low : simple_expression ;
interval_op : LT | LE ;
interval_item : simple_expression ;
interval_high : simple_expression ;
query_expression : 'query' LPAREN variable_id LTSTAR aggregate_source BAR logical_expression  RPAREN; 
aggregate_source : simple_expression ;
logical_expression : expression ;
primary : literal | qualifiable_factor qualifier*;
literal :  real_literal | logical_literal | integer_literal | STRING ;  // binary_literal is missing, but not appearing in IFC
real_literal : DIGIT+ '.' DIGIT* ('e' (PLUS|MINUS)? DIGIT+)? ;
integer_literal : DIGIT+ ;
logical_literal : 'false' | 'true' | 'unknown' ;
qualifiable_factor : attribute_ref | constant_factor | function_call | population | general_ref ;
attribute_ref : IDENT ;
constant_factor : built_in_constant | constant_ref ;
built_in_constant : 'const_e' | 'pi' | 'self' | QUESTION | STAR ;
constant_ref : IDENT ;
function_call : (built_in_function | function_ref ) LPAREN actual_parameter_list? RPAREN ;  // TODO: this seems to be wrong, parans from parameter list should go here, except when parameterless calls are not allowed or ar done without parans
built_in_function : 'abs' | 'acos' | 'asin' | 'atan' | 'blength' | 'cos' | 'exists' | 'exp' | 'format' | 'hibound' | 'hiindex' | 'length' | 'lobound' | 'loindex' | 'log' | 'log2' | 'log10' | 'nvl' | 'odd' | 'rolesof' | 'sin' | 'sizeof' | 'sqrt' | 'tan' | 'typeof' | 'usedin' | 'value' | 'value_in' | 'value_unique' ; 
function_ref : IDENT ;
actual_parameter_list : parameter (COMMA parameter)*;
parameter: expression;
population: entity_ref ;
general_ref : parameter_ref | variable_ref ;
variable_ref : IDENT ;
parameter_ref : IDENT;
qualifier : attribute_qualifier | group_qualifier | index_qualifier ;
attribute_qualifier : DOT attribute_ref ; /* global_ident */
group_qualifier : BACKSLASH entity_ref ;
index_qualifier : LBRACK index_1 (COLON index_2)? RBRACK ;
index_1: index ;
index_2: index ;
index: numeric_expression ;
enumeration_ref : IDENT;
element : expression (COLON repetition)? ;
repetition : numeric_expression ;
expression : simple_expression  (rel_op_extended  simple_expression)? ;
rel_op_extended : rel_op | 'in' | 'like';
rel_op : LT | GT | LE | GE | LTGT | ASSIGN | COLLTGT | COLEQCOL ;

stmt : alias_stmt | assignment_stmt | case_stmt | compound_stmt | escape_stmt |  if_stmt |  null_stmt |  procedure_call_stmt |  repeat_stmt |  return_stmt |  skip_stmt ;
alias_stmt: 'alias' variable_id 'for' general_ref (qualifier)* SEMI stmt stmt* 'end_alias' ;
assignment_stmt: general_ref qualifier* COLEQ expression SEMI ;
case_stmt: 'case' selector 'of' case_action* 'otherwise' COLON stmt 'end_case' SEMI ;
selector : expression ;
case_action : case_label (COMMA case_label)* COLON stmt;
case_label : expression ;
compound_stmt : 'begin' stmt stmt* 'end' SEMI ;
escape_stmt : 'escape' SEMI ;
if_stmt : 'if' logical_expression 'then' stmt stmt* else_clause? 'end_if' SEMI ;
else_clause: 'else' stmt stmt* ;
null_stmt : SEMI;
procedure_call_stmt : (built_in_procedure | procedure_ref ) LPAREN actual_parameter_list? RPAREN SEMI;
built_in_procedure : 'insert' | 'remove' ;
procedure_ref : IDENT ;
repeat_stmt : 'repeat' repeat_control SEMI stmt stmt* 'end_repeat' SEMI ;
repeat_control: increment_control? while_control? until_control? ;
increment_control: variable_id COLEQ bound_1 'to' bound_2 ('by' increment)? ;
increment : numeric_expression ;
while_control: 'while' logical_expression ;
until_control: 'until' logical_expression ;
return_stmt : 'return' (LPAREN expression RPAREN)? SEMI;
skip_stmt : 'skip' SEMI ;

subtype_constraint_head : 'subtype_constraint' subtype_constraint_id 'for' entity_ref SEMI;
subtype_constraint_id : IDENT;
subtype_constraint_body : abstract_supertype? total_over? (supertype_expression SEMI)?;


abstract_supertype: 'abstract' 'supertype' SEMI ;
total_over: 'total_over' LPAREN entity_ref (COMMA entity_ref)* RPAREN SEMI;
domain_rule : (label COLON)? logical_expression;
qualified_attribute : 'self' group_qualifier attribute_qualifier;



/* Express LEXICAL RULES  */

COMMENT	:	'(*' .*? '*)' -> skip ;
LINECOMMENT : '--'~[\n\r]* -> skip ;
LANG_VERSION : 'iso standard 10303 part (11) version (4)' ;
SEMI :	';' ;
QUESTION :	'?' ;
LPAREN :	'(' ;
RPAREN :	')' ;
LBRACK :	'[' ;
RBRACK :	']' ;
LCURLY :	'{' ;
RCURLY :	'}' ;
BACKSLASH :	'\\' ;
BAR :	'|' ;
AMPERSAND :	'&' ;
COLON :	':' ;
COLEQ :	':=' ;
COLEQCOL :	':=:' ;
COLLTGT :	':<>:' ;
COMMA :	',' ;
DOT :	'.' ;
ASSIGN :	'=' ;
LT :	'<' ;
GT :	'>' ;
LE :	'<=' ;
GE :	'>=' ;
DIVSIGN :	'/' ;
PLUS :	'+' ;
MINUS :	'-' ;
STAR :	'*' ;
AT :	'@' ;
WS :	[ \t\f\r\n]+ -> skip ;
LTSTAR : '<*' ;
LTGT : '<>' ;
DOUBLESTAR : '**' ;
DOUBLEBAR  : '||' ;
STRING : '\''	(~'\'')*	'\''	;
IDENT :	(LETTER|'_') (LETTER|'A'..'Z'|'_'|DIGIT)*	;
LETTER : 'a'..'z'|'A'..'Z' ;
DIGIT	:	'0'..'9' ;
