package pers.internship.demo.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 该实体类用于封装有关分页的信息
 */
@Getter
@ToString
public class Page {

    // 当前页码
    private int current = 1;

    // 显示上限
    private int limit = 10;

    // 数据总数(用于计算总页数)
    private int rows;

    // 查询路径(用于复用分页链接)
    @Setter
    private String path;

    // 效验用户对页码的输入
    public void setCurrent(int current) {
        if (current >= 1) {
            this.current = current;
        }
    }

    // 效验用户对显示上限的输入
    public void setLimit(int limit) {
        if (limit >= 1) {
            this.limit = limit;
        }
    }

    // 效验数据总数
    public void setRows(int rows) {
        if (rows >= 0) {
            this.rows = rows;
        }
    }

    /**
     * 该方法用于获取起始行
     * @return 返回一个整数类型
     */
    public int getOffset() {
        int total = getTotal();
        if (current >= total) {
            return (total - 1) * limit;
        }
        return (current - 1) * limit;
    }

    /**
     * 该方法用于获取总页数
     * @return 返回一个整数类型
     */
    public int getTotal() {
        if (rows % limit == 0) {
            return rows / limit;
        } else {
            return rows / limit + 1;
        }
    }

    /**
     * 该方法用于获取起始页码
     * @return 返回一个整数类型
     */
    public int getFrom() {
        int from = current - 2;
        int total = getTotal();
        if ((current + 2) >= total) {
            from =  total - 4;
        }
        return Math.max(from, 1);
    }

    /**
     * 该方法用于获取结束页码
     * @return 返回一个整数类型
     */
    public int getTo() {
        int to = current + 2;
        int total = getTotal();
        if (total >= 5 && current < 3) {
            return 5;
        }
        return Math.min(to, total);
    }
}
