/*
 wsmo4j - a WSMO API and Reference Implementation

 Copyright (c) 2006, University of Innsbruck, Austria

 This library is free software; you can redistribute it and/or modify it under
 the terms of the GNU Lesser General Public License as published by the Free
 Software Foundation; either version 2.1 of the License, or (at your option)
 any later version.
 This library is distributed in the hope that it will be useful, but WITHOUT
 ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 details.
 You should have received a copy of the GNU Lesser General Public License along
 with this library; if not, write to the Free Software Foundation, Inc.,
 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package org.deri.wsmo4j.common;

import java.util.*;

import org.omwg.logicalexpression.*;
import org.omwg.ontology.*;
import org.wsmo.common.*;
import org.wsmo.common.exception.*;
import org.wsmo.mediator.*;
import org.wsmo.service.*;

/**
 * This utility class cleans a given TopEntity from all its
 * previous definitions (if any).
 *
 * <pre>
 * Created on 10.04.2006
 * Committed by $Author: otajmoua $
 * $Source: /users/cvsroot/QoS-DiscoveryComponent/Workspace/Projects/QoSDiscoveryComponent/src/org/deri/wsmo4j/common/ClearTopEntity.java,v $,
 * </pre>
 *
 * @author Holger Lausen (holger.lausen@deri.org)
 *
 * @version $Revision: 1.1 $ $Date: 2006/08/08 17:20:18 $
 */
public class ClearTopEntity {
    
    
    @SuppressWarnings("unchecked")
	public static void clearTopEntity(Mediator m) throws SynchronisationException, InvalidModelException{
        if (m == null){
            return;
        }
        clearCommonElements(m);
        if (m.listSources()!=null){
            for (Iterator mediatorI = new ArrayList(m.listSources()).iterator(); mediatorI.hasNext();){
                m.removeSource((IRI)mediatorI.next());
            }
        }
        m.setTarget(null);
        m.setMediationService(null);
    }
    
    @SuppressWarnings("unchecked")
	public static void clearTopEntity(ServiceDescription s) throws SynchronisationException, InvalidModelException{
        if (s == null){
            return;
        }
        clearCommonElements(s);
        if (s.listInterfaces() != null){
            for (Iterator interfaceI = new ArrayList(s.listInterfaces()).iterator(); interfaceI.hasNext();){
                Interface i = (Interface)interfaceI.next();
                clearNfp(i.getChoreography());
                clearNfp(i.getOrchestration());
                i.setChoreography(null);
                i.setOrchestration(null);
            }
        }
        Capability cap = s.getCapability();
        if (cap==null){
            return;
        }
        clearCommonElements(cap);
        //shared Variables
        if (cap.listSharedVariables()!=null){
            for (Iterator varI = new ArrayList(cap.listSharedVariables()).iterator();varI.hasNext();){
                cap.removeSharedVariable((Variable)varI.next());
            }
        }
        //assumption
        if (cap.listAssumptions()!=null){
            for (Iterator i = new ArrayList(cap.listAssumptions()).iterator();i.hasNext();){
                Axiom a = (Axiom)i.next();
                clearNfp(a);
                cap.removeAssumption(a);
            }
        }
        if (cap.listEffects()!=null){
            for (Iterator i = new ArrayList(cap.listEffects()).iterator();i.hasNext();){
                Axiom a = (Axiom)i.next();
                clearNfp(a);
                cap.removeEffect(a);
            }
        }
        if (cap.listPostConditions()!=null){
            for (Iterator i = new ArrayList(cap.listPostConditions()).iterator();i.hasNext();){
                Axiom a = (Axiom)i.next();
                clearNfp(a);
                cap.removePostCondition(a);
            }
        }
        if (cap.listPreConditions()!=null){
            for (Iterator i = new ArrayList(cap.listPreConditions()).iterator();i.hasNext();){
                Axiom a = (Axiom)i.next();
                clearNfp(a);
                cap.removePreCondition(a);
            }
        }
        s.setCapability(null);
    }
    
