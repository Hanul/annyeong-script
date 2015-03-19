package 안녕Script;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.EmptyStackException;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import bsh.EvalError;

/**
 * 안녕 스크립트
 * 
 * 최초버젼 1.0
 * 
 * @author 심영재 (Mr. 하늘)
 */
public class 안녕 {
	/**
	 * 직접 실행시
	 */
	public static void main(String[] args) {
		안녕 안녕 = new 안녕(); // 새 안녕 인스턴스 생성
		try {
			안녕.execute(new File("test"));
		} catch (FileNotFoundException e) {
			throw new Error();
		} catch (IOException e) {
			throw new Error();
		}
	}
	
	private String filePath;

	/**
	 * 파일 실행
	 */
	public void execute(File sourceFile) throws FileNotFoundException, IOException {
		BufferedReader br = new BufferedReader(new FileReader(sourceFile));
		StringBuffer sb = new StringBuffer();
		int readByte = 0;
		while ((readByte = br.read()) != -1) {
			sb.append((char) readByte);
		}
		filePath = sourceFile.getAbsolutePath();
		source = sb.toString();
		_execute();
	}
	
	/**
	 * 코드 바로 실행
	 */
	public void execute(String source) {
		this.source = source;
		_execute();
	}
	
	private static Map<String, Integer> operatorOrderMap; // 연산자 연산 순서 맵
	static {
		operatorOrderMap = new HashMap<String, Integer>();
		operatorOrderMap.put(".", 0);
		
		operatorOrderMap.put("(", 99);
		operatorOrderMap.put(")", 0);
		
		operatorOrderMap.put("\"", 99);
		operatorOrderMap.put("'", 99);
		
		operatorOrderMap.put("{", 99);
		operatorOrderMap.put("}", 0);
		
		operatorOrderMap.put("[", 99);
		operatorOrderMap.put("]", 0);
		
		operatorOrderMap.put("/*", 99); // 주석
		operatorOrderMap.put("*/", 0); // 주석
		
		operatorOrderMap.put("//", 99); // 주석
		operatorOrderMap.put("#", 99); // 주석
		
		// 1
		operatorOrderMap.put(">>", 1); // 자바 명령어 실행
		
		// 2
		operatorOrderMap.put("++", 2);
		operatorOrderMap.put("--", 2);
		
		// 3
		operatorOrderMap.put("**", 3);
		operatorOrderMap.put("^", 3);
		
		// 4
		operatorOrderMap.put("*", 4);
		operatorOrderMap.put("/", 4);
		operatorOrderMap.put("%", 4);
		
		// 5
		operatorOrderMap.put("+", 5);
		operatorOrderMap.put("-", 5);
		
		// 6
		operatorOrderMap.put("==", 6);
		operatorOrderMap.put("!=", 6);
		operatorOrderMap.put("<", 6);
		operatorOrderMap.put("<=", 6);
		operatorOrderMap.put(">", 6);
		operatorOrderMap.put(">=", 6);
		
		// 7
		operatorOrderMap.put("!", 7);
		
		// 8
		operatorOrderMap.put("|", 8);
		operatorOrderMap.put("||", 8);
		operatorOrderMap.put("&", 8);
		operatorOrderMap.put("&&", 8);
		
		// 9
		operatorOrderMap.put("=", 9);
		operatorOrderMap.put("*=", 9);
		operatorOrderMap.put("/=", 9);
		operatorOrderMap.put("%=", 9);
		operatorOrderMap.put("+=", 9);
		operatorOrderMap.put("-=", 9);
		
		// ?
		operatorOrderMap.put("`", 99);
		operatorOrderMap.put("~", 99);
		operatorOrderMap.put("@", 99);
		operatorOrderMap.put("$", 99);
		operatorOrderMap.put("\\", 99);
		operatorOrderMap.put(":", 99);
		operatorOrderMap.put(";", 99);
		operatorOrderMap.put(",", 99);
		operatorOrderMap.put("?", 99);
	}
	
	private static Map<String, String> wrapperOperatorMap; // 감싸는 연산자 맵
	static {
		wrapperOperatorMap = new HashMap<String, String>();
		wrapperOperatorMap.put("\"", "\"");
		wrapperOperatorMap.put("'", "'");
		wrapperOperatorMap.put("{", "}");
		wrapperOperatorMap.put("[", "]");
		wrapperOperatorMap.put("/*", "*/");
	}

	private VM vm; // 가상 머신
	private String source; // 소스
	private int line;
	
	private Stack<String> operatorStack = new Stack<String>(); // 연산자 스택
	private Stack<Stack<String>> valueStackStack = new Stack<Stack<String>>(); // 값 스택을 저장하는 스택
	private Stack<String> valueStack = new Stack<String>(); // 값 스택
	private Stack<String> wrapperStack = new Stack<String>(); // 감싸는 스택
	
	private Stack<String> lateIncreaseStack = new Stack<String>(); // 나중에 증가시킬 스택
	private Stack<String> lateDecreaseStack = new Stack<String>(); // 나중에 감소시킬 스택
	
	private int index = 0;
	private int lastIndex = 0;
	private boolean isSaveBlank = true;

