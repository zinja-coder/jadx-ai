package jadx.gui.plugins;

import jadx.gui.ui.MainWindow;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class MCPIntegrationPlugin {
	private final MainWindow mainWindow;

	public MCPIntegrationPlugin(MainWindow mainWindow) {
		this.mainWindow = mainWindow;
		setupMenuItem();
		startHttpBridge(); // simplified
	}

	private void setupMenuItem() {
		JMenu toolsMenu = mainWindow.getJMenuBar().getMenu(3); // Tools menu
		JMenuItem sendToMCPItem = new JMenuItem("Send Decompiled Code to MCP");

		sendToMCPItem.addActionListener((ActionEvent e) -> {
			sendToMCP(); // no need to extract code here anymore
		});

		toolsMenu.add(sendToMCPItem);
	}

	private void startHttpBridge() {
		new MCPHttpServer(mainWindow).start();
	}

	private void sendToMCP() {
		try {
			URL url = new URL("http://localhost:8650/current-class");
			HttpURLConnection getConn = (HttpURLConnection) url.openConnection();
			getConn.setRequestMethod("GET");
			getConn.setRequestProperty("Accept", "application/json");

			if (getConn.getResponseCode() != 200) {
				JOptionPane.showMessageDialog(mainWindow, "Failed to get code from plugin server.");
				return;
			}

			String responseJson = new String(getConn.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
			getConn.disconnect();

			URL postUrl = new URL("http://localhost:8000/upload");
			HttpURLConnection postConn = (HttpURLConnection) postUrl.openConnection();
			postConn.setRequestMethod("POST");
			postConn.setRequestProperty("Content-Type", "application/json");
			postConn.setDoOutput(true);

			try (OutputStream os = postConn.getOutputStream()) {
				byte[] input = responseJson.getBytes(StandardCharsets.UTF_8);
				os.write(input, 0, input.length);
			}

			int status = postConn.getResponseCode();
			if (status == 200) {
				JOptionPane.showMessageDialog(mainWindow, "Code sent to MCP server.");
			} else {
				JOptionPane.showMessageDialog(mainWindow, "Failed to send code. Status: " + status);
			}

			postConn.disconnect();
		} catch (Exception ex) {
			JOptionPane.showMessageDialog(mainWindow, "Error: " + ex.getMessage());
		}
	}
}
