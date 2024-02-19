import com.google.common.collect.Lists;
import org.apache.commons.collections4.CollectionUtils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 树形数据工具类
 *
 * @author bear-2
 * @date 2024/1/24 20:31
 */

public class TreeUtil {

    /**
     * 树形数据组合
     *
     * @param dataList 待处理数据
     * @return
     */
    public static <U> List<U> build(List<U> dataList, String idName, String pidName, String childrenDataName) {
        // 1、判空
        if (CollectionUtils.isEmpty(dataList)) {
            return Lists.newArrayList();
        }
        try {
            // 2、基本组合
            List<TreeDO<U>> treeDOList = handleData2TreeDO(dataList, idName, pidName);
            // 3、组建
            return handleBuildTree(treeDOList, idName, childrenDataName);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 处理组建
     *
     * @param treeDOList
     * @param childrenCollectionName
     * @param <U>
     * @return
     * @throws NoSuchFieldException
     */
    private static <U> List<U> handleBuildTree(List<TreeDO<U>> treeDOList, String idName, String childrenCollectionName) throws NoSuchFieldException, IllegalAccessException {
        // 待查找的数据，进行剔除，即 pid = 0 或为 NULL 数据
        List<TreeDO<U>> treeDOList4Find = treeDOList.stream()
                .filter(treeDO -> Objects.nonNull(treeDO.getPid()) && !Objects.equals(((Long) treeDO.getPid()).longValue(), 0L))
                .collect(Collectors.toList());
        List<TreeDO<U>> treeDOList4Level1 = (List<TreeDO<U>>) CollectionUtils.removeAll(treeDOList, treeDOList4Find);
        // 创建返回值
        // 1、判断类型
        Field childrenField = treeDOList.get(0).getData().getClass().getDeclaredField(childrenCollectionName);
        if (!Collection.class.isAssignableFrom(childrenField.getType())) {
            throw new RuntimeException("类型必须是集合类型");
        }
        // 2、循环数据创建
        List<U> dataLevel1 = treeDOList4Level1.stream()
                .map(treeDO -> treeDO.getData())
                .collect(Collectors.toList());
        handleBuildTree(dataLevel1, treeDOList4Find, idName, childrenCollectionName);
        // 3、组合数据
        List<U> result = treeDOList4Level1.stream()
                .map(treeDO -> treeDO.getData())
                .collect(Collectors.toList());
        return result;
    }


    /**
     * @param dataListLevelP 上层数据
     * @param dataList4Find  待找数据
     * @param <U>
     * @return
     */
    private static <U> List<U> handleBuildTree(List<U> dataListLevelP, List<TreeDO<U>> dataList4Find, String idName, String childrenCollectionName) throws NoSuchFieldException, IllegalAccessException {
        if (CollectionUtils.isNotEmpty(dataListLevelP)) {
            for (U data : dataListLevelP) {
                // 1、判空
                if (CollectionUtils.isEmpty(dataList4Find)) {
                    return dataListLevelP;
                }

                // 2、查找，找到后删掉待找数据
                List<U> childrenResultList = new ArrayList();
                List<TreeDO<U>> childrenTreeDOList = dataList4Find.stream()
                        .filter(data4Find -> {
                            try {
                                return Objects.equals(data4Find.getPid(), getIdFieldValue(data, idName));
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                        })
                        .peek(data4Find -> childrenResultList.add(data4Find.getData()))
                        .collect(Collectors.toList());
                dataList4Find.removeAll(childrenTreeDOList);
                if (CollectionUtils.isEmpty(childrenResultList)) {
                    continue;
                } else {
                    setFieldCollectionValue(data, childrenCollectionName, handleBuildTree(childrenResultList, dataList4Find, idName, childrenCollectionName));
                }
            }
        }
        return dataListLevelP;
    }

    /**
     * 组件树形数据
     *
     * @param dataList
     * @param idName
     * @param pidName
     * @param <U>
     * @return
     * @throws NoSuchFieldException
     */
    private static <U> List<TreeDO<U>> handleData2TreeDO(List<U> dataList, String idName, String pidName) throws NoSuchFieldException, IllegalAccessException {
        List<TreeDO<U>> treeDOList = new ArrayList(dataList.size());
        for (U data : dataList) {
            TreeDO<U> treeDO = new TreeDO();
            treeDO.setId(getIdFieldValue(data, idName));
            treeDO.setPid(getIdFieldValue(data, pidName));
            treeDO.setData(data);
            treeDOList.add(treeDO);
        }
        return treeDOList;
    }


    /**
     * 获取对象 id 的值
     *
     * @param data
     * @param idName
     * @return
     * @throws NoSuchFieldException
     * @throws IllegalAccessException
     */
    private static Object getIdFieldValue(Object data, String idName) throws NoSuchFieldException, IllegalAccessException {
        Field idField = data.getClass().getDeclaredField(idName);
        idField.setAccessible(true);
        return idField.get(data);
    }

    /**
     * 给某字段设值
     *
     * @param data
     * @param fieldName
     * @param value
     * @throws NoSuchFieldException
     * @throws IllegalAccessException
     */
    private static void setFieldCollectionValue(Object data, String fieldName, Object value) throws NoSuchFieldException, IllegalAccessException {
        Field field = data.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(data, value);
    }

}
