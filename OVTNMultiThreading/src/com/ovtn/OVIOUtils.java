package com.ovtn;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class OVIOUtils {
	public static void writeLines(String iFile, String[] iLines, boolean iAppend) throws IOException {
		PrintWriter lWriter = null;
		try {
			lWriter = new PrintWriter(new FileWriter(iFile, iAppend));
			for (String lLine : iLines) {
				lWriter.println(lLine);
			}
		} finally {
			if (lWriter != null) {
				lWriter.close();
				lWriter = null;
			}
		}
	}
}
