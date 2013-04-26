package br.ufrgs.ppgc.gia.jhekaton.view;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import br.ufrgs.ppgc.gia.jhekaton.StateType;
import br.ufrgs.ppgc.gia.jhekaton.xml.Model.PackagedElement.Region;
import br.ufrgs.ppgc.gia.jhekaton.xml.Model.PackagedElement.Region.Subvertex;
import br.ufrgs.ppgc.gia.jhekaton.xml.Model.PackagedElement.Region.Transition;
import br.ufrgs.ppgc.gia.jhekaton.xml.XMI;

import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxICell;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.view.mxGraph;

public class JHekaton {

	private JFrame frmJhekaton;
	private JTextField txtFile;
	private JPanel panelDiagram;
	private File file;
	private JTextArea txtLogs;
	private StatusBar statusBar;
	private mxGraph diagram;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					JHekaton window = new JHekaton();
					window.frmJhekaton.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public JHekaton() {
		initialize();
	}

	private void updateDiagram(List<Subvertex> vertex, List<Transition> trans ){
		
		diagram.getModel().beginUpdate();
		diagram.removeCells(diagram.getChildVertices(diagram.getDefaultParent()));

		Object parent = diagram.getDefaultParent();

		try {
			long position = 20;
			long prevWidth = 0;
			
			List lista = new ArrayList();
			
			for (Subvertex subvertex : vertex) {
				prevWidth = subvertex.getName().length() * 7;
				Object o;
				if(subvertex.getType().equalsIgnoreCase(StateType.INITIAL.getUmlType())){
					o = diagram.insertVertex(parent, subvertex.getId(), subvertex.getName(), position, position, prevWidth, 27,"shape=ellipse;perimeter=100;whiteSpace=wrap;fillColor=green");
				} else if(subvertex.getType().equalsIgnoreCase(StateType.FINAL.getUmlType())){
					o = diagram.insertVertex(parent, subvertex.getId(), subvertex.getName(), position, position, prevWidth, 27,"shape=ellipse;perimeter=100;whiteSpace=wrap;fillColor=red");
				} else {
					o = diagram.insertVertex(parent, subvertex.getId(), subvertex.getName(), position, position, prevWidth, 27);
				}
				lista.add(o);
				position += prevWidth+5;
			}
			
			for (Transition transition : trans) {
				diagram.insertEdge(parent, null, "Edge", find(transition.getSource(), lista), find(transition.getTarget(), lista));
			}
			
		}
		finally {
			diagram.getModel().endUpdate();
		}
	}
	
	private Object find(String id, List vet){
		for (Object object : vet) {
			if(((mxICell)object).getId().equalsIgnoreCase(id)){
				return object;
			}
		}
		return null;
	}
	
	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frmJhekaton = new JFrame();
		frmJhekaton.setTitle("JHekaton");
		frmJhekaton.setBounds(100, 100, 647, 450);
		frmJhekaton.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frmJhekaton.getContentPane().setLayout(new BorderLayout(0, 0));
		
		JTabbedPane tabs = new JTabbedPane(JTabbedPane.TOP);
		frmJhekaton.getContentPane().add(tabs);
		
		panelDiagram = new JPanel();
		panelDiagram.setLayout(new BorderLayout(0, 0));
		
		diagram = new mxGraph();
		mxGraphComponent graphComponent = new mxGraphComponent(diagram);
		panelDiagram.add(graphComponent);
		
		tabs.addTab("Diagram", null, panelDiagram, null);
		
		txtLogs = new JTextArea();
		txtLogs.setFont(new Font("Courier 10 Pitch", Font.PLAIN, 12));
		txtLogs.setEditable(false);
		JScrollPane sp = new JScrollPane(txtLogs); 
		tabs.addTab("Logs", null, sp, null);
		redirectSystemStreams();
		
		JPanel panel = new JPanel();
		frmJhekaton.getContentPane().add(panel, BorderLayout.NORTH);
		panel.setLayout(new BorderLayout(0, 0));
		
		JLabel lblFile = new JLabel("File:");
		panel.add(lblFile, BorderLayout.WEST);
		lblFile.setHorizontalAlignment(SwingConstants.RIGHT);
		
		txtFile = new JTextField();
		panel.add(txtFile);
		txtFile.setEditable(false);
		txtFile.setColumns(10);
		
		statusBar = new StatusBar("Ready");
	    frmJhekaton.getContentPane().add(statusBar, BorderLayout.SOUTH);
		
		JPanel panel_1 = new JPanel();
		panel.add(panel_1, BorderLayout.EAST);
		
		JButton btnOpen = new JButton("Open");
		panel_1.add(btnOpen);

		final br.ufrgs.ppgc.gia.jhekaton.JHekaton jhek = new br.ufrgs.ppgc.gia.jhekaton.JHekaton();
		
		final JFileChooser fc = new JFileChooser();
		btnOpen.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				int returnVal = fc.showOpenDialog(frmJhekaton);
				if (returnVal == JFileChooser.APPROVE_OPTION) {
		            file = fc.getSelectedFile();
		            txtFile.setText(file.getAbsolutePath());
		            statusBar.setMessage("XMI File Loaded");
		            Region diag = jhek.parseFile(file);
		            updateDiagram(diag.getSubvertex(), diag.getTransition());
				}
				
			}
		});
		
		JButton btnRun = new JButton("Run");
		panel_1.add(btnRun);
		btnRun.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				txtLogs.setText("");
				statusBar.setMessage("Processing...");
				jhek.extractTopology();
				jhek.processa();
				statusBar.setMessage("... done!");
			}
		});
	}
	
	private void updateTextArea(final String text) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				txtLogs.append(text);
			}
		});
	}

	private void redirectSystemStreams() {
		
		OutputStream out = new OutputStream() {
		
			@Override
			public void write(int b) throws IOException {
				updateTextArea(String.valueOf((char) b));
			}
	
			@Override
			public void write(byte[] b, int off, int len) throws IOException {
				updateTextArea(new String(b, off, len));
			}
			
			@Override
			public void write(byte[] b) throws IOException {
				write(b, 0, b.length);
			}
		};

		System.setOut(new PrintStream(out, true));
		System.setErr(new PrintStream(out, true));
	}
	
}