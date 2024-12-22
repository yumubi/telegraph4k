package io.goji.telegraph

import io.goji.io.goji.telegraph.models.Node
import io.goji.io.goji.telegraph.models.NodeElement
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.jsoup.nodes.TextNode

object HTMLParser {
    fun parseHTML(html: String): List<Node> {
        val document = Jsoup.parse(html)
        return document.body().childNodes().map { parseNode(it) }
    }

    private fun parseNode(node: org.jsoup.nodes.Node): Node = when (node) {
        is TextNode -> node.text()
        is Element -> NodeElement(
            tag = node.tagName(),
            attrs = if (node.attributes().size() > 0) {
                node.attributes().associate { it.key to it.value }
            } else null,
            children = if (node.childNodes().isNotEmpty()) {
                node.childNodes().map { parseNode(it) }
            } else null
        )
        else -> ""
    }
}
