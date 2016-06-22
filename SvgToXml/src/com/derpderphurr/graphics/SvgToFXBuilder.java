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
import javafx.scene.paint.Paint;
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
	protected Map<String,Paint> namedFills;
	
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
		
		knownStyleAttribs = Arrays.asList("stroke","stroke-width","stroke-linecap","stroke-miterlimit","stroke-linejoin","stroke-dasharray","fill","fill-rule");
		
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
						s.setStroke(Color.web(value.trim()));
					}
					break;
				case "fill":
					
					if(value.trim().equals("none")) {
						
						s.setFill(Color.TRANSPARENT);
					} else {
						try {
						s.setFill(Color.web(style.get(key)));
						} catch (IllegalArgumentException e) {
							System.err.println("Invalid Color specification: "+style.get(key));
							e.printStackTrace();
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
