package at.co.svc.agate.core.reference.xml;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import at.co.svc.agate.core.reference.ComparisonResult;
import at.co.svc.agate.core.reference.DifferenceType;
import at.co.svc.agate.core.reference.ReferenceCompareConfig;
import at.co.svc.agate.core.reference.ResponseComparator;
import at.co.svc.agate.core.reference.ResponseFormat;
import at.co.svc.agate.core.reference.UnorderedRule;

public class XmlResponseComparator
        implements ResponseComparator {

    private static final int MAX_DIFFERENCES = 100;

    @Override
    public ResponseFormat getFormat() {
        return ResponseFormat.XML;
    }

    @Override
    public String prepareReference(String content)
            throws Exception {

        if (content == null || content.isBlank()) {
            return content;
        }

        Document document = parse(content);

        normalizeDocument(document);

        return prettyPrint(document);
    }

    @Override
    public ComparisonResult compare(
            String reference,
            String actual,
            ReferenceCompareConfig config)
            throws Exception {

        Document referenceDocument =
                parse(reference);

        Document actualDocument =
                parse(actual);

        ReferenceCompareConfig effectiveConfig =
                config != null
                        ? config
                        : ReferenceCompareConfig.empty();

        /*
         * 1. Remove ignored dynamic nodes from both documents.
         */
        applyIgnoredPaths(
                referenceDocument,
                effectiveConfig.getIgnore());

        applyIgnoredPaths(
                actualDocument,
                effectiveConfig.getIgnore());

        /*
         * 2. Normalize whitespace.
         */
        normalizeDocument(referenceDocument);
        normalizeDocument(actualDocument);

        /*
         * 3. Normalize order for explicitly configured collections.
         */
        applyUnorderedRules(
                referenceDocument,
                effectiveConfig.getUnordered());

        applyUnorderedRules(
                actualDocument,
                effectiveConfig.getUnordered());

        /*
         * 4. Compare the resulting DOM trees.
         */
        ComparisonResult result =
                new ComparisonResult();

        compareNodes(
                referenceDocument.getDocumentElement(),
                actualDocument.getDocumentElement(),
                "/" + localName(
                        referenceDocument.getDocumentElement()),
                result);

        return result;
    }

    // =========================================================
    // XML PARSING
    // =========================================================

    private Document parse(String xml)
            throws Exception {

        if (xml == null || xml.isBlank()) {
            throw new IllegalArgumentException(
                    "XML response must not be empty");
        }

        DocumentBuilderFactory factory =
                DocumentBuilderFactory.newInstance();

        factory.setNamespaceAware(true);

        /*
         * Secure XML parsing.
         */
        try {
            factory.setFeature(
                    "http://apache.org/xml/features/disallow-doctype-decl",
                    true);
        } catch (Exception ignored) {
        }

        try {
            factory.setFeature(
                    "http://xml.org/sax/features/external-general-entities",
                    false);
        } catch (Exception ignored) {
        }

        try {
            factory.setFeature(
                    "http://xml.org/sax/features/external-parameter-entities",
                    false);
        } catch (Exception ignored) {
        }

        factory.setXIncludeAware(false);
        factory.setExpandEntityReferences(false);

        DocumentBuilder builder =
                factory.newDocumentBuilder();

        Document document =
                builder.parse(
                        new InputSource(
                                new StringReader(xml.trim())));

        document.getDocumentElement().normalize();

        return document;
    }

    // =========================================================
    // IGNORE
    // =========================================================

    private void applyIgnoredPaths(
            Document document,
            List<String> ignoredPaths)
            throws Exception {

        if (ignoredPaths == null
                || ignoredPaths.isEmpty()) {
            return;
        }

        XPath xpath =
                XPathFactory.newInstance()
                        .newXPath();

        for (String ignoredPath : ignoredPaths) {

            if (ignoredPath == null
                    || ignoredPath.isBlank()) {
                continue;
            }

            NodeList nodes =
                    (NodeList) xpath.evaluate(
                            ignoredPath,
                            document,
                            XPathConstants.NODESET);

            /*
             * NodeList is live in some DOM implementations.
             * Copy nodes before removing them.
             */
            List<Node> nodesToRemove =
                    new ArrayList<>();

            for (int i = 0;
                 i < nodes.getLength();
                 i++) {

                nodesToRemove.add(
                        nodes.item(i));
            }

            for (Node node : nodesToRemove) {

                if (node == null) {
                    continue;
                }

                /*
                 * XPath may select an attribute as well.
                 */
                if (node.getNodeType()
                        == Node.ATTRIBUTE_NODE) {

                    Attr attr = (Attr) node;

                    if (attr.getOwnerElement() != null) {
                        attr.getOwnerElement()
                                .removeAttributeNode(attr);
                    }

                    continue;
                }

                Node parent =
                        node.getParentNode();

                if (parent != null) {
                    parent.removeChild(node);
                }
            }
        }
    }

    // =========================================================
    // UNORDERED LISTS
    // =========================================================

    private void applyUnorderedRules(
            Document document,
            List<UnorderedRule> rules)
            throws Exception {

        if (rules == null
                || rules.isEmpty()) {
            return;
        }

        XPath xpath =
                XPathFactory.newInstance()
                        .newXPath();

        for (UnorderedRule rule : rules) {

            if (rule == null
                    || rule.getPath() == null
                    || rule.getPath().isBlank()) {
                continue;
            }

            NodeList nodes =
                    (NodeList) xpath.evaluate(
                            rule.getPath(),
                            document,
                            XPathConstants.NODESET);

            /*
             * Group matching elements by parent.
             *
             * Usually:
             *
             * <patients>
             *     <patient/>
             *     <patient/>
             * </patients>
             */
            Map<Node, List<Node>> nodesByParent =
                    new LinkedHashMap<>();

            for (int i = 0;
                 i < nodes.getLength();
                 i++) {

                Node node =
                        nodes.item(i);

                if (node == null
                        || node.getParentNode() == null) {
                    continue;
                }

                nodesByParent
                        .computeIfAbsent(
                                node.getParentNode(),
                                key -> new ArrayList<>())
                        .add(node);
            }

            for (Map.Entry<Node, List<Node>> entry
                    : nodesByParent.entrySet()) {

                sortMatchedChildren(
                        entry.getKey(),
                        entry.getValue(),
                        rule,
                        xpath);
            }
        }
    }

    private void sortMatchedChildren(
            Node parent,
            List<Node> matchedNodes,
            UnorderedRule rule,
            XPath xpath)
            throws Exception {

        if (matchedNodes == null
                || matchedNodes.size() <= 1) {
            return;
        }

        /*
         * Remember the location of the first list element.
         */
        Node firstNode =
                matchedNodes.get(0);

        Node marker =
                parent.getOwnerDocument()
                        .createComment(
                                "AGATE_REFERENCE_SORT_MARKER");

        parent.insertBefore(
                marker,
                firstNode);

        List<NodeWithSortKey> sortable =
                new ArrayList<>();

        for (Node node : matchedNodes) {

            String key =
                    determineSortKey(
                            node,
                            rule,
                            xpath);

            sortable.add(
                    new NodeWithSortKey(
                            node,
                            key));
        }

        /*
         * Remove the list items temporarily.
         */
        for (NodeWithSortKey item : sortable) {

            if (item.node().getParentNode()
                    == parent) {

                parent.removeChild(
                        item.node());
            }
        }

        sortable.sort(
                Comparator.comparing(
                        NodeWithSortKey::key,
                        Comparator.nullsFirst(
                                String::compareTo)));

        /*
         * Insert sorted nodes at the original list position.
         */
        for (NodeWithSortKey item : sortable) {

            parent.insertBefore(
                    item.node(),
                    marker);
        }

        parent.removeChild(marker);
    }

    private String determineSortKey(
            Node node,
            UnorderedRule rule,
            XPath xpath)
            throws Exception {

        if (rule.hasMatchBy()) {

            String value =
                    xpath.evaluate(
                            rule.getMatchBy(),
                            node);

            if (value != null
                    && !value.isBlank()) {

                return value.trim();
            }
        }

        /*
         * No matchBy:
         * compare complete semantic content.
         */
        return canonicalSignature(node);
    }

    // =========================================================
    // NORMALIZATION
    // =========================================================

    private void normalizeDocument(
            Document document) {

        if (document == null) {
            return;
        }

        normalizeNode(
                document.getDocumentElement());
    }

    private void normalizeNode(Node node) {

        if (node == null) {
            return;
        }

        NodeList children =
                node.getChildNodes();

        List<Node> remove =
                new ArrayList<>();

        for (int i = 0;
             i < children.getLength();
             i++) {

            Node child =
                    children.item(i);

            if (child.getNodeType()
                    == Node.TEXT_NODE) {

                String value =
                        child.getNodeValue();

                if (value == null
                        || value.trim().isEmpty()) {

                    remove.add(child);

                } else {

                    /*
                     * Existing AGATE BODY assertions also operate
                     * effectively with trimmed values.
                     */
                    child.setNodeValue(
                            value.trim());
                }

            } else {

                normalizeNode(child);
            }
        }

        for (Node child : remove) {
            node.removeChild(child);
        }
    }

    // =========================================================
    // SEMANTIC COMPARISON
    // =========================================================

    private void compareNodes(
            Node expected,
            Node actual,
            String path,
            ComparisonResult result) {

        if (result.getDifferenceCount()
                >= MAX_DIFFERENCES) {
            return;
        }

        if (expected == null
                && actual == null) {
            return;
        }

        if (expected == null) {

            result.addDifference(
                    DifferenceType.UNEXPECTED_ELEMENT,
                    path,
                    "<missing>",
                    describeNode(actual));

            return;
        }

        if (actual == null) {

            result.addDifference(
                    DifferenceType.ELEMENT_MISSING,
                    path,
                    describeNode(expected),
                    "<missing>");

            return;
        }

        if (expected.getNodeType()
                != actual.getNodeType()) {

            result.addDifference(
                    DifferenceType.NODE_TYPE_CHANGED,
                    path,
                    String.valueOf(
                            expected.getNodeType()),
                    String.valueOf(
                            actual.getNodeType()));

            return;
        }

        if (expected.getNodeType()
                == Node.ELEMENT_NODE) {

            compareElements(
                    expected,
                    actual,
                    path,
                    result);

            return;
        }

        if (expected.getNodeType()
                == Node.TEXT_NODE) {

            String expectedValue =
                    safeTrim(
                            expected.getNodeValue());

            String actualValue =
                    safeTrim(
                            actual.getNodeValue());

            if (!Objects.equals(
                    expectedValue,
                    actualValue)) {

                result.addDifference(
                        DifferenceType.VALUE_CHANGED,
                        path,
                        expectedValue,
                        actualValue);
            }
        }
    }

    private void compareElements(
            Node expected,
            Node actual,
            String path,
            ComparisonResult result) {

        String expectedName =
                semanticNodeName(expected);

        String actualName =
                semanticNodeName(actual);

        if (!Objects.equals(
                expectedName,
                actualName)) {

            result.addDifference(
                    DifferenceType.VALUE_CHANGED,
                    path,
                    expectedName,
                    actualName);

            return;
        }

        compareAttributes(
                expected,
                actual,
                path,
                result);

        List<Node> expectedChildren =
                comparableChildren(expected);

        List<Node> actualChildren =
                comparableChildren(actual);

        int max =
                Math.max(
                        expectedChildren.size(),
                        actualChildren.size());

        for (int i = 0; i < max; i++) {

            if (result.getDifferenceCount()
                    >= MAX_DIFFERENCES) {
                return;
            }

            Node expectedChild =
                    i < expectedChildren.size()
                            ? expectedChildren.get(i)
                            : null;

            Node actualChild =
                    i < actualChildren.size()
                            ? actualChildren.get(i)
                            : null;

            String childPath =
                    buildChildPath(
                            path,
                            expectedChild,
                            actualChild,
                            i);

            compareNodes(
                    expectedChild,
                    actualChild,
                    childPath,
                    result);
        }
    }

    private void compareAttributes(
            Node expected,
            Node actual,
            String path,
            ComparisonResult result) {

        Map<String, String> expectedAttributes =
                attributes(expected);

        Map<String, String> actualAttributes =
                attributes(actual);

        for (Map.Entry<String, String> entry
                : expectedAttributes.entrySet()) {

            String attributeName =
                    entry.getKey();

            String expectedValue =
                    entry.getValue();

            if (!actualAttributes
                    .containsKey(attributeName)) {

                result.addDifference(
                        DifferenceType.ATTRIBUTE_MISSING,
                        path + "/@" + attributeName,
                        expectedValue,
                        "<missing>");

                continue;
            }

            String actualValue =
                    actualAttributes.get(
                            attributeName);

            if (!Objects.equals(
                    expectedValue,
                    actualValue)) {

                result.addDifference(
                        DifferenceType.ATTRIBUTE_CHANGED,
                        path + "/@" + attributeName,
                        expectedValue,
                        actualValue);
            }
        }

        for (Map.Entry<String, String> entry
                : actualAttributes.entrySet()) {

            if (!expectedAttributes
                    .containsKey(
                            entry.getKey())) {

                result.addDifference(
                        DifferenceType.UNEXPECTED_ATTRIBUTE,
                        path + "/@" + entry.getKey(),
                        "<missing>",
                        entry.getValue());
            }
        }
    }

    // =========================================================
    // CHILDREN
    // =========================================================

    private List<Node> comparableChildren(
            Node node) {

        List<Node> result =
                new ArrayList<>();

        NodeList children =
                node.getChildNodes();

        for (int i = 0;
             i < children.getLength();
             i++) {

            Node child =
                    children.item(i);

            switch (child.getNodeType()) {

            case Node.ELEMENT_NODE:
                result.add(child);
                break;

            case Node.TEXT_NODE:

                if (child.getNodeValue() != null
                        && !child.getNodeValue()
                                .trim()
                                .isEmpty()) {

                    result.add(child);
                }

                break;

            default:
                /*
                 * Comments, processing instructions etc.
                 * are intentionally not part of API response semantics.
                 */
                break;
            }
        }

        return result;
    }

    private String buildChildPath(
            String parentPath,
            Node expected,
            Node actual,
            int index) {

        Node node =
                expected != null
                        ? expected
                        : actual;

        if (node == null) {
            return parentPath
                    + "/unknown["
                    + (index + 1)
                    + "]";
        }

        if (node.getNodeType()
                == Node.TEXT_NODE) {

            return parentPath
                    + "/text()";
        }

        return parentPath
                + "/"
                + localName(node)
                + "["
                + occurrenceIndex(node)
                + "]";
    }

    private int occurrenceIndex(Node node) {

        if (node == null
                || node.getParentNode() == null) {
            return 1;
        }

        String targetName =
                semanticNodeName(node);

        int index = 0;

        Node sibling =
                node.getParentNode()
                        .getFirstChild();

        while (sibling != null) {

            if (sibling.getNodeType()
                    == Node.ELEMENT_NODE
                    && Objects.equals(
                            semanticNodeName(sibling),
                            targetName)) {

                index++;
            }

            if (sibling == node) {
                return Math.max(index, 1);
            }

            sibling =
                    sibling.getNextSibling();
        }

        return 1;
    }

    // =========================================================
    // ATTRIBUTES
    // =========================================================

    private Map<String, String> attributes(
            Node node) {

        Map<String, String> result =
                new HashMap<>();

        NamedNodeMap attributes =
                node.getAttributes();

        if (attributes == null) {
            return result;
        }

        for (int i = 0;
             i < attributes.getLength();
             i++) {

            Node attribute =
                    attributes.item(i);

            /*
             * xmlns declaration is not application data.
             */
            if (XMLConstants.XMLNS_ATTRIBUTE_NS_URI
                    .equals(
                            attribute.getNamespaceURI())
                    || "xmlns".equals(
                            attribute.getNodeName())
                    || attribute.getNodeName()
                            .startsWith("xmlns:")) {

                continue;
            }

            result.put(
                    semanticNodeName(attribute),
                    safeTrim(
                            attribute.getNodeValue()));
        }

        return result;
    }

    // =========================================================
    // SEMANTIC NAMES
    // =========================================================

    private String semanticNodeName(Node node) {

        if (node == null) {
            return null;
        }

        String localName =
                localName(node);

        String namespace =
                node.getNamespaceURI();

        /*
         * Prefix is intentionally ignored.
         *
         * soap:Envelope and s:Envelope are equal when namespace URI
         * is equal.
         */
        if (namespace == null
                || namespace.isBlank()) {

            return localName;
        }

        return "{"
                + namespace
                + "}"
                + localName;
    }

    private String localName(Node node) {

        if (node == null) {
            return "unknown";
        }

        if (node.getLocalName() != null) {
            return node.getLocalName();
        }

        String name =
                node.getNodeName();

        int colon =
                name.indexOf(':');

        if (colon >= 0) {
            return name.substring(
                    colon + 1);
        }

        return name;
    }

    // =========================================================
    // CANONICAL SORT SIGNATURE
    // =========================================================

    private String canonicalSignature(
            Node node) {

        StringBuilder builder =
                new StringBuilder();

        appendCanonical(
                node,
                builder);

        return builder.toString();
    }

    private void appendCanonical(
            Node node,
            StringBuilder builder) {

        if (node == null) {
            return;
        }

        switch (node.getNodeType()) {

        case Node.ELEMENT_NODE:

            builder.append("<")
                    .append(
                            semanticNodeName(node));

            Map<String, String> attrs =
                    attributes(node);

            attrs.entrySet()
                    .stream()
                    .sorted(
                            Map.Entry.comparingByKey())
                    .forEach(entry ->
                            builder.append(" ")
                                    .append(
                                            entry.getKey())
                                    .append("=")
                                    .append(
                                            entry.getValue()));

            builder.append(">");

            for (Node child
                    : comparableChildren(node)) {

                appendCanonical(
                        child,
                        builder);
            }

            builder.append("</")
                    .append(
                            semanticNodeName(node))
                    .append(">");

            break;

        case Node.TEXT_NODE:

            builder.append(
                    safeTrim(
                            node.getNodeValue()));

            break;

        default:
            break;
        }
    }

    // =========================================================
    // DISPLAY / STORAGE
    // =========================================================

    private String prettyPrint(
            Document document)
            throws Exception {

        TransformerFactory factory =
                TransformerFactory.newInstance();

        try {
            factory.setFeature(
                    XMLConstants.FEATURE_SECURE_PROCESSING,
                    true);
        } catch (Exception ignored) {
        }

        Transformer transformer =
                factory.newTransformer();

        transformer.setOutputProperty(
                OutputKeys.INDENT,
                "yes");

        transformer.setOutputProperty(
                OutputKeys.OMIT_XML_DECLARATION,
                "yes");

        transformer.setOutputProperty(
                "{http://xml.apache.org/xslt}indent-amount",
                "4");

        StringWriter writer =
                new StringWriter();

        transformer.transform(
                new DOMSource(document),
                new StreamResult(writer));

        return writer.toString()
                .trim()
                + System.lineSeparator();
    }

    private String describeNode(Node node) {

        if (node == null) {
            return "<missing>";
        }

        if (node.getNodeType()
                == Node.TEXT_NODE) {

            return safeTrim(
                    node.getNodeValue());
        }

        return "<"
                + localName(node)
                + ">";
    }

    private String safeTrim(String value) {

        return value != null
                ? value.trim()
                : null;
    }

    private record NodeWithSortKey(
            Node node,
            String key) {
    }
}