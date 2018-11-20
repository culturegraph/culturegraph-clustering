package org.culturegraph.clustering.graph.plugin;

import java.io.IOException;
import java.io.Writer;

public class GraphMLExporter {

    private Writer write;
    private boolean lock;

    public GraphMLExporter(Writer  writer) {
        this.write = writer;
        this.lock = false;
    }

    private String declaration() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
    }

    private String openGraphml() {
        return "<graphml xmlns=\"http://graphml.graphdrawing.org/xmlns\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://graphml.graphdrawing.org/xmlns http://graphml.graphdrawing.org/xmlns/1.0/graphml.xsd\">";
    }

    private String closeGraphml() {
        return "</graphml>";
    }

    private String defineKeyLabel() {
        return "<key id=\"d0\" for=\"node\" attr.name=\"label\" attr.type=\"string\"/>";
    }

    private String defineKeyComponent() {
        return "<key id=\"d1\" for=\"all\" attr.name=\"component\" attr.type=\"int\"/>";
    }

    private String openGraph() {
        return "<graph id=\"G\" edgedefault=\"undirected\" parse.order=\"nodesfirst\">";
    }

    private String closeGraph() {
        return "</graph>";
    }

    private void stop() {
        throw new IllegalArgumentException("Can't insert a node after a edge in a nodesfirst graphml document.");
    }

    public void push(String s) throws IOException {
        write.write(s + "\n");
    }

    public void start() throws IOException {
        push(declaration());
        push(openGraphml());
        push(defineKeyLabel());
        push(defineKeyComponent());
        push(openGraph());
    }

    public void addNode(String nodeId) throws IOException {
        if (lock) stop();
        String node = "<node id=\"" + nodeId + "\"/>";
        push(node);
    }

    public void addNode(String nodeId, String label) throws IOException {
        if (lock) stop();
        String node = "<node id=\"" + nodeId + "\"><data key=\"d0\">" + label + "</data></node>";
        push(node);
    }

    public void addNode(String nodeId, String label, int component) throws IOException {
        if (lock) stop();
        String node = "<node id=\"" + nodeId + "\"><data key=\"d0\">" + label + "</data><data key=\"d1\">" + component + "</data></node>";
        push(node);
    }

    public void addEdge(int id, String sourceId, String targetId) throws IOException {
        if (!lock) lock = true;
        String edge = "<edge id=\"e" + id + "\" source=\"" + sourceId + "\" target=\"" +  targetId + "\"></edge>";
        push(edge);
    }

    public void addEdge(int id, String sourceId, String targetId, int component) throws IOException {
        if (!lock) lock = true;
        String edge = "<edge id=\"e" + id + "\" source=\"" + sourceId + "\" target=\"" +  targetId + "\"><data key=\"d1\">" + component + "</data></edge>";
        push(edge);
    }

    public void end() throws IOException {
        push(closeGraph());
        push(closeGraphml());
    }

}
