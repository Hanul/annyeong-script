package 안녕Script;

import java.util.List;

public class Method {
	private String name;
	private String filePath;
	private int line;
	private List<String> 에ParameterNames;
	private List<String> 을ParameterNames;
	private String source;

	public Method(String name, String filePath, int line,
			List<String> 에ParameterNames, List<String> 을ParameterNames,
			String source) {
		this.name = name;
		this.filePath = filePath;
		this.line = line;
		this.에ParameterNames = 에ParameterNames;
		this.을ParameterNames = 을ParameterNames;
		this.source = source;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	public int getLine() {
		return line;
	}

	public void setLine(int line) {
		this.line = line;
	}

	public List<String> get에ParameterNames() {
		return 에ParameterNames;
	}

	public void set에ParameterNames(List<String> 에ParameterNames) {
		this.에ParameterNames = 에ParameterNames;
	}

	public List<String> get을ParameterNames() {
		return 을ParameterNames;
	}

	public void set을ParameterNames(List<String> 을ParameterNames) {
		this.을ParameterNames = 을ParameterNames;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	@Override
	public String toString() {
		return "Method [name=" + name + ", filePath=" + filePath + ", line="
				+ line + ", 에ParameterNames=" + 에ParameterNames
				+ ", 을ParameterNames=" + 을ParameterNames + ", source=" + source
				+ "]";
	}

}