	@SuppressWarnings("unchecked")
	private void _execute() { // 실행
		vm = new VM();
		line = 1;
		
		for (; index < source.length(); index++) {
			if (source.charAt(index) == '\n') {
				line++;
			}
			if (source.charAt(index) >= '0' && source.charAt(index) <= '9') { // 숫자면 저장
				isSaveBlank = false; // 공백을 제외하고,
				saveValue(); // 이전까지의 값을 저장
				
				isSaveBlank = false; // 다음에 오는 값이 공백이면 제외
				int startIndex = index++;
				for (; index < source.length(); index++) { // 소스를 돌면서,
					if (!(
							(source.charAt(index) >= '0' && source.charAt(index) <= '9') || // 숫자거나
							(index + 1 != source.length() && source.charAt(index) == '.' && source.charAt(index + 1) >= '0' && source.charAt(index + 1) <= '9') // 소수점 표현이
						)) { // 아닐 값이 나올 경우
						valueStack.push(source.substring(startIndex, index)); // 숫자를 저장하고
						break; // 끝냅니다.
					}
				}
				index--;
				lastIndex = index + 1; // 마지막 인덱스를 조절
			} else {
				String nowOperator = null;
				for (String operator : operatorOrderMap.keySet()) {
					if ((nowOperator == null || nowOperator.length() < operator.length())
							&& isToken(operator)) { // 토큰인가?
						nowOperator = operator;
					}
				}
				if (nowOperator != null) {
					saveValue(); // 이전까지의 값을 저장
					
					if (nowOperator.equals(".")) { // 마침표일 경우
						while (!operatorStack.isEmpty()) { // 스택에 있는 모든 연산자를 계산
							calculate(operatorStack.pop());
						}
						while (!lateIncreaseStack.isEmpty()) { // 나중에 증가시킬 스택의 모든 변수 1 증가
							Var var = vm.getVar(lateIncreaseStack.pop());
							var.setSource(
								((Double)(getNumberValue(var.getSource()) + 1)).toString() // 변수 1 증가
							);
						}
						while (!lateDecreaseStack.isEmpty()) { // 나중에 감소시킬 스택의 모든 변수 1 증가
							Var var = vm.getVar(lateDecreaseStack.pop());
							var.setSource(
								((Double)(getNumberValue(var.getSource()) - 1)).toString() // 변수 1 감소
							);
						}
						if (!valueStack.isEmpty()) { // 남아있는 값이 있으면,
							runValue(); // 남아있는 값들을 실행한다.
						}
						valueStack.clear(); // 값 스택 초기화
						index += nowOperator.length() - 1; // i를 연산자 길이만큼 증가
					}
					else if (nowOperator.equals("(")) { // 소괄호를 여는 경우
						if (valueStack.peek().equals("")) { // 최근 값이 빈 값이면
							valueStack.pop(); // 제거한다.
						}
						valueStackStack.push((Stack<String>) valueStack.clone()); // 값 스택의 복사본을 저장한다.
						valueStack.clear(); // 그리고는 초기화한다.
						
						operatorStack.push(nowOperator); // 연산자를 저장
						index += nowOperator.length() - 1; // i를 연산자 길이만큼 증가
					}
					else if (nowOperator.equals(")")) { // 소괄호를 닫는 경우
						isSaveBlank = false;
						while (!operatorStack.isEmpty()) { // 스택에 있는 모든 연산자를 계산
							String operator = operatorStack.pop();
							if (operator.equals("(")) { // 소괄호를 여는 곳 까지 도달하면
								if (!valueStack.isEmpty()) { // 남아있는 값이 있으면,
									runValue(); // 남아있는 값들을 실행한다.
									if (!valueStack.isEmpty()) { // 실행한 뒤 남아있는 값이 있으면,
										if (valueStack.size() == 1) { // 남아있는 값이 1개면,
											String v = valueStack.pop(); // 값을 v에 저장
											valueStack = valueStackStack.pop(); // 값 스택의 복사본을 불러온다.
											valueStack.push(v); // v를 값 스택에 저장
										} else {
											throw new Error();
										}
									}
								} else {
									valueStack = valueStackStack.pop(); // 값 스택의 복사본을 불러온다.
								}
								break; // 정지
							} else {
								calculate(operator); // 저장되었던 연산자를 계산
							}
						}
						index += nowOperator.length() - 1; // i를 연산자 길이만큼 증가
					}
					else if (nowOperator.equals("#") || nowOperator.equals("//")) { // 주석인 경우
						if (valueStack.peek().equals("")) { // 최근 값이 빈 값이면
							valueStack.pop(); // 제거한다.
						}
						
						for (; index < source.length(); index++) { // 소스를 돌면서,
							if (source.charAt(index) == '\n') {
								line++;
								break;
							}
						}
						isSaveBlank = false;
					}
					else if (wrapperOperatorMap.keySet().contains(nowOperator)) { // 감싸는 연산자 시작
						if (valueStack.peek().equals("")) { // 최근 값이 빈 값이면
							valueStack.pop(); // 제거한다.
						}
						int startIndex = index++;
						String startOperator = nowOperator;
						wrapperStack.push(nowOperator); // 현재 감싸는 연산자를 저장한다.
						for (; index < source.length(); index++) { // 소스를 돌면서,
							if (source.charAt(index) == '\\') { // 앞 글자가 \이면 다음 연산자를 무시한다.
								index++;
							} else {
								if (source.charAt(index) == '\n') {
									line++;
								}
								nowOperator = null;
								for (String key : wrapperOperatorMap.keySet()) { // 감싸는 연산자의 끝인지 검사
									String operator = wrapperOperatorMap.get(key);
									if ((nowOperator == null || nowOperator.length() < operator.length())
											&& isToken(operator)) { // 토큰인가?
										nowOperator = operator;
									}
								}
								if (nowOperator != null && // 감싸는 연산자의 끝을 찾았고,
									nowOperator.equals(wrapperOperatorMap.get(wrapperStack.peek()))) { // 최근 감싸는 연산자의 시작과 지금 찾은 끝이 쌍을 이루면,
									wrapperStack.pop(); // 감싸는 연산자 한단계 제거
									if (wrapperStack.isEmpty()) { // 모든 감싸는 연산자가 제거되면,
										isSaveBlank = false;
										if (!startOperator.equals("/*")) { // 주석은 저장하지 않는다.
											String subsource = source.substring(startIndex, index + 1);
											if (startOperator.equals("\"") || startOperator.equals("'")) { // 문자열이면,
												valueStack.push(getDynamicString(subsource)); // 문자열로 변한 한 뒤 값 스택에 저장한다.
											}
											else if (startOperator.equals("{")) { // 코드 조각이면,
												valueStack.push(subsource); // 값 스택에 저장한다.
											}
											else {
												throw new Error();
											}
										}
										index += nowOperator.length() - 1; // i를 연산자 길이만큼 증가
										break;
									}
								} else {
									nowOperator = null;
									for (String operator : wrapperOperatorMap.keySet()) { // 감싸는 연산자의 시작인지 검사
										if ((nowOperator == null || nowOperator.length() < operator.length())
												&& isToken(operator)) { // 토큰인가?
											nowOperator = operator;
										}
									}
									if (nowOperator != null) { // 감싸는 연산자의 시작을 찾으면,
										wrapperStack.push(nowOperator); // 저장
									}
								}
							}
						}
					}
					else {
						if (!operatorStack.isEmpty() && operatorOrderMap.get(operatorStack.peek()) < operatorOrderMap.get(nowOperator)) { // 후위표현식으로 계산합니다.
							calculate(operatorStack.pop()); // 저장되었던 연산자를 계산
						}
						operatorStack.push(nowOperator); // 연산자를 저장
						index += nowOperator.length() - 1; // i를 연산자 길이만큼 증가
					}
					lastIndex = index + 1; // 마지막 인덱스를 조절
				}
			}
		}
		if (!valueStack.isEmpty()) {
			throw new Error();
		}
		if (!operatorStack.isEmpty()) {
			throw new Error();
		}
		
		vm.printMemory();
	}
	
