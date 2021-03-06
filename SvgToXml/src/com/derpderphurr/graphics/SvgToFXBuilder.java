package com.derpderphurr.graphics;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Paint;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Circle;
import javafx.scene.shape.FillRule;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.SVGPath;
import javafx.scene.shape.Shape;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;
import javafx.scene.transform.Affine;
import javafx.scene.transform.Transform;
import javafx.scene.transform.Translate;

public class SvgToFXBuilder {
	protected Map<String,Element> elementMap;
	protected Stack<String> styleStack;
	protected Map<String,Document> docs;
	protected DocumentBuilderFactory dbf;
	protected List<String> lookfor;
	protected List<String> knownAttribs;
	protected Map<String,Paint> namedFills; 
	protected String currentDoc;
	
	protected EventHandler<? super MouseEvent> defaultEnteredHandler;
	protected EventHandler<? super MouseEvent> defaultExitHandler;
	protected EventHandler<? super MouseEvent> defaultPressedHandler;
	protected EventHandler<? super MouseEvent> defaultReleasedHandler;
	protected EventHandler<? super MouseEvent> defaultClickHandler;
	/**
	 * Utility function to find one of the inner names in an already instantiated group
	 * @param name
	 * @param s
	 * @return
	 */
	
	public Document getDocument() { return docs.get(currentDoc); }
	
	public static javafx.scene.Node findNode(String name,javafx.scene.Node s) {
		if(s instanceof Parent) {
			ObservableList<javafx.scene.Node> children = ((Parent)s).getChildrenUnmodifiable();
			javafx.scene.Node c = null;
			for(javafx.scene.Node n : children) {
				if(n instanceof javafx.scene.Parent) {
					javafx.scene.Node a = findNode(name,n);
					if(a != null) {
						return a;
					}
				} else {
					if(n.getId().equals(name)) {
						return n;
					}
				}
			}
			return c;
		} else {
			if(s == null) { return null; }
			String id = s.getId();
			if(id != null && s.getId().equals(name)) {
				return s;
			}
		}
		return null;
	}
	
	

	public EventHandler<? super MouseEvent> getDefaultClickHandler() {
		return defaultClickHandler;
	}



	public void setDefaultClickHandler(EventHandler<? super MouseEvent> defaultClickHandler) {
		this.defaultClickHandler = defaultClickHandler;
	}



	public void setDefaultEnteredHandler(EventHandler<? super MouseEvent> defaultEnteredHandler) {
		this.defaultEnteredHandler = defaultEnteredHandler;
	}

	

	public EventHandler<? super MouseEvent> getDefaultExitHandler() {
		return defaultExitHandler;
	}

	public void setDefaultExitHandler(EventHandler<? super MouseEvent> defaultExitHandler) {
		this.defaultExitHandler = defaultExitHandler;
	}

	public EventHandler<? super MouseEvent> getDefaultPressedHandler() {
		return defaultPressedHandler;
	}

	public void setDefaultPressedHandler(EventHandler<? super MouseEvent> defaultPressedHandler) {
		this.defaultPressedHandler = defaultPressedHandler;
	}

	public EventHandler<? super MouseEvent> getDefaultReleasedHandler() {
		return defaultReleasedHandler;
	}

	public void setDefaultReleasedHandler(EventHandler<? super MouseEvent> defaultReleasedHandler) {
		this.defaultReleasedHandler = defaultReleasedHandler;
	}

	public EventHandler<? super MouseEvent> getDefaultEnteredHandler() {
		return defaultEnteredHandler;
	}

	public javafx.scene.Node createInstanceFromId(String id) {
		if(elementMap.containsKey(id)) {
			return generateFX(elementMap.get(id),null);
		}else {
			return null; 
			}
	}
	
