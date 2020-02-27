package com.jillesvangurp.springdepend;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
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
     * @param root root of the tree
     * @param getChildenFunction function to figure out child nodes for a given root
     * @return acyclic tree following the dependencies from the root.
     * @param <T> node type
     */
    public static <T> SimpleGraph<T> treeBuilder(T root,Function<T,Collection<T>> getChildenFunction) {
        Set<T> simpleGraphs = new HashSet<>();
        SimpleGraph<T> subDependencies = SimpleGraph.buildGraph(new SimpleGraph<T>(), root, getChildenFunction, simpleGraphs);
        SimpleGraph<T> tree = new SimpleGraph<>();
        tree.put(root, subDependencies);
        return tree;
    }

    /**
     * Simple helper to build a acyclic graph.
     * @param parent graph to insert into.
     * @param current current node
     * @param getChildenFunction function to figure out child nodes for a given root
     * @return the graph
     * @param <T> node type
     */
    public static <T> SimpleGraph<T> buildGraph(SimpleGraph<T> parent, T current, Function<T,Collection<T>> getChildenFunction, Set<T> treeCrawlerSet) {
        Collection<T> children = getChildenFunction.apply(current);
        if(children!=null) {
            for(T c : children) {
                SimpleGraph<T> subGraph = parent.get(c);
                if(subGraph==null) {
                    subGraph=new SimpleGraph<T>();
                }
                if(!treeCrawlerSet.contains(c)) {
                    treeCrawlerSet.add(c);
                    parent.put(c, buildGraph(subGraph, c, getChildenFunction, treeCrawlerSet));
                }
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
            if(v.size()>0){
                buf.append(":\n");
                buf.append(v.toStringWithIndent(indent+1));
            } else {
                buf.append("\n");
            }
        });

        return buf.toString();
    }

    @Override
    public String toString() {
        return toStringWithIndent(0);
    }

    public String toCypher(String nodeLabel, String dependencyLabel, Function<T,String> nodeNameFunction) {
        Set<String> nodeCreateStatements = new LinkedHashSet<>();
        Set<String> relationCreateStatements = new LinkedHashSet<>();
        toCypher(Optional.empty(),nodeLabel,dependencyLabel, nodeNameFunction, new HashSet<>(), nodeCreateStatements, relationCreateStatements);
        StringBuilder buf = new StringBuilder();
        nodeCreateStatements.forEach(s -> buf.append(s + '\n'));
        relationCreateStatements.forEach(s -> buf.append(s + '\n'));

        return buf.toString();

    }

    private void toCypher(Optional<T> maybeParent,String nodeLabel, String dependencyLabel, Function<T,String> nodeNameFunction, Set<T> seen, Collection<String> createNodes, Collection<String>createRelations) {

        this.forEach((node,subNodes) -> {
            String nodeName=nodeNameFunction.apply(node);
            if(!seen.contains(node)) {
                seen.add(node);
                createNodes.add(cypherNode(nodeName, nodeLabel));
            }
            if(maybeParent.isPresent()) {
                T parentNode = maybeParent.get();
                createRelations.add(cypherRelation(nodeNameFunction.apply(parentNode), dependencyLabel, nodeName));
            }
            subNodes.toCypher(Optional.of(node),nodeLabel,dependencyLabel,nodeNameFunction,seen,createNodes,createRelations);
        });
    }

    private static String cypherNode(String name, String label) {
        return "CREATE ("+name+":"+label+" {name:\""+name+"\"})";
    }

    private static String cypherRelation(String n1, String label, String n2) {
        return "CREATE ("+n1+")-[:"+label+"]->("+n2+")";
    }
}