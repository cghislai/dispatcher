package com.charlyghislain.dispatcher.api.rendering;

/**
 * Represent the final rendering target.
 */
public enum RenderingMedia {

    /**
     * The message is intended to be rendered on a web page.
     * Inlined html mail images may point to http urls rather than mime attachment reference. This can be used to preview
     * messages or provide a fallback link to users unable to render html in their mail client.
     * // TODO: link attachment image url configuration doc
     */
    WEB_PAGE,

    /**
     * Normal rendering.
     */
    NORMAL;
}
