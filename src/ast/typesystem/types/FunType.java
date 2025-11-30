package ast.typesystem.types;

import java.util.Objects;

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

    @Override
    public String toString()
    {
        return "(" + paramType + " -> " + returnType + ")";
    }
}
