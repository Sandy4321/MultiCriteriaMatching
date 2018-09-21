/**
 * 
 * This software is released under the licence CeCILL
 * 
 * see LICENSE.TXT
 * 
 * see <http://www.cecill.info/ http://www.cecill.info/
 * 
 * 
 * @copyright IGN
 * 
 * 
 */
package fr.ign.cogit.io;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.geotools.referencing.CRS;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.ibm.icu.text.SimpleDateFormat;

import fr.ign.cogit.dao.LigneResultat;
import fr.ign.cogit.geoxygene.api.spatial.coordgeom.IDirectPosition;
import fr.ign.cogit.geoxygene.api.spatial.coordgeom.ILineString;
import fr.ign.cogit.geoxygene.feature.DefaultFeature;
import fr.ign.cogit.geoxygene.feature.Population;
import fr.ign.cogit.geoxygene.feature.SchemaDefaultFeature;
import fr.ign.cogit.geoxygene.schema.schemaConceptuelISOJeu.AttributeType;
import fr.ign.cogit.geoxygene.schema.schemaConceptuelISOJeu.FeatureType;
import fr.ign.cogit.geoxygene.spatial.coordgeom.DirectPosition;
import fr.ign.cogit.geoxygene.spatial.coordgeom.GM_LineString;
import fr.ign.cogit.geoxygene.spatial.geomaggr.GM_MultiCurve;
import fr.ign.cogit.geoxygene.util.conversion.ShapefileWriter;


/**
 * 
 * @author M-D Van Damme
 */
public class ExportToShape {
	
	
	public static void exportLien (List<LigneResultat> lres, String pathname) {
		
		FeatureType newFeatureType = new FeatureType();
	    newFeatureType.setTypeName("Lien");
	    newFeatureType.setGeometryType(ILineString.class);
	                    
	    AttributeType cleRef = new AttributeType("cleRef", "String");
	    AttributeType cleComp = new AttributeType("cleComp", "String");
	    AttributeType evalIGN = new AttributeType("eval", "Double");
	    AttributeType diffIGN = new AttributeType("diff", "Double");
	    newFeatureType.addFeatureAttribute(cleRef);
	    newFeatureType.addFeatureAttribute(cleComp);
	    newFeatureType.addFeatureAttribute(evalIGN);
	    newFeatureType.addFeatureAttribute(diffIGN);
	        
	    // Création d'un schéma associé au featureType
	    SchemaDefaultFeature schema = new SchemaDefaultFeature();
	    schema.setFeatureType(newFeatureType);
	        
	    newFeatureType.setSchema(schema);
	                    
	    Map<Integer, String[]> attLookup = new HashMap<Integer, String[]>(0);
	    attLookup.put(new Integer(0), new String[] { cleRef.getNomField(), cleRef.getMemberName() });
	    attLookup.put(new Integer(1), new String[] { cleComp.getNomField(), cleComp.getMemberName() });
	    attLookup.put(new Integer(2), new String[] { evalIGN.getNomField(), evalIGN.getMemberName() });
	    attLookup.put(new Integer(3), new String[] { diffIGN.getNomField(), diffIGN.getMemberName() });
	    schema.setAttLookup(attLookup);
	        
	    // Création de la population
	    Population<DefaultFeature> entrees = new Population<DefaultFeature>(false, "LienRando", DefaultFeature.class, true);
	    entrees.setFeatureType(newFeatureType);
	    
	    // Toutes les lignes
	    for (int i = 0; i < lres.size(); i++) {
	    	
	    	LigneResultat ligne = lres.get(i);
	    	
	    	boolean isNA = ligne.getNomTopoComp().equals("NA");
	      
	    	String nomRef = ligne.getIdTopoRef();
	    	String nomComp = ligne.getIdTopoComp();
	      
	    	double pign = ligne.getProbaPignistiquePremier();
	    	double diff = ligne.getProbaPignistiqueSecond();
	      
	    	if (ligne.isDecision().equals("true") && !isNA) {
	            
	    		// On ajoute un lien
	    		List<IDirectPosition> l = new ArrayList<IDirectPosition>();
	    		
	    		GM_LineString geomLigneComp = (GM_LineString)((GM_MultiCurve<?>)ligne.getGeomComp()).get(0);
	    		ILineString geomLigneRef = (ILineString) ligne.getGeomRef();
	    		
	    		l.add(new DirectPosition(geomLigneComp.centroid().getX(), geomLigneComp.centroid().getY()));
	    		l.add(new DirectPosition(geomLigneRef.centroid().getX(), geomLigneRef.centroid().getY()));
	    		GM_LineString lienGeom = new GM_LineString(l);
	              
	            DefaultFeature n = entrees.nouvelElement(lienGeom);
	            n.setSchema(schema);
	            Object[] attributes = new Object[] { nomRef, nomComp , pign, diff };
	            n.setAttributes(attributes);
	              
	            
	          }
	          
	    }
	        
	    
	    try {
	    	CoordinateReferenceSystem crs = CRS.decode("EPSG:2154");
	    	ShapefileWriter.write(entrees, pathname + "-" + new SimpleDateFormat("yyyyMMdd-HHmmssSSS", Locale.FRANCE).format(new Date()) + ".shp", crs);
	    } catch (Exception e) {
	      e.printStackTrace();
	    }
	    
		
	}

}