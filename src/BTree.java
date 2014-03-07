import java.util.ArrayDeque;
import java.util.Queue;



public class BTree implements RootChangedListener {
	private TreeNode root = null;
	
	private int height;		// Number of edged from root to leaves
	
	
	public BTree(int _order, int _leafOrder) {
		IndexNode.indexOrder = _order;
		LeafNode.leafOrder = _leafOrder;
		root = new LeafNode();
		root.setRootChangedListner(this);
		height = 0;
	}
	
	public int getHeight() {
		return height;
	}
	
	public boolean contains(int key) {
		TreeNode node = root;
		while(node instanceof IndexNode)
			node = node.childForKey(key);
		
		return (node.indexForKey(key) != -1);
	}
	
	public String valueForKey(int key) {
		TreeNode node = root;
		while(node instanceof IndexNode)
			node = node.childForKey(key);
		
		return node.valueForKey(key);
	}
	
	public void insert(int key, String value) {
		root.insert(key, value);
	}
	
	public void printTree() {
		Queue<TreeNode> nodeQueue = new ArrayDeque<TreeNode>();
		nodeQueue.offer(root);
		
		while(!nodeQueue.isEmpty()) {
			TreeNode node = nodeQueue.poll();
					
			for(int keysIt = 0; keysIt < node.size; keysIt++) {
				System.out.print(node.keyAtIndex(keysIt) + " ");
				if(node instanceof IndexNode)
					nodeQueue.offer(node.childAtIndex(keysIt));
			}
			
			if(node instanceof IndexNode)
				nodeQueue.offer(node.childAtIndex(node.size));
		}
		
		System.out.println();
	}

	@Override
	public void rootChanged(TreeNode newRoot) {
		root.setRootChangedListner(null);
		root = newRoot;
		height++;
		root.setRootChangedListner(this);
	}
	
	public static void main(String[] args) {
		BTree t = new BTree(3, 3);
		t.insert(10, "10");
		t.insert(6, "6");
		t.insert(8, "8");
		t.insert(12, "12");
		t.insert(15, "15");
		t.insert(16, "16");
		t.insert(9, "9");
		t.insert(5, "5");
		t.insert(18, "18");
		t.insert(20, "20");
		t.printTree();
		System.out.println(t.getHeight());
	}
}
