package jadx.gui.plugins;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import jadx.api.JavaClass;
import jadx.api.plugins.JadxPlugin;
import jadx.api.plugins.JadxPluginContext;
import jadx.api.plugins.JadxPluginInfo;
import jadx.api.plugins.data.IJadxPlugins;
import jadx.api.plugins.data.JadxPluginRuntimeData;
import jadx.core.Jadx;
import jadx.core.plugins.PluginContext;
import jadx.gui.JadxWrapper;
import jadx.gui.plugins.context.GuiPluginContext;
import jadx.gui.ui.MainWindow;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.List;

public class MCPHttpServer implements JadxPlugin {
	private final MainWindow mainWindow;
	private final Gson gson = new Gson();

	private static final Path EXPORT_PATH = Paths.get("/tmp/jadx-export");


	@Override
	public void init(JadxPluginContext context) {
		//context.getAppContext().getGuiContext().getMainFrame();

		MainWindow mainWindow = (MainWindow) context.getGuiContext().getMainFrame(); //context.getAppContext().getGuiContext().getMainFrame();;//context.getMainWindow();
		System.out.println("MCP HTTP Plugin: Starting HTTP server...");
		new MCPHttpServer(mainWindow).start();
	}


	public MCPHttpServer(MainWindow mainWindow) {
		this.mainWindow = mainWindow;
	}

