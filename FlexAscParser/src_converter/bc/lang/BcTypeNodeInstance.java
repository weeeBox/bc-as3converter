package bc.lang;

public class BcTypeNodeInstance
{
	private BcTypeNode type;
	private boolean qualified;

	public BcTypeNodeInstance(BcTypeNode type, boolean qualified)
	{
		this.type = type;
		this.qualified = qualified;
	}
	
	public BcTypeNode getType()
	{
		return type;
	}
	
	public BcTypeName getTypeName()
	{
		return type.getTypeName();
	}
	
	public String getQualifier()
	{
		return type.getQualifier();
	}
	
	public String getQualifiedName()
	{
		return type.getQualifiedName();
	}

	public String getName()
	{
		return type.getName();
	}
	
	public String getFullname()
	{
		return isQualified() ? getQualifiedName() : getName();
	}
	
	public boolean isQualified()
	{
		return qualified;
	}
	
	public boolean isIntegral()
	{
		return type.isIntegral();
	}
}