	/**
	 * 값을 저장
	 */
	private void saveValue() {
		if (isSaveBlank) {
			String subsource = source.substring(lastIndex, index).trim();
			valueStack.push(subsource); // 값 스택에 저장
		} else { // 방금 감싸는 연산자가 닫혔고, 공백이 나오면 저장하지 않는다.
			String subsource = source.substring(lastIndex, index).trim();
			if (!subsource.equals("")) { // 공백이 아니면,
				valueStack.push(subsource); // 공백을 제거하고 값 스택에 저장
			}
			isSaveBlank = true;
		}
	}

	/**
	 * 문자열 동적 처리 후 반환
	 */
	private String getDynamicString(String subsorce) {
		 String string = subsorce
			.replace("\\\"", "\"") // "
			.replace("\\'", "'") // '
			.replace("\\n", "\n") // 줄넘김
	 		.replace("\\t", "\t") // 탭
		 ;
		 return string;
	}
	
	/**
	 * 값들을 실행
	 */
	private void runValue() {
		String lastValueStr = valueStack.pop();
		
		if (valueStack.size() == 2 && (StringUtils.equalsStrings(lastValueStr, "다") || StringUtils.equalsStrings(lastValueStr, "이다"))) { // 값 저장
			String valueStr = valueToString(valueStack.pop()); // 값
			String varName = remove는(valueStack.pop()); // 변수 명
			vm.insertVar(varName, filePath, line, valueStr); // 변수 저장
			valueStack.push(varName); // 변수 명을 곱게 넣어줍니다.
		}
		else if (StringUtils.equalsLastStringString(lastValueStr, 2, "이다") || StringUtils.equalsLastStringString(lastValueStr, 1, "다")) { // 값 저장
			String varName = null;
			String string = null;
			if (StringUtils.equalsLastStringString(lastValueStr, 2, "이다")) {
				string = StringUtils.getBackSubstring(lastValueStr, 2);
			}
			else if (StringUtils.equalsLastStringString(lastValueStr, 1, "다")) {
				string = StringUtils.getBackSubstring(lastValueStr, 1);	
			}
			String valueStr = valueToString(string);
			if (valueStr == null) {
				for (int i = 0 ; i < string.length() ; i++) {
					if (is는(StringUtils.getSubstring(string, i, 1))) {
						valueStr = valueToString(string.substring(i + 1));
						if (valueStr != null) {
							varName = remove는(string.substring(0, i + 1));
							break;
						}
					}
				}
			}
			if (varName != null) {
				vm.insertVar(varName, filePath, line, valueStr); // 변수 저장
			}
			valueStack.push(varName); // 변수 명을 곱게 넣어줍니다.
		}
		else if (valueStack.empty() && valueToString(lastValueStr) != null) {
			valueStack.push(lastValueStr); // 다시 마지막 값을 곱게 넣어줍니다.
		}
		else {
			throw new Error();
		}
	}
	
