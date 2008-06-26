/*
 * Copyright (c) 2003, the JUNG Project and the Regents of the University of
 * California All rights reserved.
 * 
 * This software is open-source under the BSD license; see either "license.txt"
 * or http://jung.sourceforge.net/license.txt for a description.
 * 
 */
package renderer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.HashSet;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JApplet;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.collections15.Transformer;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import edu.uci.ics.jung.algorithms.connectivity.KNeighborhoodExtractor;
import edu.uci.ics.jung.algorithms.layout.FRLayout2;
import edu.uci.ics.jung.visualization.DefaultVisualizationModel;
import edu.uci.ics.jung.visualization.GraphZoomScrollPane;
import edu.uci.ics.jung.visualization.RenderContext;
import edu.uci.ics.jung.visualization.VisualizationModel;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.VisualizationServer.Paintable;
import edu.uci.ics.jung.visualization.annotations.AnnotatingGraphMousePlugin;
import edu.uci.ics.jung.visualization.annotations.AnnotatingModalGraphMouse;
import edu.uci.ics.jung.visualization.annotations.AnnotationControls;
import edu.uci.ics.jung.visualization.control.CrossoverScalingControl;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ScalingControl;
import edu.uci.ics.jung.visualization.decorators.PickableEdgePaintTransformer;
import edu.uci.ics.jung.visualization.decorators.PickableVertexPaintTransformer;
import edu.uci.ics.jung.visualization.decorators.ToStringLabeller;
import edu.uci.ics.jung.visualization.renderers.Renderer;
import graph.Attributes;
import graph.DependencyDirectedSparceMultiGraph;

/**
 * 
 * @author Raul Cajias
 * 
 */
public class DependencyGrapher<V, E> extends JApplet {

	File _dependencyTree = new File("E:\\niku\\main_mssql\\build\\xog\\bin\\DependencyTree.xml");
	final static String NODE_DEPLOY = "deploy";
	final static String NODE_DEPENDANCY = "dependancy";
	final static String ATTR_DEP_ATTRIBUTE = "attribute";
	final static String ATTR_DEP_ISREQUIRED = "required";
	static final String instructions = 
		"<html>"+
		"<b><h2><center>Instructions for Annotations</center></h2></b>"+
		"<p>The Annotation Controls allow you to select:"+
		"<ul>"+
		"<li>Shape"+
		"<li>Color"+
		"<li>Fill (or outline)"+
		"<li>Above or below (UPPER/LOWER) the graph display"+
		"</ul>"+
		"<p>Mouse Button one press starts a Shape,"+
		"<p>drag and release to complete."+
		"<p>Mouse Button three pops up an input dialog"+
		"<p>for text. This will create a text annotation."+
		"<p>You may use html for multi-line, etc."+
		"<p>You may even use an image tag and image url"+
		"<p>to put an image in the annotation."+
		"<p><p>"+
		"<p>To remove an annotation, shift-click on it"+
		"<p>in the Annotations mode."+
		"<p>If there is overlap, the Annotation with center"+
		"<p>closest to the mouse point will be removed.";
	private static final String ATTR_NODE_TYPE = "type";
	private FRLayout2<String,Number> filterLayout = null;

	private static enum EdgeType { IN_OUT, IN, OUT };
	private EdgeType _filterEdgeDirection = EdgeType.IN_OUT;
	private String _filterChoice = "ALL";
	private int _kNeighbourhoodDepth = 1;

	JDialog helpDialog;

	Paintable viewGrid;

