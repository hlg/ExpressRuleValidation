package expressrules;

import org.antlr.v4.runtime.RuleContext;

import java.util.HashMap;
import java.util.Map;

public class ExpressDeclarationTableListener extends ExpressBaseListener {

    public Map<String, ExpressParser.Entity_declContext> entityDeclarations = new HashMap<String, ExpressParser.Entity_declContext>();
    public Map<String, ExpressParser.Type_declContext> typeDeclarations = new HashMap<String, ExpressParser.Type_declContext>();
    public Map<String, ExpressParser.Procedure_declContext> procedureDeclarations = new HashMap<String, ExpressParser.Procedure_declContext>();
    public Map<String, ExpressParser.Function_declContext> functionDeclarations = new HashMap<String, ExpressParser.Function_declContext>();

    @Override
    public void enterEntity_decl(ExpressParser.Entity_declContext ctx) {
        entityDeclarations.put(ctx.entity_head().IDENT().getSymbol().getText(), ctx);
    }

    @Override
    public void enterType_decl(ExpressParser.Type_declContext ctx) {
        typeDeclarations.put(ctx.IDENT().getSymbol().getText(), ctx);
    }

    @Override
    public void enterProcedure_decl(ExpressParser.Procedure_declContext ctx) {
        procedureDeclarations.put(ctx.procedure_head().procedure_id().getText(), ctx);    // TODO unify
    }

    @Override
    public void enterFunction_decl(ExpressParser.Function_declContext ctx) {
        functionDeclarations.put(ctx.function_head().function_id().getText(), ctx);
    }
}
