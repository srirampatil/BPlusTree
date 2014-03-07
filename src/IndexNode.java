import java.util.ArrayList;
import java.util.List;

public class IndexNode extends TreeNode {
	public static int indexOrder;

	private List<TreeNode> children;

	public IndexNode() {
		super();
		children = new ArrayList<TreeNode>();
	}

	public IndexNode(List<Integer> subList, List<TreeNode> subList2) {
		super(subList);
		children = new ArrayList<TreeNode>(subList2);
		size = subList.size();
	}

	public TreeNode childForKey(int key) {
		int keyIndex = 0;
		for (; keyIndex < size; keyIndex++)
			if (key <= keyList.get(keyIndex))
				break;

		if (keyIndex == size)
			return children.get(size);

		return (key < keyList.get(keyIndex)) ? children.get(keyIndex)
				: children.get(keyIndex + 1);
	}

	public TreeNode childAtIndex(int index) {
		return children.get(index);
	}

	public void replace(int replaceKey, int newKey, TreeNode lChild,
			TreeNode rChild) {
		if (replaceKey == newKey)
			return;

		int keyIndex = indexForKey(replaceKey);
		if (keyIndex != -1) {
			this.keyList.set(keyIndex, newKey);
			this.children.set(keyIndex, lChild);
			this.children.set(keyIndex + 1, rChild);
		}
	}

	public void add(int key, TreeNode lChild, TreeNode rChild) {
		int keyIndex = indexForKey(key);
		if (keyIndex == -1) {
			keyIndex = addKeySorted(key);

			if (lChild != null) {
				if (keyIndex < children.size())
					children.remove(keyIndex);
				children.add(keyIndex, lChild);
			}

			children.add(keyIndex + 1, rChild);
			size++;

			if (size > indexOrder) {
				int mid = size / 2;
				int parentKey = this.keyAtIndex(mid);

				IndexNode newIndexNode = new IndexNode(keyList.subList(mid + 1,
						size), children.subList(mid + 1, children.size()));

				for (int i = size - 1; i >= mid; i--) {
					keyList.remove(i);
					children.get(i + 1).parent = newIndexNode;
					children.remove(i + 1);
					size--;
				}

				newIndexNode.nextSibling = this.nextSibling;
				if (this.nextSibling != null)
					this.nextSibling.prevSibling = newIndexNode;

				this.nextSibling = newIndexNode;
				newIndexNode.prevSibling = this;
				newIndexNode.parent = this.parent;

				if (this.parent != null) {
					this.parent.add(parentKey, this, newIndexNode);

				} else {
					IndexNode newRoot = new IndexNode();
					this.parent = newIndexNode.parent = newRoot;
					newRoot.add(parentKey, this, newIndexNode);

					// An issue here for lock-free operation
					if (listener != null)
						listener.rootChanged(newRoot);
				}
			}
		}
	}

	public void mergeNode(TreeNode node, int parentKey) {
		if (parentKey != -1) {
			this.keyList.add(this.size, parentKey);
			this.size++;
		}

		for (int i = 0, j = size; i < node.size; i++, j++) {
			this.keyList.add(j, node.keyList.get(i));
			this.children.add(j + 1, ((IndexNode) node).childAtIndex(i));
		}

		this.children.add(((IndexNode) node).childAtIndex(node.size));
		this.size = this.size + node.size;
	}

	public void redistributeKeys(TreeNode node) {
		int mid = (size + node.size) / 2;

		if (node.size > indexOrder / 2) {
			// Redistribute the next sibling keys
			for (int i = size, j = 0; i < mid; i++, j++) {
				this.keyList.add(i, node.keyAtIndex(j));
				if (i < this.children.size())
					this.children
							.set(i + 1, ((IndexNode) node).children.get(j));
				else
					this.children.add(((IndexNode) node).children.get(j));
			}

			size = mid;
			for (int i = 0, j = mid; j < node.size; i++, j++) {
				node.keyList.set(i, node.keyAtIndex(j));
				((IndexNode) node).children.set(i,
						((IndexNode) node).children.get(j));
			}
			node.size = node.size - mid;

		} else {
			// Redistribute the current node keys
			for (int i = size - mid + node.size, j = 0; i < size; i++, j++) {
				node.keyList.add(j, this.keyAtIndex(i));
				((IndexNode) node).children.add(j, this.children.get(i + 1));
				this.children.get(i + 1).parent = (IndexNode) node;
			}

			node.size = mid;
			size = size - mid;
		}
	}

	public void delete(int key) {
		int previousKey = this.keyAtIndex(0);

		int keyIndex = this.deleteKeySorted(key);
		children.remove(keyIndex + 1);
		size--;

		int minRequiredKeys = indexOrder / 2;
		if (size < minRequiredKeys) {
			if (nextSibling != null && nextSibling.size > minRequiredKeys
					&& this.parent == nextSibling.parent) {
				// Redistribute keys from next sibling
				int siblingPreviousKey = nextSibling.keyAtIndex(0);
				redistributeKeys(nextSibling);

				if (this.parent == this.prevSibling.parent)
					this.parent.replace(previousKey, this.keyAtIndex(0),
							this.prevSibling, this);

				this.parent.replace(siblingPreviousKey,
						nextSibling.keyAtIndex(0), this, nextSibling);

			} else if (prevSibling != null
					&& prevSibling.size > minRequiredKeys
					&& this.parent == prevSibling.parent) {
				TreeNode leafNode = this.childAtIndex(0);
				while (leafNode instanceof IndexNode)
					leafNode = leafNode.childAtIndex(0);
				int parentKey = leafNode.keyAtIndex(0);
				
				// Redistribute keys from previous sibling
				((IndexNode) prevSibling).redistributeKeys(this);
				this.parent.replace(parentKey, this.keyAtIndex(0),
						prevSibling, this);
				this.replace(this.keyAtIndex(minRequiredKeys - 1), this.childAtIndex(minRequiredKeys).keyAtIndex(0),
						this.childAtIndex(minRequiredKeys - 1), this.childAtIndex(minRequiredKeys));

			} else if (nextSibling != null && this.parent == nextSibling.parent) {
				TreeNode leafNode = nextSibling;
				while (leafNode instanceof IndexNode)
					leafNode = leafNode.childAtIndex(0);
				int nextSiblingKey = leafNode.keyAtIndex(0);

				// Merge with next sibling
				this.mergeNode(nextSibling, nextSiblingKey);
				this.parent.delete(nextSiblingKey);
				this.nextSibling = this.nextSibling.nextSibling;

			} else if (prevSibling != null && this.parent == prevSibling.parent) {
				TreeNode leafNode = this;
				while (leafNode instanceof IndexNode)
					leafNode = leafNode.childAtIndex(0);
				int parentKey = leafNode.keyAtIndex(0);
				
				// Merge with previous sibling
				((IndexNode) prevSibling).mergeNode(this, parentKey);
				this.parent.delete(parentKey);
				prevSibling.nextSibling = this.nextSibling;
				
				for(int i = 0; i < this.children.size(); i++)
					this.children.get(i).parent = (IndexNode) prevSibling; 

			} else {
				if (this.size == 0 && this.listener != null)
					this.listener
							.rootChanged((this.childAtIndex(0).size > 0) ? this
									.childAtIndex(0) : this.childAtIndex(1));
			}
		}
	}
}
