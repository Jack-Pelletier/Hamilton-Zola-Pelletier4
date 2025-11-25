package ast.nodes;

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
    public Object evaluate(Environment env)
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
}
