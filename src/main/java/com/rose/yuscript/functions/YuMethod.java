/*
 * This Java File is Created By Rose
 */
package com.rose.yuscript.functions;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URL;
import java.net.URLConnection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.rose.yuscript.YuContext;
import com.rose.yuscript.annotation.ScriptMethod;
import com.rose.yuscript.tree.YuExpression;
import com.rose.yuscript.tree.YuSyntaxError;
import com.rose.yuscript.tree.YuValue;
import kotlin.Unit;
import net.mamoe.mirai.message.data.*;

import static com.rose.yuscript.YuInterpreter.*;

/**
 * @author Rose
 *
 */
@SuppressWarnings("unused")
public class YuMethod {
	
	//public final static ConcurrentHashMap<Long, Speed> map = new ConcurrentHashMap<>();
	
	/**
	 * No instance
	 */
	private YuMethod() {}
	
	@ScriptMethod
	public static String hs(Object url) throws Exception {
	    URLConnection connection = new URL(getString(url)).openConnection();
	    connection.setConnectTimeout(3000);
        connection.setDoInput(true);
        connection.connect();
        BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(),connection.getContentEncoding() == null ? "UTF8" : connection.getContentEncoding()));
        StringBuilder sb = new StringBuilder();
        String line;
        while((line = br.readLine()) != null) {
            sb.append(line);
        }
        br.close();
        return sb.toString();
	}

	@ScriptMethod
	public static void syso(YuContext context,Object[] value) {
	    /*StringBuilder sb = new StringBuilder();
		
		for(Object obj : value)
			sb.append(obj).append('\n');
		
		Speed s = map.computeIfAbsent(context.getSession(), (param) -> new Speed());

		boolean v = s.valid;
		if(s.compute()) {
			try {
				context.getDeclaringInterpreter().getGroup().sendMessageAsync(translateMiraiCode(sb));
				return;
			}catch (Exception e) {
				e.printStackTrace();
			}
			context.getDeclaringInterpreter().getGroup().sendMessageAsync(sb.toString());
		}else if(v) {
			context.getDeclaringInterpreter().getGroup().sendMessageAsync( "YuScript一分钟内在本群发送的消息数目内已达到上限, 一分钟之后再发吧~");
		}*/
	}

	private static Message translateMiraiCode(StringBuilder text) {
		MessageChain chain = MessageUtils.newChain();
		Pattern pattern = Pattern.compile("\\[mirai:(image|face):(.)+?]");
		Matcher matcher = pattern.matcher(text);
		int index = 0;
		while(matcher.find()) {
			String previousString = text.substring(index, matcher.start());
			if(!previousString.isEmpty()) {
				chain = chain.plus(previousString);
			}
			try {
				String matchedText = text.substring(matcher.start(), matcher.end()).substring("[mirai:".length());
				if (matchedText.startsWith("image:")) {
					String imageId = matchedText.substring("image:".length(), matchedText.length() - 1);
					Image image = MessageUtils.newImage(imageId);
					chain = chain.plus(image);
				} else if (matchedText.startsWith("face:")) {
					String id = matchedText.substring("face:".length(), matchedText.length() - 1);
					int idNum = Integer.parseInt(id);
					chain = chain.plus(new Face(idNum));
				} else {
					chain = chain.plus(text.substring(matcher.start(), matcher.end()));
				}
			} catch (Exception e) {
				chain = chain.plus(text.substring(matcher.start(), matcher.end()));
			}
			index = matcher.end();
		}
		String restString = text.substring(index,text.length());
		if(!restString.isEmpty()) {
			chain = chain.plus(text.substring(index, text.length()));
		}
		if(chain.getSize() == 1) {
			AtomicReference<Message> msg = new AtomicReference<>();
			chain.forEachContent((value) -> {
				msg.set(value);
				return Unit.INSTANCE;
			});
			Message msgObj = msg.get();
			msg.set(null);
			if(msgObj != null) {
				return msgObj;
			}
		}
		return chain;
	}

	@ScriptMethod
	public static void stop(Object time) throws InterruptedException {
		Thread.sleep(Long.parseLong(time.toString()));
	}

	@ScriptMethod
	public static void interruptCheck() throws InterruptedException {
		if(Thread.interrupted()) {
			throw new InterruptedException();
		}
	}

	@ScriptMethod
	public static boolean interrupted() {
		return Thread.interrupted();
	}
	
	@ScriptMethod
	public static void tw(YuContext context,Object[] objs) {
	    syso(context,objs);
	}
	
	@ScriptMethod
	public static Long s(YuContext context,YuExpression expr) {
	    return calculate(context,expr);
	}
	
	@ScriptMethod
	public static Double s2(YuContext context,YuExpression expr) {
        BigDecimal bg = BigDecimal.valueOf(calculate2(context, expr));
        return bg.setScale(2, RoundingMode.HALF_UP).doubleValue();
    }
	
	@ScriptMethod
	public static Double sn(YuContext context,YuExpression expr) {
	    return calculate2(context, expr);
	}
	
	@SuppressWarnings("incomplete-switch")
    public static Long calculate(YuContext context,YuValue expr) {
	    if(expr instanceof YuExpression) {
	        YuExpression e = (YuExpression) expr;
	        long ans = 0;
	        long composing = calculate(context, e.getChildren().get(0));
	        boolean plusOrMinus = true;
	        for(int i = 0;i < e.getOperators().size();i++) {
	            long val = calculate(context, e.getChildren().get(i + 1));
	            switch(e.getOperators().get(i)) {
	                case PLUS:
	                    if(plusOrMinus) {
	                        ans += composing;
	                    }else {
	                        ans -= composing;
	                    }
	                    composing = val;
	                    plusOrMinus = true;
	                    break;
	                case MINUS:
	                    if(plusOrMinus) {
                            ans += composing;
                        }else {
                            ans -= composing;
                        }
                        composing = val;
                        plusOrMinus = false;
                        break;
	                case MULTIPLY:
	                    composing *= val;
	                    break;
	                case DIVIDE:
	                    composing /= val;
	                    break;
	            }
	        }
	        if(plusOrMinus) {
                ans += composing;
            }else {
                ans -= composing;
            }
	        return ans;
	    }else {
	        Object value = null;
	        switch(expr.getType()) {
	            case YuValue.TYPE_BOOL:
	                value = expr.isInvert() == (!expr.getBool());
                    if((Boolean)value) {
                        value = 1;
                    }else {
                        value = 0;
                    }
	                break;
	            case YuValue.TYPE_NULL:
	                value = 0;
	                break;
	            case YuValue.TYPE_NUM:
	                value = expr.getNumber();
	                break;
	            case YuValue.TYPE_STR:
	                value = expr.getString();
	                break;
	            case YuValue.TYPE_VAR:
	                value = expr.getValue(context);
	        }
	        if(value == null) {
	            return 0L;
	        }else if(value instanceof Number){
                return ((Number)value).longValue();
            }else {
	            return (long)Double.parseDouble(value instanceof String ? (String)value : value.toString());
	        }
	    }
	}
	@SuppressWarnings("incomplete-switch")
    public static Double calculate2(YuContext context,YuValue expr) {
        if(expr instanceof YuExpression) {
            YuExpression e = (YuExpression) expr;
            double ans = 0D;
            double composing = calculate2(context, e.getChildren().get(0));
            boolean plusOrMinus = true;
            for(int i = 0;i < e.getOperators().size();i++) {
                double val = calculate2(context, e.getChildren().get(i + 1));
                switch(e.getOperators().get(i)) {
                    case PLUS:
                        if(plusOrMinus) {
                            ans += composing;
                        }else {
                            ans -= composing;
                        }
                        composing = val;
                        plusOrMinus = true;
                        break;
                    case MINUS:
                        if(plusOrMinus) {
                            ans += composing;
                        }else {
                            ans -= composing;
                        }
                        composing = val;
                        plusOrMinus = false;
                        break;
                    case MULTIPLY:
                        composing *= val;
                        break;
                    case DIVIDE:
                        composing /= val;
                        break;
                }
            }
            if(plusOrMinus) {
                ans += composing;
            }else {
                ans -= composing;
            }
            return ans;
        }else {
            Object value = null;
            switch(expr.getType()) {
                case YuValue.TYPE_BOOL:
                    value = expr.isInvert() == (!expr.getBool());
                    if((Boolean)value) {
                        value = 1;
                    }else {
                        value = 0;
                    }
                    break;
                case YuValue.TYPE_NULL:
                    value = 0;
                    break;
                case YuValue.TYPE_NUM:
                    value = expr.getNumber();
                    break;
                case YuValue.TYPE_STR:
                    value = expr.getString();
                    break;
                case YuValue.TYPE_VAR:
                    value = expr.getValue(context);
            }
            if(value == null) {
                return 0D;
            }else if(value instanceof Number){
                return ((Number)value).doubleValue();
            }else {
                return Double.parseDouble(value instanceof String ? (String)value : value.toString());
            }
        }
    }
	
	@ScriptMethod
	public static String ssg(Object str,Object p1,Object p2) {
	    return getString(str).substring(getInt(p1), getInt(p2));
	}
	
	@ScriptMethod
    public static String sj(Object str,Object p1,Object p2) {
	    String a = getString(p1);
	    String b = getString(p2);
	    String c = getString(str);
        return c.substring(c.indexOf(a) + a.length() , c.indexOf(b));
    }
	
	@ScriptMethod
    public static String sr(Object str,Object p1,Object p2) {
        String a = getString(p1);
        String b = getString(p2);
        String c = getString(str);
        return c.replace(a, b);
    }
	
	@ScriptMethod
    public static String sr(Object str,Object p1,Object p2,Object action) {
        if(getBool(action)) {
            String a = getString(p1);
            String b = getString(p2);
            String c = getString(str);
            return c.replaceAll(a, b);
        }else {
            return sr(str, p1, p2);
        }
    }
	
	@ScriptMethod
	public static String[] sl(Object a,Object b) {
	    return getString(a).split(getString(b));
	}
	
	@ScriptMethod
	public static int sgszl(Object array) {
	    try {
	        return Array.getLength(array);
	    }catch (Exception e) {
            return -1;
        }
	}
	
	@ScriptMethod
    public static Object sgsz(Object array,Object pos) {
        try {
            return Array.get(array,getInt(pos));
        }catch (Exception e) {
            return null;
        }
    }
	
	@ScriptMethod
	public static void sssz(Object array,Object pos,Object val) {
	    if(array.getClass().isArray()) {
	        Array.set(array, getInt(val), val);
	    }else {
	        throw new IllegalArgumentException();
	    }
	}
	
	private static boolean getBool(Object p) {
        if(p == null) {
            return false;
        }else if(p instanceof Boolean) {
            return (Boolean) p;
        }else if(p instanceof CharSequence){
            try {
                return Boolean.parseBoolean(p.toString());
            }catch (Exception e) {
                return true;
            }
        }else {
            return true;
        }
    }
	
	private static String getString(Object p) {
	    if(p == null) {
	        return "null";
	    }else if(p instanceof String) {
	        return (String)p;
	    }else {
	        return String.valueOf(p);
	    }
	}
	
	private static int getInt(Object p) {
	    if(p == null) {
	        return 0;
	    }else if(p instanceof Number) {
	        return ((Number)p).intValue();
	    }else {
	        return Integer.parseInt(p instanceof String ? (String)p : p.toString());
	    }
	}

	@ScriptMethod
	public static Object[] nsz(Object size) {
		return new Object[getInt(size)];
	}

	@ScriptMethod
	public static Object nsz(Object size,Object type) {
		return Array.newInstance(ClassManager.findClass(getString(type)),getInt(size));
	}

	@ScriptMethod
	public static Class<?> cls(Object name) {
		return ClassManager.findClass(getString(name));
	}

	private static Object performCast(Object obj,Class<?> clazz) {
		if(clazz == int.class || clazz == Integer.class) {
			return getInt(obj);
		}
		if(clazz == long.class || clazz == Long.class) {
			return Long.parseLong(getString(obj));
		}
		if(clazz == boolean.class || clazz == Boolean.class) {
			return getBool(obj);
		}
		if(clazz == float.class || clazz == Float.class) {
			return Float.parseFloat(getString(obj));
		}
		if(clazz == double.class || clazz == Double.class) {
			return Double.parseDouble(getString(obj));
		}
		if(clazz == byte.class || clazz == Byte.class) {
			return (byte)getInt(obj);
		}
		if(clazz == char.class || clazz == Character.class) {
			return (char)getInt(obj);
		}
		if(clazz == short.class || clazz == Short.class) {
			return (short)getInt(obj);
		}
		if(clazz == String.class) {
			return getString(obj);
		}
		if(clazz == CharSequence.class) {
			return getCharSeq(obj);
		}
		return clazz.cast(obj);
	}

	private static Class<?> getClass(Object obj) {
		if(obj instanceof Class) {
			return (Class<?>)obj;
		}else{
			return ClassManager.findClass(getString(obj));
		}
	}

	@ScriptMethod(returnValueAtBegin = true)
	public static Object javanew(Object[] args) {
		try {
			Class<?> clazz = getClass(args[0]);
			if((args.length & 1) != 0) {
				int length = (args.length - 1) >> 1;
				Class<?>[] types = new Class[length];
				Object[] arguments = new Object[length];
				for(int i = 0,j = 1,k = 2;i < length;i++,j += 2,k += 2) {
					types[i] = ClassManager.findClass(getString(args[j]));
					arguments[i] = performCast(args[k],types[i]);
				}
				Constructor<?> constructor = clazz.getDeclaredConstructor(types);
				constructor.setAccessible(true);
				return constructor.newInstance(arguments);
			}else{
				System.err.println("javanew():Argument count illegal");
				return null;
			}
		}catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	@ScriptMethod(returnValueAtBegin = true)
	public static Object java(Object[] args) {
		try{
			String str = getString(args[1]);
			int ix = str.lastIndexOf(".");
			Class<?> clazz = ClassManager.findClass(str.substring(0,ix));
			String name = str.substring(ix + 1);
			if((args.length & 1) == 0) {
				int length = (args.length - 2) >> 1;
				Class<?>[] types = new Class[length];
				Object[] arguments = new Object[length];
				for(int i = 0,j = 2,k = 3;i < length;i++,j += 2,k += 2) {
					types[i] = ClassManager.findClass(getString(args[j]));
					arguments[i] = performCast(args[k],types[i]);
				}
				Method method = clazz.getDeclaredMethod(name,types);
				method.setAccessible(true);
				return method.invoke(args[0],arguments);
			}else{
				System.err.println("java():Argument count illegal");
				return null;
			}
		}catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	@ScriptMethod(returnValueAtBegin = true)
	public static Object javax(Object[] args) {
		//instance,class,name,args...
		try{
			Class<?> clazz = getClass(args[1]);
			String name = getString(args[2]);
			if((args.length & 1) != 0) {
				int length = (args.length - 3) >> 1;
				Class<?>[] types = new Class[length];
				Object[] arguments = new Object[length];
				for(int i = 0,j = 3,k = 4;i < length;i++,j += 2,k += 2) {
					types[i] = ClassManager.findClass(getString(args[j]));
					arguments[i] = performCast(args[k],types[i]);
				}
				Method method = clazz.getDeclaredMethod(name,types);
				method.setAccessible(true);
				return method.invoke(args[0],arguments);
			}else{
				System.err.println("javax():Argument count illegal");
				return null;
			}
		}catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	@ScriptMethod
	public static Object[] clssm(Object clazz, Object name) {
		String n = getString(name);
		Class<?> c = getClass(clazz);
		switch (n) {
			case "init":
				return c.getDeclaredConstructors();
			case "field":
				return c.getDeclaredFields();
			case "method":
				return c.getDeclaredMethods();
		}
		return null;
	}

	@ScriptMethod(returnValueAtBegin = true)
	public static Object javags(Object obj, Object clazz, Object name) throws NoSuchFieldException, IllegalAccessException, IllegalArgumentException {
		Class<?> klass = getClass(clazz);
		Field field = klass.getDeclaredField(getString(name));
		field.setAccessible(true);
		return field.get(obj);
	}

	@ScriptMethod
	public static void javass(Object obj, Object clazz, Object name, Object value) throws NoSuchFieldException, IllegalAccessException, IllegalArgumentException, SecurityException {
		Class<?> klass = getClass(clazz);
		Field field = klass.getDeclaredField(getString(name));
		field.setAccessible(true);
		field.set(obj, performCast(value,field.getType()));
	}

	@ScriptMethod
	public static Class<?> cls(Object loader,Object name) {
		try {
			return ((ClassLoader)loader).loadClass(getString(name));
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			return null;
		}
	}
    
	@ScriptMethod
	public static void t(YuContext context) {
		if(!context.hasCodeBlock()) {
			throw new YuSyntaxError("code block required");
		}else{
			context.getCodeBlock();
		}
		/*final YuContext newContext = new YuContext(context);
		final YuCodeBlock block = context.getCodeBlock();
		Thread local = new Thread() {
			public void run() {
				context.getDeclaringInterpreter().visitCodeBlock(block, newContext);
			}
		};*/
		//local.start();
		syso(context,new Object[] { "SYSTEM:Thread is not allowed." });
	}

	private static CharSequence getCharSeq(Object p) {
		if(p == null) {
			return "null";
		}else if(p instanceof CharSequence) {
			return (CharSequence) p;
		}else {
			return String.valueOf(p);
		}
	}
	
	@ScriptMethod
	public static String strim(Object obj) {
		return stringForm(obj).trim();
	}
	
	@ScriptMethod
	public static int slg(Object obj) {
		return stringForm(obj).length();
	}
	
	//Test reversed returning statement
	@ScriptMethod(returnValueAtBegin = true)
	public static int rslg(Object obj) {
		return stringForm(obj).length();
	}
	
}
