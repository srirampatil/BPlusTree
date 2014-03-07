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
			LeafNode newLeafNode = new LeafNode(keyList.subList(mid, size),
					records.subList(mid, records.size()));

			for (int i = size - 1; i >= mid; i--) {
				keyList.remove(i);
				records.remove(i);
				size--;
			}

			newLeafNode.nextSibling = this.nextSibling;
			
			if(this.nextSibling != null)
				this.nextSibling.prevSibling = newLeafNode;
			
			this.nextSibling = newLeafNode;
			newLeafNode.prevSibling = this;
			newLeafNode.parent = this.parent;

			if (this.parent != null) {
				this.parent.add(newLeafNode.keyAtIndex(0), this, newLeafNode);

			} else {
				IndexNode newRoot = new IndexNode();
				this.parent = newLeafNode.parent = newRoot;

				newRoot.add(newLeafNode.keyAtIndex(0), this, newLeafNode);

				// An issue here for lock-free operation
				if (listener != null)
					listener.rootChanged(newRoot);
			}
		}
	}

	public void redistributeKeys(TreeNode node) {
		int mid = (size + node.size) / 2;

		if (node.size > leafOrder / 2) {
			// Redistribute the next sibling keys
			for (int i = size, j = 0; i < mid; i++, j++) {
				this.keyList.add(i, node.keyAtIndex(j));
				String record = ((LeafNode) node).records.get(j);
				if (i < this.records.size())
					this.records.set(i, record);
				else
					this.records.add(record);
			}

			size = mid;
			for (int i = 0, j = mid; j < node.size; i++, j++) {
				node.keyList.set(i, node.keyAtIndex(j));
				((LeafNode) node).records.set(i,
						((LeafNode) node).records.get(j));
			}
			node.size = node.size - mid;

		} else {
			// Redistribute the current node keys
			for (int i = size - mid + node.size, j = 0; i < size; i++, j++) {
				node.keyList.add(j, this.keyAtIndex(i));
				((LeafNode) node).records.add(j, this.records.get(i));
			}

			node.size = mid;
			size = size - mid;
		}
	}

	public void mergeNode(TreeNode node) {
		for(int i = 0, j = size; i < node.size; i++, j++) {
			this.keyList.add(j, node.keyList.get(i));
			this.records.add(j, ((LeafNode) node).records.get(i));
		}
		
		this.size = this.size + node.size;
	}

	public void delete(int key, String r) {

		int previousKey = this.keyAtIndex(0);

		int keyIndex = this.deleteKeySorted(key);
		records.remove(keyIndex);
		size--;

		int minRequiredKeys = (leafOrder / 2);
		if (size < minRequiredKeys) {
			
			if (nextSibling != null && nextSibling.size > minRequiredKeys  && this.parent == nextSibling.parent) {
				// Redistribute keys from next sibling
				int siblingPreviousKey = nextSibling.keyAtIndex(0);
				redistributeKeys(nextSibling);
				
				
				this.parent.replace(siblingPreviousKey, nextSibling.keyAtIndex(0),
						this, nextSibling);
				
				if(this.parent == this.prevSibling.parent)
					this.parent.replace(previousKey, this.keyAtIndex(0), this.prevSibling, this);

			} else if (prevSibling != null && prevSibling.size > minRequiredKeys && this.parent == prevSibling.parent) {
				// Redistribute keys from previous sibling
				((LeafNode) prevSibling).redistributeKeys(this);
				this.parent.replace(previousKey, this.keyAtIndex(0),
						prevSibling, this);
			
			} else if (nextSibling != null && this.parent == nextSibling.parent) {
				int nextSiblingKey = nextSibling.keyAtIndex(0);
				// Merge with next sibling
				this.mergeNode(nextSibling);
				nextSibling.parent.delete(nextSiblingKey);
				this.parent.replace(previousKey, nextSiblingKey, this.prevSibling, this);
				this.nextSibling = this.nextSibling.nextSibling;
				
				if(this.parent.size == 0)
					this.parent = null;

			} else if(prevSibling != null && this.parent == prevSibling.parent) {
				// Merge with previous sibling
				((LeafNode) prevSibling).mergeNode(this);
				this.parent.delete(previousKey);
				prevSibling.nextSibling = this.nextSibling;

				if(prevSibling.parent.size == 0)
					prevSibling.parent = null;
			}
			
		} else {
			this.parent.replace(previousKey, keyList.get(0), prevSibling, this);
		}
	}
}
