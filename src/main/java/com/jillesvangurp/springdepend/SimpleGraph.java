package com.jillesvangurp.springdepend;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Function;

/**
 * Simple graph abstraction based on LinkedHashMap so we preserve the insertion order.
 */
public class SimpleGraph<T> extends LinkedHashMap<T, SimpleGraph<T>> {
    private static final long serialVersionUID = 2744375702301542235L;

    public SimpleGraph() {
        super();
    }

    /**
     * Build a tree graph from a single root
     * @param root
     * @param getChildenFunction
     * @return acyclic tree following the dependencies from the root.
     */
    public static <T> SimpleGraph<T> treeBuilder(T root,Function<T,Collection<T>> getChildenFunction) {
        SimpleGraph<T> subDependencies = SimpleGraph.buildGraph(new SimpleGraph<T>(), root, getChildenFunction);
        SimpleGraph<T> tree = new SimpleGraph<>();
        tree.put(root, subDependencies);
        return tree;
    }

    /**
     * Simple helper to build a acyclic graph.
     * @param parent graph to insert into.
     * @param current current node
     * @param getChildenFunction function to produce children for the current node
     * @return the graph
     */
    public static <T> SimpleGraph<T> buildGraph(SimpleGraph<T> parent, T current, Function<T,Collection<T>> getChildenFunction) {
        Collection<T> children = getChildenFunction.apply(current);
        if(children!=null) {
            for(T c : children) {
                SimpleGraph<T> subGraph = parent.get(c);
                if(subGraph==null) {
                    subGraph=new SimpleGraph<T>();
                }
                parent.put(c, buildGraph(subGraph, c, getChildenFunction));
            }
        }
        return parent;
    }

    public Map<Integer, Set<T>> getLayers() {
        Map<T, Integer> layerMap = new HashMap<>();
        buildLayers(layerMap, this, 0);

        Map<Integer,Set<T>> layers = new TreeMap<>();
        layerMap.forEach((k, v) -> {
            Set<T> set = layers.get(v);
            if(set==null){
                set=new LinkedHashSet<>();
                layers.put(v, set);
            }
            set.add(k);
        });
        return layers;
    }

    private void buildLayers(Map<T, Integer> layerMap, SimpleGraph<T> current, int depth) {
        current.forEach((k, v) -> {
            Integer kCount = layerMap.get(k);
            if(kCount == null || kCount < depth) {
                layerMap.put(k, depth);
            }
            buildLayers(layerMap, v, depth + 1);
        });
    }

    private String toStringWithIndent(int indent) {
        StringBuilder buf = new StringBuilder();
        forEach((k,v) -> {
            for(int i=0;i<indent;i++) {
                buf.append('\t');
            }
            buf.append(k);
            buf.append(": ");
            buf.append(v.toStringWithIndent(indent+1));
            buf.append("\n");
        });

        return buf.toString();
    }

    @Override
    public String toString() {
        return toStringWithIndent(0);
    }
}