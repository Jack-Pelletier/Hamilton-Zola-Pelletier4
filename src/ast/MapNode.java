package ast.nodes;

import ast.nodes.SyntaxNode;
import ast.typesystem.types.Type;
import environment.Environment;
import environment.TypeEnvironment;

/**
 * Base implementation of MapNode for Phase 3.
 * This file currently contains only structure and stubs.
 */
public class MapNode extends SyntaxNode
{
    private SyntaxNode func;
    private SyntaxNode listExpr;

    public MapNode(SyntaxNode func, SyntaxNode listExpr, int lineNumber)
    {
        super(lineNumber);
        this.func = func;
        this.listExpr = listExpr;
    }

    @Override
    public Object evaluate(Environment env)
    {
        throw new UnsupportedOperationException("MapNode.evaluate not implemented yet.");
    }

    @Override
    public Type typeOf(TypeEnvironment tenv)
    {
        throw new UnsupportedOperationException("MapNode.typeOf not implemented yet.");
    }

    @Override
    public void displaySubtree(StringBuilder sb, int indent)
    {
        indent(sb, indent);
        sb.append("MapNode\n");

        indent(sb, indent + 1);
        sb.append("Function:\n");
        func.displaySubtree(sb, indent + 2);

        indent(sb, indent + 1);
        sb.append("List:\n");
        listExpr.displaySubtree(sb, indent + 2);
    }
}
