package 안녕Script;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VM {
	private Map<String, Method> methodMap; // 함수 맵
	private Map<String, Var> varMap; // 변수 맵

	public VM() {
		methodMap = new HashMap<String, Method>(); // 함수 맵 초기화
		varMap = new HashMap<String, Var>(); // 변수 맵 초기화
	}

	/**
	 * VM 메모리에 메소드 추가
	 * 
	 * @param name
	 * @param filePath
	 * @param line
	 * @param 에ParameterNames
	 * @param 을ParameterNames
	 * @param source
	 */
	public void insertMethod(String name, String filePath, int line, List<String> 에ParameterNames, List<String> 을ParameterNames, String source) {
		methodMap.put(
			getMethodId(name, 에ParameterNames.size(), 을ParameterNames.size()),
			new Method(name, filePath, line, 에ParameterNames, 을ParameterNames, source)
		);
	}
	
	/**
	 * 메소드를 반환
	 * 
	 * @param name
	 * @param 에ParameterSize
	 * @param 을ParameterSize
	 * @return method
	 */
	public Method getMethod(String name, int 에ParameterSize, int 을ParameterSize) {
		return methodMap.get(getMethodId(name, 에ParameterSize, 을ParameterSize));
	}
	
	/**
	 * 메소드 ID를 반환
	 * 
	 * @param name
	 * @param 에ParameterSize
	 * @param 을ParameterSize
	 * @return methodId
	 */
	public String getMethodId(String name, int 에ParameterSize, int 을ParameterSize) {
		return getProperName(name) + ":" + 에ParameterSize + ":" + 을ParameterSize;
	}
	
	/**
	 * VM 메모리에 변수 추가
	 * 
	 * @param name
	 * @param filePath
	 * @param line
	 * @param source
	 */
	public void insertVar(String name, String filePath, int line, String source) {
		varMap.put(getProperName(name), new Var(name, filePath, line, source));
	}
	
	/**
	 * 변수를 반환
	 * 
	 * @param name
	 * @return var
	 */
	public Var getVar(String name) {
		return varMap.get(getProperName(name));
	}

	/**
	 * 대표 이름 추출
	 */
	private String getProperName(String name) {
		Pattern blankPattern = Pattern.compile("[ |\\t|\\n|\\r]*"); // 모든 공백을
		Matcher blankMatcher = blankPattern.matcher(name);
		StringBuffer sb = new StringBuffer();
		while (blankMatcher.find()) {
			blankMatcher.appendReplacement(sb, ""); // 제거
		}
		blankMatcher.appendTail(sb);
		return sb.toString().toLowerCase(); // 소문자로 만듦
	}
	
	public void printMemory() {
		System.out.println(varMap);
	}
	
}