	public SvgToFXBuilder() {
		elementMap = new HashMap<>();
		styleStack = new Stack<>();
		docs = new HashMap<>();
		dbf = DocumentBuilderFactory.newInstance();
		lookfor = new ArrayList<>();
		namedFills = new HashMap<>();
		
		lookfor.add("svg");
		lookfor.add("g");
		lookfor.add("rect");
		lookfor.add("circle");
		lookfor.add("elipse");
		lookfor.add("path");
		lookfor.add("polygon");
		lookfor.add("polyline");
		lookfor.add("line");
		lookfor.add("linearGradient");
		
		knownAttribs = Arrays.asList("stroke","stroke-width","stroke-linecap","stroke-miterlimit","stroke-linejoin","stroke-dasharray","fill","fill-rule","stop-color","stop-opacity","transform");
	}
	
	public void loadXML(String name,InputSource s) throws ParserConfigurationException {
		try {
			currentDoc = name;
			DocumentBuilder db = dbf.newDocumentBuilder();
			long tStart = System.currentTimeMillis();
			Document d = db.parse(s);
			System.out.println("Parsing Took: "+(System.currentTimeMillis()-tStart)+" Milliseconds");
			System.out.println("Done Parsing XML");
			docs.put(name,d);
			findElements(d.getDocumentElement());
		}catch(ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void loadXML(Document d,String name) {
		currentDoc = name;
		docs.put(name, d);
		findElements(d.getDocumentElement());
	}

	protected Map<String,String> findAttribs(Element e) {
		Map<String,String> styles = new HashMap<>();
		if(e.hasAttribute("style")) {
			styles.putAll(convertStyle(e.getAttribute("style")));
		}
		styles.putAll(checkForStyleAttribs(e));
		return styles;
	}
	
	protected Map<String,String> checkForStyleAttribs(Element e) {
		Map<String,String> attrstyles = new HashMap<>();
		knownAttribs.forEach( (attrname) -> { 
			if(e.hasAttribute(attrname)) {
				attrstyles.put(attrname,e.getAttribute(attrname).toString() );
			}
		});
		return attrstyles;
	}
	
	public javafx.scene.Node generateFX(Element e,Map<String,String> style_in) {
		HashMap<String,String> style;
		if(style_in == null) {
			style = new HashMap<>(); 
		} else {
			style = new HashMap<>(style_in);
		}
		
		style.putAll(findAttribs(e));
		javafx.scene.Node n = null;
		if(lookfor.contains(e.getNodeName())) {
			if(e.hasAttribute("style")) {
				style.putAll(convertStyle(e.getAttribute("style")));
			}
			
			switch(e.getNodeName()) {
			case "circle":
				Circle c = new Circle();
				c.setCenterX(Double.parseDouble(e.getAttribute("cx")));
				c.setCenterY(Double.parseDouble(e.getAttribute("cy")));
				c.setRadius(Double.parseDouble(e.getAttribute("r")));
				applyStyle(style, c);
				n = c;
				break;
			case "path":
				SVGPath p = new SVGPath();
				p.setContent(e.getAttribute("d"));
				applyStyle(style, p);
				n = p;
				break;
			case "linearGradient":
				parseLinearGradient(e);
				break;
			case "rect":
				Rectangle rect = new Rectangle();
					rect.setX(Double.parseDouble(e.getAttribute("x")));
					rect.setY(Double.parseDouble(e.getAttribute("y")));
					rect.setWidth(Double.parseDouble(e.getAttribute("width")));
					rect.setHeight(Double.parseDouble(e.getAttribute("height")));
					if(e.hasAttribute("rx") && e.hasAttribute("ry")) {
						rect.setArcWidth(Double.parseDouble(e.getAttribute("rx")));
						rect.setArcHeight(Double.parseDouble(e.getAttribute("ry")));
					}
					applyStyle(style,rect);
					n = rect;
				break;
			case "svg":
			{
				javafx.scene.Group g = new javafx.scene.Group();
				NodeList nl = e.getChildNodes();
				for(int i=0; i < nl.getLength(); i++) {
					if(nl.item(i) instanceof Element) {
						javafx.scene.Node ni = generateFX((Element)nl.item(i),style); 
						if(ni != null) {
							g.getChildren().add(ni);
						}
					} else {
						//System.out.println("Not an element: "+nl.item(i).getNodeName()+":"+nl.item(i).getNodeValue());
					}
				}
				n = g;
			}
				break;
			case "g":
			{
				javafx.scene.Group g = new javafx.scene.Group();
				if(style.containsKey("transform")) {
					//Apply transform
					g.getTransforms().addAll(
							splitTransforms(
									style.get("transform")).stream().map(tf -> parseTransform(tf)).collect(Collectors.toList()
											)
							);
					style.remove("transform");
				}
				if(e.hasChildNodes()){
					NodeList nl = e.getChildNodes();
					for(int i=0; i < nl.getLength(); i++) {
						if(nl.item(i) instanceof Element) {
							javafx.scene.Node ni = generateFX((Element)nl.item(i),style); 
							if(ni != null) {
								g.getChildren().add(ni);
							}
						} else {
							//System.out.println("Not an element: "+nl.item(i).getNodeName()+":"+nl.item(i).getNodeValue());
						}
					}
					
				}
				
				
				n = g;
			}
				break;
			default:
				System.out.println(e.getNodeName()+" has not yet been implemented");
				break;
			}
			if(e.hasAttribute("id")) {
				//System.out.println("SVG ID Set , Node id = "+e.getAttribute("id"));
				n.setId(e.getAttribute("id"));
			}
		} else {
			System.err.println("Unsure how to handle: "+e.getNodeName());
		}
		if( n != null && !(n instanceof Group) ) {
			System.out.println("Mouse Handler was set on: "+e.getAttribute("id"));
				n.setOnMouseEntered(defaultEnteredHandler);
				n.setOnMouseExited(defaultExitHandler);
				n.setOnMousePressed(defaultPressedHandler);
				n.setOnMouseReleased(defaultReleasedHandler);
				n.setOnMouseClicked(defaultClickHandler);
				return n;
		}
		
		
		return n;
	}
	
	private void parseElementById(Document d,String id) {
		Element e = d.getElementById(id);
		if(e == null) {
			System.err.println("Cannot find element with id of "+id);
			return;
		} else {
			if(e.getNodeName().equals("linearGradient")) {
				parseLinearGradient(e);
			}
		}
	}
	
	private void parseLinearGradient(Element e) {
		System.err.println("Calling ParseLinearGradient");
		List<Stop> stops = new ArrayList<>();
		
		//Defaults for gradient
		double x1=0,y1=0,x2=1,y2=0;
		
		if(e.hasChildNodes()) {
			NodeList children = e.getChildNodes();
			for(int i=0; i < children.getLength(); i++) {
				if( children.item(i) instanceof Element) {
					Element c = (Element)children.item(i);
					if(c.getNodeName().equals("stop")) {
						Stop s = parseStop(c);
						stops.add(s);
					}
				}
			}
		}
		LinearGradient lg = new LinearGradient(
				x1, 
				y1, 
				x2, 
				y2, 
				true, //Not sure about this one... pretty sure this is absolute vs realtive
				CycleMethod.NO_CYCLE, //same here, what is the svg equivilent 
				stops);
		if( e.hasAttribute("id")) {
			namedFills.put(e.getAttribute("id"),lg);
		} else {
			System.out.println("Linear Gradient has No id");
		}
	}
	
	public List<String> splitTransforms(String transforms) {
		return Arrays.asList(transforms.split("\\s"));
	}
	
	public Transform parseTransform(String value) {
		
		String doublePattern = "[-+]?[0-9]*\\.?[0-9]+([eE][-+]?[0-9]+)?";
		if(value.toUpperCase().startsWith("TRANSLATE")) {
			//regex stolen from (http://www.regular-expressions.info/floatingpoint.html)
			
			Pattern translateRegex = Pattern.compile("^translate\\(("+doublePattern+"),("+doublePattern+")\\)");
			Matcher m = translateRegex.matcher(value.toLowerCase());
			if(m.find()) {
				System.out.println(String.format("translate(%s,%s)",m.group(1),m.group(3) ));
				Translate t = new Translate();
					t.setX(Double.parseDouble(m.group(1)));
					t.setY(Double.parseDouble(m.group(3)));
				return t;	
			} else {
				System.out.println("Translate found but Error Parsing");
				
			}
		} else if(value.toUpperCase().startsWith("MATRIX")) {
			Pattern matrixPattern = Pattern.compile("^matrix\\(("+doublePattern+"),("+doublePattern+"),("+doublePattern+"),("+doublePattern+"),("+doublePattern+"),("+doublePattern+")\\)");
			
			Matcher m = matrixPattern.matcher(value.toLowerCase());
			if(m.find()) {
				double a,b,c,d,e,f;
				
				a = Double.parseDouble(m.group(1));
				b = Double.parseDouble(m.group(3));
				c = Double.parseDouble(m.group(5));
				d = Double.parseDouble(m.group(7));
				e = Double.parseDouble(m.group(9));
				f = Double.parseDouble(m.group(11));
				
				Affine affine = Transform.affine(
						a, c, 1, e, 
						b, d, 1, f, 
						0, 0, 1, 1);
				return affine;
				
			} else {
				System.out.println("matrix found but Error Parsing");
				System.out.println("\tRegex: "+matrixPattern.toString());
				System.out.println("\tdata: "+value);
				
			}
		} else {
			System.out.println("Unimplemented Transform: "+value);
		}
		return null;
		
	}
	
	private Stop parseStop(Element e) {
		if(e.getNodeName() != "stop") {
			return null;
		} else {
			if(e.hasAttribute("stop-opacity")){
				Stop s = new Stop(Double.parseDouble(e.getAttribute("offset")), Color.web(e.getAttribute("stop-color"),Double.parseDouble(e.getAttribute("stop-opacity") ) ) );
				return s;
			} else {
				try {
					Stop s = new Stop(Double.parseDouble(e.getAttribute("offset")), Color.web(e.getAttribute("stop-color")));
					return s;
				} catch ( IllegalArgumentException ex) {
					System.err.println("Element: "+e.toString());
					return null;
				}
				
			}
		}
	}

	protected Double percentToDouble(String s) {
		Pattern pPercent = Pattern.compile("(\\d+)%");
		Matcher m = pPercent.matcher(s);
		if(m.find()) {
			String group = m.group(1);
			return Double.parseDouble(group);
		} else { return null; }
	}
	
	protected String getRefId(String n) {
		Pattern p = Pattern.compile("url\\(#(\\w+)\\)");
		Matcher m = p.matcher(n);
		
		if(m.find()) {
			String group = m.group(1);
			System.out.println("Refid="+group);
			return group;
		} else {
			System.out.println("unable to parse Reference");
		}
		return null;
	}
	
	protected void applyStyle(Map<String,String> style,Shape s) {
		//System.out.println("Applying Following Style Parameters to Shape: "+s.getClass().getName());
		for(String key:style.keySet()) {
			String value = style.get(key);
			switch(key) {
				case "stroke":
					if(value.trim().equals("none")) {
						s.setStroke(Color.TRANSPARENT);
					} else { 
						if(value.startsWith("url(#")) {
							
							
						} else {
							if(style.containsKey("stroke-opacity")) {
								s.setStroke(Color.web(value.trim(),Double.parseDouble(style.get("stroke-opacity"))));
							} else {
								s.setStroke(Color.web(value.trim()));
							}
						}
					}
					break;
				case "fill":
					if(value.trim().equals("none")) {
						s.setFill(Color.TRANSPARENT);
					} else if(value.startsWith("url(#")) {
						System.out.println("Fill value is Reference: \""+value+"\"");
						String refid = getRefId(value);
						if(namedFills.containsKey(refid)) {
							s.setFill(namedFills.get(refid));
							System.out.println("Applying Linear Gradient to "+s);
						} else {
							parseElementById(docs.get(currentDoc), refid);
							System.out.println("Cannot find "+refid);
							if(namedFills.containsKey(refid)) {
								s.setFill(namedFills.get(refid));
							} else {
								System.err.println("Referenced Style does not exist");
							}
						}
					} else {
						System.out.println("Setting Fill: "+value);
						
						//This is from the merge
						if(value.startsWith("url(#")) {
							String id = value.substring(4, value.length()-1);
							s.setFill(namedFills.get(id));
						} else if( value.startsWith("#")) {
							if(style.containsKey("fill-opacity")) {
								s.setFill(Color.web(value.trim(),Double.parseDouble(style.get("fill-opacity"))));
							} else {
								s.setFill(Color.web(value.trim()));
							}
						}
					}
					break;
				case "stroke-width":
					s.setStrokeWidth(Double.parseDouble(value));
					break;
				case "stroke-linecap":
					s.setStrokeLineCap(StrokeLineCap.valueOf(value.toUpperCase()));
					break;
				case "stroke-linejoin":
					s.setStrokeLineJoin(StrokeLineJoin.valueOf(value.toUpperCase()));
					break;
				case "stroke-miterlimit":
					s.setStrokeMiterLimit(Double.parseDouble(value));
					break;
				case "fill-rule":
					//System.out.println("fill-rule: "+value);
					if(value.toUpperCase().equals("EVENODD")) {
						value = "EVEN_ODD";
					} else if(value.toUpperCase().equals("NONZERO")) {
						value = "NON_ZERO";
					} else {
						System.out.println("UNSUPPORTED FILL RULE");
					}
					if(s instanceof SVGPath) {
						SVGPath p = (SVGPath)s;
						p.setFillRule(FillRule.valueOf(value.toUpperCase()));
					}
					break;
				case "stroke-dasharray":
					if(value.equals("none")) {
						//don't bother stetting
					} else {
						System.out.println("Stroke Dash Array: "+value);
						//TODO implement me
						//s.getStrokeDashArray().setAll(-values-)
					}
					break;
				case "stroke-opacity":
					s.setOpacity(Double.parseDouble(value));
					break;
				case "transform":
					List<String> stransforms = splitTransforms(value);
					List<Transform> trans = stransforms.stream().map(strans -> parseTransform(strans)).collect(Collectors.toList());
					s.getTransforms().addAll(trans);
					//System.out.println(s.get);
					break;
				default:
					System.out.println("UNIMPLEMENTED STYLE: "+key+": "+value);
						break;
				}
		}
	}
	
	protected Map<String,String> convertStyle(String style) {
		Map<String,String> styleOptions = new HashMap<>();
		String[] options = style.split(";");
		for(String op:options) {
			String[] keyvalue = op.split(":");
			if(keyvalue.length == 2) {
				//Correct format
				styleOptions.put(keyvalue[0].trim(), keyvalue[1]);
			} else {
				System.out.println("Style Error");
			}
		}
		return styleOptions;
	}
	
	
	
	private void findElements(Element d) {
		
		if(d.hasAttribute("id") && lookfor.contains(d.getNodeName())) {
			elementMap.put(d.getAttribute("id"), d);
			//System.out.println("Found Element with idTag: "+d.getNodeName()+":"+d.getAttribute("id"));
			if(d.getNodeName().equals("linearGradient")) {
				parseLinearGradient(d);
			}
		}
		if(d.hasChildNodes()) {
			NodeList nlChildren = d.getChildNodes();
			for(int i=0; i < nlChildren.getLength(); i++) {
				Node n = nlChildren.item(i);
				if(n instanceof Element) {
					findElements((Element)n);
				}
			}
		}
	}
	
	
}
