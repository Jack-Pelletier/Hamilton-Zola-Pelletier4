package ast.nodes;

import ast.EvaluationException;
import ast.typesystem.TypeException;
import ast.typesystem.inferencer.Inferencer;
import ast.typesystem.types.Type;
import environment.Environment;
import environment.TypeEnvironment;

/**
 * Base implementation of FoldNode (foldl and foldr) for Phase 3.
 * This file currently contains only structure and stubs.
 */
public class FoldNode extends SyntaxNode
{
    private SyntaxNode func;
    private SyntaxNode init;
    private SyntaxNode listExpr;
    private boolean left; // true = foldl, false = foldr

    public FoldNode(SyntaxNode func, SyntaxNode init, SyntaxNode listExpr, boolean left, int lineNumber)
    {
        super(lineNumber);
        this.func = func;
        this.init = init;
        this.listExpr = listExpr;
        this.left = left;
    }

    @Override
    public Object evaluate(Environment env) throws EvaluationException
    {
        throw new UnsupportedOperationException("FoldNode.evaluate not implemented yet.");
    }

    @Override
    public Type typeOf(TypeEnvironment tenv)
    {
        throw new UnsupportedOperationException("FoldNode.typeOf not implemented yet.");
    }

    @Override
    public void displaySubtree(StringBuilder sb, int indent)
    {
        indent(sb, indent);
        sb.append(left ? "FoldLNode\n" : "FoldRNode\n");

        indent(sb, indent + 1);
        sb.append("Function:\n");
        func.displaySubtree(sb, indent + 2);

        indent(sb, indent + 1);
        sb.append("Init:\n");
        init.displaySubtree(sb, indent + 2);

        indent(sb, indent + 1);
        sb.append("List:\n");
        listExpr.displaySubtree(sb, indent + 2);
    }

    private void indent(StringBuilder sb, int indent) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'indent'");
    }

    @Override
    public Type typeOf(TypeEnvironment tenv, Inferencer inferencer) throws TypeException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'typeOf'");
    }

    @Override
    public void displaySubtree(int indentAmt) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'displaySubtree'");
    }
}
