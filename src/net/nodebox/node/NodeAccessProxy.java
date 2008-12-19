package net.nodebox.node;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * The NodeAccessProxy makes access to nodes and parameters more
 * convenient from expressions. So, instead of writing:
 * <p/>
 * <pre><code>network.getNode("rect1").getParameter("x").getValue()</code></pre>
 * <p/>
 * You can write:
 * <p/>
 * <pre><code>network.rect1.x</code></pre>
 * <p/>
 * The network and node classes in this example are proxies; they look up
 * the node in the nodes dictionary for the network, and return another
 * proxy object.
 */
public class NodeAccessProxy implements Map {

    private Node node;
    private Set<String> keySet;


    public NodeAccessProxy(Node node) {
        this.node = node;
        updateKeys();
    }

    private void updateKeys() {
        keySet = new HashSet<String>();
        // keySet is created in reverse order; from global to local scope.
        // 1. Add nodes
        // 1.1 Add names of the sibling nodes (nodes in the same network as this node.)
        if (node.inNetwork()) {
            for (Node n : node.getNetwork().getNodes()) {
                keySet.add(n.getName());
            }
        }
        // 1.2 If this is a network, add its child nodes.
        if (node instanceof Network) {
            for (Node n : ((Network) node).getNodes()) {
                keySet.add(n.getName());
            }
        }
        // 2. Add parameters
        for (Parameter p : node.getParameters()) {
            keySet.add(p.getName());
        }
        // 3. Add reserved words
        keySet.add("output");
        keySet.add("root");
        keySet.add("network");
    }

    public Node getNode() {
        return node;
    }

    public int size() {
        return keySet.size();
    }

    public boolean isEmpty() {
        return false;
    }

    public boolean containsKey(Object key) {
        if (key == null) return false;
        if (!(key instanceof String)) return false;
        return keySet.contains((String) key);
    }

    public boolean containsValue(Object value) {
        throw new RuntimeException("Not implemented");
    }

    /**
     * Retrieve the node or parameter value from the proxy.
     * Value retrieval happens in a specific order, from local to global:
     * <ul>
     * <li>Search for reserved words (network, root, output).</li>
     * <li>Search for the name in the parameters of the current node.</li>
     * <li>If this node is a network, search in the nodes of the current network. (children)</li>
     * <li>If this node is in a network, search for other nodes in that network. (siblings)</li>
     *
     * @param key the key to search for
     * @return a Parameter value or a NodeProxy object.
     */
    public Object get(Object key) {
        if (key == null) return null;
        String k = key.toString();

        // First search for reserved words.
        if (k.equals("network")) {
            if (node.inNetwork()) {
                return new NodeAccessProxy(node.getNetwork());
            } else {
                return null;
            }
        } else if (k.equals("root")) {
            if (node.inNetwork()) {
                return new NodeAccessProxy(node.getRootNetwork());
            } else {
                return null;
            }
        } else if (k.equals("output")) {
            return node.getOutputValue();
        }

        // Search the parameters
        if (node.hasParameter(k)) {
            return node.getValue(k);
        }

        // Network searches
        // If this is a network, search its nodes first.
        if (node instanceof Network && ((Network) node).contains(k)) {
            return new NodeAccessProxy(((Network) node).getNode(k));
        }

        // Check the siblings (nodes in this node's network).
        if (node.inNetwork() && node.getNetwork().contains(k)) {
            return new NodeAccessProxy(node.getNetwork().getNode(k));
        }

        // Don't know what to return.
        return null;
    }

    public Object put(Object key, Object value) {
        throw new AssertionError("You cannot change the node access proxy.");
    }

    public Object remove(Object key) {
        throw new AssertionError("You cannot change the node access proxy.");
    }

    public void putAll(Map t) {
        throw new AssertionError("You cannot change the node access proxy.");
    }

    public void clear() {
        throw new AssertionError("You cannot change the node access proxy.");
    }

    public Set keySet() {
        return keySet;
    }

    public Collection values() {
        throw new RuntimeException("Not implemented");
    }

    public Set entrySet() {
        throw new RuntimeException("Not implemented");
    }
}