	/**
	 * create an instance of a simple graph in two views with controls to
	 * demo the features.
	 * 
	 */
	public DependencyGrapher() {

		// create a simple graph for the demo
		final DependencyDirectedSparceMultiGraph<String, Number> graph = createGraph(); //TestGraphs.getOneComponentGraph();

		// the preferred sizes for the two views
		final Dimension preferredSize1 = new Dimension(700,700);

		// create one layout for the graph
		final FRLayout2<String,Number> layout = new FRLayout2<String,Number>(graph);
		layout.setMaxIterations(500);

		VisualizationModel<String,Number> vm =
			new DefaultVisualizationModel<String,Number>(layout, preferredSize1);

		Transformer<Number,String> stringer = new Transformer<Number,String>(){
			public String transform(Number e) {
				if(graph.getEdgeAttributes(e) !=null)
				{
					return graph.getEdgeAttributes(e).toString();
				}
				return null;
			}
		};
		// create 2 views that share the same model
		final VisualizationViewer<String,Number> vv = 
			new VisualizationViewer<String,Number>(vm, preferredSize1);
		vv.setBackground(Color.white);
		vv.getRenderContext().setEdgeLabelTransformer(stringer);		
		vv.getRenderContext().setEdgeDrawPaintTransformer(new PickableEdgePaintTransformer<String,Number>(vv.getPickedEdgeState(), Color.black, Color.cyan));
		vv.getRenderContext().setVertexFillPaintTransformer(new PickableVertexPaintTransformer<String>(vv.getPickedVertexState(), Color.red, Color.yellow));
		vv.getRenderContext().setVertexLabelTransformer(new ToStringLabeller<String>());
		vv.getRenderer().getVertexLabelRenderer().setPosition(Renderer.VertexLabel.Position.AUTO);


		// add default listener for ToolTips
		vv.setVertexToolTipTransformer(new ToStringLabeller<String>());

//		ToolTipManager.sharedInstance().setDismissDelay(10000);

		Container content = getContentPane();
		Container panel = new JPanel(new BorderLayout());

		GraphZoomScrollPane gzsp = new GraphZoomScrollPane(vv);
		panel.add(gzsp);

		helpDialog = new JDialog();
		helpDialog.getContentPane().add(new JLabel(instructions));

		RenderContext<String, Number> rc = vv.getRenderContext();
		AnnotatingGraphMousePlugin annotatingPlugin = new AnnotatingGraphMousePlugin(rc);
		// create a GraphMouse for the main view
		// 
		final AnnotatingModalGraphMouse graphMouse = new AnnotatingModalGraphMouse(rc, annotatingPlugin);
		vv.setGraphMouse(graphMouse);
		vv.addKeyListener(graphMouse.getModeKeyListener());

		final ScalingControl scaler = new CrossoverScalingControl();


		JButton filterReset = new JButton("Reset");
		filterReset.addActionListener(new ActionListener()
		{

			public void actionPerformed(ActionEvent e) {
				vv.getModel().setGraphLayout(layout);
			}
		}
		);
		JButton filterFilter = new JButton("Filter");
		filterReset.addActionListener(new ActionListener()
		{

			public void actionPerformed(ActionEvent e) {
				vv.getModel().setGraphLayout(layout);
			}
		}
		);		
		JRadioButton filterDirectionInOut = new JRadioButton("In/Out");
		filterDirectionInOut.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e) {
				System.out.println("Dependency Direction: "+EdgeType.IN_OUT);
				_filterEdgeDirection = EdgeType.IN_OUT;
			}

		});
		JRadioButton filterDirectionIn = new JRadioButton("In");
		filterDirectionIn.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e) {
				System.out.println("Dependency Direction: "+EdgeType.IN);
				_filterEdgeDirection = EdgeType.IN;
			}

		});
		JRadioButton filterDirectionOut = new JRadioButton("Out");
		filterDirectionOut.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e) {
				System.out.println("Dependency Direction: "+EdgeType.OUT);
				_filterEdgeDirection = EdgeType.OUT;
			}

		});		
		ButtonGroup filterRadios = new ButtonGroup();
		filterRadios.add(filterDirectionInOut);
		filterRadios.add(filterDirectionIn);
		filterRadios.add(filterDirectionOut);
		filterRadios.setSelected(filterDirectionInOut.getModel(), true);

		JComboBox modeBox = graphMouse.getModeComboBox();
		modeBox.setSelectedItem(ModalGraphMouse.Mode.PICKING);

		final JComboBox filterBox = new JComboBox(graph.getVertices().toArray());
		filterBox.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e) {
				HashSet<String> rootNodes = new HashSet();
				String filterChoice = filterBox.getSelectedItem().toString();
				System.out.println(filterChoice);
				rootNodes.add(filterChoice);
				switch(_filterEdgeDirection)
				{
				case IN_OUT: filterLayout = new FRLayout2<String, Number>(KNeighborhoodExtractor.extractNeighborhood(graph,rootNodes, _kNeighbourhoodDepth));
				break;
				case IN: filterLayout = new FRLayout2<String, Number>(KNeighborhoodExtractor.extractInDirectedNeighborhood(graph,rootNodes, _kNeighbourhoodDepth));
				break;
				case OUT: filterLayout = new FRLayout2<String, Number>(KNeighborhoodExtractor.extractOutDirectedNeighborhood(graph,rootNodes,_kNeighbourhoodDepth));
				break;
				}
				filterLayout.setSize(preferredSize1);				
				vv.getModel().setGraphLayout(filterLayout);
			}
		}
		);

		JButton help = new JButton("Help");
		help.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				helpDialog.pack();
				helpDialog.setVisible(true);
			}
		});

		JPanel controls = new JPanel();
