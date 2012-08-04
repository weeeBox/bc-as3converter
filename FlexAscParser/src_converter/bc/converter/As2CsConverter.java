package bc.converter;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import bc.code.ListWriteDestination;
import bc.code.WriteDestination;
import bc.help.BcCodeHelper;
import bc.help.CsCodeHelper;
import bc.lang.BcArgumentsList;
import bc.lang.BcClassDefinitionNode;
import bc.lang.BcFuncParam;
import bc.lang.BcFuncRegister;
import bc.lang.BcFunctionDeclaration;
import bc.lang.BcFunctionTypeNode;
import bc.lang.BcImportList;
import bc.lang.BcInterfaceDefinitionNode;
import bc.lang.BcTypeNode;
import bc.lang.BcTypeNodeInstance;
import bc.lang.BcVariableDeclaration;
import bc.lang.BcVectorTypeNode;

public class As2CsConverter extends As2WhateverConverter
{
	private ListWriteDestination src;
	private BcFuncRegister funcRegister;
	
	public As2CsConverter()
	{
		super(new CsCodeHelper());
		funcRegister = new BcFuncRegister();
	}
	
	@Override
	protected void writeForeach(WriteDestination dest, Object loopVarName, BcTypeNodeInstance loopVarTypeInstance, Object collection, BcTypeNodeInstance collectionTypeInstance, Object body)
	{
		final String collectionTemp = "__" + loopVarName + "s_";
		dest.writelnf("%s %s = %s;", type(collectionTypeInstance), collectionTemp, collection);
		dest.writelnf("if (%s != %s)", collectionTemp, getCodeHelper().literalNull());
		dest.writeBlockOpen();
		dest.writelnf("foreach (%s %s in %s)", type(loopVarTypeInstance), loopVarName, collectionTemp);		
		dest.writeln(body);		
		dest.writeBlockClose();
	}
	
	private void writeImports(WriteDestination dest, List<String> imports)	
	{
		List<String> sortedImports = new ArrayList<String>(imports);
		Collections.sort(sortedImports);
		
		for (String importString : sortedImports)
		{
			dest.writelnf("using %s;", importString);
		}
	}
	
	private void writeUsings(ListWriteDestination dest, List<BcTypeNode> types)
	{
		for (BcTypeNode type : types)
		{
			String classType = classType(type);
			dest.writelnf("using %s = %s.%s;", classType, type.getQualifier(), classType);
		}
	}

	private void writeInterfaceFunctions(BcClassDefinitionNode bcClass)
	{
		List<BcFunctionDeclaration> functions = bcClass.getFunctions();
		for (BcFunctionDeclaration bcFunc : functions)
		{
			String type = bcFunc.hasReturnType() ? type(bcFunc.getReturnType()) : "void";
			String name = getCodeHelper().identifier(bcFunc.getName());
			
			if (bcFunc.isConstructor())
			{
				continue;
			}
			
			src.writef("%s %s(", type, name);
			
			StringBuilder paramsBuffer = new StringBuilder();
			StringBuilder argsBuffer = new StringBuilder();
			List<BcFuncParam> params = bcFunc.getParams();
			int paramIndex = 0;
			for (BcFuncParam bcParam : params)
			{
				String paramType = type(bcParam.getTypeInstance());
				String paramName = getCodeHelper().identifier(bcParam.getIdentifier());
				paramsBuffer.append(String.format("%s %s", paramType, paramName));
				argsBuffer.append(paramName);
				if (++paramIndex < params.size())
				{
					paramsBuffer.append(", ");
					argsBuffer.append(", ");
				}
			}
			
			src.write(paramsBuffer);
			src.writeln(");");
		}
	}

