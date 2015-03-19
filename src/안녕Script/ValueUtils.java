package 안녕Script;

public class ValueUtils {

	/**
	 * 숫자 값을 문자열로 만든다.
	 */
	static String numberValueToString(double d) {
		if (d % 1 == 0.0) {
			return "" + (int) d;
		} else {
			return "" + d;
		}
	}

	/**
	 * 문자열에서 숫자를 구합니다.
	 */
	static Double getNumberValue(String numberStr) {
		try {
			return Double.parseDouble(numberStr.trim());
		} catch (NumberFormatException e) {
			return null;
		}
	}
	
	/**
	 * Boolean 값을 문자열로 만든다.
	 */
	static String booleanValueToString(boolean b) {
		if (b) {
			return "참";
		} else {
			return "거짓";
		}
	}
	
	/**
	 * 문자열에서 Boolean을 구합니다.
	 */
	static Boolean getBooleanValue(String booleanStr) {
		if (booleanStr.trim().equals("참")) {
			return true;
		} else if (booleanStr.trim().equals("거짓")) {
			return false;
		} else {
			return null;
		}
	}
	
	/**
	 * 문자열 값을 문자열로 만든다.
	 */
	static String stringValueToString(String string) {
		return "\"" + string + "\"";
	}
	
	/**
	 * 문자열에서 문자열을 구합니다.
	 */
	static String getStringValue(String stringStr) {
		if (stringStr.length() > 2) {
			if (
				(stringStr.charAt(0) == '\'' && stringStr.charAt(stringStr.length() - 1) == '\'') ||
				(stringStr.charAt(0) == '\"' && stringStr.charAt(stringStr.length() - 1) == '\"')
			) {
				return stringStr;
			}
		}
		return null;
	}
	
	/**
	 * 실제 문자열 값을 구합니다.
	 */
	static String getRealStringValue(String stringStr) {
		return stringStr.substring(1, stringStr.length() - 1);
	}
	
	/**
	 * 코드블록을 문자열로 만든다.
	 */
	static String codeBlockToString(String string) {
		return "{" + string + "}";
	}
	
	/**
	 * 문자열에서 코드블록을 구합니다.
	 */
	static String getCodeBlock(String stringStr) {
		if (stringStr.length() > 2) {
			if (stringStr.charAt(0) == '{' && stringStr.charAt(stringStr.length() - 1) == '}') {
				return stringStr;
			}
		}
		return null;
	}
	
	/**
	 * 어떤 값을 문자열로 만든다.
	 */
	static String valueToString(String valueStr) {
		Double numberValue = getNumberValue(valueStr); // 값 숫자
		String stringValue = getStringValue(valueStr); // 값 문자열
		Boolean booleanValue = getBooleanValue(valueStr); // 값 Boolean
		
		String codeBlock = getCodeBlock(valueStr); // 코드블록
		
		if (numberValue != null) { // 값이 숫자라면,
			return numberValueToString(numberValue);
		}
		else if (stringValue != null) { // 값이 문자열이라면,
			return stringValueToString(getRealStringValue(stringValue));
		}
		else if (booleanValue != null) { // 값이 Boolean이라면,
			return booleanValueToString(booleanValue);
		}
		else if (codeBlock != null) { // 값이 코드블록이라면,
			return codeBlockToString(codeBlock);
		}
		else { // 알 수 없는 값
			return null;
		}
	}
	
}
