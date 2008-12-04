package com.medallia.spider;

/**
 * Abstract class that can be inherited instead of {@link Task}
 * in order to avoid having to also implement {@link IAjaxRenderTask}.
 */
public abstract class AjaxRenderTask extends Task implements IAjaxRenderTask {

}
