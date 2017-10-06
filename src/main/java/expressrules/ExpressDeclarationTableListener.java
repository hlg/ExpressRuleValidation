package expressrules;

import org.antlr.v4.runtime.RuleContext;
import expressrules.ExpressBaseListener;
import expressrules.ExpressParser;

import java.util.HashMap;
import java.util.Map;

public class ExpressDeclarationTableListener extends ExpressBaseListener {

    public Map<String, RuleContext> declarationTable = new HashMap<String, RuleContext>();

    @Override
    public void enterEntity_decl(ExpressParser.Entity_declContext ctx) {
        declarationTable.put(ctx.entity_head().IDENT().getSymbol().getText(), ctx);
    }

    @Override
    public void enterType_decl(ExpressParser.Type_declContext ctx) {
        declarationTable.put(ctx.IDENT().getSymbol().getText(), ctx);
    }

    @Override
    public void enterProcedure_decl(ExpressParser.Procedure_declContext ctx) {
        declarationTable.put(ctx.procedure_head().procedure_id().getText(), ctx);    // TODO unify
    }

    @Override
    public void enterFunction_decl(ExpressParser.Function_declContext ctx) {
        declarationTable.put(ctx.function_head().function_id().getText(), ctx);
    }
}