	public static void clearTopEntity(Ontology ont) throws SynchronisationException, InvalidModelException{
        if (ont == null){
            return;
        }
        //clear concepts
        if (ont.listConcepts()!=null){
            for (Iterator conceptI = new ArrayList(ont.listConcepts()).iterator(); conceptI.hasNext(); ){
                Concept concept = (Concept) conceptI.next();
                if (concept.listSuperConcepts()!=null){
                    for (Iterator superI = new ArrayList(concept.listSuperConcepts()).iterator(); superI.hasNext(); ){
                        concept.removeSuperConcept((Concept) superI.next());
                    }
                }
                if (concept.listAttributes()!=null){
                    for (Iterator attrI = new ArrayList(concept.listAttributes()).iterator(); attrI.hasNext(); ){
                        Attribute attr = (Attribute)attrI.next();
                        attr.setConstraining(false);
                        attr.setInverseOf(null);
                        attr.setReflexive(false);
                        attr.setSymmetric(false);
                        attr.setReflexive(false);
                        for (Iterator typesI = attr.listTypes().iterator(); typesI.hasNext();){
                           attr.removeType((Type) typesI.next());
                        }
                        concept.removeAttribute(attr);
                        clearNfp(attr);
                    }
                }
                clearNfp(concept);
                concept.setOntology(null);
            }
        }
        //clear instances
        if (ont.listInstances()!=null){
            for (Iterator instanceI = new ArrayList(ont.listInstances()).iterator(); instanceI.hasNext();){
                Instance instance = (Instance)instanceI.next();
                if (instance.listConcepts()!=null){
                    for (Iterator memberOfI = new ArrayList(instance.listConcepts()).iterator(); memberOfI.hasNext();){
                        instance.removeConcept((Concept)memberOfI.next());
                    }
                }
                if (instance.listAttributeValues()!=null){
                    for (Iterator attributeI = new ArrayList(instance.listAttributeValues().keySet()).iterator(); attributeI.hasNext();){
                        instance.removeAttributeValues((Identifier)attributeI.next());
                    }
                }
                clearNfp(instance);
                instance.setOntology(null);
            }
        }
        
        //clear RelationInstances
        if (ont.listRelationInstances()!=null){
            for (Iterator instanceI = ont.listRelationInstances().iterator();instanceI.hasNext();){
                RelationInstance instance = (RelationInstance) instanceI.next();
                if (instance.listParameterValues()!=null){
                    for (byte i = (byte)(instance.listParameterValues().size()-1); i>=0; i--){
                        instance.setParameterValue(i,null);
                    }
                }
                //instance.setRelation(null);
                clearNfp(instance);
                instance.setOntology(null);
            }
        }

        //clear Relations
        if (ont.listRelations()!=null){
            for (Iterator relI = new ArrayList(ont.listRelations()).iterator();relI.hasNext();){
                Relation rel = (Relation) relI.next();
                if (rel.listParameters()!=null){
                    for (byte i = (byte)(rel.listParameters().size()-1); i>=0; i--){
                        rel.removeParameter(i);
                    }
                }
                if (rel.listSuperRelations()!=null){
                    for (Iterator superI = new ArrayList(rel.listSuperRelations()).iterator();superI.hasNext();){
                        rel.removeSuperRelation((Relation)superI.next());
                    }
                }
                clearNfp(rel);
                rel.setOntology(null);
            }
        }
        
        //clearAxioms
        if(ont.listAxioms()!=null){
            for (Iterator axiomI = new ArrayList(ont.listAxioms()).iterator();axiomI.hasNext();){
                Axiom a = (Axiom)axiomI.next();
                for (Iterator defI = new ArrayList(a.listDefinitions()).iterator(); defI.hasNext();){
                    a.removeDefinition((LogicalExpression)defI.next());
                }
                clearNfp(a);
                a.setOntology(null);
            }
        }

        
        clearCommonElements(ont);
    }

    private static void clearNfp(Entity e) throws SynchronisationException, InvalidModelException{
        if (e!=null && e.listNFPValues()!=null){
            for (Iterator nfpI = e.listNFPValues().keySet().iterator(); nfpI.hasNext(); ){
                e.removeNFP((IRI)nfpI.next());
            }
        }
            
    }
    
    @SuppressWarnings({"unchecked","unchecked", "unchecked"})
	private static void clearCommonElements(TopEntity te) throws SynchronisationException, InvalidModelException{
        //clear importsMediator
        if (te==null){
            return;
        }
        if (te.listMediators()!=null){
            for (Iterator medI = new ArrayList(te.listMediators()).iterator(); medI.hasNext();){
                te.removeMediator((IRI)medI.next());
            }
        }
        //clear namespace
        if (te.listNamespaces()!=null){
            for (Iterator nsI = new ArrayList(te.listNamespaces()).iterator(); nsI.hasNext();){
                te.removeNamespace(((Namespace)nsI.next()));
            }
        }
        //clear importsOntology
        if(te.listOntologies()!=null){
            for (Iterator ontI = new ArrayList(te.listOntologies()).iterator(); ontI.hasNext();){
                te.removeOntology(((Ontology)ontI.next()));
            }
        }
        te.setWsmlVariant(null);
        te.setDefaultNamespace((IRI)null);
        clearNfp(te);
    }


}


/*
 *$Log: ClearTopEntity.java,v $
 *Revision 1.1  2006/08/08 17:20:18  otajmoua
 **** empty log message ***
 *
 *Revision 1.2  2006/04/27 10:34:10  holgerlausen
 *fixed potential null pointer exceptions
 *
 *Revision 1.1  2006/04/11 16:06:59  holgerlausen
 *addressed RFE 1468651 ( http://sourceforge.net/tracker/index.php?func=detail&aid=1468651&group_id=113501&atid=665349)
 *currently the default behaviour of the parser is still as before
 *
 */

