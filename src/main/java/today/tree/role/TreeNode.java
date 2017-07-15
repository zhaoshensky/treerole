package today.tree.role;

import net.sf.json.JSONArray;

/**
* description: treerole的node节点
*
*
* @author today zhaojintian@ediankai.com
* @date 2017年7月15日
* @Company: ediankai
*/
public class TreeNode {

	//当前node节点的上级
	private JSONArray nodeUpperList;
	//所有当前node节点的下级
	private JSONArray nodeLowerList;
	/**
	 * @return the nodeUpperList
	 */
	public JSONArray getNodeUpperList() {
		return nodeUpperList;
	}
	/**
	 * @param nodeUpperList the nodeUpperList to set
	 */
	public void setNodeUpperList(JSONArray nodeUpperList) {
		this.nodeUpperList = nodeUpperList;
	}
	/**
	 * @return the nodeLowerList
	 */
	public JSONArray getNodeLowerList() {
		return nodeLowerList;
	}
	/**
	 * @param nodeLowerList the nodeLowerList to set
	 */
	public void setNodeLowerList(JSONArray nodeLowerList) {
		this.nodeLowerList = nodeLowerList;
	}

	
	
	
}
