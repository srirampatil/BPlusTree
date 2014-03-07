import java.util.ArrayList;
import java.util.List;

public class TreeNode {
	protected IndexNode parent;
	protected List<Integer> keyList;
	protected int size;
	protected TreeNode nextSibling;
	protected RootChangedListener listener;

	public TreeNode() {
		// This initialization can be changed to change the keys representation
		// from arrays to linked list
		keyList = new ArrayList<Integer>();
	}
	
	
	public TreeNode(List<Integer> keys) {
		// This initialization can be changed to change the keys representation
		// from arrays to linked list
		keyList = new ArrayList<Integer>(keys);
	}
	
	public void setRootChangedListner(RootChangedListener l) {
		listener = l;
	}
	
	public int keyAtIndex(int index) {
		return keyList.get(index);
	}
	
	public int addKeySorted(int key) {
		int i = 0;
		for(; i < size && key > keyList.get(i); i++);
		
		keyList.add(i, key);
		return i;
	}
	
	public int indexForKey(int key) {
		return keyList.indexOf(key);
	}

	public TreeNode childForKey(int key) {
		return ((IndexNode) this).childForKey(key);
	}
	
	public TreeNode childAtIndex(int index) {
		return ((IndexNode) this).childAtIndex(index);
	}
	
	public String valueForKey(int key) {
		return ((LeafNode) this).valueForKey(key);
	}
	
	public void insert(int key, String value) {
		TreeNode node = this;
		while(node instanceof IndexNode)
			node = node.childForKey(key);
		
		((LeafNode) node).insert(key, value);
	}
}
