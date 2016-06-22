package com.derpderphurr.graphics;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Paint;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Circle;
import javafx.scene.shape.FillRule;
import javafx.scene.shape.SVGPath;
import javafx.scene.shape.Shape;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;

public class SvgToFXBuilder {
	protected Map<String,Element> elementMap;
	protected Stack<String> styleStack;
	protected Map<String,Document> docs;
	protected DocumentBuilderFactory dbf;
	protected List<String> lookfor;
	protected List<String> knownStyleAttribs;
	protected Map<String,Paint> namedPaints;
	
	public javafx.scene.Node createInstanceFromId(String id) {
		if(elementMap.containsKey(id)) {
			System.out.println("Generating "+id);
			return generateFX(elementMap.get(id),null);
		}else {
			System.out.println("No ID Found, returning null");
			return null; }
	}
	
	public SvgToFXBuilder() {
		elementMap = new HashMap<>();
		styleStack = new Stack<>();
		docs = new HashMap<>();
		dbf = DocumentBuilderFactory.newInstance();
		lookfor = new ArrayList<>();
		namedPaints = new HashMap<>();
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
		
		knownStyleAttribs = Arrays.asList("stroke","stroke-width","stroke-linecap","stroke-miterlimit","stroke-linejoin","stroke-dasharray","fill","fill-rule","stop-color","stop-opacity");
		
	}
	
	public void loadXML(String name,InputSource s) throws ParserConfigurationException {
		try {
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

	protected Map<String,String> findStyles(Element e) {
		Map<String,String> styles = new HashMap<>();
		if(e.hasAttribute("style")) {
			styles.putAll(convertStyle(e.getAttribute("style")));
		}
		styles.putAll(checkForStyleAttribs(e));
		return styles;
	}
	
	protected Map<String,String> checkForStyleAttribs(Element e) {
		Map<String,String> attrstyles = new HashMap<>();
		knownStyleAttribs.forEach( (attrname) -> { 
			if(e.hasAttribute(attrname)) {
				attrstyles.put(attrname,e.getAttribute(attrname).toString() );
			}
		});
		return attrstyles;
	}
	
	public javafx.scene.Node generateFX(Element e,Map<String,String> style) {
		if(style == null) { style = new HashMap<>(); }
		style.putAll(findStyles(e));
		javafx.scene.Node n = null;
		if( e.getNodeName().equals("g")) {
			if(e.hasChildNodes()){
				javafx.scene.Group g = new javafx.scene.Group();
				NodeList nl = e.getChildNodes();
				for(int i=0; i < nl.getLength(); i++) {
					if(nl.item(i) instanceof Element) {
						javafx.scene.Node ni = generateFX((Element)nl.item(i),style); 
						if(ni != null) {
							g.getChildren().add(ni);
						}
					} else {
						System.out.println("Not an element: "+nl.item(i).getNodeName()+":"+nl.item(i).getNodeValue());
					}
				}
				return g;
			}
		} else if(lookfor.contains(e.getNodeName())) {
			if(e.hasAttribute("style")) {
				style.putAll(convertStyle(e.getAttribute("style")));
			}
			switch(e.getNodeName()) {
			case "circle":
				Circle c = new Circle();
				System.out.println("CIRCLE! "+e.getAttribute("cx")+","+e.getAttribute("cy")+","+e.getAttribute("r"));
				c.setCenterX(Double.parseDouble(e.getAttribute("cx")));
				c.setCenterY(Double.parseDouble(e.getAttribute("cy")));
				c.setRadius(Double.parseDouble(e.getAttribute("r")));
				applyStyle(style, c);
				n = c;
				break;
			case "path":
				System.out.println("PATH!");
				SVGPath p = new SVGPath();
				p.setContent(e.getAttribute("d"));
				applyStyle(style, p);
				n = p;
				break;
			case "linearGradient":
				parseLinearGradient(e);
				break;
			default:
				System.out.println(e.getNodeName()+" has not yet been implemented");
				break;
			}
			if(n != null && e.hasAttribute("id") && e.getAttribute("id") != null && !e.getAttribute("id").isEmpty() ) {
				System.out.println("SVG ID Set , Node id = "+e.getAttribute("id"));
				n.setId(e.getAttribute("id"));
			}
		} else {
			
		}
		
		return n;
	}
	
	private void parseLinearGradient(Element e) {
		List<Stop> lgStops = new ArrayList<>();
		NodeList children = e.getChildNodes();
		for(int i=0; i < children.getLength(); i++) {
			if(children.item(i) instanceof Element && ((Element)children.item(i)).getNodeName().equals("stop")) {
				lgStops.add(parseStop((Element)children.item(i)));
			}
		}
		double x1,x2,y1,y2;
		x1 = Double.parseDouble(e.getAttribute("x1"));
		x2 = Double.parseDouble(e.getAttribute("x2"));
		y1 = Double.parseDouble(e.getAttribute("y1"));
		y2 = Double.parseDouble(e.getAttribute("y2"));
		LinearGradient lg = new LinearGradient(x1, y1, x2, y2, false, CycleMethod.REPEAT, lgStops);
		if( e.hasAttribute("id") && !e.getAttribute("id").isEmpty()) {
			namedPaints.put(e.getAttribute("id"), lg);
		}
	}

	private Stop parseStop(Element item) {
		Color c = null;
		Stop s = null;
		Map<String,String> style = findStyles(item);
		if(style.containsKey("stop-opacity")) {
			c = Color.web(style.get("stop-color").trim(),Double.parseDouble(style.get("stop-opacity")));
		} else {
			c = Color.web(style.get("stop-color").trim());
		}
		System.out.println("New Stop Color: "+c.toString()+" Offset: "+item.getAttribute("offset"));
		s = new Stop(Double.parseDouble(item.getAttribute("offset")),c);
		
		
		return s;
	}

	protected void applyStyle(Map<String,String> style,Shape s) {
		System.out.println("Applying Following Style Parameters to Shape: "+s.getClass().getName());
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
					} else {
						
						if(value.startsWith("url(#")) {
							String id = value.substring(4, value.length()-1);
							s.setFill(namedPaints.get(id));
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
					}
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
			System.out.println("Found Element with idTag: "+d.getNodeName()+":"+d.getAttribute("id"));
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
