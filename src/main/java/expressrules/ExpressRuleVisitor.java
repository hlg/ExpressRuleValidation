package expressrules;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Stack;

public class ExpressRuleVisitor extends ExpressBaseVisitor<Value> {

    private EntityAdapter obj;
    private Value qualifierScope ;
    private Map<String, ExpressParser.Entity_declContext> entityDeclarations;

    private Stack<Aggregate> stack = new Stack<Aggregate>();

    public ExpressRuleVisitor(EntityAdapter obj) {
        this.obj = obj;
    }

    public ExpressRuleVisitor(EntityAdapter obj, Map<String, ExpressParser.Entity_declContext> entityDeclarations) {
        this.obj = obj;
        this.entityDeclarations = entityDeclarations;
    }

    @Override
    public Value visitSchema_decl(ExpressParser.Schema_declContext ctx) {
        return visit(ctx.schema_body());
    }

    @Override
    public Value visitSchema_body(ExpressParser.Schema_bodyContext ctx) {
        log("found " + ctx.declaration().size() + " declarations");
        if(obj!=null) {
            // TODO check rules in all entity declarations of object under validation, can also take them from decl table
            // TODO check rules of all types that are used for attributes of the entity
            // TODO plus function declarations, they are in decl table  (type declarations needed?)
            // no subtype constraints or procudure declarations in IFC4
            boolean isValid = true;
            for(String superType : obj.getTypes()){
                Value validationResult = visit(entityDeclarations.get(superType));
                isValid = isValid && ((Simple) validationResult).getBoolean();
            }
            return new Simple(isValid);
        } else {
            return visitChildren(ctx);
        }
    }

    private void log(String s) {
        System.out.println("[Validator] " + s);
    }

    @Override
    public Value visitDeclaration(ExpressParser.DeclarationContext ctx) {
        // entity_decl |  type_decl |  subtype_constraint_decl |  function_decl |  procedure_decl ;
        return ctx.entity_decl()!=null ? visit(ctx.entity_decl()) : null ;
    }

    @Override
    public Value visitEntity_decl(ExpressParser.Entity_declContext ctx) {
        // entity_head entity_body 'end_entity' SEMI (entity_head : 'entity' IDENT subsuper? SEMI)
        return visit(ctx.entity_body());
    }

    @Override
    public Value visitEntity_body(ExpressParser.Entity_bodyContext ctx) {
        return ctx.where_clause()!=null ? visit(ctx.where_clause()) : null;
    }

    @Override
    public Value visitWhere_clause(ExpressParser.Where_clauseContext ctx) {
        boolean valid = true;
        for (ExpressParser.Domain_ruleContext domainRuleContext : ctx.domain_rule()){
            Simple validationResult = (Simple) visit(domainRuleContext);
            valid = valid && validationResult.getBoolean();

        }
        return new Simple(valid);
    }

    @Override
    public Value visitDomain_rule(ExpressParser.Domain_ruleContext ctx) {
        String ruleName = ctx.label().getText(); // for logging, otherwise delete this method
        log("checking rule: " + ruleName);
        return visit(ctx.logical_expression());
    }

    @Override
    public Value visitLogical_expression(ExpressParser.Logical_expressionContext ctx) {
        return visit(ctx.expression());
    }

    @Override
    public Value visitExpression(ExpressParser.ExpressionContext ctx) {
        Value eval1 = visit(ctx.simple_expression(0));
        if (ctx.simple_expression().size()>1) {
            Value eval2 = visit(ctx.simple_expression(1));
            if (ctx.rel_op_extended().getText().toLowerCase().equals("in")) return eval1.in(eval2);  // simple val in aggregation
            if (ctx.rel_op_extended().getText().toLowerCase().equals("like")) throw new RuntimeException("not implemented");   // not used in IFC4 express
            if (ctx.rel_op_extended().rel_op().ASSIGN()!=null) return eval1.eq(eval2);
            else if (ctx.rel_op_extended().rel_op().LT()!=null)  return eval1.lt(eval2);
            else if(ctx.rel_op_extended().rel_op().GT()!=null) return eval1.gt(eval2) ;
            else if(ctx.rel_op_extended().rel_op().LE()!=null) return eval1.le(eval2);
            else if(ctx.rel_op_extended().rel_op().GE()!=null) return eval1.ge(eval2);
            else if(ctx.rel_op_extended().rel_op().LTGT()!=null) return eval1.neq(eval2);
            else if(ctx.rel_op_extended().rel_op().COLEQCOL()!=null) return eval1.eqi(eval2);  // aggregations
            else if(ctx.rel_op_extended().rel_op().COLLTGT()!=null) return eval1.neqi(eval2); //aggregations
        }
        return eval1;
    }

