package ch.ehi.ili2db.fromili;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import ch.interlis.ili2c.metamodel.*;

public class ModelElementSelector {
	private TransferDescription td=null;
	private boolean createItfLineTables=false;
	private boolean includeEnums=false;
	public List<Element> getModelElements(List<String> modelNames,TransferDescription td,boolean createItfLineTables,boolean includeEnums)
	{
		this.td=td;
		this.createItfLineTables=createItfLineTables;
		this.includeEnums=includeEnums;
		List<Model> models=new ArrayList<Model>();
		if(modelNames==null || modelNames.isEmpty()){
			models.add(td.getLastModel());
		}else{
			for(String modelName:modelNames){
				Model model=(Model)td.getElement(Model.class, modelName);
				if(model==null){
					throw new IllegalArgumentException("unknown model <"+modelName+">");
				}
				models.add(model);
			}
		}
		HashSet<Element> accu=new HashSet<Element>();
		HashSet<Model> accuScope=new HashSet<Model>();
		for(Model model:models){
			visitModel(accu,accuScope,model);
		}
		visitStructsInScope(accu,accuScope);
		
		ArrayList<Element> ret=new ArrayList<Element>(accu);
		java.util.Collections.sort(ret,new java.util.Comparator<Element>(){
			@Override
			public int compare(Element arg0, Element arg1) {
				if(arg0.getDefidx()<arg1.getDefidx()){
					return -1;
				}
				if(arg0.getDefidx()>arg1.getDefidx()){
					return 1;
				}
				return 0;
			}
		});
		return ret;
	}
	private void visitStructsInScope(HashSet<Element> accu,HashSet<Model> accuScope)
	{
		Iterator<Model> modeli=accuScope.iterator();
		while(modeli.hasNext()){
			Model scope=modeli.next();
			Iterator topici=scope.iterator();
			while(topici.hasNext()){
				Object tObj=topici.next();
				if(tObj instanceof Table && !((Table)tObj).isIdentifiable()){
					Table root=(Table)((Table)tObj).getRootExtending();
					if(root!=null && accu.contains(root)){
						// if tObj is an extension of a known structure, add it
						visitViewable(accu,accuScope,(Table)tObj);
					}
				}
			}
		}
	}
	private void visitModel(HashSet<Element> accu,HashSet<Model> accuScope, Model model)
	{
		if(model.equals(td.INTERLIS)){
			visitDomain(accu,accuScope,td.INTERLIS.BOOLEAN);
			visitDomain(accu,accuScope,td.INTERLIS.HALIGNMENT);
			visitDomain(accu,accuScope,td.INTERLIS.VALIGNMENT);
			return;
		}
		Iterator topici=model.iterator();
		while(topici.hasNext()){
			Object tObj=topici.next();
			if (tObj instanceof Topic){
				Topic topic=(Topic)tObj;
				visitTopic(accu,accuScope,topic);
			}else if(tObj instanceof Viewable){
				visitViewable(accu,accuScope,(Viewable)tObj);
			}else if(tObj instanceof Domain){
				visitDomain(accu,accuScope,(Domain)tObj);
			}
		}
	}
	private void visitTopic(HashSet<Element> visitedElements,HashSet<Model> accuScope,Topic def)
	{
		if(visitedElements.contains(def)){
			return;
		}
		visitedElements.add(def);
		Iterator classi=def.iterator();
		while(classi.hasNext()){
			Object classo=classi.next();
			if(classo instanceof Viewable){
				visitViewable(visitedElements,accuScope,(Viewable)classo);
			}else if(classo instanceof Domain){
				visitDomain(visitedElements,accuScope,(Domain)classo);
			}
		}
		// base topic
		Topic base=(Topic)def.getExtending();
		if(base!=null){
			visitTopic(visitedElements,accuScope,base);
		}
		
		// depends on
		Iterator depi=def.getDependentOn();
		while(depi.hasNext()){
			Topic dep=(Topic)depi.next();
			visitTopic(visitedElements,accuScope,dep);
		}
		
		// parent model
		Model model=(Model)def.getContainer(Model.class);
		if(!accuScope.contains(model)){
			accuScope.add(model);
		}
	}
	private void visitViewable(HashSet<Element> visitedElements,HashSet<Model> accuScope,Viewable def)
	{
		if(visitedElements.contains(def)){
			return;
		}
		visitedElements.add(def);
		
		//	generateViewable(def);
		Iterator attri=def.iterator();
		while(attri.hasNext()){
			Object attro=attri.next();
			if(attro instanceof AttributeDef){
				AttributeDef attr=(AttributeDef)attro;
				Type type=attr.getDomain();
				if(type instanceof CompositionType){
					CompositionType compType=(CompositionType)type;
					visitViewable(visitedElements,accuScope,compType.getComponentType());
					Iterator resti=compType.iteratorRestrictedTo();
					while(resti.hasNext()){
						Viewable rest=(Viewable)resti.next();
						visitViewable(visitedElements,accuScope,rest);
					}
				}else if(type instanceof TypeAlias){
					visitDomain(visitedElements,accuScope,((TypeAlias)type).getAliasing());
				}else if(createItfLineTables && attr.getDomainResolvingAll() instanceof SurfaceOrAreaType){
					visitItfLineTable(visitedElements,accuScope,attr);
				}else if(includeEnums && attr.getDomainResolvingAll() instanceof EnumerationType){
					visitAttributeDef(visitedElements,accuScope,attr);
				}
			}
		}
		// base viewable
		Viewable base=(Viewable)def.getExtending();
		if(base!=null){
			visitViewable(visitedElements,accuScope,base);
		}
	}
	private void visitDomain(HashSet<Element> visitedElements,HashSet<Model> accuScope,Domain def)
	{
		if(def==td.INTERLIS.BOOLEAN){
			// skip it
			return;
		}
		if(visitedElements.contains(def)){
			return;
		}
		visitedElements.add(def);
		//	generateDomain(def);
		if(def.getType() instanceof TypeAlias){
			visitDomain(visitedElements,accuScope,((TypeAlias)def.getType()).getAliasing());
		}
		// base viewable
		Domain base=(Domain)def.getExtending();
		if(base!=null){
			visitDomain(visitedElements,accuScope,base);
		}
	}
	private void visitItfLineTable(HashSet<Element> visitedElements,HashSet<Model> accuScope,AttributeDef def)
	{
		if(visitedElements.contains(def)){
			return;
		}
		visitedElements.add(def);
	}
	private void visitAttributeDef(HashSet<Element> visitedElements,HashSet<Model> accuScope,AttributeDef def)
	{
		if(visitedElements.contains(def)){
			return;
		}
		visitedElements.add(def);
	}
}