	private boolean is는(String string) {
		return string.equals("은") || string.equals("는");
	}
	
	/**
	 * (은, 는)을 지움
	 */
	private String remove는(String string) {
		if (string.length() > 1 && is는(StringUtils.getLastSubstring(string, 1))) {
			return StringUtils.getBackSubstring(string, 1);
		} else {
			return string;
		}
	}
	
	/**
	 * 연산자를 계산
	 */
	private void calculate(String operator) {
		try {
			if (operator.equals("++")) { // 증가
				String rightValueStr = valueStack.pop(); // 우측 값 문자열
				String leftValueStr = valueStack.pop(); // 좌측 값 문자열
				
				Var leftValueVar = vm.getVar(leftValueStr); // 좌측 값 변수
				Var rightValueVar = vm.getVar(rightValueStr); // 우측 값 변수
				if (leftValueStr.equals("") && rightValueVar != null) {
					rightValueVar.setSource(
						((Double)(getNumberValue(rightValueVar.getSource()) + 1)).toString() // 우측 값 변수 1 증가
					);
					valueStack.push(rightValueStr); // 다시 우측 값을 곱게 넣어줍니다.
				}
				else if (leftValueVar != null && rightValueStr.equals("")) {
					lateIncreaseStack.push(leftValueStr); // 나중에 좌측 값 변수 1 증가
					valueStack.push(leftValueStr); // 다시 좌측 값을 곱게 넣어줍니다.
				}
				else {
					throw new Error();
				}
			}
			else if (operator.equals("--")) { // 감소
				String rightValueStr = valueStack.pop(); // 우측 값 문자열
				String leftValueStr = valueStack.pop(); // 좌측 값 문자열
				
				Var leftValueVar = vm.getVar(leftValueStr); // 좌측 값 변수
				Var rightValueVar = vm.getVar(rightValueStr); // 우측 값 변수
				if (leftValueStr.equals("") && rightValueVar != null) {
					rightValueVar.setSource(
						((Double)(getNumberValue(rightValueVar.getSource()) - 1)).toString() // 우측 값 변수 1 감소
					);
					valueStack.push(rightValueStr); // 다시 우측 값을 곱게 넣어줍니다.
				}
				else if (leftValueVar != null && rightValueStr.equals("")) {
					lateDecreaseStack.push(leftValueStr); // 나중에 좌측 값 변수 1 감소
					valueStack.push(leftValueStr); // 다시 좌측 값을 곱게 넣어줍니다.
				}
				else {
					throw new Error();
				}
			}
			else if (operator.equals("**") || operator.equals("^")) { // 제곱
				String rightValueStr = valueStack.pop(); // 우측 값 문자열
				String leftValueStr = valueStack.pop(); // 좌측 값 문자열
				
				Double leftNumberValue = getNumberValue(leftValueStr); // 좌측 값 숫자
				Double rightNumberValue = getNumberValue(rightValueStr); // 우측 값 숫자
				if (leftNumberValue != null && rightNumberValue != null) { // 둘 다 숫자라면,
					valueStack.push(
						((Double)(Math.pow(leftNumberValue, rightNumberValue))).toString() // 제곱해서 저장
					);
				}
				else {
					throw new Error();
				}
			}
			else if (operator.equals("*")) { // 곱하기			
				String rightValueStr = valueStack.pop(); // 우측 값 문자열
				String leftValueStr = valueStack.pop(); // 좌측 값 문자열
				
				Double leftNumberValue = getNumberValue(leftValueStr); // 좌측 값 숫자
				Double rightNumberValue = getNumberValue(rightValueStr); // 우측 값 숫자
				if (leftNumberValue != null && rightNumberValue != null) { // 둘 다 숫자라면,
					valueStack.push(
						((Double)(leftNumberValue * rightNumberValue)).toString() // 곱해서 저장
					);
				}
				else {
					throw new Error();
				}
			}
			else if (operator.equals("/")) { // 나누기
				String rightValueStr = valueStack.pop(); // 우측 값 문자열
				String leftValueStr = valueStack.pop(); // 좌측 값 문자열
				
				Double leftNumberValue = getNumberValue(leftValueStr); // 좌측 값 숫자
				Double rightNumberValue = getNumberValue(rightValueStr); // 우측 값 숫자
				if (leftNumberValue != null && rightNumberValue != null) { // 둘 다 숫자라면,
					valueStack.push(
						((Double)(leftNumberValue / rightNumberValue)).toString() // 나눠서 저장
					);
				}
				else {
					throw new Error();
				}
			}
			else if (operator.equals("%")) { // 나머지
				String rightValueStr = valueStack.pop(); // 우측 값 문자열
				String leftValueStr = valueStack.pop(); // 좌측 값 문자열
				
				Double leftNumberValue = getNumberValue(leftValueStr); // 좌측 값 숫자
				Double rightNumberValue = getNumberValue(rightValueStr); // 우측 값 숫자
				if (leftNumberValue != null && rightNumberValue != null) { // 둘 다 숫자라면,
					valueStack.push(
						((Double)(leftNumberValue % rightNumberValue)).toString() // 나머지를 저장
					);
				}
				else {
					throw new Error();
				}
			}
			else if (operator.equals("+")) { // 더하기
				String rightValueStr = valueStack.pop(); // 우측 값 문자열
				String leftValueStr = valueStack.pop(); // 좌측 값 문자열
				
				Double leftNumberValue = getNumberValue(leftValueStr); // 좌측 값 숫자
				Double rightNumberValue = getNumberValue(rightValueStr); // 우측 값 숫자
				
				String leftStringValue = getStringValue(leftValueStr); // 좌측 값 문자열
				String rightStringValue = getStringValue(rightValueStr); // 우측 값 문자열
				
				Boolean leftBooleanValue = getBooleanValue(leftValueStr); // 좌측 값 Boolean
				Boolean rightBooleanValue = getBooleanValue(rightValueStr); // 우측 값 Boolean
				
				if (leftValueStr.equals("") && rightNumberValue != null) { // 좌측이 비었고, 우측이 숫자라면,
					valueStack.push(
						((Double)(+1 * rightNumberValue)).toString() // 양수로 저장
					);
				}
				else if (leftNumberValue != null && rightNumberValue != null) { // 둘 다 숫자라면,
					valueStack.push(
						((Double)(leftNumberValue + rightNumberValue)).toString() // 더해서 저장
					);
				}
				else if (leftStringValue != null && rightStringValue != null) { // 둘 다 문자열이면,
					valueStack.push(
						ValueUtils.stringValueToString(
							ValueUtils.getRealStringValue(leftStringValue) +
							ValueUtils.getRealStringValue(rightStringValue) // 더해서 저장
						)
					);
				}
				else if (leftStringValue != null && rightNumberValue != null) { // 좌측이 문자열이고, 우측이 숫자라면,
					valueStack.push(
						ValueUtils.stringValueToString(
							ValueUtils.getRealStringValue(leftStringValue) +
							ValueUtils.numberValueToString(rightNumberValue) // 더해서 저장
						)
					);
				}
				else if (leftStringValue != null && rightBooleanValue != null) { // 좌측이 문자열이고, 우측이 Boolean이라면,
					valueStack.push(
						ValueUtils.stringValueToString(
							ValueUtils.getRealStringValue(leftStringValue) +
							ValueUtils.booleanValueToString(rightBooleanValue) // 더해서 저장
						)
					);
				}
				else if (leftNumberValue != null && rightStringValue != null) { // 좌측이 숫자이고, 우측이 문자열이면,
					valueStack.push(
						ValueUtils.stringValueToString(
							ValueUtils.numberValueToString(leftNumberValue) +
							ValueUtils.getRealStringValue(rightStringValue) // 더해서 저장
						)
					);
				}
				else if (leftBooleanValue != null && rightStringValue != null) { // 좌측이 Boolean이고, 우측이 문자열이면,
					valueStack.push(
							ValueUtils.stringValueToString(
							ValueUtils.booleanValueToString(leftBooleanValue) +
							ValueUtils.getRealStringValue(rightStringValue) // 더해서 저장
						)
					);
				}
				else {
					throw new Error();
				}
			}
			else if (operator.equals("-")) { // 빼기
				String rightValueStr = valueStack.pop(); // 우측 값 문자열
				String leftValueStr = valueStack.pop(); // 좌측 값 문자열
				
				Double leftNumberValue = getNumberValue(leftValueStr); // 좌측 값 숫자
				Double rightNumberValue = getNumberValue(rightValueStr); // 우측 값 숫자
				if (leftValueStr.equals("") && rightNumberValue != null) { // 좌측이 비었고, 우측이 숫자라면,
					valueStack.push(
						((Double)(-1 * rightNumberValue)).toString() // 음수로 저장
					);
				}
				else if (leftNumberValue != null && rightNumberValue != null) { // 둘 다 숫자라면,
					valueStack.push(
						((Double)(leftNumberValue - rightNumberValue)).toString() // 빼서 저장
					);
				}
				else {
					throw new Error();
				}
			}
			else if (operator.equals(">>")) { // 자바 명령어 실행
				String rightValueStr = valueStack.pop(); // 우측 값 문자열
				String leftValueStr = valueStack.pop(); // 좌측 값 문자열
				
				String rightStringValue = getStringValue(rightValueStr); // 우측 값 문자열
				if (leftValueStr.equals("") && rightStringValue != null) { // 좌측이 비었고, 우측이 문자열이라면,
					bsh.Interpreter bshi = new bsh.Interpreter();
					try {
						Object result = bshi.eval(ValueUtils.getRealStringValue(rightStringValue));
						if (result != null) { // 결과가 있을때,
							if (result instanceof String) { // 결과가 문자열이면,
								valueStack.push(ValueUtils.stringValueToString((String) result)); // 문자열 형식으로 저장.
							} else {
								valueStack.push(result.toString());
							}
						}
					} catch (EvalError e) {
						throw new Error();
					}
				}
				else {
					throw new Error();
				}
			}
			else if (operator.equals("==")) { // 같다
				String rightValueStr = valueStack.pop(); // 우측 값 문자열
				String leftValueStr = valueStack.pop(); // 좌측 값 문자열
				
				Double leftNumberValue = getNumberValue(leftValueStr); // 좌측 값 숫자
				Double rightNumberValue = getNumberValue(rightValueStr); // 우측 값 숫자
				
				Boolean leftBooleanValue = getBooleanValue(leftValueStr); // 좌측 값 Boolean
				Boolean rightBooleanValue = getBooleanValue(rightValueStr); // 우측 값 Boolean
				
				String leftStringValue = getStringValue(leftValueStr); // 좌측 값 문자열
				String rightStringValue = getStringValue(rightValueStr); // 우측 값 문자열
				
				if (leftNumberValue != null && rightNumberValue != null) { // 둘 다 숫자라면,
					if (leftNumberValue == rightNumberValue) {
						valueStack.push("참");
					} else {
						valueStack.push("거짓");
					}
				}
				else if (leftBooleanValue != null && rightBooleanValue != null) { // 둘 다 Boolean이라면,
					if (leftBooleanValue == rightBooleanValue) {
						valueStack.push("참");
					} else {
						valueStack.push("거짓");
					}
				}
				else if (leftStringValue != null && rightStringValue != null) { // 둘 다 문자라면,
					if (ValueUtils.getRealStringValue(leftStringValue).equals(ValueUtils.getRealStringValue(rightStringValue))) {
						valueStack.push("참");
					} else {
						valueStack.push("거짓");
					}
				}
				else {
					throw new Error();
				}
			}
			else if (operator.equals("!=")) { // 다르다
				String rightValueStr = valueStack.pop(); // 우측 값 문자열
				String leftValueStr = valueStack.pop(); // 좌측 값 문자열
				
				Double leftNumberValue = getNumberValue(leftValueStr); // 좌측 값 숫자
				Double rightNumberValue = getNumberValue(rightValueStr); // 우측 값 숫자
				
				Boolean leftBooleanValue = getBooleanValue(leftValueStr); // 좌측 값 Boolean
				Boolean rightBooleanValue = getBooleanValue(rightValueStr); // 우측 값 Boolean
				
				String leftStringValue = getStringValue(leftValueStr); // 좌측 값 문자열
				String rightStringValue = getStringValue(rightValueStr); // 우측 값 문자열
				
				if (leftNumberValue != null && rightNumberValue != null) { // 둘 다 숫자라면,
					if (leftNumberValue != rightNumberValue) {
						valueStack.push("참");
					} else {
						valueStack.push("거짓");
					}
				}
				else if (leftBooleanValue != null && rightBooleanValue != null) { // 둘 다 Boolean이라면,
					if (leftBooleanValue != rightBooleanValue) {
						valueStack.push("참");
					} else {
						valueStack.push("거짓");
					}
				}
				else if (leftStringValue != null && rightStringValue != null) { // 둘 다 문자라면,
					if (!ValueUtils.getRealStringValue(leftStringValue).equals(ValueUtils.getRealStringValue(rightStringValue))) {
						valueStack.push("참");
					} else {
						valueStack.push("거짓");
					}
				}
				else {
					throw new Error();
				}
			}
			else if (operator.equals("<")) { // 작다
				String rightValueStr = valueStack.pop(); // 우측 값 문자열
				String leftValueStr = valueStack.pop(); // 좌측 값 문자열
				
				Double leftNumberValue = getNumberValue(leftValueStr); // 좌측 값 숫자
				Double rightNumberValue = getNumberValue(rightValueStr); // 우측 값 숫자
				if (leftNumberValue != null && rightNumberValue != null) { // 둘 다 숫자라면,
					if (leftNumberValue < rightNumberValue) {
						valueStack.push("참");
					} else {
						valueStack.push("거짓");
					}
				}
				else {
					throw new Error();
				}
			}
			else if (operator.equals("<=")) { // 작거나 같다
				String rightValueStr = valueStack.pop(); // 우측 값 문자열
				String leftValueStr = valueStack.pop(); // 좌측 값 문자열
				
				Double leftNumberValue = getNumberValue(leftValueStr); // 좌측 값 숫자
				Double rightNumberValue = getNumberValue(rightValueStr); // 우측 값 숫자
				if (leftNumberValue != null && rightNumberValue != null) { // 둘 다 숫자라면,
					if (leftNumberValue <= rightNumberValue) {
						valueStack.push("참");
					} else {
						valueStack.push("거짓");
					}
				}
				else {
					throw new Error();
				}
			}
			else if (operator.equals(">")) { // 크다
				String rightValueStr = valueStack.pop(); // 우측 값 문자열
				String leftValueStr = valueStack.pop(); // 좌측 값 문자열
				
				Double leftNumberValue = getNumberValue(leftValueStr); // 좌측 값 숫자
				Double rightNumberValue = getNumberValue(rightValueStr); // 우측 값 숫자
				if (leftNumberValue != null && rightNumberValue != null) { // 둘 다 숫자라면,
					if (leftNumberValue > rightNumberValue) {
						valueStack.push("참");
					} else {
						valueStack.push("거짓");
					}
				}
				else {
					throw new Error();
				}
			}
			else if (operator.equals(">=")) { // 크거나 같다
				String rightValueStr = valueStack.pop(); // 우측 값 문자열
				String leftValueStr = valueStack.pop(); // 좌측 값 문자열
				
				Double leftNumberValue = getNumberValue(leftValueStr); // 좌측 값 숫자
				Double rightNumberValue = getNumberValue(rightValueStr); // 우측 값 숫자
				if (leftNumberValue != null && rightNumberValue != null) { // 둘 다 숫자라면,
					if (leftNumberValue >= rightNumberValue) {
						valueStack.push("참");
					} else {
						valueStack.push("거짓");
					}
				}
				else {
					throw new Error();
				}
			}
			else if (operator.equals("!")) { // 반대
				String rightValueStr = valueStack.pop(); // 우측 값 문자열
				String leftValueStr = valueStack.pop(); // 좌측 값 문자열
				
				Boolean rightBooleanValue = getBooleanValue(rightValueStr); // 우측 값 Boolean
				if (leftValueStr.equals("") && rightBooleanValue != null) { // 좌측이 비었고, 우측이 Boolean이라면,
					if (!rightBooleanValue) {
						valueStack.push("참");
					} else {
						valueStack.push("거짓");
					}
				}
				else {
					throw new Error();
				}
			}
			else if (operator.equals("|") || operator.equals("||")) { // OR			
				String rightValueStr = valueStack.pop(); // 우측 값 문자열
				String leftValueStr = valueStack.pop(); // 좌측 값 문자열
				
				Boolean leftBooleanValue = getBooleanValue(leftValueStr); // 좌측 값 Boolean
				Boolean rightBooleanValue = getBooleanValue(rightValueStr); // 우측 값 Boolean
				if (leftBooleanValue != null && rightBooleanValue != null) { // 둘 다 Boolean이라면,
					if (leftBooleanValue || rightBooleanValue) {
						valueStack.push("참");
					} else {
						valueStack.push("거짓");
					}
				}
				else {
					throw new Error();
				}
			}
			else if (operator.equals("&") || operator.equals("&&")) { // AND			
				String rightValueStr = valueStack.pop(); // 우측 값 문자열
				String leftValueStr = valueStack.pop(); // 좌측 값 문자열
				
				Boolean leftBooleanValue = getBooleanValue(leftValueStr); // 좌측 값 Boolean
				Boolean rightBooleanValue = getBooleanValue(rightValueStr); // 우측 값 Boolean
				if (leftBooleanValue != null && rightBooleanValue != null) { // 둘 다 Boolean이라면,
					if (leftBooleanValue && rightBooleanValue) {
						valueStack.push("참");
					} else {
						valueStack.push("거짓");
					}
				}
				else {
					throw new Error();
				}
			}
			else if (operator.equals("=")) { // 넣기				
				String rightValueStr = valueStack.pop(); // 우측 값 문자열
				String leftValueStr = valueStack.pop(); // 좌측 값 문자열
				
				String valueStr = valueToString(rightValueStr);
				
				if (valueStr != null) {
					vm.insertVar(leftValueStr, filePath, line, valueStr); // 변수 저장
				}
				else {
					throw new Error();
				}
				
				valueStack.push(leftValueStr); // 다시 좌측 값을 곱게 넣어줍니다.
			}
			else if (operator.equals("*=")) { // 곱해서 넣기			
				String rightValueStr = valueStack.pop(); // 우측 값 문자열
				String leftValueStr = valueStack.pop(); // 좌측 값 문자열
				
				Var leftValueVar = vm.getVar(leftValueStr); // 좌측 값 변수
				Double rightNumberValue = getNumberValue(rightValueStr); // 우측 값 숫자
				if (leftValueVar != null && rightNumberValue != null) {
					leftValueVar.setSource(
						((Double)(getNumberValue(leftValueVar.getSource()) * rightNumberValue)).toString() // 우측 값 숫자를 곱해서 저장
					);
					valueStack.push(leftValueStr); // 다시 좌측 값을 곱게 넣어줍니다.
				}
				else {
					throw new Error();
				}
			}
			else if (operator.equals("/=")) { // 나눠서 넣기			
				String rightValueStr = valueStack.pop(); // 우측 값 문자열
				String leftValueStr = valueStack.pop(); // 좌측 값 문자열
				
				Var leftValueVar = vm.getVar(leftValueStr); // 좌측 값 변수
				Double rightNumberValue = getNumberValue(rightValueStr); // 우측 값 숫자
				if (leftValueVar != null && rightNumberValue != null) {
					leftValueVar.setSource(
						((Double)(getNumberValue(leftValueVar.getSource()) / rightNumberValue)).toString() // 우측 값 숫자를 나눠서 저장
					);
					valueStack.push(leftValueStr); // 다시 좌측 값을 곱게 넣어줍니다.
				}
				else {
					throw new Error();
				}
			}
			else if (operator.equals("%=")) { // 나머지 넣기			
				String rightValueStr = valueStack.pop(); // 우측 값 문자열
				String leftValueStr = valueStack.pop(); // 좌측 값 문자열
				
				Var leftValueVar = vm.getVar(leftValueStr); // 좌측 값 변수
				Double rightNumberValue = getNumberValue(rightValueStr); // 우측 값 숫자
				if (leftValueVar != null && rightNumberValue != null) {
					leftValueVar.setSource(
						((Double)(getNumberValue(leftValueVar.getSource()) % rightNumberValue)).toString() // 우측 값 숫자를 나머지한 뒤 저장
					);
					valueStack.push(leftValueStr); // 다시 좌측 값을 곱게 넣어줍니다.
				}
				else {
					throw new Error();
				}
			}
			else if (operator.equals("+=")) { // 더해서 넣기			
				String rightValueStr = valueStack.pop(); // 우측 값 문자열
				String leftValueStr = valueStack.pop(); // 좌측 값 문자열
				
				Var leftValueVar = vm.getVar(leftValueStr); // 좌측 값 변수
				Double rightNumberValue = getNumberValue(rightValueStr); // 우측 값 숫자
				String rightStringValue = getStringValue(rightValueStr); // 우측 값 문자열
				
				if (leftValueVar != null && rightNumberValue != null) {
					leftValueVar.setSource(
						((Double)(getNumberValue(leftValueVar.getSource()) + rightNumberValue)).toString() // 우측 값 숫자를 더해서 저장
					);
					valueStack.push(leftValueStr); // 다시 좌측 값을 곱게 넣어줍니다.
				}
				else if (leftValueVar != null && rightStringValue != null) {
					String string = getStringValue(leftValueVar.getSource());
					leftValueVar.setSource(
						ValueUtils.getRealStringValue(string) + ValueUtils.getRealStringValue(rightStringValue) // 우측 값 문자열을 더해서 저장
					);
					valueStack.push(leftValueStr); // 다시 좌측 값을 곱게 넣어줍니다.
				}
				else {
					throw new Error();
				}
			}
			else if (operator.equals("-=")) { // 빼서 넣기			
				String rightValueStr = valueStack.pop(); // 우측 값 문자열
				String leftValueStr = valueStack.pop(); // 좌측 값 문자열
				
				Var leftValueVar = vm.getVar(leftValueStr); // 좌측 값 변수
				Double rightNumberValue = getNumberValue(rightValueStr); // 우측 값 숫자
				if (leftValueVar != null && rightNumberValue != null) {
					leftValueVar.setSource(
						((Double)(getNumberValue(leftValueVar.getSource()) - rightNumberValue)).toString() // 우측 값 숫자를 뺀 뒤 저장
					);
					valueStack.push(leftValueStr); // 다시 좌측 값을 곱게 넣어줍니다.
				}
				else {
					throw new Error();
				}
			}
			else {
				throw new Error();
			}
		} catch (EmptyStackException e) {
			throw new Error();
		}
	}
	
