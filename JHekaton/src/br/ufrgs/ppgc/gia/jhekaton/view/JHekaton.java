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
import java.util.Hashtable;
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
import br.ufrgs.ppgc.gia.jhekaton.XmiUtil;
import br.ufrgs.ppgc.gia.jhekaton.xml.Model.PackagedElement.Region;
import br.ufrgs.ppgc.gia.jhekaton.xml.Model.PackagedElement.Region.Subvertex;
import br.ufrgs.ppgc.gia.jhekaton.xml.Model.PackagedElement.Region.Transition;

import com.mxgraph.layout.mxFastOrganicLayout;
import com.mxgraph.layout.mxIGraphLayout;
import com.mxgraph.model.mxICell;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.swing.util.mxMorphing;
import com.mxgraph.util.mxConstants;
import com.mxgraph.util.mxEventObject;
import com.mxgraph.util.mxEventSource.mxIEventListener;
import com.mxgraph.view.mxGraph;
import com.mxgraph.view.mxStylesheet;

public class JHekaton {

	private JFrame frmJhekaton;
	private JTextField txtFile;
	private JPanel panelDiagram;
	private File file;
	private JTextArea txtLogs;
	private StatusBar statusBar;

	private mxGraphComponent graphComponent;
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

	private void updateDiagram(Region region){
		
		this.graphComponent.setEnabled(false);
		
		this.diagram.getModel().beginUpdate();
		this.diagram.removeCells(this.diagram.getChildVertices(this.diagram.getDefaultParent()));

		Object parent = this.diagram.getDefaultParent();
		
		List lista = new ArrayList();

		List<Subvertex> states = region.getSubvertex();
		
		// Loop Version
//		for(Subvertex state : states){
//			double width = state.getName().length() * 12;
//			String style = state.getType().equals(StateType.INITIAL.getUmlType()) ? "INITIAL" : state.getType().equals(StateType.FINAL.getUmlType()) ? "FINAL" : "STATE" ;
//			Object o = diagram.insertVertex(parent, state.getId(), state.getName(), 20, 20, width, 30, style);
//			lista.add(o);
//		}
		
		// Recursive Version
		List<String> ids = new ArrayList<String>();
		Subvertex initialState = XmiUtil.getStatesByType(region, StateType.INITIAL).get(0);
		this.addVertex(parent, lista, ids, region, initialState, 1, 1, 0);
		
		for (Transition transition : region.getTransition()) {
			diagram.insertEdge(parent, transition.getId(), null, find(transition.getSource(), lista), find(transition.getTarget(), lista),"ARROW");
		}
		
		diagram.getModel().endUpdate();
		
		
		// define layout
        mxIGraphLayout layout = new mxFastOrganicLayout(this.diagram);
        // layout using morphing
        this.diagram.getModel().beginUpdate();
        try {
            layout.execute(this.diagram.getDefaultParent());
        } finally {
            mxMorphing morph = new mxMorphing(graphComponent, 20, 1.2, 20);
            morph.startAnimation();
            this.diagram.getModel().endUpdate();
        }
	}
	
	private void addVertex(Object parent, List lista, List<String> ids, Region region, Subvertex state, int position, int count, double x){

		int division = count+1;
		double ratio = this.graphComponent.getHeight()/division;
		double position_x = 60+x;
		double position_y = ratio * position;
		double width = state.getName().length() * 10;

		String style = state.getType().equals(StateType.INITIAL.getUmlType()) ? "INITIAL" : state.getType().equals(StateType.FINAL.getUmlType()) ? "FINAL" : "STATE" ;
		
		Object o = diagram.insertVertex(parent, state.getId(), state.getName(), position_x, position_y, width, 30, style);
		lista.add(o);
		ids.add(state.getId());
		
		List<Transition> transitions = XmiUtil.getTransitionsFromState(region, state.getId());

		position_x += width+30; 
		
		if(transitions != null){
			for (int i = 0; i < transitions.size() ; i++) {
				
				Transition transition = transitions.get(i);
				
				Subvertex newState = XmiUtil.getStateById(region, transition.getTarget());
				
				if(!ids.contains(newState.getId())){
					this.addVertex(parent, lista, ids,region, newState, i+1, transitions.size(), position_x);					
				}
				
				
			}
		}
	}

	private void fillStyles() {
		mxStylesheet stylesheet = diagram.getStylesheet();
		Hashtable<String, Object> styleInitial = new Hashtable<String, Object>();
		styleInitial.put(mxConstants.STYLE_SHAPE, mxConstants.SHAPE_HEXAGON);
		styleInitial.put(mxConstants.STYLE_OPACITY, 80);
		styleInitial.put(mxConstants.STYLE_FONTCOLOR, "#FFFFFF");
		styleInitial.put(mxConstants.STYLE_FILLCOLOR, "#000000");
		styleInitial.put(mxConstants.STYLE_SHADOW, "true");
		stylesheet.putCellStyle("INITIAL", styleInitial);

		Hashtable<String, Object> styleFinal = new Hashtable<String, Object>();
		styleFinal.put(mxConstants.STYLE_SHAPE, mxConstants.SHAPE_CYLINDER);
		styleFinal.put(mxConstants.STYLE_OPACITY, 80);
		styleFinal.put(mxConstants.STYLE_FONTCOLOR, "#FFFFFF");
		styleFinal.put(mxConstants.STYLE_FILLCOLOR, "#000000");
		styleFinal.put(mxConstants.STYLE_SHADOW, "true");
		stylesheet.putCellStyle("FINAL", styleFinal);
		
		Hashtable<String, Object> styleState = new Hashtable<String, Object>();
		styleState.put(mxConstants.STYLE_SHAPE, mxConstants.SHAPE_RECTANGLE);
		styleState.put(mxConstants.STYLE_OPACITY, 80);
		styleState.put(mxConstants.STYLE_FONTCOLOR, "#000000");
		styleState.put(mxConstants.STYLE_FILLCOLOR, "#FFFFFF");
		styleState.put(mxConstants.STYLE_STROKECOLOR, "#000000");
		styleState.put(mxConstants.STYLE_SHADOW, "true");
		stylesheet.putCellStyle("STATE", styleState);

		Hashtable<String, Object> styleArrow = new Hashtable<String, Object>();
		styleArrow.put(mxConstants.STYLE_FONTCOLOR, "#000000");
		styleArrow.put(mxConstants.STYLE_STROKECOLOR, "#000000");
		styleArrow.put(mxConstants.STYLE_SHADOW, "true");
		stylesheet.putCellStyle("ARROW", styleArrow);
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
		graphComponent = new mxGraphComponent(diagram);
		this.diagram.setAllowLoops(true);
		this.fillStyles();
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
		
		final JFileChooser fc = new JFileChooser("/home/igor/git/JHekaton/JHekaton/src/");
		btnOpen.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				int returnVal = fc.showOpenDialog(frmJhekaton);
				if (returnVal == JFileChooser.APPROVE_OPTION) {
		            file = fc.getSelectedFile();
		            txtFile.setText(file.getAbsolutePath());
		            statusBar.setMessage("XMI File Loaded");
		            Region diag = jhek.parseFile(file);
		            updateDiagram(diag);
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