	@Override
	protected void writeClassDefinition(BcClassDefinitionNode bcClass, File outputDir) throws IOException
	{
		boolean isInterface = bcClass instanceof BcInterfaceDefinitionNode;
		
		String className = getClassName(bcClass);
		
		String packageName = bcClass.getPackageName();
		String subPath = packageName.replace(".", "/");
		
		File srcFileDir = new File(outputDir, subPath);
		if (!srcFileDir.exists())
		{
			boolean successed = srcFileDir.mkdirs();
			failConversionUnless(successed, "Can't make output dir: %s", srcFileDir.getAbsolutePath());
		}
		
		File outputFile = new File(srcFileDir, className + ".cs");
		
		if (!shouldWriteClassToFile(bcClass, outputFile))
		{
			return;
		}
		
		src = new ListWriteDestination();		
		
		src.writeln("using System;");
		writeBlankLine(src);
		
		CsImportsData importsData = getImports(bcClass);
		
		writeImports(src, importsData.getNamespaces());
		writeUsings(src, importsData.getUsingTypes());
		writeBlankLine(src);
		
		src.writeln("namespace " + getCodeHelper().namespace(bcClass.getPackageName()));
		writeBlockOpen(src);
		
		if (bcClass.hasFunctionTypes())
		{
			writeFunctionTypes(bcClass);
		}
		
		if (isInterface)
		{
			src.writelnf("public interface %s", className);
			writeBlockOpen(src);
			writeInterfaceFunctions(bcClass);
			writeBlockClose(src);
		}
		else
		{
			if (bcClass.isFinal())
			{
				src.writef("public sealed class %s", className);
			}
			else
			{
				src.writef("public class %s", className);
			}
			
			boolean hasExtendsType = bcClass.hasExtendsType();
			boolean hasInterfaces = bcClass.hasInterfaces();
			
			if (hasExtendsType || hasInterfaces)
			{
				src.write(" : ");
			}
			
			if (hasExtendsType)
			{
				src.write(type(bcClass.getExtendsType()));
				if (hasInterfaces)
				{
					src.write(", ");
				}
			}
			
			if (hasInterfaces)
			{
				List<BcTypeNodeInstance> interfaces = bcClass.getInterfaces();
				int interfaceIndex= 0;
				for (BcTypeNodeInstance bcInterface : interfaces) 
				{					
					String interfaceType = type(bcInterface);
					src.write(++interfaceIndex == interfaces.size() ? interfaceType : (interfaceType + ", "));
				}
			}
			
			List<BcVariableDeclaration> bcInitializedFields = collectFieldsWithInitializer(bcClass);
			needFieldsInitializer = bcInitializedFields.size() > 0;
			
			src.writeln();
			writeBlockOpen(src);
			
			writeFields(bcClass);
			if (needFieldsInitializer)
			{
				writeFieldsInitializer(bcClass, bcInitializedFields);
			}
			writeFunctions(bcClass);
			
			writeBlockClose(src);
		}		
		
		writeBlockClose(src);
		
		writeDestToFile(src, outputFile);
	}

	private void writeFunctionTypes(BcClassDefinitionNode bcClass) 
	{
		List<BcFunctionTypeNode> functionTypes = bcClass.getFunctionTypes();
		for (BcFunctionTypeNode funcType : functionTypes) 
		{
			String packageName = bcClass.getPackageName();
			if (funcRegister.isRegistered(packageName, funcType))
			{
				writeFunctionType(bcClass, funcType);
				funcRegister.register(packageName, funcType);
			}
		}
	}

	private void writeFunctionType(BcClassDefinitionNode bcClass, BcFunctionTypeNode funcType) 
	{
		String type = funcType.hasReturnType() ? type(funcType.getReturnType()) : "void";
		String name = getCodeHelper().identifier(funcType.getName());			
		
		src.writelnf("public delegate %s %s(%s);", type, type(name), paramsDef(funcType.getParams()));
	}

	private void writeFields(BcClassDefinitionNode bcClass)
	{
		List<BcVariableDeclaration> fields = bcClass.getFields();
		
		for (BcVariableDeclaration bcField : fields)
		{
			String type = type(bcField.getTypeInstance());
			String name = getCodeHelper().identifier(bcField.getIdentifier());
						
			src.write(bcField.getVisiblity() + " ");
			
			if (bcField.isConst())
			{
				if (canBeClass(bcField.getType()))
				{
					src.write("static ");
				}
				else
				{
					src.write("const ");
				}
			}
			else if (bcField.isStatic())
			{
				src.write("static ");
			}			
			
			src.writef("%s %s", type, name);
			if (bcField.hasInitializer() && isSafeInitialized(bcClass, bcField))
			{
				src.writef(" = %s", bcField.getInitializer());
			}
			src.writeln(";");
		}
	}
	
	private void writeFieldsInitializer(BcClassDefinitionNode bcClass, List<BcVariableDeclaration> bcFields) 
	{
		src.writelnf("private void %s()", internalFieldInitializer);
		writeBlockOpen(src);
		
		for (BcVariableDeclaration bcVar : bcFields) 
		{
			String name = getCodeHelper().identifier(bcVar.getIdentifier());
			src.writelnf("%s = %s;", name, bcVar.getInitializer());
		}
		
		writeBlockClose(src);
	}
	
