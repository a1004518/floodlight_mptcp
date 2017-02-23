package net.floodlightcontroller.fdmcalculator;


import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.projectfloodlight.openflow.types.DatapathId;
import org.projectfloodlight.openflow.types.U64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.floodlightcontroller.core.types.NodePortTuple;
import net.floodlightcontroller.forwarding.Forwarding;
import net.floodlightcontroller.linkdiscovery.Link;
import net.floodlightcontroller.routing.Path;
import net.floodlightcontroller.routing.PathId;


class FDMTopology {
	protected static final Logger log = LoggerFactory.getLogger(FDMTopology.class);
	
	//LinkedList<Link> allLinks = new LinkedList<Link>();
	private ArrayList<CustomizedLink> allLinks;// = new ArrayList<CustomizedLink>();
	private int switchesnum;
	//private Set<CustomizedLink> linkset;// = new HashSet<CustomizedLink>();
	private Map<CustomizedLink,Integer> invertlinkmap;
	private Map<PathId,List<LinkedList<Integer>>> adjlinkfromswitch;  // Assume one allocation per node

	private Map<String,CustomizedLink> cuslinksmapping;
	
	//private Float[] req;
	
	Float total_requirement = 0.0f;
	
	
	Integer msgLen = 1;
		
	public FDMTopology(Integer msgLen, Map<DatapathId, Set<Link>> topLinks,Map<String,List<Float>> rule) {
		
		ArrayList<CustomizedLink> cusLinks = new ArrayList<CustomizedLink>();
		Map<String,CustomizedLink> linksmapping = new HashMap<String,CustomizedLink>();

		//invertlinkmap = new HashMap<CustomizedLink,Integer>();

		for(DatapathId s:topLinks.keySet()){
			for(Link link:topLinks.get(s)){
				int currentIndex = cusLinks.size();
				String switchTuple = link.getSrc().toString()+'-'+link.getSrcPort().toString()+'-'+
										link.getDst().toString()+'-'+link.getDstPort();
				CustomizedLink cuslink = null;
				if(rule.containsKey(switchTuple)){
					log.info("find the rules");
					cuslink = new CustomizedLink(link,rule.get(switchTuple).get(1),rule.get(switchTuple).get(0));
				}
				else{
					log.info("failed to find the rules");
					cuslink = new CustomizedLink(link,Float.MAX_VALUE,0.0f);
				}
				cusLinks.add(cuslink);
				linksmapping.put(switchTuple,cuslink);
				//System.out.println(cuslink.toString());
				//this.invertlinkmap.put(cuslink, currentIndex);
			}
		}
		switchesnum = topLinks.keySet().size();
		adjlinkfromswitch = new HashMap<PathId,List<LinkedList<Integer>>>();
		allLinks = cusLinks;
		cuslinksmapping = linksmapping;
		//initRequirements();
	}
	
	
	
	public void addPathtoTopology(List<Path> paths){
		
		for(Path path:paths){
			List<NodePortTuple> nstlist = path.getPath();
			ArrayList<LinkedList<Integer>> ll = null;
			log.info(path.toString());
			if(this.adjlinkfromswitch.containsKey(path.getId())){
				ll = (ArrayList<LinkedList<Integer>>)adjlinkfromswitch.get(path.getId());
			}
			else{
				ll = new ArrayList<LinkedList<Integer>>();
			}
			LinkedList<Integer> l = new LinkedList<Integer>();

			for(int i=1 ; i < nstlist.size()-1; i+=2){

				String switchTuple = nstlist.get(i).getNodeId().toString()+'-'+nstlist.get(i).getPortId().toString()+'-'+nstlist.get(i+1).getNodeId().toString()+'-'+nstlist.get(i+1).getPortId().toString();
				if(cuslinksmapping.containsKey(switchTuple)){
					CustomizedLink link =cuslinksmapping.get(switchTuple);
					int index = allLinks.indexOf(link); 
					if(i==1){
						link.setrequirement(2.0f);
						this.total_requirement +=2.0f;
					}
					System.out.println("find the link"+ link.toString() );
					l.addLast(index);
				}
				else{
					System.out.println("Cannot find "+switchTuple);
					break;
				}
			}
			ll.add(l);
		}
		
	}
	
	private void initRequirements(){
		
	}
	
	
	public List<CustomizedLink> getallLinks(){
		return this.allLinks;
	}
	
	public Map<PathId,List<LinkedList<Integer>>> getadj(){
		return this.adjlinkfromswitch;
	}
	
	public CustomizedLink getCustomizedLink(int index){
		return allLinks.get(index);
	}
	
	public Integer getCustomizedLinkindex(CustomizedLink cl){
		return this.allLinks.indexOf(cl);
	}
	
	public Integer getNoLinks() {
		return allLinks.size();
	}

	public Integer getNoNodes() {
		return switchesnum;
	}


	public void updateCusLink(String nodeTuple,Float req,Float cap){
		if(cuslinksmapping.containsKey(nodeTuple)) {
			CustomizedLink link = cuslinksmapping.get(nodeTuple);
			link.setCapacity(cap);
			link.setrequirement(req);
			System.out.println("find the link"+ link.toString() );
		}
		else{
			System.out.println("Cannot find "+nodeTuple);
		}
		return;
	}
	
//	public void initCapacity(Float[] linkCapacities) {
////		capacity = new Float[getNoLinks()];
//		capacity = linkCapacities;
//	}
//	
//	public void initRequirements(Float [][] a_req) {
//		for(Integer i = 0; i < getNoNodes(); i++) {
//			for(Integer j = 0; j < getNoNodes(); j++) {
//				if (a_req[i][j] > 0) {
//					req[i][j] = a_req[i][j];
//					total_requirement += a_req[i][j];
//				}
//				else {
//					req[i][j] = 0.0f;
//				}
//			}
//		}
//	}
	
//	public void initCapacity(Float[] a_cap) {
//		//System.arraycopy(a_cap, 0, capacity, 0, capacity.length);
//	}

//	public Integer getEnd1(Integer index) {
//		Link currentLink = allLinks.get(index);
//		int node = nodes.indexOf(currentLink.getSrc());
////		System.out.println("End 1 Value: " + node);
//		return node;
//	}

//	public Integer getEnd2(Integer index) {
//		Link currentLink = allLinks.get(index);
//		int node = nodes.indexOf(currentLink.getDst());
////		System.out.println("End 1 Value: " + node);
//		return node;
//	}

	public Float getTotal_requirement() {
		return total_requirement;
	}

//	public LinkedList<Integer>[] getAdj() {
//		return nodeAdjLinks;
//	}
//
//	public Float[] getCapacity() {
//		return capacity;
//	}

	public Integer getMsgLen() {
		return msgLen;
	}
	
	
	
	
}