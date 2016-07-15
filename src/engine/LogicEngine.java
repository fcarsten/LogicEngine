/**
 * Copyright 2003-2016 Carsten Friedrich (Carsten.Friedrich@gmail.com) and Colin Murray
 *
 * License: GNU GENERAL PUBLIC LICENSE 3.0 (https://www.gnu.org/copyleft/gpl.html)
 *
 */
package engine;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JApplet;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.text.BadLocationException;

public class LogicEngine extends JApplet implements ActionListener {
	private JButton button;
	private static JTextArea textArea;
	public static JTextArea outArea;

	static {
		LogicEngine.textArea = new JTextArea(5, 20);
		LogicEngine.outArea = new JTextArea(10, 20);
	}

	public LogicEngine() {
		this.button = new JButton("Create Engine");
	}

	public void startEngine() {
		final int numVariables = 3;
		String line = null;
		final Vector<Vector<String>> formula = new Vector<>();
		final String input = LogicEngine.textArea.getText();
		for (int nl = 0; nl < LogicEngine.textArea.getLineCount(); ++nl) {
			try {
				if (LogicEngine.textArea.getLineEndOffset(nl) - 1 > LogicEngine.textArea.getLineStartOffset(nl)) {
					line = LogicEngine.textArea.getText(LogicEngine.textArea.getLineStartOffset(nl),
							LogicEngine.textArea.getLineEndOffset(nl) - LogicEngine.textArea.getLineStartOffset(nl));
					final Vector<String> clause = new Vector<>(numVariables);
					final StringTokenizer st = new StringTokenizer(line);
					if (st.countTokens() != numVariables) {
						if (st.countTokens() == 0) {
							break;
						}
						LogicEngine.outArea
								.append("Error: Enter " + numVariables + " variables per clause: \"" + line + "\"\n");
						return;
					} else {
						for (int i = 0; i != numVariables; ++i) {
							final String var = st.nextToken();
							clause.add(var);
						}
						formula.add(clause);
					}
				}
			} catch (BadLocationException e) {
				LogicEngine.outArea.append("Internal parser error in line \"" + nl + "\": " + e + "\n");
			}
		}
		final EngLogic eng = new EngLogic(formula);
		try {
			new EngApp(eng.topConfiguration, eng.bottomConfiguration, eng.variables);
		} catch (NoClassDefFoundError e2) {
			JOptionPane.showMessageDialog(null, "Could not find Java3D classes.\nPlease check your installation.",
					"Alert", 0);
			System.exit(-1);
		}
	}

	@Override
	public void actionPerformed(final ActionEvent e) {
		this.startEngine();
	}

	@Override
	public void init() {
		this.buildFrame(this.getContentPane());
	}

	public static void main(final String[] arg) {
		final LogicEngine le = new LogicEngine();
		final JFrame frame = new JFrame("Logic Engine Input");
		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(final WindowEvent e) {
				System.exit(0);
			}
		});
		le.buildFrame(frame.getContentPane());
		frame.pack();
		frame.setVisible(true);
	}

	public void buildFrame(final Container panel) {
		LogicEngine.outArea.setLineWrap(true);
		LogicEngine.outArea.setWrapStyleWord(true);
		this.button.addActionListener(this);
		this.button.setHorizontalAlignment(0);
		this.button.setBorder(BorderFactory.createMatteBorder(1, 1, 2, 2, Color.black));
		LogicEngine.textArea.setBorder(BorderFactory.createMatteBorder(1, 1, 2, 2, Color.black));
		LogicEngine.outArea.setBorder(BorderFactory.createMatteBorder(1, 1, 2, 2, Color.black));
		final JPanel textPanel = new JPanel();
		textPanel.setLayout(new BorderLayout());
		panel.setLayout(new BorderLayout());
		panel.add(this.button, "South");
		textPanel.add(new JScrollPane(LogicEngine.textArea), "North");
		textPanel.add(new JScrollPane(LogicEngine.outArea), "South");
		LogicEngine.outArea.setText("Output:\n");
		panel.add(textPanel, "Center");
	}
}
