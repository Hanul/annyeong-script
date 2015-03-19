package 안녕Script;

public class StringUtils {

	static boolean equalsStrings(String string, String string2) {
		if (string == null || string2 == null) {
			return false;
		}
		return string.equals(string2);
	}

	static boolean equalsLastStringString(String string, int n, String string2) {
		if (string.length() < n) {
			return false;
		}
		return equalsStrings(getLastSubstring(string, n), string2);
	}

	static String getLastSubstring(String string, int n) {
		return string.substring(string.length() - n);
	}

	static String getBackSubstring(String string, int n) {
		return string.substring(0, string.length() - n);
	}

	static String getSubstring(String string, int startIndex, int n) {
		if (string.length() < startIndex + n) {
			return string.substring(startIndex);
		}
		return string.substring(startIndex, startIndex + n);
	}

}
