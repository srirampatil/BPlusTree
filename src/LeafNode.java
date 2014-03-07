import java.util.ArrayList;
import java.util.List;

public class LeafNode extends TreeNode {
	public static int leafOrder;
	private List<String> records;

	public LeafNode() {
		super();
		records = new ArrayList<String>();
	}

	public LeafNode(List<Integer> subList, List<String> subRecords) {
		super(subList);
		records = new ArrayList<String>(subRecords);
		size = subList.size();
	}

	public String valueForKey(int key) {
		int keyIndex = super.indexForKey(key);
		return (keyIndex == -1) ? null : records.get(keyIndex);
	}

	public void insert(int key, String r) {
		int keyIndex = this.addKeySorted(key);
		records.add(keyIndex, r);
		size++;

		if (size > leafOrder) {
			int mid = size / 2;
			LeafNode newLeafNode = new LeafNode(keyList.subList(mid,
					size), records.subList(mid, records.size()));

			for (int i = size - 1; i >= mid; i--) {
				keyList.remove(i);
				records.remove(i);
				size--;
			}

			newLeafNode.nextSibling = this.nextSibling;
			this.nextSibling = newLeafNode;
			newLeafNode.parent = this.parent;
			
			if(this.parent != null) {
				this.parent.add(newLeafNode.keyAtIndex(0), this, newLeafNode);
				
			} else {
				IndexNode newRoot = new IndexNode();
				this.parent = newLeafNode.parent = newRoot;
				
				newRoot.add(newLeafNode.keyAtIndex(0), this, newLeafNode);
				
				// An issue here for lock-free operation
				if(listener != null)
					listener.rootChanged(newRoot);
			}
		}
	}
}