//		JPanel zoomControls = new JPanel();
//		zoomControls.setBorder(BorderFactory.createTitledBorder("Zoom"));
//		zoomControls.add(plus);
//		zoomControls.add(minus);
//		controls.add(zoomControls);

		JPanel modeControls = new JPanel();
		modeControls.setBorder(BorderFactory.createTitledBorder("Mouse Mode"));
		modeControls.add(graphMouse.getModeComboBox());
		controls.add(modeControls);

		JPanel annotationControlPanel = new JPanel();
		annotationControlPanel.setBorder(BorderFactory.createTitledBorder("Annotation Controls"));

		AnnotationControls annotationControls = new AnnotationControls(annotatingPlugin);

		annotationControlPanel.add(annotationControls.getAnnotationsToolBar());
		controls.add(annotationControlPanel);

		JPanel helpControls = new JPanel();
		helpControls.setBorder(BorderFactory.createTitledBorder("Help"));
		helpControls.add(help);
		controls.add(helpControls);

		JPanel filterControls = new JPanel();
		filterControls.setBorder(BorderFactory.createTitledBorder("Filter"));
		filterControls.add(filterBox);
		filterControls.add(filterDirectionInOut);
		filterControls.add(filterDirectionIn);
		filterControls.add(filterDirectionOut);		
		filterControls.add(filterReset);
		controls.add(filterControls);		


		content.add(panel);
		content.add(controls, BorderLayout.SOUTH);


	}

	private DependencyDirectedSparceMultiGraph<String, Number> createGraph() {
		DependencyDirectedSparceMultiGraph<String,Number> g = new DependencyDirectedSparceMultiGraph<String,Number>();

		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db;
		try {
			db = dbf.newDocumentBuilder();

			Document doc = db.parse(_dependencyTree);
			doc.getDocumentElement().normalize();

			NodeList nodes = doc.getElementsByTagName(NODE_DEPLOY);

			for(int i = 0; i< nodes.getLength(); i++)
			{ 
				Node nodeFrom = nodes.item(i);
				String vFrom = nodeFrom.getAttributes().getNamedItem(ATTR_NODE_TYPE).getNodeValue();
				g.addVertex(vFrom);
				if(nodeFrom.hasChildNodes())
				{				
					NodeList childNodes = nodeFrom.getChildNodes();
					for(int j = 0; j < childNodes.getLength(); j++)
					{
						Node nodeTo = childNodes.item(j);
						if(nodeTo.getNodeName().equals("dependency"))
						{
							Attributes attr;
							String attrName = null;
							boolean isRequired;

							String vTo = nodeTo.getAttributes().getNamedItem(ATTR_NODE_TYPE).getNodeValue();
							if(nodeTo.getAttributes().getNamedItem(ATTR_DEP_ATTRIBUTE) != null)
							{
								attrName = nodeTo.getAttributes().getNamedItem(ATTR_DEP_ATTRIBUTE).getNodeValue();						
							}
							if(nodeTo.getAttributes().getNamedItem(ATTR_DEP_ISREQUIRED) != null)
							{
								isRequired = Boolean.parseBoolean(nodeTo.getAttributes().getNamedItem(ATTR_DEP_ISREQUIRED).getNodeValue());
							}else
							{
								isRequired = false;
							}
							createEdge(g, vFrom, vTo, new Attributes(attrName, isRequired));
						}
					}
				}
			}

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return g;
	}

	private static void createEdge(
			DependencyDirectedSparceMultiGraph<String, Number> g,
			String v1Label,
			String v2Label,
			Attributes attr) {

//		if(g.findEdge(v1Label, v2Label) == null){
			g.addEdge(new Double(Math.random()), v1Label, v2Label, attr);
//		}
	}


	/**
	 * a driver for this demo
	 */
	public static void main(String[] args) {
		JFrame f = new JFrame();
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.getContentPane().add(new DependencyGrapher());
		f.pack();
		f.setVisible(true);
	}
}
