package expressrules;

import org.bimserver.models.ifc4.IfcRoot;

public class ExpressRuleVisitor extends ExpressBaseVisitor<Value> {

    private IfcRoot obj;

    public ExpressRuleVisitor(IfcRoot obj) {
        this.obj = obj;
    }

    @Override
    public Value visitDomain_rule(ExpressParser.Domain_ruleContext ctx) {
        String ruleName = ctx.label().getText(); // for logging, otherwise delete this method
        return visitLogical_expression(ctx.logical_expression());
    }

    @Override
    public Boolean visitExpression(ExpressParser.ExpressionContext ctx) {
        Value eval1 = visitSimple_expression(ctx.simple_expression(0));
        if (ctx.simple_expression().size()>1) {
            Value eval2 = visitSimple_expression(ctx.simple_expression(1));
            ctx.rel_op_extended().getText().equals("in");  // TODO
            ctx.rel_op_extended().getText().equals("like");   // TODO
            if (ctx.rel_op_extended().rel_op().ASSIGN()!=null) return eval1.eq(eval2);
            else if (ctx.rel_op_extended().rel_op().LT()!=null)  return eval1.lt(eval2);
            else if(ctx.rel_op_extended().rel_op().GT()!=null) return eval1.gt(eval2) ;
            else if(ctx.rel_op_extended().rel_op().LE()!=null) return eval1.le(eval2);
            else if(ctx.rel_op_extended().rel_op().GE()!=null) return eval1.ge(eval2);
            else if(ctx.rel_op_extended().rel_op().LTGT()!=null) return eval1.eq(eval2).not();
            else if(ctx.rel_op_extended().rel_op().COLEQCOL()!=null) return eval1.eq(eval2);
            else if(ctx.rel_op_extended().rel_op().COLLTGT()!=null) return eval1.eq(eval2).not();
        }
        return eval1.getBoolean();
    }

    @Override
    public Value visitSimple_expression(ExpressParser.Simple_expressionContext ctx) {
        Value result = visitTerm(ctx.term(0));
        assert ctx.term().size() == ctx.add_like_op().size() +1;
        for(int i=0; i<ctx.add_like_op().size(); i++){
            if( ctx.add_like_op(i).getText().equals("-") ){ result.subtract(visitTerm(ctx.term(i + 1))); }
            else if( ctx.add_like_op(i).getText().equals("+") ){ result.add(visitTerm(ctx.term(i+1))); }
            // TODO  'or' | 'xor'
        }
        return result;
    }

    @Override
    public Value visitTerm(ExpressParser.TermContext ctx) {
        Value result = visitFactor(ctx.factor(0));
        assert ctx.factor().size() == ctx.multiplication_like_op().size()+1;
        for(int i=0; i<ctx.multiplication_like_op().size(); i++) {
            if (ctx.multiplication_like_op(i).getText().equals("*")) {result.multiply(visitFactor(ctx.factor(i + 1)));}
            else if (ctx.multiplication_like_op(i).getText().equals("/")) {result.divide(visitFactor(ctx.factor(i + 1)));}
            // TODO 'div' | 'mod' | 'and' | DOUBLEBAR
        }
        return result;
    }

    @Override
    public Value visitSimple_factor(ExpressParser.Simple_factorContext ctx) {
        // aggregate_initializer | interval | query_expression | (unary_op? ((LPAREN expression RPAREN) | primary)) | entity_constructor | enumeration_reference ;
        // TODO change grammar for unary_op expression
        return visitChildren(ctx);
    }

    @Override
    public Value visitAggregate_initializer(ExpressParser.Aggregate_initializerContext ctx) {
        Aggregation result = new Aggregation();
        for(ExpressParser.ElementContext element : ctx.element()) {
            result.addElement(visitExpression(element.expression()));
        }
        return result;

    }

    @Override
    public  Value visitFactor (ExpressParser.FactorContext ctx){
        Value result = visitSimple_factor(ctx.simple_factor(0));
        if(ctx.simple_factor().size()>1){
            result = result.power(visitSimple_factor(ctx.simple_factor(1)));
        }
        return result;
    }

}