	private void writeFunctions(BcClassDefinitionNode bcClass)
	{
		List<BcFunctionDeclaration> functions = bcClass.getFunctions();
		for (BcFunctionDeclaration bcFunc : functions)
		{
			src.write(bcFunc.getVisiblity() + " ");
			if (bcFunc.isConstructor())
			{
				src.write(getClassName(bcClass));
			}			
			else
			{
				if (bcFunc.isStatic())
				{
					src.write("static ");
				}
				else if (bcFunc.isOverride())
				{
					src.write("override ");
				}
				else if (!bcFunc.isPrivate() && !bcClass.isFinal())
				{
					src.write("virtual ");
				}
				
				String type = bcFunc.hasReturnType() ? type(bcFunc.getReturnTypeInstance()) : "void";
				String name = getCodeHelper().identifier(bcFunc.getName());			
				
				if (bcFunc.isGetter())
				{
					name = getCodeHelper().getter(name);
				}
				else if (bcFunc.isSetter())
				{
					name = getCodeHelper().setter(name);
				}
				src.writef("%s %s", type, name);
			}
			
			src.writelnf("(%s)", paramsDef(bcFunc.getParams()));
			
			ListWriteDestination body = bcFunc.getBody();
			if (bcFunc.isConstructor())
			{
				writeConstructorBody(body);
			}
			else
			{
				src.writeln(body);
			}
		}
	}

	private void writeConstructorBody(ListWriteDestination body) 
	{
		List<String> lines = body.getLines();
		String firstLine = lines.get(1).trim();
		if (firstLine.startsWith(BcCodeHelper.thisCallMarker))
		{
			firstLine = firstLine.replace(BcCodeHelper.thisCallMarker, "this");
			if (firstLine.endsWith(";"))
			{
				firstLine = firstLine.substring(0, firstLine.length() - 1);
			}
			
			src.writeln(" : " + firstLine);
			lines.remove(1);
		}
		else if (firstLine.startsWith(BcCodeHelper.superCallMarker))
		{
			firstLine = firstLine.replace(BcCodeHelper.superCallMarker, "base");
			if (firstLine.endsWith(";"))
			{
				firstLine = firstLine.substring(0, firstLine.length() - 1);
			}
			
			src.writeln(" : " + firstLine);
			lines.remove(1);
		}
		
		if (needFieldsInitializer)
		{
			lines.add(1, String.format("\t%s();", internalFieldInitializer));
		}
		
		src.writeln(new ListWriteDestination(lines));
	}
	
	private CsImportsData getImports(BcClassDefinitionNode bcClass)
	{
		CsImportsData importsData = new CsImportsData(bcClass);
		
		if (bcClass.hasExtendsType())
		{
			tryAddUniqueNamespace(importsData, bcClass.getExtendsType());
		}
		
		if (bcClass.hasInterfaces())
		{
			List<BcTypeNodeInstance> interfaces = bcClass.getInterfaces();
			for (BcTypeNodeInstance bcInterface : interfaces)
			{
				tryAddUniqueNamespace(importsData, bcInterface.getType());
			}
		}
		
		List<BcVariableDeclaration> classVars = bcClass.getDeclaredVars();
		for (BcVariableDeclaration bcVar : classVars)
		{
			BcTypeNode type = bcVar.getType();
			tryAddUniqueNamespace(importsData, type);
		}
		
		List<BcFunctionDeclaration> functions = bcClass.getFunctions();
		for (BcFunctionDeclaration bcFunc : functions)
		{
			if (bcFunc.hasReturnType())
			{
				BcTypeNode returnType = bcFunc.getReturnType();
				tryAddUniqueNamespace(importsData, returnType);
			}
			
			List<BcFuncParam> params = bcFunc.getParams();
			for (BcFuncParam param : params)
			{
				BcTypeNode type = param.getType();
				tryAddUniqueNamespace(importsData, type);
			}
		}
		
		List<BcTypeNode> additionalImports = bcClass.getAdditionalImports();
		for (BcTypeNode bcType : additionalImports) 
		{
			tryAddUniqueNamespace(importsData, bcType);
		}
		
		return importsData;
	}
		
