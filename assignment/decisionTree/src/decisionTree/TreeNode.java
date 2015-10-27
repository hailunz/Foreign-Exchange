package decisionTree;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

/**
 * Hailun Zhu
 * ID: hailunz
 * Date: 9/25/15
 */
public class TreeNode implements Serializable {
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

    public void BFS(){
        TreeNode root =this;
        List<List<Integer>> res = new ArrayList<List<Integer>>();
        if (root == null)
            return ;
        TreeNode last = root;
        LinkedList<TreeNode> queue = new LinkedList<>();
        queue.add(root);
        List<Integer> level = new ArrayList<Integer>();
        List<Integer> leaf = new ArrayList<>();
        while(!queue.isEmpty()){
            TreeNode cur = queue.pop();
            if (cur.left!=null)
                queue.add(cur.left);
            if (cur.right!=null)
                queue.add(cur.right);
            if (cur.isLabel)
                leaf.add(cur.label);
            else
                level.add(cur.feature);


            if (cur == last){
                res.add(level);
                level = new ArrayList<Integer>();
                last = queue.peekLast();
            }
        }

        for(List<Integer> l : res){
            for (int i : l) {
                System.out.print(i);
                System.out.print(" ");
            }
            System.out.println();
        }

        for (int i : leaf) {
            System.out.print(i);
            System.out.print(" ");
        }

        System.out.println();



    }



}
