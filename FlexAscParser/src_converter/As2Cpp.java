import java.io.File;
import java.io.IOException;

import bc.converter.As2CppConverter;
import bc.converter.As2WhateverConverter;

public class As2Cpp
{
	public static void main(String[] args)
	{
		File outputDir = new File(args[0]);

		String[] filenames = new String[args.length - 1];
		System.arraycopy(args, 1, filenames, 0, filenames.length);
		
		try
		{
			As2WhateverConverter converter = new As2CppConverter();
			converter.convert(outputDir, filenames);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
}
