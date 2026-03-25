package com.epam.indigoeln.aws.util;

import lombok.SneakyThrows;
import org.jsoup.Jsoup;
import org.jsoup.helper.W3CDom;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CSPUtil {

    private static final XPath xpath = XPathFactory.newInstance().newXPath();

    @SneakyThrows
    public static String[] buildCsp(Path root) {
        String html = Files.readString(root.resolve("index.html"), StandardCharsets.UTF_8);

        Document doc = new W3CDom().fromJsoup(Jsoup.parse(html));

        Stream<String> styleHashes = Stream.concat(
                extractHashes(doc, "//*[(local-name()='style') or (local-name()='link' and @rel='stylesheet' and @integrity)]"),
                //extractHashes(root, ".css")
                Stream.of()
        );
        Stream<String> scriptHashes = Stream.concat(
                extractHashes(doc, "//*[(local-name()='script') or (local-name()='link' and @rel='modulepreload' and @integrity)]"),
                extractOnloadHashes(doc, "//*")
                //extractHashes(root, ".js")
        );

        return new String[]{formatHashes(styleHashes), formatHashes(scriptHashes)};
    }

    private static String formatHashes(Stream<String> stream) {
        return stream
                .map(x -> "'" + x + "'")
                .sorted()
                .distinct()
                .collect(Collectors.joining(" "));
    }

    private static Stream<String> extractHashes(Path root, String endsWith) throws IOException {
        //noinspection resource
        return Files.walk(root)
                .filter(path -> path.toString().endsWith(endsWith))
                .map(CSPUtil::calculateSHA);
    }

    private static Stream<String> extractHashes(Document doc, String xpath) throws Exception {
        return queryAll(doc, xpath).stream()
                .map(el -> {
                    if (el.hasAttribute("integrity")) {
                        return el.getAttribute("integrity");
                    }
                    return calculateSHA(el.getTextContent());
                });
    }

    private static Stream<String> extractOnloadHashes(Document doc, String xpath) {
        return queryAll(doc, xpath).stream()
                .filter(el -> el.hasAttribute("onload"))
                .map(el -> calculateSHA(el.getAttribute("onload")));
    }

    @SneakyThrows
    private static List<Element> queryAll(Document doc, String expression) {
        NodeList nodes = (NodeList) xpath.evaluate(expression, doc, XPathConstants.NODESET);
        List<Element> list = new ArrayList<>();
        for (int i = 0; i < nodes.getLength(); i++) {
            list.add((Element) nodes.item(i));
        }
        return list;
    }

    private static String calculateSHA(String string) {
        return calculateSHA(string.getBytes(StandardCharsets.UTF_8));
    }

    @SneakyThrows
    private static String calculateSHA(Path file) {
        return calculateSHA(Files.readAllBytes(file));
    }

    @SneakyThrows
    private static String calculateSHA(byte[] bytes) {
        MessageDigest digest = MessageDigest.getInstance("SHA-384");
        byte[] hash = digest.digest(bytes);
        return "sha384-" + Base64.getEncoder().encodeToString(hash);
    }
}
