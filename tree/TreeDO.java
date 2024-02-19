import lombok.Data;

/**
 * @author bear-2
 * @date 2024/1/24 20:34
 */

@Data
public class TreeDO<U> {

    private Object id;

    private Object pid;

    private U data;

    public Object getId() {
        return id;
    }

    public void setId(Object id) {
        this.id = id;
    }

    public Object getPid() {
        return pid;
    }

    public void setPid(Object pid) {
        this.pid = pid;
    }

    public U getData() {
        return data;
    }

    public void setData(U data) {
        this.data = data;
    }
}
