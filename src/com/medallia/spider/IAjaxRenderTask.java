package com.medallia.spider;

/**
 * Similar to {@link IRenderTask}, but instead of using the 'page'
 * template only the content of the task itself is written to the
 * response. This is useful for rendering fragments in response
 * to AJAX calls.
 * 
 * Classes can inherit {@link AjaxRenderTask}.
 */
public interface IAjaxRenderTask extends ITask {

}