    @Override
    public Value visitUnique_rule(ExpressParser.Unique_ruleContext ctx) {
        return null; // TODO;

    }

    @Override
    public Value visitSimple_expression(ExpressParser.Simple_expressionContext ctx) {
        Value result = visit(ctx.term(0));
        log("checking simple expression: " + ctx.getText());
        assert ctx.term().size() == ctx.add_like_op().size() +1;
        for(int i=0; i<ctx.add_like_op().size(); i++){
            if( ctx.add_like_op(i).MINUS()!=null ){ result = result.subtract(visit(ctx.term(i + 1))); }
            else if( ctx.add_like_op(i).PLUS()!=null ){ result = result.add(visit(ctx.term(i + 1))); }
            // TODO  'or' | 'xor'
        }
        return result;
    }

    @Override
    public Value visitTerm(ExpressParser.TermContext ctx) {
        Value result = visitFactor(ctx.factor(0));
        assert ctx.factor().size() == ctx.multiplication_like_op().size()+1;
        for(int i=0; i<ctx.multiplication_like_op().size(); i++) {
            if (ctx.multiplication_like_op(i).getText().equals("*")) {
                result = result.multiply(visit(ctx.factor(i + 1)));
            }
            else if (ctx.multiplication_like_op(i).getText().equals("/")) {result.divide(visit(ctx.factor(i + 1)));}
            // TODO 'div' | 'mod' | 'and' | DOUBLEBAR
        }
        return result;
    }

    @Override
    public Value visitSimple_factor(ExpressParser.Simple_factorContext ctx) {
        // aggregate_initializer | interval | query_expression | (unary_op? ((LPAREN expression RPAREN) | primary)) | entity_constructor | enumeration_reference ;
        return ctx.primary()!= null ? visit(ctx.primary()) : null;  // TODO alternatives
    }

    @Override
    public Value visitAggregate_initializer(ExpressParser.Aggregate_initializerContext ctx) {
        Aggregate result = new Aggregate();
        for(ExpressParser.ElementContext element : ctx.element()) {
            result.addValue(visit(element.expression()));
        }
        return result;

    }

    @Override
    public Value visitPrimary(ExpressParser.PrimaryContext ctx) {
        // primary : literal | qualifiable_factor qualifier*;
        if(ctx.literal()!= null) return visit(ctx.literal());
        if(obj== null) return new Simple(0.);     // for testing IFC4 coverage
        qualifierScope = new Entity(obj);
        qualifierScope = visit(ctx.qualifiable_factor());
        for (ExpressParser.QualifierContext qualifier : ctx.qualifier()){
            qualifierScope = visit(qualifier);
        }
        return qualifierScope;
    }

    @Override
    public Value visitConstant_factor(ExpressParser.Constant_factorContext ctx) {
        // built_in_constant | constant_ref
        return visitChildren(ctx);
    }

    @Override
    public Value visitBuilt_in_constant(ExpressParser.Built_in_constantContext ctx) {
        if("const_e".equals(ctx.getText())) return new Simple(Math.E);
        else if ("pi".equals(ctx.getText())) return new Simple(Math.PI);
        else if ("self".equals(ctx.getText())) return null;
        else if (ctx.STAR()!=null || ctx.QUESTION() != null) log("found * or ? constant, currently unhandled"); return null;
    }

    @Override
    public Value visitConstant_ref(ExpressParser.Constant_refContext ctx) {
        log("found constant reference, currently unhandled: " + ctx.IDENT());
        return null;
    }

    @Override
    public Value visitQualifiable_factor(ExpressParser.Qualifiable_factorContext ctx) {
        // attribute_ref | constant_factor | function_call | population | general_ref    // no custom constants in IFC4
        return visitChildren(ctx);
    }