	/**
	 * 토큰인가?
	 */
	private boolean isToken(String token) {
		if (token.equals(".")) { // 마침표가 소수점을 표현하는 경우엔 토큰이 아닙니다.
			if (ValueUtils.getNumberValue(StringUtils.getSubstring(source, index + 1, 1)) != null) {
				return false;
			}
		}
		return StringUtils.getSubstring(source, index, token.length()).equals(token);
	}
	
	/**
	 * 문자열에서 숫자를 구합니다. (가상머신에 값이 있으면 불러옵니다.)
	 */
	private Double getNumberValue(String numberStr) {
		numberStr = VMSourceTunnel(numberStr); // 가상머신의 값을 불러옵니다. (없으면 현재 값을 유지합니다.)
		return ValueUtils.getNumberValue(numberStr);
	}
	
	/**
	 * 문자열에서 Boolean을 구합니다. (가상머신에 값이 있으면 불러옵니다.)
	 */
	private Boolean getBooleanValue(String booleanStr) {
		booleanStr = VMSourceTunnel(booleanStr); // 가상머신의 값을 불러옵니다. (없으면 현재 값을 유지합니다.)
		return ValueUtils.getBooleanValue(booleanStr);
	}
	
	/**
	 * 문자열에서 문자열을 구합니다. (가상머신에 값이 있으면 불러옵니다.)
	 */
	private String getStringValue(String stringStr) {
		stringStr = VMSourceTunnel(stringStr); // 가상머신의 값을 불러옵니다. (없으면 현재 값을 유지합니다.)
		return ValueUtils.getStringValue(stringStr);
	}
	
	/**
	 * 어떤 값을 문자열로 만든다. (가상머신에 값이 있으면 불러옵니다.)
	 */
	private String valueToString(String valueStr) {
		valueStr = VMSourceTunnel(valueStr); // 가상머신의 값을 불러옵니다. (없으면 현재 값을 유지합니다.)
		return ValueUtils.valueToString(valueStr);
	}
	
	/**
	 * 가상머신의 값을 불러옵니다. (없으면 현재 값을 유지합니다.)
	 */
	private String VMSourceTunnel(String string) {
		Var var = vm.getVar(string);
		if (var != null) {
			return var.getSource(); // 변수의 값을 불러옵니다.
		}
		return string;
	}

}
