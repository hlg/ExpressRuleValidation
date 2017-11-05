package expressrules;

import java.util.*;

public class ExpressRuleVisitor extends ExpressBaseVisitor<Value> {

    private EntityAdapter obj;
    private Value qualifierScope ;
    private Map<String, ExpressParser.Entity_declContext> entityDeclarations;
    private Map<String, List<String>> enumerationTypeDeclarations;
    private Map<String, ExpressParser.Function_declContext> functionDeclarations;

    private Stack<Aggregate> stack = new Stack<Aggregate>();
    private Stack<Map<String, Value>> stackFrames = new Stack<Map<String, Value>>();
    private boolean functionReturn = false;

    public ExpressRuleVisitor(EntityAdapter obj) {
        this.obj = obj;
    }

    public ExpressRuleVisitor(EntityAdapter obj, Map<String, ExpressParser.Entity_declContext> entityDeclarations, Map<String, List<String>> enumerationTypeDeclarations) {
        this.obj = obj;
        this.entityDeclarations = entityDeclarations;
        this.enumerationTypeDeclarations = enumerationTypeDeclarations;
    }

    public ExpressRuleVisitor(EntityAdapter obj, Map<String, ExpressParser.Entity_declContext> entityDeclarations, Map<String, List<String>> enumerationTypeDeclarations, Map<String, ExpressParser.Function_declContext> functionDeclarations){
        this(obj, entityDeclarations, enumerationTypeDeclarations);
        this.functionDeclarations = functionDeclarations;
    }

    @Override
    public Value visitSchema_decl(ExpressParser.Schema_declContext ctx) {
        return visit(ctx.schema_body());
    }

