package expressrules;

import antlr.ParseTree;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.RuleNode;
import org.bimserver.emf.IdEObject;
import org.bimserver.models.ifc2x3tc1.IfcObject;
import org.bimserver.models.ifc4.*;
import org.bimserver.models.ifc4.impl.IfcActorRoleImpl;
import org.eclipse.emf.ecore.EDataType;
import org.eclipse.emf.ecore.EStructuralFeature;

import java.util.Stack;

public class ExpressRuleVisitor extends ExpressBaseVisitor<Value> {

    private EntityAdapter obj;
    private Value currentScope;

    public ExpressRuleVisitor(EntityAdapter obj) {
        this.obj = obj;
        this.currentScope = new Entity(obj);
    }

    @Override
    public Value visitSchema_decl(ExpressParser.Schema_declContext ctx) {
        return visit(ctx.schema_body());
    }

    @Override
    public Value visitSchema_body(ExpressParser.Schema_bodyContext ctx) {
        log("found " + ctx.declaration().size() + " declarations");
        return visitChildren(ctx); // TODO check all entity type declarations of object under validation, can also take them from decl table
    }

    private void log(String s) {
        System.out.println("[Validator] " + s);
    }

    @Override
    public Value visitDeclaration(ExpressParser.DeclarationContext ctx) {
        // entity_decl |  type_decl |  subtype_constraint_decl |  function_decl |  procedure_decl ;
        boolean entityDeclWithObjClass = ctx.entity_decl().entity_head().IDENT().getText().equals(obj.getExpressClassName());
        return entityDeclWithObjClass ? visit(ctx.entity_decl()) : null;
        // TODO plus function declarations, they are in decl table  (type declarations needed?)
    }

    @Override
    public Value visitEntity_decl(ExpressParser.Entity_declContext ctx) {
        // entity_head entity_body 'end_entity' SEMI (entity_head : 'entity' IDENT subsuper? SEMI)
        return visit(ctx.entity_body());
    }

    @Override
    public Value visitEntity_body(ExpressParser.Entity_bodyContext ctx) {
        return visit(ctx.where_clause());
    }

    @Override
    public Value visitWhere_clause(ExpressParser.Where_clauseContext ctx) {
        boolean valid = true;
        for (ExpressParser.Domain_ruleContext domainRuleContext : ctx.domain_rule()){
            valid = valid && ((Simple)visit(domainRuleContext)).getBoolean();

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
            if(ctx.rel_op_extended().getText().toLowerCase().equals("in")) return eval1.in(eval2);  // simple val in aggregation
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
        return visit(ctx.primary());  // TODO alternatives
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
        currentScope = visit(ctx.qualifiable_factor());
        for (ExpressParser.QualifierContext qualifier : ctx.qualifier()){
            currentScope = visit(qualifier);
        }
        return currentScope;
    }

    @Override
    public Value visitQualifiable_factor(ExpressParser.Qualifiable_factorContext ctx) {
        // attribute_ref | constant_factor | function_call | population | general_ref    // no cust constants in IFC4
        return visit(ctx.attribute_ref()); // TODO alternatives
    }
    @Override
    public Value visitQualifier(ExpressParser.QualifierContext ctx) {
        // attribute_qualifier | group_qualifier | index_qualifier;
        return visit(ctx.attribute_qualifier()); // TODO alternatives
    }

    @Override
    public Value visitAttribute_qualifier(ExpressParser.Attribute_qualifierContext ctx) {
        return visit(ctx.attribute_ref());
    }

    @Override
    public Value visitAttribute_ref(ExpressParser.Attribute_refContext ctx) {
        Value resolved = currentScope.resolveRef(ctx.IDENT().getText());
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
