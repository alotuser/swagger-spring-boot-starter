package cn.alotuser.util;

import cn.alotus.core.util.StrUtil;

/*
 * Utility class for handling Swagger-related string operations.
 * This class provides methods to extract keys from strings formatted with a specific pattern.
 */
public class SwaggerUtil {

	public static final String SWAGGER_EMPTY = "";
	public static final String SWAGGER_PRE = "#{";
	public static final String SWAGGER_END = "}";

	/**
	 * Extracts a key from a string that is formatted with the pattern "#{key}".
	 * 
	 * @param str the input string containing the key
	 * @return the extracted key or an empty string if the pattern is not found
	 */
	public static String getKey(String str) {
		return StrUtil.cleanBlank(StrUtil.subBetween(str, SwaggerUtil.SWAGGER_PRE, SwaggerUtil.SWAGGER_END));
	}
}
