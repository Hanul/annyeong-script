package 안녕Script;

public class Var {
	private String name;
	private String filePath;
	private int line;
	private String source;

	public Var(String name, String filePath, int line, String source) {
		this.name = name;
		this.filePath = filePath;
		this.line = line;
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

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	@Override
	public String toString() {
		return source;
	}

}
