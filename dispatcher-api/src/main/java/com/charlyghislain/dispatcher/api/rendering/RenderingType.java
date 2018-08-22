package com.charlyghislain.dispatcher.api.rendering;

/**
 * Specify the type of rendering, which will alter the resources link generated.
 * Only relevant for html mail, where cid: resources are used in mail and url link
 * for web preview.
 */
public enum RenderingType {

    WEB,
    MAIL;
}
