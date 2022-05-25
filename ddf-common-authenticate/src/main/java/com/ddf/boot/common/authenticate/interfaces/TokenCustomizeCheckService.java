package com.ddf.boot.common.authenticate.interfaces;

import com.ddf.boot.common.authenticate.model.AuthenticateCheckResult;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.servlet.http.HttpServletRequest;

/**
 * <p>在通用的校验规则上可以实现该接口实现自己的校验规则</p >
 *
 * @author Snowball
 * @version 1.0
 * @date 2022/05/25 09:57
 */
public interface TokenCustomizeCheckService {

    /**
     * 自定义校验规则
     *
     * @param request
     * @param authenticateCheckResult
     */
    void customizeCheck(HttpServletRequest request, AuthenticateCheckResult authenticateCheckResult);


    public class TreeNode {
      int val;
      TreeNode left;
      TreeNode right;
      TreeNode() {}
      TreeNode(int val) { this.val = val; }
      TreeNode(int val, TreeNode left, TreeNode right) {
          this.val = val;
          this.left = left;
          this.right = right;
      }
  }

    class Solution {
        public List<List<Integer>> levelOrderBottom(TreeNode root) {
            if (root == null) {
                return Collections.emptyList();
            }
            List<List<Integer>> list = new ArrayList<>();
            list.add(Collections.singletonList(root.val));

            TreeNode currentNode = root;
            while ((currentNode != null)) {
                currentNode = root.left;
            }
            return null;
        }

        public static void main(String[] args) {
            TreeNode root = new TreeNode(3);
            TreeNode node9 = new TreeNode(9);
            TreeNode node20 = new TreeNode(20);
            TreeNode node15 = new TreeNode(15);
            TreeNode node7 = new TreeNode(7);
            TreeNode node10 = new TreeNode(10);
            TreeNode node11 = new TreeNode(11);

            root.left = node9;
            root.right = node20;

            node20.left = node15;
            node20.right = node7;

            node7.left = node10;
            node10.left = node11;

            List<List<Integer>> ints = new ArrayList<>();
            System.out.println(loopNode(root, ints));
        }

        public static List<List<Integer>> loopNode(TreeNode node, List<List<Integer>> ints) {
            TreeNode currentNode = node;
            List<Integer> currentList = new ArrayList<>();
            while ((node = node.left) != null) {
                currentList.add(currentNode.left.val);
                loopNode(node, ints);
            }
            node = currentNode;
            while ((node = node.right) != null) {
                currentList.add(currentNode.right.val);
                loopNode(node, ints);
            }
            ints.add(currentList);
            return ints;
        }
    }
}