	public void start() {
		//if (this.mainWindow.getProject().isSaveFileSelected()) {
		//	mainWindow.getProject().save();
		//} else {
	//		this.mainWindow.getProject().saveAs(EXPORT_PATH);
			//this.mainWindow.saveProjectAs();
	//	}


		try {
			HttpServer server = HttpServer.create(new InetSocketAddress(8650), 0);
			server.createContext("/current-class", new CurrentClassHandler());
			server.createContext("/all-classes", new AllClassesHandler());
			server.createContext("/selected-text", new SelectedTextHandler());
			server.createContext("/method-by-name", new MethodByNameHandler());
			server.createContext("/class-source", new ClassSourceHandler());
			server.createContext("/search-method", new SearchMethodHandler());
			server.createContext("/methods-of-class", new MethodsOfClassHandler());
			server.createContext("/fields-of-class", new FieldsOfClassHandler());
			server.createContext("/smali-of-class", new SmaliOfClassHandler());

			server.setExecutor(null);
			server.start();
			System.out.println("JADX MCP plugin HTTP server started at http://localhost:8650/");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public JadxPluginInfo getPluginInfo() {
		return null;
	}

	class CurrentClassHandler implements HttpHandler {
		@Override
		public void handle(HttpExchange exchange) throws IOException {
			String className = getSelectedTabTitle();
			String code = extractTextFromCurrentTab();

			Map<String, Object> result = new HashMap<>();
			result.put("name", className != null ? className.replace(".java", "") : "unknown");
			result.put("type", "code/java");
			result.put("content", code != null ? code : "");

			sendJson(exchange, 200, result);
		}
	}


	class AllClassesHandler implements HttpHandler {
		@Override
		public void handle(HttpExchange exchange) throws IOException {
			List<String> classList = new ArrayList<>();

			try {
				JadxWrapper wrapper = mainWindow.getWrapper();

				List<JavaClass> classes = wrapper.getIncludedClassesWithInners();
				for (JavaClass cls : classes) {
					classList.add(cls.getFullName());
				}

				Map<String, Object> result = new HashMap<>();
				result.put("type", "class-list");
				result.put("count", classList.size());
				result.put("classes", classList);

				sendJson(exchange, 200, result);
			} catch (Exception e) {
				e.printStackTrace();
				sendJson(exchange, 500, Map.of("error", "Failed to load class list."));
			}
		}
	}

	class SelectedTextHandler implements HttpHandler {
		@Override
		public void handle(HttpExchange exchange) throws IOException {
			JTextArea textArea = findTextArea(mainWindow.getTabbedPane().getSelectedComponent());
			String selectedText = textArea != null ? textArea.getSelectedText() : null;

			Map<String, String> result = new HashMap<>();
			result.put("selectedText", selectedText != null ? selectedText : "");
			sendJson(exchange, 200, result);
		}
	}

	class MethodByNameHandler implements HttpHandler {
		@Override
		public void handle(HttpExchange exchange) throws IOException {
			Map<String, String> params = queryToMap(exchange.getRequestURI().getQuery());
			String methodName = params.get("method");

			if (methodName == null || methodName.isEmpty()) {
				sendJson(exchange, 400, Map.of("error", "Missing 'method' parameter"));
				return;
			}

			try {
				JadxWrapper wrapper = mainWindow.getWrapper();
				if (wrapper == null) {
					sendJson(exchange, 500, Map.of("error", "JadxWrapper not initialized"));
					return;
				}

				for (JavaClass cls : wrapper.getIncludedClassesWithInners()) {
					for (jadx.api.JavaMethod method : cls.getMethods()) {
						if (method.getName().equalsIgnoreCase(methodName)) {
							String codeStr;
							try {
								codeStr = method.getCodeStr();
							} catch (Exception e) {
								codeStr = "Error retrieving code: " + e.getMessage();
							}

							Map<String, Object> result = new HashMap<>();
							result.put("class", cls.getFullName());
							result.put("method", method.getName());
							result.put("decl", String.valueOf(method.getCodeNodeRef()));
							result.put("code", codeStr);
							sendJson(exchange, 200, result);
							return;
						}
					}
				}

				sendJson(exchange, 404, Map.of("error", "Method not found in any class."));
			} catch (Exception e) {
				e.printStackTrace(); // Also log this to your IDE or terminal
				sendJson(exchange, 500, Map.of("error", "Internal error: " + e.getMessage()));
			}
		}
	}



		class ClassSourceHandler implements HttpHandler {
		@Override
		public void handle(HttpExchange exchange) throws IOException {
			Map<String, String> params = queryToMap(exchange.getRequestURI().getQuery());
			String className = params.get("class");

			if (className == null || className.isEmpty()) {
				sendJson(exchange, 400, Map.of("error", "Missing 'class' parameter."));
				return;
			}

			try {
				JadxWrapper wrapper = mainWindow.getWrapper();
				for (JavaClass cls : wrapper.getIncludedClassesWithInners()) {
					if (cls.getFullName().equals(className)) {
						sendJson(exchange, 200, Map.of(
								"class", className,
								"type", "code/java",
								"content", cls.getCode()
						));
						return;
					}
				}
				sendJson(exchange, 404, Map.of("error", "Class not found."));
			} catch (Exception e) {
				e.printStackTrace();
				sendJson(exchange, 500, Map.of("error", "Internal error retrieving class source."));
			}
		}
	}


	class SearchMethodHandler implements HttpHandler {
		@Override
		public void handle(HttpExchange exchange) throws IOException {
			Map<String, String> params = queryToMap(exchange.getRequestURI().getQuery());
			String methodName = params.get("method");
			List<String> results = new ArrayList<>();

			if (methodName == null) {
				sendJson(exchange, 400, Map.of("error", "Missing method parameter"));
				return;
			}

			try {
				JadxWrapper wrapper = mainWindow.getWrapper();
				for (JavaClass cls : wrapper.getIncludedClassesWithInners()) {
					if (cls.getCode().contains(methodName)) {
						results.add(cls.getFullName());
					}
				}
				sendJson(exchange, 200, results);
			} catch (Exception e) {
				e.printStackTrace();
				sendJson(exchange, 500, Map.of("error", "Internal error during method search"));
			}
		}
	}


	class MethodsOfClassHandler implements HttpHandler {
		@Override
		public void handle(HttpExchange exchange) throws IOException {
			Map<String, String> params = queryToMap(exchange.getRequestURI().getQuery());
			String className = params.get("class");

			try {
				JadxWrapper wrapper = mainWindow.getWrapper();
				for (JavaClass cls : wrapper.getIncludedClassesWithInners()) {
					if (cls.getFullName().equals(className)) {
						List<String> methods = cls.getMethods().stream()
								.map(m -> m.getAccessFlags() + " " + m.getReturnType() + " " + m.getName() + m.getMethodNode() + m.getFullName())
								.toList();
						sendJson(exchange, 200, methods);
						return;
					}
				}
				sendJson(exchange, 404, Map.of("error", "Class not found."));
			} catch (Exception e) {
				e.printStackTrace();
				sendJson(exchange, 500, Map.of("error", "Internal error retrieving methods."));
			}
		}
	}


	class FieldsOfClassHandler implements HttpHandler {
		@Override
		public void handle(HttpExchange exchange) throws IOException {
			Map<String, String> params = queryToMap(exchange.getRequestURI().getQuery());
			String className = params.get("class");

			try {
				JadxWrapper wrapper = mainWindow.getWrapper();
				for (JavaClass cls : wrapper.getIncludedClassesWithInners()) {
					if (cls.getFullName().equals(className)) {
						List<String> fields = cls.getFields().stream()
								.map(f -> f.getAccessFlags() + " " + f.getType() + " " + f.getName())
								.toList();
						sendJson(exchange, 200, fields);
						return;
					}
				}
				sendJson(exchange, 404, Map.of("error", "Class not found."));
			} catch (Exception e) {
				e.printStackTrace();
				sendJson(exchange, 500, Map.of("error", "Internal error retrieving fields."));
			}
		}
	}


	class SmaliOfClassHandler implements HttpHandler {
		@Override
		public void handle(HttpExchange exchange) throws IOException {
			Map<String, String> params = queryToMap(exchange.getRequestURI().getQuery());
			String className = params.get("class");

			if (className == null || className.isEmpty()) {
				sendJson(exchange, 400, Map.of("error", "Missing 'class' parameter."));
				return;
			}

			try {
				JadxWrapper wrapper = mainWindow.getWrapper();
				for (JavaClass cls : wrapper.getIncludedClassesWithInners()) {
					if (cls.getFullName().equals(className)) {
						sendJson(exchange, 200, Map.of(
								"class", className,
								"type", "code/smali",
								"content", cls.getSmali()
						));
						return;
					}
				}
				sendJson(exchange, 404, Map.of("error", "Class not found."));
			} catch (Exception e) {
				e.printStackTrace();
				sendJson(exchange, 500, Map.of("error", "Internal error retrieving class source."));
			}
		}
	}


	// ----------------- Helpers ------------------

	private String getSelectedTabTitle() {
		JTabbedPane tabs = mainWindow.getTabbedPane();
		int index = tabs.getSelectedIndex();
		return (index != -1) ? tabs.getTitleAt(index) : null;
	}

	private String extractTextFromCurrentTab() {
		Component selectedComponent = mainWindow.getTabbedPane().getSelectedComponent();
		JTextArea textArea = findTextArea(selectedComponent);
		return textArea != null ? textArea.getText() : null;
	}

	private JTextArea findTextArea(Component component) {
		if (component instanceof JTextArea) return (JTextArea) component;
		if (component instanceof Container) {
			for (Component child : ((Container) component).getComponents()) {
				JTextArea result = findTextArea(child);
				if (result != null) return result;
			}
		}
		return null;
	}

	private void sendJson(HttpExchange exchange, int statusCode, Object responseObj) throws IOException {
		String json = gson.toJson(responseObj);
		byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
		exchange.getResponseHeaders().add("Content-Type", "application/json");
		exchange.sendResponseHeaders(statusCode, bytes.length);
		try (OutputStream os = exchange.getResponseBody()) {
			os.write(bytes);
		}
	}

	private Path findSourcesDir() {
		Path sourcesDir = EXPORT_PATH.resolve("sources");
		return Files.exists(sourcesDir) ? sourcesDir : null;
	}

	private Map<String, String> queryToMap(String query) {
		Map<String, String> map = new HashMap<>();
		if (query == null) return map;
		for (String param : query.split("&")) {
			String[] pair = param.split("=");
			if (pair.length == 2) {
				map.put(pair[0], java.net.URLDecoder.decode(pair[1], StandardCharsets.UTF_8));
			}
		}
		return map;
	}

	private String readJavaSourceFromDisk(String className) {
		try {
			Path sourcesDir = findSourcesDir();
			if (sourcesDir == null) return null;
			Path classPath = sourcesDir.resolve(className.replace('.', File.separatorChar) + ".java");
			return Files.readString(classPath);
		} catch (IOException e) {
			return null;
		}
	}
	

	private int countChar(String str, char ch) {
		int count = 0;
		for (char c : str.toCharArray()) {
			if (c == ch) count++;
		}
		return count;
	}
}
