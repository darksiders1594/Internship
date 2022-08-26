package pers.internship.demo.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 这是一个用于敏感词过滤的工具类
 * @author Darksiders1594
 * @since 1.8
 */
@Component
public class SensitiveFilter {

    private static final Logger logger = LoggerFactory.getLogger(SensitiveFilter.class);

    // 替代字符
    private static final String ALTERNATE_CHARACTER = "***";

    // 初始化根节点
    private TrieNode rootNode = new TrieNode();

    /**
     * 该方法于 Spring 容器启动时执行, 通过读取敏感词文件来初始化前缀树的节点
     * @see TrieNode
     */
    @PostConstruct
    private void initializeTrieNode() {
        String sensitiveFilePath = Objects.requireNonNull(this.getClass().getClassLoader().getResource("sensitive-words.txt")).toString();
        try  (
                BufferedReader bufferedReader = new BufferedReader(new FileReader(sensitiveFilePath))
                ) {
            String sensitiveWord;
            while ((sensitiveWord = bufferedReader.readLine()) != null) {
                this.addKeyword(sensitiveWord);
            }
        } catch (IOException e) {
            logger.error("敏感词文件读取失败 " + e.getMessage());
        }
    }

    /**
     * 该方法将一个字符串添加到前缀树中
     * @param sensitiveWord 敏感词
     * @see TrieNode
     */
    private void addKeyword(String sensitiveWord) {
        // 以根节点为源, 建立索引节点用于迭代
        TrieNode indexNode = rootNode;

        for (int index = 0; index <= sensitiveWord.length(); index++) {
            char key = sensitiveWord.charAt(index);

            // 尝试获取子节点
            TrieNode subNode = indexNode.getSubNode(key);

            // 空值处理
            if (subNode == null) {
                subNode = new TrieNode();
                indexNode.addSubNode(key, subNode);
            }

            // 索引节点指向子节点, 准备下一次迭代
            indexNode = subNode;

            // 最后一个节点表示迭代结束, 同时代表关键词结束
            if (index == sensitiveWord.length() - 1) {
                subNode.setKeywordEnd(true);
            }
        }
    }

    /**
     * 该内部类实现了前缀树的数据结构
     * @author Darksiders1594
     * @since 1.8
     */
    private static class TrieNode {

        // 关键词结束标识
        private boolean isKeywordEnd = false;

        // key: 子节点代表的字符, value: 子节点
        private Map<Character, TrieNode> subNodes = new HashMap<>();

        // Getter and Setter
        public boolean isKeywordEnd() {
            return isKeywordEnd;
        }
        public void setKeywordEnd(boolean keywordEnd) {
            isKeywordEnd = keywordEnd;
        }

        // Getter and Setter
        public void addSubNode(Character key, TrieNode trieNode) {
            subNodes.put(key, trieNode);
        }
        public TrieNode getSubNode(Character key) {
            return subNodes.get(key);
        }

    }

}
