import java.util.ArrayList;
import java.util.List;

public class IndexNode extends TreeNode {
	public static int indexOrder;
	
	private List<TreeNode> children;

	public IndexNode() {
		super();
		children = new ArrayList<TreeNode>();
	}

	public IndexNode(List<Integer> subList,
			List<TreeNode> subList2) {
		super(subList);
		children = new ArrayList<TreeNode>(subList2);
		size = subList.size();
	}

	public TreeNode childForKey(int key) {
		int keyIndex = 0;
		for(; keyIndex < size; keyIndex++)
			if(key <= keyList.get(keyIndex))
				break;
		
		if(keyIndex == size)
			return children.get(size);
		
		return (key < keyList.get(keyIndex)) ? children.get(keyIndex)
				: children.get(keyIndex + 1);
	}
	
	public TreeNode childAtIndex(int index) {
		return children.get(index);
	}
	
	public void add(int key, TreeNode lChild, TreeNode rChild) {
		int keyIndex = indexForKey(key);
		if(keyIndex == -1) {
			keyIndex = addKeySorted(key);

			if(keyIndex < children.size())
				children.remove(keyIndex);
			children.add(keyIndex, lChild);
			
			children.add(keyIndex + 1, rChild);
			size++;
			
			if(size > indexOrder) {
				int mid = size / 2;
				int parentKey = this.keyAtIndex(mid);
						
				IndexNode newIndexNode = new IndexNode(keyList.subList(mid + 1,
						size), children.subList(mid + 1, children.size()));

				for (int i = size - 1; i >= mid; i--) {
					keyList.remove(i);
					children.remove(i + 1);
					size--;
				}

				newIndexNode.nextSibling = this.nextSibling;
				this.nextSibling = newIndexNode;
				newIndexNode.parent = this.parent; 
				
				if(this.parent != null) {
					this.parent.add(parentKey, this, newIndexNode);
					
				} else {
					IndexNode newRoot = new IndexNode();
					this.parent = newIndexNode.parent = newRoot;
					newRoot.add(parentKey, this, newIndexNode);
					
					// An issue here for lock-free operation
					if(listener != null)
						listener.rootChanged(newRoot);
				}
			}
		}
	}
}
