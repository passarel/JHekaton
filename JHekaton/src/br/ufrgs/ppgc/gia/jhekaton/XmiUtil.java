package br.ufrgs.ppgc.gia.jhekaton;

import java.util.ArrayList;
import java.util.List;

import br.ufrgs.ppgc.gia.jhekaton.xml.Model.PackagedElement.Region;
import br.ufrgs.ppgc.gia.jhekaton.xml.Model.PackagedElement.Region.Subvertex;
import br.ufrgs.ppgc.gia.jhekaton.xml.Model.PackagedElement.Region.Transition;

/**
 * 
 * Utility class to manipulate XMI State Machine Diagram Structure
 * 
 * @author Igor Montezano - imsantos@inf.ufrgs.br
 *
 */
public class XmiUtil {

	public static List<Subvertex> getStatesByType(Region region, StateType type){
		List<Subvertex> result = new ArrayList<Subvertex>();
		for(Subvertex state : region.getSubvertex()){
			if(state.getType().equalsIgnoreCase(type.getUmlType())){
				result.add(state);
			}
		}
		return result.size() == 0 ? null : result;
	}
	
	public static Subvertex getStateById(Region region, String id){
		for(Subvertex state : region.getSubvertex()){
			if(state.getId().equalsIgnoreCase(id)){
				return state;
			}
		}
		return null;
	}
	
	public List<Transition> getTransitionsFromState(Region region, String id){
		List<Transition> result = new ArrayList<Transition>();
		for(Transition transition : region.getTransition()){
			if(transition.getSource().equalsIgnoreCase(id)){
				result.add(transition);
			}
		}
		return result.size() == 0 ? null : result;
		
	}
	
	public List<Transition> getTransitionsToState(Region region, String id){
		List<Transition> result = new ArrayList<Transition>();
		for(Transition transition : region.getTransition()){
			if(transition.getTarget().equalsIgnoreCase(id)){
				result.add(transition);
			}
		}
		return result.size() == 0 ? null : result;
		
	}

	
}
