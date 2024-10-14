package me.ash.reader.ui.component.webview

object WebViewScript {

    fun get(bionicReading: Boolean) = """
function bionicRead() {
    let div = document.body;

    // Check if the input is empty
    if (!div) {
        alert("The element with id 'readability-page-1' does not exist.");
        return;
    }

    // Remove all existing <strong> tags
    let strongTags = div.querySelectorAll('strong');
    strongTags.forEach(tag => {
        let parent = tag.parentNode;
        while (tag.firstChild) {
            parent.insertBefore(tag.firstChild, tag);
        }
        parent.removeChild(tag);
    });

    // Get all text nodes within the div, ignoring <code> elements and their children
    let walker = document.createTreeWalker(div, NodeFilter.SHOW_TEXT, {
        acceptNode: function(node) {
            let parent = node.parentNode;
            while (parent) {
                if (parent.nodeName === 'CODE') {
                    return NodeFilter.FILTER_REJECT;
                }
                parent = parent.parentNode;
            }
            return NodeFilter.FILTER_ACCEPT;
        }
    });

    let textNodes = [];
    while (walker.nextNode()) {
        textNodes.push(walker.currentNode);
    }

    // Regex to match emoji characters
    const emojiRegex = /[\u{1F600}-\u{1F6FF}|\u{1F300}-\u{1F5FF}|\u{1F680}-\u{1F6FF}|\u{1F700}-\u{1F77F}|\u{1F780}-\u{1F7FF}|\u{1F800}-\u{1F8FF}|\u{1F900}-\u{1F9FF}|\u{1FA00}-\u{1FA6F}|\u{1FA70}-\u{1FAFF}|\u{2600}-\u{26FF}|\u{2700}-\u{27BF}|\u{1F1E0}-\u{1F1FF}]/u;

    // Process each text node
    textNodes.forEach(node => {
        let text = node.textContent;

        // Split text into words and process each word
        let words = text.split(/(\s+)/); // Keep spaces in the split
        let formattedText = "";
        words.forEach(word => {
            if (word.trim() && !emojiRegex.test(word)) {
                let halfIndex = Math.round(word.length / 2);
                let half = word.substr(0, halfIndex);
                let remHalf = word.substr(halfIndex);
                formattedText += "<strong>" + half + "</strong>" + remHalf;
            } else {
                formattedText += word; // Preserve spaces and skip emoji
            }
        });

        // Create a temporary div to parse HTML
        let tempDiv = document.createElement('div');
        tempDiv.innerHTML = formattedText;

        // Replace original text node with new HTML content
        while (tempDiv.firstChild) {
            node.parentNode.insertBefore(tempDiv.firstChild, node);
        }
        node.parentNode.removeChild(node);
    });
}

${if (bionicReading) "bionicRead()" else ""}

var images = document.querySelectorAll("img");

images.forEach(function(img) {
    img.onload = function() {
        img.classList.add("loaded");
        console.log("Image width:", img.width, "px");
        if (img.width < 412) {
            img.classList.add("thin");
        }
    };

    img.onerror = function() {
        console.error("Failed to load image:", img.src);
    };
});
"""
}
