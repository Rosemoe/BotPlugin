/*
 * This Java File is Created By Rose
 */
package com.rose.yuscript;

import java.io.BufferedReader;
import java.io.FileReader;

/**
 * @author Rose
 */
public class Main {
	
	public static void main(String[] args) throws Throwable {
		StringBuilder sb = new StringBuilder();
		BufferedReader br = new BufferedReader(new FileReader("d:\\Test.iyu"));
		br.lines().forEach(string -> sb.append(string).append('\n'));
		br.close();
		String str = sb.toString();
		YuInterpreter i = new YuInterpreter(0);
		i.eval(str);
	}

}