	private void tryAddUniqueNamespace(CsImportsData importsData, BcTypeNode type)
	{
		if (canBeClass(type))
		{
			importsData.addType(type);
			
			if (type instanceof BcVectorTypeNode)
			{
				BcVectorTypeNode vectorType = (BcVectorTypeNode) type;
				BcTypeNode generic = vectorType.getGeneric();
				if (generic != null)
				{
					tryAddUniqueNamespace(importsData, generic);
				}
			}
		}
	}
	
	/* code helper */
	
	private static final String NEW = "new";
	private static final String IS = "is";
	
	protected static final String VECTOR_BC_TYPE = "Vector";
		
	@Override
	public String thisSelector(BcClassDefinitionNode bcClass, Object selector)
	{
		return memberSelector("this", selector);
	}
	
	@Override
	public String superSelector(BcClassDefinitionNode bcClass, Object selector)
	{
		return memberSelector("base", selector);
	}
	
	@Override
	protected String classType(String name)
	{
		if (name.equals("String"))
		{
			return name;
		}
		
		return super.classType(name);
	}
	
	@Override
	public String construct(String type, Object initializer)
	{
		return NEW + " " + type + "(" + initializer + ")";
	}
	
	@Override
	protected String vectorType(BcVectorTypeNode vectorType)
	{
		String genericName = type(vectorType.getGeneric());
		return type(VECTOR_BC_TYPE) + "<" + genericName + ">";
	}
	
	@Override
	public String constructVector(BcVectorTypeNode vectorType, BcArgumentsList args)
	{
		return NEW + " " + type(VECTOR_BC_TYPE) + "<" + type(vectorType.getGeneric()) + ">" + "(" + args + ")";
	}
	
	@Override
	public String constructLiteralVector(BcVectorTypeNode vectorType, BcArgumentsList args)
	{
		return constructVector(vectorType, args);
	}
	
	@Override
	public String operatorIs(Object lhs, Object rhs)
	{
		return String.format("%s %s %s", lhs, IS, type(rhs.toString()));
	}
	
	@Override
	public String toString(Object expr)
	{
		return memberCall(expr, "ToString");
	}
	
	private class CsImportsData
	{
		private List<String> namespaces;
		private List<BcTypeNode> uniqueTypes;
		private BcClassDefinitionNode bcClass;

		public CsImportsData(BcClassDefinitionNode bcClass)
		{
			this.bcClass = bcClass;
			namespaces = new ArrayList<String>();
			uniqueTypes = new ArrayList<BcTypeNode>();
		}
		
		public void addType(BcTypeNode type)
		{
			BcClassDefinitionNode classNode = type.getClassNode();
			failConversionUnless(classNode != null, "Can't add type to workspace: %s", type.getName());
			
			String packageName = classNode.getPackageName();
			failConversionUnless(packageName != null, "Can't get class package: %s", classNode.getName());
			
			if (!namespaces.contains(packageName))
			{
				namespaces.add(packageName);
			}
			
			if (!uniqueTypes.contains(type))
			{
				uniqueTypes.add(type);
			}
		}
		
		public List<String> getNamespaces()
		{
			return namespaces;
		}
		
		public List<BcTypeNode> getUsingTypes()
		{
			List<BcTypeNode> usingTypes = new ArrayList<BcTypeNode>();
			for (String namespace : namespaces)
			{
				List<BcTypeNode> packageTypes = BcTypeNode.typesForPackage(namespace);
				for (BcTypeNode uniqueType : uniqueTypes)
				{
					String typeName = uniqueType.getName();
					String typeQualifier = uniqueType.getQualifier();
					
					for (BcTypeNode packageType : packageTypes)
					{
						if (typeName.equals(packageType.getName()) && !typeQualifier.equals(packageType.getQualifier()))
						{
							BcTypeNode duplicateType;
							if ((duplicateType = findDuplicateType(usingTypes, uniqueType.getName())) != null)
							{
								BcImportList classImportList = bcClass.getImportList();
								if (!classImportList.containsType(duplicateType))
								{
									usingTypes.remove(duplicateType);
								}
								
								if (classImportList.containsType(uniqueType))
								{
									usingTypes.add(uniqueType);
								}
							}
							else
							{
								usingTypes.add(uniqueType);
							}
						}
					}
				}
			}
			return usingTypes;
		}
		
		private BcTypeNode findDuplicateType(List<BcTypeNode> types, String typeName)
		{
			for (BcTypeNode type : types)
			{
				if (typeName.equals(type.getName()))
				{
					return type;
				}
			}
			return null;
		}
	}
}
