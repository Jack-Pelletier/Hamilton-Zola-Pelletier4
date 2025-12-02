package ast.typesystem.types;
import ast.typesystem.inferencer.Inferencer;


public final class FunType extends Type
{
    private final Type paramType;
    private final Type returnType;

    public FunType(Type paramType, Type returnType)
    {
        this.paramType = paramType;
        this.returnType = returnType;
    }

    public Type getParamType()
    {
        return paramType;
    }

    public Type getReturnType()
    {
        return returnType;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) return true;
        if (!(obj instanceof FunType)) return false;
        FunType f = (FunType) obj;
        return paramType.equals(f.paramType) &&
               returnType.equals(f.returnType);
    }

    /**
     * Gets the type as a string.
     * 
     * @return a the type as a string.
     */
    @Override
    public String toString()
    {
        // Externalize the full function type so type variables normalize
        // (t0, t1, ...) exactly like the test suite expects.
        Inferencer inf = new Inferencer();
        Type ext = inf.getSubstitutions().externalize(this);

        FunType f = (FunType) ext;

        return f.paramType.toString() + " -> " + f.returnType.toString();
    }
}