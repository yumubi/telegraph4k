package io.goji.io.goji.telegraph

import io.goji.io.goji.telegraph.models.Node

// Represents a DOM element node.
fun NodeElement(
    tag: String,
    attrs: Map<String, String>? = null,
    children: List<Node>? = null
): Node = NodeElement(tag, attrs, children)


fun String.toNode(): Node = this

