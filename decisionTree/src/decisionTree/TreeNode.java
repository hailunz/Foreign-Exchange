package decisionTree;

import java.util.Stack;

/**
 * Hailun Zhu
 * ID: hailunz
 * Date: 9/25/15
 */
public class TreeNode {
    // left 0, right 1

    TreeNode left;
    TreeNode right;
    int feature;
    int label;
    boolean isLabel;
    public TreeNode(int f){
        feature = f;
    }

    public TreeNode(int v, boolean is){
        feature = -1;
        isLabel = is;
        label = v;
    }


    public void print(){
        System.out.println("me:"+this.feature
                +"left:" +(this.left!=null? this.left.feature:null)
                +" right:"+(this.right!=null?this.right.feature:null)
                + " isLabel" + this.isLabel);
    }
    public void printTree(){
        Stack<TreeNode> s = new Stack<TreeNode>();
        s.push(this);
        while(!s.isEmpty()){
            TreeNode node = s.pop();
            node.print();
            if (node.right != null){
                s.push(node.right);
            }
            if (node.left != null) {
                s.push(node.left);
            }
        }
    }


}