    @Override
    public Value visitSchema_body(ExpressParser.Schema_bodyContext ctx) {
        log("found " + ctx.declaration().size() + " declarations");
        if(obj!=null) {
            // TODO check rules of all types that are used for attributes of the entity?
            // TODO plus function declarations, they are in decl table  (type declarations needed?)
            // no subtype constraints or procudure declarations in IFC4
            boolean isValid = true;
            for(String superType : obj.getTypes()){
                Value validationResult = (entityDeclarations.containsKey(superType))? visit(entityDeclarations.get(superType)): new Simple(true);
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
        return ctx.where_clause()!=null ? visit(ctx.where_clause()) : new Simple(true);
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
        Value result = visit(ctx.logical_expression());
        log(ruleName + (((Simple)result).getBoolean()? " valid" : " error"));
        return result;
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
        log("evaluating simple expression: " + ctx.getText());
        Value result = visit(ctx.term(0));
        assert ctx.term().size() == ctx.add_like_op().size() +1;
        for(int i=0; i<ctx.add_like_op().size(); i++){
            if( ctx.add_like_op(i).MINUS()!=null ){ result = result.subtract(visit(ctx.term(i + 1))); }
            else if( ctx.add_like_op(i).PLUS()!=null ){ result = result.add(visit(ctx.term(i + 1))); }
            else if ("or".equals(ctx.add_like_op(i).getText().toLowerCase())) {result = result.or(visit(ctx.term(i + 1))); }
            else if ("xor".equals(ctx.add_like_op(i).getText().toLowerCase())) { result = result.xor(visit(ctx.term(i+1))); }
        }
        return result;
    }

    @Override
    public Value visitTerm(ExpressParser.TermContext ctx) {
        Value result = visitFactor(ctx.factor(0));
        assert ctx.factor().size() == ctx.multiplication_like_op().size()+1;
        for(int i=0; i<ctx.multiplication_like_op().size(); i++) {
            ExpressParser.Multiplication_like_opContext operator = ctx.multiplication_like_op(i);
            String operatorTxt = ctx.multiplication_like_op(i).getText().toLowerCase();
            Value operand = visit(ctx.factor(i + 1));
            if (operator.STAR()!= null) {  result = result.multiply(operand); }
            else if (operator.DIVSIGN()!=null) {result = result.divide(operand);}
            else if (operatorTxt.equals("div")){ result = result.intDiv(operand);}
            else if (operatorTxt.equals("mod")){ result = result.modulo(operand);}
            else if (operatorTxt.equals("and")){ result = result.and(operand);}
            else if (operator.DOUBLEBAR()!=null){ result = result.combine(operand); }  // complex entity constructor
            // TODO 'div' | 'mod' | 'and' | DOUBLEBAR
        }
        return result;
    }

    @Override
    public Value visitSimple_factor(ExpressParser.Simple_factorContext ctx) {
        // aggregate_initializer | interval | query_expression | (unary_op? ((LPAREN expression RPAREN) | primary)) | entity_constructor | enumeration_reference ;
        if (ctx.expression() != null) {
            if (ctx.unary_op()!= null) {
                if (ctx.unary_op().MINUS()!= null) return new Simple(0.).subtract(visit(ctx.expression()));
                if (ctx.unary_op().PLUS() != null) return new Simple(0.).add(visit(ctx.expression()));
                if ("not".equals(ctx.unary_op().getText().toLowerCase())) return new Simple(!((Simple)visit(ctx.expression())).getBoolean());
            }
            return visit(ctx.expression());
        }
        if (ctx.primary()!= null) return visit(ctx.primary());
        if (ctx.aggregate_initializer()!=null) return visit(ctx.aggregate_initializer());

        return null; // TODO alternatives
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
        ExpressParser.Attribute_refContext attribute_ref = ctx.qualifiable_factor().attribute_ref();
        if(attribute_ref!=null && enumerationTypeDeclarations.containsKey(attribute_ref.IDENT().getText())){
            assert(ctx.qualifier().size()==1);
            assert(ctx.qualifier(0).attribute_qualifier()!=null);
            String enumValue = ctx.qualifier(0).attribute_qualifier().attribute_ref().IDENT().getText();
            assert(enumerationTypeDeclarations.get(attribute_ref.IDENT().getText()).contains(enumValue));
            return new Simple(enumValue);
        }
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
        if("const_e".equals(ctx.getText().toLowerCase())) return new Simple(Math.E);
        else if ("pi".equals(ctx.getText().toLowerCase())) return new Simple(Math.PI);
        else if ("self".equals(ctx.getText().toLowerCase())) return null;
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
        // general ref is parsed as attribute_ref and resolved there, population is not handled
        return visitChildren(ctx);
    }

    @Override
    public Value visitFunction_call(ExpressParser.Function_callContext ctx) {
        stack.push( (Aggregate) visit(ctx.actual_parameter_list()));
        if(ctx.built_in_function()!=null) return visit(ctx.built_in_function());
        return visit(ctx.function_ref());
    }

    @Override
    public Value visitFunction_ref(ExpressParser.Function_refContext ctx) {
        return visit(functionDeclarations.get(ctx.IDENT().getText()));
    }

    @Override
    public Value visitFunction_decl(ExpressParser.Function_declContext ctx) {
        log("entering custom function: "+ctx.function_head().function_id().IDENT().getText());
        Aggregate parameters = stack.pop();
        // assert parameters.value.size()==ctx.function_head().formal_parameter().sum{ it.parameter_id().size() }
        int i = 0;
        Map<String, Value> scope = new HashMap<String, Value>();
        if (!stackFrames.empty()) scope.putAll(stackFrames.peek()); // theoretically, the stack could also contain declarations and there should be a global base stack frame with the function and entity type declarations
        stackFrames.push(scope);
        for (ExpressParser.Formal_parameterContext param : ctx.function_head().formal_parameter()){
            for (ExpressParser.Parameter_idContext paramId: param.parameter_id()) {
                scope.put(paramId.IDENT().getText(), parameters.value.get(i++));
            }
        }
        Value result = new Simple(null);
        for(ExpressParser.StmtContext stmt: ctx.stmt()){
            result = visit(stmt);
            if(functionReturn) break;
        }
        stackFrames.pop();
        return result;
    }

    @Override
    public Value visitStmt(ExpressParser.StmtContext ctx) {
        //  | assignment_stmt | case_stmt | compound_stmt | escape_stmt |  if_stmt |  null_stmt |  procedure_call_stmt |  repeat_stmt |  skip_stmt
        // done: alias_stmt, return_stmt,
        return visitChildren(ctx);
    }

    @Override
    public Value visitReturn_stmt(ExpressParser.Return_stmtContext ctx) {
        functionReturn = true;
        return visit(ctx.expression());
    }

    @Override
    public Value visitAlias_stmt(ExpressParser.Alias_stmtContext ctx) {
        throw new NotImplementedException("alias_stmt");
    }

    @Override
    public Value visitAssignment_stmt(ExpressParser.Assignment_stmtContext ctx) {
        qualifierScope = new Entity(obj);
        qualifierScope = visit(ctx.expression());
        for (ExpressParser.QualifierContext qualifier : ctx.qualifier()){
            qualifierScope = visit(qualifier);
        }
        stackFrames.peek().put(ctx.general_ref().getText(), qualifierScope);
        return new Simple(null); // return assigned value? spec does not say anything
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
        // general_ref is parsed as attribute_ref and is handled here as well
        String refLabel = ctx.IDENT().getText();
        if(!stackFrames.isEmpty() && stackFrames.peek().containsKey(ctx.IDENT().getText())){
            return stackFrames.peek().get(refLabel);
        }
        Value resolved = qualifierScope.resolveRef(refLabel);
        log("resolved reference " + refLabel + " = " + resolved);
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
