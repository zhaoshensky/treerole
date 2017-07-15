package today.tree.role;

import java.util.Iterator;
import net.sf.json.JSONArray;
import today.tree.role.redis.RedisBase;
import today.tree.role.util.SerializeUtil;


/**
* description: TreeRole基础结构
*
*
* @author today zhaojintian@ediankai.com
* @date 2017年7月15日
* @Company: ediankai
*/
public class TreeRole {
	

	private static RedisBase redisBase = new RedisBase();
	private static String redisKey = "test";
	
	/**
     *  treerole 映射结构添加
     * @param parentId：父级ID
     * @param nodeId：nodeID
     * @return rows =0 fail | >0 sucess
     */
    public static Long addTreeRole(Long pNodeId, Long nodeId) throws Exception{
		// TODO Auto-generated method stub
    	
    	//返回结果
    	Long rows = 0l;
    	TreeNode treeNode = new TreeNode();
    	JSONArray arrObj = null;
    	
		if( nodeId > 0 ){
			
			//部门key
			String dRedisKey = nodeId.toString();
			//父亲key
			String pRedisKey = pNodeId.toString();
			
			//第一层节点并且父节点不存在	
			if( pNodeId == 0 && !redisBase.hexists(redisKey,pRedisKey) ){
				//一级节点上级为空
				arrObj = JSONArray.fromObject( "[]" );
				treeNode.setNodeUpperList( arrObj );
				treeNode.setNodeLowerList( arrObj );
				rows = redisBase.hset(redisKey, dRedisKey, SerializeUtil.serialize(treeNode).toString());
				
			}else if( pNodeId > 0 ){
				//不是一级节点
				//此处保证线程安全 添加节点进入映射结构
				arrObj = addSynDep(dRedisKey,pRedisKey,pNodeId);

			}
			//***************当写入场景过多时  此处需要调整   BY Today
			//更新所有上级数据  
			if( arrObj != null){
				
				@SuppressWarnings("rawtypes")
				Iterator iterator = arrObj.iterator();
				
				while( iterator.hasNext() ){
					
					String ppRedisKey = iterator.next().toString();
					adjustAddSynDep(ppRedisKey,nodeId);
					
				}
			}
			
		}
		
		return rows;
		
	}
    
    //线程安全调整
    private static synchronized void adjustAddSynDep(String ppRedisKey,Long nodeId){
    	
    	String ppJsonString = redisBase.hget( redisKey,ppRedisKey );
    	TreeNode treeNode = (TreeNode) SerializeUtil.unserialize( ppJsonString.getBytes() );

		JSONArray ppArrObj0 = treeNode.getNodeLowerList();
    	//将自己添加到上级的下级中
		ppArrObj0.add( nodeId );
		treeNode.setNodeLowerList( ppArrObj0 );
		
		redisBase.hset(redisKey, ppRedisKey, SerializeUtil.serialize(treeNode).toString());
    }
    
    //线程安全添加
    private static synchronized JSONArray addSynDep(String dRedisKey,String pRedisKey,Long parentId){
    	
    	if( redisBase.hexists(redisKey,dRedisKey)  || !redisBase.hexists(redisKey,pRedisKey) ){
			//部门存在 或者 父亲不存在
			return null;
		}
    	
    	TreeNode treeNode = new TreeNode();
    	
		//取出父亲数据
		String jsonString = redisBase.hget( redisKey,pRedisKey );
		TreeNode pTreeNode = (TreeNode)SerializeUtil.unserialize(jsonString.getBytes());
		
		//下级为空
		JSONArray arrObj0 = JSONArray.fromObject( "[]" );
		treeNode.setNodeLowerList( arrObj0 );
		// 父亲的上级
		JSONArray arrObj1 = pTreeNode.getNodeUpperList();
		//追加自己的父亲
		arrObj1.add(parentId);
		treeNode.setNodeUpperList( arrObj1 );
    
		Long rows = redisBase.hset(redisKey, dRedisKey, SerializeUtil.serialize(treeNode).toString());
		if( rows > 0 ){
			//成功 返回自己的上级
			return arrObj1;
		}
		//失败
		return null;
    }
    
    
   /** 映射结构删除
    * @param nodeId：节点ID 
    * @return rows =0 fail | >0 sucess
    */
   public static Long delDepartMent(Long nodeId) throws Exception{
	   
	   	Long rows = 0L;

		//部门key
		String dRedisKey = nodeId.toString();
		//线程删除
		Long dRows = 0L ;
		String jsonString = redisBase.hget( redisKey,dRedisKey );
		if( nodeId > 0 ) dRows = delSynDep( dRedisKey );
		//删除成功
	   	if( dRows > 0 ){
	   		
	   		//取出所有数据
			TreeNode pTreeNodes = (TreeNode) SerializeUtil.unserialize(jsonString.getBytes());
			//所有上级
			JSONArray arrObj1 = pTreeNodes.getNodeUpperList();
			@SuppressWarnings("rawtypes")
			Iterator iterator = arrObj1.iterator();
			while( iterator.hasNext() ){
				
				String ppRedisKey = iterator.next().toString();
				//调整删除节点上级的下级
				adjustDelSynDep(ppRedisKey,dRedisKey);
				

			}

			rows = redisBase.hdel(redisKey, dRedisKey);
	   		
	   	}
	  
	   	return rows;
	   
    }
   
   	//调整删除时上级的下级
   	private static synchronized void adjustDelSynDep( String ppRedisKey,String dRedisKey ){
	   
	   	String ppJsonString = redisBase.hget( redisKey,ppRedisKey );
	   	TreeNode treeNode = (TreeNode) SerializeUtil.unserialize( ppJsonString.getBytes() );
		JSONArray ppArrObj0 = treeNode.getNodeLowerList();
		
		@SuppressWarnings("rawtypes")
		Iterator iterator1 = ppArrObj0.iterator();
		//删除标志
		int delSign = 0;
		
		while( iterator1.hasNext() ){
			String delKey = iterator1.next().toString();
			if( dRedisKey.equals( delKey )  ){
				ppArrObj0 = ppArrObj0.discard(delSign);
				break;
			}
			delSign ++;
		}

		treeNode.setNodeLowerList( ppArrObj0 );
		
		redisBase.hset(redisKey, ppRedisKey, SerializeUtil.serialize(treeNode).toString());
	   
   }
   
   //线程安全删除
   private static synchronized Long delSynDep(String dRedisKey){
   	
	   	Long rows = 0l;   
	    //查询是否存在
	   	if( !redisBase.hexists(redisKey,dRedisKey)  ){
				//部门不存在
				return rows;
		}
	   	//执行删除
	   	rows = redisBase.hdel(redisKey, dRedisKey);
		//返回
		return rows;
   }
   
   
   /** 部门redis 获取数据
	 * @param nodeId：节点ID
	 * @param type：0 部门下级  1 部门上级 
	 * @return JSONArray
	 */
   public static JSONArray getDepartMents( Long nodeId, int type) throws Exception{
	   
	   JSONArray arrObj = null;
 	   //部门key
 	   String dRedisKey = nodeId.toString();
	   
 	   //取出所有上级数据
 	   String jsonString = redisBase.hget( redisKey,dRedisKey );
 	   if( jsonString == null ) return null;
 	   
 	   TreeNode treeNode = (TreeNode) SerializeUtil.unserialize( jsonString.getBytes() );
 	   
 	   switch (type) {
		case 0:
			arrObj = treeNode.getNodeLowerList();
			break;
		case 1:
			arrObj = treeNode.getNodeUpperList();
			break;	
		}
 	   
 	   return arrObj;
   }
   
        
}