    @Override
    public Value visitFunction_call(ExpressParser.Function_callContext ctx) {
        stack.push( (Aggregate) visit(ctx.actual_parameter_list()));
        return visit(ctx.built_in_function()); // TODO function_ref
    }

    @Override
    public Value visitActual_parameter_list(ExpressParser.Actual_parameter_listContext ctx) {
        List<Value> parameters = new ArrayList<Value>();
        for (ExpressParser.ParameterContext parameter : ctx.parameter()){
            parameters.add(visit(parameter.expression()));
        }
        return new Aggregate(parameters);
    }

    @Override
    public Value visitBuilt_in_function(ExpressParser.Built_in_functionContext ctx) {
        return Functions.valueOf(ctx.getText().toUpperCase()).evaluate(stack.pop().value);
    }


    @Override
    public Value visitFunction_ref(ExpressParser.Function_refContext ctx) {
        return null; // TODO:
    }

    @Override
    public Value visitVariable_ref(ExpressParser.Variable_refContext ctx) {
        log("found variable ref " + ctx.IDENT().getText()); // TODO
        return null;
    }

    @Override
    public Value visitParameter_ref(ExpressParser.Parameter_refContext ctx) {
        log("found parameter ref " + ctx.IDENT().getText()); // TODO
        return null;
    }

    @Override
    public Value visitPopulation(ExpressParser.PopulationContext ctx) {
        log("found population context, currently unhandled: " + ctx.entity_ref().IDENT().getText());
        return null;
    }



    @Override
    public Value visitQualifier(ExpressParser.QualifierContext ctx) {
        // attribute_qualifier | group_qualifier | index_qualifier;
        return visitChildren(ctx);
    }

    @Override
    public Value visitGroup_qualifier(ExpressParser.Group_qualifierContext ctx) {
        // TODO: check whether group qualifier can be ignored (no ambiguous cases in IFC4)
        log("group qalifier found, not handeled: " + ctx.getText()); // should pop and push scope
        return null;
    }

    @Override
    public Value visitIndex_qualifier(ExpressParser.Index_qualifierContext ctx) {
        Value start = visit(ctx.index_1().index().numeric_expression());
        if (ctx.index_2()!=null){
            Value end = visit(ctx.index_2().index().numeric_expression());
            return qualifierScope.resolveIndex(start, end);
        }
        return qualifierScope.resolveIndex(start, start);
    }

    @Override
    public Value visitAttribute_qualifier(ExpressParser.Attribute_qualifierContext ctx) {
        return visit(ctx.attribute_ref());
    }

    @Override
    public Value visitAttribute_ref(ExpressParser.Attribute_refContext ctx) {
        Value resolved = qualifierScope.resolveRef(ctx.IDENT().getText());
        log("resolved reference " + ctx.IDENT().getText() + " - result = " + resolved.toString());
        return resolved;
    }

    @Override
    public Value visitLiteral(ExpressParser.LiteralContext ctx) {
        if(ctx.STRING()!=null) return new Simple(ctx.STRING().getText()); // TODO: use visitTerminal?
        else return visitChildren(ctx);
    }



    @Override
    public Value visitLogical_literal(ExpressParser.Logical_literalContext ctx) {
        log("found literal BOOLEAN: " + ctx.getText());
        return new Simple(Boolean.parseBoolean(ctx.getText()));
    }

    @Override
    public Value visitReal_literal(ExpressParser.Real_literalContext ctx) {
        log("found literal REAL: " + ctx.getText());
        return new Simple(Double.parseDouble(ctx.getText()));
    }

    @Override
    public Value visitInteger_literal(ExpressParser.Integer_literalContext ctx) {
        log("found literal INTEGER: " + ctx.getText());
        return new Simple(Double.parseDouble(ctx.getText()));  // TODO do Integers need separate treatment?
    }

    @Override
    public  Value visitFactor (ExpressParser.FactorContext ctx){
        Value result = visit(ctx.simple_factor(0));
        if(ctx.simple_factor().size()>1){
            result = result.power(visit(ctx.simple_factor(1)));
        }
        return result;
    }

    @Override
    protected Value aggregateResult(Value aggregate, Value nextResult) {
        if(aggregate!=null && nextResult!=null) log("found multiple children while processing alternatives, using only last");
        return (nextResult == null) ? aggregate : nextResult;
    }
}
