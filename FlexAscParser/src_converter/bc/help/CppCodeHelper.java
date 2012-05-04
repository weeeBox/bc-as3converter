package bc.help;

import bc.lang.BcTypeNode;
import bc.lang.BcVectorTypeNode;

public class CppCodeHelper extends BcCodeHelper
{
	private static final String PREFIX_REF = "_ref";
	private static final String NEW = "AS_NEW";
	private static final String STRING_LITERAL = "ASL";
	
	private static final String IS_OPERATOR = "AS_IS";
	
	@Override
	public String construct(String type, Object initializer)
	{
		return String.format("%s(%s, (%s))", NEW, type(type), initializer);
	}

	@Override
	public String operatorIs(Object lhs, Object rhs)
	{
		return String.format("%s(%s, %s)", IS_OPERATOR, lhs, rhs);
	}

	@Override
	public String literalNull()
	{
		return "AS_NULL";
	}
	
	@Override
	public String literalString(String value)
	{
		return String.format("%s(\"%s\")", STRING_LITERAL, BcStringUtils.replaceEscapes(value));
	}

	@Override
	protected String vectorType(BcVectorTypeNode vectorType)
	{
		return "foo";
	}

	@Override
	protected String constructVector(BcVectorTypeNode vectorType, Object initializer)
	{
		return "foo";
	}
	
	@Override
	public String cast(Object expr, BcTypeNode type)
	{
		return String.format("(%s)(%s)", typeRef(type), expr);
	}

	@Override
	public String memberSelector(Object target, Object selector)
	{
		return String.format("%s->%s", target, selector);
	}
	
	@Override
	public String staticSelector(Object target, Object selector)
	{
		return String.format("%s::%s", target, selector);
	}
	
	public String typeRef(BcTypeNode bcType)
	{
		return typeRef(bcType.getName());
	}

	public String typeRef(String type)
	{
		if (isBasicType(type))
		{
			return type(type);
		}
		return String.format("%s%s", type(type), PREFIX_REF);
	}
	
	@Override
	public String paramDecl(BcTypeNode type, String identifier)
	{
		if (isBasicType(type))
		{
			return String.format("%s %s", type(type), identifier(identifier));
		}
		return String.format("const %s& %s", typeRef(type), identifier(identifier));
	}
	
	@Override
	public String varDecl(BcTypeNode type, String identifier)
	{
		return String.format("%s %s", typeRef(type), identifier(identifier));
	}
	
	public String include(String filename)
	{
		return String.format("#include \"%s\"", filename);
	}
}