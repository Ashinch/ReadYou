package me.ash.reader.infrastructure.html


import net.dankito.readability4j.extended.processor.ArticleGrabberExtended
import net.dankito.readability4j.extended.util.RegExUtilExtended
import net.dankito.readability4j.model.ArticleGrabberOptions
import net.dankito.readability4j.model.ReadabilityOptions
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.nodes.TextNode

open class RYArticleGrabberExtended(options: ReadabilityOptions, regExExtended: RegExUtilExtended) : ArticleGrabberExtended(options, regExExtended) {

    override fun prepareNodes(doc: Document, options: ArticleGrabberOptions): List<Element> {
        val elementsToScore = ArrayList<Element>()
        var node: Element? = doc

        while(node != null) {
            val matchString = node.className() + " " + node.id()

            // Check to see if this node is a byline, and remove it if it is.
            if(checkByline(node, matchString)) {
                node = removeAndGetNext(node, "byline")
                continue
            }

            // Remove unlikely candidates
            if(options.stripUnlikelyCandidates) {
                if(regEx.isUnlikelyCandidate(matchString) &&
                    regEx.okMaybeItsACandidate(matchString) == false &&
                    node.tagName() != "body" &&
                    node.tagName() != "a") {
                    node = this.removeAndGetNext(node, "Removing unlikely candidate")
                    continue
                }
            }

            // Remove DIV, SECTION, and HEADER nodes without any content(e.g. text, image, video, or iframe).
            if((node.tagName() == "div" || node.tagName() == "section" || node.tagName() == "header" ||
                        node.tagName() == "h1" || node.tagName() == "h2" || node.tagName() == "h3" ||
                        node.tagName() == "h4" || node.tagName() == "h5" || node.tagName() == "h6") &&
                this.isElementWithoutContent(node)) {
                node = this.removeAndGetNext(node, "node without content")
                continue
            }

            if(DEFAULT_TAGS_TO_SCORE.contains(node.tagName())) {
                elementsToScore.add(node)
            }

            // Turn all divs that don't have children block level elements into p's
            if(node.tagName() == "div") {
                // Sites like http://mobile.slate.com encloses each paragraph with a DIV
                // element. DIVs with only a P element inside and no text content can be
                // safely converted into plain P elements to avoid confusing the scoring
                // algorithm with DIVs with are, in practice, paragraphs.
                if(this.hasSinglePInsideElement(node)) {
                    val newNode = node.child(0)
                    node.replaceWith(newNode)
                    node = newNode
                    elementsToScore.add(node)
                }
                else if(!this.hasChildBlockElement(node)) {
                    setNodeTag(node, "p")
                    elementsToScore.add(node)
                }
                else {
                    node.childNodes().forEach { childNode ->
                        if(childNode is TextNode && childNode.text().trim().length > 0) {
                            val p = doc.createElement("p")
                            p.text(childNode.text())
                            // EXPERIMENTAL
                            // p.attr("style", "display: inline;")
                            // p.addClass("readability-styled")
                            childNode.replaceWith(p)
                        }
                    }
                }
            }

            node = if(node != null) this.getNextNode(node) else null
        }

        return elementsToScore
    }
}
